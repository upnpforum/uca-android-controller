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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.comarch.android.upnp.ibcdemo.R;
import com.comarch.android.upnp.ibcdemo.busevent.connector.data.MediaServerBrowseResponse;
import com.comarch.android.upnp.ibcdemo.connectivity.common.UpnpActionCallback;
import com.comarch.android.upnp.ibcdemo.model.DeviceUpnpCollection;
import com.comarch.android.upnp.ibcdemo.model.MediaRenderer;
import com.comarch.android.upnp.ibcdemo.model.MediaRendererCollectionAdapter;
import com.comarch.android.upnp.ibcdemo.model.MediaServer;
import com.comarch.android.upnp.ibcdemo.model.mediaserver.Directory;
import com.comarch.android.upnp.ibcdemo.model.mediaserver.File;
import com.comarch.android.upnp.ibcdemo.model.mediaserver.FilePropertyAdapter;
import com.comarch.android.upnp.ibcdemo.model.mediaserver.Resource;
import com.comarch.android.upnp.ibcdemo.model.mediaserver.ResourceCollectionAdapter;
import com.comarch.android.upnp.ibcdemo.persistence.AccompanyingFilePersistence;
import com.comarch.android.upnp.ibcdemo.ui.newview.busevents.NotifyDeviceListChangedEvent;
import com.comarch.android.upnp.ibcdemo.util.deliverer.FragmentWithBusDeliverer;

public class MediaServerFragment extends FragmentWithBusDeliverer implements OnItemClickListener {
    public static final String EXTRAS_UUID = "uuid";
    
    private final String TAG = getClass().getName();
    private MediaServer mediaServer;

    private Callback mCallback;
    private ResourceCollectionAdapter mResourcesAdapter;
    private MediaRendererCollectionAdapter mRenderersAdapter;
    private FilePropertyAdapter mFilePropertyAdapter;
    
    private Directory root;
    private Directory parentDirectory;
    private File selectedFile;
    
    private ListView mMainList;
    private TextView mTitle;
    private TextView mSubtitle;
    private TextView mNotConnectedInfoView;
    private View mFileView;
    private Spinner mMediaRenderersSpinner;
    
    private ImageButton mPlayButton;
    private ImageButton mScheduleButton;
    
    private ListView mFilePropertyList;
    private View mOptionsMenuIcon;
    
    private AccompanyingFilePersistence mAccompanyingFile;

    private SpinnerOnItemSelectedListener spinnerOnItemSelectedListener = new SpinnerOnItemSelectedListener();

        
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	mAccompanyingFile = new AccompanyingFilePersistence(getActivity());
    	
        View mainView = inflater.inflate(R.layout.fragment_media_server, container, false);
        mTitle = (TextView) mainView.findViewById(R.id.mediaserver_title);
        mSubtitle = (TextView) mainView.findViewById(R.id.mediaserver_subtitle);
        mMainList = (ListView) mainView.findViewById(R.id.mediaserver_list);
        mNotConnectedInfoView = (TextView) mainView.findViewById(R.id.mediaserver_not_connected_info);
        mFileView = mainView.findViewById(R.id.mediaserver_file_view);
        mMediaRenderersSpinner = (Spinner) mainView.findViewById(R.id.mediaserver_mediarenderers_spiner);
        mFilePropertyList = (ListView) mainView.findViewById(R.id.mediaserver_file_properties_list);
        
        mOptionsMenuIcon = mainView.findViewById(R.id.optionsMenuImage);
        mOptionsMenuIcon.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getActivity().openOptionsMenu();
				
			}
		});
        
        mPlayButton = (ImageButton) mainView.findViewById(R.id.button_play);
        mPlayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,String> args = new HashMap<String,String>();
                args.put("CurrentURI", selectedFile.getUri());
                args.put("InstanceID", "0");
                args.put("CurrentURIMetaData",selectedFile.getMetaData());
                mCallback.performMediaRendererAction(mCallback.getDefaultMediaRenderer(), "SetAVTransportURI", args,
                        new UpnpActionCallback(){
                    @Override
                    public void run() {
                        Map<String,String> args = new HashMap<String,String>();
                        args.put("InstanceID", "0");
                        args.put("Speed", "1");
                        mCallback.performMediaRendererAction(mCallback.getDefaultMediaRenderer(), "Play", args);
                        
                    }
                });
            }
        });
        mScheduleButton = (ImageButton) mainView.findViewById(R.id.button_schedule);
        mScheduleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	SetAccompanyingContent();
                //Map<String,String> args = new HashMap<String,String>();
                //args.put("NextURI", selectedFile.getUri());
                //args.put("InstanceID", "0");
                //args.put("NextURIMetaData", selectedFile.getMetaData());
                //mCallback.performMediaRendererAction(mCallback.getDefaultMediaRenderer(), "SetNextAVTransportURI", args);
            }
        });
        mainView.findViewById(R.id.mediaserver_headerBar).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               onBackPressed();
            }
        });

        return mainView;
    }
    
    @Override
    public void onResume() {
        Log.i(TAG,"onResume");
        super.onResume();
        getBus().register(this);
        mediaServer = (MediaServer) mCallback.getCurrentDevice();
        setDeviceState();
        mTitle.setText(mediaServer.getName());
        mMainList.setAdapter(mResourcesAdapter);
        mMainList.setOnItemClickListener(this);
        
        mRenderersAdapter = new MediaRendererCollectionAdapter(getActivity(), new DeviceUpnpCollection(mCallback.getUpnpDevices().allDevices(),MediaRenderer.class));
        mMediaRenderersSpinner.setAdapter(mRenderersAdapter);
        updateDefaultRenderer();
        mMediaRenderersSpinner.setOnItemSelectedListener(spinnerOnItemSelectedListener);

        mFilePropertyList.setAdapter(mFilePropertyAdapter);
        
        if(parentDirectory == null){
            mCallback.browseMediaServerDirectory(mediaServer,root.getId(),root.getParentId());
            mCallback.getProtocolInfo(mediaServer);
        }
    }
    
    @Override
    public void onPause() {
        Log.i(TAG,"onPause");
        super.onPause();
        getBus().unregister(this);
        mediaServer = null;
        mMainList.setAdapter(null);
        mMainList.setOnItemClickListener(null);
        
        setAdapterAndlistenerToSpinner(null,null);
        
        mFilePropertyList.setAdapter(null);
        
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callback) activity;
            root = new Directory("0");
            parentDirectory = null;
            selectedFile = null;
            mResourcesAdapter = new ResourceCollectionAdapter(getActivity(),root);
            
            mFilePropertyAdapter = new FilePropertyAdapter(getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Callback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mResourcesAdapter.notifyDataSetInvalidated();
        mFilePropertyAdapter.notifyDataSetInvalidated();
        mResourcesAdapter = null;
        mRenderersAdapter = null;
        mFilePropertyAdapter = null;
    }
    //--
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.media_server_fragment_menu, menu);
    }

    private void SetAccompanyingContent() {
		mAccompanyingFile.setAccompanyingContent(selectedFile.getUri(), selectedFile.getMetaData());
		Toast.makeText(getActivity(), R.string.accompanying_content_set_successfuly, Toast.LENGTH_SHORT).show();
    }
    
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.accompanying_content_option:
			SetAccompanyingContent();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
    //==
    public void onBackPressed(){
        if(!mediaServer.isAvailable()){
            mCallback.finishFragment();
        }
        else if(selectedFile!=null){
            selectedFile=null;
            setDeviceState();
            mSubtitle.setText(parentDirectory.getName());
        }
        else if(parentDirectory == null || parentDirectory==root){
            mCallback.finishFragment();
        } else {
            parentDirectory = parentDirectory.getParent();
            mResourcesAdapter.setDirectory(parentDirectory);
            mSubtitle.setText(parentDirectory.getName());
        }
    }
    
    public void onEventMainThread(MediaServerBrowseResponse event){
        Log.i(TAG,"MediaServerBrowseResponse");
        if(event.getFromId().endsWith(mediaServer.getUuid())){
            Directory eventDirectory = event.getDirectory();
            if(root.getId().equals(eventDirectory.getId())){
                root = event.getDirectory();
                root.setName(getResources().getString(R.string.root_directory_name));
                parentDirectory = root;
            }else{
                for(Resource r : parentDirectory.getChildren()){
                    if(eventDirectory.getId().equals(r.getId()) && r instanceof Directory){
                        Directory d = (Directory) r;
                        d.setChildren(eventDirectory.getChildren());
                        parentDirectory = d;
                       
                        break;
                    }
                }
            }
            mResourcesAdapter.setDirectory(parentDirectory);
            mSubtitle.setText(parentDirectory.getName());
        }
    }
    public void onEventMainThread(NotifyDeviceListChangedEvent event) {
        mediaServer = (MediaServer) mCallback.getCurrentDevice();
        if(parentDirectory == null){
            mCallback.browseMediaServerDirectory(mediaServer,root.getId(),root.getParentId());
        }
        
        mRenderersAdapter = new MediaRendererCollectionAdapter(getActivity(), new DeviceUpnpCollection(mCallback.getUpnpDevices().allDevices(),MediaRenderer.class));
        OnItemSelectedListener listener = mMediaRenderersSpinner.getOnItemSelectedListener();
        setAdapterAndlistenerToSpinner(null,null);
        setAdapterAndlistenerToSpinner(mRenderersAdapter,listener);
        
        updateDefaultRenderer();
        setDeviceState();
    }

    private void setDeviceState(){
    	setHasOptionsMenu(false);
        if(mediaServer.isAvailable()){
            mNotConnectedInfoView.setVisibility(View.INVISIBLE);
            if(selectedFile!=null){
                mMainList.setVisibility(View.INVISIBLE);
                mFileView.setVisibility(View.VISIBLE);
                mSubtitle.setText(selectedFile.getName());
                setHasOptionsMenu(true);
            }else{
                mMainList.setVisibility(View.VISIBLE);
                mFileView.setVisibility(View.INVISIBLE);
                if(parentDirectory!=null){
                    mSubtitle.setText(parentDirectory.getName());
                }else{
                    mSubtitle.setText("");
                }
            }
        }else{
            mNotConnectedInfoView.setVisibility(View.VISIBLE);
            mMainList.setVisibility(View.INVISIBLE);
            mFileView.setVisibility(View.INVISIBLE);
            mSubtitle.setText(R.string.not_connected);
        }
    }

    @Override
    public void setHasOptionsMenu(boolean hasMenu){
    	super.setHasOptionsMenu(hasMenu);
    	mOptionsMenuIcon.setVisibility(hasMenu?View.VISIBLE:View.INVISIBLE);
    }
    private void updateDefaultRenderer() {
        Log.i(TAG,"updateDefaultRenderer");

        MediaRenderer device = mCallback.getDefaultMediaRenderer();
        if (device != null && mMediaRenderersSpinner != null) {
            for (int i = 0; i < mRenderersAdapter.getCount(); ++i) {
                if (device.getKey().equals(mRenderersAdapter.getItem(i).getKey())) {
                    Log.i(TAG,"setSelection "+i);
                    mMediaRenderersSpinner.setSelection(i);
                    
                    return;
                }
            }
        }
        Log.i(TAG,"setSelection default");
        if(mRenderersAdapter.getCount()==0){
            mPlayButton.setEnabled(false);
            mScheduleButton.setEnabled(true);
        }
        mMediaRenderersSpinner.setSelection(0);
    }
    
    private void setAdapterAndlistenerToSpinner(SpinnerAdapter adapter,OnItemSelectedListener listener){
        Log.i(TAG,"setAdapterAndlistenerToSpinner("+adapter+","+listener+")");
        if(mMediaRenderersSpinner!=null){
            mMediaRenderersSpinner.setAdapter(adapter);
            if(listener != mMediaRenderersSpinner.getOnItemSelectedListener()){
                mMediaRenderersSpinner.setOnItemSelectedListener(listener);
            }
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Resource resource = mResourcesAdapter.getItem(position);
        if(resource instanceof Directory){
            ((Directory)resource).setParent(parentDirectory);
            mCallback.browseMediaServerDirectory(mediaServer,resource.getId(), resource.getParentId());
        }else if(resource instanceof File){
            selectedFile = (File) resource;
            mSubtitle.setText(selectedFile.getName());
            mFilePropertyAdapter.setFile(selectedFile);
            setDeviceState();
        }
    }

    class SpinnerOnItemSelectedListener implements OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            Log.i(TAG, "onItemSelected pos." + position);
            MediaRenderer mr = mRenderersAdapter.getItem(position);
            mCallback.setDefaultMediaRenderer(mr);
            mPlayButton.setEnabled(mr.isAvailable());
            mScheduleButton.setEnabled(mr.isAvailable());
        }
        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            Log.i(TAG,"onNothingSelected");
        }
    }
}
