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
package com.comarch.android.upnp.ibcdemo.connectivity.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.comarch.android.upnp.ibcdemo.busevent.ActionCollectionEvent;
import com.comarch.android.upnp.ibcdemo.busevent.UpnpServiceActionEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.LocalConnectionRequestEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.XmppConnectionCloseRequestEvent;
import com.comarch.android.upnp.ibcdemo.model.DimmableLight;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;
import com.comarch.android.upnp.ibcdemo.model.Sensor;
import com.comarch.android.upnp.ibcdemo.model.SensorLight;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;

import de.greenrobot.event.EventBus;

public class ResetDemoEngine {

	private EventBus mEventBus;
	private Collection<SourcedDeviceUpnp> mDevices;
	private List<UpnpServiceActionEvent> actions = new ArrayList<UpnpServiceActionEvent>();
	enum ResetDemoEngineEvent{
		SEND_NEXT;
	}
	public ResetDemoEngine(Collection<SourcedDeviceUpnp> devices,EventBus eventBus){
		mDevices = devices;
		mEventBus = eventBus;
		mEventBus.register(this);
	}
	
	public void start(){
		for(SourcedDeviceUpnp device : mDevices){
			if(device instanceof DimmableLight){
				resetDimmableLight((DimmableLight)device);
			}else if(device instanceof MediaRenderer){
				resetMediaRenderer((MediaRenderer)device);
			}else if(device instanceof SensorLight){
				resetSensorLight((SensorLight)device);
			}
		}		
		mEventBus.post(ResetDemoEngineEvent.SEND_NEXT);
	}

	public void onEvent(ResetDemoEngineEvent e){
		sendNext();
	}
	private void sendNext() {
		if(actions.isEmpty()){
			mEventBus.unregister(this);
			mEventBus.post(new XmppConnectionCloseRequestEvent());
			mEventBus.post(LocalConnectionRequestEvent.CLOSE);
			mEventBus.post(ActionCollectionEvent.CLEAR);
		}else{
			final UpnpServiceActionEvent event = actions.get(0);
			event.setCallback(new UpnpActionCallback(true) {
				@Override
				public void run() {
					actions.remove(event);
					mEventBus.post(ResetDemoEngineEvent.SEND_NEXT);
				}
			});
			mEventBus.post(event);
			
		}
	}

	//No callbacks here! Will be overrided!
	private void resetMediaRenderer(MediaRenderer device) {
        Map<String,String> args = new HashMap<String,String>();
        args.put("InstanceID", "0");
        actions.add(new UpnpServiceActionEvent(device,MediaRenderer.AV_TRANSPORT_SERVICE, "Stop", args,null));
	}
	private void resetSensorLight(SensorLight sensor) {
		String sensorUrn = sensor.getSensorURNWhichBegin(SensorLight.SWITCH_SENSOR_URN);
		Map<String,String> args = sensor.prepareWriteMap(sensorUrn,SensorLight.SWITCH_SENSOR_ARG_NAME,"0");
		actions.add(new UpnpServiceActionEvent(sensor,Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,Sensor.WRITE_SENSOR_ACTION,args,null));

		sensorUrn = sensor.getSensorURNWhichBegin(SensorLight.BRIGHTNESS_SENSOR_URN);
		args = sensor.prepareWriteMap(sensorUrn,SensorLight.BRIGHTNES_SENSOR_ARG_NAME,"100");
		actions.add(new UpnpServiceActionEvent(sensor,Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,Sensor.WRITE_SENSOR_ACTION,args,null));

	}
	private void resetDimmableLight(DimmableLight device) {
		Map<String,String> args = new HashMap<String,String>();
		args.put(DimmableLight.NEW_TARGET_VALUE_ARG,"0");
		actions.add(new UpnpServiceActionEvent(device, DimmableLight.SWITCH_SERVICE, DimmableLight.SET_TARGET_ACTION, args, null));
		args = new HashMap<String,String>();
		args.put(DimmableLight.NEW_LOAD_LEVEL_TARGET_ARG,"100");
		actions.add(new UpnpServiceActionEvent(device, DimmableLight.DIMMING_SERVICE, DimmableLight.SET_LOAD_LEVEL_TARGET_ACTION, args, null));		
	}
	

}
