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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

import com.comarch.android.upnp.ibcdemo.XmlUtils;

public class Directory extends Resource{

	private final String TAG = getClass().getSimpleName();
    private List<Resource> children;
    private Directory parent;
    
    public Directory(String id){
        this(id,null,null);
    }
    public Directory(String id,String parentId){
        this(id,parentId,null);
    }
    
    public Directory(String id,String parentId,String name){
        super(id,parentId,name);
        children = new ArrayList<Resource>();
    }

    public List<Resource> getChildren() {
        return children;
    }

    public void setChildren(List<Resource> children) {
        this.children = children;
    }
    public Directory getParent() {
        return parent;
    }
    public void setParent(Directory parent) {
        this.parent = parent;
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
    
    public boolean setChildrenFromResult(String result){
	    try{
	    	setChildren(getChildrenFromResult(result));
	    } catch (XmlPullParserException e) {
	        Log.e(TAG,"Exception",e);
	        return false;
	    } catch (IOException e) {
	        Log.e(TAG,"Exception",e);
	        return false;
	    }
	    return true;
    }
    private List<Resource> getChildrenFromResult(String result) throws XmlPullParserException, IOException {
        List<Resource> list = new ArrayList<Resource>();
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(result));
        int eventType = parser.getEventType();
        Resource resource = null;
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
                        list.add(resource);
                    }else if(tagName.equalsIgnoreCase("container")){
                    	resource = new Directory(parser.getAttributeValue(null,"id"),parser.getAttributeValue(null,"parentID"),"");
                    	resource.parse(parser);
                    	list.add(resource);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
                }
                eventType = parser.next();
                
            }       
        return list;
    }
}
