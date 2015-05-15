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

package com.comarch.android.upnp.ibcdemo.ui;

import java.util.HashMap;
import java.util.Map;

import com.comarch.android.upnp.ibcdemo.XmlUtils;
import com.comarch.android.upnp.ibcdemo.busevent.UpnpServiceActionEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.data.MediaServerBrowseResponse;
import com.comarch.android.upnp.ibcdemo.connectivity.common.UpnpActionCallback;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingServiceActionEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingServiceActionEvent.Service;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppAVTGetProtocolInfoEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppMediaServerBrowseEvent;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;
import com.comarch.android.upnp.ibcdemo.model.MediaServer;
import com.comarch.android.upnp.ibcdemo.model.Source;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;
import com.comarch.android.upnp.ibcdemo.model.mediaserver.Directory;

import de.greenrobot.event.EventBus;

public class Broker {
    
    private EventBus bus;
    
    public Broker(EventBus bus) {
        this.bus = bus;
    }
    
    public void onBrowseMediaServer(final MediaServer mediaServer,final String dirId,String parentId){
		if(mediaServer.getSources().contains(Source.XMPP)){
			bus.post(new XmppMediaServerBrowseEvent(mediaServer,dirId,parentId));
		}
		if(mediaServer.getSources().contains(Source.LOCAL)){
			Map<String,String> args = new HashMap<String, String>();
	        args.put("ObjectID", dirId);
	        args.put("BrowseFlag", "BrowseDirectChildren");
	        args.put("Filter", "*");
	        args.put("StartingIndex", "0");
	        args.put("RequestedCount", "0");
	        args.put("SortCriteria", "");
	        UpnpActionCallback callback = new UpnpActionCallback() {
				
				@Override
				public void run() {
			        String result = (String) getResponse().get("Result");
			        Directory dir = new Directory(dirId);
			        boolean success = dir.setChildrenFromResult(XmlUtils.unescapeXML(result));
			        if(success){
			        	bus.post(new MediaServerBrowseResponse(mediaServer.getUuid(),dir));
			        }
				}
			};
			bus.post(new ClingServiceActionEvent(mediaServer,new Service("schemas-upnp-org","ContentDirectory"),"Browse",args,callback));
		}
    }

    public void onAVGetProtocolInfo(final SourcedDeviceUpnp sourcedDeviceUpnp) {
        bus.post(new XmppAVTGetProtocolInfoEvent(sourcedDeviceUpnp));
    }

    public void onMediaRendererAction(MediaRenderer mediaRenderer, String actionName, Map<String, String> args) {
       this.onMediaRendererAction(mediaRenderer, actionName, args, null);
    }
    
    public void onMediaRendererAction(MediaRenderer mediaRenderer, String actionName, Map<String, String> args,UpnpActionCallback callback) {
    	onUpnpAction(mediaRenderer,MediaRenderer.AV_TRANSPORT_SERVICE, actionName, args, callback);
     }

    public void onRenderingControlAction(MediaRenderer mediaRenderer, String actionName, Map<String, String> args,UpnpActionCallback callback) {
    	onUpnpAction(mediaRenderer, "urn:schemas-upnp-org:service:RenderingControl:1", actionName, args, callback);    
    }

	public void onUpnpAction(SourcedDeviceUpnp device,String serviceName,String actionName,Map<String, String> args, UpnpActionCallback callback) {
		bus.post(new UpnpServiceActionEvent(device, serviceName, actionName, args, callback));
	}

}
