
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

public class HashMapList<K, V> extends HashMap<K, ArrayList<V>> {
	private static final long serialVersionUID = 1L;

	public void insert(K key, V value) {
		ArrayList<V> l = super.get(key);
		if (l == null) {
			l = new ArrayList<V>();
			super.put(key, l);
		}
		if (value != null) 
			l.add(value);
	}
	public void addAll(K key, Collection<V> value) {
		ArrayList<V> l = super.get(key);
		if (l == null) {
			l = new ArrayList<V>();
			super.put(key, l);
		}
		if (value != null) 
			l.addAll(value);
	}
	public boolean contains(K k,V v) {
		if (!containsKey(k)) return false;
		return get(k).contains(v);
	}
	public List<V> getList(K k) {
		if (containsKey(k)) return get(k);
		return new ArrayList<V>();
	}

	public void debug(String surfix, Logger logger) {
		if (keySet().size() == 0) {
			logger.debug(surfix + "EMPTY!!");
		}
		for (K k : keySet()) {
			logger.debug(surfix + k + " : " + get(k));
		}
	}
}
