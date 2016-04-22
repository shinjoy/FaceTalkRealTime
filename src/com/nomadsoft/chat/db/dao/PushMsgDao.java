package com.nomadsoft.chat.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.google.android.gcm.server.Sender;
import com.nomadsoft.chat.config.Config;
import com.nomadsoft.chat.db.dto.PushService;
import com.nomadsoft.chat.log.Log;
import com.nomadsoft.util.push.PushNotification;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

public class PushMsgDao {
	
	public static void loadPushService(ConcurrentHashMap<String, PushService> serviceList ) {

		Connection connection = DBConnectionPool.getConnection();				
		try {				
			String query = "select service_id, gcm_pkg_id, gcm_api_key, apns_production, apns_path, apns_pass  from T_PUSH_SERVICE"; 
						
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);	
			
			while(rs.next()) {						
				String service_id = rs.getString(1);
				String gcm_pkg_id = rs.getString(2); 
				String gcm_api_key = rs.getString(3); 
				int apns_production = rs.getInt(4);
				String apns_path = rs.getString(5); 
				String apns_pass = rs.getString(6);
				//apns_path = "C:/push/loveqast/loveqast_sandbox.p12";
				
				
				PushService pushService = new PushService();
				pushService.setService_id(service_id);
				pushService.setGcm_pkg_id(gcm_pkg_id);
				pushService.setGcm_api_key(gcm_api_key);
				pushService.setApns_production(apns_production);
				pushService.setApns_path(apns_path);
				pushService.setApns_pass(apns_pass);
								
				ApnsService service = null;
				try {
					if(apns_production == 1) {
						
							service =
								    APNS.newService()
								    .withCert(apns_path
								    		, apns_pass)
								    .withProductionDestination()
								    .build();	
							
					} else {
						service =
							    APNS.newService()
							    .withCert(apns_path
							    		, apns_pass)
							    .withSandboxDestination()
							    .build();	
					}	
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				Sender sender = null;
				if(gcm_api_key != null && gcm_api_key.equals("") == false) {
					sender = new Sender(gcm_api_key);
				}
				
				pushService.setApnsService(service);
				pushService.setGcmSender(sender);
				
				serviceList.put(service_id, pushService);				
			}
			rs.close();
			statement.close();
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
	
	public static void checkNotice() {
		
		Connection connection = DBConnectionPool.getConnection();				
		try {				
			String query = "select top 800 seq, os, push_Key, msg, msg_type, msg_key, push_type, userId, badge, status, reg_date, service_id, sender "
					+ " from T_PUSH where status = 0 order by reg_date desc"; 
			
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);	
			
			Statement stmt = connection.createStatement();
			while(rs.next()) {
					
				long seq = rs.getLong(1);	
				String os = ChatDBhelper.getSafeString(rs.getString(2));
				String push_Key = ChatDBhelper.getSafeString(rs.getString(3));
				String msg = ChatDBhelper.getSafeString(rs.getString(4));
				String msg_type = ChatDBhelper.getSafeString(rs.getString(5));
				String msg_key = ChatDBhelper.getSafeString(rs.getString(6));
				int push_type = rs.getInt(7);
				String userId = ChatDBhelper.getSafeString(rs.getString(8));
				int badge = rs.getInt(9);
				int status = rs.getInt(10);
				String service_id = ChatDBhelper.getSafeString(rs.getString(12));
				String sender = ChatDBhelper.getSafeString(rs.getString(13));
				
				String sql = "update T_PUSH set status = 1 where seq = " + seq;
				stmt.execute(sql);
     							
				PushNotification.sendPush(false, userId, msg, push_Key, msg_type
						, msg_key, push_type, os, sender ,"", badge, service_id, seq);				
			}
			stmt.close();
			statement.close();
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
	
	

	public static void backupData(long seq, int status) {
		
		Connection connection = DBConnectionPool.getConnection();				
		try {		
			Statement stmt = connection.createStatement();
			String sql = "update T_PUSH set status = " + status + " where seq = " + seq;
			stmt.execute(sql);
						
			sql = "insert into T_PUSH_BACKUP select * from T_PUSH where seq = " + seq;
			stmt.execute(sql);
			
			sql = "delete from T_PUSH where seq = " + seq;
			stmt.execute(sql);			
			
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
	}	
	
	
}
