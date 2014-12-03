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

package com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data;

import android.util.Log;

import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;

public class DeviceDescription {

    private final String TAG = getClass().getSimpleName();
	private final String jid;
    private final String uuid;
    private final String resource;
    private boolean deviceDescriptionNeeded = false;
    
    private SourcedDeviceUpnp boundInstace;
    
    public DeviceDescription(String jid, String uuid){
        this.jid = jid;
        this.uuid = uuid;
        this.resource = findResourceString(jid);
    }

    private static String findResourceString(String jid) {
    	int slashIndex = jid.indexOf('/');
    	if (slashIndex < 0) return null;
    	return jid.substring(slashIndex + 1);
    }
    
    public SourcedDeviceUpnp getBoundDevice() {
    	return this.boundInstace;
    }
    
    public void bindInstance(SourcedDeviceUpnp instance) {
    	if (this.uuid.equals(instance.getUuid())) {
    		this.boundInstace = instance;
    		instance.setDescription(this);
    	}
    }
    
    public String getResource() {
    	return resource;
    }
    
    public String getJid() {
        return jid;
    }

    public String getUuid() {
        return uuid;
    }
    
    public String getDeviceType(){
    	String result = null;
    	try
    	{
    		result = resource.substring(0, resource.indexOf(":uuid"));
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG, e.toString());
    	}
    	return result;
    }

    public boolean isDeviceDescriptionNeeded() {
        return deviceDescriptionNeeded;
    }

    public void setDeviceDescriptionNeeded(boolean deviceDescriptionNeeded) {
        this.deviceDescriptionNeeded = deviceDescriptionNeeded;
    }
    
    
}
