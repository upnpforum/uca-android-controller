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
package com.comarch.android.upnp.ibcdemo.connectivity.local;

import java.util.Collection;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.comarch.android.upnp.ibcdemo.busevent.ConnectionStateChangedEvent.ConnectionState;
import com.comarch.android.upnp.ibcdemo.busevent.DeviceListRefreshRequestEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.LocalConnectionRequestEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.LocalConnectionStateChangedEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.Connector;
import com.comarch.android.upnp.ibcdemo.connectivity.SerializedTaskWorker;
import com.comarch.android.upnp.ibcdemo.connectivity.busevent.NotifyWithDevicesEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingAddUpnpDeviceEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingDevicePropertyChanged;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingRemoveUpnpDeviceEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingRequestRefreshEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingRequestRereadEvent;
import com.comarch.android.upnp.ibcdemo.model.Sensor;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.greenrobot.event.EventBus;

public class LocalConnector extends SerializedTaskWorker implements Connector<SourcedDeviceUpnp> {

    private final String TAG = getClass().getSimpleName();

    private final int POOLING_REFRESH_TIME = 2000;
    
    private Context mContext;
    private EventBus bus;

    private Map<String,SourcedDeviceUpnp> devices = Maps.newHashMap();

    public LocalConnector(EventBus bus, Context ctx) {
        this.bus = bus;
        Log.i(TAG, "LocalConnector start");
        this.mContext = ctx;
        this.bus.postSticky(new LocalConnectionStateChangedEvent(ConnectionState.DISCONNECTED));
    }

    public void connect() {
        addTask(new Runnable() {
            @Override
            public void run() {
                mContext.startService(new Intent(mContext, LocalUpnpService.class));
            }
        });

        addTask(new Runnable() {
            @Override
            public void run() {
                
                bus.post(new ClingRequestRereadEvent());
                /*
                if (!isShouldQuit()) {
                    addTask(this, POOLING_REFRESH_TIME);
                }
                */
            }
        }, POOLING_REFRESH_TIME);
    }

    public void disconnect() {
        addTask(new Runnable() {
            @Override
            public void run() {
                mContext.stopService(new Intent(mContext, LocalUpnpService.class));
                devices.clear();
                notifyUi();
            }
        });
    }

    public void onEvent(final ClingAddUpnpDeviceEvent event) {
        addTask(new Runnable() {
            @Override
            public void run() {
            	devices.put(event.getDevice().getKey(), event.getDevice());
                notifyUi();
            }
        });
    }

    public void onEvent(final ClingDevicePropertyChanged event){
        /*addTask(new Runnable() {
            @Override
            public void run() {
                notifyUi();
            }
        });*/
    }
    public void onEvent(final ClingRemoveUpnpDeviceEvent event) {
        addTask(new Runnable() {
            @Override
            public void run() {
            	if(event.getDevice() instanceof Sensor){
            		for(String key : devices.keySet()){
            			if(key.startsWith(event.getDevice().getUuid())){
            				devices.remove(key);
            			}
            		}
            	}else{
            		devices.remove(event.getDevice().getKey());
            	}
                notifyUi();
            }
        });
    }
    public void onEvent(DeviceListRefreshRequestEvent event){
    	notifyUi();
    	bus.post(new ClingRequestRefreshEvent());
    }
    public void close() {
    }

    public void onEvent(LocalConnectionRequestEvent event) {
        switch (event) {
        case OPEN:
            connect();
            break;
        case CLOSE:
            disconnect();
            break;
        }
    }

    private void notifyUi() {
        addTask(new Runnable() {
            @Override
            public void run() {
                bus.post(new NotifyWithDevicesEvent());
            }
        });
    }

    @Override
    public void refresh() {
        notifyUi();
    }

    @Override
    public Collection<SourcedDeviceUpnp> getDeviceList() {
        return Lists.newArrayList(devices.values());
    }

}
