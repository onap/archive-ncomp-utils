
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
import org.apache.log4j.Logger;

public class HashMapMap<K1,K2,V> extends HashMap<K1,HashMap<K2,V>> {
	private static final long serialVersionUID = 1L;

	public void insert(K1 key1, K2 key2, V value) {
		HashMap<K2,V> l = super.get(key1);
		if (l == null) {
			l = new HashMap<K2,V>();
			super.put(key1, l);
		}
		l.put(key2, value);
	}
	public boolean contains(K1 key1, K2 key2) {
		HashMap<K2,V> l = super.get(key1);
		if (l == null) return false;
		return l.containsKey(key2);
	}
	
	public void debug(String surfix, Logger logger) {
		if (keySet().size() == 0) {
			logger.debug(surfix + "EMPTY!!");
		}
		for (K1 k : keySet()) {
			HashMap<K2,V> l = super.get(k);
			for (K2 k2 : l.keySet()) {
				logger.debug(surfix + k + " : " + k2 + " : " + l.get(k2));
			}
		}
	}
	public V get(K1 k1, K2 k2) {
		HashMap<K2,V> l = super.get(k1);
		if (l == null)
			return null;
		return l.get(k2);
	}
}
