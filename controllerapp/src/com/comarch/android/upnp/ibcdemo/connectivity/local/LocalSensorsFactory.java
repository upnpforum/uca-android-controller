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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.xmlpull.v1.XmlPullParserException;

import com.comarch.android.upnp.ibcdemo.connectivity.common.CommonSensorFactory;
import com.comarch.android.upnp.ibcdemo.model.Sensor;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;
import com.google.common.collect.Lists;

public class LocalSensorsFactory extends CommonSensorFactory{

	private final String TAG = getClass().getSimpleName();
	
	private UpnpService mUpnpService;
	
	public LocalSensorsFactory(UpnpService upnpService){
		super();
		mUpnpService = upnpService;
	}
	protected String getLogTag(){
		return TAG;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<SourcedDeviceUpnp> createSensors(Device device) {
		List<SourcedDeviceUpnp> sensors = new ArrayList<SourcedDeviceUpnp>();
        String name = device.getDetails().getFriendlyName();
        String uuid = device.getIdentity().getUdn().getIdentifierString();
        
        Service configurationManagement = device.findService(new UDAServiceType("ConfigurationManagement"));
        Action getValuesAction = configurationManagement.getAction("GetValues"); /* returns null if action not found */
        ActionInvocation getNumberOfSensors = new ActionInvocation(getValuesAction); /* blows up if given null */
        getNumberOfSensors.setInput("Parameters", createParameters(Lists.newArrayList("/UPnP/SensorMgt/SensorCollections/1/SensorsNumberOfEntries")));
        new ActionCallback.Default(getNumberOfSensors, mUpnpService.getControlPoint()).run();
        
        ActionArgumentValue ret = findCaseInsensitive("ParameterValueList",getNumberOfSensors.getOutput());
        
        Map<String, String> retMap;
		try {
			retMap = getParameterValueList((String)ret.getValue());
	        int numberOfSensors = Integer.parseInt(retMap.get("/UPnP/SensorMgt/SensorCollections/1/SensorsNumberOfEntries"));
	        for(int i=1;i<=numberOfSensors;++i){
	        	Sensor s = createSensor(i,device,uuid,name);
	        	if(s!=null){	        		
	        		sensors.add(s);
	        	}
	        }
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sensors;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Sensor createSensor(int i, Device device, String uuid, String name) throws XmlPullParserException, IOException {
		String sensorPath = "/UPnP/SensorMgt/SensorCollections/1/Sensors/"+i;
        Service configurationManagement = device.findService(new UDAServiceType("ConfigurationManagement"));
        Action getValuesAction = configurationManagement.getAction("GetValues");
        ActionInvocation getSensor = new ActionInvocation(getValuesAction);
        getSensor.setInput("Parameters", createParameters(Lists.newArrayList(sensorPath)));        
        new ActionCallback.Default(getSensor, mUpnpService.getControlPoint()).run();
        ActionArgumentValue ret = findCaseInsensitive("ParameterValueList",getSensor.getOutput());
        Map<String, String> retMap = getParameterValueList((String)ret.getValue());
        Sensor sensor =  createSensor(uuid, sensorPath, retMap);
        return sensor;
	}
}
