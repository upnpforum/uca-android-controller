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

import org.jivesoftware.smack.packet.Packet;

import android.util.Log;

import com.comarch.android.upnp.ibcdemo.connectivity.common.UpnpActionCallback;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.PacketListenerWithFilter;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.SoapResponseIQ;

public class CallbacksListener implements PacketListenerWithFilter{
    private final String TAG = getClass().getName();
    
    private Map<String,UpnpActionCallback> callbacks;
    
    public CallbacksListener(){
        callbacks = new HashMap<String,UpnpActionCallback>();
    }
    @Override
    public void processPacket(Packet packet) {
        
        String key = packet.getPacketID();
        Log.i(TAG,"Run callback for "+key);
        UpnpActionCallback clb =callbacks.get(key);
        HashMap<String,Object> response = new HashMap<String, Object>();
        for(String name : ((SoapResponseIQ)packet).getArgumentNames()){
        	response.put(name, ((SoapResponseIQ)packet).getArgumentValue(name));
        }
        clb.setResponse(response);
        clb.run();
        callbacks.remove(key);
    }

    @Override
    public boolean accept(Packet packet) {
        return callbacks.containsKey(packet.getPacketID());
    }
    
    public void registerCallback(String key,UpnpActionCallback runnable){
        Log.i(TAG,"Register callback for "+key);
        callbacks.put(key,runnable);
    }
}
