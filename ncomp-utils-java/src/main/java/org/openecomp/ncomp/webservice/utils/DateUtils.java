
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

import java.util.*;

import javax.xml.datatype.*;

import org.openecomp.ncomp.utils.FindFiles;
import org.openecomp.ncomp.utils.FindFiles.ParameterizedFile;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class DateUtils {
	private static DatatypeFactory factory;
	static {
		try {
			factory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException("unable to configure datatype factory", e);
		}
	}

	public static Date toDate(XMLGregorianCalendar dateTime) {
		if (dateTime == null)
			return null;
		if (dateTime.toGregorianCalendar() == null)
			throw new RuntimeException("Unable to convert to Date: " + dateTime.toString());
		return dateTime.toGregorianCalendar().getTime();
	}

	public static XMLGregorianCalendar toDateTime(Date date) {
		if (date == null)
//			throw new RuntimeException("Date is null");
			return null;
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(date);
		return factory.newXMLGregorianCalendar(c).normalize();
	}

	// @formatter:off turn off Eclipse formating.
	private static DateFormat dfs[] = {
			// since first matching format is used, the most specific need to be
			// first
			new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z"), new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),new SimpleDateFormat("yyyy-MM-dd HH:mm"),
			new SimpleDateFormat("MM/dd/yyyy"), new SimpleDateFormat("yyyy-MM-dd"),
			new RelativeDateFormat("%dday", 24 * 3600 * 1000), new RelativeDateFormat("%dweek", 7 * 24 * 3600 * 1000),
			new RelativeDateFormat("%dhour", 3600 * 1000), new RelativeDateFormat("%dmin", 60 * 1000), new FuzzyDateFormat() };

	// @formatter:on
	public static synchronized Date dateFromString(String str) {
		Date d = null;
		if ("now".equals(str)) return new Date();
		for (DateFormat df : dfs) {
			try {
				d = df.parse(str);
				break;
			} catch (ParseException e) {
//				 e.printStackTrace();
			} catch (RuntimeException e) {
//				 e.printStackTrace();				
			}
		}
		if (d == null)
			throw new RuntimeException("Unable to parse date from: " + str);
		return d;
	}
	
	// handle %H format strings
	public static Date dateFromString(String str,String format) {
		FindFiles f = new FindFiles("/"+format);
		ParameterizedFile pf = f.fileMatch("/" + str);
		return  pf != null ? pf.getDate() : null;
	}

	public static Object dateFromString(String string, Date relativeDate) {
		throw new UnsupportedOperationException();
	}

	public static Date unix2date(String string) {
		StringTokenizer st = new StringTokenizer(string, ".");
		long l = Long.parseLong(st.nextToken());
		// System.out.println(string + " " + l);
		return new Date(l * 1000);
	}

	// sample format "yyyy_MM_dd'/outputs/'HH_mm_ss'.%U.gz'"
	public static String gmtFormat(Date date, String format) {
		DateFormat indfm = new SimpleDateFormat(format);
		indfm.setTimeZone(TimeZone.getTimeZone("GMT"));
		return indfm.format(date).replaceAll("%U", Long.toString(date.getTime() / 1000));
	}

	public static List<Date> dateRange(Date start, Date end, long intervalMilliSeconds) {
		long d = start.getTime() / intervalMilliSeconds * intervalMilliSeconds;
		List<Date> res = new ArrayList<Date>();
		if (end.before(start)) return res;
		res.add(new Date(d));
		while (true) {
			d += intervalMilliSeconds;
			if (d >= end.getTime())
				break;
			res.add(new Date(d));
		}
		return res;
	}

	public static List<Date> dateRange(Date d1, Date d2) {
		return dateRange(d1,d2,1000*3600*24);
	}

	public static long stringToDuration(String interval) {
		Scanner scanner = new Scanner(interval);
		Long res = null;
		if (interval.contains("hour")) {
			scanner.useDelimiter("h");
			res = scanner.nextLong() * 3600 * 1000;
		}
		if (interval.contains("min")) {
			scanner.useDelimiter("m");
			res = scanner.nextLong() * 60 * 1000;
		}
		if (interval.contains("sec")) {
			scanner.useDelimiter("s");
			res = scanner.nextLong() * 1000;
		}
		if (interval.contains("day")) {
			scanner.useDelimiter("d");
			res = scanner.nextLong() * 3600 * 24 * 1000;
		}
		scanner.close();
		if (res == null)
			throw new RuntimeException("bad format: " + interval);
		return res;
	}

	public static String toString(String format, Date time) {
		format = format.replace("%S", String.format("%tS", time));
		format = format.replace("%M", String.format("%tM", time));
		format = format.replace("%H", String.format("%tH", time));
		format = format.replace("%Y", String.format("%tY", time));
		format = format.replace("%m", String.format("%tm", time));
		format = format.replace("%d", String.format("%td", time));
		format = format.replace("%y", String.format("%ty", time));
		format = format.replace("%k", String.format("%tk", time));
		format = format.replace("%U", String.format("%ts", time));
		format = format.replace("%s", String.format("%ts", time));
		format = format.replace("%L", String.format("%tL", time));
		return format;
	}

	public static String delay2String(long delay1) {
		if (delay1 < 0) {
			return delay2String(-delay1) + " in the future";
		}
		double delay = delay1/1000.0;
		if (delay < 200)
			return String.format("%.1f seconds", delay);
		delay = delay/60;
		if (delay < 60)
			return String.format("%.1f minutes", delay);
		delay = delay/60;
		if (delay < 72)
			return String.format("%.1f hours", delay);
		delay = delay/24;
		return String.format("%.1f days", delay);
	}
}

class FuzzyDateFormat extends DateFormat {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
		return toAppendTo.append(DateUtils.toString("%H:%M", date));
	}

	@Override
	public Date parse(String source, ParsePosition pos) {
		String s = source.substring(pos.getIndex());
		Date d = null;
		if (s.matches("\\d\\d:\\d\\d")) {
			DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			try {
				Date now = new Date();
				d = f.parse(DateUtils.toString("%Y-%m-%d ",now)+s);
				if (d.after(now)) {
					d.setTime(d.getTime()-24*3600*1000);
				}
				pos.setIndex(source.length());
			} catch (Exception e) {
				 e.printStackTrace();
			}
		}
		if (s.matches("\\d+/\\d+")) {
			DateFormat f = new SimpleDateFormat("yyyy/MM/dd");
			try {
				Date now = new Date();
				d = f.parse(DateUtils.toString("%Y/",now)+s);
				if (d.after(now)) {
			    	Calendar cal = Calendar.getInstance();
			    	cal.setTime(d);
			    	cal.add(Calendar.YEAR,-1);  
					d = cal.getTime();
				}
				pos.setIndex(source.length());
			} catch (Exception e) {
				 e.printStackTrace();
			}
		}
		if (s.matches("\\d+/\\d+ \\d\\d:\\d\\d")) {
			DateFormat f = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			try {
				Date now = new Date();
				d = f.parse(DateUtils.toString("%Y/",now)+s);
				if (d.after(now)) {
			    	Calendar cal = Calendar.getInstance();
			    	cal.setTime(d);
			    	cal.add(Calendar.YEAR,-1);  
					d = cal.getTime();
				}
				pos.setIndex(source.length());
			} catch (Exception e) {
				 e.printStackTrace();
			}
		}
		return d;
	}
	
}
class RelativeDateFormat extends DateFormat {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String format;
	private long intervalMilliSeconds;

	RelativeDateFormat(String format, long intervalMilliSeconds) {
		this.format = format;
		this.intervalMilliSeconds = intervalMilliSeconds;
	}

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
		Date now = new Date();
		int delay = (int) ((now.getTime() - date.getTime()) / intervalMilliSeconds);
		toAppendTo.append(String.format(format, delay));
		return toAppendTo;
	}

	@Override
	public Date parse(String source, ParsePosition pos) {
		String s = source.substring(pos.getIndex());
		if (format.startsWith("%d")) {
			Scanner scanner = new Scanner(s);
			scanner.useDelimiter(format.substring(2));
			if (!scanner.hasNextInt()) {
				scanner.close();
				return null;
			}
			int i = scanner.nextInt();
			if (scanner.hasNext()) {
				scanner.close();
				return null;
			}
			Date d = new Date();
			d.setTime(d.getTime() + i * intervalMilliSeconds);
			pos.setIndex(source.length());
			scanner.close();
			return d;
		}
		return null;
	}
}
