
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
	
package org.openecomp.ncomp.utils.journaling;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceConfigurationError;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.serialization.ValidatingObjectInputStream;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.openecomp.ncomp.utils.SecurityUtils;

public abstract class JournalingObject {
	public static final Logger logger = Logger.getLogger(JournalingObject.class);
	static final int SET_METHOD = 0;
	static final int SAVE_METHOD = -999;
	protected HashMap<String, JournalingObject> children = new HashMap<String, JournalingObject>();
	private JournalingObject parent = null;
	private String context;
	private File dir;
	private ObjectOutputStream jStream;

	private List<JournalingEvent> playList = new ArrayList<JournalingEvent>();
	private int snapShotInterval = 30 * 60000; // every 30 minutes
	private Date lastSnapShot  = new Date();
	private int numLogs = 0;
	static private List<String> whiteList = new ArrayList<String>();

	static {
		startCleanupThread();
		whiteList.add("org.openecomp.ncomp.utils.journaling.*");
		whiteList.add("java.util.*");
		whiteList.add("java.lang.*");
	}

	public JournalingObject(String context, JournalingObject parent) {
		setSnapshotInterval(snapShotInterval);
		this.parent = parent;
		this.context = context;
		if (parent.children.get(context) != null)
			throw new RuntimeException("dublicate child");
		parent.children.put(context, this);
		init();
	}

	public JournalingObject() {
	}

	public abstract void init();

	static protected JournalingObject create2(File dir, JournalingObject o) {
		if (dir.exists() && !dir.isDirectory())
			throw new RuntimeException("journaling directory exists but is not a directory");
		if (!dir.exists())
			dir.mkdirs();
		logger.info("creating journaling data structure: " + o.getClass().getName() + " " + dir);
		File logFile = saveObjectFile(dir, "log.dat");
		File snapshotFile = SecurityUtils.createSafeFile(dir, "snapshot.dat");
		if (snapshotFile.exists()) {
			JournalingObject oo = initFromSnapshot(snapshotFile);
			if (oo != null) {
				o = oo;
			}
		}
		o.init(); // setup object
		o.jStream = getObjectFile(dir, "log.dat");
		o.dir = dir;
		if (logFile != null) {
			o.initFromLog(logFile);
			logger.info("initialized from file: " + logFile);
			o.save();
		}
		addCleanupDirectory(dir);
		return o;
	}

	protected void initChild(String context, JournalingObject child) {
		children.put(context, child);
		child.parent = this;
		child.context = context;
		child.init();
	}

	public synchronized void log(JournalingEvent event) {
		if (parent != null) {
			event.addContext(this);
			parent.log(event);
			return;
		}
		try {
			if (logger.isDebugEnabled())
				logger.debug("log " + eventToString(event));
			jStream.writeUnshared(event);
			numLogs++;
			if (event.context.size() == 0 && event.method == SAVE_METHOD) {
				jStream.flush();
				if (lastSnapShot.getTime() + snapShotInterval < new Date().getTime() && numLogs > 100) {
					createSnapshot();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void logAttributeValue(String arg, Object value) {
		log(new JournalingEvent(null, SET_METHOD, arg, value));
	}

	public void save() {
		if (parent != null) {
			throw new RuntimeException("Can only save on the toplevel object");
		}
		log(new JournalingEvent(null, SAVE_METHOD, null, null));
		try {
			if (jStream != null)
				jStream.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			jStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getContext() {
		return context;
	}

	private void play(JournalingEvent e) {
		// System.out.println("play: " + e);
		if (logger.isDebugEnabled())
			logger.debug("play: " + eventToString(e));
		if (e.context.size() == 0 && e.method == SAVE_METHOD) {
			for (JournalingEvent e1 : playList) {
				play(e1, e1.context.size());
			}
			playList.clear();
		} else
			playList.add(e);
	}

	void play(JournalingEvent e, int index) {
		if (index == 0) {
			switch (e.method) {
			case SET_METHOD: {
				try {
					Field fld = this.getClass().getDeclaredField(e.pname);
					logAttributeValue(e.pname, e.value);
					fld.set(this, e.value);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					logger.error("Unable to set attribute: " + p(e.pname) + " " + this.getClass().getName() + " " + e1);
				}
				return;
			}
			default:
				throw new RuntimeException("Unexpected method: " + e.method);
			}
		}
		String context = e.context.get(index - 1);
		JournalingObject c = children.get(context);
		if (c == null) {
			throw new RuntimeException("Unknown Child: " + context + " not in " + children.keySet());
		}
		c.play(e, index - 1);
	}

	private String p(String s) {
		return SecurityUtils.logForcingProtection(s);
	}

	protected String eventToString(JournalingEvent e) {
		switch (e.method) {
		case SET_METHOD:
			return "object:method context=" + e.context + " key=" + e.pname + " value=" + e.value;
		default:
			return e.toString();
		}
	}

	void createSnapshot() {
		Date now = new Date();
		String fName = "snapshot.dat";
		String tName = fName + "." + now.getTime();
		logger.debug("create snapshot:" + fName + " " + this);
		try {
			ObjectOutputStream out = getObjectFile(dir, tName);
			try {
				out.writeObject(this); 	
			} finally {
				out.close();
			}
			File f = SecurityUtils.createSafeFile(dir, fName);
			File t = SecurityUtils.createSafeFile(dir, tName);
			if (f.exists()) {
				f.delete();
			}
			if (!t.renameTo(f)) {
				throw new RuntimeException("Unable to rename file:" + f);
			}
			jStream.close();
			saveObjectFile(dir, "log.dat", now);
			jStream = getObjectFile(dir, "log.dat");
			numLogs = 0;
			lastSnapShot = new Date();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static JournalingObject initFromSnapshot(File file) {
		try {
			logger.debug("reading" + file);
			BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file),16777216);
			ValidatingObjectInputStream in = new ValidatingObjectInputStream(fin);
			addAccept(in);
			Object o = null;
			try {
				o = in.readObject();
			} finally {
				in.close();
			}
			return (JournalingObject) o;
		} catch (Exception e) {
			logger.error("Unable to init from snapshot file: " + file + " " + e);
			if (logger.isDebugEnabled())
				e.printStackTrace();
		}
		return null;
	}

	public void setSnapshotInterval(int i) {
		snapShotInterval = i;
		lastSnapShot = new Date();
		// make sure snapshots time are randomized and not happening at the same time.
		lastSnapShot.setTime(lastSnapShot.getTime()-(long) (i*SecurityUtils.inSecureRandom()));
	}

	public int getLogSize() {
		return numLogs;
	}

	@SuppressWarnings({ "static-access", "deprecation" })
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		CommandLineParser parser = new GnuParser();

		// create the Options
		Options options = new Options();
		options.addOption("h", "help", false, "Show usage");
		options.addOption(OptionBuilder.withLongOpt("file").withArgName("fileName").hasArg().create('f'));
		// Handle inputs
		CommandLine line = null;
		try {
			line = parser.parse(options, args);
		} catch (ParseException e) {
		}
		if (line == null || line.hasOption("help") || line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("util", options);
			return;
		}
		// String args1[] = line.getArgs();
		if (line.hasOption("file")) {
			FileInputStream fin = new FileInputStream(new File(line.getOptionValue("file")));
			ValidatingObjectInputStream in = new ValidatingObjectInputStream(fin);
			addAccept(in);
			try {
				while (true) {
					Object o;
					try {
						o = in.readUnshared();
						if (o instanceof JournalingEvent) {
							JournalingEvent e = (JournalingEvent) o;
							System.out.println(e.toString());
							continue;
						}
						System.out.println(o.toString());
					} catch (EOFException e) {
						break;
					}
				}
			} finally {
				in.close();
			}
		}
	}

	private static void addAccept(ValidatingObjectInputStream in) {
		for (String s : whiteList) {
			in.accept(s);
		}
	}
	public static void addClassToWhiteList(String s) {
		whiteList.add(s);
	}

	static int num = 0;

	static private File saveObjectFile(File dir, String fname) {
		return saveObjectFile(dir, fname, new Date());
	}

	static private File saveObjectFile(File dir, String fname, Date now) {
		String fname2 = fname + "." + now.getTime() + "." + num++;
		File f1 = SecurityUtils.createSafeFile(dir, fname);
		File f2 = SecurityUtils.createSafeFile(dir, fname2);
		if (f1.exists()) {
			if (f2.exists()) {
				f2.delete();
			}
			if (!f1.renameTo(f2)) {
				throw new RuntimeException("Unable to rename file: " + f2);
			}
			return f2;
		}
		return null;
	}

	static private ObjectOutputStream getObjectFile(File dir, String fname) {
		File f1 = SecurityUtils.createSafeFile(dir, fname);
		ObjectOutputStream s = null;
		try {
			s = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f1)));
		} catch (Exception e) {
			throw new RuntimeException("unknown error: " + e);
		}
		return s;
	}

	private void initFromLog(File file) {
		int numEvents = 0;
		Object o = null;
		try {
			FileInputStream fin = null; 
			ValidatingObjectInputStream in = null;
			try {
				fin = new FileInputStream(file);
				in = new ValidatingObjectInputStream(fin);
				addAccept(in);
				while (true) {
					try {
						o = in.readUnshared();
						// logger.info("new object: " + o);
						numEvents++;
						if (o instanceof JournalingEvent) {
							JournalingEvent e = (JournalingEvent) o;
							play(e);
						}
						if (o == null) {
							logger.warn("read null object from: " + file);
						}
					} catch (EOFException e1) {
						break;
					} catch (StreamCorruptedException e1) {
						break;  
					}
				}
			} finally {
				if (in != null) 
					in.close();
				if (fin != null) 
					fin.close();
			}
			playList.clear();
		} catch (EOFException e) {
			logger.debug("initFromLog failed: " + file + " numEvents=" + numEvents + " o=" + o);
		} catch (Exception e) {
			logger.debug("initFromLog failed: " + file + " numEvents=" + numEvents + " o=" + o);
			e.printStackTrace();
		}
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		for (String k : children.keySet()) {
			json.put(k, children.get(k).toJson());
		}
		return json;
	}

	// Cleanup old files
	static List<File> cleanupDirectories = new ArrayList<File>();
	private static void startCleanupThread() {
		Thread t = new Thread("journaling cleanup") {
			public void run() {
				while (true) {
					try {
						cleanup();
						Thread.sleep(300000);
					} catch (Exception e) {
						e.printStackTrace();
						logger.error(e);
					}
				}
			}

		};
		t.setDaemon(true);
		t.start();
	}
	private static void cleanup() {
		List<File> l = new ArrayList<File>();
		synchronized (cleanupDirectories) {
			l.addAll(cleanupDirectories);
		}
		Date now = new Date();
		for (File dir : l) {
			if (!dir.exists() || ! dir.isDirectory()) {
				synchronized (cleanupDirectories) {
					logger.warn("removing bad journaling directory" + dir.getAbsolutePath());
					cleanupDirectories.remove(dir);
					continue;
				}
			}
			for (File f : dir.listFiles()) {
				// ignore file that is recently changed
				if (f.lastModified() + 3600000 > now.getTime()) continue;
				if (f.getName().startsWith("log.dat.")) 
					f.delete();
			}
		}
	}
	private static void addCleanupDirectory(File dir) {
		synchronized (cleanupDirectories) {
			cleanupDirectories.add(dir);
		}
	}
}
