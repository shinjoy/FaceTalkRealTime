package com.nomadsoft.chat.linkedNetwork;

import java.util.concurrent.ConcurrentHashMap;

import com.nomadsoft.chat.config.Config;
import com.nomadsoft.chat.db.dao.ChatDao;
import com.nomadsoft.chat.db.dto.Packet;
import com.nomadsoft.chat.db.dto.Param;

public class ClientPacketBufferThread extends Thread{
	//BlockingQueue 
	final ConcurrentHashMap<Long, Packet> buffer = new ConcurrentHashMap<Long, Packet>();
	
	ConcurrentHashMap<Long, Packet> getBuffer() {
		return buffer;
	}
	
	static ClientPacketBufferThread clientPacketBufferThread = null;
	public static ClientPacketBufferThread getInstance() {
		if(clientPacketBufferThread == null) {
			clientPacketBufferThread = new ClientPacketBufferThread();
		}
		return clientPacketBufferThread;
	}
	
	static long bufferKey = 0;
	public static long getBufferKey() {		
		++bufferKey;
		
		if(bufferKey > 1000000) {
			bufferKey = 0;
		}
		return Long.valueOf(String.valueOf(System.currentTimeMillis()) + String.valueOf((bufferKey)));		
	}
	
	public void add(String param, int pt, int st, String destID, long bufKey ) {
		Packet packet = new Packet();		
		
		
		packet.setBufferKey(bufKey);
		packet.setDestID(destID);
		packet.setPt(pt);
		packet.setSt(st);
		packet.setSendTick(System.currentTimeMillis());
		packet.setServerID(Config.getInstance().getServerId());
		
		packet.setParam(param);
		
		getBuffer().put(bufKey, packet);
	}
	
	public void remove(long bufKey) {
		buffer.remove(bufKey);
	}

	public void run() {
		try {
			while(interrupted() == false) {
				try {
					long now = System.currentTimeMillis();
					for(Packet packet : buffer.values()) {
						long delay = now - packet.getSendTick();
						if(delay > 10000) {
							//지연이 10초보다 크다면
							buffer.remove(delay);
							
							//DB 기록.
							ChatDao.insertBufferMsg(packet);
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					Thread.sleep(10);
				}				
			}			
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
}
