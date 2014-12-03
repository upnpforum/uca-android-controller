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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.comarch.android.upnp.ibcdemo.R;
import com.comarch.android.upnp.ibcdemo.model.SensorTemperature;
import com.comarch.android.upnp.ibcdemo.ui.newview.busevents.NotifyDeviceListChangedEvent;
import com.comarch.android.upnp.ibcdemo.util.deliverer.FragmentWithBusDeliverer;

public class TemperatureSensorFragment extends FragmentWithBusDeliverer{

	private Callback mCallback;
	
	private TextView mNotConnected;
	private View mConnectedView;
	private TextView mTitleText;
	private TextView mSubtitleText;
	
	private TextView mLine1;
	private TextView mLine2;
	private TextView mLine3;
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_thermometer_sensor, container, false);
        mainView.setBackgroundColor(0xFFFFFFFF);
        
        mTitleText = (TextView) mainView.findViewById(R.id.title);
        mTitleText.setText("Temperature sensor");
        mSubtitleText = (TextView) mainView.findViewById(R.id.subtitle);
        mNotConnected = (TextView) mainView.findViewById(R.id.not_connected_info);
        mConnectedView = mainView.findViewById(R.id.connected_view);
        
        mLine1 = (TextView) mainView.findViewById(R.id.temperature_line_1);
        mLine2 = (TextView) mainView.findViewById(R.id.temperature_line_2);
        mLine3 = (TextView) mainView.findViewById(R.id.temperature_line_3);   

        mainView.findViewById(R.id.headerBar).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               mCallback.finishFragment();
            }
        });
                
        return mainView;
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
    public void onResume() {
        super.onResume();
        mSubtitleText.setText(mCallback.getCurrentDevice().getName());
        updateConnectionState();
        getBus().register(this);
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	getBus().unregister(this);
    }
    
    public void onEventMainThread(NotifyDeviceListChangedEvent event) {
    	updateConnectionState();
    }

    private void updateConnectionState(){
    	SensorTemperature sensor = (SensorTemperature) mCallback.getCurrentDevice();
    	boolean available = sensor.isAvailable();
    	mNotConnected.setVisibility(!available?View.VISIBLE:View.INVISIBLE);
    	mConnectedView.setVisibility(available?View.VISIBLE:View.INVISIBLE);
    	
    	updateTemperature(sensor.getThemperature());
    }

    private void updateTemperature(int kelvinDeg){
    	mLine1.setText(kelvinToCelcius(kelvinDeg)+" \u00B0C");
    	mLine2.setText(kelvinToFarenheit(kelvinDeg)+" \u00B0F");
    	mLine3.setText(kelvinDeg+" K");
    }
    
    private int kelvinToCelcius(int k){
    	return (int)(k-273.15);
    }
    
    private int kelvinToFarenheit(int k){
    	return (int)((9.f/5.f)*(k-273.15f)+32);
    }
}
