/**
 *
 * Copyright 2013-2014 UPnP Forum All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE OR WARRANTIES OF 
 * NON-INFRINGEMENT, ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are 
 * those of the authors and should not be interpreted as representing official 
 * policies, either expressed or implied, by the UPnP Forum.
 *
 **/
package com.comarch.android.upnp.ibcdemo.connectivity.local;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

import android.util.Log;

import com.comarch.android.upnp.ibcdemo.busevent.ConnectionStateChangedEvent.ConnectionState;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.LocalConnectionStateChangedEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.common.BaseSensorPooling.SensorPoolingObserver;
import com.comarch.android.upnp.ibcdemo.connectivity.common.UpnpActionCallback;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingAddUpnpDeviceEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingDevicePropertyChanged;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingRemoveUpnpDeviceEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingRequestRefreshEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingRequestRereadEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingServiceActionEvent;
import com.comarch.android.upnp.ibcdemo.model.DeviceUpnp;
import com.comarch.android.upnp.ibcdemo.model.Sensor;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;
import com.comarch.android.upnp.ibcdemo.util.deliverer.ServiceBusDeliverer;

import de.greenrobot.event.EventBus;

public class LocalUpnpService extends AndroidUpnpServiceImpl implements SensorPoolingObserver {

    public final static String SERVICE_TYPE_SWITCH = "SwitchPower";

    public final static String SWITCHED_GET_ACTION = "GetStatus";

    public final static String SWITCHED_GET_ACTION_PARAM = "ResultStatus";
    public final static String SWITCHED_SET_ACTION_PARAM = "NewTargetValue";

    public final static String SERVICE_TYPE_DIMMING = "Dimming";

    public final static String DIMMING_GET_ACTION = "GetLoadLevelStatus";
    public final static String DIMMING_SET_ACTION = "SetLoadLevelTarget";

    public final static String DIMMING_GET_ACTION_PARAM = "retLoadlevelStatus";
    public final static String DIMMING_SET_ACTION_PARAM = "newLoadlevelTarget";

    public final static String SERVICE_TYPE_AVTRANSPORT = "AVTransport";
    
	private final int MIN_REFRESH_INTERVAL = 5000; /* 5s */
	private long lastRefreshTime = 0;
    
    private final String TAG = getClass().getSimpleName();

    private final ServiceBusDeliverer mAdapter = new ServiceBusDeliverer(this);

    private final LocalRegistryListener listener = new LocalRegistryListener(this);
    private LocalUpnpDeviceFactory deviceFactory;
    private LocalSensorPooling mSensorPooling;
    
    @Override
    protected UpnpServiceConfiguration createConfiguration() {
        return new AndroidUpnpServiceConfiguration() {

            @Override
            public int getRegistryMaintenanceIntervalMillis() {
                return 7000;
            }
/*
            @Override
            public ServiceType[] getExclusiveServiceTypes() {
                return new ServiceType[] { new UDAServiceType("SwitchPower"), new UDAServiceType("Dimming"), new UDAServiceType("AVTransport") };
            }
            */
        };
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onCreate() {
        Log.d(TAG, "Upnp service started");
        super.onCreate();
        getBus().postSticky(new LocalConnectionStateChangedEvent(ConnectionState.CONNECTED));
        deviceFactory = new LocalUpnpDeviceFactory(upnpService,getBus());
        listener.setService(upnpService);
        upnpService.getRegistry().addListener(listener);
        getBus().register(this);

        for (Device device : upnpService.getRegistry().getDevices()) {
            listener.deviceAdded(device);
        }

        mSensorPooling = new LocalSensorPooling(getBus());
        mSensorPooling.setObserver(this);
        mSensorPooling.start();
        refresh();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Upnp service stopped");
        super.onDestroy();
        mSensorPooling.stop();
        getBus().unregister(this);
        getBus().postSticky(new LocalConnectionStateChangedEvent(ConnectionState.DISCONNECTED));
        upnpService.getRegistry().removeListener(listener);
    }

    public void onEvent(ClingRequestRefreshEvent event) {
    	long now = System.currentTimeMillis();
    	long diff = now - lastRefreshTime;
    	if (diff > MIN_REFRESH_INTERVAL) {
    		lastRefreshTime = now;
    		refresh();
    		Log.i(TAG, "Refreshing.");
    	}
    }

    @SuppressWarnings("rawtypes")
    public void onEvent(ClingRequestRereadEvent event) {
        for (Device device : upnpService.getRegistry().getDevices()) {
            onDeviceUpdate(device);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void onEvent(final ClingServiceActionEvent event){
        DeviceUpnp eventDevice = event.getDevice();

        Device device = upnpService.getRegistry().getDevice(new UDN(eventDevice.getUuid()), true);
        Service<Device, Service> service = device.findService(new ServiceType(event.getService().getNamespace(),event.getService().getName()));
        Action action = service.getAction(event.getActionName());
        ActionInvocation<Service> actionInvocation = new ActionInvocation(action);
        for(Entry<String, String> entry : event.getArgs().entrySet()){
        	if(action.getInputArgument(entry.getKey())!=null){
        		actionInvocation.setInput(entry.getKey(), entry.getValue());
        	}else{
        		Log.e(TAG, "Invalid action invocation param: "+entry.getKey());
        	}
        		
        }
        
        ActionCallback actionCallback = new ActionCallback(actionInvocation){

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse operation,
					String defaultMessage) {
				Log.e(TAG, defaultMessage);
				UpnpActionCallback clb = event.getCallback();
				if(clb!=null && clb.isSupportsFail()){
					clb.setFailed(true);
					clb.run();
				}
				
			}

			@Override
			public void success(ActionInvocation invocation) {
				ActionArgumentValue[] output = invocation.getOutput();
				UpnpActionCallback clb = event.getCallback();
				if(clb!=null){
					Map<String,Object> map = new HashMap<String,Object>();
					for(ActionArgumentValue aav : output){
						map.put(aav.getArgument().getName(), aav.getValue());
					}
					clb.setResponse(map);
					clb.run();
				}
			}
        	
        };
        upnpService.getControlPoint().execute(actionCallback);
        //new ActionCallback.Default(actionInvocation, upnpService.getControlPoint()).run();
        
    }

    private EventBus getBus() {
        return mAdapter.getBus();
    }

    @SuppressWarnings("rawtypes")
    protected void onDeviceAdded(Device device) {
    	List<SourcedDeviceUpnp> modelDevices = deviceFactory.create(device);
    	if(modelDevices!=null && modelDevices.size()>0){
    		for(SourcedDeviceUpnp model : modelDevices){
    			getBus().post(new ClingAddUpnpDeviceEvent(model));
    			if(model instanceof Sensor){
        			mSensorPooling.addSensor((Sensor) model);
        		}
    		}
    		
    	}
    }

    @SuppressWarnings("rawtypes")
    protected void onDeviceUpdate(Device device) {
        onDeviceAdded(device);
    }

    @SuppressWarnings("rawtypes")
    protected void onDeviceRemoved(Device device) {
    	
        String name = device.getDetails().getFriendlyName();
        String uuid = device.getIdentity().getUdn().getIdentifierString();
        SourcedDeviceUpnp deviceupnp = null;
        if(device.getType().implementsVersion(new UDADeviceType("SensorManagement"))){
        	deviceupnp = new Sensor(uuid, name, null);
        	mSensorPooling.removeSensor((Sensor) deviceupnp);
        }else{
        	deviceupnp = new SourcedDeviceUpnp(uuid, name);
        }
        getBus().post(new ClingRemoveUpnpDeviceEvent(deviceupnp));
    }

    private void refresh() {
        upnpService.getControlPoint().search();
    }

	@Override
	public void onDevicePropertiesChanged(DeviceUpnp device,Map<String, Object> changes) {
		getBus().post(new ClingDevicePropertyChanged(device,changes));		
	}

}
