
/*-
 * ============LICENSE_START==========================================
 * OPENECOMP - DCAE
 * ===================================================================
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 */
	
package org.openecomp.ncomp.utils;

import static org.openecomp.ncomp.utils.Base64.decode64;
import static org.openecomp.ncomp.utils.Base64.encode64;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import org.openecomp.ncomp.webservice.utils.FileUtils;

public class CryptoUtils {
	public static final Logger logger = Logger.getLogger(CryptoUtils.class);

	public enum EncryptionType {
		NONE, ENCRYPT, DECRYPT
	};

	public static String genNewKey() {
		return UUID.randomUUID().toString();
	}

	public static String encryptPublic(String key, String value) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			KeySpec keySpec = new X509EncodedKeySpec(decode64(key));
			PublicKey key1 = keyFactory.generatePublic(keySpec);
			Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			rsa.init(Cipher.ENCRYPT_MODE, key1);
			return encode64(rsa.doFinal(value.getBytes()));
		} catch (Exception e) {
			throw new RuntimeException("encryption failed:" + e);
		}
	}

	public static String decryptPrivate(String key, String value) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			KeySpec keySpec = new PKCS8EncodedKeySpec(decode64(key));
			PrivateKey key1 = keyFactory.generatePrivate(keySpec);
			Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			rsa.init(Cipher.DECRYPT_MODE, key1);
			return new String(rsa.doFinal(decode64(value)));
		} catch (Exception e) {
			throw new RuntimeException("encryption failed:" + e);
		}

	}

	public static InputStream getInputStream(final InputStream in, final EncryptionType type, final String key) {
		final Cipher aes;
		logger.debug("crypto in stream:" + PropertyUtil.replaceForLogForcingProtection(type) + " " + PropertyUtil.replaceForLogForcingProtection(key));
		try {
			aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
			switch (type) {
			case DECRYPT:
				aes.init(Cipher.DECRYPT_MODE, string2key(key));
				break;
			case ENCRYPT:
				aes.init(Cipher.ENCRYPT_MODE, string2key(key));
				break;
			default:
				break;
			}
			return new CipherInputStream(in, aes);
		} catch (Exception e) {
			throw new RuntimeException("encryption failed:" + e);
		}
	}

	public static OutputStream getOutputStream(final OutputStream out, final EncryptionType type, final String key) {
		final Cipher aes;
		logger.debug("crypto out stream:" + type + " " + key);
		try {
			aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
			switch (type) {
			case DECRYPT:
				aes.init(Cipher.DECRYPT_MODE, string2key(key));
				break;
			case ENCRYPT:
				aes.init(Cipher.ENCRYPT_MODE, string2key(key));
				break;
			default:
				break;
			}
			return new CipherOutputStream(out, aes);
		} catch (Exception e) {
			throw new RuntimeException("encryption failed:" + e);
		}
	}

	private static SecretKeySpec string2key(String key) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA");
		digest.update(key.getBytes());
		return new SecretKeySpec(digest.digest(), 0, 16, "AES");
	}

	public static String getKey(String fileName) {
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		InputStream in = null;
		try {
			in = new FileInputStream(FileUtils.safeFileName(fileName));
			FileUtils.copyStream(in, o);
		} catch (IOException e) {
			throw new RuntimeException("getKey failed:" + e);
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return o.toString();
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		String command = "createKeyPair";
		if (args.length > 0)
			command = args[0];
		if (command.equals("createKeyPair")) {
			String key = (args.length <= 1) ? "key" : args[1];
			createKeyPair(key);
		}
		if (command.equals("file")) {
			EncryptionType t = EncryptionType.valueOf(args[1].toUpperCase());
			InputStream in = new FileInputStream(FileUtils.safeFileName(args[2]));
			OutputStream out = new FileOutputStream(FileUtils.safeFileName(args[3]));
			try {
				in = getInputStream(in, t, args[4]);
				FileUtils.copyStream(in, out);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			}
		}
	}

	public static String digest(byte[] a) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA");
		return encode64(digest.digest(a));
	}

	public static void createKeyPair(String key) throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(FileUtils.safeFileName(key + ".private"));
			out.write(encode64(privateKey.getEncoded()).getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (out != null)
				out.close();
		}
		try {
			out = new FileOutputStream(FileUtils.safeFileName(key + ".public"));
			out.write(encode64(publicKey.getEncoded()).getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (out != null)
				out.close();
		}
		System.out.println("Create private file: " + key + ".private " + digest(privateKey.getEncoded()));
		System.out.println("Create public file: " + key + ".public " + digest(publicKey.getEncoded()));
	}

	public static String encrypt(String key, String value) {
		logger.debug("encrypt: " + key + " " + value);
		try {
			Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
			aes.init(Cipher.ENCRYPT_MODE, string2key(key));
			return encode64(aes.doFinal(value.getBytes()));
		} catch (Exception e) {
			throw new RuntimeException("encrypt failed:" + e);
		}
	}

	public static String decrypt(String key, String value) {
		logger.debug("decrypt: " + key + " " + value);
		try {
			Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
			aes.init(Cipher.DECRYPT_MODE, string2key(key));
			return new String(aes.doFinal(decode64(value)));
		} catch (Exception e) {
			throw new RuntimeException("decrypt failed:" + e);
		}
	}

	public static String digestFile(String filename) throws Exception {
		InputStream fis = null;
		MessageDigest complete = null;
		try {
			fis = new FileInputStream(FileUtils.safeFileName(filename));
			byte[] buffer = new byte[1024];
			complete = MessageDigest.getInstance("MD5");
			int numRead;
			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (fis != null)
				fis.close();
		}
		return encode64(complete.digest());
	}

}
