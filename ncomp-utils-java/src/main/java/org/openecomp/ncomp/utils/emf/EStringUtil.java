
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
import java.io.IOException;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.openecomp.ncomp.webservice.utils.ErrorMap;
import org.openecomp.ncomp.webservice.utils.FileUtils;

public class EStringUtil<T extends EObject> {
	private ResourceSet resourceSet;
	private EPackage p;
	private boolean lenient;
	public boolean isLenient() {
		return lenient;
	}

	public void setLenient(boolean lenient) {
		this.lenient = lenient;
	}

	protected T sample;
	private EAttribute ignoreAttr = EcoreFactory.eINSTANCE.createEAttribute();
	public ErrorMap errors;
	EList<EAttribute> featureList;

	public void setFeatureMap(int id) {
		featureList.clear();
		featureList.add((EAttribute) sample.eClass().getEStructuralFeature(id));
	}

	public void setFeatureMap(int ids[]) {
		featureList.clear();
		for (int id : ids) {
			if (id == 0)
				featureList.add(ignoreAttr);
			else
				featureList.add((EAttribute) sample.eClass().getEStructuralFeature(id));
		}
	}

	public EStringUtil(T sample1, ErrorMap errors1) {
		sample = sample1;
		errors = errors1;
		if (sample == null)
			throw new RuntimeException("Sample can not be null");
		p = sample.eClass().getEPackage();
		if (p != null && resourceSet == null) {
			resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
					Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
			resourceSet.getPackageRegistry().put(p.getNsPrefix(), p.getEFactoryInstance());
		}
		featureList = new BasicEList<EAttribute>();
		for (EAttribute attr : sample.eClass().getEAllAttributes()) {
			featureList.add(attr);
		}
	}

	protected String[] delim = { "|", "%", "@" };
	private String[] delimRegexp = { "\\|", "%", "@" };

	public void setDelim(String[] delim) {
		this.delim = delim;
		delimRegexp = new String[delim.length];
		int i = 0;
		for (String s : delim) {
			String ss = s;
			if (s.equals("|"))
				ss = "\\|";
			delimRegexp[i++] = ss;
		}
	}

	@SuppressWarnings("unchecked")
	public String ecore2str(T e) {
		StringBuffer res = new StringBuffer();
		boolean first = true;
		for (EAttribute attr : featureList) {
			if (first) first = false;
			else res.append(delim[0]);
			if (attr == ignoreAttr) 
				res.append("");
			else 
				if (e.eGet(attr) == null)
					res.append("");
				else 
					if (attr.getUpperBound()==-1) {
						List<Object> l = (List<Object>) e.eGet(attr);
						boolean first1 = true;
						for (Object value : l) {
							if (first1) first1 = false;
							else res.append(delim[1]);
							res.append(value.toString());
						}
					}
					else
						res.append(e.eGet(attr).toString());
		}
		return res.toString();
	}

	String ecore2str(T e, int[][] i) {
		throw new RuntimeException("TODO");
	}

	public String ecorelist2str(List<T> list) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for (T e : list) {
			if (!first) {
				buf.append(":");
			}
			first = false;
			buf.append(ecore2str(e));
		}
		return buf.toString();
	}

	/**
	 * Returns a eobject from a string. Currently only works for attributes with
	 * multi=1;
	 * 
	 * @param str
	 */
	public T str2ecore(String str) {
		String[] fields = str.split(checkRegexp(delimRegexp[0]),-1);
		int j = 0;
		T e = sample;
		for (EAttribute attr : featureList) {
			if (attr == ignoreAttr) {
				j++;
				continue;
			}
			if (j >= fields.length) {
				if (lenient) {
					if (errors != null) 
						errors.add("Too Fee fields", e.eClass().getName());
					break;
				}
				throw new RuntimeException("Too Few fields j=" + j + " " + str);
			}
			EDataType t = attr.getEAttributeType();
			if (attr.getUpperBound() == -1) {
				EList<Object> l = new BasicEList<Object>();
				String s = fields[j++];
				String[] values = {};
				// empty string should an empty list instead of a one element list with and empty string
				if (s.length()>0) values = s.split(checkRegexp(delimRegexp[1]),-1);
				for (String v : values) {
					String vv = fixValue(t, v);
					l.add(t.getEPackage().getEFactoryInstance().createFromString(attr.getEAttributeType(), vv));
				}
				e.eSet(attr, l);
			} else {
				String v = fixValue(t, fields[j++]);
				e.eSet(attr, t.getEPackage().getEFactoryInstance().createFromString(attr.getEAttributeType(), v));
			}
		}
		return e;
	}

	// ensure that not arbitary regexp is evaluated: Denial of Service: Regular Expression
	private String checkRegexp(String regexp) {
		switch (regexp) {
		case "\\|":
		case ":":
		case "\t":
		case ",": return regexp;
		}
		throw new RuntimeException("Regexp not trusted: " + regexp);
	}

	private String fixValue(EDataType t, String v) {
		if (t.getName().equals("EBoolean")) {
			if (v.equals("0"))
				return "false";
			if (v.equals("1"))
				return "true";
		}
		return v;
	}

	/**
	 * Read the lines of a text file and convert them to T using str2ecore.
	 * 
	 * @param fileName
	 * @param i
	 * @param eclass
	 * @return
	 */
	public EList<T> file2ecores(String fileName) {
		BufferedReader reader = FileUtils.filename2reader(fileName, null);
		EList<T> res = new BasicEList<T>();
		if (reader == null)
			return res;
		try {
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				res.add((T) EcoreUtil.copy(str2ecore(line)));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
}
