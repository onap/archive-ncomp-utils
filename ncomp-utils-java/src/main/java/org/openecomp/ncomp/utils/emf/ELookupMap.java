
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
	
package org.openecomp.ncomp.utils.emf;

import java.util.HashMap;

import org.eclipse.emf.ecore.EObject;


public class ELookupMap<T extends EObject> {
	private HashMap<String,T> map = new HashMap<String,T>();
	private boolean ignoreCase = false;
	public boolean isIgnoreCase() {
		return ignoreCase;
	}
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	int featureId;
	int featureId1[] = null;
	int featureId2[][] = null;
	EUtils<T> util = new EUtils<T>(null);
	public ELookupMap(int featureIdX) {
		featureId = featureIdX;
	}
	public ELookupMap(int featureIdX[]) {
		featureId1 = featureIdX;
	}
	public T get(String str) {
		if (ignoreCase) str = str.toLowerCase();
		return map.get(str);
	}
	public void add(T e) {
		String key;
		if (featureId2 != null) {
			key = util.ecore2str(e, featureId2);
		} 
		else if (featureId1 != null) {
			key = util.ecore2str(e, featureId1);
		}
		else {
			key = util.ecore2str(e, featureId);
		}
		if (ignoreCase) key = key.toLowerCase();
		if (map.get(key) != null) throw new RuntimeException("ELookupMap: adding object with existing key;" + key);
		map.put(key, e);
	}
	public T get(int s) {
		return get(Integer.toString(s));
	}
	public T get(EObject o, String s) {
		return get(o.toString()+ util.delim[0] + s);
	}
	public T get(String s, EObject o) {
		return get(s + util.delim[0] + o.toString());
	}
	public T get(String s, String s1) {
		return get(s + util.delim[0] + s1);
	}
	public int size() {
		return map.size();
	}
}
