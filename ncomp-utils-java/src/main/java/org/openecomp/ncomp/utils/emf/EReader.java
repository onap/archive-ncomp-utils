
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;

import org.openecomp.ncomp.webservice.utils.FileUtils;

public class EReader<T extends EObject> implements Iterator<T>, Iterable<T> {
	private EStringUtil<T> util = null;
	private BufferedReader reader;
	private String fileName;
	private String line;
	private T e = null;

	public EReader(String fileName2, EStringUtil<T> u, boolean isFilename) {
		util = u;
		fileName = fileName2;
		if (isFilename) 
			reader = FileUtils.filename2reader(fileName2, u.errors);
		else 
			reader = FileUtils.cmd2reader(fileName2);
	}
	public EReader(String fileName2, EStringUtil<T> u) {
		util = u;
		fileName = fileName2;
		reader = FileUtils.filename2reader(fileName2, u.errors);
	}

	public EReader(File file, EStringUtil<T> u) {
		util = u;
		fileName = file.getName();
		reader = FileUtils.filename2reader(fileName, u.errors);
	}

	/**
	 * 
	 * @return An T object for the next line (null if empty). Note the object is
	 *         not a new object. Uses EcoreUtils.copy if needed.
	 */
	private T findNext() {
		line = null;
		try {
			if (reader == null)
				return null;
			line = reader.readLine();
			if (line == null) {
				reader.close();
				reader = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (line == null)
			return null;
		try {
			return util.str2ecore(line);
		} catch (Exception e) {
			throw new RuntimeException("Read error in " + fileName + " : " + e);
		}
	}

	public String getFileName() {
		return fileName;
	}
	@Override
	public boolean hasNext() {
		if (e != null) return true;
		e = findNext();
		// TODO Auto-generated method stub
		return e != null;
	}
	@Override
	public void remove() {
		throw new RuntimeException("Can not remove from a reader");
	}
	@Override
	public T next() {
		if (e != null) {
			T ee = e;
			e = null;
			return ee;
		}
		return findNext();
	}
	@Override
	public Iterator<T> iterator() {
		return this;
	}
	public String getLine() {
		return line;
	}
}
