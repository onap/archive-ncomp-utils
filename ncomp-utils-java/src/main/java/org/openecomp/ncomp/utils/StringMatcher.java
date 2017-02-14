
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.openecomp.ncomp.webservice.utils.DateUtils;

public class StringMatcher {
	public static final Logger logger = Logger.getLogger(StringMatcher.class);
	private Pattern pattern;
	private List<String> parameterNames = new ArrayList<String>();
	private HashMap<Integer, String> i2p = new HashMap<Integer,String>();
	private HashMap<Integer, String> i2n = new HashMap<Integer,String>();
	private List<Character> parameters = new ArrayList<Character>();
	private String str;
	private String patternOrig;

	public StringMatcher(String pattern) { 
		patternOrig = pattern;
		Pattern p = Pattern.compile("\\$\\{(.*?)}");
		Matcher m = p.matcher(pattern);
		StringBuffer buf = new StringBuffer();
		int i = 0;
		while (m.find()) {
			String k = m.group(1);
			int j = k.indexOf(":");
			if (j==-1) {
				i2p.put(i,".*");
			}
			else {
				String kk = k.substring(0, j);
				i2p.put(i,k.substring(j+1));
				k=kk;
			}
			if(parameterNames.contains(k)) 
				throw new RuntimeException("Pattern contain the same name multiple times:" + k);
			parameterNames.add(k);
			i2n.put(i,k);
			m.appendReplacement(buf,"%"+i++);
		}
		m.appendTail(buf);
		str = buf.toString();
		buf = new StringBuffer("^");
		int index2 = 0;
		for (int index = str.indexOf('%', index2); index != -1; index = str.indexOf('%', index2)) {
			Character c = str.charAt(index + 1);
			parameters.add(c);
			buf.append(str.substring(index2, index));
			if ('0' <= c && c <= '9') {
				// parameter
				int j = c - '0';
				buf.append("(" + i2p.get(j) + ")");
			} else {
				// Time specifier
				switch (c) {
				case 'Y':
					buf.append("(\\d\\d\\d\\d)");
					break;
				case 'L':
					buf.append("(\\d\\d\\d)");
					break;
				case 'y':
				case 'm':
				case 'd':
				case 'S':
				case 'M':
				case 'H':
					buf.append("(\\d\\d)");
					break;
				case 'k':
					buf.append("(\\d\\d|\\d)");
					break;
				case 's':
				case 'U': // Deprecated.
					buf.append("(\\d*)");
					break;
				default:
					throw new RuntimeException("Unknown pattern specifier: "
							+ c + " " + " str=" + str);
				}
			}
			index2 = index + 2;
		}
		buf.append("$");
		if (logger.isDebugEnabled())
			logger.debug("pattern=" + patternOrig + " str=" + str + " buf=" + buf);
		this.pattern = Pattern.compile(buf.toString());
	}


	public boolean match(String name, HashMap<String, String> h, Date date) {
    	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(0);
		Matcher m = pattern.matcher(name);
		boolean b = m.find();
		int index = 1;
		if (logger.isDebugEnabled())
			logger.debug("Match " + str + " name=" + name + " match=" + b);
		if (!b)
			return false;
		for (Character c : parameters) {
			if ('0' <= c && c <= '9') {
				h.put(i2n.get(c - '0'),m.group(index++));
			} else {
				// Time specifier
				switch (c) {
				case 'm':
					// month is numbered from zero
					cal.set(Calendar.MONTH,
							Integer.parseInt(m.group(index++)) - 1);
					break;
				case 'd':
					cal.set(Calendar.DAY_OF_MONTH,
							Integer.parseInt(m.group(index++)));
					break;
				case 'y':
					cal.set(Calendar.YEAR,
							2000 + Integer.parseInt(m.group(index++)));
					break;
				case 'Y':
					cal.set(Calendar.YEAR, Integer.parseInt(m.group(index++)));
					break;
				case 'S':
					cal.set(Calendar.SECOND, Integer.parseInt(m.group(index++)));
					break;
				case 'M':
					cal.set(Calendar.MINUTE, Integer.parseInt(m.group(index++)));
					break;
				case 'L':
					cal.set(Calendar.MILLISECOND, Integer.parseInt(m.group(index++)));
					break;
				case 'k':
				case 'H':
					cal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(m.group(index++)));
					break;
				case 's':
				case 'U':
					cal.setTime(DateUtils.unix2date(m.group(index++)));
					break;
				default:
					throw new RuntimeException("Unknown pattern specifier: "
							+ c);
				}
			}
		}
		date.setTime(cal.getTimeInMillis());
		return true;
	}
}
