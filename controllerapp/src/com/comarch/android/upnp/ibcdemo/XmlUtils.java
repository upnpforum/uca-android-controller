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

package com.comarch.android.upnp.ibcdemo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XmlUtils {
    
    public static String unescapeXML( final String xml )
    {
        Pattern xmlEntityRegex = Pattern.compile( "&(#?)([^;]+);" );
        //Unfortunately, Matcher requires a StringBuffer instead of a StringBuilder
        StringBuffer unescapedOutput = new StringBuffer( xml.length() );

        Matcher m = xmlEntityRegex.matcher( xml );
        Map<String,String> builtinEntities = null;
        String entity;
        String hashmark;
        String ent;
        int code;
        while ( m.find() ) {
            ent = m.group(2);
            hashmark = m.group(1);
            if ( (hashmark != null) && (hashmark.length() > 0) ) {
                code = Integer.parseInt( ent );
                entity = Character.toString( (char) code );
            } else {
                //must be a non-numerical entity
                if ( builtinEntities == null ) {
                    builtinEntities = buildBuiltinXMLEntityMap();
                }
                entity = builtinEntities.get( ent );
                if ( entity == null ) {
                    //not a known entity - ignore it
                    entity = "&" + ent + ';';
                }
            }
            m.appendReplacement( unescapedOutput, entity );
        }
        m.appendTail( unescapedOutput );

        return unescapedOutput.toString();
    }

    private static Map<String,String> buildBuiltinXMLEntityMap()
    {
        Map<String,String> entities = new HashMap<String,String>(10);
        entities.put( "lt", "<" );
        entities.put( "gt", ">" );
        entities.put( "amp", "&" );
        entities.put( "apos", "'" );
        entities.put( "quot", "\"" );
        return entities;
    }
    
	public static String nameWithoutNS(String tag){
		if(tag==null) return null;
		
		int i = tag.lastIndexOf(":");
		if(i>0){
			return tag.substring(i+1);
		}else{
			return tag;
		}
	}
	public static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
}
