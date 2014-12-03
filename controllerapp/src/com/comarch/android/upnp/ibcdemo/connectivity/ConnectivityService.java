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

package com.comarch.android.upnp.ibcdemo.connectivity;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.IBinder;

import com.comarch.android.upnp.ibcdemo.busevent.ActionCollectionEvent;
import com.comarch.android.upnp.ibcdemo.busevent.BaseDevicePropertyChanged;
import com.comarch.android.upnp.ibcdemo.busevent.ConnectionStateChangedEvent.ConnectionState;
import com.comarch.android.upnp.ibcdemo.busevent.UpnpServiceActionEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.LocalConnectionStateChangedEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.XmppConnectionStateChangedEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.data.UpdateDeviceListEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.data.UpdateDeviceProperty;
import com.comarch.android.upnp.ibcdemo.connectivity.busevent.AccompanyingURIChanedEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.busevent.NotifyWithDevicesEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.common.AccompanyingDevicesEventProcessor;
import com.comarch.android.upnp.ibcdemo.connectivity.common.CommonConnector;
import com.comarch.android.upnp.ibcdemo.connectivity.common.ResetDemoEngine;
import com.comarch.android.upnp.ibcdemo.connectivity.local.LocalConnector;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingServiceActionEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.XmppConnector;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppServiceActionEvent;
import com.comarch.android.upnp.ibcdemo.model.DeviceUpnp;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;
import com.comarch.android.upnp.ibcdemo.model.Source;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer.TransportState;
import com.comarch.android.upnp.ibcdemo.util.deliverer.ServiceWithBusDeliverer;

public class ConnectivityService extends ServiceWithBusDeliverer {

	@SuppressWarnings("unused")
	private final String TAG = getClass().getSimpleName();
	
    private XmppConnector xmppConnector;
    private LocalConnector localConnector;
    private CommonConnector commonConnector;
    private AccompanyingDevicesEventProcessor eventProcessor;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getBus().register(this);

        xmppConnector = new XmppConnector(this, getBus());
        xmppConnector.start();
        getBus().register(xmppConnector);
        
        localConnector = new LocalConnector(getBus(), this);
        localConnector.start();
        getBus().register(localConnector);
        
        commonConnector = new CommonConnector(xmppConnector, localConnector);
        
        getBus().postSticky(new XmppConnectionStateChangedEvent(ConnectionState.DISCONNECTED));
        getBus().postSticky(new LocalConnectionStateChangedEvent(ConnectionState.DISCONNECTED));
        
        eventProcessor = new AccompanyingDevicesEventProcessor(this,commonConnector,getBus());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        xmppConnector.close();
        localConnector.close();
        getBus().unregister(xmppConnector);
        getBus().unregister(localConnector);

        getBus().unregister(this);
    }
    
    public void onEvent(NotifyWithDevicesEvent devicesEvent) {
        getBus().post(new UpdateDeviceListEvent(commonConnector.getDeviceList()));
    }
    
    public void onEvent(BaseDevicePropertyChanged event){
    	getBus().post(new UpdateDeviceProperty(event.getDevice(),commonConnector.getDeviceList()));
    	/* filter out all events but those indicating stopped state; 
    	 * all others can be generated when action is called */
    	Map<String, Object> changes = event.getChanges();
    	if (changes.containsKey(MediaRenderer.TRANSPORT_STATE)) {
    		Object value = changes.get(MediaRenderer.TRANSPORT_STATE);
    		if (value.equals(TransportState.STOPPED)) {
    			eventProcessor.processEvent(event.getDevice(), event.getChanges());
    		}
    	}
    }
    
    public void onEvent(AccompanyingURIChanedEvent event) {
    	if (eventProcessor == null) return;
    	
    	Map<String, Object> changes = new HashMap<String, Object>();
    	changes.put("AVTransportURIMetaData", "whatever, this on will be ignored");
    	eventProcessor.processEvent(event.getMasterDevice(), changes);
    }
    
    public void onEvent(UpnpServiceActionEvent event){
    	if(event.getDevice().getSources().contains(Source.XMPP)){
    		getBus().post(new XmppServiceActionEvent(event));
		}
		if(event.getDevice().getSources().contains(Source.LOCAL)){
			getBus().post(new ClingServiceActionEvent(event));
		}
		/* control accompanying devices */
		if (event.getServiceName().equals(MediaRenderer.AV_TRANSPORT_SERVICE)
				&& event.getToAccompanying() == false) {
			String actionName = event.getActionName();
			DeviceUpnp device = event.getDevice();
			Map<String, Object> changes = new HashMap<String, Object>();
			if (actionName.equals("Play")) {
				changes.put(MediaRenderer.TRANSPORT_STATE, TransportState.PLAYING);
				//changes.put("AVTransportURIMetaData", "whatever, this on will be ignored");
				eventProcessor.processEvent(device, changes);
			} else if (actionName.equals("Pause")) {
				changes.put(MediaRenderer.TRANSPORT_STATE, TransportState.PAUSED_PLAYBACK);
				eventProcessor.processEvent(device, changes);
			} else if (actionName.equals("Stop")) {
				changes.put(MediaRenderer.TRANSPORT_STATE, TransportState.STOPPED);
				eventProcessor.processEvent(device, changes);
			} else if (actionName.equals("SetAVTransportURI")) {
				changes.put("AVTransportURIMetaData", "whatever, this on will be ignored");
				eventProcessor.processEvent(device, changes);
			}
		}
    }
    
    public void onEvent(ActionCollectionEvent e){
    	if(e.equals(ActionCollectionEvent.RESET_DEMO)){
    		new ResetDemoEngine(commonConnector.getDeviceList(),getBus()).start();
    	}
    }

}
