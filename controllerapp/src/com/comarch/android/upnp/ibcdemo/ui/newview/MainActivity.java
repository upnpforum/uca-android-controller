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

/**
 * 
 */
package com.comarch.android.upnp.ibcdemo.ui.newview;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.comarch.android.upnp.ibcdemo.R;
import com.comarch.android.upnp.ibcdemo.busevent.DeviceListRefreshRequestEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.LocalConnectionRequestEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.XmppConnectionCloseRequestEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.XmppConnectionOpenRequestEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.data.UpdateDeviceListEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.data.UpdateDeviceProperty;
import com.comarch.android.upnp.ibcdemo.connectivity.common.UpnpActionCallback;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppChatMessageRecivedEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppExceptionEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data.DeviceDescription;
import com.comarch.android.upnp.ibcdemo.model.ControlPoint;
import com.comarch.android.upnp.ibcdemo.model.Credentials;
import com.comarch.android.upnp.ibcdemo.model.DeviceUpnpCollection;
import com.comarch.android.upnp.ibcdemo.model.DimmableLight;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;
import com.comarch.android.upnp.ibcdemo.model.MediaServer;
import com.comarch.android.upnp.ibcdemo.model.Sensor;
import com.comarch.android.upnp.ibcdemo.model.SensorTemperature;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;
import com.comarch.android.upnp.ibcdemo.model.interfaces.IDimmableLight;
import com.comarch.android.upnp.ibcdemo.persistence.ChatDao;
import com.comarch.android.upnp.ibcdemo.persistence.ControlPointDao;
import com.comarch.android.upnp.ibcdemo.persistence.CredentialsPersistence;
import com.comarch.android.upnp.ibcdemo.persistence.DimmableLightDao;
import com.comarch.android.upnp.ibcdemo.persistence.MediaRendererDao;
import com.comarch.android.upnp.ibcdemo.persistence.MediaServerDao;
import com.comarch.android.upnp.ibcdemo.persistence.SensorsDao;
import com.comarch.android.upnp.ibcdemo.ui.Broker;
import com.comarch.android.upnp.ibcdemo.ui.SettingsActivity;
import com.comarch.android.upnp.ibcdemo.ui.newview.busevents.NotifyDeviceListChangedEvent;
import com.comarch.android.upnp.ibcdemo.ui.newview.busevents.UICurrentDevice;
import com.comarch.android.upnp.ibcdemo.util.deliverer.ActivityWithBusDeliverer;

public class MainActivity extends ActivityWithBusDeliverer implements Callback{

    private final static String TAG = MainActivity.class.getName();
    
    enum CurrentState {
        MAIN_VIEW,
        MEDIA_SERVER,
        MEDIA_RENDERER,
        DIMMABLE_LIGHT,
        CONTROL_POINT,
        TEMPERATURE
    }
    
    private Broker mBroker;

    private MainViewFragment mFragmentMain;
    private MediaServerFragment mFragmentMediaServer;
    private MediaRendererFragment mFragmentMediaRenderer;
    private DimmableLightFragment mFragmentDimmableLight;
    private ControlPointFragment mFragmentControlPoint;
    private TemperatureSensorFragment mTemperatureSensor;
    
    private CurrentState mCurrentState;
    private DeviceUpnpCollection mUpnpCollection;

    private MediaRendererDao mediaRendererDao;
    private MediaServerDao mediaServerDao;
    private DimmableLightDao dimmableLightDao;
    private ControlPointDao contronPointDao;
    private SensorsDao sensorsDao;
    private ChatDao chatDao;

    private String defaultMediaRendererKey;
    private SourcedDeviceUpnp currentDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_bulbulator);

        mFragmentMain = new MainViewFragment();
        mFragmentMediaServer = new MediaServerFragment();
        mFragmentMediaRenderer = new MediaRendererFragment();
        mFragmentDimmableLight = new DimmableLightFragment();
        mFragmentControlPoint = new ControlPointFragment();
        mTemperatureSensor = new TemperatureSensorFragment();
        
        mCurrentState = CurrentState.MAIN_VIEW;
        
        mBroker = new Broker(getBus());
        
        mUpnpCollection = new DeviceUpnpCollection();

        mediaRendererDao = new MediaRendererDao(this);
        mediaServerDao = new MediaServerDao(this);
        dimmableLightDao = new DimmableLightDao(this);
        contronPointDao = new ControlPointDao(this);
        sensorsDao = new SensorsDao(this);
        chatDao = new ChatDao(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        mediaServerDao.open();
        mediaRendererDao.open();
        dimmableLightDao.open();
        contronPointDao.open();
        sensorsDao.open();
        
        try {
            Iterable<MediaRenderer> mediaRenderes = mediaRendererDao.getAll();
            Iterable<MediaServer> mediaServers = mediaServerDao.getAll();
            Iterable<DimmableLight> dimmableLight = dimmableLightDao.getAll();
            Iterable<ControlPoint> controlPoints = contronPointDao.getAll();
            Iterable<Sensor> sensors = sensorsDao.getAll();
            
            mUpnpCollection.addAll(mediaRenderes, MediaRenderer.class);
            mUpnpCollection.addAll(mediaServers, MediaServer.class);
            mUpnpCollection.addAll(dimmableLight, DimmableLight.class);
            mUpnpCollection.addAll(controlPoints,ControlPoint.class);
            mUpnpCollection.addAll(sensors,Sensor.class);
            
            mUpnpCollection.removeOther(mediaRenderes, MediaRenderer.class);
            mUpnpCollection.removeOther(mediaServers, MediaServer.class);
            mUpnpCollection.removeOther(dimmableLight, DimmableLight.class);
            mUpnpCollection.removeOther(controlPoints,ControlPoint.class);
            mUpnpCollection.removeOther(sensors,Sensor.class);
            
            updateControlPointsStatus();
        } finally {
            mediaServerDao.close();
            mediaRendererDao.close();
            dimmableLightDao.close();
            contronPointDao.close();
            sensorsDao.close();
        }

        goToCurrentFragment();

        getBus().registerSticky(this);
        getBus().postSticky(new DeviceListRefreshRequestEvent());
    }

    @Override
    public void onPause() {
        super.onPause();
        mediaServerDao.open();
        mediaRendererDao.open();
        dimmableLightDao.open();
        contronPointDao.open();
        sensorsDao.open();
        try {
            for (SourcedDeviceUpnp device : mUpnpCollection.allDevices()) {
            	if(device instanceof MediaRenderer){
            		mediaRendererDao.insertOrUpdate((MediaRenderer)device);
            	}else if(device instanceof MediaServer){
            		mediaServerDao.insertOrUpdate((MediaServer)device);
            	}else if(device instanceof DimmableLight){
            		dimmableLightDao.insertOrUpdate((DimmableLight)device);
            	}else if(device instanceof ControlPoint){
            		contronPointDao.insertOrUpdate((ControlPoint)device);
            	}else if(device instanceof Sensor){
            		sensorsDao.insertOrUpdate((Sensor) device);
            	}
            }
        } finally {
            mediaServerDao.close();
            mediaRendererDao.close();
            dimmableLightDao.close();
            contronPointDao.close();
            sensorsDao.close();
        }

        getBus().unregister(this);
    }
    @Override
    public void onBackPressed() {
        switch(mCurrentState){
        case MEDIA_SERVER:
            mFragmentMediaServer.onBackPressed();
            break;
        case MEDIA_RENDERER:
        	mFragmentMediaRenderer.onBackPressed();
        	break;
        case CONTROL_POINT:
        case DIMMABLE_LIGHT:
        case TEMPERATURE:
            returnToMainScreen();
            break;
        case MAIN_VIEW:
            super.onBackPressed();
            break;
            
        }
    }

    public void onEventMainThread(XmppExceptionEvent ev){
        Toast.makeText(this, ev.getMessage(), Toast.LENGTH_LONG).show();
    }
    public void onEventMainThread(UpdateDeviceListEvent ev) {
        List<SourcedDeviceUpnp> deviceList = ev.getDeviceList();
        mUpnpCollection.setAllNotAvailable();
        mUpnpCollection.addAll(deviceList);
        updateControlPointsStatus();
        //mUpnpCollection.addAll(deviceList,MediaServer.class);

        if(currentDevice!=null){
        	setCurrentDevice(mUpnpCollection.getDevice(currentDevice.getKey()));
        }

        getBus().post(new NotifyDeviceListChangedEvent());
    }
    
    public void onEventMainThread(UpdateDeviceProperty ev){
    	Collection<SourcedDeviceUpnp> deviceList = ev.getDeviceList();
    	mUpnpCollection.addAll(deviceList);
    	
    	if(mCurrentState==CurrentState.MAIN_VIEW){
    		getBus().post(new NotifyDeviceListChangedEvent());
    	}else if(currentDevice!=null){
        	setCurrentDevice(mUpnpCollection.getDevice(currentDevice.getKey()));
	    	if(currentDevice.getKey().equals(ev.getDevice().getKey())){
	    		getBus().post(new NotifyDeviceListChangedEvent());
	    	}
    	}
    }
    
    public void onEventMainThread(XmppChatMessageRecivedEvent event){
    	String uuid = event.getFromUuid();
    	uuid = uuid.substring(uuid.lastIndexOf(':') + 1);
    	
    	contronPointDao.open();
    	ControlPoint cp = contronPointDao.getOne(uuid);
    	contronPointDao.close();
    	String from = cp == null ? "unknown control point" : cp.getName();
    	String body = event.getBody();
    	
    	StringBuilder message = new StringBuilder();
    	message.append("New message from ");
    	message.append(from);
    	message.append(" :\n '");
    	if (body.length() > 20) {
    		message.append(body.substring(0, 20));
    		message.append("...");
    	} else {
    		message.append(event.getBody());
    	}
    	message.append("'");
    	
    	Toast toast = Toast.makeText(this, message.toString(), Toast.LENGTH_LONG);
    	toast.show();
    	updateControlPointsStatus();
    	getBus().post(new NotifyDeviceListChangedEvent());
    }
    
    private void updateControlPointsStatus(){
    	chatDao.open();
    	try{
    	for(SourcedDeviceUpnp device : mUpnpCollection.allDevices()){
    		if(device instanceof ControlPoint){
    			ControlPoint cp = (ControlPoint) device;
    			cp.setUnreaded(chatDao.hasUnread(cp.getUuid()));    			
    		}
    	}
    	}finally{
    		chatDao.close();
    	}
    }
    public void onLocalConnectionStateClick(View view) {
        ToggleButton tb = (ToggleButton) view;
        if (tb.isChecked()) {
            getBus().post(LocalConnectionRequestEvent.OPEN);
        } else {
            getBus().post(LocalConnectionRequestEvent.CLOSE);
        }
    }
    
    public void onXmppConnectionStateClick(View view) {
        ToggleButton tb = (ToggleButton) view;
        if (tb.isChecked()) {
            CredentialsPersistence credDao = new CredentialsPersistence(this);
            Credentials data = credDao.load();
            if (TextUtils.isEmpty(data.getJid()) || TextUtils.isEmpty(data.getPassword())
                    || TextUtils.isEmpty(data.getServer())) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else {
            	credDao.save(data);
                getBus().post(
                        new XmppConnectionOpenRequestEvent(
                        		data.getJid(), 
                        		data.getPassword(), 
                        		data.getServer(), 
                        		data.getPort(),
                        		data.getPubsub(),
                        		data.getUuid(),
                        		data.getControlPointName()
                        	));
            }
        } else {
            getBus().post(new XmppConnectionCloseRequestEvent());
        }
    }

    private void goToMediaServerScreen(){
        if(currentDevice!=null && currentDevice instanceof MediaServer){
            openFragment(mFragmentMediaServer);
            mCurrentState = CurrentState.MEDIA_SERVER;
        }
    }
    
    private void goToMediaRendererScreen(){
        if(currentDevice!=null && currentDevice instanceof MediaRenderer){
            openFragment(mFragmentMediaRenderer);
            mCurrentState = CurrentState.MEDIA_RENDERER;
        }
    }
    private void goToDimmableLightScreen(){
        if(currentDevice!=null && currentDevice instanceof IDimmableLight){
            openFragment(mFragmentDimmableLight);
            mCurrentState = CurrentState.DIMMABLE_LIGHT;
        }
    }
    
    private void goToControlPointScreen(){
        if(currentDevice!=null && currentDevice instanceof ControlPoint){
            openFragment( mFragmentControlPoint);
            mCurrentState = CurrentState.CONTROL_POINT;
        }
    }
    private void goToTemperatureScreen(){
        if(currentDevice!=null && currentDevice instanceof SensorTemperature){
            openFragment(mTemperatureSensor);
            mCurrentState = CurrentState.TEMPERATURE;
        }
    }
    private void returnToMainScreen() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_view, mFragmentMain);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        ft.commit();
        mCurrentState = CurrentState.MAIN_VIEW;
        setTitle(getString(R.string.app_name));
        setCurrentDevice(null);
    }
    private void openFragment(Fragment aFragment){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_view, aFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    private void goToCurrentFragment() {
        switch(mCurrentState){
        case MAIN_VIEW:
            returnToMainScreen();
            break;
        case MEDIA_SERVER:
            goToMediaServerScreen();
            break;
        case MEDIA_RENDERER:
            goToMediaRendererScreen();
            break;
        case DIMMABLE_LIGHT:
        	goToDimmableLightScreen();
        	break;
		case CONTROL_POINT:
			goToControlPointScreen();
			break;
		case TEMPERATURE:
			goToTemperatureScreen();
			break;
		default:
			break;
        }
    }

    @Override
    public void onDeviceSelected(SourcedDeviceUpnp device) {
        if(device instanceof MediaServer){
            Log.i(TAG,"Media server clicked "+device.getName());
            setCurrentDevice(device);
            goToMediaServerScreen();
        }else if(device instanceof MediaRenderer){
            Log.i(TAG,"Media renderer clicked "+device.getName());
            setCurrentDevice(device);
            goToMediaRendererScreen();
        }else if(device instanceof IDimmableLight){
        	setCurrentDevice(device);
        	goToDimmableLightScreen();
        }else if(device instanceof ControlPoint){
        	setCurrentDevice(device);
        	goToControlPointScreen();
        }else if(device instanceof SensorTemperature){
        	setCurrentDevice(device);
        	goToTemperatureScreen();
        }
        
    }

    @Override
    public SourcedDeviceUpnp getCurrentDevice() {
        return currentDevice;
    }

    public void setCurrentDevice(SourcedDeviceUpnp device){
    	currentDevice = device;
    	getBus().postSticky(new UICurrentDevice(device));
    }
    
    @Override
    public void finishFragment() {
        returnToMainScreen();
    }

    @Override
    public void browseMediaServerDirectory(MediaServer mediaServer, String dirId, String parentId) {
        Log.i(TAG, "browseMediaServer " + dirId + " parent " + parentId);
        mBroker.onBrowseMediaServer(mediaServer, dirId, parentId);
    }
    
    @Override
    public void performMediaRendererAction(MediaRenderer mediaRenderer,String actionName, Map<String, String> args) {
        mBroker.onMediaRendererAction(mediaRenderer,actionName,args);
    }
    
    @Override
    public void performMediaRendererAction(MediaRenderer mediaRenderer,String actionName, Map<String, String> args,UpnpActionCallback callback) {
        mBroker.onMediaRendererAction(mediaRenderer,actionName,args,callback);
    }
    
    @Override
    public void perfomRenderingControlAction(MediaRenderer mediaRenderer, String actionName, Map<String, String> args, UpnpActionCallback callback) {
        mBroker.onRenderingControlAction(mediaRenderer,actionName,args,callback);
    }
    
    @Override
    public void performUpnpDeviceAction(SourcedDeviceUpnp device,String serviceName,String actionName,Map<String,String> args,UpnpActionCallback callback){
    	mBroker.onUpnpAction(device,serviceName,actionName,args,callback);
    }
    
	@Override
	public void performWriteSensor(Sensor sensor, String sensorURN,
			Map<String, String> values, UpnpActionCallback callback) {
		Map<String,String> args = sensor.prepareWriteMap(sensorURN,values);
		mBroker.onUpnpAction(sensor, Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE, Sensor.WRITE_SENSOR_ACTION, args, callback);
	}

	@Override
	public void performReadSensor(Sensor sensor, String sensorURN,List<String> keys, UpnpActionCallback callback) {
		Map<String,String> args = sensor.prepareReadMap(sensorURN,keys);
		mBroker.onUpnpAction(sensor, Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE, Sensor.READ_SENSOR_ACTION, args, callback);

		
	}
	
    @Override
    public MediaRenderer getDefaultMediaRenderer() {
        if(defaultMediaRendererKey!=null){
            return (MediaRenderer) mUpnpCollection.getDevice(defaultMediaRendererKey);
        }
        return null;
    }

    @Override
    public void setDefaultMediaRenderer(MediaRenderer mediaRenderer) {
        if(mediaRenderer!=null){
            defaultMediaRendererKey = mediaRenderer.getKey();
        }else{
            defaultMediaRendererKey = null;
        }
        
    }

    @Override
    public DeviceUpnpCollection getUpnpDevices(){
    	if(mUpnpCollection==null){
    		Log.e(TAG,"Create ew UpnpCollection");
    		mUpnpCollection = new DeviceUpnpCollection();
    	}
    	return mUpnpCollection;
    }



}
