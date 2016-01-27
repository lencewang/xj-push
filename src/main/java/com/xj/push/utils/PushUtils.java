package com.xj.push.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.ListMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.AbstractTemplate;
import com.gexin.rp.sdk.template.TransmissionTemplate;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Sender;
import com.xj.emob.Messages;
import com.xj.push.Push2ClientBuilder;
import com.xj.push.apns.IApnsService;
import com.xj.push.apns.model.Payload;

import cn.jpush.api.JPushClient;

public class PushUtils {
	
	/**
	 * 个推推送消息    uses 最大999
	 * @return
	 */
	public static void sendGtPushMessage(IGtPush gtPush, String appId, String appkey, ObjectNode node,String equipment,String ...users){
		Assert.isTrue(users.length<1000 ,"单次推送用户数量最多999");
		Assert.isTrue(users.length>0 ,"请提供目标用户");
		List<Target> targets = new ArrayList<Target>(users.length);
		for (String user : users) {
			Target target = new Target();
			target.setAlias(user);
			target.setAppId(appId);
			targets.add(target);
		}
		gtPush.pushMessageToList(gtPush.getContentId(getListMessage(getTransmissionTemplate(appId, appkey, node))), targets);
	}
	
	
	/**
	 * 个推推送通知
	 * @return
	 */
	public static void sendGtPushNotification(IGtPush gtPush,String appId, String appkey, ObjectNode node,String ...string){
		Assert.isTrue(string.length<1000 ,"单次推送用户数量最多999");
		Assert.isTrue(string.length>0 ,"请提供目标用户");
		List<Target> targets = new ArrayList<Target>(string.length);
		for (String user : string) {
			Target target = new Target();
			target.setAlias(user);
			target.setAppId(appId);
			targets.add(target);
		}
		IPushResult result = gtPush.pushMessageToList(gtPush.getContentId(getListMessage(getTransmissionTemplate(appId, appkey, node))), targets);
		System.out.println(result);
	}
	
	
	/**
	 * 获取通知模板
	 * @param appId
	 * @param appkey
	 * @param node
	 * @return
	 */
//	private static AbstractTemplate getNotificationTemplate(String appId, String appkey, ObjectNode node) {
//		NotificationTemplate template = new NotificationTemplate();
//		template.setAppId(appId);
//		template.setAppkey(appkey);
//		template.setTitle(node.get("title").toString());
//		template.setText(node.get("content").toString());
//		template.setIsRing(true); //响铃
//		template.setIsVibrate(true); //震动
//		template.setIsClearable(true);  //可清除
//		template.setTransmissionType(1); // 透传消息设置，1为强制启动应用，客户端接收到消息后就会立即启动应用；2为等待应用启动
//		template.setTransmissionContent(node.toString());
//		
//		return template;
//	}
	
	/**
	 * 获取透传消息模板
	 * @param appId
	 * @param appkey
	 * @param node
	 * @param equipment 
	 * @return
	 */
	private static AbstractTemplate getTransmissionTemplate(String appId, String appkey,ObjectNode  node) {
		TransmissionTemplate template = new TransmissionTemplate();
		template.setAppId(appId);
		template.setAppkey(appkey);
		template.setTransmissionType(2);
		template.setTransmissionContent(node.toString());
		
		return template;
	}

	/**
	 * 获取多人推送消息
	 * @param template
	 * @return
	 */
	private static ListMessage getListMessage(AbstractTemplate template) {
		ListMessage message = new ListMessage();
		message.setData(template);
		message.setOffline(true);
		message.setOfflineExpireTime(60*1000);
		message.setPushNetWorkType(0);
		
		return message;
	}
	
	/**
	 * 极光推送消息
	 * @param jPushClient
	 * @param node
	 * @param equipment
	 * @param users
	 */
	public static void sendJpushMessage(JPushClient jPushClient,  ObjectNode node,String equipment,String ...users) {
		Assert.isTrue(users.length<1000 ,"单次推送用户数量最多999");
		Assert.isTrue(users.length>0 ,"请提供目标用户");
		
		try {
			jPushClient.sendAndroidMessageWithAlias(node.get("title").toString(), node.toString(), users);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 极光推送通知 
	 * @param jPushClient
	 * @param node
	 * @param equipment
	 * @param users
	 */
//	public static void sendJpushNotification(JPushClient jPushClient, Map<String,String> node,String equipment,String ...users) {
//		Assert.isTrue(users.length<1000 ,"单次推送用户数量最多999");
//		Assert.isTrue(users.length>0 ,"请提供目标用户");
//		try {
//			jPushClient.sendAndroidNotificationWithAlias(node.get("title"), node.get("title"), node, users);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * 小米推送
	 * @param sender
	 * @param node
	 * @param packageName
	 * @param users
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void sendMiPushMessage(Sender sender,  ObjectNode node,String packageName,String ...users) throws IOException, ParseException {
		Assert.isTrue(users.length<1000 ,"单次推送用户数量最多999");
		Assert.isTrue(users.length>0 ,"请提供目标用户");
		Message message = new Message.Builder().title(node.get("title").toString()) .description(node.get("content").toString()).payload(node.toString()).restrictedPackageName(packageName).notifyType(1).build();    // 使用默认提示音提示
		sender.sendToAlias(message, Arrays.asList(users), 0);
	}
	
	/**
	 * 环信推送  
	 * @param sender
	 * @param user
	 * @param node
	 */
	public static void sendEmobPushMessage(Messages messages, ObjectNode node,String ...users) {
		Assert.isTrue(users.length<1000 ,"单次推送用户数量最多999");
		Assert.isTrue(users.length>0 ,"请提供目标用户");
		
		JsonNodeFactory factory = JsonNodeFactory.instance;
		ArrayNode arrayNode = factory.arrayNode();
		for (String user : users) {
			arrayNode.add(user);
		}
		ObjectNode objectMsg = factory.objectNode().put("type", "txt").put("msg", node.get("content").toString());
		ObjectNode myNode = factory.objectNode().put("ext",node.toString());
		messages.sendMessages("users", arrayNode,objectMsg, "85317d8dec832c56171a0d039b813861", myNode);
	}
	
	
	

	/**
	 * 发送ios推送
	 * @param node
	 * @param messageId
	 * @param users
	 */
	public static void sendIosPush(ObjectNode node, Long messageId, String... users) {
		if (null == users || users.length == 0) {
			return;
		}
		
		IApnsService service = Push2ClientBuilder.getApnsService();
		for (int i = 0; i < users.length; i++) {
			try {
				Payload payload = new Payload();
				payload.setAlert(node.get("title").toString());
				payload.setBadge(1);
				payload.setSound("default");
				payload.addParam("content", node);
				service.sendNotification(users[i], payload);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}
}
