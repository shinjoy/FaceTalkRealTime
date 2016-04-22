package com.nomadsoft.chat.linkedNetwork;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.nomadsoft.chat.config.Config;
import com.nomadsoft.chat.db.dao.ChatDao;
import com.nomadsoft.chat.db.dto.Packet;
import com.nomadsoft.chat.db.dto.Param;
import com.nomadsoft.chat.openServer.ChatServerHandler;
import com.nomadsoft.chat.openServer.ChatUserContext;
import com.nomadsoft.chat.openServer.ProtocolType;

public class ClientBufferConsumerThread extends Thread{

	static ClientBufferConsumerThread clientBufferConsumerThread = null;
	public static ClientBufferConsumerThread getInstance() {
		if(clientBufferConsumerThread == null) {
			clientBufferConsumerThread = new ClientBufferConsumerThread();
		}
		return clientBufferConsumerThread;
	}
	
	public void run() {
		try {
			while(interrupted() == false) {
				try {		
					ArrayList<Packet> pList = ChatDao.getBufferMsg();
					
					if(pList != null) {						
						for (Packet packet : pList) {							
							ChatUserContext userContext = ChatUserContext.getUserContextMap().get(packet.getDestID());
							
							if(userContext != null) {
								ChatServerHandler.sendRawData(userContext.getChannel(), ProtocolType.ptChat
									, ProtocolType.stChatStatusRes, packet.getParam());					
							}
						}						
					}
					
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					Thread.sleep(100);
				}				
			}			
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
}
