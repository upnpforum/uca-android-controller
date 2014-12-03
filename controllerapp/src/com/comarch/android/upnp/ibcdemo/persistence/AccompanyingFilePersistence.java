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
package com.comarch.android.upnp.ibcdemo.persistence;

import com.google.common.collect.Lists;

import android.content.Context;

public class AccompanyingFilePersistence {
	
	//private final static String DEFAULT_URI = "http://192.168.1.1/default.mp3";
	private final static String DEFAULT_URI = null;
	private final static String DEFAULT_DIDL = "<DIDL-Lite " +
			"xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite\" " +
			"xmlns:dc=\"http://purl.org/dc/elements/1.1\" " +
			"xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp\">" +
			"<item id=\"default\">" +
			"<dc:title>Local</dc:title>" +
			"<dc:date>1970-01-01</dc:date>" +
			"<res protocolInfo=\"http-get:*:audio/mp3:*\" size=\"1\">http://192.168.1.1/default.mp3</res>" +
			"</item></DIDL-Lite>";
	
	private PreferencesHelper helper;
	 
	public AccompanyingFilePersistence(Context ctx){
		helper = new PreferencesHelper(ctx);
	}
	
    public String getAccompanyingContentUri(){
    	return helper.getString(PreferencesHelper.CONTENT_URI, DEFAULT_URI);
    }
    public String getAccompanyingContentDIDL(){
    	return helper.getString(PreferencesHelper.CONTENT_DIDL, DEFAULT_DIDL);
    }
    
    public void setAccompanyingContent(String uri,String didl){
    	helper.putString(PreferencesHelper.CONTENT_URI, uri);
    	helper.putString(PreferencesHelper.CONTENT_DIDL,didl);
    }
    
    public void resetAccompanyingContent(){
    	helper.remove(Lists.newArrayList(PreferencesHelper.CONTENT_DIDL,PreferencesHelper.CONTENT_URI));
    }
}
