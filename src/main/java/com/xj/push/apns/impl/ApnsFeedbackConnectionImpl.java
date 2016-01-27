/*
 * Copyright 2013 DiscoveryBay Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xj.push.apns.impl;

import static com.xj.push.apns.model.ApnsConstants.*;

import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.net.SocketFactory;

import com.xj.push.apns.IApnsFeedbackConnection;
import com.xj.push.apns.model.ApnsConfig;
import com.xj.push.apns.model.Feedback;
import com.xj.push.apns.tools.ApnsTools;

/**
 * @author RamosLi
 */
public class ApnsFeedbackConnectionImpl implements IApnsFeedbackConnection {

	private static int READ_TIMEOUT = 10000; // 10s
	private static int DATA_LENGTH = 38;
	private ApnsConfig config;
	private SocketFactory factory;

	public ApnsFeedbackConnectionImpl(ApnsConfig config, SocketFactory factory) {
		this.config = config;
		this.factory = factory;
	}

	@Override
	public List<Feedback> getFeedbacks() {
		List<Feedback> list = null;
		Socket socket = null;
		InputStream is = null;
		try {
			String host = FEEDBACK_HOST_PRODUCTION_ENV;
			int port = FEEDBACK_PORT_PRODUCTION_ENV;
			if (config.isDevEnv()) {
				host = FEEDBACK_HOST_DEVELOPMENT_ENV;
				port = FEEDBACK_PORT_DEVELOPMENT_ENV;
			}
			socket = factory.createSocket(host, port);
			socket.setSoTimeout(READ_TIMEOUT);
			
			is = socket.getInputStream();
			while (true) {
				byte[] bytes = new byte[DATA_LENGTH];
				int size = is.read(bytes);
				if (size == DATA_LENGTH) {
					if (list == null) {
						list = new ArrayList<Feedback>();
					}
					/**
					 * see The Feedback Service chapter
					 * https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/CommunicatingWIthAPS.html
					 * 
					 */
					byte[] tokenByte = new byte[32]; 
					System.arraycopy(bytes, 6, tokenByte, 0, 32);
					Feedback feedback = new Feedback();
					feedback.setTime(ApnsTools.parse4ByteInt(bytes[0], bytes[1], bytes[2], bytes[3]));
					feedback.setToken(ApnsTools.encodeHex(tokenByte).toLowerCase());
					list.add(feedback);
				} else {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return list;
	}
}