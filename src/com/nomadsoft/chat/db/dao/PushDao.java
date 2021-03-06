package com.nomadsoft.chat.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.nomadsoft.chat.db.dto.Push;

public class PushDao {


	public static void addPush(Push push) {
		
		Connection connection = DBConnectionPool.getConnection();		
		try {
			 
			String query = "" +
					"INSERT INTO T_PUSH " +
					"	(os, push_Key, msg, msg_type, msg_key, push_type, userId, badge, status, reg_date, service_id, sender) " +
					"VALUES " +
					"	(?, ?, ?, ?, ?, ?, ?, ?, ?, getDate(), ?, ?) ";

			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setString(1, push.getOs());
			stmt.setString(2, push.getPushKey());
			stmt.setString(3, push.getMsg());
			stmt.setString(4, push.getMsgType());
			stmt.setString(5, push.getMsgKey());
			stmt.setInt(6, push.getPushType());
			stmt.setString(7, push.getUserid());
			stmt.setInt(8, push.getBadge());
			stmt.setInt(9, push.getStatus());
			stmt.setString(10, push.getServiceId());
			stmt.setString(11, push.getSender());
			
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
	}
	
}
