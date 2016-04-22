package com.nomadsoft.chat.config;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.nomadsoft.chat.db.dao.ServerDao;
import com.nomadsoft.chat.db.dto.ServerInfo;

public class ServerList {
	static ServerList serverList = null;
	
	ServerInfo localServer = null;	
	ConcurrentHashMap<String, ServerInfo> remoteServerList = null; 
	
	
	public static ServerList getInstance() {
		if(serverList == null) {
			serverList = new ServerList();
		}
		return serverList;
	}
	
	public ServerInfo getLocalServer() {
		return localServer;
	}

	public ConcurrentHashMap<String, ServerInfo> getRemoteServerList() {
		return remoteServerList;
	}

	public ServerList() {	
		localServer = ServerDao.getLocalServerInfo(Config.getInstance().getServerId());
		remoteServerList = ServerDao.getRemoteServerList(Config.getInstance().getServerId());		
	}
	
	
	public void updateServerList() {
		try {
			ServerInfo oldLocalServer = localServer;
			ConcurrentHashMap<String, ServerInfo> oldRemoteServerList = remoteServerList;
			
			ServerInfo newLocalServer = ServerDao.getLocalServerInfo(Config.getInstance().getServerId());
			ConcurrentHashMap<String, ServerInfo> newRemoteServerList = ServerDao.getRemoteServerList(Config.getInstance().getServerId());
			
			synchronized (this) {
				localServer = newLocalServer;
				remoteServerList = newRemoteServerList;
				
				oldLocalServer = null;
				oldRemoteServerList.clear();
				oldRemoteServerList = null;
			}			
			
			if(localServer == null || localServer.getStatus() != 0) {
				//서비스 종료
				//구현필요.
				System.exit(0);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
