
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
	
package org.openecomp.ncomp.utils.maps;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@SuppressWarnings("unchecked")
public class LittleMap<K,V> implements Map<K,V> {
	// A Map designed to perform well (space & time) for 1 or 2 items
	// Doesn't support remove within entrySet or keySet

	private K key1 = null;
	private V val1 = null;
	// if key1 == null, then key2 is type HashTable, otherwise type K
	private Object key2 = null;
	private V val2 = null;

	private class LittleEntryIter implements Iterator<Map.Entry<K,V>> {
		private int i;
		
		public LittleEntryIter() {
			i = 0;
		}
		
		@Override
		public boolean hasNext() {
			if (i==0) return (key1 != null);
			if (i==1) return (key2 != null);
			return false;
		}
		
		@Override
		public Map.Entry<K, V> next() {
			i++;
			if (i == 1) return new AbstractMap.SimpleEntry<K,V>(key1, val1);
			else if (i == 2) return new AbstractMap.SimpleEntry<K,V>((K) key2, val2);
			else throw new NoSuchElementException();
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}
	
 	private class LittleEntrySet implements Set<Map.Entry<K,V>> {
		@Override
		public boolean add(Map.Entry<K, V> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends Map.Entry<K, V>> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			key1 = null;
			val1 = null;
			key2 = null;
			val2 = null;
		}

		@Override
		public boolean contains(Object arg0) {
			for (Map.Entry<K, V> e : this) {
				if (e.equals(arg0)) return true;
			}
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> arg0) {
			for (Object o : arg0) {
				if (!contains(o)) return false;
			}
			return true;
		}

		@Override
		public boolean isEmpty() {
			return (key1 == null);
		}

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new LittleEntryIter();
		}

		@Override
		public boolean remove(Object arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			if (key1 == null) return 0;
			else if (key2 == null) return 1;
			else return 2;
		}

		@Override
		public Object[] toArray() {
			if (key1 == null) {
				return new Object[0];
			} else if (key2 == null) {
				Object[] arr = new Object[1];
				arr[0] = new AbstractMap.SimpleEntry<K,V>(key1, val1);
				return arr;
			} else {
				Object[] arr = new Object[2];
				arr[0] = new AbstractMap.SimpleEntry<K,V>(key1, val1);
				arr[1] = new AbstractMap.SimpleEntry<K,V>((K) key2, val2);
				return arr;
			}
		}

		@Override
		public <T> T[] toArray(T[] arg0) {
			if (key1 == null) {
				return (T[]) new Object[0];
			} else if (key2 == null) {
				T[] arr = (T[]) new Object[1];
				arr[0] = (T) new AbstractMap.SimpleEntry<K,V>(key1, val1);
				return arr;
			} else {
				T[] arr = (T[]) new Object[2];
				arr[0] = (T) new AbstractMap.SimpleEntry<K,V>(key1, val1);
				arr[1] = (T) new AbstractMap.SimpleEntry<K,V>((K) key2, val2);
				return arr;
			}
		}
	}

	private class LittleKeyIter implements Iterator<K> {
		private int i;
		
		public LittleKeyIter() {
			i = 0;
		}
		
		@Override
		public boolean hasNext() {
			if (i==0) return (key1 != null);
			if (i==1) return (key2 != null);
			return false;
		}
		
		@Override
		public K next() {
			i++;
			if (i == 1) return key1;
			else if (i == 2) return (K) key2;
			else throw new NoSuchElementException();
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}
	
 	private class LittleKeySet implements Set<K> {
		@Override
		public boolean add(K arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends K> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			key1 = null;
			val1 = null;
			key2 = null;
			val2 = null;
		}

		@Override
		public boolean contains(Object arg0) {
			for (K key : this) {
				if (key.equals(arg0)) return true;
			}
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> arg0) {
			for (Object o : arg0) {
				if (!contains(o)) return false;
			}
			return true;
		}

		@Override
		public boolean isEmpty() {
			return (key1 == null);
		}

		@Override
		public Iterator<K> iterator() {
			return new LittleKeyIter();
		}

		@Override
		public boolean remove(Object arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			if (key1 == null) return 0;
			else if (key2 == null) return 1;
			else return 2;
		}

		@Override
		public Object[] toArray() {
			if (key1 == null) {
				return new Object[0];
			} else if (key2 == null) {
				Object[] arr = new Object[1];
				arr[0] = key1;
				return arr;
			} else {
				Object[] arr = new Object[2];
				arr[0] = key1;
				arr[1] = key2;
				return arr;
			}
		}

		@Override
		public <T> T[] toArray(T[] arg0) {
			if (key1 == null) {
				return (T[]) new Object[0];
			} else if (key2 == null) {
				T[] arr = (T[]) new Object[1];
				arr[0] = (T) key1;
				return arr;
			} else {
				T[] arr = (T[]) new Object[2];
				arr[0] = (T) key1;
				arr[1] = (T) key2;
				return arr;
			}
		}
	}

	private class LittleValIter implements Iterator<V> {
		private int i;
		
		public LittleValIter() {
			i = 0;
		}
		
		@Override
		public boolean hasNext() {
			if (i==0) return (key1 != null);
			if (i==1) return (key2 != null);
			return false;
		}
		
		@Override
		public V next() {
			i++;
			if (i == 1) return val1;
			else if (i == 2) return val2;
			else throw new NoSuchElementException();
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}
	
 	private class LittleValSet implements Collection<V> {
		@Override
		public boolean add(V arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends V> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			key1 = null;
			val1 = null;
			key2 = null;
			val2 = null;
		}

		@Override
		public boolean contains(Object arg0) {
			for (V val : this) {
				if (val.equals(arg0)) return true;
			}
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> arg0) {
			for (Object o : arg0) {
				if (!contains(o)) return false;
			}
			return true;
		}

		@Override
		public boolean isEmpty() {
			return (key1 == null);
		}

		@Override
		public Iterator<V> iterator() {
			return new LittleValIter();
		}

		@Override
		public boolean remove(Object arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			if (key1 == null) return 0;
			else if (key2 == null) return 1;
			else return 2;
		}

		@Override
		public Object[] toArray() {
			if (key1 == null) {
				return new Object[0];
			} else if (key2 == null) {
				Object[] arr = new Object[1];
				arr[0] = val1;
				return arr;
			} else {
				Object[] arr = new Object[2];
				arr[0] = val1;
				arr[1] = val2;
				return arr;
			}
		}

		@Override
		public <T> T[] toArray(T[] arg0) {
			if (key1 == null) {
				return (T[]) new Object[0];
			} else if (key2 == null) {
				T[] arr = (T[]) new Object[1];
				arr[0] = (T) val1;
				return arr;
			} else {
				T[] arr = (T[]) new Object[2];
				arr[0] = (T) val1;
				arr[1] = (T) val2;
				return arr;
			}
		}
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		if (key1 == null && key2 != null) {
			return ((HashMap<K,V>) key2).entrySet();
		} else {
			return new LittleEntrySet();
		}
	}
		
	private HashMap<K,V> toHashMap () {
		HashMap<K,V> map = new HashMap<K,V>();
		if (key1 != null) map.put(key1,val1);
		if (key2 != null) map.put((K) key2, val2);
		key1 = null;
		val1 = null;
		key2 = map;
		val2 = null;
		return map;
	}
	
	@Override
	public V put(K key, V value) {
		if (key1 == null) {
			if (key2 != null) {
				return ((HashMap<K,V>) key2).put(key, value);
			} else {
				key1 = key;
				val1 = value;
				return null;
			}
		} else {
			if (key1.equals(key)) {
				V oldv = val1;
				val1 = value;
				return oldv;
			}
			if (key2 != null) {
				if (key2.equals(key)) {
					V oldv = val2;
					val2 = value;
					return oldv;
				} else {
					toHashMap().put(key, value);
					return null;
				}
			} else {
				key2 = key;
				val2 = value;
				return null;
			}
		}
	}

	@Override
	public void clear() {
		key1 = null;
		val1 = null;
		key2 = null;
		val2 = null;
	}

	@Override
	public boolean containsKey(Object key) {
		if (key1 == null) {
			if (key2 != null) {
				return ((HashMap<K,V>) key2).containsKey(key);
			} else {
				return false;
			}
		} else {
			if (key1.equals(key)) {
				return true;
			}
			if (key2 != null && key2.equals(key)) {
				return true;
			}
			return false;
		}
	}

	@Override
	public boolean containsValue(Object value) {
		if (key1 == null) {
			if (key2 != null) {
				return ((HashMap<K,V>) key2).containsValue(value);
			} else {
				return false;
			}
		} else {
			if (val1.equals(value)) {
				return true;
			}
			if (key2 != null && val2.equals(value)) {
				return true;
			}
			return false;
		}
	}

	@Override
	public V get(Object key) {
		if (key1 == null) {
			if (key2 != null) {
				return ((HashMap<K,V>) key2).get(key);
			} else {
				return null;
			}
		} else {
			if (key1.equals(key)) {
				return val1;
			}
			if (key2 != null && key2.equals(key)) {
				return val2;
			}
			return null;
		}
	}

	@Override
	public boolean isEmpty() {
		if (key1 == null && key2 != null) {
			return ((HashMap<K,V>) key2).isEmpty();
		}
		return (key1 == null);
	}

	@Override
	public Set<K> keySet() {
		if (key1 == null && key2 != null) {
			return ((HashMap<K,V>) key2).keySet();
		} else {
			return new LittleKeySet();
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if (key1 == null && key2 != null) {
			((HashMap<K,V>) key2).putAll(m);
		} else {
			if (m.size() + size() > 2) {
				toHashMap().putAll(m);
			} else {
				for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
					put(e.getKey(), e.getValue());
				}
			}
		}
	}

	@Override
	public V remove(Object key) {
		return toHashMap().remove(key);
	}

	@Override
	public int size() {
		if (key1 == null && key2 != null) {
			return ((HashMap<K,V>) key2).size();
		}
		if (key1 == null) return 0;
		if (key2 == null) return 1;
		return 2;
	}

	@Override
	public Collection<V> values() {
		if (key1 == null && key2 != null) {
			return ((HashMap<K,V>) key2).values();
		}
		return new LittleValSet();
	}
}
