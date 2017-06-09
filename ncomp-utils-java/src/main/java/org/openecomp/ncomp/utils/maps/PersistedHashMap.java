
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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import org.openecomp.ncomp.webservice.utils.FileUtils;

public class PersistedHashMap<K extends Serializable, V extends Serializable> extends HashMap<K, V> {
	private static final long serialVersionUID = -1926264388357597164L;
	private String file;

	@SuppressWarnings("unchecked")
	public PersistedHashMap(String file) {
		super();
		this.file = file;
		HashMap<K, V> m = null;
		try {
			m = (HashMap<K, V>) FileUtils.file2object(file,Arrays.asList(PersistedDateHashMap.class.getName()));
		} catch (Exception e) {
		}
		if (m != null) 
			putAll(m);
	}
	public void save() {
		FileUtils.object2file(this, file);
	}

}
