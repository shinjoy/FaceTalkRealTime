package com.nomadsoft.chat.linkedNetwork;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.nomadsoft.chat.config.ServerList;
import com.nomadsoft.chat.db.dto.Param;
import com.nomadsoft.chat.db.dto.ServerInfo;
import com.nomadsoft.chat.log.Log;
import com.nomadsoft.chat.openServer.ChatServerHandler;
import com.nomadsoft.chat.openServer.ProtocolType;

public class ClientTimer extends TimerTask{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			//String log = "comm client Size : " + Client.getInstance().getClientChannelMap().size();
			
			ConcurrentHashMap<String, ServerInfo> serverList = ServerList.getInstance().getRemoteServerList();    	  
			for(ServerInfo serverInfo : serverList.values()) {
				if(serverInfo.getStatus() == 1) {
				    
					Channel channel = Client.getInstance().getClientChannelMap().get(serverInfo.getServer_id());					
					if(channel == null) {						
						Bootstrap strap = Client.getInstance().getBootstrap().handler(new ClientHandler(serverInfo.getServer_id()));	        	  
			            channel = strap.connect(serverInfo.getAddress(), serverInfo.getPort()).sync().channel();
			              
			            Client.getInstance().getClientChannelMap().put(serverInfo.getServer_id(), channel);		
					} else {
						if(channel.isActive() == false) {														
							channel.connect(channel.remoteAddress());
						} else {
							//커넥션 체크 허트비트 전송
							
							ServerInfo localServerInfo = ServerList.getInstance().getLocalServer();        
					        if(localServerInfo != null) {
						        Param packet = new Param();
						        packet.put("id", localServerInfo.getServer_id());
						        packet.put("authCode", localServerInfo.getAuth_code());
						        packet.put("mode", "1");
						        
								ChatServerHandler.sendData(channel, ProtocolType.ptRemoteHeartBeat
										, ProtocolType.stRemoteHeartBeatReq, packet);
					        }					        
						}
					}
				} else {
					Channel channel = Client.getInstance().getClientChannelMap().get(serverInfo.getServer_id());					
					if(channel != null) {
						if(channel.isActive()) {
							channel.close();
						}
					}
				}
			}
			
			//반대로 체크.
			ConcurrentHashMap<String, Channel> channelList = Client.getInstance().getClientChannelMap();						
			Enumeration<String> keys = channelList.keys();
			
			while(keys.hasMoreElements() ) {			
				String key = keys.nextElement();
				if( key != null && key.equals("") == false ) {
					ServerInfo si = serverList.get(key);
					if(si == null || si.getStatus() != 1) {
						Channel channe = channelList.get(key);
						channe.close();
						channelList.remove(key);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
