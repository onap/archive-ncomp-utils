
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

public class CounterMap<K> extends HashMap<K,Integer> {
	private static final long serialVersionUID = 1L;

	public void increment(K key) {
		Integer i = super.get(key);
		int j;
		if (i == null) {
			super.put(key, 0);
			j = 0;
		}
		else j = i;
		put(key, j+1);
	}
	public Integer get(Object key) {
		Integer i = super.get(key);
		if (i == null) return 0;
		return i;
	}
}
