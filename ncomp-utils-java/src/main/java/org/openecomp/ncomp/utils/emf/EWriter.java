
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
	
package org.openecomp.ncomp.utils.emf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.emf.ecore.EObject;

import org.openecomp.ncomp.webservice.utils.FileUtils;

public class EWriter<T extends EObject> {
	private EStringUtil<T> util = null;
	private OutputStreamWriter writer;
	private String fileName;

	public EWriter(String fileName2, EStringUtil<T> u) {
		util = u;
		fileName = fileName2;
		boolean gzip = fileName.endsWith(".gz");
		writer = FileUtils.filename2writer(fileName2 + ".tmp", gzip);
	}

	public void write(T e) {
		try {
			writer.write(util.ecore2str(e) + "\n");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void close() {
		try {
			writer.close();
			File f = new File(fileName + ".tmp");
			File dest = new File(fileName);
			f.renameTo(dest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getFileName() {
		return fileName;
	}
}
