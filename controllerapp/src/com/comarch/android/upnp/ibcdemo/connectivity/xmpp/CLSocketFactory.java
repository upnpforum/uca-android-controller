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

package com.comarch.android.upnp.ibcdemo.connectivity.xmpp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

public class CLSocketFactory extends SocketFactory {

    private static int roundrobin = 0;
    private static int timeout = 30000;
    
    public Socket createSocket(String paramString, int paramInt)
      throws IOException, UnknownHostException
    {
      Socket localSocket = new Socket(Proxy.NO_PROXY);
      InetAddress[] arrayOfInetAddress = InetAddress.getAllByName(paramString);
      localSocket.connect(new InetSocketAddress(arrayOfInetAddress[(roundrobin++ % arrayOfInetAddress.length)], paramInt),timeout);
      return localSocket;
    }
    
    public Socket createSocket(String paramString, int paramInt1, InetAddress paramInetAddress, int paramInt2)
      throws IOException, UnknownHostException
    {
      return new Socket(paramString, paramInt1, paramInetAddress, paramInt2);
    }
    
    public Socket createSocket(InetAddress paramInetAddress, int paramInt)
      throws IOException
    {
      Socket localSocket = new Socket(Proxy.NO_PROXY);
      localSocket.connect(new InetSocketAddress(paramInetAddress, paramInt),timeout);
      return localSocket;
    }
    
    public Socket createSocket(InetAddress paramInetAddress1, int paramInt1, InetAddress paramInetAddress2, int paramInt2)
      throws IOException
    {
      return new Socket(paramInetAddress1, paramInt1, paramInetAddress2, paramInt2);
    }

}
