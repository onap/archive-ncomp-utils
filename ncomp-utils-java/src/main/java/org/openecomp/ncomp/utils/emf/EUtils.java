
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.openecomp.ncomp.webservice.utils.FileUtils;

public class EUtils<T extends EObject> {
	public static final Logger logger = Logger.getLogger(EUtils.class);
	private ResourceSet resourceSet;
	private EPackage p;
	private boolean useProxy;
	private HashMap<String, List<String>> features = new HashMap<String, List<String>>();

	public EUtils(EPackage p1) {
		p = p1;
		if (p != null && resourceSet == null) {
			resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
					.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
			resourceSet.getPackageRegistry().put(p.getNsPrefix(), p.getEFactoryInstance());
		}
	}

	public T file2ecore(String fileName) {
		return file2ecore(fileName, true);
	}

	@SuppressWarnings("unchecked")
	public T file2ecore(String fileName, boolean unload) {
		File file = new File(fileName);
		if (!file.exists()) {
			throw new RuntimeException("File does not exists: " + fileName);
		}
		// URI uri = URI.createFileURI(file.getAbsolutePath());
		URI uri = URI.createURI("file:///" + file.getAbsolutePath().replace("\\", "/"));
		Resource resource;
		System.err.println("Loading " + uri);
		resource = resourceSet.getResource(uri, true);
		// throw new RuntimeException("Bad responds");
		EObject e = resource.getContents().get(0);
		if (unload)
			resource.unload();
		return (T) e;
	}

	public void json2ecore(JSONObject o, T e) throws JSONException {
		json2ecore2(o, e);
	}

	private void json2ecore2(JSONObject o, EObject e) throws JSONException {
		if (o == null)
			return;
		for (EReference feature : e.eClass().getEAllReferences()) {
			if (feature.getUpperBound() == -1 || feature.getUpperBound() > 1) {
				JSONArray a = o.getJSONArray(feature.getName());
				EList<EObject> l = new BasicEList<EObject>();
				for (int i = 0; i < a.length(); i++) {
					EObject e1 = p.getEFactoryInstance().create(feature.eClass());
					json2ecore2((JSONObject) a.get(i), e1);
					l.add(e1);
				}
				e.eSet(feature, l);
			} else {
				EObject e1 = p.getEFactoryInstance().create(feature.eClass());
				json2ecore2(o.getJSONObject(feature.getName()), e1);
				e.eSet(feature, e1);
			}
		}
		for (EAttribute attr : e.eClass().getEAllAttributes()) {
			EDataType t = attr.getEAttributeType();
			if (attr.getUpperBound() == -1 || attr.getUpperBound() > 1) {
				JSONArray a = o.getJSONArray(attr.getName());
				EList<Object> l = new BasicEList<Object>();
				for (int i = 0; i < a.length(); i++) {
					String s = a.getString(i);
					Object oo = t.getEPackage().getEFactoryInstance().createFromString(t, s);
					l.add(oo);
				}
				e.eSet(attr, l);

			} else {
				String s = o.getString(attr.getName());
				Object oo = t.getEPackage().getEFactoryInstance().createFromString(t, s);
				e.eSet(attr, oo);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private JSONObject ecore2json(EObject ecore, HashMap<EObject, String> map, String context) throws JSONException {
		if (ecore == null)
			return null;
		JSONObject res = new JSONObject();
		if (useProxy && map.get(ecore) != null) {
			res.put("__proxy", map.get(ecore));
			return res;
		}
		map.put(ecore, context);
		if (context.length() > 0) {
			context = context + ".";
		}
		;
		String cfeature = ecore.eContainingFeature() == null ? "none" : ecore.eContainingFeature().getName();
		// res.put("_cfeature", cfeature);
		List<String> show = features.get(cfeature);
		for (EAttribute attr : ecore.eClass().getEAllAttributes()) {
			EDataType t = attr.getEAttributeType();
			if (show != null && show.size() > 0 && !show.contains(attr.getName()))
				continue;
			if (attr.getUpperBound() == -1 || attr.getUpperBound() > 1) {
				JSONArray a = new JSONArray();
				List<Object> l = (List<Object>) ecore.eGet(attr);
				for (Object o : l) {
					a.put(t.getEPackage().getEFactoryInstance().convertToString(attr.getEAttributeType(), o));
				}
				res.put(attr.getName(), a);
			} else {
				Object o = ecore.eGet(attr);
				res.put(attr.getName(),
						t.getEPackage().getEFactoryInstance().convertToString(attr.getEAttributeType(), o));
			}
		}
		for (EReference ref : ecore.eClass().getEAllReferences()) {
			if (show != null && show.size() > 0 && !show.contains(ref.getName()))
				continue;
			if (ref.getUpperBound() == -1 || ref.getUpperBound() > 1) {
				JSONArray a = new JSONArray();
				EList<EObject> l = (EList<EObject>) ecore.eGet(ref);
				int i = 0;
				for (EObject o : l) {
					JSONObject json = ecore2json(o, map, context + ref.getName() + i);
					a.put(json);
					json.put("__index", i);
					i++;
				}
				res.put(ref.getName(), a);
			} else {
				Object ee = ecore.eGet(ref);
				res.put(ref.getName(), ecore2json((EObject) ee, map, context + ref.getName()));
			}
		}
		return res;
	}

	public JSONObject ecore2json(T ecore) {
		try {
			return ecore2json(ecore, new HashMap<EObject, String>(), "");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String ecore2jsonString(T ecore, int indentFactor) {
		return ecore2json(ecore, new HashMap<EObject, String>(), "").toString(indentFactor);
	}

	protected String[] delim = { "%", "@" };

	public void setDelim(String[] delim) {
		this.delim = delim;
	}

	String ecore2str(T e, int i) {
		EStructuralFeature f = e.eClass().getEStructuralFeature(i);
		return e.eGet(f).toString();
	}

	String ecore2str(T e, int[] i) {
		StringBuffer res = new StringBuffer();
		for (int j = 0; j < i.length; j++) {
			if (j > 0)
				res.append(delim[0]);
			res.append(ecore2str(e, i[j]));
		}
		return res.toString();
	}

	String ecore2str(T e, int[][] i) {
		throw new RuntimeException("TODO");
	}

	public String ecorelist2str(EList<T> list) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for (T e : list) {
			if (!first) {
				buf.append(":");
			}
			first = false;
			buf.append(e.toString());
		}
		return buf.toString();
	}

	/**
	 * Returns a eobject from a string. Currently only works for attributes with
	 * multi=1;
	 * 
	 * @param str
	 * @param i
	 *            a list of features
	 * @param eclass
	 *            (The eclass of T is needed) Redundant. How do I fix this?
	 */
	@SuppressWarnings("unchecked")
	public T str2ecore(String str, int[] i, EClass eclass) {
		String[] fields = str.split(delim[0]);
		T e = (T) p.getEFactoryInstance().create(eclass);
		for (int j = 0; j < i.length; j++) {
			EAttribute attr = e.eClass().getEAllAttributes().get(i[j]);
			EDataType t = attr.getEAttributeType();
			if (attr.getUpperBound() == -1)
				throw new RuntimeException("Does not handle this case");
			String v = fixValue(t, fields[j]);
			e.eSet(attr, t.getEPackage().getEFactoryInstance().createFromString(attr.getEAttributeType(), v));
		}
		return e;
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
	public EList<T> file2ecores(String fileName, int[] i, EClass eclass) {
		BufferedReader reader = FileUtils.filename2reader(fileName, null);
		EList<T> res = new BasicEList<T>();
		if (reader == null)
			return res;
		try {
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				res.add(str2ecore(line, i, eclass));
			}
		} catch (IOException e) {
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

	@SuppressWarnings("unchecked")
	public static String attrToString(EObject o, EAttribute attr) {
		EDataType t = attr.getEAttributeType();
		Object v = o.eGet(attr);
		String vv;
		if (attr.getUpperBound() == -1) {
			List<String> ss = new ArrayList<String>();
			for (Object vvv : (List<Object>) v) {
				ss.add(t.getEPackage().getEFactoryInstance().convertToString(t, vvv));
			}
			vv = ss.toString();
		} 
		else {
			vv = t.getEPackage().getEFactoryInstance().convertToString(t, v);
		}
		return vv;
	}

	public static String attrToString(EObject o, String aName) {
		EStructuralFeature s = o.eClass().getEStructuralFeature(aName);
		if (s instanceof EAttribute) {
			return attrToString(o, (EAttribute) s);
		}
		return null;
	}

	public static List<Object> refName2objects(EObject o, String name) {
		return refName2objects(o, name.split("\\."),0);
	}


	@SuppressWarnings("unchecked")
	private static List<Object> refName2objects(EObject o, String[] names, int i) {
		List<Object> l = new ArrayList<Object>();
		if (o == null || i >= names.length) return l;
		EStructuralFeature feature = o.eClass().getEStructuralFeature(names[i]);
		if (feature == null) return l;
		if (i == names.length-1) {
			if (feature.isMany()) {
				l.addAll((List<Object>) o.eGet(feature));
			}
			else {
				l.add(o.eGet(feature));
			}
		}
		else {
			EReference ref = (EReference) feature;
			if (ref.isMany()) {
				EList<EObject> ll = (EList<EObject>) o.eGet(ref);
				for (EObject o2 : ll) {
					l.addAll(refName2objects(o2, names, i+1));
				}
			}
			else {
				l.addAll(refName2objects((EObject) o.eGet(ref), names, i+1));
			}
		}
		return l;
	}

	public static EOperation name2operation(EClass c, String opName) {
		for (EOperation op : c.getEAllOperations()) {
			if (op.getName().equals(opName)) return op;
		}
		logger.warn("Unable to find operation: " + opName + " " + c.getName());
		return null;
	}
}
