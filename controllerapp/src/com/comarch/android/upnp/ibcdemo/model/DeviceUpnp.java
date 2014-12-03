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

import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data.DeviceDescription;
import com.comarch.android.upnp.ibcdemo.model.interfaces.IDeviceUpnp;


public class DeviceUpnp implements IDeviceUpnp {

    private DeviceDescription description;
    private final String uuid;
    private String configIdCloud = null;

    private String name;

    protected DeviceUpnp(String uuid) {
        this.uuid = uuid;
    }

    protected DeviceUpnp(DeviceUpnp device) {
        this.uuid = device.getUuid();
        this.description = device.getDescription();
        this.configIdCloud = device.getConfigIdCloud();
        setName(device.getName());
    }

    protected DeviceUpnp(String uuid,String name){
        this.uuid = uuid;
        this.name = name;
    }
    
    public String getKey(){
    	return getUuid();
    }
    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(IDeviceUpnp another) {
        if (uuid == null && another.getUuid() == null) {
            return 0;
        } else if (another.getUuid() == null) {
            return -1;
        }
        return uuid.compareTo(another.getUuid());
    }

    @Override
    public int hashCode() {
        return uuid == null ? 0 : uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeviceUpnp other = (DeviceUpnp) obj;
        if (uuid == null) {
            return false;
        } else if (other.uuid == null) {
            return false;
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DeviceUpnp [uuid=" + uuid + ", name=" + name + "]";
    }

    public DeviceDescription getDescription() {
        return description;
    }

    public void setDescription(DeviceDescription description) {
        this.description = description;
    }

    public String getConfigIdCloud() {
        return configIdCloud;
    }

    public void setConfigIdCloud(String configIdCloud) {
        this.configIdCloud = configIdCloud;
    }

}
