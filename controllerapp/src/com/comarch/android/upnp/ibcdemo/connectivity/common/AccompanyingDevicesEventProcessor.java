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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.comarch.android.upnp.ibcdemo.busevent.UpnpServiceActionEvent;
import com.comarch.android.upnp.ibcdemo.model.DeviceUpnp;
import com.comarch.android.upnp.ibcdemo.model.DimmableLight;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;
import com.comarch.android.upnp.ibcdemo.model.Sensor;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer.TransportState;
import com.comarch.android.upnp.ibcdemo.model.SensorLight;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;
import com.comarch.android.upnp.ibcdemo.persistence.AccompanyingDevicesDao;
import com.comarch.android.upnp.ibcdemo.persistence.AccompanyingFilePersistence;

import de.greenrobot.event.EventBus;

public class AccompanyingDevicesEventProcessor {

	private final String TAG = getClass().getSimpleName();
	
	private AccompanyingDevicesDao mDao;
	private CommonConnector mConnector;
	private EventBus mEventBus;
	private AccompanyingFilePersistence mAccompanyingFile;
	
	public AccompanyingDevicesEventProcessor(Context ctx, CommonConnector commonConnector, EventBus eventBus) {
		mDao = new AccompanyingDevicesDao(ctx);
		mConnector = commonConnector;
		mEventBus = eventBus;
		mAccompanyingFile = new AccompanyingFilePersistence(ctx);
	}

	public void processEvent(DeviceUpnp device, Map<String, Object> changes) {
		if (changes==null || changes.isEmpty()) return;
		
		if (device instanceof MediaRenderer) {
			if (changes.containsKey("AVTransportURIMetaData")) {
				processMediaRendererChangedTransportUriMetaData((MediaRenderer) device,(String)changes.get("AVTransportURIMetaData"));
			}
			if (changes.containsKey(MediaRenderer.TRANSPORT_STATE)) {
				TransportState state = (TransportState) changes.get(MediaRenderer.TRANSPORT_STATE);
				processMediaRendererChangedTransportState((MediaRenderer)device, state);
			}
		}
	}

	private void postActionEvent(UpnpServiceActionEvent event) {
		event.setToAccompanying(true);
		mEventBus.post(event);
	}
	
	private List<SourcedDeviceUpnp> getAccompanying(List<String> accompanyingKeys){
		List<SourcedDeviceUpnp> devices = new ArrayList<SourcedDeviceUpnp>();
		for(SourcedDeviceUpnp device : mConnector.getDeviceList()){
			if(accompanyingKeys.contains(device.getKey())){
				devices.add(device);
			}
		}
		return devices;
	}
	private void processMediaRendererChangedTransportUriMetaData(MediaRenderer device, String object) {
		try{
			mDao.open();
			List<String> accompanying = mDao.getAccompanying(device.getKey());
			
			String accompanyingUri = mAccompanyingFile.getAccompanyingContentUri();
			String accompanyingDidl = mAccompanyingFile.getAccompanyingContentDIDL();
			
			if (accompanyingUri != null && accompanyingDidl != null) {
				for (SourcedDeviceUpnp accDevice : getAccompanying(accompanying)) {
					if (accDevice instanceof MediaRenderer) {
						Map<String,String> args = new HashMap<String,String>();
						args.put("CurrentURI", accompanyingUri);
						args.put("InstanceID", "0");
						args.put("CurrentURIMetaData", accompanyingDidl);
						postActionEvent(new UpnpServiceActionEvent(accDevice,MediaRenderer.AV_TRANSPORT_SERVICE, "SetAVTransportURI", args, null));
					}
				}
			} else {
				Log.i(TAG, "URI and DIDL-Lite for accompanying content not set.");
			}
		} finally {
			mDao.close();
		}
	}
	private void processMediaRendererChangedTransportState(MediaRenderer device, TransportState state) {
		
		try{
			mDao.open();
			List<String> accompanying = mDao.getAccompanying(device.getKey());
			for(SourcedDeviceUpnp accDevice : getAccompanying(accompanying)){
				if(accDevice instanceof SensorLight){
					processMediaRendererChangedTransportStateForSensorLight(state, (SensorLight) accDevice);
				}
				else if(accDevice instanceof DimmableLight){
					processMediaRendererChangedTransportStateForDimmbleLight(state,(DimmableLight) accDevice);
				}else if(accDevice instanceof MediaRenderer){
					processMediaRendererChangedTransportStateForMediaRenderer(state,(MediaRenderer) accDevice);
				}
			}
			
		}finally{
			mDao.close();
		}
	}

	private void processMediaRendererChangedTransportStateForMediaRenderer(TransportState state, final MediaRenderer device) {
		Map<String,String> args = new HashMap<String,String>();
		switch(state){
		case PLAYING:
            args.put("InstanceID", "0");
            args.put("Speed", "1");
            postActionEvent(new UpnpServiceActionEvent(device,MediaRenderer.AV_TRANSPORT_SERVICE, "Play", args,null));
			break;
		case PAUSED_PLAYBACK:
	        args.put("InstanceID", "0");
	        postActionEvent(new UpnpServiceActionEvent(device,MediaRenderer.AV_TRANSPORT_SERVICE, "Pause", args,null));
			break;
		case STOPPED:
	        args.put("InstanceID", "0");
	        postActionEvent(new UpnpServiceActionEvent(device,MediaRenderer.AV_TRANSPORT_SERVICE, "Stop", args,null));
			break;
		default:
		}
		
	}
	private void processMediaRendererChangedTransportStateForSensorLight(TransportState state, SensorLight device) {
		Map<String,String> args = new HashMap<String,String>();
		String sensorUrn = null;
		switch(state){
		case PLAYING:
			sensorUrn = device.getSensorURNWhichBegin(SensorLight.SWITCH_SENSOR_URN);
			args = device.prepareWriteMap(sensorUrn,SensorLight.SWITCH_SENSOR_ARG_NAME,"0");
			postActionEvent(new UpnpServiceActionEvent(device,Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,Sensor.WRITE_SENSOR_ACTION,args,null));
			break;
		case PAUSED_PLAYBACK:
			if(!((SensorLight)device).isSwitched()){
				
				sensorUrn = device.getSensorURNWhichBegin(SensorLight.BRIGHTNESS_SENSOR_URN);
				args = device.prepareWriteMap(sensorUrn,SensorLight.BRIGHTNES_SENSOR_ARG_NAME,"50");
				postActionEvent(new UpnpServiceActionEvent(device,Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,Sensor.WRITE_SENSOR_ACTION,args,null));

				args = new HashMap<String,String>();
				sensorUrn = device.getSensorURNWhichBegin(SensorLight.SWITCH_SENSOR_URN);
				args = device.prepareWriteMap(sensorUrn,SensorLight.SWITCH_SENSOR_ARG_NAME,"1");
				postActionEvent(new UpnpServiceActionEvent(device,Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,Sensor.WRITE_SENSOR_ACTION,args,null));
				

			}
			break;
		case STOPPED:
			sensorUrn = device.getSensorURNWhichBegin(SensorLight.SWITCH_SENSOR_URN);
			args = device.prepareWriteMap(sensorUrn,SensorLight.SWITCH_SENSOR_ARG_NAME,"1");
			postActionEvent(new UpnpServiceActionEvent(device,Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,Sensor.WRITE_SENSOR_ACTION,args,null));
			
			args = new HashMap<String,String>();
			sensorUrn = device.getSensorURNWhichBegin(SensorLight.BRIGHTNESS_SENSOR_URN);
			args = device.prepareWriteMap(sensorUrn,SensorLight.BRIGHTNES_SENSOR_ARG_NAME,"100");
			postActionEvent(new UpnpServiceActionEvent(device,Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,Sensor.WRITE_SENSOR_ACTION,args,null));

			break;
		default:
		}
	}
	
	private void processMediaRendererChangedTransportStateForDimmbleLight(TransportState state, DimmableLight device) {
		Map<String,String> args = new HashMap<String,String>();
		switch(state){
		case PLAYING:
			args.put(DimmableLight.NEW_TARGET_VALUE_ARG,"0");
			postActionEvent(new UpnpServiceActionEvent(device, DimmableLight.SWITCH_SERVICE, DimmableLight.SET_TARGET_ACTION, args, null));
			break;
		case PAUSED_PLAYBACK:
			if(!((DimmableLight)device).isSwitched()){
				
				args.put(DimmableLight.NEW_TARGET_VALUE_ARG,"1");
				postActionEvent(new UpnpServiceActionEvent(device, DimmableLight.SWITCH_SERVICE, DimmableLight.SET_TARGET_ACTION, args, null));
				args = new HashMap<String,String>();
				args.put(DimmableLight.NEW_LOAD_LEVEL_TARGET_ARG,"50");
				postActionEvent(new UpnpServiceActionEvent(device, DimmableLight.DIMMING_SERVICE, DimmableLight.SET_LOAD_LEVEL_TARGET_ACTION, args, null));

			}
			break;
		case STOPPED:
			args.put(DimmableLight.NEW_TARGET_VALUE_ARG,"1");
			postActionEvent(new UpnpServiceActionEvent(device, DimmableLight.SWITCH_SERVICE, DimmableLight.SET_TARGET_ACTION, args, null));
			args = new HashMap<String,String>();
			args.put(DimmableLight.NEW_LOAD_LEVEL_TARGET_ARG,"100");
			postActionEvent(new UpnpServiceActionEvent(device, DimmableLight.DIMMING_SERVICE, DimmableLight.SET_LOAD_LEVEL_TARGET_ACTION, args, null));

			break;
		default:
		}
	}

}
