
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;



public class PropertyUtil { 
	public static final Logger logger = Logger.getLogger(PropertyUtil.class);

	static public Properties getPropertiesFromClasspath(String propFileName) throws IOException {
		Properties props = new Properties();
		if (propFileName == null) return props;

		InputStream inputStream = null;
		
		File f = new File(propFileName);
		if (f.isAbsolute()) {
			logger.info("Loading absolute " + f);
			inputStream = new FileInputStream(f);
		} else {
			// loading xmlProfileGen.properties from the classpath
			URL url = PropertyUtil.class.getClassLoader().getResource(propFileName);
			if (url == null) {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath (null url)");
			}
			logger.info("Loading " + url);
			inputStream = PropertyUtil.class.getClassLoader().getResourceAsStream(propFileName);
			if (inputStream == null) {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
		}
		try {
			props.load(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (inputStream != null)
				inputStream.close();
		}

		for (Object k : System.getProperties().keySet()) {
			Object v = props.get(k);
			if (v != null) {
				logger.info("Overwriting property from system property: " + replaceForLogForcingProtection(k) + " = " + replaceForLogForcingProtection(v));
			}
			props.put(k, System.getProperties().get(k));
		}
		String hostname = InetAddress.getLocalHost().getHostName();
		props.put("user.hostname", hostname);
		return props;
	}

	public static String replaceForLogForcingProtection(Object v) {
		return v.toString().replace("\n", "NEWLINE");
	}

}
