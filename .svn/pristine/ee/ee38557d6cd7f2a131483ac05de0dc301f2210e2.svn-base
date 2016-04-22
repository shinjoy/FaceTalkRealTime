package com.nomadsoft.chat.openServer.event;

import io.netty.channel.Channel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nomadsoft.chat.db.dto.Param;
import com.nomadsoft.chat.log.Log;
import com.nomadsoft.chat.openServer.ChatServerHandler;
import com.nomadsoft.chat.openServer.ChatUserContext;
import com.nomadsoft.chat.openServer.ProtocolType;
import com.nomadsoft.util.push.PushSender;

public class EventRemote {

	public static void onReceiveHeartBeat(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			Param param = new Param();			
			ChatServerHandler.sendData(channel, ProtocolType.ptRemoteHeartBeat
					, ProtocolType.stRemoteHeartBeatRes, param);								
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	
	
	public static void onReceiveMsgStatus(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			if(jo == null) {
				jo = ChatServerHandler.formatJsonData(data);
			}
			
			JsonObject json = jo.getAsJsonObject();		
			String destUser = json.getAsJsonPrimitive("destUser").getAsString();
			String bufferKey = json.getAsJsonPrimitive("bufferKey").getAsString();
			
			//응답.
			Param param = new Param();	
			param.put("bufferKey", bufferKey);
			ChatServerHandler.sendData(channel, ProtocolType.ptRemoteChat
					, ProtocolType.stRemoteChatStatusRes, param);	
			//
			
			ChatUserContext member = ChatUserContext.getUserContextMap().get(destUser);	
			if(member != null) {	
				
				ChatServerHandler.sendRawData(member.getChannel(), ProtocolType.ptChat
						, ProtocolType.stChatStatusRes, json.toString());				
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	
	
	public static void onReceiveMsg(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			if(jo == null) {
				jo = ChatServerHandler.formatJsonData(data);
			}
			
			JsonObject json = jo.getAsJsonObject();		
			String destUser = json.getAsJsonPrimitive("destUser").getAsString();
			String bufferKey = json.getAsJsonPrimitive("bufferKey").getAsString();
			
			//응답.
			Param param = new Param();	
			param.put("bufferKey", bufferKey);
			ChatServerHandler.sendData(channel, ProtocolType.ptRemoteChat
					, ProtocolType.stRemoteChatMsgRes, param);	
			//
			
			ChatUserContext member = ChatUserContext.getUserContextMap().get(destUser);		
			
			if(member != null) {				
				ChatServerHandler.sendRawData(member.getChannel(), ProtocolType.ptChat
						, ProtocolType.stChatMsgRes, json.toString());				
			} else {
				//푸시 전송.
				String contentsType = json.getAsJsonPrimitive("contentsType").getAsString();
				String sendID = json.getAsJsonPrimitive("sendID").getAsString(); 
				String contents = json.getAsJsonPrimitive("contents").getAsString();
				String roomIdx = json.getAsJsonPrimitive("roomIdx").getAsString();
			
				if(contentsType.equals("3") == false && contentsType.equals("4") == false) {
					PushSender.sendPushMsg(sendID, context.getName()
						, destUser, contents, roomIdx, "0");
				}
				
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	
	

	public static void onReceiveForceClose(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			if(jo == null) {
				jo = ChatServerHandler.formatJsonData(data);
			}
			
			JsonObject json = jo.getAsJsonObject();		
			String reqDeviceId = json.getAsJsonPrimitive("reqDeviceId").getAsString();
			String reqUserId = json.getAsJsonPrimitive("reqUserId").getAsString();
			
			
			ChatUserContext member = ChatUserContext.getUserContextMap().get(reqUserId);		
			
			if(member != null) {		
				
				if(member.getDeviceId().equals(reqDeviceId) == false) {
					
					Param packet = new Param();
					packet.put("result", "true");
					ChatServerHandler.sendData(member.getChannel(), ProtocolType.ptLogin
							, ProtocolType.stCloseForceRes, packet);
					
					member.setStatus(0);
					member.setId("");					
				}								
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
}
