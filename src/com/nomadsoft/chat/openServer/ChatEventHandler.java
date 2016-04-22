package com.nomadsoft.chat.openServer;

import io.netty.channel.Channel;

import com.google.gson.JsonElement;
import com.nomadsoft.chat.log.Log;
import com.nomadsoft.chat.openServer.event.EventChat;
import com.nomadsoft.chat.openServer.event.EventHeartBeat;
import com.nomadsoft.chat.openServer.event.EventLogin;
import com.nomadsoft.chat.openServer.event.EventRemote;

public class ChatEventHandler {
    


	public static void onReceived(Channel channel, int pt, int st, byte[] data, JsonElement jo, ChatUserContext context) {
		try {			
			if(context.getStatus() == 0) {
				EventLogin.loginCheck(channel, pt, st, data, context);
			} else if(context.getStatus() == 1) {								
				if(pt == ProtocolType.ptHeartBeat) {
					EventHeartBeat.onReceiveHeartBeat(channel, data, jo, context);
				} else if(pt == ProtocolType.ptLogin) {					
					if(st == ProtocolType.stLoginReq) {
						EventLogin.onReceiveLogin(channel, data, jo, context);	
					} else if(st == ProtocolType.stLoginForceReq) {
						EventLogin.onReceiveLoginForce(channel, data, jo, context);
					}					
				} else if(pt == ProtocolType.ptChat) {								
					if(st == ProtocolType.stChatMsgReq) {
						EventChat.sendMsg(channel, data, jo, context);
					} else if(st == ProtocolType.stChatSaveMsgReq) {
						EventChat.loadSaveMsg(channel, data, jo, context);
					} else if(st == ProtocolType.stChatSaveStatusReq) {
						EventChat.loadSaveStatus(channel, data, jo, context);
					} else if(st == ProtocolType.stChatCreateRoomReq) {
						EventChat.onReceiveCreateRoom(channel, data, jo, context);
					} else if(st == ProtocolType.stChatRoomInfoReq) {
						EventChat.onReceiveRoomInfo(channel, data, jo, context);
					} else if(st == ProtocolType.stChatRoomInviteReq) {
						EventChat.onInviteMember(channel, data, jo, context);
					} else if(st == ProtocolType.stChatMemberDeleteReq) {
						EventChat.onRemoveMember(channel, data, jo, context);		
					} else if(st == ProtocolType.stChatMemberDropReq) {
						EventChat.onDropMember(channel, data, jo, context);								
					} else if(st == ProtocolType.stChatStatusReq) {
						EventChat.onRecvStatus(channel, data, jo, context);				
					} else if(st == ProtocolType.stChatReadReq) {
						EventChat.onReadMsg(channel, data, jo, context);		
					} else if(st == ProtocolType.stReqDeleteMsg) {
						EventChat.onReqDeleteMsg(channel, data, jo, context);
					} else if(st == ProtocolType.stChatReqVirtualMemberList) {
						EventChat.onRecvChatReqVirtualMemberList(channel, data, jo, context);
					} else if(st == ProtocolType.stChatLoginSaveMsgReq) {
						EventChat.loadLoginSaveMsg(channel, data, jo, context);
					}
								
				} else if(pt == ProtocolType.ptRemoteHeartBeat) {
					EventRemote.onReceiveHeartBeat(channel, data, jo, context);
				} else if(pt == ProtocolType.ptRemoteChat) {				
					if(st == ProtocolType.stRemoteChatStatusRes) {
						EventRemote.onReceiveMsgStatus(channel, data, jo, context);
					} else if(st == ProtocolType.stRemoteChatMsgReq) {
						EventRemote.onReceiveMsg(channel, data, jo, context);
					}				
				} else if(pt == ProtocolType.ptRemoteLogin) {	
					
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
}
