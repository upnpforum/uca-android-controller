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

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.comarch.android.upnp.ibcdemo.R;

public class MediaRendererCollectionAdapter implements SpinnerAdapter{

    private DeviceUpnpCollection mCollection;
    private Context context;
        
    public MediaRendererCollectionAdapter(Context context,DeviceUpnpCollection collection){
        this.context = context;
        mCollection = collection;
    }
    @Override
    public int getCount() {
        return mCollection.getCount();
    }

    @Override
    public MediaRenderer getItem(int position) {
        return (MediaRenderer) mCollection.getDevice(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView view = (TextView) inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        MediaRenderer device = getItem(position);
        
        view.setTextColor(context.getResources().getColor(R.color.bulbNameAvailable));
        if(device.isAvailable()){
            view.setText(device.getName());
        }else{
            view.setText("(off) "+device.getName());
        }
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return mCollection.getCount()==0;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver arg0) {
        
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver arg0) {
        
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        CheckedTextView view = (CheckedTextView) inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        MediaRenderer device = getItem(position);

        view.setTextColor(context.getResources().getColor(R.color.bulbNameAvailable));
        if(device.isAvailable()){
            view.setText(device.getName());
        }else{
            view.setText("(off) "+device.getName());
        }
        return view;
    }

}
