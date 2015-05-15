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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.comarch.android.upnp.ibcdemo.R;
import com.comarch.android.upnp.ibcdemo.connectivity.busevent.AccompanyingURIChanedEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.common.UpnpActionCallback;
import com.comarch.android.upnp.ibcdemo.model.AccompanyingDevicesAdapter;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer.TransportState;
import com.comarch.android.upnp.ibcdemo.model.mediaserver.File;
import com.comarch.android.upnp.ibcdemo.ui.newview.busevents.NotifyDeviceListChangedEvent;
import com.comarch.android.upnp.ibcdemo.util.deliverer.FragmentWithBusDeliverer;

public class MediaRendererFragment extends FragmentWithBusDeliverer{

	enum UI_STATE{
		DISPLAY_MR,
		ACCOMPANYING
	}
    private final String TAG = getClass().getSimpleName();
    
    private final static int VOLUME_STEP = 10;
    private Callback mCallback;
    private TextView title;
    private TextView subtitle;
    private TextView notConnectedInfo;
    private View rendererConnectedView;
    
    private ImageButton mPlayButton;
    private ImageButton mPauseButton;
    private ImageButton mStopButton;
    
    private ImageView mFileInfoIcon;
    private TextView mFileInfoLine1;
    private TextView mFileInfoLine2;
    
    private TextView mCurrentTimeView;
    private ListView mAccompanyingListView;
    
    private Long lastTimeUpdate = 0L;
    private Handler mHandler = new Handler();
    private static final int UPDATE_TIME = 300;
    private UI_STATE uiState = UI_STATE.DISPLAY_MR;
    private AccompanyingDevicesAdapter mAccompanyingAdapter;
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_media_renderer, container, false);
        title = (TextView) mainView.findViewById(R.id.mediarenderer_title);
        subtitle = (TextView) mainView.findViewById(R.id.mediarenderer_subtitle);
        notConnectedInfo = (TextView) mainView.findViewById(R.id.mediarenderer_not_connected_info);
        mPlayButton = (ImageButton) mainView.findViewById(R.id.mediarenderer_button_start);
        mPauseButton = (ImageButton) mainView.findViewById(R.id.mediarenderer_button_pause);
        mStopButton = (ImageButton) mainView.findViewById(R.id.mediarenderer_button_stop);
        rendererConnectedView = mainView.findViewById(R.id.mediarenderer_connected_view);
        mCurrentTimeView = (TextView) mainView.findViewById(R.id.mediarenderer_current_time);
        
        mFileInfoIcon = (ImageView) mainView.findViewById(R.id.fileproperty_image);
        mFileInfoLine1 = (TextView) mainView.findViewById(R.id.fileproperty_line1);
        mFileInfoLine2 = (TextView) mainView.findViewById(R.id.fileproperty_line2);
        mAccompanyingListView = (ListView) mainView.findViewById(R.id.accompanying_list_view);
        
        mStopButton.setOnClickListener(stopListener);
        mPlayButton.setOnClickListener(playListener);
        mPauseButton.setOnClickListener(pauseListener);
        
        mainView.findViewById(R.id.optionsMenuImage).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getActivity().openOptionsMenu();
				
			}
		});
        
        ((ImageButton)mainView.findViewById(R.id.mediarenderer_button_volumeup)).setOnClickListener(new SetVolumeListener(VOLUME_STEP));
        ((ImageButton)mainView.findViewById(R.id.mediarenderer_button_volumedown)).setOnClickListener(new SetVolumeListener(-VOLUME_STEP));
        mainView.findViewById(R.id.mediarenderer_headerBar).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        setHasOptionsMenu(true);
        
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
    public void onDetach() {
        super.onDetach();
    }
    
    @Override
    public void onResume(){
        super.onResume();
        getBus().register(this);
        mAccompanyingAdapter = new AccompanyingDevicesAdapter(getActivity(), mCallback.getUpnpDevices());
        mAccompanyingAdapter.setCurrentDevice(mCallback.getCurrentDevice());
        mAccompanyingListView.setAdapter(mAccompanyingAdapter);
        mCallback.getProtocolInfo(mCallback.getCurrentDevice());
        redrawDeviceState();
    }
    
    @Override
    public void onPause() {
        mHandler.removeCallbacks(updateTimeRunnable);
        getBus().unregister(this);
        
        mAccompanyingListView.setAdapter(null);
        mAccompanyingAdapter.notifyDataSetInvalidated();
        mAccompanyingAdapter = null;
        
        super.onPause();
    }
    //----------------------------
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.media_renderer_fragment_menu, menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
           case R.id.accompanying_devices_option:
        	   uiState = UI_STATE.ACCOMPANYING;
        	   redrawDeviceState();
              return true;
           default:
              return super.onOptionsItemSelected(item);
        }
     }
    public void onBackPressed(){
    	if(uiState==UI_STATE.ACCOMPANYING){
     	   uiState = UI_STATE.DISPLAY_MR;
     	   redrawDeviceState();
     	   getBus().post(new AccompanyingURIChanedEvent(mCallback.getCurrentDevice()));
    	}else{
    		mCallback.finishFragment();
    	}
    }
    //----------------------------
    public void onEventMainThread(NotifyDeviceListChangedEvent event) {
    	mAccompanyingAdapter.setCurrentDevice(mCallback.getCurrentDevice());
        redrawDeviceState();
    }
        
    private Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            MediaRenderer mr = (MediaRenderer) mCallback.getCurrentDevice();
            if(mr==null || mr.getTransportState()!=TransportState.PLAYING || mr.getCurrentTime()==null){
               return;
            }
            long diff = new Date().getTime() - lastTimeUpdate;
            if(diff>1000){
                mr.setCurrentTime(mr.getCurrentTime()+diff);
                mCurrentTimeView.setText(currentTimeToString(mr.getCurrentTime()));
            }
            mHandler.postDelayed(updateTimeRunnable, UPDATE_TIME);
        }
    };

    private void updateMRUIState(MediaRenderer mr){
        if(mr.getTransportState()==TransportState.PLAYING){
            mCurrentTimeView.setText(currentTimeToString(mr.getCurrentTime()));
            mHandler.removeCallbacks(updateTimeRunnable);
            mHandler.postDelayed(updateTimeRunnable, UPDATE_TIME);
            //mPlayButton.setImageResource(R.drawable.button_pause_states);
            //mPlayButton.setOnClickListener(pauseListener);
        }else{
            mCurrentTimeView.setText(currentTimeToString(null));
            //mPlayButton.setImageResource(R.drawable.button_play_states);
            //mPlayButton.setOnClickListener(playListener);
        }
        
        if(mr.getCurrentFile()!=null){
            File file = mr.getCurrentFile();
            switch (file.getType()) {
            case AUDIO:
                mFileInfoIcon.setImageResource(R.drawable.file_audio);
                break;
            case IMAGE:
                mFileInfoIcon.setImageResource(R.drawable.file_image);
                break;
            default:
                mFileInfoIcon.setImageResource(R.drawable.file_movie);
                break;
            }
            mFileInfoLine1.setText("File Name");
            mFileInfoLine2.setText(file.getName());
            
        }else{
            mFileInfoIcon.setImageResource(R.drawable.file_movie);
            mFileInfoLine1.setText("");
            if(mr.getTransportState()==TransportState.NO_MEDIA_PRESENT){
                mFileInfoLine2.setText("No media present");
            }else{
                mFileInfoLine2.setText("No media file");
            }
        }
        title.setText(mr.getName());
    }
    private void redrawDeviceState(){
    	Log.d(TAG,"redrawDeviceState");
        MediaRenderer mr = (MediaRenderer) mCallback.getCurrentDevice();
        if(mr==null){
            mCallback.finishFragment();
        }       
        
        if(uiState==UI_STATE.ACCOMPANYING){
            rendererConnectedView.setVisibility(View.INVISIBLE);
            notConnectedInfo.setVisibility(View.INVISIBLE);
            mAccompanyingListView.setVisibility(View.VISIBLE);
            subtitle.setText(getResources().getString(R.string.accompanying_devices));
        }else{
        	mAccompanyingListView.setVisibility(View.INVISIBLE);
	        if(mr.isAvailable()){
	            rendererConnectedView.setVisibility(View.VISIBLE);
	            notConnectedInfo.setVisibility(View.INVISIBLE);
	            subtitle.setText(mr.getTransportState().toString());
	            updateMRUIState(mr);
	        }else{
	            rendererConnectedView.setVisibility(View.INVISIBLE);
	            notConnectedInfo.setVisibility(View.VISIBLE);
	            subtitle.setText(getResources().getString(R.string.not_connected));
	        }
        }
        

    }
    
    private CharSequence currentTimeToString(Long currentTime) {
        lastTimeUpdate = new Date().getTime();
        String str = "00:00:00";
        if(currentTime!=null){
            long s = currentTime/1000;
            long m = s/60;
            long h = m/60;
            
            m -= h*60;
            s -= m*60;
            
            
            StringBuilder sb = new StringBuilder();
            if(h<10) sb.append("0");
            sb.append(h);
            sb.append(":");
            if(m<10) sb.append("0");
            sb.append(m);
            sb.append(":");
            if(s<10) sb.append("0");
            sb.append(s);
            str = sb.toString();
        }
        return str;
    }

    private OnClickListener playListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Map<String,String> args = new HashMap<String,String>();
            args.put("InstanceID", "0");
            args.put("Speed", "1");
            mCallback.performMediaRendererAction((MediaRenderer)mCallback.getCurrentDevice(), "Play", args);
        }
    };
    
    private OnClickListener pauseListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Map<String,String> args = new HashMap<String,String>();
            args.put("InstanceID", "0");
            mCallback.performMediaRendererAction((MediaRenderer)mCallback.getCurrentDevice(), "Pause", args);
        }
    };
    
    private OnClickListener stopListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Map<String,String> args = new HashMap<String,String>();
            args.put("InstanceID", "0");
            mCallback.performMediaRendererAction((MediaRenderer)mCallback.getCurrentDevice(), "Stop", args);
        }
    };
    
    

    class SetVolumeListener implements OnClickListener{
        
        private int step;
        private int clicks=0;
        
        public SetVolumeListener(int step){
            this.step = step;
        }
        @Override
        public void onClick(View v) {
            final MediaRenderer mr = (MediaRenderer)mCallback.getCurrentDevice();
            if(clicks++ == 0){
            	Map<String,String> args = new HashMap<String,String>();
            	args.put("InstanceID", "0");
            	args.put("Channel","Master");
                mCallback.perfomRenderingControlAction(mr, "GetVolume", args,new UpnpActionCallback(true){
                    @Override
                    public void run() {
                    	if(!didActionFailed()){
                    		Object rawCurrentVolume = getResponse().get("CurrentVolume");
                    		Long currentVolumeInt = (long) 0;
                    		
                    		if (rawCurrentVolume instanceof String) {
                    			String currentVolume = (String) rawCurrentVolume;
                    			currentVolumeInt = Long.parseLong(currentVolume);
                    		} else if (rawCurrentVolume instanceof UnsignedIntegerTwoBytes) {
                    			UnsignedIntegerTwoBytes bytes = (UnsignedIntegerTwoBytes) rawCurrentVolume;
                    			currentVolumeInt = bytes.getValue();
                    		}
                    		
                    		long desiredVolume = currentVolumeInt + clicks * step;
                    		desiredVolume = Math.min(100, Math.max(0, desiredVolume));
                    		
	                        HashMap<String,String> args = new HashMap<String,String>();
	                        args.put("InstanceID", "0");
	                        args.put("Channel", "Master");
	                        args.put("DesiredVolume", Long.toString(desiredVolume));
	                        
	                        mCallback.perfomRenderingControlAction(mr, "SetVolume", args,null);
                    	}
                        clicks = 0;
                       
                    }
                    
                });
            }
        }
    }
}
