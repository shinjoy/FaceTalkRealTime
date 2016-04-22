package com.nomadsoft.chat.encryption;


public class CryptoSeedNet {

	static byte pbUserKey[]  = {(byte)0x45, (byte)0x22, (byte)0x34, (byte)0x55, (byte)0x45, (byte)0x23, (byte)0x97, (byte)0x4f, (byte)0xbc, (byte)0x44, (byte)0x33, (byte)0xD2, (byte)0x32, (byte)0x98, (byte)0x31, (byte)0x89};
	static byte bszIV[] = {
			(byte)0x023, (byte)0x05d, (byte)0x022, (byte)0x0a8,
			(byte)0x0f2, (byte)0x0c8, (byte)0x08a, (byte)0x083,
			(byte)0x03f, (byte)0x0bc, (byte)0x0d7, (byte)0x0fa,
			(byte)0x045, (byte)0x082, (byte)0x011, (byte)0x033
	};
	
	public static byte[] encrypt(byte[] pbData) {				
	    return KISA_SEED_CBC.SEED_CBC_Encrypt(pbUserKey, bszIV, pbData, 0, pbData.length);	    
	}
	
	
	public static byte[] decrypt(byte[] defaultCipherText) {			    	    
	    return KISA_SEED_CBC.SEED_CBC_Decrypt(pbUserKey, bszIV, defaultCipherText, 0, defaultCipherText.length);	    
	}		
}
