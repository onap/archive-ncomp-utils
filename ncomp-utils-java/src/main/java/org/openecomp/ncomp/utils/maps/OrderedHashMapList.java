
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
import java.util.HashMap;
import java.util.List;

/**
 * Data structure for an mapping from <K>s to lists of <V>s. The data structure keeps track on the 
 * order new K was added and allow to retrive them in the that order.
 * <p>
 * Use the insert and delete operations. Do not use puts and remove.
 *
 * @param <K> Type of Keys
 * @param <V> Type of Values
 */
public class OrderedHashMapList<K, V> extends HashMap<K,ArrayList<V>> {
	private static final long serialVersionUID = 1L;
	private ArrayList<K> list = new ArrayList<K>();
	public void insert(K key, V value) {
		ArrayList<V> l = super.get(key);
		if (l == null) {
			l = new ArrayList<V>();
			super.put(key, l);
			list.add(key);
		}
		l.add(value);
	}
	public ArrayList<V> delete(K key) {
		list.remove(key);
		return super.remove(key);
	}
//	public Object remove(K key) {
//		throw new RuntimeException("USE DELETE");
//	}
	@Override
	public void clear() {
		list.clear();
		super.clear();
	}
	
	/**
	 * @return the key set in the order they were first added.
	 */
	public List<K> orderedKeySet() {
		return list;
	}
}
