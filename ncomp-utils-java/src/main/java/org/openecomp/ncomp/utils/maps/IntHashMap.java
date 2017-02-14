
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

// Similar to HashMap<Integer,V>, but without Integer overhead,
// doesn't support remove, and other limitations
public class IntHashMap<V> {
	private int size;
	private int capacity;
	private int[] keys;
	private V[] vals;
	private V special_val;
	private final static double full_load_factor = 0.75;
	private final static int initial_capacity = 16;
	private final static int special_key = 0;
	
	public IntHashMap() {
		this(initial_capacity);
	}
	public IntHashMap(int initial_capacity) {
		capacity = initial_capacity;
		size = 0;
		keys = null;
		vals = null;
		special_val = null;
	}
	
	private long getHash (int key) {
		return IntegerHasher.hashT(key);
	}
	
	private int getBin (long hash) {
		return (int) (hash & (keys.length-1));
	}
	
	private int findBin (int key) {
		int bin = getBin(getHash(key));
		
		while (keys[bin] != special_key && keys[bin] != key) {
			bin = (bin + 1) & (keys.length-1);
		}
		return bin;
	}
	
	private int findEmptyBin (int key) {
		int bin = getBin(getHash(key));
//		int bin = (int) (IntegerHasher.hashT(key) & (keys.length-1));

		while (keys[bin] != special_key) {
			bin = (bin + 1) & (keys.length-1);
		}
		return bin;
	}

	@SuppressWarnings("unchecked")
	public void grow () {
		if (size != 0) {
			capacity = size * 2;
		}
		int[] oldkeys = keys;
		V[] oldvals = vals;
		int newsz = (keys == null) ? 32 : keys.length*2;
		while (newsz * full_load_factor < capacity) newsz *= 2;
		capacity = (int) (newsz * full_load_factor);
		keys = new int[newsz];
		vals = (V[]) new Object[newsz];
		if (oldkeys != null) {
			for (int i=0; i<oldkeys.length; i++) {
				if (oldkeys[i] != special_key) {
					int b = findEmptyBin (oldkeys[i]);
					keys[b] = oldkeys[i];
					vals[b] = oldvals[i];
				}
			}
		}
	}
		
	public V put(int key, V val) {
		if (key == special_key) {
			V oldval = special_val;
			if (oldval == null) size++;
			special_val = val;
			return oldval;
		}
		
		if (keys == null) {
			grow();
		}
		int bin = findBin(key);
		if (keys[bin] == special_key) {
			size++;
			keys[bin] = key;
			vals[bin] = val;
			if (size >= capacity) {
				grow();
			}
			return null;
		}
		V oldval = vals[bin];
		vals[bin] = val;
		return oldval;
	}
	
	public V get (int key) {
		if (key == special_key) return special_val;
		if (keys == null) return null;
		int bin = findBin(key);
		if (keys[bin] == special_key) return null;
		return vals[bin];
	}
	
	public int size () {
		return size;
	}
	
	public int[] getKeys () {
		int[] ret = new int[size];
		int cnt = 0;
		for (int i=0; i<keys.length; i++) {
			if (keys[i] == special_key) continue;
			if (cnt >= ret.length){
				throw new RuntimeException("Bad count problem, cnt " + cnt + " size " + size);
			}
			ret[cnt++] = keys[i];
		}
		return ret;
	}
}
