package com.nomadsoft.chat.db.dao;

public class ChatDBhelper {
	public static String getSafeString(String value) {
		if(value == null) {
			value = "";
		}
		return value;
	}
	
	
	public static int getSafeInt(String value) {
		int result = 0;
		
		if(value == null || value.equals("")) {
			result = 0;
		} else {
			result = Integer.valueOf(value);
		}
		
		return result;
	}
}
