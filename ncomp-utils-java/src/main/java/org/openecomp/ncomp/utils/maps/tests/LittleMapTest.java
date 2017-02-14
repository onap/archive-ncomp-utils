
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

import java.util.Map;

import org.openecomp.ncomp.utils.maps.LittleMap;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class LittleMapTest extends TestCase {

	public static void main(String[] args) {
		TestRunner.run(LittleMapTest.class);
	}

	public void testLittleMap() {
		Map<String, String> m = new LittleMap<String, String>();
		assertSame("empty map size should be 0", m.size(), 0);
		for (@SuppressWarnings("unused") Map.Entry<String, String> e : m.entrySet()) {
			fail("map should be empty");
		}
		if (m.containsKey("1")) {
			fail("map should be empty");
		}
		assertNull("1 shouldn't already exist in map", m.put("1", "v1"));
		assertSame("map size should be 1", m.size(), 1);
		for (Map.Entry<String, String> e : m.entrySet()) {
			assertSame("entry key should be 1", e.getKey(), "1");
			assertSame("entry value should be v1", e.getValue(), "v1");
		}
		assertTrue("m should contain 1", m.containsKey("1"));
		assertSame("value of 1 should be v1", m.get("1"), "v1");
		assertSame("remove val should be v1", m.remove("1"), "v1");
		assertTrue("should be empty after removal", m.isEmpty());
		m.put("1", "v1");
		m.put("2", "v2");
		m.put("3", "v3");
		assertSame("value of put should be v2", m.put("2", "newv2"), "v2");
		assertSame("map size should be 3", m.size(), 3);
		m.remove("1");
		m.remove("2");
		assertSame("map size should be 1", m.size(), 1);
		m.put("2", "v2");
		m.put("1", "v1");
		m.put("4", "v4");
		m.put("5", "v5");
		m.put("6", "v6");
		assertSame("map size should be 6", m.size(), 6);
		assertSame("value of 4 should be v4", m.get("4"), "v4");
	}
}
