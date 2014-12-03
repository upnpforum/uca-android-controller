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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;

import com.comarch.android.upnp.ibcdemo.model.ControlPoint;
import com.comarch.android.upnp.ibcdemo.model.DimmableLight;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;
import com.comarch.android.upnp.ibcdemo.model.MediaServer;
import com.comarch.android.upnp.ibcdemo.model.Sensor;
import com.comarch.android.upnp.ibcdemo.persistence.DimmableLightDao;
import com.comarch.android.upnp.ibcdemo.persistence.MediaRendererDao;
import com.comarch.android.upnp.ibcdemo.persistence.MediaServerDao;

public class DeviceDescriptionFactory {

    private static final String TAG = DeviceDescriptionFactory.class.getSimpleName();
    private static final Pattern DEVICE_PATTERN = Pattern.compile("(.*)/urn:schemas-upnp-org:device:(.*):(.*):uuid:(.*)");
    private static final Pattern CP_PATTERN = Pattern.compile("(.*)@(.*)/urn:schemas-upnp-org:cloud-1-0:ControlPoint:1:(.*)");
    private MediaRendererDao mediaRendererDao;
    private MediaServerDao mediaServerDao;
    private DimmableLightDao dimmableLightDao;
    
    public DeviceDescriptionFactory(Context ctx) {
        mediaRendererDao = new MediaRendererDao(ctx);
        mediaServerDao = new MediaServerDao(ctx);
        dimmableLightDao = new DimmableLightDao(ctx);
    }

    public DeviceDescription createDevice(String type,String name,String uuid,String hash){
        DeviceDescription description = new DeviceDescription(name, uuid);
        if(type.equalsIgnoreCase("MediaRenderer")){
            mediaRendererDao.open();
            MediaRenderer mediaRenderer = null;
            try{
                mediaRenderer = mediaRendererDao.getOne(uuid);
            }finally{
                mediaRendererDao.close();
            }
            if(mediaRenderer==null || hash==null || !hash.equals(mediaRenderer.getConfigIdCloud())){
                mediaRenderer=new MediaRenderer(uuid, "MediaRenderer");
                mediaRenderer.setConfigIdCloud(hash);
                description.setDeviceDescriptionNeeded(true);
            }
            description.bindInstance(mediaRenderer);
            return description;
        }else if(type.equalsIgnoreCase("MediaServer")){
            mediaServerDao.open();
            MediaServer mediaServer = null;
            try{
                mediaServer = mediaServerDao.getOne(uuid);
            }finally{
                mediaServerDao.close();
            }
            if(mediaServer==null || hash==null || !hash.equals(mediaServer.getConfigIdCloud())){
                mediaServer=new MediaServer(uuid, "MediaServer");
                mediaServer.setConfigIdCloud(hash);
                description.setDeviceDescriptionNeeded(true);
            }
            description.bindInstance(mediaServer);
            return description;
        }else if(type.equalsIgnoreCase("DimmableLight")){
        	dimmableLightDao.open();
        	DimmableLight dimmableLight = null;
        	try{
        		dimmableLight = dimmableLightDao.getDimmableLights(uuid);
        	}finally{
        		dimmableLightDao.close();
        	}
        	if(dimmableLight==null || hash==null || !hash.equals(dimmableLight.getConfigIdCloud())){
        		dimmableLight=new DimmableLight(uuid, "DimmableLight");
        		dimmableLight.setConfigIdCloud(hash);
                description.setDeviceDescriptionNeeded(true);
            }
            description.bindInstance(dimmableLight);
            return description;
        } else if(type.equalsIgnoreCase("SensorManagement")){
        	description = new SensorDeviceDescription(description);
        	Sensor sensor = null;
        	if(sensor==null || hash==null || !hash.equals(sensor.getConfigIdCloud())){
        		sensor = new Sensor(uuid,"Sensor",null);
        		sensor.setConfigIdCloud(hash);
        		description.setDeviceDescriptionNeeded(true);
        	}
        	description.bindInstance(sensor);
        	return description;
        }else{
            Log.e(TAG,"Unknown device type: "+type);
            return null;
        } 
    }
	private DeviceDescription createControlPoint(String device, String uuid,String login) {
		String name = login;
		DeviceDescription description = new DeviceDescription(device, uuid);
		description.setDeviceDescriptionNeeded(false);
		description.bindInstance(new ControlPoint(uuid, name));
		return description;
	}
	
    public DeviceDescription createDeviceDescription(String jid, String hash){
        Log.d(TAG,"Item jid: "+jid);
        Matcher deviceMatcher = DEVICE_PATTERN.matcher(jid);
        Matcher cpMatcher = CP_PATTERN.matcher(jid);
        
        DeviceDescription description=null;
        if(deviceMatcher.matches()){
            String uuid = deviceMatcher.group(4);
            String type = deviceMatcher.group(2);
            String device = deviceMatcher.group(0);
            Log.d(TAG,"Found device with uuid: "+uuid);
            description = createDevice(type, device, uuid, hash);
        }else if(cpMatcher.matches()){
        	String login = cpMatcher.group(1);
        	String uuid = cpMatcher.group(3);
        	String device = cpMatcher.group(0);
        	description = createControlPoint(device, uuid,login);
        }
        return description;
    }
    public String getTypeFromJid(String jid){
        Matcher deviceMatcher = DEVICE_PATTERN.matcher(jid);        
        if(deviceMatcher.matches()){
            return deviceMatcher.group(2);
        }
        return null;
    }
}
