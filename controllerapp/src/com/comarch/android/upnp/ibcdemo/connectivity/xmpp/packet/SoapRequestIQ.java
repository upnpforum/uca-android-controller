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

package com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet;

import java.util.Map;

import org.jivesoftware.smack.packet.IQ;

import com.google.common.xml.XmlEscapers;

public class SoapRequestIQ extends IQ {

	@SuppressWarnings("unused")
	private final String TAG = getClass().getSimpleName();

	private final String rawBody;

	public SoapRequestIQ(final String actionName, final String serviceType,
			final Map<String, String> arguments) {
		super();
		// Log.v(TAG, "Create SoapRequestIQ");
		this.setType(Type.SET);

		rawBody = createBody(actionName, serviceType, arguments);
	}

	private static final String createBody(final String actionName,
			final String serviceType, final Map<String, String> arguments) {
		StringBuilder builder = new StringBuilder();

		builder.append("<s:Envelope xmlns:s='http://schemas.xmlsoap.org/soap/envelope/'"
				+ " s:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/' xmlns='upnpcloud'>");
		builder.append("<s:Body>");
		builder.append("<u:" + actionName + " xmlns:u='" + serviceType + "'>");

		if (arguments != null) {
			for (String key : arguments.keySet()) {
				String value = arguments.get(key);
				builder.append("<" + key + ">");
				builder.append(XmlEscapers.xmlAttributeEscaper().escape(value));
				builder.append("</" + key + ">");
			}
		}

		builder.append("</u:" + actionName + ">");
		builder.append("</s:Body>");
		builder.append("</s:Envelope>");

		return builder.toString();
	}

	@Override
	public String getChildElementXML() {
		return rawBody;
	}
}
