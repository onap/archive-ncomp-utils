
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
	
package org.openecomp.ncomp.utils.maps.tests;

import org.openecomp.ncomp.utils.maps.OrderedHashMapList;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class OrderedHashMapListTest extends TestCase {

	public static void main(String[] args) {
		TestRunner.run(OrderedHashMapListTest.class);
	}

	public void testOrderedHashMapList() {
		OrderedHashMapList<String, String> m = new OrderedHashMapList<String, String>();
		m.insert("1", "v1");
		m.insert("2", "v2");
		m.insert("3", "v3");
		m.insert("2", "v2.0");
		m.delete("2");
		m.insert("1", "v1.0");
		for (String k : m.orderedKeySet()) {
			System.out.println("key: " + k + " values: " + m.get(k));
			assertNotSame("2 should not be in the map", "2", k);
		}
	}

} 
