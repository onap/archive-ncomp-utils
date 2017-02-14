
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
	
package org.openecomp.ncomp.utils.logging;

import java.util.Date;
import java.util.Enumeration;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.openecomp.ncomp.utils.maps.HashMapMapMap;

/**
 * Utilities for handling logging. 
 * 
 */
public class LoggingUtils {
	/**
	 * Close all log4j JMS appenders. This disconnect from the JMS in a clean way. 
	 * <p>
	 * Should only be called just before process exit.
	 */
	public static void closeJMSAppenders() {
		Enumeration<?> e = Logger.getRootLogger().getAllAppenders();
		System.out.println(e);
		while (e.hasMoreElements()) {
			Object o = e.nextElement();
			System.out.println(o);
			if (o instanceof org.apache.log4j.net.JMSAppender) {
				org.apache.log4j.net.JMSAppender a = (org.apache.log4j.net.JMSAppender) o;
				a.close();
			}
		}
	}
	static private HashMapMapMap<Logger, Level, String, LoggerStat> m = new HashMapMapMap<Logger, Level, String, LoggerStat>();
	
	public static void dampingLogger(Logger logger, Level level, String message) {
		dampingLogger(logger, level, message, message);
	}
	public static void dampingLogger(Logger logger, Level level, String category, String message) {
		LoggerStat s = m.get(logger, level, category);
		if (s == null) {
			s = new LoggerStat();
			 m.insert(logger, level, category, s);
		}
		if (s.start.getTime() + 3600*1000 < new Date().getTime() ) {
			logger.log(level, category + " in last hour " + s.num + " times");
			s.num = 0;
			s.start = new Date();
		}
		if (s.num < 5) {
			logger.log(level,message);
		}
		s.num++;
	}
}

class LoggerStat {
	Date start = new Date();
	int num = 0;
}

