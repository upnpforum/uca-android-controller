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
package com.comarch.android.upnp.ibcdemo.connectivity.local.eventing;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.state.StateVariableValue;

import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingDevicePropertyChanged;
import com.comarch.android.upnp.ibcdemo.model.DeviceUpnp;
import com.comarch.android.upnp.ibcdemo.model.RGBDimmableLight;

import de.greenrobot.event.EventBus;

public class ColorLightSubscriptionCallback extends BaseSubscriptionCallback {
	private static final Pattern COLOR_PATTERN = Pattern.compile("(.*)<RGB>(\\d+),(\\d+),(\\d+)</RGB>(.*)");
	
	@SuppressWarnings("rawtypes")
	public ColorLightSubscriptionCallback(DeviceUpnp device,EventBus eventBus, Service service) {
		super(device, eventBus, service);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void eventReceived(GENASubscription sub) {
		Map<String, StateVariableValue> values = sub.getCurrentValues();
		StateVariableValue color = values.get("LastColor");
		if(color==null) return;
		
		String colorString = (String) color.getValue();
		Matcher matcher = COLOR_PATTERN.matcher(colorString);
		if(matcher.matches()){
			String r = matcher.group(2);
			String g = matcher.group(3);
			String b = matcher.group(4);
			int rgb = getRGB(r, g, b);
			if(rgb > 0 && rgb != getDevice().getColor()){
				getDevice().setColor(rgb);
				getBus().post(new ClingDevicePropertyChanged(getDevice(),"Color",rgb));
			}
		}

	}
	private int getRGB(String r,String g,String b){
		try{
			int ri = Integer.parseInt(r);
			int gi = Integer.parseInt(g);
			int bi = Integer.parseInt(b);
			return (ri << 16) + (gi << 8) + bi;
		}catch(Exception e){
			return -1;
		}		
	}
	private RGBDimmableLight getDevice(){
		return (RGBDimmableLight) mDevice;
	}

}
