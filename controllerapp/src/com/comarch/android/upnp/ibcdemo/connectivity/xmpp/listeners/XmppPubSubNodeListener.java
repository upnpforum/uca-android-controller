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

package com.comarch.android.upnp.ibcdemo.connectivity.xmpp.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

import android.util.Log;

import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data.DeviceDescription;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data.XmppDevicesStateObserver;
import com.comarch.android.upnp.ibcdemo.model.DeviceUpnp;
import com.comarch.android.upnp.ibcdemo.model.DimmableLight;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;

public class XmppPubSubNodeListener implements ItemEventListener<Item> {

	private final String TAG = getClass().getSimpleName();

	private DeviceDescription description;
	private final Node node;
	private final XmppDevicesStateObserver observer;

	private boolean isListening;

	public XmppPubSubNodeListener(final DeviceDescription description,
			final Node node, final XmppDevicesStateObserver observer) {
		this.description = description;
		this.node = node;
		this.observer = observer;
		this.isListening = false;
	}

	public void startListening() {
		if (isListening == false) {
			node.addItemEventListener(this);
			isListening = true;
		}
	}

	public void stopListening() {
		if (isListening) {
			node.removeItemEventListener(this);
			isListening = false;
		}
	}

	public void setDescription(final DeviceDescription deviceDescription) {
		this.description = deviceDescription;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handlePublishedItems(ItemPublishEvent<Item> items) {
		Log.i(TAG, "PUBSUB event received!");
		for (Item item : items.getItems()) {
			if (item instanceof PayloadItem<?>) {
				PayloadItem<SimplePayload> payloadItem = (PayloadItem<SimplePayload>) item;
				SimplePayload payload = payloadItem.getPayload();
				Log.i(TAG, "as string :" + payload.toString());
				/*
				 * TODO: payload.toString() is a quick hack to allowing to not
				 * implement our own payload class
				 */
				processPayload(payload.toString());
			}
		}
	}

	private void processPayload(String rawPayload) {
		Pattern regex = Pattern
				.compile("<property><([^>]*)>(.*)</(.*)></property>");
		Matcher matcher = regex.matcher(rawPayload);

		if (matcher.find()) {
			String name = matcher.group(1);
			String value = matcher.group(2);

			// Pattern p = Pattern.compile("val=\"<(.*?)>\"");
			// Matcher m =p.matcher(value);
			// while(m.find()){
			// value =
			// value.replace("<"+m.group(1)+">",StringUtils.escapeForXML("<"+m.group(1)+">"));
			// }
			//
			Log.i(TAG, name + " changed to " + value);
			Map<String,Object> changes = new HashMap<String,Object>();
			
			DeviceUpnp device = description.getBoundDevice();
			if (device instanceof MediaRenderer) {
				if (name.equals("LastChange")) {
					boolean shouldNotify = ((MediaRenderer) device)
							.processLastChange(value,changes);
					if (shouldNotify) {
						observer.onDevicePropertiesChanged(device,changes);
					}
				}
			} else if (device instanceof DimmableLight) {
				DimmableLight light = (DimmableLight) device;
				int numericValue = -1;
				try {
					numericValue = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					Log.e(TAG, "Failed to parse evented variable value!", e);
				}

				if (numericValue == -1)
					return;
				if (name.equals("Status")) {
					Boolean newValue = (numericValue == 1);
					if(!light.isSwitched().equals(newValue)){
						light.setSwitched(newValue);
						changes.put("Status", newValue);
						observer.onDevicePropertiesChanged(device,changes);
					}
				} else if (name.equals("LoadLevelStatus")) {
					Double newValue = numericValue / 100.0;
					if(!light.getBrightness().equals(newValue)){
						light.setBrightness(newValue);
						changes.put("LoadLevelStatus", newValue);
						observer.onDevicePropertiesChanged(device,changes);
					}
				}

			}
		}
	}
}
