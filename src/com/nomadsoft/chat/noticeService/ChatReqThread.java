package com.nomadsoft.chat.noticeService;

import com.nomadsoft.chat.db.dao.ChatReqDao;
import com.nomadsoft.chat.log.Log;

public class ChatReqThread extends Thread{
	static boolean isRunning = true;
	static ChatReqThread instance = null;
	public static ChatReqThread getChatReqThread() {
		return instance;
	}
	
	public static boolean isRunning() {
		return isRunning;
	}


	@Override
	public void run() {
		try {			
			isRunning = true;
			checkNoticePush();
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	void checkNoticePush() {
		try {
			Thread.sleep(10000);
			
			while(true) {
				
				Thread.sleep(50);
				try {
					ChatReqDao.loadChatReq();					
				} catch (Exception e) {
					// TODO: handle exception
					Log.w(e.getMessage());
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.w(e.getMessage());
		} finally {
			isRunning = false;			
		}
	}
	
	
	
	public static void startThread() {
		if(instance != null) {
			instance.interrupt();
			instance = null;
		}
		
		instance = new ChatReqThread();
		instance.start();				
	}	
}
