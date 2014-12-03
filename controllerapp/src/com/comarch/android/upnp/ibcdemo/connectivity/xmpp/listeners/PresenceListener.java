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

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import android.util.Log;

import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.PacketListenerWithFilter;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data.XmppDevicesStateObserver;

public class PresenceListener implements PacketListenerWithFilter{

    private final String TAG = getClass().getSimpleName();
    private XmppDevicesStateObserver observer;
    
    public PresenceListener(XmppDevicesStateObserver observer) {
        super();
        this.observer = observer;
    }
    @Override
    public void processPacket(Packet packet) {
        Presence presence = (Presence) packet;
        String configIdCloud = null;
        if(packet.getFrom().equals(packet.getTo())) return;
        try{
            DefaultPacketExtension extension = (DefaultPacketExtension) presence.getExtension("urn:schema-upnp-org:cloud-1-0");
            if(extension!=null){
                configIdCloud = extension.getValue("configIdCloud");
            }
        }catch(Exception e){
            Log.e(TAG,"Exception",e);
        }
        if(presence.getType()==Presence.Type.available){
            if(observer!=null){
                observer.onNewDeviceConnected(packet.getFrom(),configIdCloud,presence.getStatus());
            }
        }else if(presence.getType()==Presence.Type.unavailable){
            if(observer!=null){
                observer.onDeviceDisconnected(packet.getFrom());
            }
        }
    }

    @Override
    public boolean accept(Packet packet) {
        return packet.getClass().equals(Presence.class);
    }

}
