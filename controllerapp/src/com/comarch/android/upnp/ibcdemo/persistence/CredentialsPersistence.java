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

import java.util.UUID;

import android.content.Context;

import com.comarch.android.upnp.ibcdemo.R;
import com.comarch.android.upnp.ibcdemo.model.Credentials;

// this is just a quick implementation using shared preferences; TODO: rewrite it to authenticator later

public class CredentialsPersistence {
    
    private PreferencesHelper helper;
    
    private String defaultServer;
    private String defaultPort;

    public CredentialsPersistence(Context ctx) {
        helper = new PreferencesHelper(ctx);
        defaultServer = ctx.getString(R.string.pref_default_server);
        defaultPort = ctx.getString(R.string.pref_default_port);
        
    }

    public void save(Credentials credentials) {
        helper.putString(PreferencesHelper.XMPP_LOGIN, credentials.getJid());
        helper.putString(PreferencesHelper.XMPP_PASSWORD, credentials.getPassword());
        helper.putString(PreferencesHelper.XMPP_SERVER, credentials.getServer());
        helper.putString(PreferencesHelper.XMPP_PORT, Integer.toString(credentials.getPort())); //TODO: use int, Luke
        helper.putString(PreferencesHelper.XMPP_UUID,credentials.getUuid());
        helper.putString(PreferencesHelper.CP_NAME, credentials.getControlPointName());
    }
    
    public Credentials load() {

        Credentials data = new Credentials();
        data.setJid(helper.getString(PreferencesHelper.XMPP_LOGIN));
        data.setPassword(helper.getString(PreferencesHelper.XMPP_PASSWORD));
        data.setServer(helper.getString(PreferencesHelper.XMPP_SERVER,defaultServer));
        data.setPort(Integer.parseInt(helper.getString(PreferencesHelper.XMPP_PORT,defaultPort)));
        data.setPubsub(helper.getString(PreferencesHelper.XMPP_PUBSUB)); //TODO: again...
        data.setUuid(helper.getString(PreferencesHelper.XMPP_UUID,UUID.randomUUID().toString()));
        data.setControlPointName(helper.getString(PreferencesHelper.CP_NAME));
        return data;
    }
    

    
}
