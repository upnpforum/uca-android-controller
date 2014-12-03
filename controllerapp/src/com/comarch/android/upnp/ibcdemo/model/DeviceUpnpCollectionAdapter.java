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

import java.util.List;

import android.content.Context;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.comarch.android.upnp.ibcdemo.R;
import com.comarch.android.upnp.ibcdemo.persistence.AccompanyingDevicesDao;
import com.comarch.android.upnp.ibcdemo.ui.util.FontFactory;

public class DeviceUpnpCollectionAdapter extends BaseAdapter {

    private Context context;

    private DeviceUpnpCollection collection;
    
    private AccompanyingDevicesDao accompDao;

    public DeviceUpnpCollectionAdapter(Context context, DeviceUpnpCollection collection) {
        if(collection==null){
            throw new IllegalArgumentException();
        }
        this.context = context;
        this.collection = collection;
        this.accompDao = new AccompanyingDevicesDao(context);
    }

    @Override
    public int getCount() {
        
        return collection.getCount();
    }

    @Override
    public Object getItem(int position) {
        return collection.getDevice(position);
    }

    @Override
    public long getItemId(int position) {
        return collection.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        if (Debug.isDebuggerConnected()) {
            rowView = inflater.inflate(R.layout.row_layout, parent, false);
        } else {
            rowView = inflater.inflate(R.layout.table_cell_bulb, parent, false);
        }

        SourcedDeviceUpnp device = (SourcedDeviceUpnp)getItem(position);
        
        ImageView imageView = (ImageView) rowView.findViewById(R.id.bulbStatusImage);
        TextView nameView = (TextView) rowView.findViewById(R.id.bulbName);
        TextView accompTextView = (TextView) rowView.findViewById(R.id.accompanyingCount);
        nameView.setTypeface(FontFactory.getInstance().getOpensans(context));
        
        accompTextView.setVisibility(View.INVISIBLE);
        if (device instanceof MediaRenderer) {
        	List<String> accomps = null;
        	try
        	{
        		accompDao.open();
        		accomps = accompDao.getAccompanying(device.getKey());
        	}
        	finally
        	{
        		accompDao.close();
        	}
        	
        	if (accomps != null) {
        		Integer count = accomps.size();
        		if (count > 0) {
        			accompTextView.setText(count.toString() + "\naccomp.");
        			accompTextView.setTextColor(context.getResources().getColor(R.color.splashBackgroundEnd));
        			accompTextView.setVisibility(View.VISIBLE);
        		}
        	}
        }
        
        if (device.isAvailable()) {
        	nameView.setTextColor(context.getResources().getColor(R.color.bulbNameAvailable));
        	int resource = R.drawable.bulb_0;
        	if(device instanceof MediaServer){
        	    resource = R.drawable.server_connected;
        	}else if(device instanceof MediaRenderer){
        	    resource = R.drawable.renderer_connected;
        	}else if(device instanceof ControlPoint){
        		ControlPoint cp = (ControlPoint) device;
        		if(cp.hasUnreaded()){
        			resource = R.drawable.cp_connected_msg;
        		}else{
        			resource = R.drawable.cp_connected;
        		}
            }else if(device instanceof RGBDimmableLight){
            	RGBDimmableLight light = (RGBDimmableLight) device;
            	if(!light.isSwitched()){
            		resource = R.drawable.rgb_bulb_0;
            	}else{
            		if(light.getBrightness()<0.25){
            			resource = R.drawable.rgb_bulb_25;
            		}else if(light.getBrightness()<0.50){
            			resource = R.drawable.rgb_bulb_50;
            		}else if(light.getBrightness()<0.75){
            			resource = R.drawable.rgb_bulb_75;
            		}else{
            			resource = R.drawable.rgb_bulb_100;
            		}
            	}
            }else if(device instanceof SensorLight){
            	SensorLight light = (SensorLight) device;
            	if(!light.isSwitched()){
            		resource = R.drawable.sensor_light_0;
            	}else{
            		if(light.getBrightness()<0.25){
            			resource = R.drawable.sensor_light_25;
            		}else if(light.getBrightness()<0.50){
            			resource = R.drawable.sensor_light_50;
            		}else if(light.getBrightness()<0.75){
            			resource = R.drawable.sensor_light_75;
            		}else{
            			resource = R.drawable.sensor_light_100;
            		}
            	}
            }else if(device instanceof SensorTemperature){
            	resource = R.drawable.sensor_thermometer_on;
            }else if(device instanceof DimmableLight){
            	DimmableLight light = (DimmableLight) device;
            	if(!light.isSwitched()){
            		resource = R.drawable.bulb_0;
            	}else{
            		if(light.getBrightness()<0.25){
            			resource = R.drawable.bulb_25;
            		}else if(light.getBrightness()<0.50){
            			resource = R.drawable.bulb_50;
            		}else if(light.getBrightness()<0.75){
            			resource = R.drawable.bulb_75;
            		}else{
            			resource = R.drawable.bulb_100;
            		}
            	}
            }
        	imageView.setImageResource(resource);
        } else {
            nameView.setTextColor(context.getResources().getColor(R.color.bulbNameNotAvailable));
            int resource = R.drawable.bulb_off;
            if(device instanceof MediaServer){
                resource = R.drawable.server_not_connected;
            }else if(device instanceof MediaRenderer){
                resource = R.drawable.renderer_not_connected;
            }else if(device instanceof ControlPoint){
            	resource = R.drawable.cp_not_connected;
            }else if(device instanceof RGBDimmableLight){
            	resource = R.drawable.rgb_bulb_off;
            }else if(device instanceof SensorLight){
            	resource = R.drawable.sensor_light_off;
            }else if(device instanceof SensorTemperature){
            	resource = R.drawable.sensor_thermometer_off;
            }
            imageView.setImageResource(resource);
        }

        nameView.setText(device.getName());

        TextView textView2 = (TextView) rowView.findViewById(R.id.row_details_line);
        if (textView2!=null) {
            textView2.setText(device.getUuid());
        }
        
        return rowView;
    }
}
