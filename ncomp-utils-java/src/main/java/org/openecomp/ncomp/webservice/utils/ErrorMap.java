
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import org.openecomp.ncomp.utils.SortUtil;

public class ErrorMap {
	class Counter {
		int c = 0;
	}

	private HashMap<String, HashMap<String, Counter>> map = new HashMap<String, HashMap<String, Counter>>();

	public void add(String catagory, String s) {
		HashMap<String,Counter> cat = map.get(catagory);
		int bound = 10;
		if (cat == null) {
			cat = new HashMap<String,Counter>();
			map.put(catagory,cat);
			System.out.println("New error catagory: " + catagory);
		}
		Counter c = cat.get(s);
		if (c == null) {
			c = new Counter();
			cat.put(s,c);
			if (cat.size() < bound) {
				System.out.println("New error in catagory: " + catagory + " : " + s);
			}
		}
		c.c++;
	}
	public void report() {
		for (String cat : map.keySet()) {
			System.out.println(cat + " " + map.get(cat).size() + " errors");
		}
	}
	public void save(String directory) {
		File dir = new File(directory);
		System.out.println("Saving errors in: " + directory);
		dir.mkdirs();
		for (String cat : map.keySet()) {
			OutputStreamWriter writer = FileUtils.filename2writer(directory+"/"+cat.replace(" ", "_"));
			for (String s : SortUtil.sort(map.get(cat).keySet())) {
				try {
					writer.write(s +"," + map.get(cat).get(s).c + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
