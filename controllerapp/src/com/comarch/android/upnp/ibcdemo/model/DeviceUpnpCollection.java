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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DeviceUpnpCollection {

    private Map<String, SourcedDeviceUpnp> devices;

    public DeviceUpnpCollection() {
        devices = new TreeMap<String, SourcedDeviceUpnp>();
    }

    public DeviceUpnpCollection(Iterable<SourcedDeviceUpnp> lights) {
        devices = new TreeMap<String, SourcedDeviceUpnp>();
        addAll(lights);
    }
    public DeviceUpnpCollection(Iterable<SourcedDeviceUpnp> aDevices,Class<? extends SourcedDeviceUpnp> clazz){
    	devices = new TreeMap<String, SourcedDeviceUpnp>();
    	addAll(aDevices,clazz);
    }

    public int getCount() {
        return devices.size();
    }

    public SourcedDeviceUpnp getDevice(String key) {
        return devices.get(key);
    }

    public SourcedDeviceUpnp getDevice(int position) {
        // FIXME inefficient implementation!
        return (SourcedDeviceUpnp) devices.values().toArray()[position];
    }

    public long getItemId(int position) {
        return -1; // NOT used
    }

    public void add(SourcedDeviceUpnp device) {
        devices.put(device.getKey(), device);
    }

    public void addAll(Iterable<? extends SourcedDeviceUpnp> _devices,Class<? extends SourcedDeviceUpnp> clazz){
        for (SourcedDeviceUpnp device : _devices) {
            if(clazz.isAssignableFrom(device.getClass())){
                devices.put(device.getKey(), device);
            }
        }
    }
    public void addAll(Iterable<? extends SourcedDeviceUpnp> alldimmableLights) {
        for (SourcedDeviceUpnp light : alldimmableLights) {
            devices.put(light.getKey(), light);
        }
    }

    public SourcedDeviceUpnp remove(String uuid) {
        return devices.remove(uuid);
    }
    
    public void setAllNotAvailable() {
        for (SourcedDeviceUpnp device : devices.values()) {
            device.setSources(EnumSet.noneOf(Source.class));
        }
    }

    public void removeAll() {
        devices.clear();
    }
    
    public void removeOther(Iterable<? extends SourcedDeviceUpnp> toLeave,Class<? extends SourcedDeviceUpnp> clazz){
        List<String> listToRemove = new ArrayList<String>();
        for(String deviceKey : devices.keySet()){
        	
            boolean toRemove = true;
            if(!clazz.isAssignableFrom(devices.get(deviceKey).getClass())){
            	continue;
            }
            for(SourcedDeviceUpnp device : toLeave){
                if(deviceKey.equals(device.getKey())){
                    toRemove=false;
                    break;
                }
            }
            if(toRemove){
                listToRemove.add(deviceKey);
            }
        }
        for(String uuid : listToRemove){
            devices.remove(uuid);
        }
    }
    public void removeOther(Iterable<? extends SourcedDeviceUpnp> toLeave){
    	removeOther(toLeave,null);
    }

    public Collection<SourcedDeviceUpnp> allDevices() {
        return devices.values();
    }
}
