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

package com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.packet.Packet;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.comarch.android.upnp.ibcdemo.connectivity.common.CommonSensorFactory;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.PacketListenerWithFilter;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.XmppConnector;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.SoapRequestIQ;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.SoapResponseIQ;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.UPNPCloud;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.UPNPDevice;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.UPNPQuery;
import com.comarch.android.upnp.ibcdemo.model.ControlPoint;
import com.comarch.android.upnp.ibcdemo.model.DeviceUpnp;
import com.comarch.android.upnp.ibcdemo.model.DimmableLight;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;
import com.comarch.android.upnp.ibcdemo.model.RGBDimmableLight;
import com.comarch.android.upnp.ibcdemo.model.Sensor;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;
import com.google.common.collect.Lists;

public class DeviceDescriptionFetcher extends CommonSensorFactory implements PacketListenerWithFilter{

    private final String TAG = getClass().getSimpleName();
    
    private XmppConnector connector;
    private DeviceDescription description;
    private HashMap<String, String> sentSoapIds;
    private XmppDevicesStateObserver observer;
    
    private String mDeviceStatus;
    
    private boolean finished;
    private boolean isStopped = false;

    
    public DeviceDescriptionFetcher(XmppConnector connector, DeviceDescription device){
        this.connector = connector;
        finished = false;
        sentSoapIds = new HashMap<String, String>();
        this.description = device;
    }
    
    public boolean hasFinished() {
    	return finished;
    }
    
    public void registerObserver(XmppDevicesStateObserver observer) {
    	this.observer = observer;
    }
    
    public void start() {
        if(!isStopped){
            connector.addPacketListener(this);
            DeviceDescription desc = description;
            if(desc.isDeviceDescriptionNeeded()){
                connector.sendPacket(new UPNPCloud(desc.getJid(), "uuid:"+desc.getUuid(), "description"));
            }else{
                initState(description.getUuid(),description,null);
            }
        }
    }
    
    private void finished(){
        if(observer!=null){
            observer.onDeviceDiscoveredFinished(description);
        }
        connector.removePacketListener(this);
        finished = true;
    }

	private static String stripUuidPrefix(final String uuid) {
		String id = null;
    	if (uuid.startsWith("uuid:")) {
    		id = uuid.substring(5);
    	} else {
    		id = uuid;
    	}
		return id;
	}
    
    @Override
    public void processPacket(Packet packet) {
        if(isStopped) return;
        
    	if (packet instanceof UPNPQuery) {
    		UPNPQuery query = (UPNPQuery) packet; 
    		processQueryPacket(query);
    	} else if (packet instanceof SoapResponseIQ) {
    		SoapResponseIQ response = (SoapResponseIQ) packet;
    		processResponsePacket(response);
    	}
    }

	private void processResponsePacket(SoapResponseIQ response) {
		Log.i(TAG, response.getActionName() + " response received!");
		
		String uuid = sentSoapIds.remove(response.getPacketID());
		if (!description.getUuid().equals(uuid)) {
			Log.d(TAG, "Cound't find device description with uud = " + uuid);
			return;
		}
		if(description instanceof SensorDeviceDescription){
			processSensorResponsePacket(response);
			if (observer != null) {
	            observer.onDeviceFound(description);
	        }
		}else{
			DeviceUpnp device = description.getBoundDevice();
			if (device == null) {
				Log.d(TAG, "Cound't find device instance for description with uud = " + uuid);
				return;
			}
			
			boolean shouldNotify = procesResponsePacket(device,response);
				
	        if (shouldNotify && observer != null) {
	        	//Pass empty hashMap, don't need any event handling
	        	observer.onDevicePropertiesChanged(device,new HashMap<String,Object>());
	        }
		}
		
    	if (sentSoapIds.size() == 0) {
    		finished();
    	}
	}

	private boolean procesResponsePacket(DeviceUpnp device, SoapResponseIQ response) {
        if(device instanceof MediaRenderer){
            return processMediaRendererResponsePacket((MediaRenderer) device,response);
        }else if(device instanceof DimmableLight){
        	return processDimmableLightResponsePacket((DimmableLight) device, response);
        }
        return false;
        
    }

	private boolean processDimmableLightResponsePacket(DimmableLight device, SoapResponseIQ response){
        if (response.getActionName().equals("GetStatus")) {
            boolean status = response.getArgumentValue("ResultStatus").equals("1");
            device.setSwitched(status);
            return true;
        } else if (response.getActionName().equals("GetLoadLevelStatus")) {
            String result = response.getArgumentValue("retLoadlevelStatus");
            double level = 0.0;
            try {
                int value = Integer.parseInt(result);
                level = value / 100.0;
            } catch (NumberFormatException e) {
                Log.e(TAG, "Failed to parse response of GetLoadLevelStatus", e);
            }
            device.setBrightness(level);
            return true;
        }
        return false;
	}
    private boolean processMediaRendererResponsePacket(MediaRenderer device, SoapResponseIQ response) {
        if (response.getActionName().equals("GetTransportInfo")) {
            String transportState = response.getArgumentValue("CurrentTransportState");
            device.setTransportState(MediaRenderer.TransportState.valueOf(transportState));
            return true;
        }else if(response.getActionName().equals("GetMediaInfo")){
            device.processGetMediaInfo(response);
            return true;
        }
        return false;
        
    }

    private void processQueryPacket(UPNPQuery query) {
		UPNPDevice device = query.getRoot().getDevice();
        String uuid = stripUuidPrefix(device.getUDN());
        if(description.getUuid().equals(uuid)){
            Log.i(TAG, device.getFriendlyName() + " found " + device.getUDN());
            initState(uuid,description,device);
            description.getBoundDevice().setName(device.getFriendlyName());
        }
	}
    
	private void initState(String uuid,DeviceDescription deviceDescription,UPNPDevice device){
		if(deviceDescription instanceof SensorDeviceDescription){
	    	initSensor(uuid,deviceDescription,device);
	    }else{
	    	if(deviceDescription.getBoundDevice() instanceof ControlPoint){
	    		if(mDeviceStatus!=null && !mDeviceStatus.isEmpty()){
	    			deviceDescription.getBoundDevice().setName(mDeviceStatus);
	    		}
	    	}
	    	else if(deviceDescription.getBoundDevice() instanceof MediaRenderer){
		        initMediaRendererState(uuid,deviceDescription);
		    }else if(deviceDescription.getBoundDevice() instanceof DimmableLight){
		    	initDimmableLightState(uuid,deviceDescription,device);
		    } 
	
	        if (observer != null) {
	            observer.onDeviceFound(description);
	        }
	    }
        if (sentSoapIds.size() == 0) {
            finished();
        }
	}

	private void initSensor(String uuid, DeviceDescription deviceDescription,UPNPDevice device) {
		String jid = null;
		if(description.getUuid().equals(uuid)){
            jid = description.getJid();
        }
		HashMap<String,String> args = new HashMap<String, String>();
		args.put("Parameters", createParameters(Lists.newArrayList("/UPnP/SensorMgt/SensorCollections/1/SensorsNumberOfEntries","/UPnP/SensorMgt/SensorCollections/1/Sensors")));
        SoapRequestIQ statusIq = new SoapRequestIQ("GetValues", Sensor.CONFIGURATION_MANAGER_SERVICE, args);
        statusIq.setTo(jid);
        
        sentSoapIds.put(statusIq.getPacketID(), uuid);
        connector.sendPacket(statusIq);
	}
	
	private void processSensorResponsePacket(SoapResponseIQ response) {
		if(response.getActionName().equals("GetValues")){
	        Map<String, String> retMap;
			try {
				String parameterValueList = response.getArgumentValue("ParameterValueList");
				retMap = getParameterValueList(parameterValueList);
				
				if(retMap.containsKey("/UPnP/SensorMgt/SensorCollections/1/SensorsNumberOfEntries")){
			        int numberOfSensors = Integer.parseInt(retMap.get("/UPnP/SensorMgt/SensorCollections/1/SensorsNumberOfEntries"));
			        for(int i=1;i<=numberOfSensors;++i){
			        	Sensor sensor = createSensor(description.getUuid(), "/UPnP/SensorMgt/SensorCollections/1/Sensors/"+i, retMap);
			        	if(sensor!=null){
			        		((SensorDeviceDescription)description).AddSensor(sensor);
			        	}
			        }
				}
		
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void initDimmableLightState(String uuid, DeviceDescription deviceDescription, UPNPDevice device){
        String jid = null;
        if(description.getUuid().equals(uuid)){
            jid = description.getJid();
        }
        if(device!=null){
	        if(device.hasService("schemas-awox-com","X_ColorLight")){
	        	SourcedDeviceUpnp oldDevice = description.getBoundDevice();
	        	RGBDimmableLight rgbLight = new RGBDimmableLight(oldDevice.getUuid(), oldDevice.getName());
	        	description.bindInstance(rgbLight);
	        }
        }
        SoapRequestIQ statusIq = new SoapRequestIQ("GetStatus", DimmableLight.SWITCH_SERVICE, null);
        statusIq.setTo(jid);
        
        sentSoapIds.put(statusIq.getPacketID(), uuid);
        connector.sendPacket(statusIq);
        
        SoapRequestIQ levelIq = new SoapRequestIQ("GetLoadLevelStatus", DimmableLight.DIMMING_SERVICE, null);
        levelIq.setTo(jid);
        
        sentSoapIds.put(levelIq.getPacketID(), uuid);
        connector.sendPacket(levelIq);
	}
    private void initMediaRendererState(String uuid, DeviceDescription deviceDescription) {
        
        String jid = null;
        if(description.getUuid().equals(uuid)){
            jid = description.getJid();
        }
        
        Map<String,String> args = new HashMap<String,String>();
        args.put("InstanceID", "0");
        SoapRequestIQ transportInfoIq = new SoapRequestIQ("GetTransportInfo", MediaRenderer.AV_TRANSPORT_SERVICE, args);
        sentSoapIds.put(transportInfoIq.getPacketID(), uuid);
        transportInfoIq.setTo(jid);
        connector.sendPacket(transportInfoIq);
        
        SoapRequestIQ getMediaInfoIq = new SoapRequestIQ("GetMediaInfo", MediaRenderer.AV_TRANSPORT_SERVICE,args);
        sentSoapIds.put(getMediaInfoIq.getPacketID(), uuid);
        getMediaInfoIq.setTo(jid);
        connector.sendPacket(getMediaInfoIq);
    }

    @Override
    public boolean accept(Packet packet) {
        if(isStopped) return false;
    	boolean result = false;
    	
    	result = result || sentSoapIds.containsKey(packet.getPacketID());
        result = result || packet.getClass().equals(UPNPQuery.class);
        
        return result;
    }

    public void stop() {
        isStopped = true;
        observer = null;
        connector.removePacketListener(this);
    }

	@Override
	protected String getLogTag() {
		return TAG;
	}

	public void setDeviceStatus(String status) {
		mDeviceStatus = status;
	}
}
