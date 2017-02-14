
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
	
package org.openecomp.ncomp.utils.journaling;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

public class JournalingHashMap<V> extends JournalingObject implements Map<String, V>, Serializable {
	@Override
	public String toString() {
		return "JournalingHashMap [map=" + map + "]";
	}

	private static final long serialVersionUID = 1L;
	private HashMap<String, V> map;
	static final int REMOVE_METHOD = 2000;
	static final int PUT_METHOD = 2001;
	static final int CLEAR_METHOD = 2002;
	static final int NEWKEY_METHOD = 2003;

	@SuppressWarnings("rawtypes")
	public static JournalingHashMap create(File dir) {
		return (JournalingHashMap) create2(dir, new JournalingHashMap());
	}

	public JournalingHashMap(String context, JournalingObject parent) {
		super(context, parent);
	}

	public JournalingHashMap() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() {
		if (map == null)
			map = new HashMap<String, V>();
		else {
			for (String k : map.keySet()) {
				V o = map.get(k);
				if (o instanceof JournalingObject) {
					JournalingObject oo = (JournalingObject) o;
					initChild(k, oo);
				}
			}
		}

	}
	@SuppressWarnings("unchecked")
	void play(JournalingEvent e, int index) {
		if (index == 0) {
			switch (e.method) {
			case REMOVE_METHOD: {
				remove(e.pname);
				return;
			}
			case PUT_METHOD: {
				put(e.pname, (V) e.value);
				return;
			}
			case CLEAR_METHOD: {
				clear();
				return;
			}
			case NEWKEY_METHOD: {
				try {
					newKey(e.pname, (Class<V>) Class.forName((String) e.value));
				} catch (ClassNotFoundException e1) {
					throw new RuntimeException("Unexpected class: " + e.method);
				}
				return;
			}
			default:
				logger.debug("Unexpected method: " + e.method);
				return;
			}
		}
		String context = e.context.get(index - 1);
		V c = map.get(context);
		if (c == null) {
			throw new RuntimeException("Unknown key: " + context + " not in " + map.keySet());
		}
		JournalingObject cc = (JournalingObject) c;
		cc.play(e, index - 1);
	}

	public V newKey(String key, Class<V> clazz) {
		return newKey2(key, clazz);
	}

	public V newList(String key) {
		return newKey2(key, JournalingList.class);
	}

	public V newMap(String key) {
		return newKey2(key, JournalingHashMap.class);
	}

	private V newKey2(String key, @SuppressWarnings("rawtypes") Class clazz) {
		try {
			if (clazz.getName().contains("$"))
				throw new RuntimeException("Cannot be an inner class: " + clazz.getName());
			@SuppressWarnings("unchecked")
			Constructor<V> m = clazz.getConstructor(String.class, JournalingObject.class);
			V v = m.newInstance(key, this);
			log(new JournalingEvent(null, NEWKEY_METHOD, key, clazz.getName()));
			map.put(key, v);
			return v;
		} catch (Exception e) {
			throw new RuntimeException("Unable to create new " + clazz.getName() + " " + e);
		}
	}

	protected String eventToString(JournalingEvent e) {
		switch (e.method) {
		case REMOVE_METHOD:
			return "map:remove " + e.context + " " + e.pname;
		case PUT_METHOD:
			return "map:put " + e.context + " " + e.pname + " " + e.value;
		case CLEAR_METHOD:
			return "map:put " + e.context;
		case NEWKEY_METHOD:
			return "map:new " + e.context + " key=" + e.pname + " " + e.value;
		default:
			return "map:def " + e.context + " key=" + e.pname + " value=" + e.value;
		}
	}

	// //////////////////// Standard Map Method //////////////////////////////
	@Override
	public void clear() {
		log(new JournalingEvent(null, CLEAR_METHOD, null, null));
		map.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsKey(value);
	}

	@Override
	public Set<java.util.Map.Entry<String, V>> entrySet() {
		return map.entrySet();
	}

	@Override
	public V get(Object key) {
		return map.get(key);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return map.keySet();
	}

	@Override
	public V put(String key, V value) {
		if (value instanceof JournalingObject) {
			throw new RuntimeException("Use newkey method instead of put for values of type JournalingObject");
		}
		log(new JournalingEvent(null, PUT_METHOD, key, value));
		return map.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends V> m) {
		for (String k : m.keySet()) {
			map.put(k, m.get(k));
		}
	}

	@Override
	public V remove(Object key) {
		if (key == null || key instanceof String) {
			String k = (String) key;
			log(new JournalingEvent(null, REMOVE_METHOD, k, null));
			return map.remove(key);
		}
		return null;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		for (String k : map.keySet()) {
			Object o = map.get(k);
			if (o instanceof JournalingObject) {
				JournalingObject o1 = (JournalingObject) o;
				json.put(k, o1.toJson());
			}
			else {
				json.put(k, o.toString());
			}
		}
		return json;
	}

}
