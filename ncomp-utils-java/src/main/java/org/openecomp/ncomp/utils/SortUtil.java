
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
	
package org.openecomp.ncomp.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.openecomp.ncomp.utils.maps.HashMapList;

public class SortUtil {
	
	public static
	<K extends Comparable<? super K>,V> LinkedHashMap<K, V> sortMapByKey(HashMap<K,V> m) {
		LinkedHashMap<K, V> res = new LinkedHashMap<K, V>();
		for (K k : SortUtil.sort(m.keySet())) {
			res.put(k,m.get(k));
		}
		return res;
	}
	public static
	<K,V extends Comparable<? super V>> LinkedHashMap<K, V> sortMapByValue(HashMap<K,V> m) {
		LinkedHashMap<K, V> res = new LinkedHashMap<K, V>();
		HashMapList<V,K> m1 = new HashMapList<V, K>();
		for (K k : m.keySet()) {
			m1.insert(m.get(k), k);
		}
		for (V v : SortUtil.sort(m1.keySet())) { 
			for (K k: m1.get(v)) {
				res.put(k,v);
			}
		} 
		return  res;
	}
	
	public static
	<T extends Comparable<? super T>> List<T> sort(Collection<T> c) {
	  List<T> list = new ArrayList<T>(c);
	  java.util.Collections.sort(list);
	  return list;
	}
	
	public static
	<T> List<T> sort(Collection<T> c, Comparator<? super T> comp) {
	  List<T> list = new ArrayList<T>(c);
	  java.util.Collections.sort(list,comp);
	  return list;
	}

	public static
	<T extends Comparable<? super T>>
	int compareList (Collection<T> a, Collection<T> b) {
		Iterator<T> ai = a.iterator();
		Iterator<T> bi = b.iterator();
		while (ai.hasNext() && bi.hasNext()) {
			int ret = ai.next().compareTo(bi.next());
			if (ret != 0) return ret;
		}
		if (bi.hasNext()) return 1;
		if (ai.hasNext()) return -1;
		return 0;
	}
	
	public static <T> int compareList (Collection<T> a, Collection<T> b, Comparator<? super T> comp) {
		Iterator<T> ai = a.iterator();
		Iterator<T> bi = b.iterator();
		while (ai.hasNext() && bi.hasNext()) {
			int ret = comp.compare(ai.next(), bi.next());
			if (ret != 0) return ret;
		}
		if (bi.hasNext()) return 1;
		if (ai.hasNext()) return -1;
		return 0;
	}

	public static
	<T extends Comparable<? super T>>
	int compareNull (T a, T b) {
		if (a == null && b == null) return 0;
		if (a == null) return 1;
		if (b == null) return -1;
		return a.compareTo(b);
	}
	
	public static <T> int compareNull (T a, T b, Comparator<? super T> comp) {
		if (a == null && b == null) return 0;
		if (a == null) return 1;
		if (b == null) return -1;
		return comp.compare(a,b);
	}
	
	public static int compareInt (int a, int b) {
		if (a < b) return -1;
		else if (a > b) return 1;
		else return 0;
	}
	public static int compareFloat (float a, float b) {
		if (a < b) return -1;
		else if (a > b) return 1;
		else return 0;
	}
	public static int compareDouble (double a, double b) {
		if (a < b) return -1;
		else if (a > b) return 1;
		else return 0;
	}
	public static int compareBoolean (boolean a, boolean b) {
		if (a && !b) return 1;
		else if (!a && b) return -1;
		else return 0;
	}


}
