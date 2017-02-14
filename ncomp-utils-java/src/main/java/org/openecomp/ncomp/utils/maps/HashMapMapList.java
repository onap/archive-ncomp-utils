
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class HashMapMapList<K1,K2,V> extends HashMap<K1,HashMapList<K2, V>> {
	private static final long serialVersionUID = 1L;

	public void insert(K1 key1, K2 key2, V value) {
		HashMapList<K2, V> l = super.get(key1);
		if (l == null) {
			l = new HashMapList<K2, V>();
			super.put(key1, l);
		}
		l.insert(key2,value); 
	}
	public void addAll(K1 key1, K2 key2, Collection<V> value) {
		HashMapList<K2, V> l = super.get(key1);
		if (l == null) {
			l = new HashMapList<K2, V>();
			super.put(key1, l);
		}
		l.addAll(key2,value); 
	
	}
	public List<V> get(K1 key1, K2 key2) {
		HashMapList<K2, V> l = super.get(key1);
		if (l == null) return null;
		return l.get(key2);
	}
}
