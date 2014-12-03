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

import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.UnsignedIntegerOneByte;

import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingDevicePropertyChanged;
import com.comarch.android.upnp.ibcdemo.model.DeviceUpnp;
import com.comarch.android.upnp.ibcdemo.model.DimmableLight;

import de.greenrobot.event.EventBus;

public class DimmableSubscriptionCallback extends BaseSubscriptionCallback {

    @SuppressWarnings("rawtypes")
	public DimmableSubscriptionCallback(DeviceUpnp device,
			EventBus eventBus, Service service) {
		super(device, eventBus, service);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void eventReceived(GENASubscription sub) {
        Map<String, StateVariableValue> values = sub.getCurrentValues();
        StateVariableValue loadLevelStatus = values.get("LoadLevelStatus");
        UnsignedIntegerOneByte value = (UnsignedIntegerOneByte) loadLevelStatus.getValue();
        Double newValue = value.getValue()/100.0;
        if( getDevice().getBrightness()!=newValue){
        	getDevice().setBrightness(newValue);
        	getBus().post(new ClingDevicePropertyChanged(getDevice(),"LoadLevelStatus",newValue));
        }
    }

    private DimmableLight getDevice(){
    	return (DimmableLight) mDevice;
    }

}
