package com.nomadsoft.chat.linkedNetwork;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.nomadsoft.chat.log.Log;
import com.nomadsoft.chat.openServer.ProtocolType;
import com.nomadsoft.chat.openServer.event.EventLogin;
import com.nomadsoft.chat.openServer.event.EventChat;
import com.nomadsoft.chat.openServer.event.EventHeartBeat;

public class ClientEventHandler {
    
	public static void onReceived(Channel channel, int pt, int st, JsonElement jo, String serverId) {
		try {			
			if(pt == ProtocolType.ptRemoteHeartBeat) {
				//¿¿¥‰				
				//System.currentTimeMillis();				
			} else if(pt == ProtocolType.ptRemoteHeartBeat) {
				if(st == ProtocolType.stRemoteChatStatusRes) {
					JsonObject js = jo.getAsJsonObject();
					String bufferKey = js.getAsJsonPrimitive("bufferKey").getAsString();
					ClientPacketBufferThread.getInstance().remove(Long.valueOf(bufferKey));
										
				} else if(st == ProtocolType.stRemoteChatMsgRes) {
					JsonObject js = jo.getAsJsonObject();
					String bufferKey = js.getAsJsonPrimitive("bufferKey").getAsString();
					ClientPacketBufferThread.getInstance().remove(Long.valueOf(bufferKey));
					
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
}
