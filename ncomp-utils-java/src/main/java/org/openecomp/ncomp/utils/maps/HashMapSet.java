
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

import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

public class HashMapSet<K, V> extends HashMap<K,HashSet<V>> {
	private static final long serialVersionUID = 1L;

	public void insert(K key, V value) {
		HashSet<V> l = super.get(key);
		if (l == null) {
			l = new HashSet<V>();
			super.put(key, l);
		}
		l.add(value);
	}
	public boolean contains(K key, V v) {
		HashSet<V> l = super.get(key);
		if (l == null) return false;
		return l.contains(v);
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
