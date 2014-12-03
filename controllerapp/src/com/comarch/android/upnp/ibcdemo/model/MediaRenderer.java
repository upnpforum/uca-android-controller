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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

import com.comarch.android.upnp.ibcdemo.XmlUtils;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.SoapResponseIQ;
import com.comarch.android.upnp.ibcdemo.model.mediaserver.File;

public class MediaRenderer extends SourcedDeviceUpnp {

    public static final String TRANSPORT_STATE = "TransportState";

	public static final String AV_TRANSPORT_SERVICE = "urn:schemas-upnp-org:service:AVTransport:1";
    
	private final String TAG = getClass().getName();
    public enum TransportState{
        STOPPED,
        PLAYING,
        TRANSITIONING,
        PAUSED_PLAYBACK,
        PAUSED_RECORDING,
        RECORDING,
        NO_MEDIA_PRESENT
    }
    
    private TransportState transportState=TransportState.NO_MEDIA_PRESENT;
    private File currentFile;
    private String trackDuration;
    private Long currentTime;
    
    public MediaRenderer(MediaRenderer mediaRenderer){
        super(mediaRenderer);
        transportState = mediaRenderer.getTransportState();
        currentFile = mediaRenderer.getCurrentFile();
        trackDuration = mediaRenderer.getTrackDuration();
        currentTime = mediaRenderer.getCurrentTime();
    }
    public MediaRenderer(String uuid, String name) {
        super(uuid, name);
    }
    public boolean processLastChange(String value,Map<String,Object> changes) {
        boolean changed = false;
        Map<String,Map<String,String>> result = parseLastChangeEvent(value);
        if(result.get("0")!=null){
            Map<String,String> instanceMap = result.get("0");
            if(instanceMap.containsKey(TRANSPORT_STATE)){
            	TransportState newValue = TransportState.valueOf(instanceMap.get(TRANSPORT_STATE));
            	if(!getTransportState().equals(newValue)){
            		setTransportState(newValue);
            		changes.put(TRANSPORT_STATE,newValue);
                	changed = true;
            	}
            }
            if(instanceMap.containsKey("AVTransportURIMetaData")){
                try {
                	File newValue = getFileFromMetadata(instanceMap.get("AVTransportURIMetaData"));
                	if(newValue != null && !newValue.equals(getCurrentFile())){
                		setCurrentFile(newValue);
                		changed = true;
                		changes.put("AVTransportURIMetaData",instanceMap.get("AVTransportURIMetaData"));
                	}
                    
                } catch (XmlPullParserException e) {
                    Log.e(TAG,"Exception",e);
                } catch (IOException e) {
                    Log.e(TAG,"Exception",e);
                }
            }
            if(instanceMap.containsKey("TrackDuration")){
            	String newValue = instanceMap.get("TrackDuration");
            	if(newValue != null && !getTrackDuration().equals(newValue)){
            		changes.put("TrackDuration", newValue);
            		setTrackDuration(newValue);
            		changed = true;
            	}
            }
            if(instanceMap.containsKey("RelativeTimePosition")){
            	Long newValue = parseTime(instanceMap.get("RelativeTimePosition"));
            	if(newValue != null && !newValue.equals(getCurrentTime())){
            		setCurrentTime(newValue);
            		changes.put("RelativeTimePosition",newValue);
                	changed = true;
            	}
            }
        }
        return changed;
        
    }

	public void processGetMediaInfo(SoapResponseIQ response){
        try {
            if(response.getArgumentValue("CurrentURIMetadata")!=null){
                setCurrentFile(getFileFromMetadata(XmlUtils.unescapeXML(response.getArgumentValue("CurrentURIMetadata"))));
            }
            setTrackDuration(response.getArgumentValue("MediaDuration"));
        } catch (XmlPullParserException e) {
            Log.e(TAG,"Exception",e);
        } catch (IOException e) {
            Log.e(TAG,"Exception",e);
        }

    }
    private Long parseTime(String argumentValue) {
        if(argumentValue==null) return null;
        String[] splits = argumentValue.split(":");
        Long retVal = null;
        if(splits.length==3){
            try{
                long h = Long.parseLong(splits[0]);
                long m = Long.parseLong(splits[1]);
                long s = Long.parseLong(splits[2]);
                retVal = (h*60*60+m*60+s)*1000;
            }catch(NumberFormatException e){
            }
        }
        return retVal;
    }
    private File getFileFromMetadata(String value) throws XmlPullParserException, IOException{
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(value));
        int eventType = parser.getEventType();
        File resource=null;
        while (eventType != XmlPullParser.END_DOCUMENT) {

            String tagName = parser.getName();

            switch (eventType) {
            case XmlPullParser.START_DOCUMENT:
                break;
            case XmlPullParser.START_TAG:
                tagName = parser.getName();
                if(tagName.equalsIgnoreCase("item")){
                    resource = new File(parser.getAttributeValue(null,"id"),parser.getAttributeValue(null,"parentID"));
                    resource.parse(parser);
                }
                break;
            case XmlPullParser.END_TAG:
                break;
            }
            eventType = parser.next();
        }
        return resource;
    }
    
    private Map<String, Map<String, String>> parseLastChangeEvent(String eventString) {
        Map<String,Map<String,String>> instancesValues = new HashMap<String,Map<String,String>>();
        Map<String,String> currentInstance = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(eventString));
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                case XmlPullParser.START_TAG:
                    if(parser.getName().equals("InstanceID")){
                        currentInstance = new HashMap<String,String>();
                        instancesValues.put(parser.getAttributeValue(null, "val"),currentInstance );
                    }else{
                        String value = parser.getAttributeValue(null, "val");
                        if(value!=null){
                            currentInstance.put(parser.getName(), value);
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    String value = parser.getAttributeValue(null, "val");
                    if(value!=null){
                        currentInstance.put(parser.getName(), value);
                    }
                    break;
                }
                eventType = parser.next();
            }
            
            return instancesValues;
        } catch (XmlPullParserException e) {
            Log.e(TAG,"Exception",e);
        } catch (IOException e) {
            Log.e(TAG,"Exception",e);
        }
        return instancesValues;
    }
    
    public TransportState getTransportState() {
        return transportState;
    }
    public void setTransportState(TransportState transportState) {
        this.transportState = transportState;
    }
    public File getCurrentFile() {
        return currentFile;
    }
	private void setCurrentFile(File currentFile) {
		this.currentFile = currentFile;
	}
    public String getTrackDuration() {
        return trackDuration;
    }
    private void setTrackDuration(String trackDuration) {
		this.trackDuration = trackDuration;
	}
    public Long getCurrentTime() {
        return currentTime;
    }
    public void setCurrentTime(Long currentTime) {
        this.currentTime = currentTime;
    }
    
    
}
