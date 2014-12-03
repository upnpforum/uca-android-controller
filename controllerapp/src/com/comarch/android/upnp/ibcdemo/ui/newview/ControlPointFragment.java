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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.comarch.android.upnp.ibcdemo.R;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppChatMessageRecivedEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppSendChatMessageEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.data.DeviceDescription;
import com.comarch.android.upnp.ibcdemo.model.ControlPoint;
import com.comarch.android.upnp.ibcdemo.model.Credentials;
import com.comarch.android.upnp.ibcdemo.model.MessageChat;
import com.comarch.android.upnp.ibcdemo.persistence.ChatDao;
import com.comarch.android.upnp.ibcdemo.persistence.CredentialsPersistence;
import com.comarch.android.upnp.ibcdemo.ui.newview.busevents.NotifyDeviceListChangedEvent;
import com.comarch.android.upnp.ibcdemo.util.deliverer.FragmentWithBusDeliverer;

public class ControlPointFragment extends FragmentWithBusDeliverer{

	private Callback mCallback;
	
	private WebView mMessagesHistoryText;
	private EditText mInputText;
	private TextView mTitleText;
	private TextView mSubtitleText;
	private LinearLayout mNewMessageLayout;
	private TextView mNotConnected;
	
	private ChatDao mChatDao;
    private CredentialsPersistence credDao;
    
    private final String STYLE = "<style>.msg{width:90%;border-radius:5px;margin-bottom: 5px; padding:5px;} .sent{float:right; text-align:right;background-color:#FFCCCC;} .recived{float:left;background-color:#CCCCFF;} .time{color:#555; font-size:8px;}</style>";
	private final String HEADER = "<html><head><meta charset='utf-8'> "+STYLE+"</head><body>";
	private final String FOOTER = "</body></html>";
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
   
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_control_point, container, false);
        mainView.setBackgroundColor(0xFFFFFFFF);
        
        mTitleText = (TextView) mainView.findViewById(R.id.cp_title);
        mSubtitleText = (TextView) mainView.findViewById(R.id.cp_subtitle);
        mNewMessageLayout = (LinearLayout) mainView.findViewById(R.id.newMessageLayout);
        mNotConnected = (TextView) mainView.findViewById(R.id.cp_not_connected_info);
        
        mMessagesHistoryText = (WebView) mainView.findViewById(R.id.messagesList);
        mMessagesHistoryText.setWebViewClient(new WebViewClient(){
        	public void onPageFinished(final WebView view,String url){
        		getActivity().runOnUiThread(new Runnable(){
					public void run() {
						new Handler().postDelayed(new Runnable(){
							public void run() {
								view.pageDown(true);
							}
							
						},100);
					}
        		});
        		
        	}
        });
        	

        mInputText = (EditText) mainView.findViewById(R.id.inputChatText);
        mainView.findViewById(R.id.cp_headerBar).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               mCallback.finishFragment();
            }
        });
        
        mainView.findViewById(R.id.sendChatButton).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!mInputText.getText().toString().isEmpty()){
					sendMessage(mInputText.getText().toString());
					mInputText.setText("");
				}
				
			}
		});
        
        mChatDao = new ChatDao(getActivity());
        credDao = new CredentialsPersistence(getActivity());
        
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
    public void onResume() {
        super.onResume();
        getBus().register(this);
        updateMessagesList();
        mTitleText.setText("Control point");
        mSubtitleText.setText(mCallback.getCurrentDevice().getName());
        updateConnectionState();
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	getBus().unregister(this);
    }
    
    public void onEventMainThread(XmppChatMessageRecivedEvent event){
    	String jid = null;
    	DeviceDescription desc = mCallback.getCurrentDevice().getDescription();
    	if(desc!=null) jid = desc.getJid();
    	if(event.getFromUuid().equals(jid)){
    		updateMessagesList();
    	}
    }
    public void updateMessagesList(){
    	String fromUuid = mCallback.getCurrentDevice().getUuid();
        List<MessageChat> list;
        try{
        	mChatDao.open();
        	list = mChatDao.getMessages(fromUuid);
        	mChatDao.markAsRead(fromUuid);
        	((ControlPoint)mCallback.getCurrentDevice()).setUnreaded(false);
        }finally{
        	mChatDao.close();
        }
        
        if(list!=null){
        	Long lastDatetime = 0L;
	        StringBuilder data = new StringBuilder(HEADER);
	        for(MessageChat msg : list){
	        	boolean isFrom = msg.getFrom().equals(fromUuid);
	        	Date date = new Date(msg.getDatetime());
	        	String dateString = "<div class='time'>"+dateFormat.format(date)+"</div>";
				data.append("<div class='msg "+(isFrom?"recived":"sent")+"'>"+dateString+msg.getMessage()+"</div>");
				lastDatetime = msg.getDatetime();
	        }
	        data.append(FOOTER);
	        mMessagesHistoryText.loadDataWithBaseURL("file:///"+lastDatetime.toString(),data.toString(), "text/html", "UTF-8","");
	        //mMessagesHistoryText.reload();
	        
        }
    }
    public void sendMessage(String body){
    	String jid = mCallback.getCurrentDevice().getDescription().getJid();
		String uuid = mCallback.getCurrentDevice().getUuid();
		getBus().post(new XmppSendChatMessageEvent(body, jid));
		try{
			mChatDao.open();
			Credentials data = credDao.load();
			mChatDao.add(data.getUuid(), uuid, body, new Date().getTime());
		}finally{
			mChatDao.close();
		}
		updateMessagesList();
    }
    
    private void updateConnectionState(){
    	boolean available = mCallback.getCurrentDevice().isAvailable();
    	mNewMessageLayout.setVisibility(available?View.VISIBLE:View.INVISIBLE);
    	mNotConnected.setVisibility(!available?View.VISIBLE:View.INVISIBLE);
    }
    
    public void onEventMainThread(NotifyDeviceListChangedEvent event) {
    	updateConnectionState();
    }
}
