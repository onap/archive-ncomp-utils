
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
	
package org.openecomp.ncomp.utils.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

public class SimpleCache<Key, Value, Builder extends CacheBuilder<Key,Value>> 
	implements SimpleCacheMBean<Key, Value, Builder> {
	
	private static final Logger logger = Logger.getLogger("org.openecomp.ncomp.utils.simplecache");

	private static final String defaultProperties = 
		"simplecache.default.cache.maxentries=10\n" +
		"simplecache.default.cache.maxage=-1\n" +
		"simplecache.default.cache.maxMemSize=-1\n" +
		"simplecache.default.ready.maxentries=1\n" +
		"simplecache.default.ready.maxage=3600000\n" +
		"simplecache.default.ready.maxMemSize=-1\n" +
		"simplecache.default.prefetch.threads=0\n" +
		"simplecache.Topology.cache.maxentries=30\n" +
		"simplecache.LsaMonitor.cache.maxentries=300\n" +
		"simplecache.LsaMonitor.ready.maxentries=100\n" +
		"simplecache.LsaMonitor.ready.maxage=60000\n" +
		"simplecache.LsaMonitor.prefetch.threadname=LsaCachePrefetch\n" +
		"simplecache.LsaMonitor.prefetch.threads=20\n" +
		"simplecache.OspfNetwork.cache.maxentries=10\n";
	
	private static final Properties cacheProperties = loadProperties();
	
 	public enum EntryStateEnum {
		/**
		 * An entry which has been created, but not yet on any list.
		 */
		NEW_ENTRY(0, "New Entry"),

		/**
		 * An entry waiting for preFetch, on prefetchList
		 */
		PREFETCH_ON_QUEUE(1, "Prefetch On Queue"),

		/**
		 * An entry being built by prefetch, on buildingPrefetchList
		 */
		PREFETCH_RUNNING(3, "Prefetch Running"),

		/**
		 * An entry that has been prefetched, but not yet used, on readyList
		 */
		PREFETCH_READY(5, "Prefetch Ready"),


		/**
		 * An entry being built (from Get, not Prefetch), on buildingGetList
		 */
		BUILD_RUNNING(7, "Build Running"),

		/**
		 * An entry in the cache, that has been used, on cacheList
		 */
		CACHED(9, "Cached"),

		/**
		 * An entry that has been removed from the active cache.
		 * This is currently not used, but available for future cache miss tracking.
		 */
		DEAD(10, "Dead"),
		
		/**
		 * An entry for which build failed, on failedList.
		 * This acts like an entry that doesn't exist, except that it
		 * tracks failures.
		 */
		FAILED(11, "Failed");
		
		private final int value;
		private final String name;

		private EntryStateEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
		  return value;
		}

		public String getName() {
		  return name;
		}

		@Override
		public String toString() {
			return name;
		}
	} //EntryStateEnum
	
	private class Entry {
		public EntryStateEnum state;
		public Date lastUsed;
		public int age;
		public final Key key;
		public Value value;
		public Entry next;
		public Entry prev;
		public long memSize;
		public Entry (Key key) {
			logger.debug("Entry() New entry " + key);
			state = EntryStateEnum.NEW_ENTRY;
			lastUsed = null;
			age = cacheAge;
			this.key = key;
			value = null;
			next = null;
			prev = null;
			memSize = 0;
		}
	}
	
	private class EntryList implements Iterable<Entry> {
		private final Entry listDummy;
		private int size;
		private int maxsize;
		private long memSize;
		private long maxMemSize;
		private long maxage;

		private class EntryIterator implements Iterator<Entry> {
			private Entry e;
			
			public EntryIterator () {
				e = listDummy;
			}
			
			@Override
			public boolean hasNext() {
				return (e.next != listDummy);
			}

			@Override
			public Entry next() {
				e = e.next;
				return e;
			}

			@Override
			public void remove() {
				Entry e_save = e;
				e = e.prev;
				EntryList.this.remove(e_save);
			}
		}
		
		public EntryList () {
			this(-1);
		}
		
		public EntryList (int maxsize) {
			listDummy = new Entry(null);
			listDummy.next = listDummy;
			listDummy.prev = listDummy;
			size = 0;
			this.maxsize = maxsize;
			memSize = 0;
			maxMemSize = -1;
			maxage = -1;
		}
		
		public void addToFront (Entry e) {
			e.next = listDummy.next;
			e.prev = listDummy;
			listDummy.next.prev = e;
			listDummy.next = e;
			size++;
			memSize += e.memSize;
		}
		
		public void addToBack (Entry e) {
			e.next = listDummy;
			e.prev = listDummy.prev;
			listDummy.prev.next = e;
			listDummy.prev = e;
			size++;
			memSize += e.memSize;
		}
		
		public void remove (Entry e) {
			e.next.prev = e.prev;
			e.prev.next = e.next;
			e.prev = null;
			e.next = null;
			size--;
			memSize -= e.memSize;
		}
		
		public void moveToFront (Entry e) {
			remove (e);
			addToFront (e);
		}
		
		@SuppressWarnings("unused")
		public void moveToBack (Entry e) {
			remove (e);
			addToBack (e);
		}
		
		public Entry getFront () {
			if (listDummy.next == listDummy) return null;
			return listDummy.next;
		}
		
		public Entry getBack () {
			if (listDummy.prev == listDummy) return null;
			return listDummy.prev;
		}
		
		public Entry getOverflow (Entry space) {
			boolean full = ((space == null) ? isOverFull(0,0) : isOverFull(1,space.memSize));
			if (full) return getBack();
			if (maxage >= 0) {
				Entry e = getBack();
				if (e == null) return null;
				Date d = new Date();
				d.setTime(d.getTime()-maxage);
				if (e.lastUsed.before(d)) return e;
			}
			return null;
		}
		
		public int size () {
			return size;
		}
		
		public long memSize() {
			return memSize;
		}
		
		public boolean isEmpty() {
			return (listDummy.next == listDummy);
		}
		
		public void setMaxSize (int maxsize) {
			this.maxsize = maxsize;
		}
		
		public void setMaxMemSize (long maxMemSize) {
			this.maxMemSize = maxMemSize;
		}
		
		public int getMaxSize () {
			return maxsize;
		}
		
		public long getMaxMemSize () {
			return maxMemSize;
		}
		
		public boolean isOverFull(int extraCnt, long extraMem) {
			return ((maxsize >= 0 && size + extraCnt > maxsize) ||
					(maxMemSize >= 0 && memSize + extraMem > maxMemSize));
		}
		
		public void setMaxAge (long maxage) {
			this.maxage = maxage;
		}
		
		public long getMaxAge () {
			return maxage;
		}
		
		public boolean add (Entry e) {
			addToBack (e);
			return true;
		}
		
		public Entry poll () {
			Entry e = getFront();
			remove (e);
			return e;
		}
		
		
		@Override
		public Iterator<Entry> iterator() {
			return new EntryIterator();
		}
		
		public String[] getEntries() {
			String[] entrylist = new String[size()];
			int i = 0;
			synchronized (SimpleCache.this) {
				for (Entry e : this) {
					entrylist[i++] = e.key.toString();
				}
			}
			return entrylist;
		}

	}
	
	private class PrefetchThread extends Thread {
		private final int num;
		
		public PrefetchThread (ThreadGroup group, int num_) {
			super(group, group.getName() + "_" + num_);
			num = num_;
			setDaemon(true);
		}
		
		public void run() {
			for (;;) {
				Entry e = null;
				synchronized (SimpleCache.this) {
					while (num < numthreads && (prefetchList.isEmpty() || readyList.isOverFull(buildingPrefetchList.size()+1,0))) {
						try {
							SimpleCache.this.wait();
						} catch (InterruptedException e1) {
							// ignore
						}
					}
					if (num >= numthreads) return;
					e = prefetchList.poll();
					logger.debug("prefetch thread got " + e.key + " state " + e.state.getName() + " -> PREFETCH_RUNNING");
					e.state = EntryStateEnum.PREFETCH_RUNNING;
					buildingPrefetchList.add(e);
				}
				buildEntryPrefetch (e);
			}
		}
	}
	
	private final String name;
	private final Map<Key,Entry> cache;
	private int cacheAge;
	private final Builder builder;
	private final EntryList prefetchList;
	private final EntryList buildingPrefetchList;
	private final EntryList buildingGetList;
	private final EntryList cacheList;
	private final EntryList readyList;
	private final EntryList failedList;
	private int cacheQueries;
	private int cacheHits;
	private int cacheHitPrefetch;
	private int cacheBuilt;
	private int prefetchQueries;
	private int prefetchHits;
	private int prefetchBuilt;
	private int buildFailed;
	private int validateFailed;
//	private final EntryList deadList;
	private int numthreads;
	private final ThreadGroup prefetchGroup;
	private final ArrayList<PrefetchThread> prefetchThreads;

	public SimpleCache (String name, Builder builder) {
		this.name = name;
		cache = new HashMap<Key,Entry>();
		this.builder = builder;
		cacheList = new EntryList(getPropertyInt("cache.maxentries", 10));
		cacheList.setMaxAge(getPropertyLong("cache.maxage", -1));
		cacheList.setMaxMemSize(getPropertyLong("cache.maxMemSize", -1));
		readyList = new EntryList(getPropertyInt("ready.maxentries", 1));
		readyList.setMaxAge(getPropertyLong("ready.maxage", 3600000));
		readyList.setMaxMemSize(getPropertyLong("ready.maxMemSize", -1));
		failedList = new EntryList(getPropertyInt("failed.maxentries", 1000));
		prefetchList = new EntryList();
		buildingPrefetchList = new EntryList();
		buildingGetList = new EntryList();
		resetCounters();
//		deadList = new EntryList();
		this.numthreads = getPropertyInt("prefetch.threads", 0);
		String threadname = getProperty("prefetch.threadname");
		if (threadname == null) threadname = name + "PrefetchThread";
		prefetchGroup = new ThreadGroup (threadname);
		prefetchThreads = new ArrayList<PrefetchThread>(numthreads);
		for (int i=0; i<numthreads; i++) {
			PrefetchThread t = new PrefetchThread (prefetchGroup, i);
			prefetchThreads.add(t);
			t.start();
		}
	}
	
	private static URL getResource (String resource) {
		URL ret = null;
		
		if (resource == null) return null;
		try {
			ret = new URL(resource);
		} catch (MalformedURLException e) {
			logger.debug("Malformed url" + resource);
		}
		if (ret != null) return ret;
		ret = SimpleCache.class.getResource(resource);
		if (ret != null) return ret;
		ret = SimpleCache.class.getClassLoader().getResource (resource);
		if (ret != null) return ret;
		ret = ClassLoader.getSystemResource(resource);
		return ret;
	}
	
	private static Properties loadProperties () {
		Properties defaultProps = new Properties();
		try {
			defaultProps.load(new StringReader(defaultProperties));
		} catch (IOException e1) {
			logger.error("IOException while reading default properties");
		}
		Properties properties = new Properties(defaultProps);
		String property_name = System.getProperty("simplecache.configuration","simplecache.properties");
		URL prop_resource = getResource (property_name);
		InputStream property_stream = null;
		
		if (prop_resource != null) {
			try {
				property_stream = prop_resource.openStream();
			} catch (IOException e1) {
				logger.error("IOException while opening property_stream");
			}
		}
		if (property_stream != null) {
			try {
				properties.load(property_stream);
			} catch (IOException e) {
				logger.error("IO Exception loading properties");
			}
		}
		return properties;
	}

	private String getProperty(String pname) {
		String val = cacheProperties.getProperty("simplecache." + name + "." + pname);
		if (val != null) return val;
		return cacheProperties.getProperty("simplecache.default." + pname);
	}

	@SuppressWarnings("unused")
	private String getProperty(String pname, String def) {
		String val = cacheProperties.getProperty("simplecache." + name + "." + pname);
		if (val != null) return val;
		return cacheProperties.getProperty("simplecache.default." + pname, def);
	}

	private int getPropertyInt(String pname, int def) {
		String val = cacheProperties.getProperty("simplecache." + name + "." + pname);
		if (val != null) return Integer.parseInt(val);
		val = cacheProperties.getProperty("simplecache.default." + pname);
		if (val != null) return Integer.parseInt(val);
		return def;
	}

	private long getPropertyLong(String pname, long def) {
		String val = cacheProperties.getProperty("simplecache." + name + "." + pname);
		if (val != null) return Long.parseLong(val);
		val = cacheProperties.getProperty("simplecache.default." + pname);
		if (val != null) return Long.parseLong(val);
		return def;
	}

	public ObjectName registerMBean (String domain) throws Exception {
		String basename = domain + ":type=SimpleCache,name=" + name;
		ObjectName objectName = new ObjectName(basename);
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		mbs.registerMBean(this, objectName);
		return objectName;
	}
	
	public ObjectName registerMBeanMultiple (String domain) throws Exception {
		String basename = domain + ":type=SimpleCache,name=" + name;
		ObjectName objectName = new ObjectName(basename);
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		int cnt = 0;
		synchronized (this.getClass()) {
			while (mbs.isRegistered(objectName)) {
				cnt++;
				objectName = new ObjectName(basename + "_" + cnt);
			}
			mbs.registerMBean(this, objectName);
		}
		return objectName;
	}
	
	public void unregisterMBean (ObjectName name) throws MBeanRegistrationException, InstanceNotFoundException {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		mbs.unregisterMBean(name);
	}
	
	private void buildEntryPrefetch (Entry e) {
		synchronized (e) {
			if (e.state != EntryStateEnum.PREFETCH_RUNNING) {
				logger.warn("Prefetch build stolen for " + e.key.toString());
				return;
			}
			boolean failed = false;
			try {
				logger.debug("buildEntryPrefetch build " + e.key);
				e.value = builder.build(e.key);
			} catch (Exception ex) {
				logger.error("Failure for prefetch building " + e.key.toString(), ex);
				failed = true;
				e.value = null;
			}
			long memSize = (e.value == null) ? 0 : builder.memSize(e.value);
			synchronized (this) {
				if (e.state != EntryStateEnum.PREFETCH_RUNNING) {
					throw new RuntimeException("Prefetch build status changed for " + e.key.toString());
				}
				if (failed) {
					buildingPrefetchList.remove(e);
					e.state = EntryStateEnum.FAILED;
					pruneList(failedList, e, "failedList");
					failedList.addToFront(e);
					buildFailed++;
					return;
				}
				logger.debug("buildEntryPrefetch state " + e.key + " " + e.state.getName() + " -> PREFETCH_READY");
				buildingPrefetchList.remove(e);
				e.state = EntryStateEnum.PREFETCH_READY;
				e.lastUsed = new Date();
				e.memSize = memSize;
				pruneList(readyList, e, "readyList");
				readyList.addToFront (e);
				this.notifyAll();
				prefetchBuilt++;
			}
		}
	}
	
	private Value buildEntry (Entry e) {
		synchronized (e) {
			synchronized (this) {
				switch (e.state) {
				case NEW_ENTRY:
					logger.debug("buildEntry state " + e.key + " NEW_ENTRY -> BUILD_RUNNING");
					e.state = EntryStateEnum.BUILD_RUNNING;
					buildingGetList.add(e);
					break;
				case PREFETCH_ON_QUEUE:
					logger.debug("buildEntry state " + e.key + " PREFETCH_ON_QUEUE -> BUILD_RUNNING");
					prefetchList.remove(e);
					e.state = EntryStateEnum.BUILD_RUNNING;
					buildingGetList.add(e);
					break;
				case BUILD_RUNNING:
					logger.debug("buildEntry state " + e.key + " BUILD_RUNNING");
					break;
				case PREFETCH_RUNNING:
					logger.debug("buildEntry state " + e.key + " PREFETCH_RUNNING -> BUILD_RUNNING");
					buildingPrefetchList.remove(e);
					e.state = EntryStateEnum.BUILD_RUNNING;
					buildingGetList.add(e);
					break;
				case CACHED:
					if (builder.validate(e.key, e.value)) {
						logger.debug("buildEntry state " + e.key + " CACHED");
						e.lastUsed = new Date();
						cacheList.moveToFront(e);
						cacheHits++;
						return e.value;
					} else {
						logger.debug("buildEntry state " + e.key + " CACHED but failed validate");
						cacheList.remove(e);
						e.value = null;
						e.lastUsed = new Date();
						e.state = EntryStateEnum.BUILD_RUNNING;
						buildingGetList.add(e);
						validateFailed++;
						break;
					}
				case PREFETCH_READY:
					if (builder.validate(e.key, e.value)) {
						logger.debug("buildEntry state " + e.key + " PREFETCH_READY -> CACHED");
						readyList.remove(e);
						e.lastUsed = new Date();
						e.state = EntryStateEnum.CACHED;
						pruneList (cacheList, e, "cacheList");
						cacheList.addToFront(e);
						cacheHitPrefetch++;
						this.notifyAll();
						return e.value;
					} else {
						logger.debug("buildEntry state " + e.key + " PREFETCH_READY but failed validate");
						readyList.remove(e);
						e.lastUsed = new Date();
						e.value = null;
						e.state = EntryStateEnum.BUILD_RUNNING;
						buildingGetList.add(e);
						validateFailed++;
						this.notifyAll();
						break;
					}
				default:
					throw new RuntimeException("Unexpected get state " + e.state.toString() + " for " + e.key.toString());
				}
			}
			Value v;
			String failure = null;
			try {
				logger.debug("buildEntry build " + e.key);
				v = builder.build(e.key);
			} catch (Exception ex) {
				logger.error("Failure for get building " + e.key.toString(), ex);
				failure = ex.getMessage();
				v = null;
			}
			e.value = v;
			long memSize = (v == null) ? 0 : builder.memSize(v);
			synchronized (this) {
				if (e.state != EntryStateEnum.BUILD_RUNNING) {
					logger.error("Get build status changed for " + e.key.toString());
				}
				if (failure != null) {
					buildingGetList.remove(e);
					pruneList(failedList, e, "failedList");
					e.state = EntryStateEnum.FAILED;
					failedList.addToFront(e);
					buildFailed++;
					throw new RuntimeException("Simple cache build failed: " + failure);
				}
				logger.debug("buildEntry state " + e.key + " " + e.state.getName() + " -> CACHED");
				buildingGetList.remove(e);
				e.state = EntryStateEnum.CACHED;
				e.lastUsed = new Date();
				e.memSize = memSize;
				pruneList (cacheList, e, "cacheList");
				cacheList.addToFront (e);
				cacheBuilt++;
			}
			return v;
		}
	}
		
	private int pruneList (EntryList l, Entry space, String listName) {
		int ret = 0;
		Entry e = l.getOverflow(space);
		while (e != null) {
			logger.debug("pruneList pruned " + listName + " " + e.key + " state " + e.state.getName());
			l.remove(e);
			cache.remove(e.key);
			ret++;
			e = l.getOverflow(space);
		}
		return ret;
	}

	public void preFetch (Key k) {
		if (numthreads == 0) return;
		Entry e;
		synchronized (this) {
			prefetchQueries++;
			e = cache.get(k);
			if (e != null && e.state != EntryStateEnum.FAILED) {
				logger.debug("preFetch " + k + " hit");
				prefetchHits++;
				return;
			}
			logger.debug("preFetch " + k + " miss, state -> PREFETCH_ON_QUEUE");
			if (e == null) {
				e = new Entry (k);
				cache.put(k, e);
			} else {
				failedList.remove(e);
			}
			e.state = EntryStateEnum.PREFETCH_ON_QUEUE;
			prefetchList.add(e);
			this.notifyAll();
		}
	}
	
	public Value get (Key k) {
		Entry e;
		synchronized (this) {
			cacheQueries++;
			e = cache.get(k);
			if (e != null && e.state != EntryStateEnum.FAILED) {
				logger.debug("get " + k + " hit, state = " + e.state.getName());
			} else {
				logger.debug("get " + k + " miss, state -> BUILD_RUNNING");
				if (e == null) {
					e = new Entry (k);
					cache.put(k, e);
				} else {
					failedList.remove(e);
				}
				e.state = EntryStateEnum.BUILD_RUNNING;
				buildingGetList.add(e);
			}
		}
		return buildEntry(e);
	}

	@Override
	public int getMaxEntries() {
		return cacheList.getMaxSize();
	}

	@Override
	public void setMaxEntries(int maxsize) {
		synchronized (this) {
			cacheList.setMaxSize (maxsize);
			pruneList(cacheList, null, "cacheList");
		}
	}

	@Override
	public int getNumEntries() {
		return cacheList.size();
	}
	
	@Override
	public long getMaxMemSize() {
		return cacheList.getMaxMemSize();
	}
	
	@Override
	public void setMaxMemSize(long maxMemSize) {
		synchronized (this) {
			cacheList.setMaxMemSize(maxMemSize);
			pruneList(cacheList, null, "cacheList");
		}
	}
	
	@Override
	public long getMemSize() {
		return cacheList.memSize();
	}
	
	@Override
	public long getMaxEntryAge() {
		return cacheList.getMaxAge();
	}

	@Override
	public void setMaxEntryAge(long age) {
		synchronized (this) {
			cacheList.setMaxAge(age);
			pruneList(cacheList, null, "cacheList");
		}
	}

	@Override
	public String[] getEntries() {
		synchronized (this) {
			return cacheList.getEntries();
		}
	}
	
	@Override
	public String[] getEntriesDetailed() {
		synchronized (this) {
			String[] entrylist = new String[cache.size()];
			int i = 0;
			for (Map.Entry<Key,Entry> me : cache.entrySet()) {
				Entry e = me.getValue();
				entrylist[i++] = e.state.toString() + "|" + e.key.toString() + "|" + e.lastUsed + "|" + e.age;
			}
			Arrays.sort(entrylist);
			return entrylist;
		}
	}

	@Override
	public int getMaxPendingEntries() {
		return readyList.getMaxSize();
	}

	@Override
	public void setMaxPendingEntries(int maxsize) {
		synchronized (this) {
			readyList.setMaxSize(maxsize);
			if (pruneList(readyList, null, "readyList") > 0) this.notifyAll();
		}
	}

	@Override
	public long getMaxPendingMemSize() {
		return readyList.getMaxMemSize();
	}
	
	@Override
	public void setMaxPendingMemSize(long maxMemSize) {
		synchronized (this) {
			readyList.setMaxMemSize(maxMemSize);
			if (pruneList(readyList, null, "readyList") > 0) this.notifyAll();
		}
	}
	
	@Override
	public int getNumPendingEntries() {
		return readyList.size();
	}

	@Override
	public long getPendingMemSize() {
		return readyList.memSize();
	}
	
	@Override
	public long getMaxPendingEntryAge() {
		return readyList.getMaxAge();
	}

	@Override
	public void setMaxPendingEntryAge(long age) {
		synchronized (this) {
			readyList.setMaxAge(age);
			if (pruneList(readyList, null, "readyList") > 0) this.notifyAll();
		}
	}

	@Override
	public String[] getPendingEntries() {
		synchronized (this) {
			return readyList.getEntries();
		}
	}
	
	@Override
	public int getNumPrefetchQueued() {
		return prefetchList.size();
	}

	@Override
	public String[] getPrefetchQueuedEntries() {
		synchronized (this) {
			return prefetchList.getEntries();
		}
	}
	
	@Override
	public int getNumBuildingPrefetch() {
		return buildingPrefetchList.size();
	}
	
	@Override
	public String[] getBuildingPrefetchEntries() {
		synchronized (this) {
			return buildingPrefetchList.getEntries();
		}
	}
	
	@Override
	public int getNumBuildingGet() {
		return buildingGetList.size();
	}
	
	@Override
	public String[] getBuildingGetEntries() {
		synchronized (this) {
			return buildingGetList.getEntries();
		}
	}
	
	@Override
	public int getMaxFailedEntries() {
		return failedList.getMaxSize();
	}
	
	@Override
	public int getNumFailedEntries() {
		return failedList.size();
	}
	
	@Override
	public void setMaxFailedEntries(int maxsize) {
		synchronized (this) {
			failedList.setMaxSize (maxsize);
			pruneList(failedList, null, "failedList");
		}
	}
	
	@Override
	public String[] getFailedEntries() {
		synchronized (this) {
			return failedList.getEntries();
		}
	}
	
	@Override
	public int getNumThreads () {
		return numthreads;
	}

	@Override
	public void setNumThreads(int n) {
		if (n < 0) n = 0;
		synchronized (prefetchThreads) {
			int oldnumthreads = numthreads;
			synchronized (this) {
				numthreads = n;
				this.notifyAll();
			}
			if (numthreads < oldnumthreads) {
				while (prefetchThreads.size() > numthreads) {
					PrefetchThread t = prefetchThreads.remove(prefetchThreads.size()-1);
					boolean joined = false;
					while (!joined) {
						try {
							t.join();
							joined = true;
						} catch (InterruptedException e) {
						}
					}
				}
			} else if (numthreads > oldnumthreads) {
				for (int i=oldnumthreads; i<numthreads; i++) {
					PrefetchThread t = new PrefetchThread (prefetchGroup, i);
					prefetchThreads.add(t);
					t.start();
				}
			}
		}		
	}

	@Override
	public int getActiveThreadCount() {
		return prefetchGroup.activeCount();
	}

	@Override
	public String[] getThreadNames() {
		String[] names = new String[prefetchThreads.size()];
		synchronized (prefetchThreads) {
			for (int i=0; i<prefetchThreads.size(); ++i) {
				names[i] = prefetchThreads.get(i).getName();
			}
		}
		return names;
	}
	
	@Override
	public void resetCounters() {
		cacheQueries = 0;
		cacheHits = 0;
		cacheHitPrefetch = 0;
		cacheBuilt = 0;
		prefetchQueries = 0;
		prefetchHits = 0;
		prefetchBuilt = 0;
		buildFailed = 0;
		validateFailed = 0;
	}

	@Override
	public void flushCache() {
		synchronized (this) {
			int old_max = cacheList.getMaxSize();
			cacheList.setMaxSize (0);
			pruneList(cacheList, null, "cacheList");
			cacheList.setMaxSize(old_max);
			old_max = failedList.getMaxSize();
			failedList.setMaxSize(0);
			pruneList(failedList, null, "failedList");
			failedList.setMaxSize(old_max);
		}
	}
	
	@Override
	public int getCacheQueries() {
		return cacheQueries;
	}

	@Override
	public int getCacheHits() {
		return cacheHits;
	}

	@Override
	public int getCacheHitPrefetch() {
		return cacheHitPrefetch;
	}

	@Override
	public int getBuildFailed() {
		return buildFailed;
	}
	
	@Override
	public int getValidateFailed() {
		return validateFailed;
	}
	
	@Override
	public int getCacheBuilt() {
		return cacheBuilt;
	}

	@Override
	public int getPrefetchQueries() {
		return prefetchQueries;
	}

	@Override
	public int getPrefetchHits() {
		return prefetchHits;
	}

	@Override
	public int getPrefetchBuilt() {
		return prefetchBuilt;
	}
}
