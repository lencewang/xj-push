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

import static com.xj.push.apns.model.ApnsConstants.ALGORITHM;
import static com.xj.push.apns.model.ApnsConstants.KEYSTORE_TYPE;
import static com.xj.push.apns.model.ApnsConstants.PROTOCOL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xj.push.apns.IApnsConnection;
import com.xj.push.apns.IApnsFeedbackConnection;
import com.xj.push.apns.IApnsService;
import com.xj.push.apns.model.ApnsConfig;
import com.xj.push.apns.model.Feedback;
import com.xj.push.apns.model.Payload;
import com.xj.push.apns.model.PushNotification;
import com.xj.push.apns.tools.ApnsTools;

/**
 * The service should be created twice at most. One for the development env, and the other for the production env
 * @author RamosLi
 */
public class ApnsServiceImpl implements IApnsService {
	
	private Logger logger = LoggerFactory.getLogger(ApnsConnectionImpl.class);
	
	private ExecutorService service = null;
	private ApnsConnectionPool connPool = null;
	private IApnsFeedbackConnection feedbackConn = null;
	
	private ApnsServiceImpl(ApnsConfig config) {
		int poolSize = config.getPoolSize();
		service = Executors.newFixedThreadPool(poolSize);
		
		SocketFactory factory = ApnsTools.createSocketFactory(config.getKeyStore(), config.getPassword(), KEYSTORE_TYPE, ALGORITHM, PROTOCOL);
		connPool = ApnsConnectionPool.newConnPool(config, factory);
		feedbackConn = new ApnsFeedbackConnectionImpl(config, factory);
	}
	
	@Override
	public void sendNotification(final String token, final Payload payload) {
		service.execute(new Runnable() {
			@Override
			public void run() {
				IApnsConnection conn = null; 
				try {
					conn = getConnection();
					conn.sendNotification(token, payload);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					if (conn != null) {
						connPool.returnConn(conn);
					}
				}
			}
		});
	}
	
	@Override
	public void sendNotification(final PushNotification notification) {
		service.execute(new Runnable() {
			@Override
			public void run() {
				IApnsConnection conn = null; 
				try {
					conn = getConnection();
					conn.sendNotification(notification);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					if (conn != null) {
						connPool.returnConn(conn);
					}
				}
			}
		});
	}
	
	private IApnsConnection getConnection() {
		IApnsConnection conn = connPool.borrowConn();
		if (conn == null) {
			throw new RuntimeException("Can't get apns connection");
		}
		return conn;
	}
	
	private static void checkConfig(ApnsConfig config) {
		if (config == null || config.getKeyStore() == null || config.getPassword() == null || "".equals(config.getPassword().trim())) {
			throw new IllegalArgumentException("KeyStore and password can't be null");
		}
		if (config.getPoolSize() <= 0 || config.getRetries() <= 0 || config.getCacheLength() <= 0) {
			throw new IllegalArgumentException("poolSize,retry, cacheLength must be positive");
		}
	}
	
	private static Map<String, IApnsService> serviceCacheMap = new HashMap<String, IApnsService>(3);
	public static IApnsService getCachedService(String name) {
		return serviceCacheMap.get(name);
	}
	
	public static IApnsService createInstance(ApnsConfig config) {
		checkConfig(config);
		String name = config.getName();
		IApnsService service = getCachedService(name);
		if (service == null) {
			synchronized (name.intern()) {
				service = getCachedService(name);
				if (service == null) {
					service = new ApnsServiceImpl(config);
					serviceCacheMap.put(name, service);
				}
			}
		}
		return service;
	}

	@Override
	public void shutdown() {
		service.shutdown();
		try {
			service.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Shutdown ApnsService interrupted", e);
        }
        connPool.close();
	}

	@Override
	public List<Feedback> getFeedbacks() {
		return feedbackConn.getFeedbacks();
	}
}