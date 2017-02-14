
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.openecomp.ncomp.utils.PropertyUtil;
import org.openecomp.ncomp.utils.StringUtil;

public class JsonUtils {
	public static final Logger logger = Logger.getLogger(JsonUtils.class);
	HashMap<String, List<String>> features = new HashMap<String, List<String>>();
	public static JsonUtils util = new JsonUtils();
	boolean useProxy = true;

	public JsonUtils() {
	}

	public boolean isUseProxy() {
		return useProxy;
	}

	public void setUseProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}

	public void addFeatures(String pfeature, String features1) {
		List<String> l = new Vector<String>();
		for (String s : features1.split(":", -1)) {
			l.add(s);
		}
		features.put(pfeature, l);
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
		res.put("_cfeature", cfeature);
		List<String> show = features.get(cfeature);
		for (EAttribute attr : ecore.eClass().getEAllAttributes()) {
			if (show != null && show.size() > 0 && !show.contains(attr.getName()))
				continue;
			if (attr.getUpperBound() == -1 || attr.getUpperBound() > 1) {
				JSONArray a = new JSONArray();
				List<Object> l = (List<Object>) ecore.eGet(attr);
				for (Object o : l) {
					if (attr.getEType().getInstanceTypeName().equals("java.lang.String")) {
						a.put(o);
					} else if (attr.getEType().getInstanceTypeName().equals("java.util.Date")) {
						a.put(o);
					} else if (attr.getEType().getInstanceTypeName().equals("int")) {
						a.put(o);
					} else if (attr.getEType().getInstanceTypeName().equals("long")) {
						a.put(o);
					} else if (attr.getEType().getInstanceTypeName().equals("boolean")) {
						a.put(o);
					} else if (attr.getEType().getInstanceTypeName().equals("double")) {
						a.put(o);
					} else {
						a.put(o.toString());
					}
				}
				res.put(attr.getName(), a);
			} else {
				Object o = ecore.eGet(attr);
				if (attr.getEType().getInstanceTypeName().equals("java.lang.String")) {
					res.put(attr.getName(), o);
				} else if (attr.getEType().getInstanceTypeName().equals("java.util.Date")) {
					res.put(attr.getName(), o);
				} else if (attr.getEType().getInstanceTypeName().equals("int")) {
					res.put(attr.getName(), ecore.eGet(attr));
				} else if (attr.getEType().getInstanceTypeName().equals("long")) {
					res.put(attr.getName(), ecore.eGet(attr));
				} else if (attr.getEType().getInstanceTypeName().equals("boolean")) {
					res.put(attr.getName(), ecore.eGet(attr));
				} else if (attr.getEType().getInstanceTypeName().equals("double")) {
					res.put(attr.getName(), o);
				} else if (attr.getEType() instanceof org.eclipse.emf.ecore.EEnum) {
					res.put(attr.getName(), o);
				} else {
					res.put(attr.getName(), "TODO: " + attr.getEType().getInstanceTypeName());
				}
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

	public JSONObject ecore2json(EObject ecore) {
		try {
			return ecore2json(ecore, new HashMap<EObject, String>(), "");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String ecore2jsonString(EObject ecore, int indentFactor) {
		try {
			if (ecore == null)
				return null;
			return ecore2json(ecore, new HashMap<EObject, String>(), "").toString(indentFactor);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public JSONArray ecores2json(EList<EObject> l) {
		JSONArray res = new JSONArray();
		for (EObject e : l) {
			res.put(ecore2json(e));
		}
		return res;
	}

	public static JSONObject merge(JSONObject md1, JSONObject md2) {
		JSONObject o = new JSONObject();
		if (md1 == null)
			return md2;
		if (md2 == null)
			return md1;
		Iterator<String> i = md1.keys();
		while (i.hasNext()) {
			String k = i.next();
			o.put(k, md1.get(k));
		}
		i = md2.keys();
		while (i.hasNext()) {
			String k = i.next();
			o.put(k, md2.get(k));
		}
		return o;
	}

	public static String getStringValue(JSONObject json, String name) {
		String[] a = name.split("\\.");
		return getStringValue(json, a, 0);
	}

	private static String getStringValue(JSONObject json, String[] a, int i) {
		if (i >= a.length)
			return null;
		Object o;
		try {
			o = json.get(a[i]);
		} catch (JSONException e) {
			return null;
		}
		if (i == a.length - 1) {
			if (o instanceof String) {
				String s = (String) o;
				return s;
			}
			if (o instanceof Boolean || o instanceof Double || o instanceof Integer || o instanceof Long) {
				return o.toString();
			}
			return null;
		}
		if (o instanceof JSONObject) {
			JSONObject json1 = (JSONObject) o;
			return getStringValue(json1, a, i + 1);
		}
		return null;
	}

	public static Object getValue(JSONObject json, String name) {
		String[] a = name.split("\\.");
		return getValue(json, a, 0);
	}

	private static Object getValue(JSONObject json, String[] a, int i) {
		if (i >= a.length)
			return null;
		Object o;
		try {
			o = json.get(a[i]);
		} catch (JSONException e) {
			return null;
		}
		if (i == a.length - 1) {
			return o;
		}
		if (o instanceof JSONObject) {
			JSONObject json1 = (JSONObject) o;
			return getValue(json1, a, i + 1);
		}
		if (o instanceof JSONArray) {
			JSONArray json1 = (JSONArray) o;
			return getValue(json1, a, i + 1);
		}

		return null;
	}
	
	private static Object getValue(JSONArray json, String[] a, int i) {
		if (i >= a.length)
			return null;
		Object o;
		try {
			o = json.get(Integer.parseInt(a[i]));
		} catch (JSONException e) {
			return null;
		}
		if (i == a.length - 1) {
			return o;
		}
		if (o instanceof JSONObject) {
			JSONObject json1 = (JSONObject) o;
			return getValue(json1, a, i + 1);
		}
		return null;
	}


	public static JSONObject file2json(String file) throws IOException {
		InputStream in = FileUtils.filename2stream(file, null);
		if (in == null)
			throw new RuntimeException("Unable to open: " + file);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		try {
			FileUtils.copyStream(in, buf);
		} finally {
			in.close();
			buf.close();
		}
		return new JSONObject(buf.toString());
	}

	public static JSONObject file2json(String file, Properties props, String prefix) throws IOException {
		InputStream in = FileUtils.filename2stream(file, null);
		if (in == null)
			throw new RuntimeException("Unable to open: " + file);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		try {
			FileUtils.copyStream(in, buf);
		} finally {
			in.close();
			buf.close();
		}
		String s = buf.toString().replaceAll("##.*", "");
		try {
			s = StringUtil.expandUsingProperties(s, props, prefix);
			return new JSONObject(s);
		} catch (JSONException e) {
			logger.debug("bad JSON String" + s + " " + e);
			throw e;
		}
	}

	public static HashMap<String, String> getStringValueMap(JSONObject v, String string) {
		Object x = getValue(v, string);
		if (!(x instanceof JSONObject)) {
			throw new RuntimeException("Expected Json object value for key: " + string);
		}
		v = (JSONObject) x;
		HashMap<String, String> res = new HashMap<String, String>();
		for (Iterator<String> i = v.keys(); i.hasNext();) {
			String key = (String) i.next();
			Object o = v.get(key);
			if (o instanceof String) {
				String s = (String) o;
				res.put(key, s);
			} else
				throw new RuntimeException("Expected string value for key: " + key);
		}
		return res;
	}

	public static JSONObject stream2json(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileUtils.copyStream(in, out);
		if (out.toString().length() == 0) return null;
		String s = out.toString().replaceAll("##.*", "");
		try {
			return new JSONObject(s);
		} catch (JSONException e) {
			logger.debug("bad JSON String" + s + " " + e);
			throw e;
		}
	}

	public static String toStringLazy(Object oo, int indentFactor, int indent) throws JSONException {
		int j;
		if (oo instanceof JSONArray) {
			JSONArray a = (JSONArray) oo;
			return toStringLazy(a, indentFactor, indent);
		}
		if (! (oo instanceof JSONObject)) {
			return JSONObject.valueToString(oo, indentFactor, indent);
		}
		JSONObject json = (JSONObject)oo;
		int n = json.length();
		if (n == 0) {
			return "{}";
		}
		Iterator<String> keys = json.keys();
		StringBuffer sb = new StringBuffer("{");
		int newindent = indent + indentFactor;
		Object o;
		if (n == 1) {
			o = keys.next();
			sb.append(o.toString());
			sb.append(": ");
			sb.append(toStringLazy(json.get((String) o), indentFactor, indent));
		} else {
			while (keys.hasNext()) {
				o = keys.next();
				if (sb.length() > 1) {
					sb.append(",\n");
				} else {
					sb.append("\n");
				}
				for (j = 0; j < newindent; j += 1) {
					sb.append(' ');
				}
				sb.append(o.toString());
				sb.append(": ");
				sb.append(toStringLazy(json.get((String) o), indentFactor, newindent));
			}
			if (sb.length() > 1) {
				sb.append('\n');
				for (j = 0; j < indent; j += 1) {
					sb.append(' ');
				}
			}
		}
		sb.append('}');
		return sb.toString();
	}
    public static String toStringLazy(JSONArray a, int indentFactor, int indent) throws JSONException {
        int len = a.length();
        if (len == 0) {
            return "[]";
        }
        int i;
        StringBuffer sb = new StringBuffer("[");
        int newindent = indent + indentFactor;
        if (len == 1) {
    		Object oo = a.get(0);
			if (oo instanceof JSONArray) {
    			JSONArray aa = (JSONArray) oo;
    			sb.append(toStringLazy(aa, indentFactor, indent));
    		}
			else if (oo instanceof JSONObject) {
				JSONObject ooo = (JSONObject) oo;
				sb.append(toStringLazy(ooo, indentFactor, indent));
    		}
			else sb.append(JSONObject.valueToString(oo,
                    indentFactor, indent));
        } else {
            sb.append('\n');
            for (i = 0; i < len; i += 1) {
                if (i > 0) {
                    sb.append(",\n");
                }
                for (int j = 0; j < newindent; j += 1) {
                    sb.append(' ');
                }
                sb.append(toStringLazy(a.get(i),
                        indentFactor, newindent));
            }
            sb.append('\n');
            for (i = 0; i < indent; i += 1) {
                sb.append(' ');
            }
        }
        sb.append(']');
        return sb.toString();
    }

	public static JSONObject getJsonFromClasspath(String filename) {
		JSONObject res = new JSONObject();
		if (filename == null) return res;
		URL url = PropertyUtil.class.getClassLoader().getResource(filename);
		InputStream inputStream = null;
		try {
			logger.info("Loading JSON" + url);
			inputStream = PropertyUtil.class.getClassLoader().getResourceAsStream(filename);
			if (inputStream == null) {
				throw new FileNotFoundException("JSON file '" + filename + "' not found in the classpath");
			}
			res = stream2json(inputStream);
		} catch (Exception e) {
			logger.warn("Bad JSON in file: " + url + " " + e);
			e.printStackTrace();
		} finally {
			if (inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("unable to close stream",e);
				}
		}
		return res;
	}
}
