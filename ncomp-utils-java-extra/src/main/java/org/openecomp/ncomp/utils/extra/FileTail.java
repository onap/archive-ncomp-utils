
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
	
package org.openecomp.ncomp.utils.extra;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import org.openecomp.ncomp.utils.FindFiles;
import org.openecomp.ncomp.utils.FindFiles.ParameterizedFile;
import org.openecomp.ncomp.utils.journaling.JournalingHashMap;
import org.openecomp.ncomp.utils.logging.LoggingUtils;
import org.openecomp.ncomp.webservice.utils.DateUtils;

public class FileTail {
	public static final Logger logger = Logger.getLogger(FileTail.class);
	private String format;
	private String directory;
	private NewLineHandler handler;
	private long checkFrequency = 60000; // milliseconds
	private long checkNewFrequency = 300000; // milliseconds
	private JournalingHashMap<Long> filePointerMap;
	private long scanDuration = DateUtils.stringToDuration("2day");
	private Object context; 

	public interface NewLineHandler {
		void newLine(String file, String line, Object context);
		void fixFilePermissions(File file);
	}

	@SuppressWarnings("unchecked")
	public FileTail(String format, String directory, String scanFreq, String scanNewFreq, String scanDuration, NewLineHandler handler, Object context) {
		super();
		if (scanFreq != null)
			checkFrequency = DateUtils.stringToDuration(scanFreq);
		if (scanNewFreq != null)
			checkNewFrequency = DateUtils.stringToDuration(scanNewFreq);
		if (scanDuration != null)
			this.scanDuration  = DateUtils.stringToDuration(scanDuration);

		this.format = format;
		this.directory = directory;
		this.handler = handler;
		this.context = context;
		filePointerMap = JournalingHashMap.create(new File(directory));
		logger.info("initial status: " + getStatus().toString(2));
		logger.info("Created: " + this);
		new MonitorThread(format);
	}
	@SuppressWarnings("unchecked")
	public FileTail(String filename, String directory, String scanFreq, NewLineHandler handler, Object context) {
		if (scanFreq != null)
			checkFrequency = DateUtils.stringToDuration(scanFreq);
		this.directory = directory;
		this.handler = handler;
		this.context = context;
		filePointerMap = JournalingHashMap.create(new File(directory));
		if (filePointerMap.get(filename) == null) {
			filePointerMap.put(filename, 0L);
		}
		new MonitorThread(filename);
	}

	@Override
	public String toString() {
		return "FileTail [format=" + format + ", directory=" + directory + ", checkFrequency="
				+ checkFrequency + ", checkNewFrequency=" + checkNewFrequency + "]";
	}

	private class MonitorThread implements Runnable {
		public MonitorThread(String name) {
			Thread t = new Thread(this, "filetail: " + name);
			t.start();
		}

		@Override
		public void run() {
			long numIntervalsPerNewFileCheck = checkNewFrequency / checkFrequency;
			long i = 0;
			while (true) {
				if ((i++ % numIntervalsPerNewFileCheck) == 0)
					scanForNewFiles();
				scanForNewLines();
				try {
					Thread.sleep(checkFrequency);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		}
	}

	private void scanForNewFiles() {
		if (format == null) return;
		FindFiles ff = new FindFiles(format);
		Date now = new Date();
		HashMap<String, Long> map = new HashMap<String, Long>();
		synchronized (this) {
			for (ParameterizedFile f : ff.findFiles()) {
				File f1 = new File(f.getFile());
				if (f1.lastModified() + scanDuration < now.getTime()) continue;
				if (filePointerMap.get(f.getFile()) == null) {
					map.put(f.getFile(), 0L);
					filePointerMap.put(f.getFile(), 0L);
					logger.info("Found new file: " + f.getFile());
				} else {
					map.put(f.getFile(), filePointerMap.get(f.getFile()));
				}
			}
			List<String> remove = new ArrayList<String>();
			for (String f : filePointerMap.keySet()) {
				if (map.get(f) == null) {
					logger.info("Deleted file: " + f);
					remove.add(f);
				}
			}
			for (String f : remove) filePointerMap.remove(f);
			filePointerMap.save();
		}
	}

	private void scanForNewLines() {
		logger.info("old status: " + getStatus().toString(2));
		Set<String> files = filePointerMap.keySet();
		for (String f : files) {
			try {
				long p;
				synchronized (this) {
					p = filePointerMap.get(f);
				}
				File file = new File(f);
				long len = file.length();
				if (len == p)
					continue;
				if (len < p) {
					logger.info("Logfile reset. Restarted at start");
					p = 0;
				}
				if (len > p) {
					if (!file.canRead()) {
						handler.fixFilePermissions(file);
					}
					if (!file.canRead()) {
						LoggingUtils.dampingLogger(logger, Level.WARN, "Unable to read: " + file.getAbsolutePath());
						continue;
					}
					RandomAccessFile rf = new RandomAccessFile(file, "r");
					rf.seek(p);
					String line = null;
					while ((line = rf.readLine()) != null) {
						if (line.length() == 0)
							continue;
						if (logger.isDebugEnabled())
							logger.debug("New line from file: " + f + " " + line);
						p = rf.getFilePointer();
						try {
							handler.newLine(file.getAbsolutePath(),line,context);
						} catch (Exception e) {
							logger.warn("Handler error: " + f + " " + e + " line=" + line);
							e.printStackTrace();
						}
					}
					rf.close();
				}
				synchronized (this) {
					filePointerMap.put(f, p);
					filePointerMap.save();
				}
			} catch (Exception e) {
				e.printStackTrace(System.out);
				logger.error("Scaning error: " + f + " " + e);
			}
		}
		logger.info("new status: " + getStatus().toString(2));
	}

	public JSONObject getStatus() {
		JSONObject o = new JSONObject();
		synchronized (this) {
			for (String f : filePointerMap.keySet()) {
				o.put(f, filePointerMap.get(f));
			}
		}
		return o;
	}

}
