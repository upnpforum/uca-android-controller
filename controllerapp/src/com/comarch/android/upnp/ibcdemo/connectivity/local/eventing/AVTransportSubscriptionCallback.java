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

import java.util.HashMap;
import java.util.Map;

import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.state.StateVariableValue;

import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingDevicePropertyChanged;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;

import de.greenrobot.event.EventBus;

public class AVTransportSubscriptionCallback extends BaseSubscriptionCallback {

	private final MediaRenderer renderer;
	private final EventBus bus;
	
	@SuppressWarnings("rawtypes")
	public AVTransportSubscriptionCallback(MediaRenderer renderer,
			EventBus bus, Service service) {
		super(renderer, bus, service);
		
		this.renderer = renderer;
		this.bus = bus;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void eventReceived(GENASubscription sub) {
		Map<String, StateVariableValue> values = sub.getCurrentValues();
		StateVariableValue lastChange = values.get("LastChange");
		if (lastChange == null) return;
		
		String changeString = (String) lastChange.getValue();
		Map<String, Object> changes = new HashMap<String, Object>();
		boolean stateChanged = renderer.processLastChange(changeString, changes);
		
		if (stateChanged) {
			bus.post(new ClingDevicePropertyChanged(renderer, changes));
		}
	}

}
