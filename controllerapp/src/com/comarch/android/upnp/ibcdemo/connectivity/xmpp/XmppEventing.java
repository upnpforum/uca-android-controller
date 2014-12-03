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

package com.comarch.android.upnp.ibcdemo.connectivity.xmpp;

import java.util.HashMap;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PubSubManager;

import android.util.Log;

import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data.DeviceDescription;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data.XmppDevicesStateObserver;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.listeners.XmppPubSubNodeListener;
import com.comarch.android.upnp.ibcdemo.model.DimmableLight;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;

public class XmppEventing {
		
	private final static String lastChangeId = "urn:upnp-org:serviceId:AVTransport";
	private final static String switchpowerId = "urn:upnp-org:serviceId:SwitchPower:1";
	private final static String dimmingId = "urn:upnp-org:serviceId:Dimming:1";
	
	private final HashMap<String, DeviceDescription> knownDevices;
	private final HashMap<String, XmppPubSubNodeListener> listeners;
	
	private final String TAG = getClass().getSimpleName();
	private final PubSubManager manager;
	private final String fullJid;
	
	private XmppDevicesStateObserver observer;
	
	public XmppEventing( XMPPConnection connection
			           , XmppDevicesStateObserver observer
			           , String fullJid
			           , String pubsubServiceName
			           ) {
		this.manager = new PubSubManager(connection, pubsubServiceName);
		this.fullJid = fullJid;
		
		this.knownDevices = new HashMap<String, DeviceDescription>();
		this.listeners = new HashMap<String, XmppPubSubNodeListener>();
		
		this.observer = observer;
	}
	
	public void registerObserver(XmppDevicesStateObserver observer) {
		this.observer = observer;
	}
	
	public void onNewDeviceDiscovered(DeviceDescription description) {
		knownDevices.put(description.getUuid(), description);
		
		
		if(description.getBoundDevice() instanceof MediaRenderer){
		    onNewMediaRendererDiscovered(description);
		}else if(description.getBoundDevice() instanceof DimmableLight){
			onNewDimmableLightDiscovered(description);
		}
	}

	private void onNewDimmableLightDiscovered(DeviceDescription description){
		assert(description.getBoundDevice() instanceof DimmableLight);
		String resource = description.getResource();
		String statusNodeName = buildNodeName(resource, switchpowerId, "Status");
		String dimmingNodeName = buildNodeName(resource, dimmingId, "LoadLevelStatus");
		
		subscribe(statusNodeName, description);
		subscribe(dimmingNodeName, description);
	}
	
	private void onNewMediaRendererDiscovered(DeviceDescription description){
	    assert(description.getBoundDevice() instanceof MediaRenderer);
	    String resource = description.getResource();
	    
	    String lastChangeNode = buildNodeName(resource,lastChangeId,"LastChange");
	    subscribe(lastChangeNode, description);
	}
	
	private static String buildNodeName( String resource
			                           , String serviceId
			                           , String variableName
			                           ) {
		StringBuilder builder = new StringBuilder();
		builder.append(resource);
		builder.append("/");
		builder.append(serviceId);
		if(variableName!=null){
    		builder.append("/");
    		builder.append(variableName);
		}
		return builder.toString();
	}
	
	private void subscribe(String nodeName, DeviceDescription description) {
		if (listeners.containsKey(nodeName)) {
		    XmppPubSubNodeListener listener = listeners.get(nodeName);
		    listener.setDescription(description);
			listener.startListening();
		} else {
			Node node = null;
			try {
				node = manager.getNode(nodeName);
			} catch (Exception e) {
				Log.e(TAG, "Coundn't find pubsub node.", e);
			}
			
			if (node == null)
				return;
			
			XmppPubSubNodeListener listener
				= new XmppPubSubNodeListener(description, node, observer);
			listener.startListening();
			listeners.put(nodeName, listener);
			
			try {
				//SubscribeForm form = new SubscribeForm(FormType.form);
				//form.setDigestOn(true);
				//form.setDigestFrequency(1000);
				
				//node.subscribe(fullJid, form);
				node.subscribe(fullJid);
			} catch (XMPPException e) {
				Log.e(TAG, "Failed to subscribe.", e);
			}
		}
	}

	public void finish() {
		for (XmppPubSubNodeListener listener : listeners.values()) {
			listener.stopListening();
		}
	}


}
