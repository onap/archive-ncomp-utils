
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class HashTDoubleMap {
	public static <T> HashMap<T, Double> topN(final HashMap<T, Double> map, int n) {
		HashMap<T, Double> res = new HashMap<T, Double>();
		List<T> l = new ArrayList<T>(map.keySet());
		Collections.sort(l, new Comparator<T>() {
			@Override
			public int compare(T arg0, T arg1) {
				Double v1 = map.get(arg0);
				Double v2 = map.get(arg1);
				return v1.compareTo(v2);
			}
		});
		for (T key : l) {
			res.put(key, map.get(key));
			if (res.size() >= n)
				break; 
		}
		return res;
	}
	public static <T> void incr(final HashMap<T, Double> map, T i, double n) {
		Double ii = map.get(i);
		if (ii == null) {
			ii = new Double(0);
		}
		map.put(i, ii + n);
	}
	public static <T> void incr(final HashMap<T, Double> map, T i) {
		incr(map,i,1);
	}
}
