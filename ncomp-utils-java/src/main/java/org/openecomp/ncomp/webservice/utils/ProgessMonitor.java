
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

// TODO add error reporting

import java.text.DecimalFormat;
import java.util.Date;

public class ProgessMonitor {
	private String name;
	private int c = 0;
	private int cThisInterval = 0;
	private Date firstTime = new Date();
	private Date lastReportTime = new Date();
	int interval = 10000;

	public ProgessMonitor(String name1) {
		name = name1;
	}

	public synchronized void newRecord() {
		c++;
		cThisInterval++;
		Date d1 = new Date();
		if (d1.getTime() - lastReportTime.getTime() > interval) {
			report();
		}
	}

	public void done() {
		report();
	}
	
	private String milliseconds2string (long s) {
		DecimalFormat twoPlaces = new DecimalFormat("00");
		long seconds = (s/1000) % 60;
		long min = (s/1000/60) % 60;
		long hour = (s/1000/60/60);
		return hour + ":" + twoPlaces.format(min) + ":" + twoPlaces.format(seconds);
	}

	void report() {
		Date d1 = new Date();
		double total = c * 1000.0 / (d1.getTime() - firstTime.getTime());
		double total2 = cThisInterval * 1000.0
				/ (d1.getTime() - lastReportTime.getTime());
		String unit = "sec";
		if (total<10) {
			unit = "min";
			total *= 60;
			total2 *= 60;
		}
		if (total<10) {
			unit = "hour";
			total *= 60;
			total2 *= 60;
		}
		System.out.println("Progress: "
				+ name
				+ " total records/" + unit + " " + (int) total
				+ " last interval " + (int) total2
				+ " running time " + milliseconds2string(d1.getTime() - firstTime.getTime())
				+ " total " + c
				+ String.format(" vsize=%.2fGB ", Runtime.getRuntime().totalMemory() / 1024.0 / 1024 / 1024));
		lastReportTime = d1;
		cThisInterval = 0;
	}
}
