
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
import static org.openecomp.ncomp.utils.CryptoUtils.createKeyPair;
import static org.openecomp.ncomp.utils.CryptoUtils.decrypt;
import static org.openecomp.ncomp.utils.CryptoUtils.decryptPrivate;
import static org.openecomp.ncomp.utils.CryptoUtils.digest;
import static org.openecomp.ncomp.utils.CryptoUtils.digestFile;
import static org.openecomp.ncomp.utils.CryptoUtils.encrypt;
import static org.openecomp.ncomp.utils.CryptoUtils.encryptPublic;
import static org.openecomp.ncomp.utils.CryptoUtils.getInputStream;
import static org.openecomp.ncomp.utils.CryptoUtils.getKey;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.TestCase;

import org.openecomp.ncomp.utils.CryptoUtils.EncryptionType;
import org.openecomp.ncomp.webservice.utils.FileUtils;



public class CryptoUtilsTest extends TestCase {
	String k = "dafdfkj";
	String v = "Hello";

    public void test_encrypt() {
    	assertEquals(v, decrypt(k,encrypt(k, v)));
    }
    public void test_streams() throws Exception {
		Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
		MessageDigest digest = MessageDigest.getInstance("SHA");
		digest.update("foobar".getBytes());
		SecretKeySpec key1 = new SecretKeySpec(digest.digest(), 0, 16, "AES");
		aes.init(Cipher.ENCRYPT_MODE, key1);
		InputStream in = new FileInputStream("test/Test.txt");
		in = new CipherInputStream(in, aes);
		FileOutputStream out = new FileOutputStream("test/Encrypted.txt");
		try {
			FileUtils.copyStream(in, out);
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
		aes.init(Cipher.DECRYPT_MODE, key1);
		in = new FileInputStream("test/Encrypted.txt");
		in = new CipherInputStream(in, aes);
		out = new FileOutputStream("test/Decrypted.txt");
		try {
			FileUtils.copyStream(in, out);
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
		assertEquals(digestFile("test/Test.txt"), digestFile("test/Decrypted.txt"));
	}
    @SuppressWarnings("resource")
	public void test_streams_2() throws Exception {
    	InputStream in = new FileInputStream("test/Test.txt");
    	in = getInputStream(in, EncryptionType.ENCRYPT, k);
    	FileOutputStream out = new FileOutputStream("test/Encrypted.txt");
		try {
			FileUtils.copyStream(in, out);
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
    	in = new FileInputStream("test/Encrypted.txt");
    	in = getInputStream(in, EncryptionType.DECRYPT, k);
    	out = new FileOutputStream("test/Decrypted.txt");
		try {
			FileUtils.copyStream(in, out);
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
		assertEquals(digestFile("test/Test.txt"), digestFile("test/Decrypted.txt"));
    }
    public void test_public_key() throws Exception {
    	KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    	KeyPair keyPair = keyPairGenerator.generateKeyPair();
		Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		rsa.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
		byte[] ciphertext = rsa.doFinal(v.getBytes());
		rsa.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
		byte[] text = rsa.doFinal(ciphertext);
		assertEquals(v, new String(text));
    }
    
    public void test_public_key_1() throws Exception {
    	createKeyPair("test/key");
    	String publicKey = getKey("test/key.public");
    	System.out.println(digest(decode64(publicKey)));
    	String privateKey = getKey("test/key.private");
    	System.out.println(digest(decode64(privateKey)));
    	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    	PublicKey k1 = keyFactory.generatePublic(new X509EncodedKeySpec(decode64(publicKey)));
    	PrivateKey k2 = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decode64(privateKey)));
    	Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    	rsa.init(Cipher.ENCRYPT_MODE, k1);
		byte[] ciphertext = rsa.doFinal(v.getBytes());
		rsa.init(Cipher.DECRYPT_MODE, k2);
		byte[] text = rsa.doFinal(ciphertext);
		assertEquals(v, new String(text));

    }

    public void test_public_key_2() throws Exception {
    	createKeyPair("test/key");
    	String publicKey = getKey("test/key.public");
    	System.out.println(digest(decode64(publicKey)));
    	String privateKey = getKey("test/key.private");
    	System.out.println(digest(decode64(privateKey)));
    	assertEquals(v, decryptPrivate(privateKey,encryptPublic(publicKey, v)));
    }

}
