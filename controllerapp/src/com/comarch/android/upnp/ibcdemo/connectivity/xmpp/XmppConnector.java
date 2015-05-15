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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.provider.ProviderManager;

import android.content.Context;
import android.util.Log;

import com.comarch.android.upnp.ibcdemo.busevent.ConnectionStateChangedEvent.ConnectionState;
import com.comarch.android.upnp.ibcdemo.busevent.ControlPointNameChanged;
import com.comarch.android.upnp.ibcdemo.busevent.DeviceListRefreshRequestEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.XmppConnectionCloseRequestEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.XmppConnectionOpenRequestEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.XmppConnectionStateChangedEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.data.MediaServerBrowseResponse;
import com.comarch.android.upnp.ibcdemo.connectivity.Connector;
import com.comarch.android.upnp.ibcdemo.connectivity.SerializedTaskWorker;
import com.comarch.android.upnp.ibcdemo.connectivity.busevent.NotifyWithDevicesEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.common.BaseSensorPooling.SensorPoolingObserver;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppAVTGetProtocolInfoEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppChatMessageRecivedEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppDevicePropertyChanged;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppExceptionEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppMediaServerBrowseEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppSendChatMessageEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppServiceActionEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data.DeviceDescription;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data.DeviceDescriptionFactory;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data.DeviceDescriptionFetcher;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data.SensorDeviceDescription;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data.XmppDevicesStateObserver;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.listeners.CLConnectionListener;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.listeners.CLPacketListener;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.listeners.CallbacksListener;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.listeners.ChatListener;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.listeners.MediaServerListener;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.listeners.PresenceListener;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.SoapRequestIQ;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.DiscoItemsIQ;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.providers.SOAPProvider;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.providers.UPNPQueryProvider;
import com.comarch.android.upnp.ibcdemo.model.DeviceUpnp;
import com.comarch.android.upnp.ibcdemo.model.Sensor;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;
import com.google.common.collect.Lists;

import de.greenrobot.event.EventBus;

public class XmppConnector extends SerializedTaskWorker implements Connector<SourcedDeviceUpnp>, XmppDevicesStateObserver,
SensorPoolingObserver{

    private final String TAG = getClass().getSimpleName();
    
    private String resource;
    private String fullJid;
    
    private final EventBus bus;
    private XMPPConnection connection;
    
    private XmppEventing eventing;
    
    private Map<String,DeviceDescriptionFetcher> discoverers = new HashMap<String, DeviceDescriptionFetcher>();
    
    private Map<String,SourcedDeviceUpnp> devicesList = new HashMap<String, SourcedDeviceUpnp>(); 

    private boolean reconnectionAllowed=false;
    
    private CLConnectionListener connectionListener;
    private CallbacksListener callbacksListnener;
    private MediaServerListener mediaServerListener;
    private DeviceDescriptionFactory deviceDescriptionFactory;
    private ChatListener mChatListener;
    private XmppSensorPooling mSensorPooling;
    
    public XmppConnector(Context ctx, EventBus bus) {
        super();
        this.bus = bus;
        Log.i(TAG,"XmppConnector start");
        SmackAndroid.init(ctx);
        XMPPConnection.DEBUG_ENABLED = true;
        bus.postSticky(new XmppConnectionStateChangedEvent(ConnectionState.DISCONNECTED));
        
        ProviderManager providers = ProviderManager.getInstance();
        providers.addIQProvider(UPNPQueryProvider.ELEMENT_NAME,UPNPQueryProvider.NAMESPACE,new UPNPQueryProvider());
        providers.addIQProvider(SOAPProvider.ELEMENT_NAME, SOAPProvider.NAMESPACE, new SOAPProvider());

        callbacksListnener = new CallbacksListener();
        mediaServerListener = new MediaServerListener(this);
        deviceDescriptionFactory = new DeviceDescriptionFactory(ctx);
        mChatListener = new ChatListener(this,ctx);
        mSensorPooling = new XmppSensorPooling(bus);
    }

    public boolean isReconnectionAllowed(){
        return reconnectionAllowed;
    }
    public void onEvent(XmppSendChatMessageEvent event){
    	if(connection!=null && connection.isConnected()){
    		Message message = new Message();
    		message.setBody(event.getBody());
    		message.setTo(event.getToUuid());
    		connection.sendPacket(message);
    	}
    }
    public void onEvent(XmppConnectionOpenRequestEvent event) {
        login(event.getLogin(), event.getPassword(), event.getHostname(), event.getPort(),event.getPubsubService(),event.getUuid(),event.getControlPointName());
    }

    public void onEvent(XmppConnectionCloseRequestEvent event) {
        disconnect();
    }
    
    public void onEvent(XmppConnectionStateChangedEvent event){
        if(event.getState()==ConnectionState.DISCONNECTED){
            devicesList.clear();
            bus.post(new NotifyWithDevicesEvent());
            mSensorPooling.stop();
        }else if(event.getState()==ConnectionState.CONNECTED){
        	mSensorPooling.start();
        	
        }
    }
    
    public void onEvent(DeviceListRefreshRequestEvent event){
        if(connection!=null && connection.isConnected()){
            notifyUi();
        }
    }
    
    public void onEvent(XmppServiceActionEvent event){
        SoapRequestIQ request = new SoapRequestIQ(event.getActionName(),event.getServiceName(),event.getArgs());
        request.setTo(event.getDevice().getDescription().getJid());
        if(event.getCallback()!=null){
            callbacksListnener.registerCallback(request.getPacketID(), event.getCallback());
        }
        connection.sendPacket(request);
    }
    public void onEvent(final XmppMediaServerBrowseEvent event) throws InterruptedException{
        Map<String,String> args = new HashMap<String,String>();
        args.put("ObjectID", event.getDirectoryId());
        args.put("BrowseFlag", "BrowseDirectChildren");
        args.put("Filter", "*");
        args.put("StartingIndex", "0");
        args.put("RequestedCount", "0");
        args.put("SortCriteria", "");
        
        SoapRequestIQ request = new SoapRequestIQ("Browse","urn:schemas-upnp-org:service:ContentDirectory:1",args);
        request.setTo(event.getMediaServer().getDescription().getJid());
        
        mediaServerListener.registerPacketIdWithDirId(request.getPacketID(), event.getDirectoryId());
        connection.sendPacket(request);
    }

    public void onEvent(final XmppAVTGetProtocolInfoEvent event){
        SoapRequestIQ request = new SoapRequestIQ("GetProtocolInfo","urn:schemas-upnp-org:service:ConnectionManager:1",null);
        request.setTo(event.getJid());
        connection.sendPacket(request);
    }

    public void onEvent(ControlPointNameChanged event){
    	if(connection!=null && connection.isConnected()){
            Presence p = new Presence(Presence.Type.available, event.getName(), 42, Mode.available);
            connection.sendPacket(p);
    	}
    }
    public void close() {
        disconnect();
        stop();
    }

    private void sendDiscoItems() {
        DiscoItemsIQ iq = new DiscoItemsIQ();
        connection.sendPacket(iq);
    }

    public void login(final String login, final String password, final String hostname, final int port,final String pubsub,final String uuid, final String cpName) {
        addTask(new Runnable() {
            @Override
            public void run() {
                String[] loginArray = login.split("@");
                String userName = loginArray[0];
                String domain = loginArray[1];
                boolean result = false;
                bus.postSticky(new XmppConnectionStateChangedEvent(ConnectionState.CONNECTING));

                ConnectionConfiguration config = new ConnectionConfiguration(hostname, port, domain);
                config.setReconnectionAllowed(false);
                config.setSecurityMode(SecurityMode.disabled);
                config.setSASLAuthenticationEnabled(true);
                config.setSocketFactory(new CLSocketFactory());

                connection = new XMPPConnection(config);
                if(connectionListener!=null){
                    connectionListener.closeListener();
                }
                connectionListener = new CLConnectionListener(XmppConnector.this,connection,bus);

                try {
                    connection.connect();

                    if (connection.isConnected() && !connection.isAuthenticated()) {
                        reconnectionAllowed = true;
                        resource = createNewResourceString(uuid);
                        fullJid = login + "/" + resource;
                    	connection.login(userName, password, resource);

                        Presence p = new Presence(Presence.Type.available, cpName, 42, Mode.available);
                        connection.sendPacket(p);
                        addPacketListener(new CLPacketListener(XmppConnector.this));
                        addPacketListener(new PresenceListener(XmppConnector.this));
                        addPacketListener(new MediaServerListener(XmppConnector.this));
                        addPacketListener(mChatListener);
                        addPacketListener(callbacksListnener);
                        addPacketListener(mediaServerListener);
                        
                        eventing = new XmppEventing(connection, XmppConnector.this, fullJid, pubsub);
                        mSensorPooling.setObserver(XmppConnector.this);

                        sendDiscoItems();
                    }
                    connection.addConnectionListener(new CLConnectionListener(XmppConnector.this,connection,bus));
                    result = connection.isConnected() && connection.isAuthenticated();

                } catch (XMPPException e) {
                    Log.e(TAG, "Could not login!", e);
                    notifyUIAboutException(e);
                    connectionListener.closeListener();
                    result = false;
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Could not login!", e);
                    notifyUIAboutException(e);
                    connectionListener.closeListener();
                    result = false;
                }

                bus.postSticky(new XmppConnectionStateChangedEvent(result ? ConnectionState.CONNECTED
                        : ConnectionState.DISCONNECTED));
            }

        });
    }
    
    private void notifyUIAboutException(Exception e) {
        String msg = e.getMessage();
        if(msg.contains("authentication failed") || msg.contains("No response from the server.") || msg.contains("Not connected to server")){
            bus.post(new XmppExceptionEvent("XMPP can not authenticate"));
        }else if(msg.contains("XMPPError connecting to")){
            bus.post(new XmppExceptionEvent("XMPP can not connect to server"));
        }
    }
    
    private static String createNewResourceString(String uuid) {
    	return "urn:schemas-upnp-org:cloud-1-0:ControlPoint:1:" + uuid;
    }
    
    public boolean addPacketListener(PacketListenerWithFilter pfwl){
        if(this.connection!=null){
            this.connection.addPacketListener(pfwl, pfwl);
            return true;
        }
        return false;
    }
    
    public void removePacketListener(PacketListenerWithFilter pfwl){
        if(this.connection!=null){
            this.connection.removePacketListener(pfwl);
        }
    }
    
    public void sendPacket(final Packet packet){
        sendPacketAsync(packet);
    }
    
    private void sendPacketAsync(final Packet packet){
        addTask(new Runnable(){
            @Override
            public void run() {
            	try {
            		connection.sendPacket(packet);
            	} catch (Exception e) {
            		Log.e(TAG, "", e);
            	}
            }
        });
    }
    
    public void disconnect() {
        addTask(new Runnable() {
            @Override
            public void run() {

                if (connection != null && connection.isConnected()) {
                    eventing.finish();
                    connection.disconnect();
                    mSensorPooling.stop();
                } else {
                    Log.d(TAG, "Trying to disconnect disconnected connection.");
                }
                if(connectionListener!=null){
                    connectionListener.closeListener();
                }
                reconnectionAllowed=false;
                bus.postSticky(new XmppConnectionStateChangedEvent(ConnectionState.DISCONNECTED));
            }
        });
    }

    public void getDeviceDetail(DeviceDescription deviceDescription, String status){
        DeviceDescriptionFetcher ddf = new DeviceDescriptionFetcher(this,deviceDescription);
        ddf.setDeviceStatus(status);
        discoverers.put(deviceDescription.getJid(), ddf);
        ddf.registerObserver(this);
        ddf.start();
    }

    private void notifyUi() {
        addTask(new Runnable() {
            @Override
            public void run() {
                bus.post(new NotifyWithDevicesEvent());
            }
        });
    }
    
    @Override
    public void refresh() {
        notifyUi();
    }

    @Override
    public Collection<SourcedDeviceUpnp> getDeviceList() {
        return Lists.newArrayList(devicesList.values());
    }

	@Override
	public void onDeviceFound(final DeviceDescription deviceDescription) {
		if(deviceDescription instanceof SensorDeviceDescription){
			SensorDeviceDescription sdd = (SensorDeviceDescription) deviceDescription;
			for(Sensor sensor : sdd.getSensors()){
				devicesList.put(sdd.getJid()+"/"+sensor.getKey(), sensor);
				mSensorPooling.addSensor(sensor);
			}
			notifyUi();
		}else{
		    devicesList.put(deviceDescription.getJid(),deviceDescription.getBoundDevice());
			notifyUi();
	
			addTask(new Runnable() {
				@Override
				public void run() {
					eventing.onNewDeviceDiscovered(deviceDescription);
				}
			});
		}
	}

	@Override
	public void onNoDevicesFound() {
		notifyUi();
	}

	@Override
	public void onDevicePropertiesChanged(DeviceUpnp device,Map<String,Object> changes) {
		bus.post(new XmppDevicePropertyChanged(device,changes));
		//notifyUi();
	}

    @Override
    public void onNewDeviceConnected(String jiid,String hash,String status) {
        if(connection!=null && connection.isConnected()){
            DeviceDescription dd = deviceDescriptionFactory.createDeviceDescription(jiid,hash);
            if(dd!=null){
                getDeviceDetail(dd,status);
            }
        }
    }

    @Override
    public void onDeviceDisconnected(String jidd) {
    	String type = deviceDescriptionFactory.getTypeFromJid(jidd);
    	if("SensorManagement".equalsIgnoreCase(type)){
    		List<String> keysToRemove = new ArrayList<String>();
    		for(String key : devicesList.keySet()){
    			if(key.startsWith(jidd)){
    				mSensorPooling.removeSensor((Sensor) devicesList.get(key));
    				keysToRemove.add(key);
    			}
    		}
    		for(String key : keysToRemove){
    			devicesList.remove(key);
    		}
    	}else{
	        if(devicesList.containsKey(jidd)){ 
	            devicesList.remove(jidd);
	        }
    	}
        if(discoverers.containsKey(jidd)){
            discoverers.get(jidd).stop();
        }
        if(connection!=null && connection.isConnected()){
            notifyUi();
        }
    }

    @Override
    public void onDeviceDiscoveredFinished(DeviceDescription deviceDescription) {
        //lights.put(deviceDescription.getJid(),deviceDescription.getBoundDevice());
        //notifyUi();
    }

    public void onMediaServerBrowseResponse(MediaServerBrowseResponse mediaServerBrowseResponse) {
        bus.post(mediaServerBrowseResponse);
        
    }

	public void chatMessageRecived(String from, String body) {
		bus.post(new XmppChatMessageRecivedEvent(from,body));
	}
	
}
