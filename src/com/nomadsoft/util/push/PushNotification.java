package com.nomadsoft.util.push;


public class PushNotification {
	

	public static void sendPush(boolean isNotify, String userId, String contents, String pushKey, 
			String msgType, String msgKey, int pushType, String osType,
			String sendId, String senderName, int badge, String serviceId, long seq) {
		try {
			PushMsg msg = new PushMsg();			
			msg.setMsg(contents);
			msg.setMsgKey(msgKey);
			msg.setMsgType(msgType);			
			msg.setPushType(pushType);
						
			msg.setUserId(userId);
			msg.setOs(osType);
			msg.setPushKey(pushKey);
			msg.setSenderName(senderName);
			msg.setSendId(sendId);
			msg.setBadge(badge);
			msg.setServiceId(serviceId);
			msg.setSeq(seq);
			
			if(isNotify) {
				NoticePushSendThread.getNoticePushSendThread().addNoti(msg);			
			} else {
				PushSendThread.getPushSendThread().add(msg);					
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
