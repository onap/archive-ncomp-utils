
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

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

public class DateUtilTest extends TestCase {
	String fmt ="%Y-%m-%d %H:%M:%S";
    public void test_dateFromString() throws ParseException {
    	System.setProperty("user.timezone", "GMT");
    	Date d = new Date();
    	assertEquals(
    			"2001-01-01 00:00:00",
    		DateUtils.toString(fmt,DateUtils.dateFromString("01/01/2001"))
    	);
    	assertEquals(
    			"2001-01-01 00:00:00",
    		DateUtils.toString(fmt,DateUtils.dateFromString("2001-01-01"))
        );
    	assertEquals("2001-01-01 02:02:02",
        		DateUtils.toString(fmt,DateUtils.dateFromString("2001-01-01T02:02:02.000"))
            );
    	assertEquals("2001-01-01 02:02:00",
        		DateUtils.toString(fmt,DateUtils.dateFromString("2001-01-01 02:02"))
            );
    	assertEquals(DateUtils.toString("%Y",d) + "-01-01 02:02:00",
        		DateUtils.toString(fmt,DateUtils.dateFromString("1/1 02:02"))
            );
    	assertEquals(DateUtils.toString("%Y",d) + "-01-01 00:00:00",
        		DateUtils.toString(fmt,DateUtils.dateFromString("1/1"))
            );

    	Calendar cal = Calendar.getInstance();
    	cal.setTime(d);
    	cal.add(Calendar.YEAR,-1);  
    	assertEquals(DateUtils.toString("%Y",cal.getTime()) + "-12-31 23:59:00",
        		DateUtils.toString(fmt,DateUtils.dateFromString("12/31 23:59"))
            );
    	assertEquals(DateUtils.toString("%Y",cal.getTime()) + "-12-31 00:00:00",
        		DateUtils.toString(fmt,DateUtils.dateFromString("12/31"))
            );


    	Date oneHourAgo = new Date(d.getTime()-3600*1000);
    	assertEquals(DateUtils.toString(fmt,oneHourAgo),
    			DateUtils.toString(fmt,DateUtils.dateFromString("-1hour"))
    	);
    	// these may fail for 2 minutes around midnight
    	Date midnight =  new Date(d.getTime()/3600/24/1000*3600*24*1000);
    	Date minBeforeMidnight = new Date(midnight.getTime()-60*1000);
    	Date minAfterMidnight = new Date(midnight.getTime()+60*1000);
    	assertEquals(DateUtils.toString(fmt,minBeforeMidnight),
    			DateUtils.toString(fmt,DateUtils.dateFromString("23:59"))
    	);
    	assertEquals(DateUtils.toString(fmt,minAfterMidnight),
    			DateUtils.toString(fmt,DateUtils.dateFromString("00:01"))
    	);

    }
    public void test_dateRange() {
    	System.setProperty("user.timezone", "GMT");
    	Date d1 = DateUtils.dateFromString("2001-01-01 00:00");
    	Date d2 = DateUtils.dateFromString("2001-01-01 02:00");
    	Date d3 = DateUtils.dateFromString("2001-01-03 00:00");
    	Date d4 = DateUtils.dateFromString("2001-01-01 01:00");
    	assertEquals("2001-01-01 00:00:00",toStr(DateUtils.dateRange(d1, d1)));
    	assertEquals("2001-01-01 00:00:00",toStr(DateUtils.dateRange(d1, d2)));
    	assertEquals("2001-01-01 00:00:00",toStr(DateUtils.dateRange(d4, d2)));
    	assertEquals("",toStr(DateUtils.dateRange(d2, d4)));
    	assertEquals("2001-01-01 00:00:00, 2001-01-02 00:00:00",toStr(DateUtils.dateRange(d1, d3)));
    	assertEquals("2001-01-01 00:00:00, 2001-01-01 01:00:00",toStr(DateUtils.dateRange(d1, d2, 1000*3600)));
    }
	private String toStr(List<Date> l) {
		StringBuffer b = new StringBuffer();
		boolean first = true;
		for (Date d : l) {
			if (first) first = false;
			else b.append(", ");
			b.append(DateUtils.toString(fmt,d));
		}
		return b.toString();
	}
}
