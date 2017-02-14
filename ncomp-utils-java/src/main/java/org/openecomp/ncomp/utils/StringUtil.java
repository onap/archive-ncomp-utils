
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import org.openecomp.ncomp.webservice.utils.JsonUtils;

public class StringUtil {
	public static final Logger logger = Logger.getLogger(StringUtil.class);

	public static String join(final Collection<?> l, String delim) {
		boolean first = true;
		StringBuffer buf = new StringBuffer();
		for (Object o : l) {
			if (!first)
				buf.append(delim);
			else
				first = false;
			buf.append(o);
		}
		return buf.toString();
	}

	public static String join(Object[] v, String delim) {
		return join(Arrays.asList(v), delim);
	}

	public static String capitalize(String s) {
		if (s.length() == 0)
			return s;
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static double parseTime(String s) {
		s = s.trim();
		Scanner scan = new Scanner(s);
		double d = 0;
		if (scan.findInLine("^(\\d+):(\\d+\\.\\d+)$") != null) {
			MatchResult result = scan.match();
			int m = Integer.parseInt(result.group(1));
			d = Double.parseDouble(result.group(2));
			scan.close();
			return d + 60 * m;
		}
		if (scan.findInLine("^(\\d+):(\\d+):(\\d+\\.\\d+)$") != null) {
			MatchResult result = scan.match();
			int h = Integer.parseInt(result.group(1));
			int m = Integer.parseInt(result.group(2));
			d = Double.parseDouble(result.group(3));
			scan.close();
			return d + 60 * m + 3600 * h;
		}
		if (scan.findInLine("^(\\d+)\\.(\\d+)$") != null) {
			scan.close();
			return Double.parseDouble(scan.match().group(1));
		}
		logger.error("parseTime: bad time string str=" + s);
		scan.close();
		return d;
	}

	public static String[] split(String s, String m) {
		if (s.length() == 0) {
			String v[] = {};
			return v;
		}
		return s.split(m);
	}
	
	public static String httpHeaderEncode(String str) {
		String s = "";
		try {
			s = URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return s;
	}
	public static String httpHeaderDecode(String str) {
		String s = "";
		try {
			s = URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return s;
	}

	public static String expandUsingProperties(String string, Properties props, String prefix) {
		// replace ${FOOO} with property value.
		String x = prefix.equals("$") ? "\\" : ""; 
		StringBuffer s;
		String k = null, k1 = null;
		try {
			Pattern p = Pattern.compile("("+x +prefix +"\\{.*?})");
			Matcher m = p.matcher(string);
			s = new StringBuffer();
			while (m.find()) {
				k = m.group(1);
				k1 = k.substring(2,k.length()-1);
				if (props.containsKey(k1)) 
					m.appendReplacement(s,props.getProperty(k1));
				else 
					m.appendReplacement(s, x+k);
			}
			m.appendTail(s);
			return s.toString();
		} catch (IllegalArgumentException e) {
			logger.debug("expand failed: " + string + " " + e + " " + k + " " + k1 + " " + props.getProperty(k1,"NULL"));
		} catch (Exception e) {
			logger.warn("expand failed: " + string + " " + e);
		}
		return string;
	}
	public static String expandUsingMap(String string, HashMap<String, String> map, String prefix) {
		// replace ${FOOO} with property value.
		String x = prefix.equals("$") ? "\\" : ""; 
		Pattern p = Pattern.compile("("+x +prefix +"\\{.*?})");
		Matcher m = p.matcher(string);
		StringBuffer s = new StringBuffer();
		while (m.find()) {
			String k = m.group(1);
			String k1 = k.substring(2,k.length()-1);
			if (map.containsKey(k1)) 
				m.appendReplacement(s,map.get(k1));
			else 
				m.appendReplacement(s, x+k);
		}
		m.appendTail(s);
		return s.toString();
	}
	public static String expandUsingJson(String string, JSONObject json, String prefix) {
		// replace ${FOOO} with JSON string value.
		String x = prefix.equals("$") ? "\\" : ""; 
		Pattern p = Pattern.compile("("+x +prefix +"\\{.*?})");
		Matcher m = p.matcher(string);
		StringBuffer s = new StringBuffer();
		try {
			while (m.find()) {
				String k = m.group(1);
				String k1 = k.substring(2,k.length()-1);
				Object o = JsonUtils.getValue(json, k1);
				if (o instanceof String) {
					String v = (String) o;
					m.appendReplacement(s,v.replace("$", "\\$"));
				}
				else if (o instanceof Integer) 
					m.appendReplacement(s,Integer.toString((Integer) o));
				else if (o instanceof Long) 
					m.appendReplacement(s,Long.toString((Long) o));
				else if (o instanceof Double) 
					m.appendReplacement(s,Double.toString((Double) o));
				else if (o instanceof Boolean) 
					m.appendReplacement(s,Boolean.toString((Boolean) o));
				else 
					m.appendReplacement(s, x+k);
			}
		} catch (Exception e) {
			logger.warn("unable to expand: " + string + " " + e);
			e.printStackTrace();
			throw new RuntimeException("unable to expand: " + string + " " + json.toString(2), e);
		}
		m.appendTail(s);
		return s.toString();
	}
}
