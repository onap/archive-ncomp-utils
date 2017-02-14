
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
	
package org.openecomp.ncomp.webservice.utils;

import java.util.HashMap;

public class DoOnceArray {
	private HashMap<Object,DoOnce> map = new HashMap<Object,DoOnce>();
	public boolean done(Object o, String string) {
		DoOnce d = map.get(o);
		if (d == null) {
			d = new DoOnce();
			map.put(o,d);
		}
		return d.done(string);
	}
	public boolean done(Object o,Object o2) {
		return done(o,o2.toString());
	}
	public boolean first(Object o,Object o2) {
		return done(o,o2) == false;
	}
	public boolean done(Object o,int i) {
		return done(o,Integer.toString(i));
	}
	public void clear() {
		map.clear();
	}

}
