package com.xj.xj_push;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.xj.push.apns.IApnsService;
import com.xj.push.apns.impl.ApnsServiceImpl;
import com.xj.push.apns.model.ApnsConfig;
import com.xj.push.apns.model.Payload;

/**
 * Unit test for simple App.
 */
public class AppTest {
	
	private static IApnsService apnsService;
	
	
	  public static void main(String[] args) {
			IApnsService service = getApnsService();
			
			Map<String,Object> content = new HashMap<String,Object>();
			content.put("CMD_CODE", 100);
			content.put("title", "hello world3");
			content.put("content", "hello3");
			content.put("messageId", System.currentTimeMillis());
			content.put("timestamp", System.currentTimeMillis()/1000);
			
			Payload payload = new Payload();
			payload.setAlert("hello3");
			payload.setBadge(1);
			payload.setSound("default");
			payload.addParam("content", content);
			
			service.sendNotification("aa2c6e99449cce1fad4574ccaa3c6c37828c26b64f9d3d7bb060e2d1faf06f4e", payload);
	}
  
  private static IApnsService getApnsService(){
	  if (apnsService == null) {
			ApnsConfig config = new ApnsConfig();
			InputStream is = AppTest.class.getClassLoader().getResourceAsStream("push.p12");
			config.setKeyStore(is);
			config.setDevEnv(true);
			config.setPassword("123456");
			config.setPoolSize(3);
			apnsService = ApnsServiceImpl.createInstance(config);
		}
		return apnsService;
  }
  
}
