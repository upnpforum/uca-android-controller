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

package com.comarch.android.upnp.ibcdemo.connectivity.xmpp.providers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.UPNPDevice;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.UPNPQuery;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.UPNPRoot;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.UPNPService;


public class UPNPQueryProvider implements IQProvider {

    public static final String ELEMENT_NAME = "query";
    public static final String NAMESPACE = "urn:schemas-upnp-org:cloud-1-0";
    
    @Override
    public IQ parseIQ(XmlPullParser parser) throws Exception {
        boolean stop = false;
        String name = null;
        UPNPQuery iq = null;

        while(false == stop)
        {
            name = parser.getName();
            switch (parser.getEventType())
            {
                case XmlPullParser.START_TAG:
                {
                    if(ELEMENT_NAME.equals(name))
                    {
                        iq = parseQuery(parser);
                        stop = true;
                    }

                    break;
                }
                case XmlPullParser.END_TAG:
                {
                    stop = ELEMENT_NAME.equals(name);
                    break;
                }
            }
            if(!stop){
                parser.next();
            }
        }

        name = null;
        return iq;
    }

    UPNPQuery parseQuery(XmlPullParser parser) throws XmlPullParserException, IOException{
        UPNPQuery query = new UPNPQuery();
        UPNPRoot root = new UPNPRoot();
        UPNPDevice device = new UPNPDevice();
        root.setDevice(device);
        query.setRoot(root);
        boolean stop = false;
        String lastOpenTag = null;
        while(false == stop){
            if(parser.getEventType()==XmlPullParser.START_TAG){
                lastOpenTag = parser.getName();
                if(parser.getName().equalsIgnoreCase("serviceList")){
                    device.setServiceList(parseServices(parser));
                }
            }else if(parser.getEventType()==XmlPullParser.TEXT){
                if(lastOpenTag.equalsIgnoreCase("friendlyName")){
                    device.setFriendlyName(parser.getText());
                }else if(lastOpenTag.equalsIgnoreCase("udn")){
                    device.setUDN(parser.getText());
                }
            }else if(parser.getEventType()==XmlPullParser.END_TAG){
                if(parser.getName().equalsIgnoreCase(ELEMENT_NAME)){
                    stop=false;
                    break;
                }
            }
            if(!stop){
                parser.next();
            }
        }
        return query;
    }

    private List<UPNPService> parseServices(XmlPullParser parser) throws XmlPullParserException, IOException{
        String openTagName = parser.getName();
        List<UPNPService> serviceList = new ArrayList<UPNPService>();
        String lastOpenTag = null;
        UPNPService service = null;
        while(!(parser.getEventType()==XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase(openTagName))){
            if(parser.getEventType()==XmlPullParser.START_TAG){
                lastOpenTag = parser.getName();
                if(lastOpenTag.equalsIgnoreCase("service")){
                    service = new UPNPService();
                }
            }else if(parser.getEventType()==XmlPullParser.TEXT){
            	if(lastOpenTag.equalsIgnoreCase("serviceType")){
            		service.setServiceType(parser.getText());
            	}else if(lastOpenTag.equalsIgnoreCase("serviceId")){
            		service.setServiceId(parser.getText());
            	}
            }else if(parser.getEventType()==XmlPullParser.END_TAG){
                if(parser.getName().equalsIgnoreCase("service") && service!=null){
                    serviceList.add(service);
                    service=null;
                }
            }
            parser.next();
        }
        return serviceList;
        
    }
}
