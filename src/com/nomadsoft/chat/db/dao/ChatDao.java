package com.nomadsoft.chat.db.dao;

import io.netty.channel.Channel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nomadsoft.chat.config.Config;
import com.nomadsoft.chat.config.Constants;
import com.nomadsoft.chat.db.dto.ChatReq;
import com.nomadsoft.chat.db.dto.ChatRoom;
import com.nomadsoft.chat.db.dto.Packet;
import com.nomadsoft.chat.db.dto.Param;
import com.nomadsoft.chat.db.dto.Push;
import com.nomadsoft.chat.linkedNetwork.Client;
import com.nomadsoft.chat.linkedNetwork.ClientPacketBufferThread;
import com.nomadsoft.chat.log.Log;
import com.nomadsoft.chat.openServer.ChatServerHandler;
import com.nomadsoft.chat.openServer.ChatUserContext;
import com.nomadsoft.chat.openServer.ProtocolType;
import com.nomadsoft.chat.utility.ChatDate;
import com.nomadsoft.chat.utility.MsgKey;
import com.nomadsoft.redis.RedisClient;
import com.nomadsoft.util.push.PushKey;
import com.nomadsoft.util.push.PushType;

public class ChatDao {	
	public static void createChatRoom(Channel channel, int roomType, String reqKey, String memberList, String userId ) {
		
		Connection connection = DBConnectionPool.getConnection();				
		try {
			ArrayList<String> mList = new ArrayList<String>();
			String[] list = memberList.split("[,]");			
			for(String member : list) {				
				if(mList.contains(member.trim()) == false) {
					mList.add(member.trim());
				}
			}
			
	        String roomIdx = "";	        
	        String title = "";
	        String createrID = "";
	        
			if(mList.size() > 1) {
				
				if(roomType == 0 && mList.size() == 2) {					

					String query = "select top 1 2 as CNT_ROOM, A.chat_room_seq from t_nf_chat_room A inner join t_nf_chat_member B on A.chat_room_seq = B.chat_room_seq"
							+ " where chat_room_type = 0 and ((B.user_id =  '" + mList.get(0) +"' and A.owner_id = '" + mList.get(1)  + "') or (B.user_id = '" + mList.get(1)  + "' and A.owner_id = '" + mList.get(0)  + "'))"
							+ " order by A.reg_date desc ";

					Statement stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery(query);

					
					
					if(rs.next()) {
						int CNT_ROOM = rs.getInt(1);
						if(CNT_ROOM == 2) {
							long eIdx = rs.getLong(2);	
							query = "select * from t_nf_chat_room where chat_room_seq = " + eIdx;
							
							
							Statement statement = connection.createStatement();
							ResultSet rsRoom = statement.executeQuery(query);
							
							if(rsRoom.next()) {
								roomIdx = String.valueOf(eIdx);
								roomType = rsRoom.getInt("chat_room_type");
								createrID = rsRoom.getString("owner_id");
								title = rsRoom.getString("title"); 
								
						
							}
							statement.close();
						}
					}
					stmt.close(); 
				}
				

				if(roomIdx.equals("")) {
					String query = "insert into t_nf_chat_room (owner_id, chat_room_type, title, reg_date) values(?, ?, ?,getdate())";
					//SQLServerStatement.RETURN_GENERATED_KEYS
					PreparedStatement stmt = connection.prepareStatement(query, 1);
	
					createrID = userId;
					stmt.setString(1, createrID);
					stmt.setInt(2, roomType);
					stmt.setString(3, title);				
					
					stmt.executeUpdate();
			
					ResultSet rs = stmt.getGeneratedKeys();
					if (rs.next()){
						long newIdx = rs.getLong(1);
						roomIdx = String.valueOf(newIdx);
						String insertQuery = "insert into t_nf_chat_member (chat_room_seq, user_id,reg_date, org_user_id, enc_user_name) values (?, ?,getDate(),?,?)";						
						for(String member : mList) {
							PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
							
							insertStmt.setLong(1,newIdx);
							insertStmt.setString(2, member.trim());
							insertStmt.setString(3, member.trim());
														
							//익명.
							if(roomType == 3) {
								insertStmt.setString(4, MsgKey.getAnonymityName());
							} else {
								insertStmt.setString(4, "");
							}
							
							insertStmt.executeUpdate();
							insertStmt.close();
						}
			        }
					
					stmt.close();
				}
			}
			
			if(roomIdx.equals("") == false){
				
				
				String resTxt = "";
				String query = "select CM.*, 0 as user_type, MX.gender, MX.user_name , MX.phone_number, 0 as group_seq, '' as group_name from t_nf_chat_member CM"
						+ " left join t_nf_user MX on CM.user_id = MX.user_id"						
						+ " where CM.chat_room_seq = ?";
				
				PreparedStatement stmt = connection.prepareStatement(query);
				stmt.setLong(1, Long.valueOf(roomIdx));
				ResultSet rs = stmt.executeQuery();	
				ArrayList<HashMap<String, String>> array = new ArrayList<HashMap<String,String>>();
				while(rs.next()) {
					HashMap<String, String> map = new HashMap<String, String>();					
					String user_id = rs.getString("user_id");
				    String user_name = ChatDBhelper.getSafeString(rs.getString("user_name"));					 
					String phone_number = ChatDBhelper.getSafeString(rs.getString("phone_number")); 
					String user_type = ChatDBhelper.getSafeString(rs.getString("user_type"));
					String group_id = ChatDBhelper.getSafeString(rs.getString("group_seq"));
					String org_nm = ChatDBhelper.getSafeString(rs.getString("group_name"));
					String gender = ChatDBhelper.getSafeString(Integer.toString(rs.getInt("gender")));
					
					map.put("user_id", user_id);
					
					//익명.
					if(roomType == 3) {
						String enc_user_name = ChatDBhelper.getSafeString(rs.getString("enc_user_name"));
						map.put("user_name", enc_user_name);
					} else {
						map.put("user_name", user_name);
					}
					
					map.put("phone_number", phone_number);
					map.put("user_type", user_type);
					map.put("group_id", group_id);
					map.put("org_nm", org_nm);
					map.put("gender", gender);
					
					array.add(map);
				}
				stmt.close(); 
				
				if(array.size() > 0) {
					Gson gson = new Gson();
					resTxt = gson.toJson(array);	
				}
				//////////
				
				Param param = new Param();
				param.put("reqKey", reqKey);
				param.put("result", "true");
				param.put("roomIdx", roomIdx);
				param.put("roomType", String.valueOf(roomType));
				param.put("ownerID", createrID);
				param.put("title", title);
				param.put("memberInfo", resTxt);				
				param.put("reqUserId", userId);
				
								
				ChatServerHandler.sendData(channel, ProtocolType.ptChat
						, ProtocolType.stChatCreateRoomRes, param);
				
				
			} else {
				
				Param param = new Param();
				param.put("reqKey", reqKey);
				param.put("result", "false");
				param.put("reqUserId", userId);
				
				ChatServerHandler.sendData(channel, ProtocolType.ptChat
						, ProtocolType.stChatCreateRoomRes, param);
				

			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}
	
	
	public static void getRoomInfo(Channel channel, String reqKey, long roomIdx, String userId ) {
		
		Connection connection = DBConnectionPool.getConnection();				
		try {			
			String query = "select chat_room_seq, chat_room_type, owner_id, title  from t_nf_chat_room where chat_room_seq=?";
							
			
			
			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setLong(1, roomIdx);
			
			ResultSet rs = stmt.executeQuery();
			
			int roomType = 0;
			String createrID = "";
			String title = "";
			
			if(rs.next()) {
				roomType = rs.getInt("chat_room_type");
				createrID = rs.getString("owner_id");
				title = rs.getString("title");	
				
			}
			stmt.close(); 
			
			
			boolean hasRoomMember = false;
			query = "select chat_room_seq from t_nf_chat_member where chat_room_seq = ? and user_id = ?";
			
			stmt = connection.prepareStatement(query);
			stmt.setLong(1, roomIdx);
			stmt.setString(2, userId);
			
			rs = stmt.executeQuery();			
			if(rs.next()) {
				hasRoomMember = true;
			}
			stmt.close(); 

			String resTxt = "";
			//if(hasRoomMember) {
				query = "select CM.*, 0 as user_type, MX.gender ,MX.user_name, MX.phone_number,MX.photo, 0 as group_seq, '' as group_name from t_nf_chat_member CM"
						+ " left join t_nf_user MX on CM.user_id = MX.user_id"						
						+ " where CM.chat_room_seq = ?";
				
				
				
				stmt = connection.prepareStatement(query);
				stmt.setLong(1, roomIdx);
				
				rs = stmt.executeQuery();		
				
			
				
				ArrayList<HashMap<String, String>> array = new ArrayList<HashMap<String,String>>();
				while(rs.next()) {
					
					HashMap<String, String> map = new HashMap<String, String>();
					
					String user_id = rs.getString("user_id");
				    String user_name = ChatDBhelper.getSafeString(rs.getString("user_name"));					 
					String phone_number = ChatDBhelper.getSafeString(rs.getString("phone_number")); 
					String user_type = ChatDBhelper.getSafeString(rs.getString("user_type"));
					String group_id = ChatDBhelper.getSafeString(rs.getString("group_seq"));
					String org_nm = ChatDBhelper.getSafeString(rs.getString("group_name"));
					String gender = ChatDBhelper.getSafeString(Integer.toString(rs.getInt("gender")));
					
					map.put("user_id", user_id);
					//익명.
					if(roomType == 3) {
						String enc_user_name = ChatDBhelper.getSafeString(rs.getString("enc_user_name"));
						map.put("user_name", enc_user_name);
					} else {
						map.put("user_name", user_name);
					}
					
					map.put("phone_number", phone_number);
					map.put("user_type", user_type);
					
					map.put("group_id", group_id);
					map.put("org_nm", org_nm);
					map.put("gender", gender);
			
					array.add(map);
				}
				stmt.close(); 
				
				if(array.size() > 0) {
					Gson gson = new Gson();
					resTxt = gson.toJson(array);	
				}
			//}
			
			
			if(resTxt.equals("") == false){
				
				Param param = new Param();
				param.put("reqKey", reqKey);
				param.put("result", "true");
				param.put("roomIdx", String.valueOf(roomIdx));
				param.put("roomType", String.valueOf(roomType));
				param.put("ownerID", createrID);
				param.put("title", title);
				param.put("memberInfo", resTxt);
				param.put("reqUserId", userId);
				//param.put("useMega", useMega);
				
				
				
				ChatServerHandler.sendData(channel, ProtocolType.ptChat
						, ProtocolType.stChatRoomInfoRes, param);
			} else {
				Param param = new Param();
				param.put("reqKey", reqKey);
				param.put("result", "false");
				param.put("roomIdx", String.valueOf(roomIdx));
				param.put("roomType", String.valueOf(roomType));
				param.put("ownerID", createrID);
				param.put("title", title);
				param.put("memberInfo", resTxt);
				param.put("reqUserId", userId);
				
				ChatServerHandler.sendData(channel, ProtocolType.ptChat
						, ProtocolType.stChatRoomInfoRes, param);
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}
	
	public static void inviteMember(Channel channel,ChatUserContext chatUserContext
			, String userId
			, String reqKey, long roomIdx, String inviteUser ) {
		
		Connection connection = DBConnectionPool.getConnection();				
		try {	
			String query = "select chat_room_seq, chat_room_type, owner_id, title  from t_nf_chat_room where chat_room_seq=?";
			
			PreparedStatement stmtx = connection.prepareStatement(query);
			stmtx.setLong(1, roomIdx);
			
			ResultSet rsx = stmtx.executeQuery();
			
			int roomType = 0;
			String createrID = "";
			String title = "";
			if(rsx.next()) {
				roomType = rsx.getInt("chat_room_type");
				createrID = rsx.getString("owner_id");
				title = rsx.getString("title");	
			}
			stmtx.close(); 
			
			
			
			query = "select chat_room_seq from t_nf_chat_member where chat_room_seq = ? and user_id = ?";
			ArrayList<String> memberList = new ArrayList<String>();
			String[] list = inviteUser.split("[,]");			
			for(String member : list) {				
				
				PreparedStatement stmt = connection.prepareStatement(query);
				stmt.setLong(1, roomIdx);
				stmt.setString(2, member);
				ResultSet rs = stmt.executeQuery();
				
				if(rs.next()) {
					//占싱뱄옙占쌍는곤옙占�
				} else {
					//占쌩곤옙
					String queryInsert = "insert into t_nf_chat_member (chat_room_seq, user_id, reg_date, org_user_id, enc_user_name) values (?, ?,getDate(), ?, ?)";
					PreparedStatement stmtInsert = connection.prepareStatement(queryInsert);
					stmtInsert.setLong(1, roomIdx);
					stmtInsert.setString(2, member);
					stmtInsert.setString(3, member.trim());
					
					//익명.
					if(roomType == 3) {
						String encname = MsgKey.getAnonymityName();
						stmtInsert.setString(4, encname);
					} else {
						stmtInsert.setString(4, "");
					}
					
					stmtInsert.executeUpdate();					
					stmtInsert.close();
					
					memberList.add(member);					
				}
				stmt.close();
			}
						
			String resTxt = "";
			ArrayList<HashMap<String, String>> array = new ArrayList<HashMap<String,String>>();
			JsonObject jj= new JsonObject();
			for(String member : memberList) {				
				query = "select A.user_type, A.user_name, A.phone_number, A.gender, A.photo, 0 as group_seq, '' as group_name from t_nf_user A "
					
						+ "where A.user_id = ? ";
								
				PreparedStatement stmt = connection.prepareStatement(query);
				stmt.setString(1, member);
				
				ResultSet rs = stmt.executeQuery();			
				if(rs.next()) {		
					HashMap<String, String> map = new HashMap<String, String>();
					
					
					String user_id = member;
				    String user_name = rs.getString("user_name");
					String IMG_REGDATE = ""; 
					String phone_number = rs.getString("phone_number"); 
					String user_type = rs.getString("user_type"); ;
					String group_id = ChatDBhelper.getSafeString(rs.getString("group_seq"));
					String org_nm = ChatDBhelper.getSafeString(rs.getString("group_name"));
					String gender = ChatDBhelper.getSafeString(Integer.toString(rs.getInt("gender")));
					String photo = ChatDBhelper.getSafeString(rs.getString("photo"));
					map.put("user_id", user_id);
					
					if(roomType == 3) {
						map.put("user_name", "익명");	
					} else {
						map.put("user_name", user_name);
					}
					
					map.put("phone_number", phone_number);
					map.put("user_type", user_type);
					map.put("group_id", group_id);
					map.put("org_nm", org_nm);
					map.put("gender", gender);
					
					String photoStatus = "0";
					if(!photo.equals("")){//�ъ쭊�댁엳�쇰㈃
						photoStatus="1";
					}
				
					jj.addProperty("photoStatus", photoStatus);
					jj.addProperty("gender", gender);
					array.add(map);
					
				}
				stmt.close(); 
			}
			
			if(array.size() > 0) {
				Gson gson = new Gson();
				resTxt = gson.toJson(array);	
			}
			String userTxt=jj.toString();
		
			
			
			if(resTxt.equals("")) {
				//占쏙옙占쏙옙
				if(channel != null) {
					Param param = new Param();
					param.put("reqKey", reqKey);
					param.put("result", "false");
					param.put("roomIdx", String.valueOf(roomIdx));
					ChatServerHandler.sendData(channel, ProtocolType.ptChat
							, ProtocolType.stChatRoomInviteRes, param);
				}
				
			} else {				
				String msgKey = MsgKey.getMsgKey();
				String cType = "3";
				String mType = "0";
				
				Date date = new Date();
				String sDate = ChatDate.getFormatterServerDate().format(date);
				
				ChatMsgDao.sendMsg(chatUserContext, true
						, sDate, msgKey,String.valueOf(roomIdx), mType, cType, userId
						, resTxt, "", "", userTxt, "", date, "");				
				
				
				Param param = new Param();
				param.put("reqKey", reqKey);
				param.put("result", "true");
				param.put("roomIdx", String.valueOf(roomIdx));
				ChatServerHandler.sendData(channel, ProtocolType.ptChat
						, ProtocolType.stChatRoomInviteRes, param);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception 
			}
		}
	}
	
	
	//�섍�湲�
	public static void removeMember(Channel channel,ChatUserContext chatUserContext, String reqKey, long roomIdx, String userID, String memberID) {
		
		Connection connection = DBConnectionPool.getConnection();
		try {

			// 諛⑹씠 �덈뒗吏��뺤씤
			String query = "select chat_room_type, owner_id from t_nf_chat_room where chat_room_seq = ?";
			PreparedStatement stmtRoom = connection.prepareStatement(query);
			stmtRoom.setLong(1, roomIdx);
			ResultSet rsRoom = stmtRoom.executeQuery();
			if (rsRoom.next()) {
				int roomType = rsRoom.getInt(1);
				String ownerId = rsRoom.getString(2);
				boolean isOwner = false;

				if (memberID.equals(ownerId)) {
					isOwner = true;
				}
				query = "select * from t_nf_chat_member where chat_room_seq = ? and user_id = ?";
				PreparedStatement memIn = connection.prepareStatement(query);
				memIn.setLong(1, roomIdx);
				memIn.setString(2,memberID);
				ResultSet memrs = memIn.executeQuery();
				if(memrs.next()){
					
					// 硫ㅻ쾭 紐뉖챸 �⑥븯�붿� �뺤씤
					query = "select count(*) from t_nf_chat_member where chat_room_seq = ?";
					PreparedStatement stmtCount = connection.prepareStatement(query);
					stmtCount.setLong(1, roomIdx);
					ResultSet countRoom = stmtCount.executeQuery();
					int nowmemCount = 0;
					if (countRoom.next()) {
						nowmemCount = countRoom.getInt(1);
					}
					stmtCount.close();
					if (nowmemCount <= 2) {// �먮챸�⑥븯�붾뜲 �섍��ㅺ퀬��
						// 諛⑹옣�몄� �뺤씤�댁빞��
						String resTxt = "";
						String userTxt="";
						if (isOwner) {// 諛⑹옣�대㈃ 諛⑺룺
	
							query = "select A.user_id, A.enc_user_name, 0 as user_type, B.gender,B.photo, B.user_name, B.phone_number, 0 as group_seq, '' as group_name from t_nf_chat_member A "
									+ "left join t_nf_user B on A.user_id = B.user_id " + "where A.chat_room_seq = ? ";
	
							PreparedStatement stmt = connection.prepareStatement(query);
							stmt.setLong(1, roomIdx);
	
							ArrayList<HashMap<String, String>> mapList = new ArrayList<HashMap<String, String>>();
							JsonObject jj = new JsonObject();
							ResultSet rs = stmt.executeQuery();
							while (rs.next()) {
	
								HashMap<String, String> map = new HashMap<String, String>();
								
								String user_id = rs.getString("user_id");
								String user_name = ChatDBhelper.getSafeString(rs.getString("user_name"));
								String enc_user_name = ChatDBhelper.getSafeString(rs.getString("enc_user_name"));
								if(roomType == 3) {
									user_name = enc_user_name;
								}
								String phone_number = ChatDBhelper.getSafeString(rs.getString("phone_number"));
								String user_type = rs.getString("user_type");
								String group_id = ChatDBhelper.getSafeString(rs.getString("group_seq"));
								String org_nm = ChatDBhelper.getSafeString(rs.getString("group_name"));
								String gender = ChatDBhelper.getSafeString(Integer.toString(rs.getInt("gender")));
								String photo = ChatDBhelper.getSafeString(rs.getString("photo"));
								String photoStatus = "0";
								if(!photo.equals("")){//�ъ쭊�댁엳�쇰㈃
									photoStatus="1";
								}
								
								jj.addProperty("photoStatus", photoStatus);
								map.put("user_id", user_id);
								map.put("user_name", user_name);
								map.put("phone_number", phone_number);
								map.put("user_type", user_type);
								map.put("group_id", group_id);
								map.put("org_nm", org_nm);
								map.put("type", "0");
								map.put("gender", gender);
								jj.addProperty("gender", gender);
								
								mapList.add(map);
							
							}
							stmt.close();
							Gson gson = new Gson();
							resTxt = gson.toJson(mapList);
							
							
							userTxt = jj.toString();
	
							String msgKey = MsgKey.getMsgKey();
							String mType = "0";
							String cType = "4";
							Date date = new Date();
							String sDate = ChatDate.getFormatterServerDate().format(date);
	
							ChatMsgDao.sendMsg(chatUserContext, true, sDate, msgKey, String.valueOf(roomIdx), mType, cType,
									userID, resTxt, "", "0",userTxt , "", date, "");
							// 占쏙옙占쏙옙占쏙옙, 占쏙옙占�占쏙옙占쏙옙.
							// 占쌨쏙옙占쏙옙 占쏙옙占쏙옙.
							// tran 占쏙옙占쏙옙.
							query = "delete from t_nf_chat_tran where chat_room_seq = ? and chat_msg_key <> ?";
							stmt = connection.prepareStatement(query);
							stmt.setLong(1, roomIdx);
							stmt.setString(2, msgKey);
							stmt.executeUpdate();
							stmt.close();
	
							query = "delete from t_nf_chat_msg where chat_room_seq = ? and chat_msg_key <> ?";
							stmt = connection.prepareStatement(query);
							stmt.setLong(1, roomIdx);
							stmt.setString(2, msgKey);
							stmt.executeUpdate();
							stmt.close();
	
							query = "delete from t_nf_chat_member where chat_room_seq = ?";
							stmt = connection.prepareStatement(query);
							stmt.setLong(1, roomIdx);
							stmt.executeUpdate();
							stmt.close();
	
							query = "delete from t_nf_chat_room where chat_room_seq = ?";
							stmt = connection.prepareStatement(query);
							stmt.setLong(1, roomIdx);
							stmt.executeUpdate();
							stmt.close();
	
						} else {// 諛⑹옣���꾨땲硫�
	
							// 硫ㅻ쾭�먯꽌 吏����
							
							//// �섍��붿궗���뺣낫媛�졇�ㅺ린
							query = "select user_id,user_name,phone_number,gender,photo " 
									+ "from t_nf_user " 
									+ "where user_id = ? ";
							PreparedStatement stmt = connection.prepareStatement(query);
							stmt.setString(1, memberID);
	
							ArrayList<HashMap<String, String>> mapList = new ArrayList<HashMap<String, String>>();
							JsonObject jj = new JsonObject();
							ResultSet rs = stmt.executeQuery();
							while (rs.next()) {
	
								HashMap<String, String> map = new HashMap<String, String>();
							
	
								String user_id = rs.getString("user_id");
								String user_name = ChatDBhelper.getSafeString(rs.getString("user_name"));
								if (roomType == 3) {
									user_name = "익명";
								}
								
								String phone_number = ChatDBhelper.getSafeString(rs.getString("phone_number"));
								String gender = ChatDBhelper.getSafeString(Integer.toString(rs.getInt("gender")));
								String photo = ChatDBhelper.getSafeString(rs.getString("photo"));
								String photoStatus = "0";
								if(!photo.equals("")){//�ъ쭊�댁엳�쇰㈃
									photoStatus="1";
								}
								
								jj.addProperty("photoStatus", photoStatus);
								map.put("user_id", user_id);
								map.put("user_name", user_name);
								map.put("phone_number", phone_number);
								map.put("gender", gender);
								jj.addProperty("gender", gender);
								mapList.add(map);	
							}
							
							stmt.close();
							Gson gson = new Gson();
							resTxt = gson.toJson(mapList);
							
							
							userTxt = jj.toString();
							String msgKey = MsgKey.getMsgKey();
							String mType = "0";
							String cType = "4";
	
							Date date = new Date();
							String sDate = ChatDate.getFormatterServerDate().format(date);
	
							ChatMsgDao.sendMsg(chatUserContext, true, sDate, msgKey, String.valueOf(roomIdx), mType, cType,
									userID, resTxt, "", "0", userTxt , "", date, "");
							
							query = "delete from t_nf_chat_member where chat_room_seq = ? and user_id = ? ";
							stmt = connection.prepareStatement(query);
							
							stmt.setLong(1, roomIdx);
							stmt.setString(2, memberID);
							stmt.executeUpdate();
							stmt.close();		
									
						}
	
					} else {// �먮챸�댁긽�댁빞
						
						
						
						
						String resTxt = "";
						String userTxt="";
						
	
						ArrayList<HashMap<String, String>> mapList = new ArrayList<HashMap<String, String>>();
						
						HashMap<String, String> map = new HashMap<String, String>();
						JsonObject jj = new JsonObject();
						String user_name = "";
						/// �섍��붿븷 �뺣낫媛�졇�ㅺ린
						query = "select user_name,gender,photo from t_nf_user where user_id = ? ";
						PreparedStatement stmt = connection.prepareStatement(query);
						stmt.setString(1, memberID);
						ResultSet rs = stmt.executeQuery();
	
						if (rs.next()) {
							user_name = ChatDBhelper.getSafeString(rs.getString("user_name"));
							String gender = ChatDBhelper.getSafeString(Integer.toString(rs.getInt("gender")));
							String photo = ChatDBhelper.getSafeString(rs.getString("photo"));
							String photoStatus = "0";
							if(!photo.equals("")){//�ъ쭊�댁엳�쇰㈃
								photoStatus="1";
							}
							
							jj.addProperty("photoStatus", photoStatus);
							map.put("user_id", memberID);
							if(roomType == 3) {
								map.put("user_name", "익명");	
							} else {
								map.put("user_name", user_name);
							}
							
							map.put("gender", gender);
							jj.addProperty("gender", gender);
							mapList.add(map);
						
						}
	
						stmt.close();
						Gson gson = new Gson();
						resTxt = gson.toJson(mapList);
					
						userTxt = jj.toString();
	
						String msgKey = MsgKey.getMsgKey();
						String mType = "0";
						String cType = "4";
						Date date = new Date();
						String sDate = ChatDate.getFormatterServerDate().format(date);
						
	
						ChatMsgDao.sendMsg(chatUserContext, true, sDate, msgKey, String.valueOf(roomIdx), mType, cType,
								userID, resTxt, "", "0", userTxt, "", date, "");
						
						query = "DELETE FROM T_NF_CHAT_MEMBER WHERE chat_room_seq = ? and user_id= ?";
						
						stmt = connection.prepareStatement(query);
						stmt.setLong(1, roomIdx);
						stmt.setString(2, memberID);
						stmt.executeUpdate();
						stmt.close(); // �쇰떒 �섎� 吏�슦怨�
						
						if (isOwner) {// �섍컙�좉� 諛⑹옣�꾩씠�붿씠硫�
							// 媛�옣 泥섏쓬 �묓븳���꾩씠�붽��몄삤湲�
							query = "select top 1 m.user_id,m.enc_user_name,u.user_name,u.gender,u.photo  "
									+ " from t_nf_chat_member as m left outer join "
									+ " t_nf_user as u on m.user_id = u.user_id "
									+ " where m.chat_room_seq = ?  order by m.reg_date asc";
	
							stmt = connection.prepareStatement(query);
							stmt.setLong(1, roomIdx);
							
							ResultSet nextUser = stmt.executeQuery();
							String nextOwner = "";
							String nextOwnerName = "";
							String nextOwnergender = "";
							String photoStatus = "0";
						
							if (nextUser.next()) {
								nextOwner = nextUser.getString("user_id");
								if(roomType == 3) {
									nextOwnerName = nextUser.getString("enc_user_name");
								} else {
									nextOwnerName = nextUser.getString("user_name");									
								}
								
								
								nextOwnergender = Integer.toString(nextUser.getInt("gender"));
								String photo = nextUser.getString("photo");
								
								if(!photo.equals("")){//�ъ쭊�댁엳�쇰㈃
									photoStatus="1";
								}
								
								
							}
	
							
							stmt.close();
							// 諛⑹젙蹂��낅뜲�댄듃
	
							query = " update t_nf_chat_room set owner_id = ? where chat_room_seq = ? ";
							stmt = connection.prepareStatement(query);
							stmt.setString(1, nextOwner);
							stmt.setLong(2, roomIdx);
							stmt.executeUpdate();
							stmt.close(); // �ㅼ쓬�좉� 諛⑹옣�쇰줈�レ쑝�덇퉴
							// 諛⑹옣 諛붾�嫄��뚮━湲�
							msgKey = MsgKey.getMsgKey();
							mType = "0";
							cType = "10";
							date = new Date();
							sDate = ChatDate.getFormatterServerDate().format(date);
							map.put("user_id", nextOwner);
							map.put("user_name", nextOwnerName);
							map.put("gender", nextOwnergender);
							jj.addProperty("gender", nextOwnergender);
							jj.addProperty("photoStatus", photoStatus);
	
							mapList.add(map);
						
							gson = new Gson();
							resTxt = gson.toJson(mapList);
							
							
							userTxt = jj.toString();
	
							ChatMsgDao.sendMsg(chatUserContext, true, sDate, msgKey, String.valueOf(roomIdx), mType, cType,
									nextOwner, resTxt, "", "", userTxt, "", date, "");
	
						}
	
						if (channel != null) {
							Param param = new Param();
							param.put("reqKey", reqKey);
							param.put("result", "true");
							param.put("code", "1");
							param.put("roomIdx", String.valueOf(roomIdx));
							ChatServerHandler.sendData(channel, ProtocolType.ptChat, ProtocolType.stChatMemberDeleteRes,
									param);
						}
					}
					stmtCount.close();
			  } else {
					Param param = new Param();
					param.put("reqKey", reqKey);
					param.put("result", "true");
					param.put("code", "1");
					param.put("roomIdx", String.valueOf(roomIdx));
					ChatServerHandler.sendData(channel, ProtocolType.ptChat, ProtocolType.stChatMemberDeleteRes,
							param);
			  }
				memIn.close();
			} else {
				// 占쏙옙占쏙옙
				if (channel != null) {
					Param param = new Param();
					param.put("reqKey", reqKey);
					param.put("result", "false");
					param.put("code", "1");
					param.put("roomIdx", String.valueOf(roomIdx));
					ChatServerHandler.sendData(channel, ProtocolType.ptChat, ProtocolType.stChatMemberDeleteRes, param);
				}
			}
			stmtRoom.close();
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}
	
	
	
	////////////////////////////媛뺥눜
	public static void dropMember(Channel channel,ChatUserContext chatUserContext, String reqKey, long roomIdx, String userID, String memberID
							  ,String dId) {
		
		Connection connection = DBConnectionPool.getConnection();				
		try {	
			
			//諛⑹씠 �덈뒗吏��뺤씤
			String query = "select chat_room_type, owner_id from t_nf_chat_room where chat_room_seq = ?";			
			PreparedStatement stmtRoom = connection.prepareStatement(query);
			stmtRoom.setLong(1, roomIdx);			
			ResultSet rsRoom = stmtRoom.executeQuery();
			if(rsRoom.next()) {
				int roomType = rsRoom.getInt(1);
				String ownerId = rsRoom.getString(2);
				boolean isOwner = false;
				if(ownerId.equals(memberID)){
					isOwner=true;
				}
				
				if(isOwner){
					
					
					//硫ㅻ쾭媛�엳�붿�
					query = "select user_id from t_nf_chat_member where chat_room_seq = ?";			
					PreparedStatement Memstmt = connection.prepareStatement(query);
					Memstmt.setLong(1, roomIdx);			
					ResultSet rs = Memstmt.executeQuery();
					ArrayList<HashMap<String, String>> mapList = new ArrayList<HashMap<String, String>>();
					JsonObject jj = new JsonObject();
					if(rs.next()){
						//移쒓뎄�몄�
						query = "select * from t_nf_friend where user_id = ? and friend_id = ? ";
						PreparedStatement Frstmt = connection.prepareStatement(query);
						Frstmt.setString(1, memberID);
						Frstmt.setString(2, dId);
						ResultSet frRs = Frstmt.executeQuery();
						boolean status = true;
						if(frRs.next()){
							status=false;
						}
						Frstmt.close();
						if(status){//移쒓뎄媛��꾨땲硫�
							query = "select user_id,user_name,phone_number,user_type,gender,photo "
									+ "from t_nf_user "							
									+ "where user_id = ? ";
									PreparedStatement Userstmt = connection.prepareStatement(query);				
									Userstmt.setString(1,dId);
									ResultSet user = Userstmt.executeQuery();
									if(user.next()){
										
										HashMap<String, String> map = new HashMap<String, String>();
										
										String user_id = user.getString("user_id");
										String user_name = ChatDBhelper.getSafeString(user.getString("user_name"));
										if(roomType == 3) {
											user_name = "익명";
										}
									    
										String phone_number = ChatDBhelper.getSafeString(user.getString("phone_number")); 
										String user_type = user.getString("user_type");
										String gender = ChatDBhelper.getSafeString(Integer.toString(user.getInt("gender")));
										String photo = ChatDBhelper.getSafeString(user.getString("photo"));
										String photoStatus="0";
										if(!photo.equals("")){//�ъ쭊�댁엳�쇰㈃
											photoStatus="1";
										}
										map.put("user_id", user_id);
										map.put("user_name", user_name);
										map.put("phone_number", phone_number);
										map.put("user_type", user_type);
										map.put("gender", gender);
										map.put("type", "1");
										jj.addProperty("gender", gender);
										jj.addProperty("photoStatus", photoStatus);
										
										mapList.add(map);
										
									}
									
									Userstmt.close(); 
									Gson gson = new Gson();
									String resTxt = gson.toJson(mapList);
									
									String userTxt = jj.toString();
									
									String msgKey = MsgKey.getMsgKey();
									String mType = "0";
									String cType = "4";
									Date date = new Date();
									String sDate = ChatDate.getFormatterServerDate().format(date);
									
									
									ChatMsgDao.sendMsg(chatUserContext, true
											, sDate, msgKey,String.valueOf(roomIdx), mType, cType, userID
											, resTxt, "", "1", userTxt, "", date, "");
									
									
									
							query = "delete from t_nf_chat_member where chat_room_seq = ? and user_id = ? ";
									
								PreparedStatement stmt = connection.prepareStatement(query);
									stmt.setLong(1, roomIdx);
									stmt.setString(2, dId);
									stmt.executeUpdate();
									stmt.close();
									
									
							query = "insert into T_NF_CHAT_OUT (block_id, chat_room_seq , reg_date) values(?, ?,getdate())";
									
							
									stmt = connection.prepareStatement(query, 1);
									stmt.setString(1, dId);
									stmt.setLong(2, roomIdx);
									stmt.executeUpdate();
									stmt.close();
									
							if (channel != null) {
								Param param = new Param();
								param.put("reqKey", reqKey);
								param.put("result", "true");
								param.put("code", "1");
								param.put("roomIdx", String.valueOf(roomIdx));
								ChatServerHandler.sendData(channel, ProtocolType.ptChat,
										ProtocolType.stChatMemberDropRes, param);
							}	
						}else{//移쒓뎄�몄�
							if(channel != null) {
								Param param = new Param();
								param.put("reqKey", reqKey);
								param.put("result", "false");
								param.put("code", "3");
								param.put("roomIdx", String.valueOf(roomIdx));
								ChatServerHandler.sendData(channel, ProtocolType.ptChat
										, ProtocolType.stChatMemberDropRes, param);	
							}
						}
					}
					Memstmt.close();
				}else{ //諛⑹옣�몄�
					if(channel != null) {
						Param param = new Param();
						param.put("reqKey", reqKey);
						param.put("result", "false");
						param.put("code", "2");
						param.put("roomIdx", String.valueOf(roomIdx));
						ChatServerHandler.sendData(channel, ProtocolType.ptChat
								, ProtocolType.stChatMemberDropRes, param);	
					}
				}
				
				
			} else { //諛��뺣낫媛��덈뒗吏�
				//占쏙옙占쏙옙
				if(channel != null) {
					Param param = new Param();
					param.put("reqKey", reqKey);
					param.put("result", "false");
					param.put("code", "1");
					param.put("roomIdx", String.valueOf(roomIdx));
					ChatServerHandler.sendData(channel, ProtocolType.ptChat
							, ProtocolType.stChatMemberDropRes, param);	
				}
			}			
			stmtRoom.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}
	
	
	public static void removeRoom(Channel channel,ChatUserContext chatUserContext, String reqKey, long roomIdx, String userID, String memberID) {
		
		Connection connection = DBConnectionPool.getConnection();
		try {

			// 諛⑹씠 �덈뒗吏��뺤씤
			String query = "select chat_room_type, owner_id from t_nf_chat_room where chat_room_seq = ?";
			PreparedStatement stmtRoom = connection.prepareStatement(query);
			stmtRoom.setLong(1, roomIdx);
			ResultSet rsRoom = stmtRoom.executeQuery();
			if (rsRoom.next()) {
				int roomType = rsRoom.getInt(1);
				String ownerId = rsRoom.getString(2);
				boolean isOwner = false;

				
				query = "select * from t_nf_chat_member where chat_room_seq = ? and user_id = ?";
				PreparedStatement memIn = connection.prepareStatement(query);
				memIn.setLong(1, roomIdx);
				memIn.setString(2,memberID);
				ResultSet memrs = memIn.executeQuery();
				if(memrs.next()){
					
				
					String resTxt = "";
					String userTxt="";
					query = "select A.user_id, A.enc_user_name, 0 as user_type, B.gender,B.photo, B.user_name, B.phone_number, 0 as group_seq, '' as group_name from t_nf_chat_member A "
							+ "left join t_nf_user B on A.user_id = B.user_id " + "where A.chat_room_seq = ? ";
	
						PreparedStatement stmt = connection.prepareStatement(query);
						stmt.setLong(1, roomIdx);
	
						ArrayList<HashMap<String, String>> mapList = new ArrayList<HashMap<String, String>>();
						JsonObject jj = new JsonObject();
						ResultSet rs = stmt.executeQuery();
						while (rs.next()) {
	
								HashMap<String, String> map = new HashMap<String, String>();
								
								String user_id = rs.getString("user_id");
								String user_name = ChatDBhelper.getSafeString(rs.getString("user_name"));
								String enc_user_name = ChatDBhelper.getSafeString(rs.getString("enc_user_name"));
								if(roomType == 3) {
									user_name = enc_user_name;
								}
								String phone_number = ChatDBhelper.getSafeString(rs.getString("phone_number"));
								String user_type = rs.getString("user_type");
								String group_id = ChatDBhelper.getSafeString(rs.getString("group_seq"));
								String org_nm = ChatDBhelper.getSafeString(rs.getString("group_name"));
								String gender = ChatDBhelper.getSafeString(Integer.toString(rs.getInt("gender")));
								
								
							
								map.put("user_id", user_id);
								map.put("user_name", user_name);
								map.put("phone_number", phone_number);
								map.put("user_type", user_type);
								map.put("group_id", group_id);
								map.put("org_nm", org_nm);
								map.put("type", "0");
								map.put("gender", gender);
								jj.addProperty("gender", gender);
								
								mapList.add(map);
							
							}
							stmt.close();
							Gson gson = new Gson();
							resTxt = gson.toJson(mapList);
							
							
							userTxt = jj.toString();
	
							String msgKey = MsgKey.getMsgKey();
							String mType = "0";
							String cType = "4";
							Date date = new Date();
							String sDate = ChatDate.getFormatterServerDate().format(date);
	
							ChatMsgDao.sendMsg(chatUserContext, true, sDate, msgKey, String.valueOf(roomIdx), mType, cType,
									userID, resTxt, "", "0",userTxt , "", date, "");
							
							
							query = "delete from t_nf_chat_tran where chat_room_seq = ? and chat_msg_key <> ?";
							stmt = connection.prepareStatement(query);
							stmt.setLong(1, roomIdx);
							stmt.setString(2, msgKey);
							stmt.executeUpdate();
							stmt.close();
	
							query = "delete from t_nf_chat_msg where chat_room_seq = ? and chat_msg_key <> ?";
							stmt = connection.prepareStatement(query);
							stmt.setLong(1, roomIdx);
							stmt.setString(2, msgKey);
							stmt.executeUpdate();
							stmt.close();
	
							query = "delete from t_nf_chat_member where chat_room_seq = ?";
							stmt = connection.prepareStatement(query);
							stmt.setLong(1, roomIdx);
							stmt.executeUpdate();
							stmt.close();
	
							query = "delete from t_nf_chat_room where chat_room_seq = ?";
							stmt = connection.prepareStatement(query);
							stmt.setLong(1, roomIdx);
							stmt.executeUpdate();
							stmt.close();
	
	
			 } else {
					Param param = new Param();
					param.put("reqKey", reqKey);
					param.put("result", "true");
					param.put("code", "1");
					param.put("roomIdx", String.valueOf(roomIdx));
					ChatServerHandler.sendData(channel, ProtocolType.ptChat, ProtocolType.stChatMemberDeleteRes,
							param);
			  }
				memIn.close();
			} else {
				// 占쏙옙占쏙옙
				if (channel != null) {
					Param param = new Param();
					param.put("reqKey", reqKey);
					param.put("result", "false");
					param.put("code", "1");
					param.put("roomIdx", String.valueOf(roomIdx));
					ChatServerHandler.sendData(channel, ProtocolType.ptChat, ProtocolType.stChatMemberDeleteRes, param);
				}
			}
			stmtRoom.close();
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}
	/////////////////////////////
	
	static void updateRecvMsgStatus(Connection connection, long roomIdx, String msgKey, String recvID, int status) {
		
		String query = "update t_nf_chat_tran set status = ? where chat_room_seq = ? and chat_msg_key = ? and rcv_id = ?";
		
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(query);
			stmt.setInt(1, status);
			stmt.setLong(2, roomIdx);
			stmt.setString(3, msgKey);
			stmt.setString(4, recvID);
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}		
	}
	

	static void deleteMsgStatus(Connection connection, long roomIdx, String msgKey, String recvID) {		
		String query = "delete from t_nf_chat_tran where chat_room_seq = ? and chat_msg_key = ? and rcv_id = ?";
		
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(query);
			stmt.setLong(1, roomIdx);
			stmt.setString(2, msgKey);
			stmt.setString(3, recvID);
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}		
	}
	
	public static void onRecvReadMsg(Channel channel
			, String msgKey, String roomIdx, String sendID, String recvID, int status) {
	
		Jedis jedis = RedisClient.getInstance().borrow();
		try {	
			
			
			if(status == 3) {
				//
				{				
					Param param = new Param();
					param.put("msgKey", msgKey);
					param.put("type", "4");			
					param.put("roomIdx", roomIdx);
					param.put("msgType", "0");
					param.put("sendID", sendID);
					param.put("recvID", recvID);
					
					Date date = new Date();
					String sDate = ChatDate.getFormatterServerDate().format(date);
					param.put("sDate", sDate);
					ChatServerHandler.sendData(channel, ProtocolType.ptChat
							, ProtocolType.stChatStatusRes, param);					
				}
				
				
				//레디스 업데이트 및 모든 사람에게 알림.
				//이전달.
				try {
					
				
					String jKey, jKey2;
					String pf = RedisClient.getBeforeMsgKeyPrefix();
					jKey = pf + "chatR:"  + msgKey;										
					String ruserid = jedis.hget(jKey, recvID);
					
					if("1".equals(ruserid)) {
						//존재하고 있음. 삭제, 업데이트, 모두에게 알림. 
						jedis.hdel(jKey, recvID);
						
						Map<String, String> dMap = jedis.hgetAll(jKey);
						
						String rcount = String.valueOf(dMap.size());
						jKey = "chatRCnt:"  + msgKey;
						jedis.set(jKey, rcount);
						jedis.expire(jKey, 5356800); //62일	
						
						
						Param param = new Param();
						param.put("cmd", "recvR");
						param.put("msgKey", msgKey);
						param.put("roomIdx", roomIdx);
						param.put("chatRCnt", rcount);
						
						Gson gson = new Gson();
						String resMsg = gson.toJson(param);
						
						//멤버 리스트.
						jKey = pf +  "chatM:"  + msgKey;										
						Map<String, String> memberMap = jedis.hgetAll(jKey);
											
						for (String member : memberMap.keySet()) {
						
							jKey = pf + member + ":msg";
							jKey2 = pf + member + "/msg";
							jedis.hset(jKey, "rcv_msg_r:" + msgKey, resMsg);	
							jedis.rpush(jKey2, "rcv_msg_r:" + msgKey);
							
							jedis.expire(jKey, 5356800); //62일
							jedis.expire(jKey2, 5356800); //62일
													
							
							
							ChatUserContext memberCtx = ChatUserContext.getUserContextMap().get(member);
							
							//실제전송
							if(memberCtx != null) {							
								//현재서버전송
								ChatServerHandler.sendRawData(memberCtx.getChannel(), ProtocolType.ptChat
										, ProtocolType.stReqGetMsg, "{}");								
							} else {
							
							}
						}	
	
						
						//PC 멤버 리스트.
						jKey = pf +"chatMP:"  + msgKey;										
						memberMap = jedis.hgetAll(jKey);
											
						for (String member : memberMap.keySet()) {
							
							jKey = pf +member + ":pmsg";
							jKey2 = pf + member + "/pmsg";
							jedis.hset(jKey, "rcv_msg_r:" + msgKey, resMsg);	
							jedis.rpush(jKey2, "rcv_msg_r:" + msgKey);
							
							jedis.expire(jKey, 5356800); //62일
							jedis.expire(jKey2, 5356800); //62일
							
							ChatUserContext memberCtx = ChatUserContext.getPcUserContextMap().get(member);
							
							//실제전송
							if(memberCtx != null) {							
								//현재서버전송
								ChatServerHandler.sendRawData(memberCtx.getChannel(), ProtocolType.ptChat
										, ProtocolType.stReqGetMsg, "{}");								
							} else {
							
							}
						}	
						
						if(rcount.equals("0")) {
							//모두 삭제.
							jKey = "chatDCnt:"  + msgKey;
							jedis.del(jKey);
							
							jKey = "chatRCnt:"  + msgKey;
							jedis.del(jKey);
							
							jKey = pf +"chatM:"  + msgKey;										
							Set<String> keys = jedis.hkeys(jKey);
							
							for(String field : keys) {
								jedis.hdel(jKey, field);	
							}
							
							jKey = pf +"chatMP:"  + msgKey;										
							keys = jedis.hkeys(jKey);						
							for(String field : keys) {
								jedis.hdel(jKey, field);	
							}
							
							
							jKey = pf +"chatD:"  + msgKey;			
							keys = jedis.hkeys(jKey);
							for(String field : keys) {
								jedis.hdel(jKey, field);	
							}
							
							jKey = pf +"chatR:"  + msgKey;
							keys = jedis.hkeys(jKey);
							for(String field : keys) {
								jedis.hdel(jKey, field);	
							}
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
				
				//레디스 업데이트 및 모든 사람에게 알림.
				//이번달
				String jKey, jKey2;
				String pf = RedisClient.getMsgKeyPrefix();
				jKey = pf + "chatR:"  + msgKey;										
				String ruserid = jedis.hget(jKey, recvID);
				
				if("1".equals(ruserid)) {
					//존재하고 있음. 삭제, 업데이트, 모두에게 알림. 
					jedis.hdel(jKey, recvID);
					
					Map<String, String> dMap = jedis.hgetAll(jKey);
					
					String rcount = String.valueOf(dMap.size());
					jKey = "chatRCnt:"  + msgKey;
					jedis.set(jKey, rcount);
					jedis.expire(jKey, 5356800); //62일	
					
					
					Param param = new Param();
					param.put("cmd", "recvR");
					param.put("msgKey", msgKey);
					param.put("roomIdx", roomIdx);
					param.put("chatRCnt", rcount);
					
					Gson gson = new Gson();
					String resMsg = gson.toJson(param);
					
					//멤버 리스트.
					jKey = pf +  "chatM:"  + msgKey;										
					Map<String, String> memberMap = jedis.hgetAll(jKey);
										
					for (String member : memberMap.keySet()) {
					
						jKey = pf + member + ":msg";
						jKey2 = pf + member + "/msg";
						jedis.hset(jKey, "rcv_msg_r:" + msgKey, resMsg);	
						jedis.rpush(jKey2, "rcv_msg_r:" + msgKey);
						
						jedis.expire(jKey, 5356800); //62일
						jedis.expire(jKey2, 5356800); //62일
												
						
						
						ChatUserContext memberCtx = ChatUserContext.getUserContextMap().get(member);
						
						//실제전송
						if(memberCtx != null) {							
							//현재서버전송
							ChatServerHandler.sendRawData(memberCtx.getChannel(), ProtocolType.ptChat
									, ProtocolType.stReqGetMsg, "{}");								
						} else {
						
						}
					}	

					
					//PC 멤버 리스트.
					jKey = pf +"chatMP:"  + msgKey;										
					memberMap = jedis.hgetAll(jKey);
										
					for (String member : memberMap.keySet()) {
						
						jKey = pf +member + ":pmsg";
						jKey2 = pf + member + "/pmsg";
						jedis.hset(jKey, "rcv_msg_r:" + msgKey, resMsg);	
						jedis.rpush(jKey2, "rcv_msg_r:" + msgKey);
						
						jedis.expire(jKey, 5356800); //62일
						jedis.expire(jKey2, 5356800); //62일

						ChatUserContext memberCtx = ChatUserContext.getPcUserContextMap().get(member);
	
						//실제전송
						if(memberCtx != null) {							
							//현재서버전송
							ChatServerHandler.sendRawData(memberCtx.getChannel(), ProtocolType.ptChat
									, ProtocolType.stReqGetMsg, "{}");								
						} else {
							ChatUserContext pcMember = ChatUserContext.getVcusercontextmap().get(member);
							if(pcMember != null) {
								ChatServerHandler.sendRawData(pcMember.getChannel(), ProtocolType.ptChat
										, ProtocolType.stReqGetMsg, "{}");						
							}
							
						}
					}	
					
					if(rcount.equals("0")) {
						//모두 삭제.
						jKey = "chatDCnt:"  + msgKey;
						jedis.del(jKey);
						
						jKey = "chatRCnt:"  + msgKey;
						jedis.del(jKey);
						
						jKey = pf +"chatM:"  + msgKey;										
						Set<String> keys = jedis.hkeys(jKey);
						
						for(String field : keys) {
							jedis.hdel(jKey, field);	
						}
						
						jKey = pf +"chatMP:"  + msgKey;										
						keys = jedis.hkeys(jKey);						
						for(String field : keys) {
							jedis.hdel(jKey, field);	
						}
						
						
						jKey = pf +"chatD:"  + msgKey;			
						keys = jedis.hkeys(jKey);
						for(String field : keys) {
							jedis.hdel(jKey, field);	
						}
						
						jKey = pf +"chatR:"  + msgKey;
						keys = jedis.hkeys(jKey);
						for(String field : keys) {
							jedis.hdel(jKey, field);	
						}
					}
				}
			} 
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
			System.out.println("onRecvReadMsg : " + e.getMessage());
		} finally {
			try {
				RedisClient.getInstance().revert(jedis);
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}
	
	
	

	public static void onRecvStatus(Channel channel
			, String msgKey, String roomIdx, String sendID, String recvID, int status) {
		
			
		
		
		Jedis jedis = RedisClient.getInstance().borrow();
		try {	
	
			if(status == 1) {
				
				//응답보넨 사람에게 응답
				{
					Param param = new Param();
					param.put("msgKey", msgKey);
					param.put("type", "2");			
					param.put("roomIdx", roomIdx);
					param.put("msgType", "0");	
					param.put("sendID", sendID);
					param.put("recvID", recvID);
					
					Date date = new Date();
					String sDate = ChatDate.getFormatterServerDate().format(date);
					param.put("sDate", sDate);			
					ChatServerHandler.sendData(channel, ProtocolType.ptChat
							, ProtocolType.stChatStatusRes, param);											
				}
				
				
				//레디스 업데이트 및 모든 사람에게 알림.
				//이전달.
				try {
					String jKey, jKey2;
					String pf = RedisClient.getBeforeMsgKeyPrefix();
					jKey = pf + "chatD:"  + msgKey;										
					String ruserid = jedis.hget(jKey, recvID);
									
					if("1".equals(ruserid)) {
						//존재하고 있음. 삭제, 업데이트, 모두에게 알림. 
						jedis.hdel(jKey, recvID);
						
						Map<String, String> dMap = jedis.hgetAll(jKey);
						
						String dcount = String.valueOf(dMap.size());
						jKey = "chatDCnt:"  + msgKey;
						jedis.set(jKey, dcount);
						jedis.expire(jKey, 5356800); //62일	
						
						Param param = new Param();
						param.put("cmd", "recvD");
						param.put("msgKey", msgKey);
						param.put("roomIdx", roomIdx);
						param.put("chatDCnt", dcount);
						
						Gson gson = new Gson();
						String resMsg = gson.toJson(param);
						
						//멤버 리스트.
						jKey = pf + "chatM:"  + msgKey;										
						Map<String, String> memberMap = jedis.hgetAll(jKey);
											
						for (String member : memberMap.keySet()) {
							
							jKey = pf + member + ":msg";	
							jKey2 = pf + member + "/pmsg";
	
							jedis.hset(jKey, "rcv_msg_d:" + msgKey , resMsg);
							jedis.rpush(jKey2, "rcv_msg_d:" + msgKey);
								
							jedis.expire(jKey, 5356800); //62일
							jedis.expire(jKey2, 5356800); //62일
							
							ChatUserContext memberCtx = ChatUserContext.getUserContextMap().get(member);
							
							//실제전송
							if(memberCtx != null) {							
								//현재서버전송
								ChatServerHandler.sendRawData(memberCtx.getChannel(), ProtocolType.ptChat
										, ProtocolType.stReqGetMsg, "{}");								
							} else {
							
							}
								
						}	
						
						
						//멤버 리스트.
						jKey = pf + "chatMP:"  + msgKey;										
						memberMap = jedis.hgetAll(jKey);
											
						for (String member : memberMap.keySet()) {
							
							jKey = pf + member + ":pmsg";
							jKey2 = pf + member + "/pmsg";
							jedis.hset(jKey, "rcv_msg_d:" + msgKey , resMsg);
							jedis.rpush(jKey2, "rcv_msg_d:" + msgKey);
							jedis.expire(jKey, 5356800); //62일
							jedis.expire(jKey2, 5356800); //62일
							
							ChatUserContext memberCtx = ChatUserContext.getUserContextMap().get(member);
							
							//실제전송
							if(memberCtx != null) {							
								//현재서버전송
								ChatServerHandler.sendRawData(memberCtx.getChannel(), ProtocolType.ptChat
										, ProtocolType.stReqGetMsg, "{}");								
							} else {
							
							}
								
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
				//레디스 업데이트 및 모든 사람에게 알림.
				//이번달.
				String jKey, jKey2;
				String pf = RedisClient.getMsgKeyPrefix();
				jKey = pf + "chatD:"  + msgKey;										
				String ruserid = jedis.hget(jKey, recvID);
								
				if("1".equals(ruserid)) {
					//존재하고 있음. 삭제, 업데이트, 모두에게 알림. 
					jedis.hdel(jKey, recvID);
					
					Map<String, String> dMap = jedis.hgetAll(jKey);
					
					String dcount = String.valueOf(dMap.size());
					jKey = "chatDCnt:"  + msgKey;
					jedis.set(jKey, dcount);
					jedis.expire(jKey, 5356800); //62일	
					
					Param param = new Param();
					param.put("cmd", "recvD");
					param.put("msgKey", msgKey);
					param.put("roomIdx", roomIdx);
					param.put("chatDCnt", dcount);
					
					Gson gson = new Gson();
					String resMsg = gson.toJson(param);
					
					//멤버 리스트.
					jKey = pf + "chatM:"  + msgKey;										
					Map<String, String> memberMap = jedis.hgetAll(jKey);
										
					for (String member : memberMap.keySet()) {
						
						jKey = pf + member + ":msg";	
						jKey2 = pf + member + "/pmsg";

						jedis.hset(jKey, "rcv_msg_d:" + msgKey , resMsg);
						jedis.rpush(jKey2, "rcv_msg_d:" + msgKey);
							
						jedis.expire(jKey, 5356800); //62일
						jedis.expire(jKey2, 5356800); //62일
						
						ChatUserContext memberCtx = ChatUserContext.getUserContextMap().get(member);
						
						//실제전송
						if(memberCtx != null) {							
							//현재서버전송
							ChatServerHandler.sendRawData(memberCtx.getChannel(), ProtocolType.ptChat
									, ProtocolType.stReqGetMsg, "{}");								
						} else {
						
						}
							
					}	
					
					
					//멤버 리스트.
					jKey = pf + "chatMP:"  + msgKey;										
					memberMap = jedis.hgetAll(jKey);
										
					for (String member : memberMap.keySet()) {
						
						jKey = pf + member + ":pmsg";
						jKey2 = pf + member + "/pmsg";
						jedis.hset(jKey, "rcv_msg_d:" + msgKey , resMsg);
						jedis.rpush(jKey2, "rcv_msg_d:" + msgKey);
						jedis.expire(jKey, 5356800); //62일
						jedis.expire(jKey2, 5356800); //62일
						
						ChatUserContext memberCtx = ChatUserContext.getUserContextMap().get(member);
						
						//실제전송
						if(memberCtx != null) {							
							//현재서버전송
							ChatServerHandler.sendRawData(memberCtx.getChannel(), ProtocolType.ptChat
									, ProtocolType.stReqGetMsg, "{}");								
						} else {
						
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
	
	
	

	public static void insertBufferMsg(Packet packet ) {
		/*
		Connection connection = DBConnectionPool.getConnection();				
		try {
			String query = "insert into t_nf_chat_buffer (server_id, dest_id, from_server_id, buffer_key, contents, pt, st, reg_date) values(?, ?, ?, ?, ?, ?, ?, getDate())";
			
			PreparedStatement stmt = connection.prepareStatement(query);
			
			stmt.setString(1, packet.getServerID());
			stmt.setString(2, packet.getDestID());
			stmt.setString(3, Config.getInstance().getServerId());
			stmt.setLong(4, packet.getBufferKey());
		
			//Gson gson = new Gson();
			//String resMsg = gson.toJson(packet.getParam());	
			stmt.setString(5, packet.getParam());//resMsg);
			stmt.setInt(6, packet.getSt());
			stmt.setInt(7, packet.getPt());
			
			stmt.executeUpdate();
					
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		*/
	}
	

	public static ArrayList<Packet> getBufferMsg() {
		/*
		ArrayList<Packet> result = null;
		
		Connection connection = DBConnectionPool.getConnection();				
		try {				
			String query = "select * from t_nf_chat_buffer where server_id = ? limit 1000 ";			
			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setString(1, Config.getInstance().getServerId());
			
			ResultSet rs = stmt.executeQuery();
			long lastSeq = -1;
			
			while(rs.next()) {
				lastSeq = rs.getLong("chat_buffer_seq");
				
				int pt = rs.getInt("pt");
				int st = rs.getInt("st");
				String contents = rs.getString("contents");
				long buffer_key = rs.getLong("buffer_key");
				String from_server_id = rs.getString("from_server_id");
				String dest_id = rs.getString("dest_id");
				String server_id = rs.getString("server_id");
				
				if(result == null) {
					result = new ArrayList<Packet>();
				}
				Packet packet = new Packet();
				
				packet.setPt(pt);
				packet.setSt(st);
				packet.setParam(contents);
				packet.setBufferKey(buffer_key);
				packet.setDestID(dest_id);
				
				result.add(packet);
			}
			stmt.close();
			
			//占쏙옙占쏙옙占쏙옙 占쏙옙占쏙옙				
			if(lastSeq > -1) {
				query = "DELETE from t_nf_chat_buffer where chat_buffer_seq <= ? ";				
				stmt = connection.prepareStatement(query);
				stmt.setLong(1, lastSeq);
				
				stmt.executeUpdate();
				stmt.close();
			}
		
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		return result;
		*/
		return null;
		
	}	
	
	
	
	/*
	public static void onRecvReplyGoStead(Channel channel, ChatUserContext context
			, String roomIdx, String Status ) {
	
		
        String gsUser = "";
        String reqId = "";
        String status = "";
        
		String myID = context.getId();
		Connection connection = DBConnectionPool.getConnection();				
		try {	
			//챗 占쏙옙 찾占쏙옙..
						
			String query = 
					"select A.chat_room_seq, B.status, B.req_user, B.rcv_user, B.req_user from t_nf_chat_room A left join t_nf_chat_notice B on A.chat_room_seq = B.chat_room_seq and B.msg_type = 0 where A.chat_room_seq = " + roomIdx;
		
			Statement statement = connection.createStatement();
			ResultSet rsRoom = statement.executeQuery(query);
			
			if(rsRoom.next()) {
				status = rsRoom.getString("status");
		        gsUser = rsRoom.getString("rcv_user");
		        reqId = rsRoom.getString("req_user");
			}
			statement.close();
			
			
			if(gsUser.equals(myID) || status.equals("1")) {				
				
				GoSteadyDao.updateGoSteady(Long.valueOf(roomIdx)
						, Integer.valueOf(Status), "");
				
				
					//String firstName = GoSteadyDao.getUserName(recvID);
					String pushMessage = context.getName() + " accept go steady with you.";
					
					//占싯몌옙 占쏙옙占�
					PushKey pushKey = UserDao.getPush(reqId);
					if(pushKey != null) {
						
						Push push = new Push();
						push.setBadge(1);
						push.setOs(pushKey.getOs());
						push.setPushKey(pushKey.getPushKey());
						push.setMsgType(PushType.PT_GO_RES);
						push.setUserid(reqId);
						push.setStatus(0);
						push.setServiceId(PushType.SERVICE_ID);
						push.setPushType(pushKey.getPushType());							
						push.setMsg(pushMessage);
						push.setMsgKey(roomIdx);
						push.setSender(myID);
												
						PushDao.addPush(push);
					}	
					///////////////////////////////////
					Param param = new Param();
					
					param.put("result", "true");
					param.put("resultCode", "0");
					param.put("roomIdx", roomIdx);
					
					ChatServerHandler.sendData(channel, ProtocolType.ptChat
							, ProtocolType.stChatReqReplyGoSteady, param);
					
					
					//占쏙옙占쏙옙占쏙옙占�
					ChatUserContext member = ChatUserContext.getUserContextMap().get(reqId);
					
					if(member != null) {							
						
						Param paramN = new Param();
						paramN.put("roomIdx", roomIdx);
						paramN.put("msgType", "reply");
						
						ChatServerHandler.sendData(member.getChannel(), ProtocolType.ptChat
								, ProtocolType.stChatNoticeGoSteady, paramN);
					}
					
			} else {		
				Param param = new Param();
				
				param.put("result", "false");
				param.put("resultCode", "503");
				param.put("roomIdx", roomIdx);
				
				ChatServerHandler.sendData(channel, ProtocolType.ptChat
						, ProtocolType.stChatReqReplyGoSteady, param);
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}
	
	
	public static void onRecvBreakGoSteady(Channel channel, ChatUserContext context
			, String rcvID ) {
	
	
        boolean isMember = false;
		String roomIdx = "";
		String sendID = context.getId();
		Connection connection = DBConnectionPool.getConnection();				
		try {	
			String query = "select 2 as CNT_ROOM, A.chat_room_seq from t_nf_chat_room A inner join t_nf_chat_member B on A.chat_room_seq = B.chat_room_seq"
					+ " where chat_room_type = 0 and ((B.user_id =  '" + sendID +"' and A.owner_id = '" + rcvID  + "') or (B.user_id = '" + rcvID  + "' and A.owner_id = '" + sendID + "'))"
					+ " order by A.reg_date desc limit 1 ";

			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			if(rs.next()) {
				int CNT_ROOM = rs.getInt(1);
				if(CNT_ROOM == 2) {
					long eIdx = rs.getLong(2);	
					roomIdx = String.valueOf(eIdx);
					isMember = true;
				}
			}
			stmt.close(); 
			
			
			
			if(isMember) {				
				GoSteadyDao.deleteGoSteady(Long.valueOf(roomIdx));				
				
				
				Param param = new Param();
				
				param.put("result", "true");
				param.put("resultCode", "0");
				param.put("roomIdx", roomIdx);
				
				ChatServerHandler.sendData(channel, ProtocolType.ptChat
						, ProtocolType.stChatResBreakGoSteady, param);
				
				//占쏙옙킷 占쏙옙占�
				String pushMessage = context.getName() + " break go steady with you.";
				
				//占싯몌옙 占쏙옙占�
				PushKey pushKey = UserDao.getPush(rcvID);
				if(pushKey != null) {
					
					Push push = new Push();
					push.setBadge(1);
					push.setOs(pushKey.getOs());
					push.setPushKey(pushKey.getPushKey());
					push.setMsgType(PushType.PT_BREAK_GO_REQ);
					push.setUserid(rcvID);
					push.setStatus(0);
					push.setServiceId(PushType.SERVICE_ID);
					push.setPushType(pushKey.getPushType());							
					push.setMsg(pushMessage);
					push.setMsgKey(roomIdx);
					push.setSender(sendID);
											
					PushDao.addPush(push);
				}	
				
				ChatUserContext member = ChatUserContext.getUserContextMap().get(rcvID);
				
				//占쏙옙占쏙옙占쏙옙占�
				if(member != null) {							
					
					Param paramN = new Param();
					paramN.put("roomIdx", roomIdx);
					paramN.put("msgType", "break");
					
					ChatServerHandler.sendData(member.getChannel(), ProtocolType.ptChat
							, ProtocolType.stChatNoticeGoSteady, paramN);
				}
			} else {
				Param param = new Param();
				param.put("result", "false");
				param.put("resultCode", "504");
				param.put("roomIdx", roomIdx);
				
				ChatServerHandler.sendData(channel, ProtocolType.ptChat
						, ProtocolType.stChatResBreakGoSteady, param);
				
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}
	*/
	
	public static void onReqDeleteMsg(Channel channel, ChatUserContext context
			, String msgkey, String msgtype ) {
	
		
		String myID = context.getId();
		boolean existRead = false;
		
		Jedis jedis = RedisClient.getInstance().borrow();				
		try {	
			
			try {
				//이전다.ㄹ
				String pf = RedisClient.getBeforeMsgKeyPrefix();
				String jKey = pf + myID + ":msg";
				String jKey2 = pf + myID + "/msg";
				
				if(context.getMode().equals("2")) {
					jKey = pf + myID + ":pmsg";
					jKey2 = pf + myID + "/pmsg";
				}
									
				if(msgtype.equals("recvmsg")) {
					jedis.hdel(jKey, "rcv_msg:" + msgkey);
					jedis.lrem(jKey2, 0, "rcv_msg:" + msgkey);
				} else if(msgtype.equals("recvR")) {

					boolean deletedata = true;
						
					////////////////
					String data = jedis.hget(jKey, "rcv_msg_r2:" + msgkey);
					if(data != null && data.equals("") == false) {
						String data2 = jedis.hget(jKey, "rcv_msg_r:"+ msgkey);
						if(data.equals(data2) == false) {
							deletedata = false;
						}
					}
					/////////////////
					if(deletedata == true) {
						existRead = true;
						jedis.hdel(jKey, "rcv_msg_r:" + msgkey);
						jedis.lrem(jKey2, 0, "rcv_msg_r:" + msgkey);								
						jedis.hdel(jKey, "rcv_msg_r2:" + msgkey);						
					} else {
						existRead = true;
						jedis.lrem(jKey2, 0, "rcv_msg_r:" + msgkey);
						jedis.rpush(jKey2, "rcv_msg_r:" + msgkey);						
					}
					
					//jedis.hdel(jKey, "rcv_msg_r:" + msgkey);
					//jedis.lrem(jKey2, 0, "rcv_msg_r:" + msgkey);
				} else if(msgtype.equals("recvD")) {
					jedis.hdel(jKey, "rcv_msg_d:" + msgkey);	
					jedis.lrem(jKey2, 0, "rcv_msg_d:" + msgkey);
				}	
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			String pf = RedisClient.getMsgKeyPrefix();
			String jKey = pf + myID + ":msg";
			String jKey2 = pf + myID + "/msg";
			
			if(context.getMode().equals("2")) {
				jKey = pf + myID + ":pmsg";
				jKey2 = pf + myID + "/pmsg";
			}
								
			if(msgtype.equals("recvmsg")) {
				jedis.hdel(jKey, "rcv_msg:" + msgkey);
				jedis.lrem(jKey2, 0, "rcv_msg:" + msgkey);
			} else if(msgtype.equals("recvR")) {
				
				boolean deletedata = true;
					
				////////////////
				String data = jedis.hget(jKey, "rcv_msg_r2:" + msgkey);
				if(data != null && data.equals("") == false) {
					String data2 = jedis.hget(jKey, "rcv_msg_r:"+ msgkey);
					if(data.equals(data2) == false) {
						deletedata = false;
					}
				}
				/////////////////
				if(deletedata == true) {
					existRead = true;
					jedis.hdel(jKey, "rcv_msg_r:" + msgkey);
					jedis.lrem(jKey2, 0, "rcv_msg_r:" + msgkey);								
					jedis.hdel(jKey, "rcv_msg_r2:" + msgkey);						
				} else {
					existRead = true;
					jedis.lrem(jKey2, 0, "rcv_msg_r:" + msgkey);
					jedis.rpush(jKey2, "rcv_msg_r:" + msgkey);						
				}
				
				
				//jedis.hdel(jKey, "rcv_msg_r:" + msgkey);
				//jedis.lrem(jKey2, 0, "rcv_msg_r:" + msgkey);
			} else if(msgtype.equals("recvD")) {
				jedis.hdel(jKey, "rcv_msg_d:" + msgkey);	
				jedis.lrem(jKey2, 0, "rcv_msg_d:" + msgkey);
			}	
			

		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
			
			System.out.println("onReqDeleteMsg : " + e.getMessage());
		} finally {
			try {
				RedisClient.getInstance().revert(jedis);
			} catch (Exception e2) {
				// TODO: handle exception
			}
			
			if(existRead) {
				ChatServerHandler.sendRawData(context.getChannel(), ProtocolType.ptChat
						, ProtocolType.stReqGetMsg, "{}");	
			}
			
		}
	}
	
	

	public static String getRoomIdx(String msgKey) {
		String result = "";
		Connection connection = DBConnectionPool.getConnection();				
		try {			
			String query = "SELECT chat_room_seq FROM t_nf_chat_msg where chat_msg_key = ?";
			
			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setString(1, msgKey);
			
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next()) {
				result =  rs.getString("chat_room_seq");
			}
			stmt.close(); 
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		
		return result;
	}
	

	

	public static String getRoomIdxByGroupID(String groupID) {
		String result = "";
		Connection connection = DBConnectionPool.getConnection();				
		try {			
			String query = "SELECT chat_room_seq FROM t_nf_chat_room where group_seq = ?";
			
			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setString(1, groupID);
			
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next()) {
				result =  rs.getString("chat_room_seq");
			}
			stmt.close(); 
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		
		return result;
	}
	

	public static String getRoomIdxByTarget(String target) {
		String result = "";
		Connection connection = DBConnectionPool.getConnection();				
		try {			
			String query = "SELECT chat_room_seq FROM t_nf_chat_room where target = ?";
			
			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setString(1, target);
			
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next()) {
				result =  rs.getString("chat_room_seq");
			}
			stmt.close(); 
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		
		return result;
	}

	public static LinkedList<ChatRoom> getDefaultRoomList() {
		LinkedList<ChatRoom> result = new LinkedList<ChatRoom>();
		
		Connection connection = DBConnectionPool.getConnection();
		try {
			String query = "" + 
					"SELECT * " +
					"FROM t_nf_chat_room " +
					"WHERE group_type = 2 or group_type = 1 or group_type = 0 ";
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);	
						
			while(rs.next()) {
				ChatRoom chatroom = new ChatRoom();
				chatroom.setChatRoomSeq(rs.getLong("chat_room_seq"));
				chatroom.setChatRoomType(rs.getInt("chat_room_type"));
				chatroom.setOwnerId(rs.getString("owner_id"));
				chatroom.setRegDate(rs.getString("reg_date"));
				chatroom.setLastMsgSeq(rs.getString("last_msg_seq"));
				chatroom.setTitle(rs.getString("title"));
				
				chatroom.setGroup_type(rs.getInt("group_type"));			
				chatroom.setGroup_seq(rs.getInt("group_seq"));	
				
				result.add(chatroom);
					
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}	
		
		return result;
	}	
	

	public static LinkedList<ChatRoom> getShareRoomList() {
		LinkedList<ChatRoom> result = new LinkedList<ChatRoom>();
		
		Connection connection = DBConnectionPool.getConnection();
		try {
			String query = "" + 
					"SELECT * " +
					"FROM t_nf_chat_room " +
					"WHERE group_type = 2 ";
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);	
						
			while(rs.next()) {
				ChatRoom chatroom = new ChatRoom();
				chatroom.setChatRoomSeq(rs.getLong("chat_room_seq"));
				chatroom.setChatRoomType(rs.getInt("chat_room_type"));
				chatroom.setOwnerId(rs.getString("owner_id"));
				chatroom.setRegDate(rs.getString("reg_date"));
				chatroom.setLastMsgSeq(rs.getString("last_msg_seq"));
				chatroom.setTitle(rs.getString("title"));
				
				chatroom.setGroup_type(rs.getInt("group_type"));			
				chatroom.setGroup_seq(rs.getInt("group_seq"));	
				
				result.add(chatroom);
					
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}	
		
		return result;
	}
	

	public static LinkedList<ChatRoom> getReportRoomList() {
		LinkedList<ChatRoom> result = new LinkedList<ChatRoom>();
		
		Connection connection = DBConnectionPool.getConnection();
		try {
			String query = "" + 
					"SELECT * " +
					"FROM t_nf_chat_room " +
					"WHERE group_type = 0 ";
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);	
						
			while(rs.next()) {
				ChatRoom chatroom = new ChatRoom();
				chatroom.setChatRoomSeq(rs.getLong("chat_room_seq"));
				chatroom.setChatRoomType(rs.getInt("chat_room_type"));
				chatroom.setOwnerId(rs.getString("owner_id"));
				chatroom.setRegDate(rs.getString("reg_date"));
				chatroom.setLastMsgSeq(rs.getString("last_msg_seq"));
				chatroom.setTitle(rs.getString("title"));
				
				chatroom.setGroup_type(rs.getInt("group_type"));			
				chatroom.setGroup_seq(rs.getInt("group_seq"));	
				
				result.add(chatroom);
					
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(e.getMessage());
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
