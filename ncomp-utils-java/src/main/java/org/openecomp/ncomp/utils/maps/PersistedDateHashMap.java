
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
	
package org.openecomp.ncomp.utils.maps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.openecomp.ncomp.utils.SortUtil;
import org.openecomp.ncomp.webservice.utils.DateUtils;

public class PersistedDateHashMap<K extends Serializable, V extends Serializable> {
	private String filePrefix;
	private HashMap<Date, PersistedHashMap<K, V>> map = new HashMap<Date, PersistedHashMap<K, V>>();
	private HashMap<Date, Boolean> uptodate = new HashMap<Date, Boolean>();
	private HashMap<Date, Date> createdTime = new HashMap<Date, Date>();

	public PersistedDateHashMap(String filePrefix) {
		super();
		this.filePrefix = filePrefix;
	}

	public void save() {
		for (Date d : map.keySet()) {
			if (!uptodate.get(d)) {
				map.get(d).save();
				uptodate.put(d, true);
			}
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
			uptodate.remove(d);
			createdTime.remove(d);
		}
	}

	final long msInDay = DateUtils.stringToDuration("1day");

	public void insert(Date d, K k, V v) {
		getPmap(d).put(k, v);
		setUpdate(d, false);
	}

	public void remove(Date d, K k) {
		getPmap(d).remove(k);
		setUpdate(d, false);
	}

	public V get(Date d, K k) {
		return getPmap(d).get(k);
	}

	private void setUpdate(Date d, boolean b) {
		Date d1 = new Date(d.getTime() / msInDay * msInDay);
		uptodate.put(d1, b);
	}

	private PersistedHashMap<K, V> getPmap(Date d) {
		Date d1 = new Date(d.getTime() / msInDay * msInDay);
		PersistedHashMap<K, V> pMap = map.get(d1);
		if (pMap == null) {
			pMap = new PersistedHashMap<K, V>(dbFile(d1));
			map.put(d1, pMap);
			uptodate.put(d1, false);
			createdTime.put(d1, new Date());
		}
		return pMap;
	}

	private String dbFile(Date d) {
		return DateUtils.toString(filePrefix + "-%Y_%m_%d", d);
	}

	public void dump() {
		for (Date d : SortUtil.sort(map.keySet())) {
			System.out.println("Status for: " + d);
			HashMap<K, V> m = map.get(d);
			for (K k : m.keySet()) {
				System.out.println("  " + k + " " + m.get(k));
			}
		}
	}
}
