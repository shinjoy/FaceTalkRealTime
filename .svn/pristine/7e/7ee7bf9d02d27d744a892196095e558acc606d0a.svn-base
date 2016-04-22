package com.nomadsoft.util.push;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.nomadsoft.chat.config.Config;
import com.nomadsoft.chat.config.PushServiceList;
import com.nomadsoft.chat.db.dao.PushMsgDao;
import com.nomadsoft.chat.db.dao.UserDao;
import com.nomadsoft.chat.db.dto.PushService;
import com.nomadsoft.chat.log.Log;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

public class NoticePushSendThread extends Thread{
	static boolean isRunning = true;
	final ConcurrentLinkedQueue<PushMsg> notiBuffer = new ConcurrentLinkedQueue<PushMsg>();
	
	static NoticePushSendThread instance = null;
	public static NoticePushSendThread getNoticePushSendThread() {
		return instance;
	}
	
	public static boolean isRunning() {
		return isRunning;
	}

	public static void startThread() {
		if(instance != null) {
			instance.interrupt();
			instance = null;
		}
		
		instance = new NoticePushSendThread();
		instance.start();				
	}	
	
	public NoticePushSendThread() {
		try {
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.w(e.getMessage());
		}
	}
	

	ConcurrentLinkedQueue<PushMsg> getNotiBuffer() {
		return notiBuffer;
	}
	
	public void addNoti(PushMsg item ) {
		getNotiBuffer().add(item);
	}

	public void run() {
		try {
			isRunning = true;
			while(interrupted() == false) {
				try {
					if(notiBuffer.isEmpty() == false) {
						sendPush(notiBuffer);					
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				} finally {
					Thread.sleep(200);
				}				
			}			
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			isRunning = false;			
		}
	}
	
	void sendPush(ConcurrentLinkedQueue<PushMsg> list) {
		try {		
			PushMsg item = null;
			int i = 0;
			
			while(i < 1000) {
				i++;
				item = list.poll();				
				if(item != null ) {
					boolean sendResult = false;
					
					try {
						PushService pushService = PushServiceList.loadPushService(item.getServiceId());
						if(pushService != null) {
							if("ios".equals(item.getOs())) {
								int badge = UserDao.getUserBadgeCnt(item.getUserId());
								String sound = "";
								if( (item.getPushType() & 2) == 2) {
									sound = "default";// "police.aiff";//						
								}
								
								String payload = APNS.newPayload().alertBody(item.getMsg())
										.sound(sound)
										.badge(badge)
										.customField("P1", item.getMsgType())
										.customField("P2", item.getMsgKey())
										.build();					
								String token = item.getPushKey();
								
								ApnsService apns = pushService.getApnsService();
								if(apns != null) {
									apns.push(token, payload);	
									sendResult = true;
								}
							} else {
								Message.Builder messageBuilder = new Message.Builder();								
						        messageBuilder.addData("pkgId", Config.getInstance().getGcm_pkg_id());
						        messageBuilder.addData("message", item.getMsg());
	
						        messageBuilder.addData("msgKey", item.getMsgKey());
						        messageBuilder.addData("msgType", item.getMsgType());
						        messageBuilder.addData("sendId", item.getSendId());
						        messageBuilder.addData("senderName", item.getSenderName());
						        
						        Message msg = messageBuilder.build();
								Result result;
								try {
									Sender sender = pushService.getGcmSender();
									
									result = sender.send(msg, item.getPushKey(), 5);
									if(result.getMessageId() != null) {
									    //성공																		
										sendResult = true;
								    } else {
								    	//실패		
								    	System.out.print(result.getErrorCodeName());
								    }									
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}  
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
					} finally {
						if(sendResult) {
							if(item.getSeq() > -1) PushMsgDao.backupData(item.getSeq(), 3);	
						} else {
							if(item.getSeq() > -1) PushMsgDao.backupData(item.getSeq(), 4);
						}
					}
				} else {				
					break;
				}
			} 	
			
		} catch (Exception e) {
			// TODO: handle exception			
			Log.i(e.getMessage());
		}
	}
	
}
