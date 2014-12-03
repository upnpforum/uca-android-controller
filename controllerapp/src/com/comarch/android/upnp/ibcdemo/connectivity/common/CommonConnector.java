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

package com.comarch.android.upnp.ibcdemo.connectivity.common;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

import com.comarch.android.upnp.ibcdemo.connectivity.Connector;
import com.comarch.android.upnp.ibcdemo.model.Source;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;
import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class CommonConnector implements Connector<SourcedDeviceUpnp> {

    private Connector<SourcedDeviceUpnp> mXmppConnector;
    private Connector<SourcedDeviceUpnp> mLocalConnector;

    public CommonConnector(Connector<SourcedDeviceUpnp> xmppConnector,Connector<SourcedDeviceUpnp> localConnector) {
        this.mXmppConnector = xmppConnector;
        this.mLocalConnector = localConnector;
        
    }

    @Override
    public void refresh() {
        mXmppConnector.refresh();
        mLocalConnector.refresh();
    }

    @Override
    public Collection<SourcedDeviceUpnp> getDeviceList() {
        // horribly unoptimal impl!
        Map<String,SourcedDeviceUpnp> baseXmpp = convertToSourcedDimmableLightMap(mXmppConnector.getDeviceList(), Source.XMPP);
        Map<String,SourcedDeviceUpnp> baseLocal = convertToSourcedDimmableLightMap(mLocalConnector.getDeviceList(), Source.LOCAL);

        Multimap<String, SourcedDeviceUpnp> multiMap = ArrayListMultimap.create();
        multiMap.putAll(Multimaps.forMap(baseXmpp));
        multiMap.putAll(Multimaps.forMap(baseLocal));
        
        Iterable<SourcedDeviceUpnp> lights = Iterables.transform(multiMap.asMap().values(), new Function<Collection<SourcedDeviceUpnp>, SourcedDeviceUpnp>() {
            @Override
            public SourcedDeviceUpnp apply(Collection<SourcedDeviceUpnp> lightsCollection) {
                EnumSet<Source> sources = EnumSet.noneOf(Source.class);
                for (SourcedDeviceUpnp light : lightsCollection) {
                    sources.addAll(light.getSources());
                }
                // we make srong assumption here that all sources on list have same state (name/switch state/brightness)
                SourcedDeviceUpnp first = Iterables.getFirst(lightsCollection, null);
                first.setSources(sources);
                return first;
            }
        });
        
        return Lists.newArrayList(lights);
    }

    private Map<String, SourcedDeviceUpnp> convertToSourcedDimmableLightMap(Collection<SourcedDeviceUpnp> devices, Source source) {
        
        for(SourcedDeviceUpnp device : devices){
            device.setSources(EnumSet.of(source));
        }
        return Maps.transformValues(Maps.uniqueIndex(devices, new Function<SourcedDeviceUpnp, String>() {
            @Override
            public String apply(SourcedDeviceUpnp device) {
                return device.getKey();
            }
        }), new SourcedDeviceUpnpConverterFunction(EnumSet.of(source)));
    }
    
}
