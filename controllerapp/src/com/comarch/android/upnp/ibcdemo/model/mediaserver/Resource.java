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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class Resource {

    protected String name;
    protected String id;
    protected String parentId;
    protected String protocolInfo;
    protected String createDate;
    
    protected Resource(){
        
    }
    
    public Resource(String id){
        this(id,null,null);
    }
    public Resource(String id,String parentId){
        this(id,parentId,null);
    }
    
    public Resource(String id,String parentId,String name){
        this.name = name;
        this.id = id;
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setProtocolInfo(String protocolInfo) {
        this.protocolInfo = protocolInfo;
    }
    
    public String getProtocolInfo(){
        return protocolInfo;
    }

    public void setCreateDate(String date) {
        this.createDate = date;
    }
    
    public String getCreateDate(){
        return createDate;
    }

    public abstract void parse(XmlPullParser parser) throws XmlPullParserException, IOException;
    
    @Override
    public boolean equals(Object o) {
    	if(o instanceof Resource){
    		Resource r = (Resource)o;
    		boolean result = true;
    		result &= (name==null && r.name==null) || (name.equals(r.name));
    		result &= (id==null && r.id==null) ||(id.equals(r.id));
    		result &= (parentId==null && r.parentId==null) ||(parentId.equals(r.parentId));
    		result &= (protocolInfo==null && r.protocolInfo==null) ||(protocolInfo.equals(r.protocolInfo));
    		result &= (createDate==null && r.createDate==null) ||(createDate.equals(r.createDate));
    		return result;
    	}
    	return false;
    }
}
