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

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.comarch.android.upnp.ibcdemo.R;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.LocalConnectionStateChangedEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.XmppConnectionStateChangedEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.common.UpnpActionCallback;
import com.comarch.android.upnp.ibcdemo.model.Credentials;
import com.comarch.android.upnp.ibcdemo.model.DimmableLight;
import com.comarch.android.upnp.ibcdemo.model.RGBDimmableLight;
import com.comarch.android.upnp.ibcdemo.model.SensorLight;
import com.comarch.android.upnp.ibcdemo.model.Source;
import com.comarch.android.upnp.ibcdemo.model.interfaces.IDimmableLight;
import com.comarch.android.upnp.ibcdemo.persistence.CredentialsPersistence;
import com.comarch.android.upnp.ibcdemo.ui.newview.busevents.NotifyDeviceListChangedEvent;
import com.comarch.android.upnp.ibcdemo.ui.newview.widgets.BulbConnectionView;
import com.comarch.android.upnp.ibcdemo.ui.newview.widgets.ColorPickerView;
import com.comarch.android.upnp.ibcdemo.ui.newview.widgets.ColorPickerView.ColorPickerViewListener;
import com.comarch.android.upnp.ibcdemo.ui.newview.widgets.RotaryKnobView;
import com.comarch.android.upnp.ibcdemo.ui.util.FontFactory;
import com.comarch.android.upnp.ibcdemo.util.deliverer.FragmentWithBusDeliverer;

public class DimmableLightFragment extends FragmentWithBusDeliverer implements ColorPickerViewListener{

    private final String TAG = this.getClass().getSimpleName();
    private IDimmableLight currentDevice;

    private Callback mCallback;

    private RotaryKnobView jogView;

    private BulbConnectionView connectionStatusView;
    private ColorPickerView colorPickerView;
    
    private TextView notConnectedTextView;
    
    private TextView deviceTitle;

    private Integer mIgnoreUpdateSensor = 0;
    
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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detailview, container, false);

        notConnectedTextView = (TextView) rootView.findViewById(R.id.bulb_detail_not_connected_info);
        jogView = (RotaryKnobView) rootView.findViewById(R.id.knob);
        
        LinearLayout headerBar = (LinearLayout) rootView.findViewById(R.id.headerBarDetails);
        headerBar.setBackgroundColor(getResources().getColor(R.color.statusBarBackgroundDetail));
        headerBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.finishFragment();
            }
        });
        CredentialsPersistence credDao = new CredentialsPersistence(getActivity());
        Credentials data = credDao.load();
        

        
        TextView jidText = ((TextView)rootView.findViewById(R.id.jidTextDetail));
        jidText.setText(data.getJid());
        deviceTitle = (TextView) rootView.findViewById(R.id.titleTextDetail);
        
        FontFactory ff = FontFactory.getInstance();
        jidText.setTypeface(ff.getOpensans(getActivity()));
        deviceTitle.setTypeface(ff.getDefaultFont(getActivity()));
        notConnectedTextView.setTypeface(ff.getOpensans(getActivity()));

        rootView.setBackgroundColor(0xFF6DBF6A);
        
        jogView.setKnobListener(new RotaryKnobView.RotaryKnobListener() {
            @Override
            public void onKnobChanged(double newValue) {
                // arg = double in <0.0f, 1.0f>

                currentDevice.setBrightness(newValue);
	            if(currentDevice instanceof DimmableLight){
	            	DimmableLight light = (DimmableLight) currentDevice;
	                Map<String,String> args = new HashMap<String,String>();
	                args.put("newLoadlevelTarget", Long.valueOf(Math.round(100 * newValue)).toString());
	                mCallback.performUpnpDeviceAction(light, DimmableLight.DIMMING_SERVICE, DimmableLight.SET_LOAD_LEVEL_TARGET_ACTION, args, null);
                }else if(currentDevice instanceof SensorLight){
                	SensorLight sensor = (SensorLight) currentDevice;
                	String sensorURN = sensor.getSensorURNWhichBegin(SensorLight.BRIGHTNESS_SENSOR_URN);
                	if(sensorURN==null){
                		Log.e(TAG, "SENSOR URN "+SensorLight.BRIGHTNESS_SENSOR_URN+" not found");
                	}else{
                		Map<String,String> values = new HashMap<String,String>();
                		values.put(SensorLight.BRIGHTNES_SENSOR_ARG_NAME, Long.valueOf(Math.round(100 * newValue)).toString());
                		ignoreUpdateSensor(true);
                		mCallback.performWriteSensor(sensor, sensorURN, values, new UpnpActionCallback(true) {
							@Override
							public void run() {
								ignoreUpdateSensor(false);
							}
						});
                	}
                }
            }

            @Override
            public void onSwitchChanged(boolean state) {
                Log.v(TAG, "switchChanged:"+state);
                currentDevice.setSwitched(state);
	            if(currentDevice instanceof DimmableLight){
	            	DimmableLight light = (DimmableLight) currentDevice;
	                Map<String,String> args = new HashMap<String,String>();
	                args.put(DimmableLight.NEW_TARGET_VALUE_ARG, state ? "1" : "0");
	                mCallback.performUpnpDeviceAction(light, DimmableLight.SWITCH_SERVICE, DimmableLight.SET_TARGET_ACTION, args, null);
	            }else if(currentDevice instanceof SensorLight){
                	SensorLight sensor = (SensorLight) currentDevice;
                	String sensorURN = sensor.getSensorURNWhichBegin(SensorLight.SWITCH_SENSOR_URN);
                	if(sensorURN==null){
                		Log.e(TAG, "SENSOR URN "+SensorLight.SWITCH_SENSOR_URN+" not found");
                	}else{
                		Map<String,String> values = new HashMap<String,String>();
                		values.put(SensorLight.SWITCH_SENSOR_ARG_NAME, state ? "1" : "0");
                		ignoreUpdateSensor(true);
                		mCallback.performWriteSensor(sensor, sensorURN, values, new UpnpActionCallback(true) {
							@Override
							public void run() {
								ignoreUpdateSensor(false);
							}
						});
                	}
                }
            }
        });



        connectionStatusView = (BulbConnectionView) rootView.findViewById(R.id.bulbConnectionView);
        colorPickerView = (ColorPickerView) rootView.findViewById(R.id.colorPickerView);
        colorPickerView.setColorPickerListener(this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        currentDevice = (IDimmableLight) mCallback.getCurrentDevice();
        resetViewToDevice();
        getBus().registerSticky(this);
        getBus().registerSticky(connectionStatusView);
        resetUpdateSensor();

    }

    @Override
    public void onPause() {
        super.onPause();
        currentDevice = null;
        getBus().unregister(this);
        getBus().unregister(connectionStatusView);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    
    public void onEventMainThread(LocalConnectionStateChangedEvent ev) {
        updateCurrentDevice();
    }

    public void onEventMainThread(XmppConnectionStateChangedEvent ev) {
        updateCurrentDevice();
    }
    
    public void onEventMainThread(NotifyDeviceListChangedEvent event) {
    	if(mIgnoreUpdateSensor.equals(0)){
    		updateCurrentDevice();
    	}
    }

    private void updateCurrentDevice() {
        if (!onChange()) {
            currentDevice = (IDimmableLight) mCallback.getCurrentDevice();
            resetViewToDevice();
        }
    }
    
    private boolean onChange() {
        return false;
    }

    private void setSwitch(boolean switchState) {
        jogView.setSwitch(switchState);
    }

    private void resetViewToDevice() {
        
        boolean connected = true;
        if(currentDevice!=null){
            deviceTitle.setText(currentDevice.getName());
            if (currentDevice.getSources().size() > 1) {
                connectionStatusView.setDeviceLocalConnectionState(true);
                connectionStatusView.setDeviceCloudConnectionState(true);
            } else if (currentDevice.getSources().contains(Source.LOCAL)) {
                connectionStatusView.setDeviceLocalConnectionState(true);
                connectionStatusView.setDeviceCloudConnectionState(false);
            } else if (currentDevice.getSources().contains(Source.XMPP)){
                connectionStatusView.setDeviceLocalConnectionState(false);
                connectionStatusView.setDeviceCloudConnectionState(true);
            } else {
                connectionStatusView.setDeviceLocalConnectionState(false);
                connectionStatusView.setDeviceCloudConnectionState(false);
                connected = false;
            }
        }else{
            connected = false;
        }
        //connectionStatusView.invalidate();

        if(connected){
            notConnectedTextView.setVisibility(View.INVISIBLE);
            jogView.setVisibility(View.VISIBLE);
            jogView.setValue(currentDevice.getBrightness());
            boolean supportRGB = currentDevice instanceof RGBDimmableLight;
            colorPickerView.setVisibility(supportRGB?View.VISIBLE:View.INVISIBLE);
            if(supportRGB){
            	colorPickerView.setColor(((RGBDimmableLight)currentDevice).getColor());
            }
            setSwitch(currentDevice.isSwitched());
        }else{
            notConnectedTextView.setVisibility(View.VISIBLE);
            jogView.setVisibility(View.INVISIBLE);
            colorPickerView.setVisibility(View.INVISIBLE);
        }
    }

	@Override
	public void colorChanged(int color) {
		((RGBDimmableLight) currentDevice).setColor(color);
		int red = (color >> 16) & 0xFF;
		int green = (color >> 8) & 0xFF;
		int blue = color & 0xFF;
		
		Log.d(TAG,"0x"+Integer.toHexString(color&0xFFFFFF));
		
		StringBuilder sb = new StringBuilder();
		sb.append(red);
		sb.append(",");
		sb.append(green);
		sb.append(",");
		sb.append(blue);
		if(currentDevice instanceof DimmableLight){
        	DimmableLight light = (DimmableLight) currentDevice;
	        Map<String,String> args = new HashMap<String,String>();
	        args.put(RGBDimmableLight.RGBCOLOR_ARG, sb.toString());
	        mCallback.performUpnpDeviceAction(light, RGBDimmableLight.COLOR_LIGHT_SERVICE, RGBDimmableLight.SET_RGB_ACTION, args, null);
		}

	}
	synchronized private void resetUpdateSensor(){
		mIgnoreUpdateSensor = 0;
	}
	synchronized private void ignoreUpdateSensor(boolean state){
		if(state){
			++mIgnoreUpdateSensor;
		}else{
			--mIgnoreUpdateSensor;
			if(mIgnoreUpdateSensor<0) mIgnoreUpdateSensor = 0;
		}
	}

}
