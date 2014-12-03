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

/**
 * 
 */
package com.comarch.android.upnp.ibcdemo.connectivity.xmpp.providers;

import java.io.IOException;
import java.util.HashMap;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.SoapResponseIQ;


public class SOAPProvider implements IQProvider {

    public final static String ELEMENT_NAME = "Envelope";
    public static final String NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";

	@Override
	public IQ parseIQ(XmlPullParser parser) throws Exception {
        boolean stop = false;
        String name = null;
        SoapResponseIQ iq = null;

        while(false == stop)
        {
            name = parser.getName();
            switch (parser.getEventType())
            {
                case XmlPullParser.START_TAG:
                {
                    if(ELEMENT_NAME.equals(name))
                    {
                        iq = parseSoap(parser);
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
	
	private static final String extractActionName(final String responseTag) {
		int pos = responseTag.indexOf("Response");
		if (pos < 1) {
			return null;
		}
		return responseTag.substring(0, pos);
	}
	
	public SoapResponseIQ parseSoap(XmlPullParser parser) throws XmlPullParserException, IOException {
		String actionName = null;
		HashMap<String, String> arguments = new HashMap<String, String>();

        boolean stop = false;
        String lastOpenTag = null;
        
        while (false == stop) {
            if(parser.getEventType() == XmlPullParser.START_TAG){
                lastOpenTag = parser.getName();
                if(parser.getName().endsWith("Response")){
                    actionName = extractActionName(parser.getName());
                    //arguments = parseArguments(parser);
                }
            }else if(parser.getEventType() ==XmlPullParser.TEXT){
                
            	String argName = lastOpenTag;
            	String value = parser.getText();
            	
            	arguments.put(argName, value);
            	
            }else if(parser.getEventType() == XmlPullParser.END_TAG){
                if(parser.getName().equalsIgnoreCase(ELEMENT_NAME)){
                    stop=false;
                    break;
                }
            }
            if(!stop){
                parser.next();
            }
        }
        return new SoapResponseIQ(actionName, arguments);
	}

    @SuppressWarnings("unused")
	private HashMap<String, String> parseArguments(XmlPullParser parser) throws XmlPullParserException, IOException {
        HashMap<String, String> arguments = new HashMap<String, String>();
        
        boolean stop = false;
        while (false == stop) {
            if(parser.getEventType() == XmlPullParser.START_TAG){
                arguments.put(parser.getName(),getContent(parser,parser.getName()));
            }else if(parser.getEventType() == XmlPullParser.END_TAG){
                if(parser.getName().equalsIgnoreCase(ELEMENT_NAME)){
                    stop=false;
                    break;
                }
            }
            if(!stop){
                parser.next();
            }
        }
        return arguments;
    }

    private String getContent(XmlPullParser parser, String elementName) throws XmlPullParserException, IOException {
        StringBuilder content = new StringBuilder();
        while(true){
            switch(parser.getEventType()){
                case XmlPullParser.END_TAG:
                    if(parser.getName().equals(elementName)){
                        return content.toString();
                    }
                    if(parser.isEmptyElementTag()){
                        content.append("<");
                        if(!parser.getNamespace().isEmpty()){
                            content.append(parser.getNamespace()+":");
                        }
                        content.append(parser.getName());
                        for(int i=0;i<parser.getAttributeCount();++i){
                            content.append(" ");
                            if(!parser.getAttributeNamespace(i).isEmpty()){
                                content.append(parser.getAttributeNamespace(i)+":");
                            }
                            content.append(parser.getAttributeName(i));
                            content.append("="+parser.getAttributeValue(i));
                        }
                        content.append(" />");
                        
                    }else{
                        content.append("</");
                        if(!parser.getNamespace().isEmpty()){
                            content.append(parser.getNamespace()+":");
                        }
                        content.append(parser.getName());
                        content.append(">");
                    }
                    break;
                case XmlPullParser.START_TAG:
                    content.append("<");
                    if(!parser.getNamespace().isEmpty()){
                        content.append(parser.getNamespace()+":");
                    }
                    content.append(parser.getName());
                    for(int i=0;i<parser.getAttributeCount();++i){
                        content.append(" ");
                        if(!parser.getAttributeNamespace(i).isEmpty()){
                            content.append(parser.getAttributeNamespace(i)+":");
                        }
                        content.append(parser.getAttributeName(i));
                        content.append("="+parser.getAttributeValue(i));
                    }
                    content.append(" >");
                    break;
                case XmlPullParser.TEXT:
                    content.append(parser.getText());
                    break;
            }
            parser.next();
        }
    }

}
