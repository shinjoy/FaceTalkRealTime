package com.nomadsoft.chat.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MsgKey {

	private static final char[] KEY_LIST = { '0', '1', '2', '3', '4', '5', '6',
		'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
		'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
		'x', 'y', 'z' };
	
	private static Random rnd = new Random();
	public static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyyMMddHHmmssSSS");

	private static String getRandomStr() {
		char[] rchar = { KEY_LIST[rnd.nextInt(36)], KEY_LIST[rnd.nextInt(36)],
				KEY_LIST[rnd.nextInt(36)], KEY_LIST[rnd.nextInt(36)],
				KEY_LIST[rnd.nextInt(36)], KEY_LIST[rnd.nextInt(36)], KEY_LIST[rnd.nextInt(36)]
				, KEY_LIST[rnd.nextInt(36)] };
		return String.copyValueOf(rchar);
	}
	
	public static String getMsgKey() {
	    return dateFormat.format(new Date(System.currentTimeMillis())) + getRandomStr();		
	}
	
	
	static long num = 0;
	public static long getUserNumber() {		
	    return ++num;		
	}	
	
	public static String getAnonymityName() {		
	    return "¿Õ∏Ì" + getUserNumber();		
	}		
	
}
