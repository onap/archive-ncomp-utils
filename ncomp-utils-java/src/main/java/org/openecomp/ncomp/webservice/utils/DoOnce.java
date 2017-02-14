
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

import java.util.HashSet;

public class DoOnce {
	private HashSet<String> doneMap = new HashSet<String>();
	public boolean done(String string) {
		if (doneMap.contains(string)) return true;
		doneMap.add(string);
		return false;
	}
	public boolean done(Object o) {
		return done(o.toString());
	}
	public boolean first(Object o) {
		return done(o) == false;
	}
	public boolean done(int i) {
		return done(Integer.toString(i));
	}
	public void clear() {
		doneMap.clear();
	}
}
