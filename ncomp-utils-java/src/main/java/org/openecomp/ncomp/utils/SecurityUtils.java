package org.openecomp.ncomp.utils;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class SecurityUtils {
	public static String whiteList(String str, List<String> l, String reason) {
		if (l.contains(str)) {
			return str;
		}
		throw new RuntimeException("String not trusted: " + str + " " + reason);
	}
	public static double inSecureRandom() {
		return (Math.random()*100.0)/100;
	}
	public static long inSecureSeed(long seed) {
		return seed;
	}
	public static String logForcingProtection(Object v) {
		return v.toString().replace("\n", "NEWLINE");
	}
	
	public static File createSafeFile(File dir, String fname) {
		String fname2 = dir.getAbsolutePath() + "/" + fname;
		return new File(safeFileName(fname2));
	}

	public static String safeFileName(String file) {
		// creating file with safer creation. 
		if (file.contains("../")) 
			throw new RuntimeException("File name contain ..: " + file);
		if (file.contains("\n")) 
			throw new RuntimeException("File name contain newline: " + file);
		return file;
	}
	
	public static File safeFile(File file) {
		// creating file with safer creation. 
		if (file.getAbsolutePath().contains("..")) 
			throw new RuntimeException("File name contain ..: " + file.getAbsolutePath());
		return file;
	}
	public static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new RuntimeException("HOSTNAME-UNKNOWN");
		}
	}
	public static String getHostAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new RuntimeException("HOSTNAME-UNKNOWN");
		}
	}
	public static String getCanonicalHostName() {
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new RuntimeException("HOSTNAME-UNKNOWN");
		}
	}


}
