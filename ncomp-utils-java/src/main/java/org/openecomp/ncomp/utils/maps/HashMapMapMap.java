
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

public class HashMapMapMap<K1,K2,K3,V> extends HashMap<K1,HashMapMap<K2,K3,V>> {
	private static final long serialVersionUID = 1L;

	public void insert(K1 key1, K2 key2, K3 key3, V value) {
		HashMapMap<K2,K3,V> l = super.get(key1);
		if (l == null) {
			l = new HashMapMap<K2,K3,V>();
			super.put(key1, l);
		}
		l.insert(key2,key3, value);
	}
	public boolean contains(K1 key1, K2 key2, K3 key3) {
		HashMapMap<K2,K3,V> l = super.get(key1);
		if (l == null) return false;
		return l.contains(key2,key3);
	}
	
	public void debug(String surfix, Logger logger) {
		if (keySet().size() == 0) {
			logger.debug(surfix + "EMPTY!!");
		}
		for (K1 k : keySet()) {
			HashMapMap<K2,K3,V> l = super.get(k);
			for (K2 k2 : l.keySet()) {
				HashMap<K3,V> l2 = l.get(k2);
				for (K3 k3 : l2.keySet()) {
					logger.debug(surfix + k + " : " + k2 + " : " + k3 + " : " + l2.get(k3));
				}
			}
		}
	}
	
	public V get(K1 k1, K2 k2, K3 k3) {
		HashMapMap<K2,K3,V> l = super.get(k1);
		if (l == null)
			return null;
		return l.get(k2,k3);
	}
}
