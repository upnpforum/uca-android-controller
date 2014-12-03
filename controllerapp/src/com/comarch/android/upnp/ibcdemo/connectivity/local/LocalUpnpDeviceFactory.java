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

import java.util.ArrayList;
import java.util.List;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;

import android.util.Log;

import com.comarch.android.upnp.ibcdemo.connectivity.local.eventing.AVTransportSubscriptionCallback;
import com.comarch.android.upnp.ibcdemo.connectivity.local.eventing.ColorLightSubscriptionCallback;
import com.comarch.android.upnp.ibcdemo.connectivity.local.eventing.DimmableSubscriptionCallback;
import com.comarch.android.upnp.ibcdemo.connectivity.local.eventing.SwitchPowerSubscriptionCallback;
import com.comarch.android.upnp.ibcdemo.model.DimmableLight;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;
import com.comarch.android.upnp.ibcdemo.model.MediaServer;
import com.comarch.android.upnp.ibcdemo.model.RGBDimmableLight;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;

import de.greenrobot.event.EventBus;

public class LocalUpnpDeviceFactory {

	private final String TAG = getClass().getSimpleName();
	private UpnpService mUpnpService;
	private EventBus mEventBus;
	private LocalSensorsFactory mSensorsFactory;
	
	public LocalUpnpDeviceFactory(UpnpService upnpService, EventBus eventBus){
		mUpnpService = upnpService;
		mEventBus=eventBus;
		mSensorsFactory = new LocalSensorsFactory(mUpnpService);
	}
	
	@SuppressWarnings("rawtypes")
	public List<SourcedDeviceUpnp> create(Device device){
		
		List<SourcedDeviceUpnp> devices = new ArrayList<SourcedDeviceUpnp>();
		DeviceType type = device.getType();
		if(type.implementsVersion(new UDADeviceType("DimmableLight"))){
			devices.add(createDimmableLight(device));
		}else if(type.implementsVersion(new UDADeviceType("MediaServer"))){
			devices.add(createMediaServer(device));
		}else if(type.implementsVersion(new UDADeviceType("MediaRenderer"))){
			devices.add(createMediaRenderer(device));
		}else if(type.implementsVersion(new UDADeviceType("SensorManagement"))){
			devices.addAll(mSensorsFactory.createSensors(device));
		}else{
			Log.d(TAG, "Unknown device type "+device.getType().toString());
		}
		return devices;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private SourcedDeviceUpnp createMediaServer(Device device) {
        String name = device.getDetails().getFriendlyName();
        String uuid = device.getIdentity().getUdn().getIdentifierString();
        
		return new MediaServer(uuid, name);
	}
	@SuppressWarnings({ "rawtypes" })
	private SourcedDeviceUpnp createMediaRenderer(Device device){
        String name = device.getDetails().getFriendlyName();
        String uuid = device.getIdentity().getUdn().getIdentifierString();
     
        MediaRenderer renderer = new MediaRenderer(uuid, name);
        
        Service avTransport = device.findService(new UDAServiceType(LocalUpnpService.SERVICE_TYPE_AVTRANSPORT));
        if (avTransport != null) {
        	mUpnpService.getControlPoint().execute(new AVTransportSubscriptionCallback(renderer, mEventBus, avTransport));
        }
        
		return renderer;
	}
	@SuppressWarnings({ "rawtypes" })
	public SourcedDeviceUpnp createDimmableLight(Device device){
        String name = device.getDetails().getFriendlyName();
        String uuid = device.getIdentity().getUdn().getIdentifierString();


        Service colorLightService = device.findService(new ServiceType("schemas-awox-com","X_ColorLight"));
        Service serviceSwitch = device.findService(new UDAServiceType(LocalUpnpService.SERVICE_TYPE_SWITCH));
        Service serviceDimming = device.findService(new UDAServiceType(LocalUpnpService.SERVICE_TYPE_DIMMING));       
        
        
        DimmableLight light = null;
        if(colorLightService!=null){
        	light = new RGBDimmableLight(uuid, name);
        	mUpnpService.getControlPoint().execute(new ColorLightSubscriptionCallback(light,mEventBus,colorLightService));
        }else{
        	light = new DimmableLight(uuid, name);
        }
        mUpnpService.getControlPoint().execute(new SwitchPowerSubscriptionCallback(light, mEventBus, serviceSwitch));
        mUpnpService.getControlPoint().execute(new DimmableSubscriptionCallback(light, mEventBus, serviceDimming));
        return light;
	}
}
