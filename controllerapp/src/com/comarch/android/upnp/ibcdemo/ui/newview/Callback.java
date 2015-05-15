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

package com.comarch.android.upnp.ibcdemo.ui.newview;

import java.util.List;
import java.util.Map;

import com.comarch.android.upnp.ibcdemo.connectivity.common.UpnpActionCallback;
import com.comarch.android.upnp.ibcdemo.model.DeviceUpnpCollection;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;
import com.comarch.android.upnp.ibcdemo.model.MediaServer;
import com.comarch.android.upnp.ibcdemo.model.Sensor;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;

public interface Callback {

    
    public DeviceUpnpCollection getUpnpDevices();
    
    public void onDeviceSelected(SourcedDeviceUpnp device);

    public SourcedDeviceUpnp getCurrentDevice();
    
    public void finishFragment();
    
    public void browseMediaServerDirectory(MediaServer mediaServer,String dirId,String parentId);

    public void getProtocolInfo(SourcedDeviceUpnp sourcedDeviceUpnp);

    public void performMediaRendererAction(MediaRenderer mediaRenderer,String actionName,Map<String,String> args);
    
    public void performMediaRendererAction(MediaRenderer mediaRenderer,String actionName,Map<String,String> args,UpnpActionCallback callback);

    public void perfomRenderingControlAction(MediaRenderer mediaRenderer,String actionName,Map<String,String> args,UpnpActionCallback callback);
    
    public void performUpnpDeviceAction(SourcedDeviceUpnp device,String serviceName,String actionName,Map<String,String> args,UpnpActionCallback callback);
    
    public void performWriteSensor(Sensor sensor,String sensorURN, Map<String,String> values,UpnpActionCallback callback);
    
    public void performReadSensor(Sensor sensor,String sensorURN, List<String> values,UpnpActionCallback callback);

    public MediaRenderer getDefaultMediaRenderer();
    
    public void setDefaultMediaRenderer(MediaRenderer mediaRenderer);

}
