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

package com.comarch.android.upnp.ibcdemo.connectivity.xmpp.listeners;

import java.util.Random;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.StreamError;

import android.util.Log;

import com.comarch.android.upnp.ibcdemo.busevent.ConnectionStateChangedEvent.ConnectionState;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.XmppConnectionStateChangedEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.XmppConnector;

import de.greenrobot.event.EventBus;

public class CLConnectionListener implements ConnectionListener {

    private final String TAG = this.getClass().getSimpleName();
    
    private EventBus bus;
    
    private XmppConnector connector;
    
    private XMPPConnection connection;
    
    private boolean done=false;
    
    private Thread reconnectionThread;
    
    private int randomBase = new Random().nextInt(11) + 5;
    
    
    public CLConnectionListener(XmppConnector xmppConnector,XMPPConnection connection, EventBus eventBus) {
        super();
        connector = xmppConnector;
        this.connection = connection;
        this.bus = eventBus;
    }
    @Override
    public void connectionClosed() {
        Log.v(TAG, "connectionClosed");
        this.done = true;
        bus.postSticky(new XmppConnectionStateChangedEvent(ConnectionState.DISCONNECTED));
    }

    @Override
    public void connectionClosedOnError(Exception paramException) {
        
        Log.v(TAG, "connectionClosedOnError");
        this.done = false;
        if ((paramException instanceof XMPPException))
        {
          XMPPException localXMPPException = (XMPPException)paramException;
          StreamError localStreamError = localXMPPException.getStreamError();
          if (localStreamError != null)
          {
            String str = localStreamError.getCode();
            if ("conflict".equals(str)) {
              return;
            }
          }
        }
        if (isReconnectionAllowed()) {
          reconnect();
        }
        bus.postSticky(new XmppConnectionStateChangedEvent(ConnectionState.DISCONNECTED));

    }

    @Override
    public void reconnectingIn(int delay) {
        Log.v(TAG, "reconnectingIn ("+delay+")");
        if(isReconnectionAllowed()){
            bus.postSticky(new XmppConnectionStateChangedEvent(ConnectionState.CONNECTING));
        }
    }

    @Override
    public void reconnectionFailed(Exception arg0) {
        Log.v(TAG, "reconnectionFailed");
        if(isReconnectionAllowed()){
            bus.postSticky(new XmppConnectionStateChangedEvent(ConnectionState.CONNECTING));
        }
    }

    @Override
    public void reconnectionSuccessful() {
        Log.v(TAG, "reconnectionSuccessful");
        bus.postSticky(new XmppConnectionStateChangedEvent(ConnectionState.CONNECTED));
    }
    
    private boolean isReconnectionAllowed(){
        return !this.done && !connection.isConnected() && connector.isReconnectionAllowed();
    }
    protected synchronized void reconnect()
    {
      if (isReconnectionAllowed())
      {
        if ((this.reconnectionThread != null) && (this.reconnectionThread.isAlive())) {
          return;
        }
        this.reconnectionThread = new Thread()
        {
          private int attempts = 0;
          
          private int timeDelay()
          {
            this.attempts += 1;
            if (this.attempts > 13) {
              return CLConnectionListener.this.randomBase * 6 * 5;
            }
            if (this.attempts > 7) {
              return CLConnectionListener.this.randomBase * 6;
            }
            return CLConnectionListener.this.randomBase;
          }
          
          public void run()
          {
            while (CLConnectionListener.this.isReconnectionAllowed())
            {
              int i = timeDelay();
              while ((CLConnectionListener.this.isReconnectionAllowed()) && (i > 0)) {
                try
                {
                  Thread.sleep(1000L);
                  i--;
                  CLConnectionListener.this.notifyAttemptToReconnectIn(i);
                }
                catch (InterruptedException localInterruptedException)
                {
                  localInterruptedException.printStackTrace();
                  
                  CLConnectionListener.this.notifyReconnectionFailed(localInterruptedException);
                }
              }
              try
              {
                if (CLConnectionListener.this.isReconnectionAllowed()) {
                    CLConnectionListener.this.connection.connect();
                }
              }
              catch (XMPPException localXMPPException)
              {
                  CLConnectionListener.this.notifyReconnectionFailed(localXMPPException);
              }
            }
          }
        };
        this.reconnectionThread.setName("Smack Reconnection Manager");
        this.reconnectionThread.setDaemon(true);
        this.reconnectionThread.start();
      }
    }
    
    protected void notifyReconnectionFailed(Exception paramException)
    {
      reconnectionFailed(paramException);
    }
    
    protected void notifyAttemptToReconnectIn(int paramInt)
    {
      reconnectingIn(paramInt);
    }
    public void closeListener() {
        done = true;
    }

}
