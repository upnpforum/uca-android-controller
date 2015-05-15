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

package com.comarch.android.upnp.ibcdemo.model.mediaserver;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

import com.comarch.android.upnp.ibcdemo.XmlUtils;

public class File extends Resource{

    private final static String DIDL_NS = "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/";
    private final static String DC_NS = "http://purl.org/dc/elements/1.1/";
    private final static String UPNP_NS = "urn:schemas-upnp-org:metadata-1-0/upnp/";
    private final String TAG = getClass().getSimpleName();
    public enum Type{
        UNKNOWN,
        AUDIO,
        VIDEO,
        IMAGE
    };
    
    private String uri;
    private Type type = Type.UNKNOWN;
    private Map<String,String> properties = new HashMap<String,String>();
        
    public File(String id,String parentId){
        super(id,parentId);
        setupType();
    }

    public File(String id,String parentId,String name,String uri){
        super(id,parentId,name);
        setUri(uri);
    }

    private void setupType(){
        if(protocolInfo==null){
            type=Type.UNKNOWN;
        }else if(protocolInfo.contains(":video/")){
            type=Type.VIDEO;
        }else if(protocolInfo.contains(":audio/")){
            type=Type.AUDIO;
        }else if(protocolInfo.contains(":image/")){
            type=Type.IMAGE;
        }else{
            type=Type.UNKNOWN;
        }
    }
    
    @Override
    public void setProtocolInfo(String protocolInfo) {
        super.setProtocolInfo(protocolInfo);
        setupType();
    }
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        if(uri!=null){
            uri = uri.trim();
        }
        this.uri = uri;
    }
    
    public Type getType(){
        return type;
    }
    
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    private void addNodeWithValue(XmlSerializer serializer,String nodeName,String value,Map<String,String> attributes) throws IllegalArgumentException, IllegalStateException, IOException{
        serializer.startTag(null, nodeName);
        if(attributes!=null){
            for(Entry<String,String> entry : attributes.entrySet()){
                serializer.attribute(null, entry.getKey(), entry.getValue());
            }
        }
        if(value!=null){
        	serializer.text(value);
        }
        serializer.endTag(null, nodeName);
    }
    public String getMetaData() {
        StringWriter writer = new StringWriter();
        XmlSerializer serializer = Xml.newSerializer();
        try {
            serializer.setOutput(writer);
            //serializer.startDocument("UTF-8", false);
            serializer.startTag(null,"DIDL-Lite");
            serializer.attribute(null,"xmlns",DIDL_NS);
            serializer.attribute(null, "xmlns:dc", DC_NS);
            serializer.attribute(null, "xmlns:upnp", UPNP_NS);
            serializer.startTag(null, "item");
            serializer.attribute(null, "id", id);
            serializer.attribute(null, "parentID", parentId);
            serializer.attribute(null, "restricted", "0");
            addNodeWithValue(serializer, "dc:title", name,null);
            addNodeWithValue(serializer, "dc:date", getCreateDate(),null);
            addNodeWithValue(serializer, "upnp:class", "object.item",null);
            addNodeWithValue(serializer, "res", uri, getProperties());
            serializer.endTag(null, "item");
            serializer.endTag(null, "DIDL-Lite");
            serializer.endDocument();
            return writer.toString();
        } catch (IllegalArgumentException e) {
            Log.e(TAG,"Exception",e);
        } catch (IllegalStateException e) {
            Log.e(TAG,"Exception",e);
        } catch (IOException e) {
            Log.e(TAG,"Exception",e);
        }
        return "";
    }

    @Override
    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        
        String tagName = parser.getName();
        String startTag = tagName;
        int eventType = parser.getEventType();
        while (true) {
            tagName = parser.getName();
            switch (eventType) {
            case XmlPullParser.START_DOCUMENT:
                break;
            case XmlPullParser.START_TAG:
                if(tagName.equalsIgnoreCase("dc:title")){
                    this.setName(XmlUtils.readText(parser));
                }else if(tagName.equalsIgnoreCase("dc:date")){
                    this.setCreateDate(XmlUtils.readText(parser));
                } else if (tagName.equalsIgnoreCase("res")) {
                    this.setProtocolInfo(parser.getAttributeValue(null, "protocolInfo"));
                    for (int i = 0; i < parser.getAttributeCount(); ++i) {
                        properties.put(parser.getAttributeName(i), parser.getAttributeValue(i));
                    }
    
                    setUri(XmlUtils.readText(parser));
                }
                break;
            case XmlPullParser.END_TAG:
                if(tagName.equalsIgnoreCase(startTag)){
                    return;
                }
            }
            eventType = parser.next();
        }
    }
    
    @Override
    public boolean equals(Object o) {
    	if(o instanceof File){
    		File f = (File)o;
    		boolean result = super.equals(o);
    		result &= (uri==null && f.uri==null) ||(uri.equals(f.uri));
    		result &= (type == f.type);
    		return result;
    	}
    	return false;
    	
    }
}
