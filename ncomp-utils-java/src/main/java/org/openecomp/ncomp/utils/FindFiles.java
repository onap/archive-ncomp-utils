
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.fs.FileStatus;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import org.openecomp.ncomp.webservice.utils.DateUtils;

public class FindFiles {
	public static final Logger logger = Logger.getLogger("org.openecomp.ncomp.utils.io.findfiles");

	private int numDays = 10;
	private int numDaysWarning = 7;
	private String email = null;
	private int minSize = 0;
	private int intervalSeconds = 24 * 3600;
	private boolean gmt = true;
	private boolean allowMultipleMatches = false;
	// private Date date;
	private String format;
	private List<String> parameters;

	private String hdfsUri;

//	private FileSystem hdfsFs;

	/**
	 * @param format
	 */
	public FindFiles(String format) {
		this.format = format;
		this.parameters = new ArrayList<String>();
	}

	public FindFiles(String format, List<String> parameters) {
		this.format = format;
		this.parameters = parameters;
	}

	public FindFiles(String format, String[] p) {
		this.format = format;
		parameters = new ArrayList<String>();
		for (String pp : p) {
			parameters.add(pp);
		}
	}

	public int getNumDays() {
		return numDays;
	}

	public void setNumDays(int numDays) {
		this.numDays = numDays;
	}

	public int getNumDaysWarning() {
		return numDaysWarning;
	}

	public void setNumDaysWarning(int numDaysWarning) {
		this.numDaysWarning = numDaysWarning;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getMinSize() {
		return minSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public int getIntervalSeconds() {
		return intervalSeconds;
	}

	public void setIntervalSeconds(int intervalSeconds) {
		throw new RuntimeException("Currently only daily granularity is suppported");
		// this.intervalSeconds = intervalSeconds;
	}

	public boolean isGmt() {
		return gmt;
	}

	public void setGmt(boolean gmt) {
		this.gmt = gmt;
	}

	public boolean isAllowMultipleMatches() {
		return allowMultipleMatches;
	}

	public void setAllowMultipleMatches(boolean allowMultipleMatches) {
		this.allowMultipleMatches = allowMultipleMatches;
	}

	public String findFile() {
		Calendar end = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		start.add(Calendar.DAY_OF_MONTH, -numDays);
		List<ParameterizedFile> pfiles = findFiles(start.getTime(), end.getTime());
		int i = pfiles.size();
		if (i == 0)
			return null;
		return pfiles.get(i - 1).getFile();
	}

	public class ParameterizedFile implements Comparable<ParameterizedFile> {
		public ParameterizedFile(ParameterizedFile current) {
			file = new StringBuffer(current.file);
			date = current.getDate();
			values = current.values.clone();
		}

		public ParameterizedFile(Date date) {
			this.date = date;
			;
		}

		public String getFile() {
			return file.toString();
		}

		public Date getDate() {
			return date;
		}

		public String[] getValues() {
			return values;
		}

		StringBuffer file = new StringBuffer();
		Date date;;
		String[] values = new String[parameters.size()];

		@Override
		public String toString() {
			return "ParametizedFile [date=" + date + ", file=" + file + ", values=" + Arrays.toString(values) + "]";
		}

		public void setDate(Date time) {
			date = time;
		}

		@Override
		public int compareTo(ParameterizedFile o) {
			return date.compareTo(o.getDate());
		}

	}

	public List<ParameterizedFile> findFiles() {
		String[] l = {};
		return findFiles(l);
	}

	public List<ParameterizedFile> findFiles(String[] values) {
		Date date = new Date();
		List<ParameterizedFile> res = new ArrayList<ParameterizedFile>();
		// hack to handle dos filesystems, where format may be "c:/..."
		MyFile root = new MyFile(new File(format.split("/")[0] + "/"));
		List<PathSegment> segs = new ArrayList<PathSegment>();
		for (String s : format.split("/")) {
			segs.add(new PathSegment(s, date, values));
		}
		ParameterizedFile f = new ParameterizedFile(date);
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTime(date);
		findFilesInternal(date, root, segs, 1, values, f, cal, res);
		return res;
	}

	public List<ParameterizedFile> findFiles(Date date) {
		String[] l = {};
		Date start = new Date((date.getTime() / 1000 / intervalSeconds) * intervalSeconds * 1000);
		Date end = new Date((date.getTime() / 1000 / intervalSeconds + 1) * intervalSeconds * 1000);
		return findFiles(start, end, l);
	}

	public List<ParameterizedFile> findFiles(Date start, Date end) {
		String[] l = {};
		return findFiles(start, end, l);
	}

	public List<ParameterizedFile> findFiles(Date start, Date end, String[] values) {
		List<ParameterizedFile> res = new ArrayList<ParameterizedFile>();
		// hack to handle dos filesystems, where format may be "c:/..."
		MyFile root = new MyFile(new File(format.split("/")[0] + "/"));
		Date date = new Date(start.getTime() / intervalSeconds / 1000 * 1000 * intervalSeconds);
		while (date.before(end)) {
			List<PathSegment> segs = new ArrayList<PathSegment>();
			for (String s : format.split("/")) {
				segs.add(new PathSegment(s, date, values));
			}
			ParameterizedFile f = new ParameterizedFile(date);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			cal.setTime(date);
			findFilesInternal(date, root, segs, 1, values, f, cal, res);
			date = new Date(date.getTime() + 1000 * getIntervalSeconds());
		}
		List<ParameterizedFile> res2 = new ArrayList<ParameterizedFile>();
		for (ParameterizedFile f : res) {
			if (logger.isDebugEnabled())
				logger.debug(f + " " + start + " " + end + " " + start.after(f.getDate()) + " "
						+ f.getDate().after(end));
			if (start.after(f.getDate()) || (!f.getDate().before(end)))
				continue;
			res2.add(f);
		}
		return res2;
	}

	public ParameterizedFile findLastFile() {
		String[] l = {};
		return findLastFile(l);
	}

	public ParameterizedFile findLastFile(Date after) {
		String[] l = {};
		return findLastFile(l, after);
	}

	public ParameterizedFile findLastFile(String[] values) {
		Date after = DateUtils.dateFromString("-30day");
		return findLastFile(values, after);
	}

	/**
	 * @param values
	 * @param after
	 *            Only consider files after this date.
	 * @return
	 */
	public ParameterizedFile findLastFile(String[] values, Date after) {
		// hack to handle DOS file systems, where format may be "c:/..."
		File root = new File(format.split("/")[0] + "/");
		List<PathSegment> segs = new ArrayList<PathSegment>();
		for (String s : format.split("/")) {
			segs.add(new PathSegment(s, values));
		}
		ParameterizedFile f = new ParameterizedFile(new Date());
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		after.setTime(after.getTime() / 24 / 3600 / 1000 * 24 * 3600 * 1000);
		cal.setTimeInMillis(after.getTime());
		return findLastFileInternal(root, segs, 1, values, f, cal, after);
	}

	public class PathSegment {
		String str;
		Pattern pattern;
		List<Character> parameters = new ArrayList<Character>();

		private String encode(String s) {
			return s.replace("\\*", ".*").replace("\\?", ".");
		}

		public PathSegment(String str1, Date date, String[] values) {
			str = str1;
			if (str.equals("**")) {
				if (logger.isDebugEnabled())
					logger.debug("str=" + str + " buf=^.*$");
				pattern = Pattern.compile("^.*$");
				return;
			}
			StringBuffer buf = new StringBuffer("^");
			int index2 = 0;
			for (int index = str.indexOf('%', index2); index != -1; index = str.indexOf('%', index2)) {
				Character c = str.charAt(index + 1);
				parameters.add(c);
				buf.append(encode(str.substring(index2, index)));
				if ('0' <= c && c <= '9') {
					// parameter
					int i = c - '0';
					if (i >= FindFiles.this.parameters.size())
						throw new RuntimeException("Too large index " + i + " " + FindFiles.this.parameters + " "
								+ values);
					String s = i < values.length ? values[i] : null;
					if (s == null)
						s = ".*";
					else
						s = encode(s);
					buf.append("(" + s + ")");
				} else {
					// Time specifier
					TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
					switch (c) {
					case 'Y':
						buf.append(String.format("%tY", date));
						break;
					case 'y':
						buf.append(String.format("%ty", date));
						break;
					case 'm':
						buf.append(String.format("%tm", date));
						break;
					case 'd':
						buf.append(String.format("%td", date));
						break;
					case 'S':
					case 'M':
					case 'H':
						buf.append("(\\d\\d)");
						break;
					case 'k':
						buf.append("(\\d\\d|\\d)");
						break;
					case 'U':
						buf.append("(\\d*)");
						break;
					default:
						throw new RuntimeException("Unknown pattern specifier: " + c);
					}
				}
				index2 = index + 2;
			}
			buf.append(encode(str.substring(index2)) + "$");
			if (logger.isDebugEnabled())
				logger.debug("date=" + date + " str=" + str + " buf=" + buf);
			pattern = Pattern.compile(buf.toString());
		}

		public PathSegment(String str1, String[] values) {
			str = str1;
			StringBuffer buf = new StringBuffer("^");
			int index2 = 0;
			for (int index = str.indexOf('%', index2); index != -1; index = str.indexOf('%', index2)) {
				Character c = str.charAt(index + 1);
				parameters.add(c);
				buf.append(encode(str.substring(index2, index)));
				if ('0' <= c && c <= '9') {
					// parameter
					int i = c - '0';
					if (i >= FindFiles.this.parameters.size())
						throw new RuntimeException("Too large index " + i + " " + FindFiles.this.parameters + " "
								+ values);
					String s = i < values.length ? values[c - '0'] : null;
					if (s == null)
						s = ".*";
					else
						s = encode(s);
					buf.append("(" + s + ")");
				} else {
					// Time specifier
					switch (c) {
					case 'Y':
						buf.append("(\\d\\d\\d\\d)");
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
					case 'U':
						buf.append("(\\d*)");
						break;
					default:
						throw new RuntimeException("Unknown pattern specifier: " + c);
					}
				}
				index2 = index + 2;
			}
			buf.append(encode(str.substring(index2)) + "$");
			if (logger.isDebugEnabled())
				logger.debug("str=" + str + " buf=" + buf);
			pattern = Pattern.compile(buf.toString());
		}

		public boolean match(String name, ParameterizedFile pfile, Calendar cal) {
			Matcher m = pattern.matcher(name);
			boolean b = m.find();
			int index = 1;
			if (logger.isDebugEnabled())
				logger.debug("Match " + str + " name=" + name + " match=" + b);
			if (!b)
				return false;
			for (Character c : parameters) {
				if ('0' <= c && c <= '9') {
					pfile.values[c - '0'] = m.group(index++);
				} else {
					// Time specifier
					switch (c) {
					case 'm':
					case 'd':
					case 'y':
					case 'Y':
						break;
					case 'S':
						cal.set(Calendar.SECOND, Integer.parseInt(m.group(index++)));
						break;
					case 'M':
						cal.set(Calendar.MINUTE, Integer.parseInt(m.group(index++)));
						break;
					case 'k':
					case 'H':
						cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(index++)));
						break;
					case 'U':
						cal.setTime(DateUtils.unix2date(m.group(index++)));
						break;
					default:
						throw new RuntimeException("Unknown pattern specifier: " + c);
					}
				}
			}
			return true;
		}

		public boolean matchFull(String name, ParameterizedFile pfile, Calendar cal) {
			Matcher m = pattern.matcher(name);
			boolean b = m.find();
			int index = 1;
			if (logger.isDebugEnabled())
				logger.debug("Match " + str + " name=" + name + " match=" + b);
			if (!b)
				return false;
			for (Character c : parameters) {
				if ('0' <= c && c <= '9') {
					pfile.values[c - '0'] = m.group(index++);
				} else {
					// Time specifier
					switch (c) {
					case 'm':
						// month is numbered from zero
						cal.set(Calendar.MONTH, Integer.parseInt(m.group(index++)) - 1);
						break;
					case 'd':
						cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(m.group(index++)));
						break;
					case 'y':
						cal.set(Calendar.YEAR, 2000 + Integer.parseInt(m.group(index++)));
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
					case 'k':
					case 'H':
						cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(index++)));
						break;
					case 'U':
						cal.setTime(DateUtils.unix2date(m.group(index++)));
						break;
					default:
						throw new RuntimeException("Unknown pattern specifier: " + c);
					}
				}
			}
			return true;
		}

		public boolean isRecursive() {
			return str.equals("**");
		}
	}

	private void findFilesInternal(Date date, MyFile dir, List<PathSegment> segs, int index, String[] values,
			ParameterizedFile current, Calendar cal, List<ParameterizedFile> res) {
		PathSegment seg = segs.get(index);
		List<MyFile> files = dir.listFiles(); 
		if (files == null) {
			if (logger.isDebugEnabled())
				logger.debug("Unable to read directory " + dir.getAbsolutePath());
			return;
		}
		if (seg.isRecursive()) {
			findFilesInternal(date, dir, segs, index + 1, values, current, cal, res);
		}
		for (MyFile file : files) {
			if (!seg.match(file.getName(), current, cal)) {
				if (logger.isDebugEnabled())
					logger.debug("Ignore " + file.getAbsolutePath());
				continue;
			}
			ParameterizedFile f = new ParameterizedFile(current);
			f.file.append("/" + file.getName());
			if (index + 1 == segs.size()) {
				f.setDate(cal.getTime());
				res.add(f);
			} else {
				if (file.isDirectory()) {
					findFilesInternal(date, file, segs, index + 1, values, f, cal, res);
					if (seg.isRecursive()) {
						findFilesInternal(date, file, segs, index, values, f, cal, res);
					}
				} else if (logger.isDebugEnabled())
					logger.debug("Ignore2 " + file.getAbsolutePath());
			}
		}
	}

	private class MyFile {
//		public MyFile(FileStatus fileStatus) {
//			h = fileStatus;
//		}
		public boolean isDirectory() {
//			if (h != null) 
				return f.isDirectory();
//			return h.isDir();
		}
		public List<MyFile> listFiles() {
			List<MyFile> res = new ArrayList<MyFile>();
			if (hdfsUri != null) {
//				try {
//					FileStatus[] status = hdfsFs.listStatus(new Path(getAbsolutePath()));
//					for (int i = 0; i < status.length; i++) {
//						res.add(new MyFile(status[i]));
//					}
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
			else {
				File[] files = f.listFiles();
				for (int i = 0; i < files.length; i++) {
					res.add(new MyFile(files[i]));
				}
			}
			return res;
		}
		public String getAbsolutePath() {
//			if (f != null) 
				return f.getAbsolutePath();
//			return h.getPath().toString();
		}
		public String getName() {
//			if (f != null) 
				return f.getName();
//			return h.getPath().getName();
		}
		public MyFile(File file) {
			f = file;
		}
		File f;
//		FileStatus h;
	}

	private ParameterizedFile findLastFileInternal(File dir, List<PathSegment> segs, int index, String[] values,
			ParameterizedFile current, Calendar cal, Date after) {
		final class FileDate implements Comparable<FileDate> {
			public final File file;
			public final ParameterizedFile pf;
			public final Calendar cal;

			FileDate(File file, ParameterizedFile pf, Calendar cal) {
				this.file = file;
				this.pf = pf;
				this.cal = cal;
			}

			@Override
			public int compareTo(FileDate x) {
				return -cal.compareTo(x.cal);
			}
		}
		PathSegment seg = segs.get(index);
		File[] files = dir.listFiles();
		if (files == null) {
			if (logger.isDebugEnabled())
				logger.debug("Unable to read directory " + dir.getAbsolutePath());
			return null;
		}
		List<FileDate> children = new ArrayList<FileDate>(files.length);
		for (File file : files) {
			Calendar cal2 = (Calendar) cal.clone();
			if (!seg.matchFull(file.getName(), current, cal2)) {
				if (logger.isDebugEnabled())
					logger.debug("Ignore " + file.getAbsolutePath());
				continue;
			}
			ParameterizedFile f = new ParameterizedFile(current);
			f.file.append("/" + file.getName());
			if (index + 1 == segs.size() || file.isDirectory()) {
				children.add(new FileDate(file, f, cal2));
			} else {
				if (logger.isDebugEnabled())
					logger.debug("Ignore2 " + file.getAbsolutePath());
			}
		}
		java.util.Collections.sort(children);
		for (FileDate fd : children) {
			if (after.after(fd.cal.getTime()))
				continue;
			if (index + 1 == segs.size()) {
				fd.pf.setDate(fd.cal.getTime());
				return fd.pf;
			} else if (fd.file.isDirectory()) {
				if (logger.isDebugEnabled())
					logger.debug("Considering directory" + fd.file.getAbsolutePath());
				ParameterizedFile f = findLastFileInternal(fd.file, segs, index + 1, values, fd.pf, fd.cal, after);
				if (f != null) {
					return f;
				}
			} else {
				throw new RuntimeException("ignore2 vanished");
			}
		}
		return null;
	}

	public String getFormat() {
		return format;
	}

	public static String convertPattern(String s) {
		StringBuffer buf = new StringBuffer();
		boolean literal = false;
		for (String p : s.split("'")) {
			if (literal) {
				buf.append(p);
			} else {
				buf.append(p.replace("yyyy", "%Y").replace("yy", "%y").replace("MM", "%m").replace("dd", "%d")
						.replace("HH", "%H").replace("mm", "%M").replace("ss", "%S"));
			}
			literal = !literal;
		}
		return buf.toString();
	}

	public ParameterizedFile fileMatch(String file) {
		List<PathSegment> segs = new ArrayList<PathSegment>();
		for (String s : format.split("/")) {
			String[] l = {};
			segs.add(new PathSegment(s, l));
		}
		ParameterizedFile f = new ParameterizedFile(new Date());
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(0);
		ParameterizedFile f1 = findMatch(file.split("/"), segs, 0, f, cal);
		if (f1 != null) {
			f1.setDate(cal.getTime());
			f1.file = new StringBuffer(file);
		}
		return f1;
	}

	private ParameterizedFile findMatch(String[] parts, List<PathSegment> segs, int i, ParameterizedFile f, Calendar cal) {
		if (i == parts.length && i == segs.size()) {
			return f;
		}
		if (i >= parts.length || i >= segs.size())
			return null;
		if (findMatch(parts, segs, i + 1, f, cal) == null)
			return null;
		PathSegment seg = segs.get(i);
		String s = parts[i];
		return seg.matchFull(s, f, cal) ? f : null;
	}

	public String toString(Date time, List<String> l) {
		String s = format;
		int index = 0;
		for (String v : l) {
			s = s.replace("%" + index, v);
			index++;
		}
		return DateUtils.toString(s,time);
	}

	public void setHdfs(String URI) {
//		this.hdfsUri = URI;
//		try {
//			hdfsFs = FileSystem.get(new java.net.URI(URI),new Configuration());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
		
		
	}
}
