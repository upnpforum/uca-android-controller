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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.comarch.android.upnp.ibcdemo.R;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data.DeviceDescription;
import com.comarch.android.upnp.ibcdemo.persistence.AccompanyingDevicesDao;
import com.comarch.android.upnp.ibcdemo.persistence.AccompanyingFilePersistence;
import com.comarch.android.upnp.ibcdemo.ui.util.FontFactory;

public class AccompanyingDevicesAdapter extends BaseAdapter {
	
    private Context context;
    private DeviceUpnpCollection collection;
    private SourcedDeviceUpnp currentDevice;
    private AccompanyingDevicesDao dao;
    private AccompanyingFilePersistence accompanyingFile;
    private List<String> accompanyed = new ArrayList<String>();
    

	public AccompanyingDevicesAdapter(Context context, DeviceUpnpCollection collection) {
        if(collection==null){
            throw new IllegalArgumentException();
        }
        this.context = context;
        this.collection = collection;
        this.dao = new AccompanyingDevicesDao(context);
        this.accompanyingFile = new AccompanyingFilePersistence(context);
    }
	
	private void getAccompanyed(){
		try{
			dao.open();
			accompanyed = dao.getAccompanying(currentDevice.getUuid());
		}finally{
			dao.close();
		}
	}
    public void setCurrentDevice(SourcedDeviceUpnp currentDevice) {
    	if(this.currentDevice!=currentDevice){
    		this.currentDevice = currentDevice;
    		this.notifyDataSetChanged();
    	}
	}
    
    @Override 
    public void notifyDataSetChanged(){
    	getAccompanyed();
    	super.notifyDataSetChanged();
    }
	@Override
	public int getCount() {
		int c = collection.getCount();
		return c == 0 ? 0 : c-1;
	}

	@Override
	public Object getItem(int position) {
		List<SourcedDeviceUpnp> col = new ArrayList<SourcedDeviceUpnp>(collection.allDevices());
		
		int shift = 0;
		int index = col.indexOf(currentDevice);
		if(index<=position){
			shift = 1;
		}
		return collection.getDevice(position+shift);
	}

	@Override
	public long getItemId(int position) {
		 return -1; //Not ussed
	}

	private static String getDeviceDisplayName(SourcedDeviceUpnp device) {
		StringBuilder builder = new StringBuilder();
		
		String name = device.getName();
		if (name == null) {
			name = "Missing Name";
		}
		builder.append(name);
		
		DeviceDescription description = device.getDescription();
		if (description != null) {
			String type = description.getDeviceType();
			if (type != null) {
				builder.append(" (");
				builder.append(type);
				builder.append(")");
			}
		}
		
		return builder.toString();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.accompanying_device_table_cell, parent, false);

        final SourcedDeviceUpnp device = (SourcedDeviceUpnp)getItem(position);
        
        final CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.accompanying_device_checked);
        checkBox.setClickable(false);
        TextView deviceName = (TextView) rowView.findViewById(R.id.deviceName);
        deviceName.setTypeface(FontFactory.getInstance().getOpensans(context));
        
        if (device.isAvailable()) {
        	deviceName.setTextColor(context.getResources().getColor(R.color.bulbNameAvailable));

        } else {
            deviceName.setTextColor(context.getResources().getColor(R.color.bulbNameNotAvailable));
        }

        checkBox.setChecked(accompanyed.contains(device.getKey()));
        //deviceName.setText(device.getName());
        deviceName.setText(getDeviceDisplayName(device));
        
        rowView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (checkBox.isChecked()) {
					remove(currentDevice.getKey(),device.getKey());
					checkBox.setChecked(false);
				} else {
					String uri = accompanyingFile.getAccompanyingContentUri();
					if (uri != null) {
						add(currentDevice.getKey(),device.getKey());
						checkBox.setChecked(true);
					} else {
						Toast toast = Toast.makeText(context, R.string.accompanying_content_missing, Toast.LENGTH_SHORT);
						toast.show();
					}
				}
			}
		});
        return rowView;
	}
	
	private void remove(String from,String to){
		try{
			dao.open();
			dao.delete(from, to);
			accompanyed.remove(to);
		}finally{
			dao.close();
		}
		
	}
	
	private void add(String from,String to){
		try{
			dao.open();
			dao.create(from, to);
			accompanyed.add(to);
		}finally{
			dao.close();
		}
		
	}


}
