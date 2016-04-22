package com.nomadsoft.chat.openServer.event;

import io.netty.channel.Channel;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.nomadsoft.chat.db.dto.Param;
import com.nomadsoft.chat.log.Log;
import com.nomadsoft.chat.openServer.ChatServerHandler;
import com.nomadsoft.chat.openServer.ChatUserContext;
import com.nomadsoft.chat.openServer.ProtocolType;

public class EventHeartBeat {

	public static void onReceiveHeartBeat(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			Param param = new Param();			
			ChatServerHandler.sendData(channel, ProtocolType.ptHeartBeat
					, ProtocolType.stHeartBeatRes, param);								
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
}
