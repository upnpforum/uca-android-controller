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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.comarch.android.upnp.ibcdemo.R;
import com.comarch.android.upnp.ibcdemo.ui.util.FontFactory;

public class FilePropertyAdapter extends BaseAdapter{

    class Property{
        private String key;
        private String value;
        public Property(String key, String value) {
            this.key = key;
            this.value = value;
        }
        public String getKey() {
            return key;
        }
        public void setKey(String key) {
            this.key = key;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
        
    }
    
    private Context context;
    private FontFactory fontFactory;
    private File file;
    private List<Property> properties;
    
    public FilePropertyAdapter(Context context){
        this.context = context;
        fontFactory = FontFactory.getInstance();
        properties = new ArrayList<Property>();
    }
    
    public void setFile(File file){
        this.file = file;
        prepareProperties();
        this.notifyDataSetChanged();
    }
    private void prepareProperties() {
        properties.clear();
        properties.add(new Property("File Name",file.getName()));
        properties.add(new Property("File Format", file.getProtocolInfo()));
        Map<String,String> props = file.getProperties();
        if(props.containsKey("size")){
            String size = parseSize(props.get("size"));
            if(size!=null){
                properties.add(new Property("Fize Size",size));
            }
        }
        if(file.getCreateDate()!=null){
            properties.add(new Property("Create date",file.getCreateDate()));
        }
        
    }

    public static double logOfBase(int base, Long value) {
        return Math.log(value) / Math.log(base);
    }
    
    public static void main(String[] args) {
        System.out.println(logOfBase(1024,1024L));
    }
    private String parseSize(String string) {
        String[] units = {"b","kB","MB","GB","TB"};
        try{
            Long value = Long.parseLong(string);
            int pow = (int) logOfBase(1024,value);
            pow = pow < units.length ? pow : units.length;
            Double retVal = value.doubleValue()/Math.pow(1024,pow);
            return String.format("%1$,.2f", retVal)+" "+units[pow];
        }catch(NumberFormatException e){
            return null;
        }
    }

    @Override
    public int getCount() {
        if(file==null) return 0;
        return properties.size();
    }

    @Override
    public Property getItem(int position) {
        return properties.get(position);
    }

    @Override
    public long getItemId(int position) {
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        if(position==0){
            rowView = inflater.inflate(R.layout.file_property_with_image_cell, parent, false);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.fileproperty_image);
            
            switch(file.getType()){
            case AUDIO:
                imageView.setImageResource(R.drawable.file_audio);
                break;
            case VIDEO:
                imageView.setImageResource(R.drawable.file_movie);
                break;
            case IMAGE:
                imageView.setBackgroundResource(R.drawable.file_image);
                break;
            default:
                rowView = inflater.inflate(R.layout.file_property_cell, parent, false);
            }
        }else{
            rowView = inflater.inflate(R.layout.file_property_cell, parent, false);
        }
        TextView line1 = (TextView) rowView.findViewById(R.id.fileproperty_line1);
        TextView line2 = (TextView) rowView.findViewById(R.id.fileproperty_line2);
        
        line1.setTypeface(fontFactory.getOpensans(context));
        line2.setTypeface(fontFactory.getDefaultFont(context));
        
        line1.setText(getItem(position).getKey());
        line2.setText(getItem(position).getValue());
        
        return rowView;
    }

}
