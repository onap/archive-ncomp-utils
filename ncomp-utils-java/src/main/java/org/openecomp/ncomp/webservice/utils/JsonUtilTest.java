
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

import junit.framework.TestCase;

import org.json.JSONObject;

public class JsonUtilTest extends TestCase {
    public void test_getStringValue() {
    	JSONObject json= new JSONObject("{id:'dr.t','r':1.2,b:false,i:23,'metadata':{'empty':'foo'}}");
    	assertEquals("dr.t", JsonUtils.getStringValue(json, "id"));
    	assertEquals("foo", JsonUtils.getStringValue(json, "metadata.empty"));
    	assertEquals("1.2", JsonUtils.getStringValue(json, "r"));
    	assertEquals("false", JsonUtils.getStringValue(json, "b"));
    	assertEquals("23", JsonUtils.getStringValue(json, "i"));
    	json = new JSONObject("{'id':'dr.test.INOUTOCTETS','feedname':'someone01','metadata':{'timestamp':1360031100000,'qname':'dr.test.INOUTOCTETS'}}");
    	assertEquals("1360031100000", JsonUtils.getStringValue(json, "metadata.timestamp"));
    }
    public void test_quotes() {
    	JSONObject json= new JSONObject();
    	json.put("aaa'\"bb", "a''\"b");
    	String s = json.toString();
    	JSONObject json2 = new JSONObject(s);
    	String s2 = json2.toString();
    	assertEquals(s, s2);
    }
}
