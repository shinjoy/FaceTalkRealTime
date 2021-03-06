package com.nomadsoft.chat.openServer;

public class ProtocolType {
	
	public static final int ptLogin = 0;
	public static final int ptHeartBeat = 1;
	public static final int ptChat = 2;
	public static final int ptNotice = 3;
	
	
	
	public static final int stLoginReq = 0;
	public static final int stLoginRes = 1;
	
	public static final int stLoginForceReq = 22;	
	public static final int stCloseForceRes = 23;
	
		
	public static final int stHeartBeatReq = 0;
	public static final int stHeartBeatRes = 1;
	
	public static final int stChatSaveMsgReq = 0; 
	public static final int stChatSaveMsgRes = 1;
	public static final int stChatLoginSaveMsgReq = 70;
	public static final int stChatLoginSaveMsgRes = 71;
	public static final int stChatSaveStatusReq = 2; 
	public static final int stChatSaveStatusRes = 3;	
	public static final int stChatCreateRoomReq = 4; 
	public static final int stChatCreateRoomRes = 5; 	
	public static final int stChatRoomInfoReq = 6;  
	public static final int stChatRoomInfoRes = 7;  	
	public static final int stChatRoomInviteReq = 8;  
	public static final int stChatRoomInviteRes = 9; 	
	public static final int stChatMemberDeleteReq = 10;  
	public static final int stChatMemberDeleteRes = 11;  	
		
	public static final int stChatMemberDropReq = 31;
	public static final int stChatMemberDropRes = 32;  	
	
	public static final int stChatReqVirtualMemberList = 40;
	public static final int stChatResVirtualMemberList = 41;  	
	
	
	
	public static final int stChatRemoveReq = 12;   
	public static final int stChatRemoveRes = 13;  	
	public static final int stChatMsgReq = 14; 
	public static final int stChatMsgRes = 15;	
	public static final int stChatStatusReq = 16;
	public static final int stChatStatusRes = 17;
	public static final int stChatReadReq = 20; 
	public static final int stChatReadRes = 21;

	public static final int stChatReqGoSteady = 22; 
	public static final int stChatResGoSteady = 23;

	public static final int stChatReqReplyGoSteady = 24; 
	public static final int stChatResReplyGoSteady = 25;
	
	public static final int stChatReqBreakGoSteady = 26; 
	public static final int stChatResBreakGoSteady = 27;
	
	public static final int stChatNoticeGoSteady = 28;
	
	public static final int stChatReqBan = 29;
	public static final int stChatReqUnban = 30;
	
	
	
	public static final int stReqGetMsg = 34;
	public static final int stResGetMsg = 35;
	
	public static final int stReqDeleteMsg = 36;
	public static final int stResDeleteMsg = 37;
	
	
	
	public static final int stNoticeReq = 1;
	public static final int stNoticeRes = 2;
	
	
	public static final int ptRemoteHeartBeat = 11;
	public static final int ptRemoteChat = 12;	
	public static final int ptRemoteLogin = 13;
	
	public static final int stRemoteHeartBeatReq = 0;
	public static final int stRemoteHeartBeatRes = 1;
	public static final int stRemoteChatMsgReq = 14; 
	public static final int stRemoteChatMsgRes = 15;
	public static final int stRemoteChatStatusReq = 16;
	public static final int stRemoteChatStatusRes = 17;
	public static final int stRemoteCloseForceRes = 24;
}
