
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
	
package org.openecomp.ncomp.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.openecomp.ncomp.webservice.utils.DateUtils;

import junit.framework.TestCase;



public class StringMatcherTest extends TestCase {
    public void test_match() {
    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    	String x = "2014-01-04 20:01:51,987  WARN [qtp5338285-5879] org.openecomp.ncomp.bsa.controller.servers.BsaController";
    	String p = "%Y-%m-%d %H:%M:%S,${millisecond:...} +${sev} \\[.*\\] ${message}";
    	StringMatcher m = new StringMatcher(p);
    	HashMap<String,String> h = new HashMap<String, String>();
    	Date date = new Date();
		assertTrue(m.match(x, h, date));
    	assertEquals("2014-01-04 20:01:51",DateUtils.toString("%Y-%m-%d %H:%M:%S",date));
    	assertEquals("WARN",h.get("sev"));
    	assertEquals("987",h.get("millisecond"));
    	assertEquals("org.openecomp.ncomp.bsa.controller.servers.BsaController",h.get("message"));
    	String p2 = "%Y-%m-%d %H:%M:%S,%L +${sev} \\[.*\\] ${message}";
    	m = new StringMatcher(p2);
    	h = new HashMap<String, String>();
		assertTrue(m.match(x, h, date));
    	assertEquals("2014-01-04 20:01:51 987",DateUtils.toString("%Y-%m-%d %H:%M:%S %L",date));
    	assertEquals("WARN",h.get("sev"));
    	assertEquals("org.openecomp.ncomp.bsa.controller.servers.BsaController",h.get("message"));
    }
}
