
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
import java.util.List;
import java.util.Set;

public class ConnectedMap<N> {
	static final long serialVersionUID = 1L;
	HashMap<N,N> representive = new HashMap<N, N>();
	HashMapList<N,N> rep2list = new HashMapList<N,N>();
	public N add(N node) {
		N n = representive.get(node);
		if (n == null) {
			representive.put(node, node);
			rep2list.insert(node, node);
			n = node;
		}
		return n;
	}	
	public void join (N n1, N n2) {
//		System.out.println("join: " + n1 + " " + n2);
		N r1 = add(n1);
		N r2 = add(n2);
		if (r1 == r2) return;
		if (rep2list.get(r1).size() < rep2list.get(r2).size()) {
			N r3 = r1;
			r1 = r2;
			r2 = r3;
		}
		List<N> l = rep2list.get(r1);
		for (N n : rep2list.get(r2)) {
			representive.put(n, r1);
			l.add(n);
		}
		rep2list.remove(r2);
	}
	public Set<N> reps() {
		return rep2list.keySet();
	}
	public List<N> values(N n) {
		return rep2list.get(add(n));
	}
}
