
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class Stream2Logger {
	
	private class Stream2LoggerThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					String line = in.readLine();
					logger.error(line);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}

	private Logger logger;
	private BufferedReader in;
	private Stream2LoggerThread t;

	public Stream2Logger(InputStream in, Logger logger) {
		this.logger = logger;
		this.in = new BufferedReader(new InputStreamReader(in));
		t = new Stream2LoggerThread();
		t.run();
	}
}
