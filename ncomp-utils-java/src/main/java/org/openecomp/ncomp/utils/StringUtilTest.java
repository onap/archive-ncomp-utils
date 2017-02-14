
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

import java.util.Properties;

import org.json.JSONObject;

import junit.framework.TestCase;



public class StringUtilTest extends TestCase {
    public void test_httpHeaderEncode() {
    	String json= "{'id':'dr.t','r':1.2,b:false,i:23,'metadata':{'empty':'foo','i':3}}";
    	String e = StringUtil.httpHeaderEncode(json);
    	String d = StringUtil.httpHeaderDecode(e);
    	assertEquals(json, d);
    	assertFalse(e.contains(","));
    }
    public void test_expandProperties() {
    	Properties props = new Properties();
    	props.put("fff", "FFF");
    	props.put("g", "FFF");
    	props.put("fff.4", "FFF");
    	assertEquals("FFFxxx",StringUtil.expandUsingProperties("${fff}xxx", props,"$"));
    	assertEquals("FFFxxFFFx",StringUtil.expandUsingProperties("${fff.4}xx${g}x", props,"$"));
    	assertEquals("${ff}xxx",StringUtil.expandUsingProperties("${ff}xxx", props,"$"));
    	assertEquals("FFFxxx",StringUtil.expandUsingProperties("%{fff}xxx", props,"%"));
    	assertEquals("FFFxxFFFx",StringUtil.expandUsingProperties("%{fff.4}xx%{g}x", props,"%"));
    	assertEquals("%{ff}xxx",StringUtil.expandUsingProperties("%{ff}xxx", props,"%"));
    }
    public void test_expandUsingJson() {
    	JSONObject json = new JSONObject("{'id':'dr.t','r':1.2,b:false,i:23,l:1372953720000,'metadata':{'empty':'foo','i':3}}");
    	assertEquals("yyydr.txxx",StringUtil.expandUsingJson("yyy${id}xxx", json,"$"));
    	assertEquals("yyyfooxxx",StringUtil.expandUsingJson("yyy${metadata.empty}xxx", json,"$"));
    	assertEquals("yyy3xxx",StringUtil.expandUsingJson("yyy${metadata.i}xxx", json,"$"));
    	assertEquals("yyy1.2xxx",StringUtil.expandUsingJson("yyy${r}xxx", json,"$"));
    	assertEquals("yyyfalsexxx",StringUtil.expandUsingJson("yyy${b}xxx", json,"$"));
    	assertEquals("yyy1372953720000xxx",StringUtil.expandUsingJson("yyy${l}xxx", json,"$"));
    }
}
