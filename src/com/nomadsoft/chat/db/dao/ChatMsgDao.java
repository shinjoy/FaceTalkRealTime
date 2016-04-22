package com.nomadsoft.chat.db.dao;

import io.netty.channel.Channel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.nomadsoft.chat.config.Config;
import com.nomadsoft.chat.config.Constants;
import com.nomadsoft.chat.db.dto.MemberItem;
import com.nomadsoft.chat.db.dto.Param;
import com.nomadsoft.chat.db.dto.VirtualMember;
import com.nomadsoft.chat.linkedNetwork.Client;
import com.nomadsoft.chat.linkedNetwork.ClientPacketBufferThread;
import com.nomadsoft.chat.log.Log;
import com.nomadsoft.chat.openServer.ChatServerHandler;
import com.nomadsoft.chat.openServer.ChatUserContext;
import com.nomadsoft.chat.openServer.ProtocolType;
import com.nomadsoft.redis.RedisClient;
import com.nomadsoft.util.push.PushNotification;

public class ChatMsgDao {
	
	public static void sendMsg(ChatUserContext context, boolean sendAll, String serverDate
			, String msgKey, String roomIdx, String msgType, String contentsType
			, String sendID, String contents, String fileName, String option1
			, String option2, String option3, Date sDate, String target) {
		
		

		Connection connection = DBConnectionPool.getConnection();		
		Jedis jedis = RedisClient.getInstance().borrow();
		
		try {
			//jedis.hset(key, field, value)
			
			String query = "select reg_date from t_nf_chat_msg where chat_msg_key = ?";
			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setString(1, msgKey);
			
			ResultSet rs = stmt.executeQuery();			
			if(rs.next() == false) {
				stmt.close();
				
				query = "select chat_room_seq, chat_room_type from t_nf_chat_room where chat_room_seq = ?";
				stmt = connection.prepareStatement(query);
				stmt.setString(1, roomIdx);
				rs = stmt.executeQuery();			
				int roomType = 0;
				
				if(rs.next()) {
					roomType = rs.getInt("chat_room_type");
				}
				stmt.close();
				
				query = "insert into t_nf_chat_msg(chat_msg_key, chat_room_seq, snd_id, m_type, c_type, contents, file_name, status, option1, option2, option3, reg_date) "
			      + "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				stmt = connection.prepareStatement(query);
				
				stmt.setString(1, msgKey);
				stmt.setString(2, roomIdx);
				stmt.setString(3, sendID);
				stmt.setString(4, msgType);
				stmt.setString(5, contentsType);
				stmt.setString(6, contents);//
				stmt.setString(7, fileName); //
				stmt.setInt(8, 1);
				stmt.setString(9, option1);
				stmt.setString(10, option2);
				stmt.setString(11, option3);
				
				Timestamp time = new Timestamp(sDate.getTime());
				stmt.setTimestamp(12, time);
				
				stmt.executeUpdate();
				stmt.close(); 
				
				Param param = new Param();
				param.put("cmd", "recvmsg");
				param.put("msgKey", msgKey);
				param.put("roomIdx", roomIdx);
				param.put("msgType", msgType);
				param.put("contentsType", contentsType);
				param.put("sendID", sendID);
				param.put("contents", contents);
				param.put("fileName", fileName);
				param.put("option1", option1);
				param.put("option2", option2);
				param.put("option3", option3);
				
				String sendName = "";
				if(com.nomadsoft.chat.config.Constants.adminId.equals(sendID)) {
					param.put("sendName", "관리자");
					param.put("senderType", "1");
					param.put("senderPhone", "");
					param.put("senderGroup", "");
					sendName = "관리자";
				} else {
					if(roomType == 3) {
						param.put("sendName", "익명");	
						sendName = "익명";
					} else {
						param.put("sendName", context.getName());
						sendName = context.getName();
					}
					
					param.put("senderType", String.valueOf(context.getType()));
					param.put("senderPhone", context.getPhone());
					param.put("senderGroup", context.getGroupID());
					
				}
				
				param.put("sDate", serverDate);	
				

				
				
				if(target.equals("") == false) {
					//query = "select A.user_id, B.server_id from t_nf_chat_member A left join  t_nf_user B on A.user_id = B.user_id where A.chat_room_seq = ? and A.user_id = ? ";

							
					query = "select A.user_id, C.server_id, C.os_type, C.pushkey, C.pushtype, C.login_status_windows, A.notification, A.enc_user_name, C.user_type, c.login_status,C.site  from t_nf_chat_member A "
							+ " left join t_nf_user C on A.user_id = C.user_id where A.chat_room_seq = ? and A.user_id = ? ";

					stmt = connection.prepareStatement(query);
					stmt.setString(1, roomIdx);
					stmt.setString(2, target);
					
				} else {

					query = "select A.user_id, C.server_id, C.os_type, C.pushkey, C.pushtype, C.login_status_windows, A.notification, A.enc_user_name, C.user_type, c.login_status,C.site  from t_nf_chat_member A "							
							+ " left join t_nf_user C on A.user_id = C.user_id where A.chat_room_seq = ? ";

					stmt = connection.prepareStatement(query);
					stmt.setString(1, roomIdx);
				}
				
				boolean addOwn = false;
				
				rs = stmt.executeQuery();	
				LinkedList<MemberItem> memberList = new LinkedList<MemberItem>();
			
				while(rs.next()) {
					String memberID = rs.getString(1);
					String serverID = ChatDBhelper.getSafeString(rs.getString(2));
					
					String os = ChatDBhelper.getSafeString(rs.getString(3));	
					String pushKey = ChatDBhelper.getSafeString(rs.getString(4));	
					String pushType = ChatDBhelper.getSafeString(rs.getString(5));	
					int pushTypeValue = 0;
					if ("".equals(pushType) == false) {
						pushTypeValue = Integer.valueOf(pushType);
					}
					int status_windows = ChatDBhelper.getSafeInt(rs.getString(6));
					int notification  = ChatDBhelper.getSafeInt(rs.getString(7));
					String encName = ChatDBhelper.getSafeString(rs.getString(8));
					String userType = ChatDBhelper.getSafeString(rs.getString(9));
					int loginStatus = ChatDBhelper.getSafeInt(rs.getString(10));
					String site =ChatDBhelper.getSafeString(rs.getString(11));
					
					
					boolean sendPc = false;
					if("5".equals(userType) || "3".equals(userType)) {
						
						if(loginStatus == 1) {
							sendPc = false;
						} else {
							sendPc = true;
						}
					}
					
					if(sendPc) {
						String pf = RedisClient.getMsgKeyPrefix();
						String jKey = pf + "chatMP:"  + msgKey;										
						jedis.hset(jKey, memberID, "1");
						jedis.expire(jKey, 5356800); //62일					
					} else {
						String pf = RedisClient.getMsgKeyPrefix();
						String jKey = "";//memberID + ":msg";					
						jKey = pf + "chatM:"  + msgKey;										
						jedis.hset(jKey, memberID, "1");
						jedis.expire(jKey, 5356800); //62일						
					}
					
					/*
					////////
					String pf = RedisClient.getMsgKeyPrefix();
					String jKey = "";//memberID + ":msg";					
					jKey = pf + "chatM:"  + msgKey;										
					jedis.hset(jKey, memberID, "1");
					jedis.expire(jKey, 5356800); //62일
					///////
					
					if(status_windows == 1) {
						jKey = pf + "chatMP:"  + msgKey;										
						jedis.hset(jKey, memberID, "1");
						jedis.expire(jKey, 5356800); //62일
					}
					*/
					
					if((memberID.equals(sendID) == false)  || 
							((sendAll == true)
									&& (contentsType.equals("3") || contentsType.equals("4") ) ) ||
							(contentsType.equals("10"))) {
				
						
						MemberItem memberItem = new MemberItem();
						
						memberItem.setMemberID(memberID);
						memberItem.setServerID(serverID);
						memberItem.setOs(os);
						memberItem.setPushKey(pushKey);
						memberItem.setPushType(pushTypeValue);
						memberItem.setStatusWindows(status_windows);
						memberItem.setNotification(notification);
						memberItem.setLoginStatus(loginStatus);
						memberItem.setUserType(userType);
						memberItem.setSite(site);
						memberList.add(memberItem);
						
						
						if(memberID.equals(sendID) == true) {
							addOwn = true;	
						}
						
					}
					
					if(memberID.equals(sendID)) {
						if(roomType == 3) {
							sendName = encName;
							param.put("sendName", encName);	
						}
					}
				}
				
				
				String jKey, jKey2;
				//String jKey = "chatMsgdata:"  + msgKey;
				//jedis.set(jKey, resMsg);
				///////
				int mCnt = memberList.size();
				
				jKey = "chatDCnt:"  + msgKey;
				jedis.set(jKey, String.valueOf(mCnt));
				jedis.expire(jKey, 5356800); //62일
				
				jKey = "chatRCnt:"  + msgKey;
				jedis.set(jKey, String.valueOf(mCnt));
				jedis.expire(jKey, 5356800); //62일
				
				param.put("rCount", String.valueOf(mCnt));	
				param.put("dCount", String.valueOf(mCnt));
				////////////////////
				
				Gson gson = new Gson();
				//String resMsg = gson.toJson(param);
				
				
				for(MemberItem item: memberList) {
	
					param.put("recvId", item.getMemberID());					
					String sendData = gson.toJson(param);
					item.setSendData(sendData);
					
					boolean sendPc = false;
					if("5".equals(item.getUserType()) || "3".equals(item.getUserType())) {
						
						if(item.getLoginStatus() == 1) {
							sendPc = false;
						} else {
							sendPc = true;
						}
					}
					String pf = RedisClient.getMsgKeyPrefix();
					if(sendPc) {

						jKey = pf + item.getMemberID() + ":pmsg";
						jKey2 = pf + item.getMemberID() + "/pmsg";
						jedis.hset(jKey, "rcv_msg:" + msgKey, sendData);
						jedis.rpush(jKey2, "rcv_msg:" + msgKey);
						
						jedis.expire(jKey, 5356800); //62일
						jedis.expire(jKey2, 5356800); //62일
					} else {
						jKey = pf + item.getMemberID() + ":msg";
						jKey2 = pf + item.getMemberID() + "/msg";
						jedis.hset(jKey, "rcv_msg:" + msgKey, sendData);
						jedis.rpush(jKey2, "rcv_msg:" + msgKey);
						
						jedis.expire(jKey, 5356800); //62일
						jedis.expire(jKey2, 5356800); //62일
						
					}
					
			
					jKey = pf + "chatD:"  + msgKey;										
					jedis.hset(jKey, item.getMemberID(), "1");
					jedis.expire(jKey, 5356800); //62일
					
					jKey = pf + "chatR:"  + msgKey;										
					jedis.hset(jKey, item.getMemberID(), "1");
					jedis.expire(jKey, 5356800); //62일			
					
					
				}
				
				if (context != null && addOwn == false) {
					param.put("recvId", sendID);
					String sendData = gson.toJson(param);
					
					if(context.getMode().equals("2")) {
						
						String pf = RedisClient.getMsgKeyPrefix();
						jKey = pf + context.getId() + ":msg";	
						jKey2 = pf + context.getId() + "/msg";
						jedis.hset(jKey, "rcv_msg:" + msgKey, sendData);
						jedis.rpush(jKey2, "rcv_msg:" + msgKey);
						
						jedis.expire(jKey, 5356800); //62일							
						jedis.expire(jKey2, 5356800); //62일
						
						ChatUserContext member = ChatUserContext.getUserContextMap().get(context.getId());
						if(member != null) {							
							
							ChatServerHandler.sendRawData(member.getChannel(), ProtocolType.ptChat
									, ProtocolType.stReqGetMsg, "{}");								
						} else {
							
							
						}
					} else {
					
						
						String pf = RedisClient.getMsgKeyPrefix();
						jKey = pf + context.getId() + ":pmsg";							
						jKey2 = pf + context.getId() + "/pmsg";
						jedis.hset(jKey, "rcv_msg:" + msgKey, sendData);
						jedis.rpush(jKey2, "rcv_msg:" + msgKey);
						
						jedis.expire(jKey, 5356800); //62일							
						jedis.expire(jKey2, 5356800); //62일
						
						ChatUserContext pcMember = ChatUserContext.getPcUserContextMap().get(context.getId());
						if(pcMember != null) {
							ChatServerHandler.sendRawData(pcMember.getChannel(), ProtocolType.ptChat
									, ProtocolType.stReqGetMsg, "{}");									
						} else {
						
							
						}
					}
				}
				
				
				
				for(MemberItem item: memberList) {


					ChatUserContext member = ChatUserContext.getUserContextMap().get(item.getMemberID());					
					ChatUserContext pcMember = ChatUserContext.getPcUserContextMap().get(item.getMemberID());
					
					
					if(pcMember != null) {
						
						int noti = item.getNotification();
						ChatServerHandler.sendRawData(pcMember.getChannel(), ProtocolType.ptChat
								, ProtocolType.stReqGetMsg, "{\"noti\":" + noti + "}");									
					} else {
						pcMember = ChatUserContext.getVcusercontextmap().get(item.getMemberID());
						if(pcMember != null) {
							int noti = item.getNotification();
							ChatServerHandler.sendRawData(pcMember.getChannel(), ProtocolType.ptChat
									, ProtocolType.stReqGetMsg, "{\"noti\":" + noti + "}");							
						}
					}
					
					if(member != null) {	
						int noti = item.getNotification();
						
						ChatServerHandler.sendRawData(member.getChannel(), ProtocolType.ptChat
								, ProtocolType.stReqGetMsg, "{\"noti\":" + noti + "}");	
						
					} else {
						
						boolean needPush = false;
						if(item.getServerID().equals("")) {
							needPush = true;
						}
						if(needPush == false ) {
							Channel client = Client.getInstance().getClientChannelMap().get(item.getServerID());							
							if(client != null) {									
								param.put("fromServer", Config.getInstance().getServerId());
								param.put("destUser", item.getMemberID());
								long bufferKey = ClientPacketBufferThread.getBufferKey();
								param.put("bufferKey", String.valueOf(bufferKey));
								Gson gson2 = new Gson();
								String resMsg2 = gson2.toJson(param);
								
								ClientPacketBufferThread.getInstance().add(resMsg2, ProtocolType.ptChat
										, ProtocolType.stChatMsgRes, item.getMemberID(), bufferKey);

								ChatServerHandler.sendRawData(client, ProtocolType.ptRemoteChat
										, ProtocolType.stRemoteChatMsgReq, item.getSendData());
							} else {
								
								if(item.getNotification() == 0) {
									needPush = false;
								} else {
									needPush = true;
								}
							}
						} else {
							if(item.getNotification() == 0) {
								needPush = false;
							}
						}
						
						if(needPush) {
							
							if(contentsType.equals("3") == false && contentsType.equals("4") == false) {
								
								if("".equals(item.getPushKey()) == false) {	
								
									if("ios".equals(item.getOs())) {
										String pushMsg = sendName + " : " +  contents;
										if(pushMsg.length() > 50) {
											pushMsg = pushMsg.substring(0, 50);
										}


										PushNotification.sendPush(false, item.getMemberID()
											, pushMsg
											, item.getPushKey(), "0"
											, roomIdx
											, item.getPushType()
											, item.getOs(), sendID, sendName,0, item.getSite(),  -1);
									} else {
										String pushMsg = sendName + " : " + contents;
										if(pushMsg.length() > 50) {
											pushMsg = pushMsg.substring(0, 50);
										}
									    
										PushNotification.sendPush(false, item.getMemberID()
											, pushMsg
											, item.getPushKey(), "0"
											, roomIdx
											, item.getPushType()
											, item.getOs(),  sendID, sendName,0,item.getSite(),  -1);
									}
									
								}								
							}
						}							
					}
				}
				
				memberList.clear();							
			} else {
				stmt.close();
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
				
				RedisClient.getInstance().revert(jedis);
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		
		//Log.i(contents);
	}	
	
	public static void loadSaveMsg(ChatUserContext context, String loadType) {
		
		Jedis jedis = RedisClient.getInstance().borrow();				
		try {	
			if (context.getMode().equals("2")) {
				int cnt = 0;
				
				//PC가 로그인 되어 있는지 체크.(REDIS)


				
				List<ChatUserContext> list = new ArrayList<ChatUserContext>();
				
				List<ChatUserContext> vlist = context.getVmList(); 
				if(vlist != null) {
					list.addAll(vlist);
				}
				list.add(context);
				
				String userId = context.getId();
				for(ChatUserContext vmember : list) {
					//이전달.
					userId = vmember.getId();
					
					String pf = RedisClient.getBeforeMsgKeyPrefix();
					String jKey = pf + userId + ":pmsg";
					String jKey2 = pf + userId + "/pmsg";
					List<String> keys = jedis.lrange(jKey2, 0, 2000);
					
					for(String key : keys) {
						String data = jedis.hget(jKey, key);
						if(data == null || data.equals("")) {						
							jedis.lrem(jKey2, 0,key);
						} else {	
							boolean sendResult = false;
							if(loadType.equals("")) {
								sendResult = ChatServerHandler.sendRawData(context.getChannel(), ProtocolType.ptChat
										, ProtocolType.stChatSaveMsgRes, data);								
							} else {
								sendResult = ChatServerHandler.sendRawData(context.getChannel(), ProtocolType.ptChat
										, ProtocolType.stChatLoginSaveMsgRes, data);
							}
							if(sendResult) {
								jedis.hdel(jKey, key);
								jedis.lrem(jKey2, 0, key);	
							}						
						}
					}
					
					
					
					//이번달.
					pf = RedisClient.getMsgKeyPrefix();
					jKey = pf + userId + ":pmsg";
					jKey2 = pf + userId + "/pmsg";
					keys = jedis.lrange(jKey2, 0, 2000);
							
					
					List<String> reArrkeysA = new ArrayList<String>();
					List<String> reArrkeysB = new ArrayList<String>();
					List<String> reArrkeysC = new ArrayList<String>();
					
					for(String key : keys) {						
						if(key.startsWith("rcv_msg:")) {
							reArrkeysA.add(key);
						} else if(key.startsWith("rcv_msg_d:")) {
							reArrkeysB.add(key);
						} else {
							reArrkeysC.add(key);
						}
					}
					
					keys.clear();
					
					keys.addAll(reArrkeysA);
					keys.addAll(reArrkeysB);
					keys.addAll(reArrkeysC);
					
					
					//바로 삭제 하기.
					String befData = "";
					for(String key : keys) {
						
						String data = jedis.hget(jKey, key);
						if(data == null || data.equals("")) {						
							jedis.lrem(jKey2, 0,key);
						} else {	
							if(befData.equals(data) == false) {
								befData = data;
							} else {
								continue;
							}
							if(key.startsWith("rcv_msg_r:")) {
								String msgkey = key.substring(10, key.length());
								
								jedis.hset(jKey, "rcv_msg_r2:" + msgkey, data);
							}

							
							
							
							boolean sendResult = false;
							if(loadType.equals("")) {
								sendResult = ChatServerHandler.sendRawData(context.getChannel(), ProtocolType.ptChat
										, ProtocolType.stChatSaveMsgRes, data);								
							} else {
								sendResult = ChatServerHandler.sendRawData(context.getChannel(), ProtocolType.ptChat
										, ProtocolType.stChatLoginSaveMsgRes, data);
							}
							if(sendResult) {
								jedis.hdel(jKey, key);
								jedis.lrem(jKey2, 0, key);	
							}						
						}
					}
					
					
					
					/*
					for(String key : keys) {
						
						String data = jedis.hget(jKey, key);
						if(data == null || data.equals("")) {						
							jedis.lrem(jKey2, 0,key);
						} else {
							if(loadType.equals("")) {
								ChatServerHandler.sendRawData(context.getChannel(), ProtocolType.ptChat
										, ProtocolType.stChatSaveMsgRes, data);								
							} else {
								ChatServerHandler.sendRawData(context.getChannel(), ProtocolType.ptChat
										, ProtocolType.stChatLoginSaveMsgRes, data);
							}
						}
					}	
					*/	
				}
			} else {
				
				String userId = context.getId();	
				
				String pf = RedisClient.getBeforeMsgKeyPrefix();							
				String jKey = pf + userId + ":msg";
				String jKey2 = pf + userId + "/msg";
				
				List<String> keys = jedis.lrange(jKey2, 0, 2000);		
				
				List<String> reArrkeysA = new ArrayList<String>();
				List<String> reArrkeysB = new ArrayList<String>();
				List<String> reArrkeysC = new ArrayList<String>();
				
				for(String key : keys) {						
					if(key.startsWith("rcv_msg:")) {
						reArrkeysA.add(key);
					} else if(key.startsWith("rcv_msg_d:")) {
						reArrkeysB.add(key);
					} else {
						reArrkeysC.add(key);
					}
				}
				
				keys.clear();
				
				keys.addAll(reArrkeysA);
				keys.addAll(reArrkeysB);
				keys.addAll(reArrkeysC);
				
				
				for(String key : keys) {
					String data = jedis.hget(jKey, key);
					if(data == null || data.equals("")) {						
						jedis.lrem(jKey2, 0,key);
					} else {	
						boolean sendResult = false;
						if(loadType.equals("")) {
							sendResult = ChatServerHandler.sendRawData(context.getChannel(), ProtocolType.ptChat
									, ProtocolType.stChatSaveMsgRes, data);								
						} else {
							sendResult = ChatServerHandler.sendRawData(context.getChannel(), ProtocolType.ptChat
									, ProtocolType.stChatLoginSaveMsgRes, data);
						}
						if(sendResult) {
							jedis.hdel(jKey, key);
							jedis.lrem(jKey2, 0, key);	
						}						
					}
				}
	


				//이번달
				String befData = "";
				pf = RedisClient.getMsgKeyPrefix();							
				jKey = pf + userId + ":msg";
				jKey2 = pf + userId + "/msg";
				
				keys = jedis.lrange(jKey2, 0, 2000);
				for(String key : keys) {
					String data = jedis.hget(jKey, key);
					if(data == null || data.equals("")) {						
						jedis.lrem(jKey2, 0,key);
					} else {	
						
						if(befData.equals(data) == false) {
							befData = data;
						} else {
							continue;
						}
						
						if(key.startsWith("rcv_msg_r:")) {
							String msgkey = key.substring(10, key.length());
							
							jedis.hset(jKey, "rcv_msg_r2:" + msgkey, data);
						}
						
						
						boolean sendResult = false;
						if(loadType.equals("")) {
							sendResult = ChatServerHandler.sendRawData(context.getChannel(), ProtocolType.ptChat
									, ProtocolType.stChatSaveMsgRes, data);								
						} else {
							sendResult = ChatServerHandler.sendRawData(context.getChannel(), ProtocolType.ptChat
									, ProtocolType.stChatLoginSaveMsgRes, data);
						}
						if(sendResult) {
							jedis.hdel(jKey, key);
							jedis.lrem(jKey2, 0, key);	
						}						
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				RedisClient.getInstance().revert(jedis);
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}	
	
	

	public static void loadSaveStatus(ChatUserContext context) {
		
	}	
	

	public static String getChatDate(String msgKey) {
		
		String result = "";
		
		Connection connection = DBConnectionPool.getConnection();		
		try {
			String query = "SELECT reg_date FROM t_nf_chat_msg where chat_msg_key = ? ";

			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setString(1, msgKey);
			
			ResultSet rs = stmt.executeQuery();
						
			if(rs.next()) {		
				result = rs.getString(1);			
			}			
			stmt.close(); 
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		
		return result;
	}
}
