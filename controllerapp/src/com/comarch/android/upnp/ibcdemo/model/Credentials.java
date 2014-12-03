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

package com.comarch.android.upnp.ibcdemo.model;

public class Credentials {
    private String jid;
    private String password;
    private String server;
    private int port;
    private String pubsub;
    private String uuid;
    private String controlPointName;

    public Credentials() {
    }
    
    public Credentials(String jid, String password, String server, int port,String pubsub,String uuid,String cpName) {
        this.jid = jid;
        this.password = password;
        this.server = server;
        this.port = port;
        this.pubsub = pubsub;
        this.uuid = uuid;
        this.controlPointName = cpName;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPubsub() {
        return pubsub;
    }

    public void setPubsub(String pubsub) {
        this.pubsub = pubsub;
    }

    public void setUuid(String uuid){
    	this.uuid = uuid;
    }
    
    public String getUuid(){
    	return uuid;
    }
    
    public String getControlPointName() {
    	if(controlPointName==null || controlPointName.isEmpty()){
    		return getLoginFromJid(getJid());
    	}
		return controlPointName;
	}

	public void setControlPointName(String controlPointName) {
		this.controlPointName = controlPointName;
	}
	
    private String getLoginFromJid(String jid){
    	if(jid==null) return "";
    	int index = jid.indexOf("@");
    	if(index<0) return "";
    	return jid.substring(0, index);
    }

	@Override
    public String toString() {
        return "CredentialsData [jid=" + jid + ", server=" + server + ", port=" + port + "]";
    }

}
