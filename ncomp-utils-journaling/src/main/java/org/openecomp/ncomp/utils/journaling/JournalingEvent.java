
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
	
package org.openecomp.ncomp.utils.journaling;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JournalingEvent implements Serializable {
	@Override
	public String toString() {
		return "JournalingEvent [context=" + context + ", o=" + value + ", method=" + method + "]";
	}
	private static final long serialVersionUID = -7697839584139633393L;
	List<String> context = new ArrayList<String>();
	Object value = null;
	int method = -1;
	public String pname;
	public JournalingEvent() {} 
	public JournalingEvent(String context, int method, String pname, Object o) {
		if (context != null)
			this.context.add(context);
		this.value = o;
		this.method = method;
		this.pname = pname;
	}
	public void addContext(JournalingObject journalingObject) {
		context.add(journalingObject.getContext());
	}
}
