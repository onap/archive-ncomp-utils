
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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class JournalingDateObject<T extends JournalingObject> {
	public static final Logger logger = Logger
			.getLogger(JournalingDateObject.class);
	private String rootDir;
	private HashMap<Date, T> map = new HashMap<Date, T>();
	private HashMap<Date, Date> createdTime = new HashMap<Date, Date>();
	@SuppressWarnings("rawtypes")
	private Class clazz;

	@SuppressWarnings("rawtypes")
	public JournalingDateObject(String filePrefix, Class clazz) {
		this.rootDir = filePrefix;
		this.clazz = clazz;
	}

	public T get(Date d) {
		return getPmap(d);
	}

	public void save() {
		synchronized (this) {
			for (Date d : map.keySet()) {
				map.get(d).save();
			}
			Date now = new Date();
			List<Date> l = new ArrayList<Date>();
			for (Date d : map.keySet()) {
				if (createdTime.get(d).getTime() + msInDay < now.getTime()) {
					// created 1 day ago. remove from map to save memory
					l.add(d);
				}
			}
			for (Date d : l) {
				map.remove(d);
				createdTime.remove(d);
			}
		}
	}

	public void close() {
		save();
		for (Date d : map.keySet()) {
			map.get(d).close();
		}
	}

	private final long msInDay = 86400000L;

	@SuppressWarnings("unchecked")
	private T getPmap(Date d) {
		synchronized (this) {
			Date d1 = new Date(d.getTime() / msInDay * msInDay);
			T o = map.get(d1);
			if (o == null) {
				try {
					@SuppressWarnings("rawtypes")
					Constructor c = clazz.getConstructor();
					Method m = clazz.getMethod("create", File.class);
					o = (T) m.invoke(c.newInstance(), dbRootDir(d1));
					map.put(d1, o);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				createdTime.put(d1, new Date());
			}
			return o;
		}
	}

	private File dbRootDir(Date d) {
		return new File(rootDir + String.format("/%1$tY_%1$tm_%1$td", d));
	}

	public void dump() {
		synchronized (this) {
			for (Date d : map.keySet()) {
				T o = map.get(d);
				System.out.println("Status for: " + d + " " + o);
			}
		}
	}

	public JSONObject toJson() {
		synchronized (this) {
			JSONObject json = new JSONObject();
			for (Date d : map.keySet()) {
				JSONObject json1 = new JSONObject();
				json1.put("createdTime", createdTime.get(d));
				json1.put("object", map.get(d).toJson());
				json.put(d.toString(), json1);
			}
			return json;
		}
	}

}
