package com.nomadsoft.chat.utility;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IntByteConvert {

		/*
	byte[] toByteArray(int value) {
	     return  ByteBuffer.allocate(4).putInt(value).array();
	}
	*/


	public static byte[] toByteArray(int value) {
	    return new byte[] {
	        (byte) ((value>>0) & 0xff) ,
	        (byte) ((value>>8) & 0xff),
	        (byte) ((value>>16) & 0xff),
	        (byte) ((value>>24) & 0xff)};
	}

	/*
	int fromByteArray(byte[] bytes) {
	     return ByteBuffer.wrap(bytes).getInt();
	}
	 */
	public static int fromByteArray(byte[] bytes) {
	     return bytes[0] & 0xff  | (bytes[1] & 0xFF) << 8 | (bytes[2] & 0xFF) << 16 | (bytes[3] & 0xFF) << 24;
	}
	
	
	
	public static int fromByte(byte byte1, byte byte2, byte byte3, byte byte4 ) {
	     return byte1 & 0xff   | (byte2 & 0xFF) << 8 | (byte3 & 0xFF) << 16 | (byte4 & 0xFF) << 24;
	}
}
