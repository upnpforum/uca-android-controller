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

import com.comarch.android.upnp.ibcdemo.XmlUtils;
import com.comarch.android.upnp.ibcdemo.busevent.connector.data.MediaServerBrowseResponse;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.XmppConnector;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.SoapResponseIQ;
import com.comarch.android.upnp.ibcdemo.model.mediaserver.Directory;

public class MediaServerListener extends CLPacketListener {

    private Map<String,String> requestIdToDirId;
    @SuppressWarnings("unused")
	private final String TAG = getClass().getName();
    
    public MediaServerListener(XmppConnector xmppConnector) {
        super(xmppConnector);
        requestIdToDirId = new HashMap<String, String>();
    }

    @Override
    public void processPacket(Packet packet) {
        SoapResponseIQ response = (SoapResponseIQ) packet;
        String result = response.getArgumentValue("Result");
        String dirId = requestIdToDirId.get(packet.getPacketID());
        requestIdToDirId.remove(packet.getPacketID());
        Directory dir = new Directory(dirId);

        boolean success = dir.setChildrenFromResult(XmlUtils.unescapeXML(result));
        if(success){
        	connector.onMediaServerBrowseResponse(new MediaServerBrowseResponse(packet.getFrom(),dir));
        }
    }



//    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
//        String result = "";
//        if (parser.next() == XmlPullParser.TEXT) {
//            result = parser.getText();
//            parser.nextTag();
//        }
//        return result;
//    }

    @Override
    public boolean accept(Packet packet) {
        if(packet instanceof SoapResponseIQ){
            SoapResponseIQ response = (SoapResponseIQ) packet;
            if(!requestIdToDirId.containsKey(packet.getPacketID())){
                return false;
            }
            
            if("browse".equalsIgnoreCase(response.getActionName())){
                return true;
            }
        }
        return false;
    }
    
    public void registerPacketIdWithDirId(String packetId,String dirId){
        requestIdToDirId.put(packetId, dirId);
    }
}
