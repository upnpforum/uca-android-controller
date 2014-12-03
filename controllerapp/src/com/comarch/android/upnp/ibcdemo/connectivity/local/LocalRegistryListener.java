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

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import android.util.Log;

public class LocalRegistryListener implements RegistryListener {
    
	
	private final String TAG = getClass().getSimpleName();
    public LocalUpnpService mReceiver;
    private final String[] supportedDeviceType = {"DimmableLight","MediaServer","MediaRenderer","SensorManagement"};
    
    @SuppressWarnings("unused")
    private UpnpService service;
    
    public LocalRegistryListener(LocalUpnpService connector) {
        this.mReceiver = connector;
    }

    public void setService(UpnpService upnpService) {
        this.service = upnpService;
    }

    @Override
    public void afterShutdown() {
    	Log.d(TAG,"afterShutdown");
    }

    @Override
    public void beforeShutdown(Registry registry) {
    	Log.d(TAG,"beforeShutdown");
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        deviceAdded(device);
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        deviceRemoved(device);
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        deviceAdded(device);
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
    	Log.d(TAG,"remoteDeviceDiscoveryFailed");
    }

    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
    	Log.d(TAG,"remoteDeviceDiscoveryStarted");
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        deviceRemoved(device);
    }

    @Override
    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
        deviceUpdated(device);
    }
    
    @SuppressWarnings("rawtypes")
    protected void deviceAdded(final Device device) {
        if (isSupportedDeviceType(device)) {
            mReceiver.onDeviceAdded(device);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void deviceRemoved(final Device device) {
        if (isSupportedDeviceType(device)) {
            mReceiver.onDeviceRemoved(device);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void deviceUpdated(final Device device) {
        // timeout prolongation; no need to do anything
    }
    
    @SuppressWarnings("rawtypes")
    private boolean isSupportedDeviceType(Device device) {
    	for(String s : supportedDeviceType){
    		if(device.getType().implementsVersion(new UDADeviceType(s))){
    			return true;
    		}
    	}
    	return false;
    }
    

}
