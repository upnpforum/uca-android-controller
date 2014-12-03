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

import java.util.Collection;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferencesHelper {

    public static final String XMPP_LOGIN = "xmpp_login";
    public static final String XMPP_PASSWORD = "xmpp_password";
    public static final String XMPP_SERVER = "xmpp_server";
    public static final String XMPP_PORT = "xmpp_port";
    public static final String XMPP_PUBSUB = "xmpp_pubsub";
	public static final String XMPP_UUID = "xmpp_uuid";
	public static final String CONTENT_URI = "accompanying_content_uri";
	public static final String CONTENT_DIDL = "accompanying_content_didl";
	public static final String CP_NAME = "xmpp_cp_name";
    
    private SharedPreferences preferences;
    
    public PreferencesHelper(Context ctx) {
        preferences =  PreferenceManager.getDefaultSharedPreferences(ctx);//ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public void putString(String key, String value) {
        Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void putInteger(String key, Integer value) {
        Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public String getString(String key) {
        return preferences.getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public Integer getInteger(String key, Integer defaultValue) {
        return preferences.getInt(key, defaultValue);
    }
    public void remove(Collection<String> keys){
    	Editor editor = preferences.edit();
    	for(String k : keys){
    		editor.remove(k);
    	}
    	editor.commit();
    }

}
