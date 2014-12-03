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

package com.comarch.android.upnp.ibcdemo.ui.newview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.comarch.android.upnp.ibcdemo.R;
import com.comarch.android.upnp.ibcdemo.busevent.ConnectionStateChangedEvent.ConnectionState;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.LocalConnectionStateChangedEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.XmppConnectionStateChangedEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.local.busevent.ClingRequestRefreshEvent;
import com.comarch.android.upnp.ibcdemo.model.Credentials;
import com.comarch.android.upnp.ibcdemo.model.DeviceUpnpCollectionAdapter;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;
import com.comarch.android.upnp.ibcdemo.persistence.CredentialsPersistence;
import com.comarch.android.upnp.ibcdemo.ui.AboutActivity;
import com.comarch.android.upnp.ibcdemo.ui.SettingsActivity;
import com.comarch.android.upnp.ibcdemo.ui.newview.busevents.NotifyDeviceListChangedEvent;
import com.comarch.android.upnp.ibcdemo.ui.util.FontFactory;
import com.comarch.android.upnp.ibcdemo.util.deliverer.FragmentWithBusDeliverer;

public class MainViewFragment extends FragmentWithBusDeliverer implements OnItemClickListener {

    enum STATE{
        SERVERS,
        RENDERES
    }
    
    private Callback mCallback;
   
    private DeviceUpnpCollectionAdapter mUpnpDevices;
    
    private ListView mMainList;
    
    private TextView jidText;

    public MainViewFragment(){
        super();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Callback");
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_mainview, container, false);
        mainView.setBackgroundColor(0xFFFFFFFF);

        mainView.findViewById(R.id.xmppConnectionToggleButton).setEnabled(false);
        mainView.findViewById(R.id.localConnectionToggleButton).setEnabled(false);

        jidText = (TextView)mainView.findViewById(R.id.jidTextMain);
        FontFactory ff = FontFactory.getInstance();
        jidText.setTypeface(ff.getOpensans(getActivity()));
        ((TextView)mainView.findViewById(R.id.titleTextMain)).setTypeface(ff.getDefaultFont(getActivity()));
        

        
        mMainList = (ListView) mainView.findViewById(R.id.mainview_list);
        
        ((ImageView)mainView.findViewById(R.id.infoImage)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                      Intent aboutIntent = new Intent(getActivity(), AboutActivity.class);
                      startActivity(aboutIntent);
            }
        });
        ((ImageView)mainView.findViewById(R.id.settingImage)).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                      Intent aboutIntent = new Intent(getActivity(), SettingsActivity.class);
                      startActivity(aboutIntent);
            }
        });
        ((View)mainView.findViewById(R.id.placeholder)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getBus().post(new ClingRequestRefreshEvent());		
			}
		});
        return mainView;
    }

    @Override
    public void onResume() {
        super.onResume();
        
        CredentialsPersistence credDao = new CredentialsPersistence(getActivity());
        Credentials data = credDao.load();
        jidText.setText(data.getJid());
        
        getBus().registerSticky(this);
        mUpnpDevices = new DeviceUpnpCollectionAdapter(getActivity(), mCallback.getUpnpDevices());
        mMainList.setOnItemClickListener(this);
        mMainList.setAdapter(mUpnpDevices);
    }

    @Override
    public void onPause() {
        super.onPause();
        getBus().unregister(this);
        mMainList.setOnItemClickListener(null);
        mMainList.setAdapter(null);

        mUpnpDevices.notifyDataSetInvalidated();
        mUpnpDevices = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SourcedDeviceUpnp device = (SourcedDeviceUpnp) mMainList.getAdapter().getItem(position);
        mCallback.onDeviceSelected(device);
    }

    public void onEventMainThread(LocalConnectionStateChangedEvent ev) {
        ToggleButton tb = (ToggleButton) getView().findViewById(R.id.localConnectionToggleButton);
        updateConnectionButton(tb, ev.getState());
    }

    public void onEventMainThread(XmppConnectionStateChangedEvent ev) {
        ToggleButton tb = (ToggleButton) getView().findViewById(R.id.xmppConnectionToggleButton);
        updateConnectionButton(tb, ev.getState());
    }

    public void onEventMainThread(NotifyDeviceListChangedEvent event) {
    	mUpnpDevices.notifyDataSetChanged();
    }

    private void updateConnectionButton(ToggleButton tb, ConnectionState state) {
        tb.setEnabled(true);
        switch (state) {
        case CONNECTED:
            tb.setTag(state.toString());
        	if(tb.getId() == R.id.localConnectionToggleButton) tb.setBackgroundResource(R.drawable.local_conn_state);
        	else tb.setBackgroundResource(R.drawable.cloud_conn_state);
            tb.setChecked(true);
            break;
        case CONNECTING:
            AnimationDrawable drawable;
        	if(tb.getId() == R.id.localConnectionToggleButton){
        	    drawable = (AnimationDrawable) this.getResources().getDrawable(R.drawable.local_conn_animation);
        	}else{ 
        	    drawable = (AnimationDrawable) this.getResources().getDrawable(R.drawable.cloud_conn_animation);
        	}
            if(!state.toString().equals(tb.getTag())){
                tb.setBackground(drawable);
                drawable.start();
                tb.setTag(state.toString());
            }
            tb.setChecked(true);
            break;
        case DISCONNECTED:
            tb.setTag(state.toString());
        	if(tb.getId() == R.id.localConnectionToggleButton) tb.setBackgroundResource(R.drawable.local_conn_state);
        	else tb.setBackgroundResource(R.drawable.cloud_conn_state);
            tb.setChecked(false);
            break;
        }
    }
}
