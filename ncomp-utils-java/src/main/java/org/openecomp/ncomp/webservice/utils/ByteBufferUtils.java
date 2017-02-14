
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
	
package org.openecomp.ncomp.webservice.utils;

import java.nio.ByteBuffer;
import java.util.Date;

public class ByteBufferUtils {

	private static boolean debug = false;

	public static void getUnixDate(ByteBuffer buf, Date date, char c) {
		date.setTime(1000*getLong(buf));
	}
	public static void getUnixDateWithMilliSeconds(ByteBuffer buf, Date date, char c) {
		date.setTime(1000*getLong(buf));
		getLong(buf);
		if (debug ) System.out.println ( "getUnixDateWithMilliSeconds: " + date);
	}

	public static int getInt(ByteBuffer buf) {
		// TODO Auto-generated method stub
		return (int) getLong(buf);
	}

	public static long getLong(ByteBuffer buf) {
		// TODO Auto-generated method stub
		int i = 0;
		byte c = buf.get(buf.position());
		if (c=='-') {
			buf.get();
			return -getLong(buf);
		}
		for (c=buf.get();c>='0'&&c<='9';c=buf.get()) {
			i = 10*i+c - '0';
		}
//		if (debug ) System.out.println ( "getLong: " + i);
		return i;
	}

	public static void forward(ByteBuffer buf, char c) {
		while (true) {
			byte cc = buf.get();
			// if (debug ) System.out.println ( "forward cc c:" + cc + " " + (byte) c + " " + (char) cc);
			if (cc == c) break;
		}
	}

	public static int getIpInt(ByteBuffer buf) {
		int ip;
		ip = getInt(buf);
		ip = 256 * ip + getInt(buf);
		ip = 256 * ip + getInt(buf);
		ip = 256 * ip + getInt(buf);
		if (debug ) System.out.println ( "getIpInt: " + IpUtils.toString(ip));
		return ip;
	}

	public static StringBuffer getBuffer(ByteBuffer buf, char c) {
		// TODO Auto-generated method stub
		StringBuffer sbuf = new StringBuffer();
		while (true) {
			byte cc = buf.get();
			// if (debug ) System.out.println ( "cc c:" + cc + " " + (byte) c + " " + (char) cc);
			if (cc == c) break;
			sbuf.append((char)cc);
		}
		if (debug ) System.out.println ( "getBuffer: " + sbuf);
		return sbuf;
	}
	public static int mask2masklen(ByteBuffer buf) {
		// 255.255.255.252 -> 30
		int len = 0;
		for (int j = 0; j < 4; j++) {
			int i = getInt(buf);
			for (int x = 0; x < 8; x++) {
				if ((i >> x) % 2 == 1) {
					len += 8 - x;
					break;
				}

			}
		}
		return len;
	}


}
