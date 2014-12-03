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
package com.comarch.android.upnp.ibcdemo.model;

import java.util.List;
import java.util.Map;

import com.comarch.android.upnp.ibcdemo.model.interfaces.IDimmableLight;

public class SensorLight extends Sensor implements IDimmableLight{

	public final static String SWITCH_SENSOR_URN = "urn:upnp-org:smgt-surn:actuator:upnp-org:switch";
	public final static String BRIGHTNESS_SENSOR_URN = "urn:upnp-org:smgt-surn:light:upnp-org:brightness";
	
	public final static String SWITCH_SENSOR_ARG_NAME = "Switch";
	public final static String BRIGHTNES_SENSOR_ARG_NAME = "Dimming";
	
    private Double brightness;
    private Boolean switched;
    
	public SensorLight(SensorLight device) {
		super(device);
		switched = device.isSwitched();
		brightness = device.getBrightness();
	}
	
	public SensorLight(String uuid,String name,Map<String,List<String>> sensorURNs){
		super(uuid,name,sensorURNs);
		brightness = 0.0;
		switched = false;
	}

	public Double getBrightness() {
		return brightness;
	}

	public void setBrightness(Double brightness) {
		this.brightness = brightness;
	}

	public Boolean isSwitched() {
		return switched;
	}

	public void setSwitched(Boolean switched) {
		this.switched = switched;
	}

	
}
