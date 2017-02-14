
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

public class SumMap<K> extends HashMap<K,Double> {
	private static final long serialVersionUID = 1L;

	public void increment(K key, double d) {
		Double i = super.get(key);
		double j;
		if (i == null) {
			super.put(key, 0.0);
			j = 0.0;
		}
		else j = i;
		put(key, j+d);
	}
	public Double get(Object key) {
		Double d = super.get(key);
		if (d == null) return 0d;
		return d;
	}
}
