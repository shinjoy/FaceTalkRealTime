package com.nomadsoft.util.push;

import com.nomadsoft.chat.db.dao.UserDao;

public class PushSender {
	
	public static void sendPushMsg(String sendId, String sendName, String recvId, String msg
			, String msgKey, String msgType) {
		try {
			//구현필요		
			
			String pushMsg = sendName + " : " +  msg;
			if(pushMsg.length() > 50) {
				pushMsg = pushMsg.substring(0, 50);
			}
			
			PushKey pk = UserDao.getPush(recvId);
			
			
			if(pk != null) {
				
				//if("ios".equals(pk.getOs())) {
				
				PushNotification.sendPush(false, recvId, pushMsg, pk.getPushKey(), "0"
						, msgKey
						, pk.getPushType()
						, pk.getOs(), sendId, "", 0, "msc", -1);
				
				
				//}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
}
