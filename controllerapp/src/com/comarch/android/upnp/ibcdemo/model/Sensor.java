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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.comarch.android.upnp.ibcdemo.XmlUtils;
import com.google.common.collect.Lists;

public class Sensor extends SourcedDeviceUpnp {

	public final static String CONFIGURATION_MANAGER_SERVICE = "urn:schemas-upnp-org:service:ConfigurationManagement:1";
	public final static String SENSOR_TRANSPORT_GENERIC_SERVICE = "urn:schemas-upnp-org:service:SensorTransportGeneric:1";
	public final static String READ_SENSOR_ACTION = "ReadSensor";
	public final static String WRITE_SENSOR_ACTION = "WriteSensor";
	
	private Map<String,List<String>> mSensorURNs;
	private String mSensorID;
	
	public Sensor(Sensor device) {
		super(device);
		mSensorURNs = new HashMap<String,List<String>>();
		mSensorID = device.getSensorID();
		for(String s : device.getSensorURNs()){
			mSensorURNs.put(s, Lists.newArrayList(device.getDataItems(s)));
		}
	}
	
	public Sensor(String uuid,String name,Map<String,List<String>> sensorURNs){
		super(uuid, name);
		this.mSensorURNs = sensorURNs;
	}

	@Override
	public String getKey(){
		return getUuid()+getName();
	}
	
	public Collection<String> getSensorURNs(){
		return mSensorURNs.keySet();
	}
	
	public String getSensorURNWhichBegin(String begin){
		for(String s : getSensorURNs()){
			if(s.startsWith(begin))
				return s;
		}
		return null;
	}
	
	public Collection<String> getDataItems(String sensorURN){
		return mSensorURNs.get(sensorURN);
	}

	public String getSensorID() {
		return mSensorID;
	}

	public void setSensorID(String mSensorID) {
		this.mSensorID = mSensorID;
	}

	public Map<String, String> prepareWriteMap(String sensorURN, String key,String value){
		Map<String,String> values = new HashMap<String,String>();
		values.put(key, value);
		return prepareWriteMap(sensorURN, values);
	}
	
	public Map<String, String> prepareWriteMap(String sensorURN, Map<String, String> values) {
		StringBuilder dataRecord = new StringBuilder();
		dataRecord.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><DataRecords xmlns=\"urn:schemas-upnp-org:ds:drecs\" xsi:schemaLocation=\"urn:schemas-upnp-org:ds:drecs http://www.upnp.org/schemas/ds/drecs-v1-20130701.xsd\" "+
					"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		for(Entry<String, String> entry : values.entrySet()){
			dataRecord.append("<datarecord><field name=\""+entry.getKey()+"\">"+entry.getValue()+"</field></datarecord>");
		}
		dataRecord.append("</DataRecords>");
		Map<String,String> args = new HashMap<String,String>();
		args.put("SensorID", mSensorID);
		args.put("SensorURN", sensorURN);
		args.put("DataRecords", dataRecord.toString());
		return args;
	}
	
	public Map<String, String> prepareReadMap(String sensorURN, List<String> keys) {
		StringBuilder recordInfo = new StringBuilder();
		recordInfo.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><SensorRecordInfo xmlns=\"urn:schemas-upnp-org:smgt:srecinfo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "+
				"xsi:schemaLocation=\"urn:schemas-upnp-org:smgt:srecinfo http://www.upnp.org/schemas/smgt/srecinfo.xsd\" >");
		for(String key : keys){
			recordInfo.append("<sensorrecord><field name=\""+key+"\" /></sensorrecord>");
		}
		recordInfo.append("</SensorRecordInfo>");
		
		Map<String,String> args = new HashMap<String,String>();
		args.put("SensorID", mSensorID);
		args.put("SensorClientID", "SensorClientID"+mSensorID);
		args.put("SensorURN", sensorURN);
		args.put("SensorRecordInfo", recordInfo.toString());
		args.put("SensorDataTypeEnable", "0");
		args.put("DataRecordCount", "1");
		return args;
	}

	public static Map<String, Object> parseDataRecords(String dataRecords) throws XmlPullParserException, IOException {
		Map<String,Object> records = new HashMap<String,Object>();
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(dataRecords));
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = XmlUtils.nameWithoutNS(parser.getName());

            switch (eventType) {

            case XmlPullParser.START_TAG:
                if("field".equals(tagName)){
                	String name = parser.getAttributeValue(null, "name");
                	String type = parser.getAttributeValue(null, "type");
                	String value = XmlUtils.readText(parser);
                	records.put(name, getValueWithType(value,type));
                }
                break;
            }
            eventType = parser.next();
            
        }
		return records;
	}

	private static Object getValueWithType(String value, String type) {
		if("uda:ui4".equals(type)){
			return Integer.parseInt(value);
		}else if("uda:boolean".equals(type)){
			value = value.equals("1") ? "true" : value; 
			return Boolean.parseBoolean(value);
		}
		return null;
	}
}
