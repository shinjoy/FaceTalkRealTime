package com.nomadsoft.chat.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoNew {
	private static byte[] getRawKeyBytes() {
		String key = "98hytgfmxra3205l";		
		
		byte[] keyBytes = new byte[16];
		byte[] b = key.getBytes();
		int len = b.length;
		if (len > keyBytes.length)
			len = keyBytes.length;
		System.arraycopy(b, 0, keyBytes, 0, len);

		return keyBytes;
	}

	public static byte[] encrypt(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//

		byte[] keyBytes = getRawKeyBytes();
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);

		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

		return cipher.doFinal(data);
	}

	public static byte[] decrypt(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

		byte[] keyBytes = getRawKeyBytes();

		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

		return cipher.doFinal(data);
	}
}
