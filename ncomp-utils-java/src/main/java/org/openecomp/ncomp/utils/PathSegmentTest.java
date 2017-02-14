
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.openecomp.ncomp.utils.FindFiles.ParameterizedFile;
import org.openecomp.ncomp.utils.FindFiles.PathSegment;

public class PathSegmentTest extends TestCase {
	private static final String[] parameters = { "p1", "p2" };
	private static final String[] values = { "ab.*", "cc" };

	public void testPathSegment() {
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date d = null;
		try {
			d = df.parse("2/3/2010");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FindFiles f = new FindFiles("",parameters);
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));;
		ParameterizedFile pfile = f.new ParameterizedFile(d);
		PathSegment p = f.new PathSegment("foo%1%Y%S%M%Hbar", d, values);
		if (!p.match("foocc2010450212bar", pfile, cal))
			fail("should match");
		Date date = cal.getTime();
		if (!String.format("%tS",date).equals("45"))
			fail("should match");
		PathSegment p2 = f.new PathSegment("foo%0%Y/%m/%d%S%M%Hbar", d, values);
		if (p2.match("foocc2010/02/03460212bar", pfile, cal))
			fail("should not match");
		System.out.println(pfile);
		if (!p2.match("fooabcc2010/02/03470212bar", pfile, cal))
			fail("should match");
		System.out.println(pfile);
		if (!String.format("%tS",cal.getTime()).equals("47"))
			fail("should match");
		System.out.println(pfile);
		PathSegment p3 = f.new PathSegment("%U", d, values);
		if (!p3.match("222222", pfile,cal))
			fail("should match");
		System.out.println(pfile);
		if (!String.format("%tS",cal.getTime()).equals("42"))
			fail("should match");
	}
}
