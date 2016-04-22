package com.nomadsoft.chat.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;

import com.nomadsoft.chat.config.Config;
import com.nomadsoft.chat.db.dto.ServerInfo;
import com.nomadsoft.chat.log.Log;
import com.nomadsoft.chat.openServer.ChatUserContext;

public class ServerDao {

	public static ServerInfo getLocalServerInfo(String serverID) {
		ServerInfo result = null;
		
		Connection connection = DBConnectionPool.getConnection();				
		try {
			String query = "select * from t_nf_server where server_id = ? ";
					
					PreparedStatement stmt = connection.prepareStatement(query);
					stmt.setString(1, serverID);
					
					ResultSet rs = stmt.executeQuery();					
					if(rs.next()) {						
						String server_id = rs.getString("server_id");
						String address = rs.getString("address");
						int port = rs.getInt("port");
						int status = rs.getInt("status");
						String auth_code = rs.getString("auth_code");
						
						result= new ServerInfo();
						result.setAddress(address);
						result.setServer_id(server_id);
						result.setPort(port);
						result.setStatus(status);
						result.setAuth_code(auth_code);
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
	
	

	public static ConcurrentHashMap<String, ServerInfo> getRemoteServerList(String serverID) {
		ConcurrentHashMap<String, ServerInfo> result = new ConcurrentHashMap<String, ServerInfo>();
		
		Connection connection = DBConnectionPool.getConnection();				
		try {
			String query = "select * from t_nf_server where server_id <> ? ";
					
					PreparedStatement stmt = connection.prepareStatement(query);
					stmt.setString(1, serverID);
					
					ResultSet rs = stmt.executeQuery();					
					while(rs.next()) {						
						String server_id = rs.getString("server_id");
						String address = rs.getString("address");
						int port = rs.getInt("port");
						int status = rs.getInt("status");
						String auth_code = rs.getString("auth_code");
						
						ServerInfo serverInfo= new ServerInfo();
						serverInfo.setAddress(address);
						serverInfo.setServer_id(server_id);
						serverInfo.setPort(port);
						serverInfo.setStatus(status);
						serverInfo.setAuth_code(auth_code);
						
						result.put(server_id, serverInfo);						
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

	public static int loginCheck(String userId, String authCode, ChatUserContext context, String mode) {
		
		int result = 0;
		
		Connection connection = DBConnectionPool.getConnection();		
		try {
			//이름, 
			String query = "SELECT server_id  FROM t_nf_server WHERE server_id = ? and auth_code = ?";

			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setString(1, userId);
			stmt.setString(2, authCode);
			
			ResultSet rs = stmt.executeQuery();
						
			if(rs.next()) {				
				int userType = 0;				
				String userName = "";
				String userPhone = "";
				
				context.setId(userId);
				context.setAuthCode(authCode);
				context.setName(userName);
				context.setPhone(userPhone);
				context.setType(userType);
				context.setMode(mode);
				result = 1;								
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
	
	public static boolean updateServerInfo(int cntContext, int cntUserContext, int linkServerContext, long readByte, long writeByte) {
		
		boolean result = false;
		
		Connection connection = DBConnectionPool.getConnection();		
		try {
			//이름, 
			String query = "update t_nf_server set connection_cnt=?, context_cnt=?, link_cnt=?, read_bytes=?, written_bytes=?, update_date=getDate() where server_id = ?";

			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setInt(1, cntContext);
			stmt.setInt(2, cntUserContext);
			stmt.setInt(3, linkServerContext);
			stmt.setLong(4, readByte);
			stmt.setLong(5, writeByte);
			
			stmt.setString(6, Config.getInstance().getServerId());

			
			stmt.executeUpdate();			
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
