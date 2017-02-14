
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class JournalingList<V> extends JournalingObject implements List<V>, Serializable {
	@Override
	public String toString() {
		return "JournalingList [list=" + list + "]";
	}

	private static final long serialVersionUID = 1L;
	private List<V> list;
	static final int ADD_METHOD = 1001;
	static final int CLEAR_METHOD = 1002;
	static final int ADDNEW_METHOD = 1003;
	static final int REMOVE_METHOD = 1004;


	@SuppressWarnings("rawtypes")
	static public JournalingList create(File dir) {
		return (JournalingList) create2(dir,new JournalingList());
	}

	public JournalingList(String context, JournalingObject parent) {
		super(context,parent);
	}
	public JournalingList() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() {
		if (list == null)
			list = new ArrayList<V>();
		else {
			int i = 0;
			for (V o : list) {
				if (o instanceof JournalingObject) {
					JournalingObject oo = (JournalingObject) o;
					String key = Integer.toString(i++);
					initChild(key, oo);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	void play(JournalingEvent e,int index) {
		if (index == 0) {
			switch (e.method) {
			case ADD_METHOD: {
				add((V) e.value);
				return;
			}
			case CLEAR_METHOD: {
				clear();
				return;
			}
			case ADDNEW_METHOD: {
				try {
					addNew((Class<V>) Class.forName((String) e.value));
				} catch (ClassNotFoundException e1) {
					throw new RuntimeException("Unexpected class: " + e.method);
				}
				return;
			}
			case REMOVE_METHOD: {
				remove((int)(Integer)e.value);
				return;
			}
			default: 
				logger.debug("Unexpected method: " + e.method); 
				return;
			}
		}
		String context = e.context.get(index-1);
		int i = Integer.parseInt(context);
		V c = list.get(i);
		if (c == null) {
			throw new RuntimeException("unknown list error");
		}
		JournalingObject cc = (JournalingObject) c;
		cc.play(e, index-1);
	}
	protected String eventToString(JournalingEvent e) {
		switch (e.method) {
		case REMOVE_METHOD:
			return "list:remove " + e.context + " " + e.value;
		case ADD_METHOD:
			return "list:add " + e.context + " " + e.value;
		case CLEAR_METHOD:
			return "list:clear " + e.context;
		case ADDNEW_METHOD:
			return "list:new " + e.context + " key=" +  e.value;
		default:
			return "list:def " + e.context + " key=" + e.pname + " value=" + e.value;
		}
	}
	
	public V addNew(Class<V> clazz) {
		return addNew2(clazz);
	}
	public V newList(String key) {
		return addNew2(JournalingList.class);
	}
	public V newMap() {
		return addNew2(JournalingHashMap.class);
	}
	private V addNew2(@SuppressWarnings("rawtypes") Class clazz) {
		try {
			if (clazz.getName().contains("$")) 
				throw new RuntimeException("Cannot be an inner class: " + clazz.getName());
			@SuppressWarnings("unchecked")
			Constructor<V> m = clazz.getConstructor(String.class,JournalingObject.class);
			String key = Integer.toString(list.size());
			V v = m.newInstance(key, this);
			log(new JournalingEvent(null, ADDNEW_METHOD, null, clazz.getName()));
			list.add(v);
			return v;
		} catch (Exception e) {
			throw new RuntimeException("Unable to create new " + clazz.getName() + " " + e);
		}
	}

	////////////////////// Standard List Method //////////////////////////////
	
	@Override
	public boolean add(V v) {
		if (v instanceof JournalingObject) {		
			throw new RuntimeException("Use newkey method instead of put for values of type JournalingObject");
		}
		log(new JournalingEvent(null, ADD_METHOD, null, v));
		return list.add(v);
	}

	@Override
	public void add(int arg0, V arg1) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public boolean addAll(Collection<? extends V> l) {
		for (V v: l) {
			if (!add(v)) return false;
		}
		return true;
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends V> arg1) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public void clear() {
		log(new JournalingEvent(null, CLEAR_METHOD, null, null));
		list.clear();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> l) {
		// TODO Auto-generated method stub
		return list.containsAll(l);
	}

	@Override
	public V get(int index) {
		return list.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<V> iterator() {
		return list.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<V> listIterator() {
		return listIterator();
	}

	@Override
	public ListIterator<V> listIterator(int i) {
		return listIterator(i);
	}

	@Override
	public boolean remove(Object arg0) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public V remove(int i) {
		log(new JournalingEvent(null, REMOVE_METHOD, null, (Integer) i));
		return list.remove(i);
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public V set(int arg0, V arg1) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public List<V> subList(int arg0, int arg1) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		JSONArray a = new JSONArray();
		json.put("array", a);
		for (V o : list) {
			if (o instanceof JournalingObject) {
				JournalingObject o1 = (JournalingObject) o;
				a.put(o1.toJson());
			}
			else {
				a.put(o.toString());
			}
		}
		return json;
	}


}
