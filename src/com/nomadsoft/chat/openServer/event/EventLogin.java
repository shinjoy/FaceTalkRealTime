package com.nomadsoft.chat.openServer.event;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.nomadsoft.chat.config.ServerList;
import com.nomadsoft.chat.db.dao.ServerDao;
import com.nomadsoft.chat.db.dao.UserDao;
import com.nomadsoft.chat.db.dto.Param;
import com.nomadsoft.chat.db.dto.ServerInfo;
import com.nomadsoft.chat.linkedNetwork.Client;
import com.nomadsoft.chat.log.Log;
import com.nomadsoft.chat.openServer.ChatEventHandler;
import com.nomadsoft.chat.openServer.ChatServerHandler;
import com.nomadsoft.chat.openServer.ChatUserContext;
import com.nomadsoft.chat.openServer.ProtocolType;

public class EventLogin {
	static void removeContext(String userID, ChatUserContext oldContext) {
		oldContext.setId("");
		oldContext.setAuthCode("");
		oldContext.setStatus(-1);
		ChatUserContext.getUserContextMap().remove(userID);
		
	}
	
	static void removePcContext(String userID, ChatUserContext oldContext) {
		oldContext.setId("");
		oldContext.setAuthCode("");
		oldContext.setStatus(-1);
		ChatUserContext.getPcUserContextMap().remove(userID);
		
	}
	
	static void removeRemoteContext(String userID, ChatUserContext oldContext) {
		oldContext.setId("");
		oldContext.setAuthCode("");
		oldContext.setStatus(-1);
		ChatUserContext.getRemoteContextMap().remove(userID);
		
	}
	
	public static void loginCheck(Channel channel, int pt, int st, byte[] data, ChatUserContext context) {
		try {
			JsonElement jo = ChatServerHandler.formatJsonData(data);
						
			JsonObject json = jo.getAsJsonObject();			
			JsonPrimitive jId = json.getAsJsonPrimitive("id");
			String userId = jId.getAsString();
			JsonPrimitive jAuthCode = json.getAsJsonPrimitive("authCode");
			String authCode = jAuthCode.getAsString();
			
			JsonPrimitive jMode = json.getAsJsonPrimitive("mode");
			String mode = "0";
			if(jMode != null) {
				mode = jMode.getAsString();	
			}
			
			int lResult = 0;
			//여기서 디비 체크.
			if(mode.equals("0")) {
				
				JsonPrimitive jDeviceId = json.getAsJsonPrimitive("di");
				String deviceId = jDeviceId.getAsString();
				
				//일반회원
				ChatUserContext oldContext = ChatUserContext.getUserContextMap().get(userId);			
				if(oldContext != null) {
										
					if(deviceId.equals(oldContext.getDeviceId()) == false) {
					
						try {			
							if(UserDao.loginCheckDevice(userId, authCode, deviceId)) {
								Param packet = new Param();
								packet.put("result", "true");
								ChatServerHandler.sendData(oldContext.getChannel(), ProtocolType.ptLogin
										, ProtocolType.stCloseForceRes, packet);
								
								oldContext.setStatus(0);
								oldContext.setId("");
							}
							
						} catch (Exception e) {
							// TODO: handle exception
						}
						
					}
					
					if(oldContext.getStatus() == 1 && oldContext.getId().equals(userId) 
							&& oldContext.getAuthCode().equals(authCode)
							&& oldContext.getDeviceId().equals(deviceId)
							) {
						
						context.setId(oldContext.getId());
						context.setAuthCode(oldContext.getAuthCode());
						context.setName(oldContext.getName());
						context.setPhone(oldContext.getPhone());
						context.setType(oldContext.getType());
						context.setMode(oldContext.getMode());
						context.setDeviceId(deviceId);
						
						removeContext(userId, oldContext);
						context.setStatus(1);
						ChatUserContext.getUserContextMap().put(userId, context);
						Log.i("context changed");
						
						ChatEventHandler.onReceived(channel, pt, st, data, jo, context);
						return;
					}
				}
				lResult = UserDao.loginCheck(userId, authCode, deviceId, context, mode);
				if(lResult == 1) {
					ChatUserContext befContext = ChatUserContext.getUserContextMap().get(userId);			
					if(befContext != null) {
						removeContext(userId, befContext);
					}
					context.setStatus(1);
					
					ChatUserContext.getUserContextMap().put(userId, context);
				}
			} else if(mode.equals("2")) {
				
				JsonPrimitive jDeviceId = json.getAsJsonPrimitive("di");
				String deviceId = jDeviceId.getAsString();
				
				//일반회원
				ChatUserContext oldContext = ChatUserContext.getPcUserContextMap().get(userId);			
				if(oldContext != null) {
										
					if(deviceId.equals(oldContext.getDeviceId()) == false) {
					
						try {			
							if(UserDao.windowsLoginCheckDevice(userId, authCode, deviceId)) {
								Param packet = new Param();
								packet.put("result", "true");
								ChatServerHandler.sendData(oldContext.getChannel(), ProtocolType.ptLogin
										, ProtocolType.stCloseForceRes, packet);
								
								oldContext.setStatus(0);
								oldContext.setId("");
							}
							
						} catch (Exception e) {
							// TODO: handle exception
						}
						
					}
					
					if(oldContext.getStatus() == 1 && oldContext.getId().equals(userId) 
							&& oldContext.getAuthCode().equals(authCode)
							&& oldContext.getDeviceId().equals(deviceId)
							) {
						
						context.setId(oldContext.getId());
						context.setAuthCode(oldContext.getAuthCode());
						context.setName(oldContext.getName());
						context.setPhone(oldContext.getPhone());
						context.setType(oldContext.getType());
						context.setMode(oldContext.getMode());
						context.setDeviceId(deviceId);
						
						removePcContext(userId, oldContext);
						context.setStatus(1);
						ChatUserContext.getPcUserContextMap().put(userId, context);
						Log.i("context changed");
						
						ChatEventHandler.onReceived(channel, pt, st, data, jo, context);
						return;
					}
				}
				lResult = UserDao.windowsLoginCheck(userId, authCode, deviceId, context, mode);
				if(lResult == 1) {
					ChatUserContext befContext = ChatUserContext.getPcUserContextMap().get(userId);			
					if(befContext != null) {
						removePcContext(userId, befContext);
					}
					context.setStatus(1);
					
					ChatUserContext.getPcUserContextMap().put(userId, context);
				}
				
			} else {
				//원격서버
				ChatUserContext oldContext = ChatUserContext.getRemoteContextMap().get(userId);			
				if(oldContext != null) {
					if(oldContext.getStatus() == 1 && userId.equals(userId) && authCode.equals(authCode)) {
						
						context.setId(oldContext.getId());
						context.setAuthCode(oldContext.getAuthCode());
						context.setName(oldContext.getName());
						context.setPhone(oldContext.getPhone());
						context.setType(oldContext.getType());
						context.setMode(oldContext.getMode());
						
						removeRemoteContext(userId, oldContext);
						context.setStatus(1);
						ChatUserContext.getRemoteContextMap().put(userId, context);

						ChatEventHandler.onReceived(channel, pt, st, data, jo, context);
						
						Log.i("context changed");
						return;
					}
				}
				
				lResult = ServerDao.loginCheck(userId, authCode, context, mode);
				if(lResult == 1) {
					ChatUserContext befContext = ChatUserContext.getRemoteContextMap().get(userId);			
					if(befContext != null) {
						removeRemoteContext(userId, befContext);
					}
					context.setStatus(1);
					
					ChatUserContext.getRemoteContextMap().put(userId, context);
				}
			}
			
			
			//로그인 성공한 이후 다시 핸들러 실행
			if(context.getStatus() == 1) {
				ChatEventHandler.onReceived(channel, pt, st, data, jo, context);
			} else {
				//로그인 실패 메시지 전송.				
				Param packet = new Param();
				packet.put("result", "false");	
				if(lResult == 2) {
					packet.put("code", "2");
				} else if(lResult == 3) {
					packet.put("code", "1");
				} else {
					packet.put("code", "0");	
				}
				
				ChatServerHandler.sendData(channel, ProtocolType.ptLogin
						, ProtocolType.stLoginRes, packet);
			}
			
			//Log.i(userId);
		} catch (Exception e) {
			// TODO: handle exception
	    	Log.i(e.getMessage());
		}
	}

	public static void onReceiveLogin(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			
			Param packet = new Param();
			packet.put("result", "true");
			ChatServerHandler.sendData(channel, ProtocolType.ptLogin
					, ProtocolType.stLoginRes, packet);
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	
	
	public static void onReceiveLoginForce(Channel clientChannel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			
			//컨텍스트중에서 다른 디바이스 아이디는 모두 삭제.
			String reqDeviceId = context.getDeviceId();
			String reqUserId = context.getId();
			
			//다른 서버에 Broadcast
			ConcurrentHashMap<String, ServerInfo> serverList = ServerList.getInstance().getRemoteServerList();    	  
			for(ServerInfo serverInfo : serverList.values()) {
				if(serverInfo.getStatus() == 1) {
					Channel channel = Client.getInstance().getClientChannelMap().get(serverInfo.getServer_id());					
					if(channel != null) {	
						if(channel.isActive() == true) {																												
							ServerInfo localServerInfo = ServerList.getInstance().getLocalServer();        
					        if(localServerInfo != null) {
						        Param packet = new Param();
						        packet.put("id", localServerInfo.getServer_id());
						        packet.put("authCode", localServerInfo.getAuth_code());
						        
						        packet.put("reqDeviceId", reqDeviceId);
						        packet.put("reqUserId", reqUserId);
						        
								ChatServerHandler.sendData(channel, ProtocolType.ptRemoteLogin
										, ProtocolType.stRemoteCloseForceRes, packet);
					        }					        
						}
					}
				}
			}
			
			Param packet = new Param();
			packet.put("result", "true");
			ChatServerHandler.sendData(clientChannel, ProtocolType.ptLogin
					, ProtocolType.stLoginRes, packet);
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
}
