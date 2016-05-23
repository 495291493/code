package com.clschina.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Encryption {
	private static Log log = LogFactory.getLog(Encryption.class);

	private static final String ALGORITHM = "DES";
	private byte[] key;
	private static Encryption instance;

	private Encryption() {
		Security.addProvider(new com.sun.crypto.provider.SunJCE());
		try {
			InputStream is = getClass().getResourceAsStream(
					"Encryption.properties");
			Properties prop = new Properties();
			prop.load(is);
			byte[] k2 = prop.get("pwd").toString().getBytes();
			if(k2.length < 8){
				throw new Exception("invalid key");
			}
			key = new byte[8];
			System.arraycopy(k2, 0, key, 0, 8);
		} catch (Exception ex) {
			log.error("ex1 in Encryption.java:" + ex.toString(), ex);
		}
	}

	// 生成密钥, 注意此步骤时间比较长
	public byte[] getKey() throws Exception {
		KeyGenerator keygen = KeyGenerator.getInstance(ALGORITHM);
		SecretKey deskey = keygen.generateKey();
		return deskey.getEncoded();
	}

	// 加密
	public byte[] encode(byte[] input) throws Exception {
		SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, ALGORITHM);
		Cipher c1 = Cipher.getInstance(ALGORITHM);
		c1.init(Cipher.ENCRYPT_MODE, deskey);
		byte[] cipherByte = c1.doFinal(input);
		return cipherByte;
	}

	// 解密
	public byte[] decode(byte[] input) throws Exception {
		SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, ALGORITHM);
		Cipher c1 = Cipher.getInstance(ALGORITHM);
		c1.init(Cipher.DECRYPT_MODE, deskey);
		byte[] clearByte = c1.doFinal(input);
		return clearByte;
	}

	public String base64Encode(byte[] bin) {
		return (new sun.misc.BASE64Encoder()).encode(bin);
	}

	public byte[] base64Decode(String s) throws IOException {
		return (new sun.misc.BASE64Decoder()).decodeBuffer(s);
	}

	/**
	 * 加密字符串
	 * 
	 * @param plainText
	 * @return
	 * @throws Exception
	 */
	public static String encodeString(String plainText) throws Exception {
		Encryption inst = getInstance();
		return inst.base64Encode(inst.encode(plainText.getBytes()));
	}

	/**
	 * 解密字符串
	 * 
	 * @param encryptedString
	 * @return
	 * @throws Exception
	 */
	public static String decodeString(String encryptedString) throws Exception {
		Encryption inst = getInstance();
		return new String(inst.decode(inst.base64Decode(encryptedString)));
	}

	public static Encryption getInstance() {
		if (instance == null) {
			instance = new Encryption();
		}
		return instance;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("Hello World! " + encodeString("Heelllooo"));
	}
	
}
