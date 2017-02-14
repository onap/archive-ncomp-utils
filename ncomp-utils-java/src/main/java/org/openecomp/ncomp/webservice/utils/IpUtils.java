
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class IpUtils {
	public static long toLong(String Ip) {
		long res = 0;
		try {
			StringTokenizer tokens = new StringTokenizer(Ip, ".");
			for (int i = 0; i < 4; i++) {
				int t = Integer.parseInt(tokens.nextToken());
				if (t < 0 || t > 255) {
					throw new RuntimeException("Bad IP: " + Ip);
				}
				res = 256 * res + t;
			}
		} catch (Exception e) {
			throw new RuntimeException("Bad IP: " + Ip);
		}
		return res;
	}

	public static int toInt(String Ip) {
		long x = toLong(Ip);
		// if (x<(1<<31)) return (int) x;
		// TODO: does this really work??
		// System.out.println(Ip + " " + x);
		return (int) x;
	}

	public static String toString(int i) {
		return ((i >> 24) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + (i & 0xFF);
	}

	public static int mask2masklen(String string) {
		// 255.255.255.252 -> 30
 		if (!isIp(string)) {
			return Integer.parseInt(string);
		}
 		if (isIpv6(string)) {
 			throw new RuntimeException("IPv6 is not supported, just use mask length");
 		} 
		StringTokenizer st = new StringTokenizer(string,".");
		int len = 0;
		for (int j = 0; j < 4; j++) {
			int i = Integer.parseInt(st.nextToken());
			for (int x = 0; x < 8; x++) {
				if ((i >> x) % 2 == 1) {
					len += 8 - x;
					break;
				}

			}
		}
		return len;
	}

	private static int[] maskvals = {0,32,31,6,30,9,5,-1,29,16,8,2,4,21,-1,19,28,
		25,15,-1,7,10,1,17,3,22,20,26,-1,11,18,23,27,12,24,13,14};
	
	public static int mask2masklen (int ip) {
		// This works because 2 is a primitive root mod 37!
		// The negation is because % is remainder, not modulus
		int indx = -(ip % 37);
		if (indx < 0) return -1; // caution for non-masks
		return maskvals[indx];
	}

	public static String toPrefixString(int ip, int len) {
		ip = (ip >> (32-len)) << (32-len);
		return IpUtils.toString(ip) + "/" + len;
	}

	private static Pattern ipv4Pattern = Pattern.compile("\\d*\\.\\d*\\.\\d*\\.\\d*");
	public static boolean isIpv4(String to) {
		if (to.isEmpty()) return false;
		char c = to.charAt(0);
		if (c > '9' || c < '0') return false;
		return ipv4Pattern.matcher(to).matches();
	}
	private static Pattern ipv6Pattern = Pattern.compile("[0-9a-fA-F]*:[0-9a-fA-F]*:[0-9a-fA-F:]*");
	public static boolean isIpv6(String to) {
		// bad heuristic but likely works most of the time.
		return ipv6Pattern.matcher(to).matches();
	}

	public static boolean isIp(String to) {
		return isIpv6(to) ||  isIpv4(to);
	}
	static int numBad = 0;
	public static InetAddress toInetAddress(String string) {
		if (!isIp(string)) return null;
		try {
			return InetAddress.getByName(string);
		} catch (UnknownHostException e) {
			if (numBad > 1000) {
				e.printStackTrace();
				// We exit here since it is important not to do lots of DNS lookups
				// We should validate that the string is a valid IP somehow.
				System.exit(-3);
			}
		}
		return null;
	}

	public static String toPrefixString(InetAddress ip, int len) {
		return mask(ip, len).getHostAddress() + "/" + len;
	}
	public static String toString(InetAddress ip) {
		return ip.getHostAddress();
	}
	public static InetAddress mask(InetAddress ip, int prefixMask) {
        int oddBits = prefixMask % 8;
        int nMaskBytes = prefixMask/8 + (oddBits == 0 ? 0 : 1);
        byte[] mask = new byte[nMaskBytes];
//        byte[] addr = Arrays.copyOf(ip.getAddress(), ip.getAddress().length); 
//        To make this Java 5 compactible.
        byte[] addr = new byte[ip.getAddress().length];
        for (int i =0 ; i < ip.getAddress().length ; i ++) 
        	addr[i]=ip.getAddress()[i];
        Arrays.fill(mask, 0, oddBits == 0 ? mask.length : mask.length - 1, (byte)0xFF);
        if (oddBits != 0) {
            int finalByte = (1 << oddBits) - 1;
            finalByte <<= 8-oddBits;
            mask[mask.length - 1] = (byte) finalByte;
        }
        for (int i=0; i < mask.length; i++) {
            addr[i] = (byte) (addr[i] & mask[i]);
        }
        for (int i=nMaskBytes; i < addr.length; i++) {
            addr[i] = 0;
        }
        try {
			return InetAddress.getByAddress(addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(2);
		}
		return null;
	}

	public static int mask (int ip, int masklen) {
		if (masklen == 0) return 0;
		return ip & -(1 << (32-masklen));
	}
	
	public static String defaultPrefix(String tmVersion) {
		if (tmVersion.equals("6")) {
			return "::/0";
		}
		return "0.0.0.0/0";
	}

	public static boolean containedIn(InetAddress ip, InetAddress ip2, int mask) {
		return (mask(ip, mask).equals(mask(ip2, mask)));
	}

	public static InetAddress anonymize(InetAddress ip) {
		byte[] a = ip.getAddress();
		byte[] a2 = Arrays.copyOf(a, a.length);
		a2[a.length-1] = (byte) (a2[a.length-1] ^ 17);
		try {
			return InetAddress.getByAddress(a2);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(3);
		}
		return null;
	}

	public static boolean isIpv4(InetAddress ip) {
		return ip.getAddress().length == 4;
	}
	
	public static String firstIp(String prefix) {
		String a[] = prefix.split("/");
		int ip = toInt(a[0]);
		int len = Integer.parseInt(a[1]);
		ip = mask(ip,len);
		return toString(ip);
	}

	public static String lastIp(String prefix) {
		String a[] = prefix.split("/");
		int ip = toInt(a[0]);
		int len = Integer.parseInt(a[1]);
		if (len == 0) return "255.255.255.255";
		ip = mask(ip,len) + (1 << (32-len)) - 1;
		return toString(ip);
	}

}
