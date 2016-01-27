package com.xj.push;

import java.io.IOException;
import java.io.InputStream;

import com.gexin.rp.sdk.http.IGtPush;
import com.xiaomi.xmpush.server.Sender;
import com.xj.push.apns.IApnsService;
import com.xj.push.apns.impl.ApnsServiceImpl;
import com.xj.push.apns.model.ApnsConfig;
import com.xj.util.SystemProperties;

import cn.jpush.api.JPushClient;
/**
 * 用户端推送
 * @author Administrator
 *
 */
public class Push2ClientBuilder{
	
	private static SystemProperties properties = SystemProperties.getInstance();
	
	//个推
	private IGtPush gtPush = null;
	
	//极光
	private JPushClient jPushClient = null;
	
	//小米推
	private Sender sender = null;
	
	//apns
	private static IApnsService apnsService;
	
	public IGtPush buildGtPushClient() {
		gtPush = new IGtPush(properties.get("push.gtpush.client.host"), properties.get("push.gtpush.client.appkey"), properties.get("push.gtpush.client.master"));  
		return gtPush;
	}

	public JPushClient buildJPushClient() {
		jPushClient = new JPushClient(properties.get("push.jpush.client.masterSecret"),properties.get("push.jpush.client.appKey") , 3);
		return jPushClient;
	}
	
	public Sender buildMiPushClient() {
		sender = new Sender(properties.get("push.mipush.client.app_secret_key"));
		return sender;
	}
	
	public String getMiPushpackageName(){
		return properties.get("push.mipush.client.my_package_name");
	}
	
	public String getGtPushAppkey(){
		return properties.get("push.gtpush.client.appkey");
	}
	
	public String getGtPushAppid(){
		return properties.get("push.gtpush.client.appId");
	}
	
	public static synchronized IApnsService getApnsService() {
		if (apnsService == null) {
			String keypath = properties.get("push.ios.keypath");
			try (InputStream is = Push2ClientBuilder.class.getClassLoader().getResourceAsStream(keypath)) {
				ApnsConfig config = new ApnsConfig();
				config.setKeyStore(is);
				config.setDevEnv(false);
				config.setPassword(properties.get("push.ios.kspassword"));
				config.setPoolSize(5);
				
				apnsService = ApnsServiceImpl.createInstance(config);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return apnsService;
	}

}
