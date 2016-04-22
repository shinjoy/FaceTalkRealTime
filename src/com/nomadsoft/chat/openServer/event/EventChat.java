package com.nomadsoft.chat.openServer.event;

import io.netty.channel.Channel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nomadsoft.chat.db.dao.ChatDBhelper;
import com.nomadsoft.chat.db.dao.ChatDao;
import com.nomadsoft.chat.db.dao.ChatMsgDao;
import com.nomadsoft.chat.db.dao.DBConnectionPool;
import com.nomadsoft.chat.db.dao.UserDao;
import com.nomadsoft.chat.db.dto.Param;
import com.nomadsoft.chat.db.dto.VirtualMember;
import com.nomadsoft.chat.log.Log;
import com.nomadsoft.chat.openServer.ChatServerHandler;
import com.nomadsoft.chat.openServer.ChatUserContext;
import com.nomadsoft.chat.openServer.ProtocolType;
import com.nomadsoft.chat.utility.ChatDate;

public class EventChat {

	public static void sendMsg(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			if(jo == null) {
				jo = ChatServerHandler.formatJsonData(data);
			}
			
			JsonObject jObject = jo.getAsJsonObject();						
			String msgKey = jObject.getAsJsonPrimitive("msgKey").getAsString();
			String roomIdx = jObject.getAsJsonPrimitive("roomIdx").getAsString();					
			String msgType = jObject.getAsJsonPrimitive("mType").getAsString();
			String contentsType = jObject.getAsJsonPrimitive("cType").getAsString();
						
			String sendID = jObject.getAsJsonPrimitive("sendID").getAsString();
			String contents = jObject.getAsJsonPrimitive("contents").getAsString();
			String fileName = jObject.getAsJsonPrimitive("fileName").getAsString();
			String option1 = jObject.getAsJsonPrimitive("option1").getAsString();
			String option2 = "";
			
			Connection connection = DBConnectionPool.getConnection();
			try {

				String query = "select gender  " 
						+ "from t_nf_user " 
						+ "where user_id = ? ";
				PreparedStatement stmt = connection.prepareStatement(query);
				stmt.setString(1, sendID);
				
				//HashMap<String, String> usermap = new HashMap<String, String>();
				ResultSet rs = stmt.executeQuery();
				JsonObject jj = new JsonObject();
				if (rs.next()) {
					
					String gender = ChatDBhelper.getSafeString(Integer.toString(rs.getInt("gender")));
					/*String photo = ChatDBhelper.getSafeString(rs.getString("photo"));
					String photoStatus = "0";
					if(!photo.equals("")){//
						photoStatus="1";
					}*/
					jj.addProperty("gender", gender);
					//jj.addProperty("photoStatus", photoStatus);

					option2 =jj.toString();
				}	
				
				stmt.close();
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				connection.close();
			}
			
			
			
			String option3 = jObject.getAsJsonPrimitive("option3").getAsString();
			
	

			Param param = new Param();
			param.put("msgKey", msgKey);
			param.put("type", "1");			
			param.put("roomIdx", roomIdx);
			param.put("msgType", msgType);	
			
			Date date = new Date();
			String sDate = ChatDate.getFormatterServerDate().format(date);
			param.put("sDate", sDate);			
			ChatServerHandler.sendData(channel, ProtocolType.ptChat
					, ProtocolType.stChatStatusRes, param);		
						
			
			if(context.getMode().equals("2") && context.getId().equals(sendID) == false) {
				ChatUserContext vContext = null;
				for(ChatUserContext cuc : context.getVmList()) {
					if(cuc.getId().equals(sendID)) {
						vContext = cuc;
						break;
					}
				}
				
				if(vContext != null) {
					ChatMsgDao.sendMsg(vContext, true, sDate, msgKey, roomIdx, msgType
							, contentsType, sendID, contents, fileName, option1
							, option2, option3, date, "");		
				}
			} else {
				ChatMsgDao.sendMsg(context, true, sDate, msgKey, roomIdx, msgType
						, contentsType, sendID, contents, fileName, option1
						, option2, option3, date, "");												
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	
	public static void loadSaveMsg(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {		
			ChatMsgDao.loadSaveMsg(context, "");
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	
	public static void loadLoginSaveMsg(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			ChatMsgDao.loadSaveMsg(context, "login");
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	
	public static void loadSaveStatus(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {		
			ChatMsgDao.loadSaveStatus(context);
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	
	public static void onReceiveCreateRoom(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			
			if(jo == null) {
				jo = ChatServerHandler.formatJsonData(data);
			}

			
			JsonObject jObject = jo.getAsJsonObject();
			
			String reqKey = jObject.getAsJsonPrimitive("reqKey").getAsString();
			String memberList = jObject.getAsJsonPrimitive("memberList").getAsString();			
			int roomType = jObject.getAsJsonPrimitive("roomType").getAsInt();			
			String userId = context.getId();

			//PC 버전.
			if(context.getMode().equals("2")) {
				if (jObject.has("ownerId")) {
					String ownerId = jObject.getAsJsonPrimitive("ownerId").getAsString();	
					
					if(ownerId.equals("") == false) {
						//가상 ID 체크
						for(ChatUserContext cuc : context.getVmList()) {
							if(cuc.getId().equals(ownerId)) {
								//channel = cuc.getChannel();
								//context = cuc;
								userId = context.getId();
								break;
							}
						}	
					}	
				}
				
			}
						
			ChatDao.createChatRoom(channel, roomType,  reqKey, memberList, userId);
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	
	
	public static void onReceiveRoomInfo(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			if(jo == null) {
				jo = ChatServerHandler.formatJsonData(data);
			}
			
			JsonObject jObject = jo.getAsJsonObject();
			
			String reqKey = jObject.getAsJsonPrimitive("reqKey").getAsString();
			String roomIdx = jObject.getAsJsonPrimitive("roomIdx").getAsString();	
			String userId = context.getId();
			
		
			//PC 버전.
			if(context.getMode().equals("2")) {
				if (jObject.has("ownerId")) {
					String ownerId = jObject.getAsJsonPrimitive("ownerId").getAsString();	
					
					if(ownerId.equals("") == false) {
						//가상 ID 체크
						userId = ownerId;
						/*
						for(ChatUserContext cuc : context.getVmList()) {
							if(cuc.getId().equals(ownerId)) {
								//channel = cuc.getChannel();
								//context = cuc;
								userId = context.getId();
								break;
							}
						}
						*/	
					}	
				}
			}
		
			ChatDao.getRoomInfo(channel, reqKey, Long.valueOf(roomIdx), userId);
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	

	public static void onInviteMember(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			
			if(jo == null) {
				jo = ChatServerHandler.formatJsonData(data);
			}
			
			JsonObject jObject = jo.getAsJsonObject();
			
			String reqKey = jObject.getAsJsonPrimitive("reqKey").getAsString();
			String roomIdx = jObject.getAsJsonPrimitive("roomIdx").getAsString();	
			String inviteUser = jObject.getAsJsonPrimitive("inviteUser").getAsString();			

			ChatDao.inviteMember(channel, context, context.getId(), reqKey, Long.valueOf(roomIdx), inviteUser);
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	

	public static void onRemoveMember(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			if(jo == null) {
				jo = ChatServerHandler.formatJsonData(data);
			}
			
			JsonObject jObject = jo.getAsJsonObject();
			
			String reqKey = jObject.getAsJsonPrimitive("reqKey").getAsString();
			String roomIdx = jObject.getAsJsonPrimitive("roomIdx").getAsString();	
			
			String userId = context.getId();

			//PC 버전.
			if(context.getMode().equals("2")) {
				if (jObject.has("ownerId")) {
					String ownerId = jObject.getAsJsonPrimitive("ownerId").getAsString();	
					
					if(ownerId.equals("") == false) {
						//가상 ID 체크
						userId = ownerId;
						/*
						for(ChatUserContext cuc : context.getVmList()) {
							if(cuc.getId().equals(ownerId)) {
								//channel = cuc.getChannel();
								//context = cuc;
								userId = context.getId();
								break;
							}
						}
						*/	
					}	
				}
				
			}
			
			ChatDao.removeMember(channel, context, reqKey, Long.valueOf(roomIdx), userId, userId);
						
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	
	

	public static void onDropMember(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			if(jo == null) {
				jo = ChatServerHandler.formatJsonData(data);
			}
			
			JsonObject jObject = jo.getAsJsonObject();
			
			String reqKey = jObject.getAsJsonPrimitive("reqKey").getAsString();
			String roomIdx = jObject.getAsJsonPrimitive("roomIdx").getAsString();				
			String destUserid = jObject.getAsJsonPrimitive("destUserid").getAsString();
			
			
			ChatDao.dropMember(channel, context, reqKey,  Long.valueOf(roomIdx), context.getId(),context.getId(), destUserid);
									
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	
	public static void onReadMsg(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			if(jo == null) {
				jo = ChatServerHandler.formatJsonData(data);
			}
			JsonObject jObject = jo.getAsJsonObject();
			
			String msgKey = jObject.getAsJsonPrimitive("msgKey").getAsString();
			String roomIdx = jObject.getAsJsonPrimitive("roomIdx").getAsString();	
			String sendID = jObject.getAsJsonPrimitive("sendID").getAsString();
			String recvID = jObject.getAsJsonPrimitive("recvID").getAsString();
			int status = jObject.getAsJsonPrimitive("status").getAsInt();
			
			ChatDao.onRecvReadMsg(channel, msgKey, roomIdx, sendID, recvID, status);

			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	
	

	public static void onRecvStatus(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			if(jo == null) {
				jo = ChatServerHandler.formatJsonData(data);
			}
			
			JsonObject jObject = jo.getAsJsonObject();
			
			String msgKey = jObject.getAsJsonPrimitive("msgKey").getAsString();
			String roomIdx = jObject.getAsJsonPrimitive("roomIdx").getAsString();	
			String sendID = jObject.getAsJsonPrimitive("sendID").getAsString();
			String recvID = jObject.getAsJsonPrimitive("recvID").getAsString();
			int status = jObject.getAsJsonPrimitive("status").getAsInt();
			
			ChatDao.onRecvStatus(channel, msgKey, roomIdx, sendID, recvID, status);
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	
	
	public static void onReqDeleteMsg(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			if(jo == null) {
				jo = ChatServerHandler.formatJsonData(data);
			}
			JsonObject jObject = jo.getAsJsonObject();
					
			String msgkey = jObject.getAsJsonPrimitive("msgkey").getAsString();			
			String msgtype = jObject.getAsJsonPrimitive("msgtype").getAsString();
			
		
			//PC 버전.
			if(context.getMode().equals("2")) {
				
				List<ChatUserContext> list = new ArrayList<ChatUserContext>();
				
				
				List<ChatUserContext> vlist = context.getVmList(); 
				if(vlist != null) {
					list.addAll(vlist);
				}
				
				list.add(context);
				
				for(ChatUserContext vmember : list) {
					ChatDao.onReqDeleteMsg(channel, vmember, msgkey, msgtype);
				}
				
				/*
				if(jObject.has("ownerId")) {
					

			
					String ownerId = jObject.getAsJsonPrimitive("ownerId").getAsString();	
					
					//가상 ID 체크
					for(ChatUserContext cuc : context.getVmList()) {
						if(cuc.getId().equals(ownerId)) {
							channel = cuc.getChannel();
							context = cuc;
							break;
						}
					}
							
				}
					*/	
			} else {
				ChatDao.onReqDeleteMsg(channel, context, msgkey, msgtype);	
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
	
	

	public static void onRecvChatReqVirtualMemberList(Channel channel, byte[] data, JsonElement jo, ChatUserContext context) {
		try {
			/*
			if(jo == null) {
				jo = ChatServerHandler.formatJsonData(data);
			}
			JsonObject jObject = jo.getAsJsonObject();
				*/	
			
			List<VirtualMember> list = UserDao.getVirtualMemberList(context.getId());
			

			HashMap<String,Object> hash = new HashMap<String, Object>();
			hash.put("member", list);
			
			Gson gson = new Gson();			
			String resMsg = gson.toJson(hash);				
			
			ChatServerHandler.sendRawData(channel, ProtocolType.ptChat
					, ProtocolType.stChatResVirtualMemberList, resMsg);		
				
			//가상회원 로드.
			//ChatUserContext.getPcUserContextMap().clear();
			context.getVmList().clear();
			List<ChatUserContext> cList = new ArrayList<ChatUserContext>();
			List<VirtualMember> vmList = UserDao.getVirtualMemberList(context.getId());
			for(VirtualMember vm : vmList) {
				ChatUserContext vContext = new ChatUserContext(context.getChannel());
				
				String subUserId = vm.getUserId();
				vContext.setId(subUserId);
				vContext.setAuthCode("");
				vContext.setName(vm.getUserName());
				vContext.setPhone("");
				vContext.setType(vm.getUserType());
				vContext.setMode("2");
				vContext.setDeviceId(context.getDeviceId());
				cList.add(vContext);		
							
				ChatUserContext.getVcusercontextmap().put(subUserId, vContext);													   				        							
			}					
			context.setVmList(cList);	
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		}
	}
}
