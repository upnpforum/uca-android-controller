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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.comarch.android.upnp.ibcdemo.R;
import com.comarch.android.upnp.ibcdemo.ui.util.FontFactory;

public class ResourceCollectionAdapter extends BaseAdapter {

    private Context context;
    private Directory mDirectory;
    
    public ResourceCollectionAdapter(Context context,Directory dir) {
        this.context = context;
        mDirectory = dir;
    }
    public void setDirectory(Directory dir){
        mDirectory = dir;
        this.notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        if(mDirectory.getChildren()==null) return 0;
        return mDirectory.getChildren().size();
    }

    @Override
    public Resource getItem(int position) {
        return mDirectory.getChildren().get(position);
    }

    @Override
    public long getItemId(int position) {
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.table_cell_bulb, parent, false);

        
        Resource resource = (Resource) getItem(position);
       
        
        ImageView imageView = (ImageView) rowView.findViewById(R.id.bulbStatusImage);
        if(resource instanceof File){
            File file = (File) resource;
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
                    imageView.setImageResource(0);
            }
        } else{
            imageView.setImageResource(0);
        }
        TextView nameView = (TextView) rowView.findViewById(R.id.bulbName);
        nameView.setTextColor(context.getResources().getColor(R.color.bulbNameAvailable));
        nameView.setTypeface(FontFactory.getInstance().getOpensans(context));
       

        nameView.setText(resource.getName());

        TextView textView2 = (TextView) rowView.findViewById(R.id.row_details_line);
        if (textView2!=null) {
            textView2.setText(resource.getId());
        }
        
        return rowView;
    }

}
