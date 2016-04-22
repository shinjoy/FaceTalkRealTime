package com.nomadsoft.chat.config;

import java.util.concurrent.ConcurrentHashMap;

import com.nomadsoft.chat.db.dao.PushMsgDao;
import com.nomadsoft.chat.db.dto.PushService;

public class PushServiceList {	
	static ConcurrentHashMap<String, PushService> serviceList = new ConcurrentHashMap<String, PushService>();

	public static ConcurrentHashMap<String, PushService> getServiceList() {
		return serviceList;
	}

	public static void loadServiceList() {
		PushMsgDao.loadPushService(serviceList);
	}	
	
	public static PushService loadPushService(String serviceId) {
		return serviceList.get(serviceId);		
	}
}
