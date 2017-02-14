
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

public class HashTIntegerMap {
	public static <T> HashMap<T, Integer> topN(final HashMap<T, Integer> map, int n) {
		HashMap<T, Integer> res = new HashMap<T, Integer>();
		List<T> l = new ArrayList<T>(map.keySet());
		Collections.sort(l, new Comparator<T>() {
			@Override
			public int compare(T arg0, T arg1) {
				int v1 = map.get(arg0);
				int v2 = map.get(arg1);
				return v2-v1;
			}
		});
		for (T key : l) {
			res.put(key, map.get(key));
			if (res.size() >= n)
				break;
		}
		return res;
	}
	public static <T> void incr(final HashMap<T, Integer> map, T i, int n) {
		Integer ii = map.get(i);
		if (ii == null) {
			ii = new Integer(0);
		}
		map.put(i, ii + n);
	}
	public static <T> void incr(final HashMap<T, Integer> map, T i) {
		incr(map,i,1);
	}
}
