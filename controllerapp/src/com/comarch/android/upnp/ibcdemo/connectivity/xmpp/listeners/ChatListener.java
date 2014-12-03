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

import java.util.Date;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import android.content.Context;
import android.util.Log;

import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.XmppConnector;
import com.comarch.android.upnp.ibcdemo.persistence.ChatDao;

public class ChatListener extends CLPacketListener {

    private final String TAG = getClass().getSimpleName();
    private ChatDao mChatDao;
    
	public ChatListener(XmppConnector connector, Context ctx) {
		super(connector);
		mChatDao = new ChatDao(ctx);
	}
	
    @Override
    public void processPacket(Packet packet) {
    	Message msg = (Message) packet; 	
    	if(msg.getBody()!=null){
    		String fromUuid = getUuidFromJid(msg.getFrom());
    		Log.d(TAG,"Recived msg from "+msg.getFrom()+" with body "+msg.getBody());
			if (fromUuid != null) {
				try {
					mChatDao.open();
					mChatDao.add(fromUuid, getUuidFromJid(msg.getTo()),
							msg.getBody(), new Date().getTime());
					connector.chatMessageRecived(msg.getFrom(), msg.getBody());
				} finally {
					mChatDao.close();
				}
			}
    	}
    }
    
    public String getUuidFromJid(String jid){
    	String sepatator = ":ControlPoint:1:";
    	int index = jid.indexOf(sepatator);
    	if(index>0 && jid.length()>index+sepatator.length()){
    		return jid.substring(index+sepatator.length());
    	}
    	Log.d(TAG,"Unhandled message from "+jid);
    	return null;
    }

    @Override
    public boolean accept(Packet packet) {
        return packet.getClass().equals(Message.class);
    }
    


}
