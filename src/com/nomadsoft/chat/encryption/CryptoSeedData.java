package com.nomadsoft.chat.encryption;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.DatatypeConverter;


public class CryptoSeedData {

	static byte pbUserKey[]  = {(byte)0x33, (byte)0x55, (byte)0x34, (byte)0x78, (byte)0x23, (byte)0x45, (byte)0x76, (byte)0x54, (byte)0x67, (byte)0xF4, (byte)0x92, (byte)0xD4, (byte)0x7C, (byte)0xB2, (byte)0xC8, (byte)0xF8};
	static byte bszIV[] = {
			(byte)0x036, (byte)0x03d, (byte)0x086, (byte)0x0c8,
			(byte)0x025, (byte)0x0c3, (byte)0x03a, (byte)0x023,
			(byte)0x01a, (byte)0x06a, (byte)0x0d9, (byte)0x0fb,
			(byte)0x015, (byte)0x01d, (byte)0x015, (byte)0x0b7
	};
	


	public static String encrypt(String value) {		
		byte pbData[] = null;
		try {
			pbData = value.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
				
	    byte[] defaultCipherText = KISA_SEED_CBC.SEED_CBC_Encrypt(pbUserKey, bszIV, pbData, 0, pbData.length);	    
	    return DatatypeConverter.printBase64Binary(defaultCipherText);
	}
	
	
	public static String decrypt(String value) {		
		
	    byte[] defaultCipherText = DatatypeConverter.parseBase64Binary(value);	    	    
	    byte[] PPPPP = KISA_SEED_CBC.SEED_CBC_Decrypt(pbUserKey, bszIV, defaultCipherText, 0, defaultCipherText.length);
	    
	    String sample = "";
		try {
			sample = new String(PPPPP, 0, PPPPP.length, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return sample;
	}	
	
}
