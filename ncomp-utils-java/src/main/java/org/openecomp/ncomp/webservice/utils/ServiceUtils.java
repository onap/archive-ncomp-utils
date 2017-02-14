
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

public class ServiceUtils {
	private static int requestNumber = 0;
	private static String requestString;
	public static EObject BackendService(EPackage pp, EObject request, String command, String dir) {	
		if (dir == null) dir = getDirectory(pp);
		String inputFile = dir + "/request";
		String outputFile = dir + "/response";
		EObject res = null;
		try {
			File dir1 = new File(dir);
			dir1.mkdirs();
			if (request != null) {
				FileUtils.ecore2file(pp, request, inputFile);
			}
			Date d1 = new Date();
			Process p = Runtime.getRuntime().exec(
					command + " " + inputFile + " " + outputFile);
			p.waitFor();
			p.destroy();
			Date d2 = new Date();
			System.err.println("Backend call: " + (d2.getTime() - d1.getTime())
					+ " milliseconds");
			res = FileUtils.file2ecore(pp,outputFile,true,false);
		} catch (Exception exception) {
			System.err.println("SERVER ERROR: " + exception + " " + dir);
			exception.printStackTrace();
		}
		return res;
	}
	public static String getDirectory(EPackage pp) {
		int n;
		String prefix = pp.getName();
		Date  now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd",new Locale("UTC"));
		String nowString = format.format(now);
		if (!nowString.equals(requestString)) {
			requestNumber = 0;
			requestString = nowString;
		}
		String dir;
		synchronized (requestString) {
			while (true) {
				n = requestNumber++;
				dir = System.getProperty("user.dir")+"/" + prefix + "/requests/" + requestString + "/" + n;
				File f = new File(dir);
				if (!f.exists()) {
					f.mkdirs();
					break;
				}
			}
		}
		return dir;
	}
}
