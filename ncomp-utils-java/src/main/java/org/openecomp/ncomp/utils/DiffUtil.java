
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
	
package org.openecomp.ncomp.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

public class DiffUtil {
	/**
	 * A {@link Comparator}-like interface for {@link DiffUtil#editDist(List, List, DiffEquals)}
	 * and {@link DiffUtil#editScript(List, List, DiffEquals)}.  Contains two boolean
	 * methods, {@link #equals(Object, Object)} and {@link #canChange(Object, Object)}.
	 * 
	 * {@link #equals(Object, Object)} determines if two items are equal, and 
	 * {@link #canChange(Object, Object)} determines if for two
	 * non-equal items a and b, a can be changed into b.
	 * 
	 * @param <T> the type being compared
	 */
	public static interface DiffEquals<T> {
		/**
		 * @param a
		 * @param b
		 * @return {@code true} if a equals b, {@code false} otherwise
		 */
		boolean equals(T a, T b);
		/**
		 * @param a
		 * @param b
		 * @return {@code true} if a can be changed to b, {@code false} otherwise
		 */
		boolean canChange(T a, T b);
	}
	/**
	 * A class implementing {@link DiffEquals} based on a {@link Comparator} comp.
	 * {@code equals(a,b)} returns {@code (comp.compare(a,b)==0)}, and
	 * {@code canChange(a,b)} returns {@code true}.
	 * 
	 * @param <T> the type being compared
	 */
	public static class DiffEqualsFromCompare<T> implements DiffEquals<T> {
		private final Comparator<T> comp;
		public DiffEqualsFromCompare(Comparator<T> comp) {
			this.comp = comp;
		}
		public boolean equals (T a, T b) {
			return (comp.compare(a,b) == 0);
		}
		public boolean canChange (T a, T b) {
			return true;
		}
	}
	/**
	 * A class implementing {@link DiffEquals} based on {@link Object#equals(Object)}.
	 * {@code equals(a,b)} returns {@code (a.equals(b))}, and
	 * {@code canChange(a,b)} returns {@code true}.
	 * 
	 * @param <T> the type being compared
	 */
	public static class DiffEqualsDefault<T> implements DiffEquals<T> {
		public boolean equals (T a, T b) {
			return (a.equals(b));
		}
		public boolean canChange (T a, T b) {
			return true;
		}
	}
	/**
	 * A class implementing {@link DiffEquals} based on {@link Object#equals(Object)},
	 * but not allowing changes.
	 * {@code equals(a,b)} returns {@code (a.equals(b))}, and
	 * {@code canChange(a,b)} returns {@code false}.
	 * 
	 * @param <T> the type being compared
	 */
	public static class DiffEqualsNoChange<T> implements DiffEquals<T> {
		public boolean equals (T a, T b) {
			return (a.equals(b));
		}
		public boolean canChange (T a, T b) {
			return false;
		}
	}
	/**
	 * A class implementing {@link DiffEquals} based on two {@link Comparator}s, comp and change.
	 * {@code equals(a,b)} returns {@code (comp.compare(a,b)==0)}, and
	 * {@code canChange(a,b)} returns {@code (change.compare(a,b)==0)}.
	 * 
	 * @param <T> the type being compared
	 */
	public static class DiffEqualsCompareChange<T> implements DiffEquals<T> {
		private final Comparator<T> comp;
		private final Comparator<T> change;
		public DiffEqualsCompareChange(Comparator<T> comp, Comparator<T> change) {
			this.comp = comp;
			this.change = change;
		}
		public boolean equals (T a, T b) {
			return (comp.compare(a,b) == 0);
		}
		public boolean canChange (T a, T b) {
			return (change.compare(a, b) == 0);
		}
	}
	
	/**
	 * A {@link Comparator}-like interface for {@link DiffUtil#editScriptSorted(Collection, Collection, DiffCompare)}.
	 * Contains an int method {@link #compare(Object, Object)} and 
	 * a boolean method {@link #canChange(Object, Object)}.
	 * <p>
	 * {@link #compare(Object, Object)} gives the ordering of the items in the list,
	 * {@link #canChange(Object, Object)} indicates items with compare()!=0 can be changed.
	 * <p>
	 * These should be compatible.  That is, items that are in the same equivalence class defined by {@link #canChange(Object, Object)}
	 * should be contiguous in the order defined by {@link #compare(Object, Object)}. 
	 * 
	 * @param <T> the type being compared
	 */
	public static interface DiffCompare<T> {
		/**
		 * @param a
		 * @param b
		 * @return {@code < 0} if a is less than b, {@code > 0} if a is greater than b, and {@code 0} if a equals b
		 */
		int compare(T a, T b);
		/**
		 * @param a
		 * @param b
		 * @return {@code true} if a can be changed to b, {@code false} otherwise
		 */
		boolean canChange(T a, T b);
	}
	/**
	 * A class implementing {@link DiffCompare} based on a {@link Comparator} comp.
	 * {@code compare(a,b)} returns {@code comp.compare(a,b)}, and
	 * {@code canChange(a,b)} returns {@code true}.
	 * 
	 * @param <T> the type being compared
	 */
	public static class DiffComparator<T> implements DiffCompare<T> {
		private final Comparator<T> comp;
		public DiffComparator(Comparator<T> comp) {
			this.comp = comp;
		}
		public int compare (T a, T b) {
			return comp.compare(a,b);
		}
		public boolean canChange (T a, T b) {
			return true;
		}
	}
	/**
	 * A class implementing {@link DiffCompare} based on {@link Comparable#compareTo(Object)}.
	 * {@code compare(a,b)} returns {@code (a.compareTo(b))}, and
	 * {@code canChange(a,b)} returns {@code true}.
	 * 
	 * @param <T> the type being compared
	 */
	public static class DiffComparable<T extends Comparable<T>> implements DiffCompare<T> {
		public int compare (T a, T b) {
			return (a.compareTo(b));
		}
		public boolean canChange (T a, T b) {
			return true;
		}
	}
	/**
	 * A class implementing {@link DiffCompare} based on two {@link Comparator}s, comp and change.
	 * {@code compare(a,b)} returns {@code comp.compare(a,b)}, and
	 * {@code canChange(a,b)} returns {@code (change.compare(a,b)==0)}.
	 * 
	 * @param <T> the type being compared
	 */
	public static class DiffCompareChange<T> implements DiffCompare<T> {
		private final Comparator<T> comp;
		private final Comparator<T> change;
		public DiffCompareChange(Comparator<T> comp, Comparator<T> change) {
			this.comp = comp;
			this.change = change;
		}
		public int compare (T a, T b) {
			return comp.compare(a,b);
		}
		public boolean canChange (T a, T b) {
			return (change.compare(a, b) == 0);
		}
	}
	
	/**
	 * 
	 * An enumeration of edit types.
	 *
	 */
	public static enum EditType {
		/**
		 * Change A to B.  Will only be used if {@code canChange(a,b)}.
		 */
		ChangeAtoB (0, "Change A to B"),
		/**
		 * Only in A.  An item that is in A but not in B (that is, it was inserted into A, or deleted from B).
		 */
		OnlyInA (1, "Only in A"),
		/**
		 * Only in B.  An item that is in B but not in A (that is, it was inserted into B, or deleted from A).
		 */
		OnlyInB (2, "Only in B"),
		/**
		 * Replace A with B.  Only generated by {@link DiffUtil#mergeReplaces(Collection)},
		 * and will only be used if {@code canChange(a,b)==false}.
		 */
		ReplaceAwithB (3, "Replace A with B");
		
		private final int value;
		private final String name;
		private EditType(int value, String name) {
			this.value = value;
			this.name = name;
		}
		public String toString() {
			return name;
		}
		public int value() {
			return value;
		}
	}
	
	/**
	 * 
	 * A description of an edit between two lists
	 *
	 * @param <T> the type of elements in the lists
	 */
	public static class Edit<T> {
		/**
		 * The {@link #EditType type} of edit 
		 */
		public final EditType type;
		/**
		 * The index of the affected entry in the "a" list.  Will be -1 if {@link #type} is {@link EditType#OnlyInB}
		 */
		public final int a_index;
		/**
		 * The index of the affected entry in the "b" list.  Will be -1 if {@link #type} is {@link EditType#OnlyInA}
		 */
		public final int b_index;
		/**
		 * The value of the affected entry in the "a" list.  Will be {@code null} if {@link #type} is {@link EditType#OnlyInB}
		 */
		public final T a_value;
		/**
		 * The value of the affected entry in the "b" list.  Will be {@code null} if {@link #type} is {@link EditType#OnlyInA}
		 */
		public final T b_value;
		public Edit (EditType type, int a_index, int b_index, T a_value, T b_value) {
			this.type = type;
			this.a_index = a_index;
			this.b_index = b_index;
			this.a_value = a_value;
			this.b_value = b_value;
		}
		public boolean equals (Object o) {
			if (o instanceof Edit<?>) {
				Edit<?> a = (Edit<?>) o;
				if (!type.equals(a.type)) return false;
				if (a_index != a.a_index) return false;
				if (b_index != a.b_index) return false;
				if (a_value == null) {
					if (a.a_value != null) return false;
				} else {
					if (!a_value.equals(a.a_value)) return false;
				}
				if (b_value == null) {
					if (a.b_value != null) return false;
				} else {
					if (!b_value.equals(a.b_value)) return false;
				}
				return true;
			} else {
				return false;
			}
		}
		@Override
		public String toString() {
			switch (type) {
			case ChangeAtoB:
				return type.toString()+"/"+a_value.toString()+"@"+a_index+"/"+b_value.toString()+"@"+b_index;
			case OnlyInA:
				return type.toString()+"/"+a_value.toString()+"@"+a_index;
			case OnlyInB:
				return type.toString()+"/"+b_value.toString()+"@"+b_index;
			case ReplaceAwithB:
				return type.toString()+"/"+a_value.toString()+"@"+a_index+"/"+b_value.toString()+"@"+b_index;
			default:
				return type.toString();
			}
		}
	}
	
	private static class DiagEntry {
		public final int dist;
		public final int loc;
		public final int dir; // 0 = NW, -1 = W, 1 = N
		public final int prev_loc;
		public DiagEntry(int dist, int loc, int dir, int prev_loc) {
			this.dist = dist;
			this.loc = loc;
			this.dir = dir;
			this.prev_loc = prev_loc;
		}
	}
	private static class DistWork<T> {
		private final List<T> a;
		private final List<T> b;
		private final DiffEquals<? super T> comp;
		// entry (i,j) ==> diags(i-j) loc i
		// diags(d) loc l ==> entry (l, l-d)
		private final ArrayList<ArrayList<DiagEntry>> diags;
		public DistWork(List<T> a, List<T> b, DiffEquals<? super T> comp) {
			this.a = a;
			this.b = b;
			this.comp = comp;
			diags = new ArrayList<ArrayList<DiagEntry>>();
		}
		private int diag2entry(int k) {
			if (k>=0) return 2*k;
			else return (-2*k-1);
		}
		private ArrayList<DiagEntry> diag2list(int k) {
			int e = diag2entry(k);
			if (e >= diags.size()) return null;
			return diags.get(e);
		}
		private int last_d_idx(int k) {
			ArrayList<DiagEntry> l = diag2list(k);
			if (l == null) return -1;
			return l.size() - 1;
		}
		private DiagEntry last_d_entry(int k) {
			ArrayList<DiagEntry> l = diag2list(k);
			if (l == null) return null;
			return l.get(l.size()-1);
		}
		private DiagEntry diag_entry(int k, int idx) {
			ArrayList<DiagEntry> l = diag2list(k);
			if (l == null) return null;
			return l.get(idx);
		}
		private int last_d(int k) {
			ArrayList<DiagEntry> l = diag2list(k);
			if (l == null || l.isEmpty()) return /* (k <= 0) ? 0 : k */ -1;
			return l.get(l.size()-1).loc;
		}
		private void last_d_add (int k, int dist, int loc, int dir, int prev_loc) {
			last_d_add (k, new DiagEntry(dist, loc, dir, prev_loc));
		}
		private void last_d_add (int k, DiagEntry d) {
			int e = diag2entry(k);
			while (e >= diags.size()) diags.add(new ArrayList<DiagEntry>());
			diags.get(e).add(d);
		}
		private DiagEntry find_last_d (int k, int d) {
			int prev_loc;
			int row;
			int dir;
						
			if (k == d || (k != -d && last_d(k-1) >= last_d(k+1))) {
				prev_loc = last_d_idx(k-1);
				row = last_d(k-1)+1;
				dir = -1;
			} else {
				prev_loc = last_d_idx(k+1);
				row = last_d(k+1);
				dir = +1;
			}
			if (last_d(k) >= row && k != d && -k != d && comp.canChange(a.get(row),b.get(row-k))) {
				prev_loc = last_d_idx(k);
				row = last_d(k)+1;
				dir = 0;
			}
			int col = row - k;
			while (row < a.size() && col < b.size() && comp.equals(a.get(row),b.get(col))) {
				row++;
				col++;
			}
			return new DiagEntry(d, row, dir, prev_loc);
		}

		public int dist () {
			int i=0;
			while (i < a.size() && i < b.size() && comp.equals(a.get(i), b.get(i))) {
				i++;
			}
			last_d_add (0, 0, i, 0, -1);
			int target = a.size() - b.size();
			int upper = (i == a.size()) ? -1 : 1;
			int lower = (i == b.size()) ? 1 : -1;
			int maxDist = 0;
			ArrayList<DiagEntry> new_entries = new ArrayList<DiagEntry>();
			while (last_d(target) < a.size()) {
				maxDist++;
				new_entries.clear();
				for (int k = lower; k <= upper; k++) {
					DiagEntry e = find_last_d(k, maxDist);
					new_entries.add(e);
				}
				int new_lower = lower-1;
				int new_upper = upper+1;
				for (int k = lower; k <= upper; k++) {
					DiagEntry e = new_entries.get(k-lower);
					last_d_add (k, e);
					if (e.loc >= a.size() && k <= new_upper) new_upper = k-1;
					if (e.loc - k >= b.size() && k >= new_lower) new_lower = k+1;
				}
				lower = new_lower;
				upper = new_upper;
			}
			DiagEntry e = last_d_entry(target);
			if (e == null) return (target >= 0) ? target : -target;
			return e.dist;
		}
		public List<Edit<T>> script() {
			int d = dist();
			LinkedList<Edit<T>> l = new LinkedList<Edit<T>>();
			if (a.size() == 0) {
				Assert.assertEquals(d,  b.size());
				for (int i=0; i<b.size(); i++) {
					l.addFirst(new Edit<T>(EditType.OnlyInB, -1, i, null, b.get(i)));
				}
				return l;
			}
			if (b.size() == 0) {
				Assert.assertEquals(d, a.size());
				for (int i=0; i<a.size(); i++) {
					l.addFirst(new Edit<T>(EditType.OnlyInA, i, -1, a.get(i), null));
				}
				return l;
			}
			int k = a.size() - b.size();
			while (d > 0) {
				int idx = d - ((k >= 0) ? k : -k);
				DiagEntry e = diag_entry (k, idx);
				Assert.assertNotNull(e);
				Assert.assertEquals(e.dist, d);
				int new_k = k + e.dir;
				int new_idx = (d-1) - ((new_k >= 0) ? new_k : -new_k);
				DiagEntry new_e = diag_entry (new_k, new_idx);
				Assert.assertEquals(e.prev_loc, new_idx);
				int aloc = new_e.loc;
				int bloc = aloc - new_k;
				switch (e.dir) {
				case 0:
					l.addFirst(new Edit<T>(EditType.ChangeAtoB, aloc, bloc, a.get(aloc), b.get(bloc)));
					break;
				case -1: 
					l.addFirst(new Edit<T>(EditType.OnlyInA, aloc, -1, a.get(aloc), null));
					break;
				case 1: 
					l.addFirst(new Edit<T>(EditType.OnlyInB, -1, bloc, null, b.get(bloc)));
					break;
				default: throw new RuntimeException("Unexpected dir " + e.dir);
				}
				d--;
				k = new_k;
			}
			return l;
		}
		@SuppressWarnings("unused")
		public void dumpDiags() {
			for (int i=0; i<diags.size(); i++) {
				int d = (i%2 == 0) ? (i/2) : (-(i+1)/2);
				System.out.print("diag " + d + ":");
				for (DiagEntry e : diags.get(i)) {
					System.out.print(" " + e.dist + "," + e.loc + "," + e.dir + "," + e.prev_loc);
				}
				System.out.println();
			}
		}
		public void dumpStats(String msg) {
			int lcnt = 0;
			int setcnt = 0;
			for (ArrayList<DiagEntry> diag : diags) {
				if (!diag.isEmpty()) {
					lcnt++;
					setcnt += diag.size();
				}
			}
			System.out.print("Dist diag stats:");
			if (msg != null) System.out.print(" "+msg);
			System.out.println(" size=" + diags.size() +
					" diags=" + lcnt + " entries=" + setcnt);
//			dumpDiags();
		}
	}

	private static class DistWorkSorted<T> {
		private final Iterator<T> a_iter;
		private int a_idx;
		private final Iterator<T> b_iter;
		private int b_idx;
		private final DiffCompare<T> comp;
		private static class ValIdx<T> {
			public final T val;
			public final int idx;
			public ValIdx(T val, int idx) {
				this.val = val;
				this.idx = idx;
			}
		}
		public DistWorkSorted(Collection<T> a, Collection<T> b, DiffCompare<T> comp) {
			this.a_iter = a.iterator();
			this.b_iter = b.iterator();
			this.comp = comp;
		}
		private void flushLists (T val, LinkedList<ValIdx<T>> a_list, LinkedList<ValIdx<T>> b_list, List<Edit<T>> ret) {
			if ((!a_list.isEmpty() && !comp.canChange(val, a_list.getFirst().val)) ||
					(!b_list.isEmpty() && !comp.canChange(val, b_list.getFirst().val))) {
				flushLists(a_list, b_list, ret);
			}
		}
		private void flushLists (LinkedList<ValIdx<T>> a_list, LinkedList<ValIdx<T>> b_list, List<Edit<T>> ret) {
			while (!a_list.isEmpty() && !b_list.isEmpty()) {
				ValIdx<T> a = a_list.removeFirst();
				ValIdx<T> b = b_list.removeFirst();
				ret.add(new Edit<T>(EditType.ChangeAtoB, a.idx, b.idx, a.val, b.val));
			}
			while (!a_list.isEmpty()) {
				ValIdx<T> a = a_list.removeFirst();
				ret.add(new Edit<T>(EditType.OnlyInA, a.idx, -1, a.val, null));
			}
			while (!b_list.isEmpty()) {
				ValIdx<T> b = b_list.removeFirst();
				ret.add(new Edit<T>(EditType.OnlyInB, -1, b.idx, null, b.val));
			}
		}
		public List<Edit<T>> script () {
			List<Edit<T>> ret = new ArrayList<Edit<T>>();
			T a_val = (a_iter.hasNext() ? a_iter.next() : null);
			T b_val = (b_iter.hasNext() ? b_iter.next() : null);
			a_idx = 0;
			b_idx = 0;
			LinkedList<ValIdx<T>> a_list = new LinkedList<ValIdx<T>>();
			LinkedList<ValIdx<T>> b_list = new LinkedList<ValIdx<T>>();
			while (a_val != null || b_val != null) {
				int c = (a_val == null) ? 1 : (b_val == null) ? -1 : comp.compare(a_val,  b_val);
				if (c < 0) {
					flushLists (a_val, a_list, b_list, ret);
					a_list.addLast(new ValIdx<T>(a_val, a_idx));
					a_val = (a_iter.hasNext() ? a_iter.next() : null);
					a_idx++;
				} else if (c > 0) {
					flushLists (b_val, a_list, b_list, ret);
					b_list.addLast (new ValIdx<T>(b_val, b_idx));
					b_val = (b_iter.hasNext() ? b_iter.next() : null);
					b_idx++;
				} else {
					flushLists (a_val, a_list, b_list, ret);
					a_val = (a_iter.hasNext() ? a_iter.next() : null);
					a_idx++;
					b_val = (b_iter.hasNext() ? b_iter.next() : null);
					b_idx++;
				}	
			}
			flushLists(a_list, b_list, ret);
			return ret;
		}
	}
	
	/**
	 * @param a input list for the comparison.  The list should support efficient random access (a.get(i))
	 * @param b input list for the comparison.  The list should support efficient random access (b.get(i))
	 * @param comp an DiffEquals (equality tester) used to determine if entries from a and b are equal
	 * @return the edit distance between a and b (the number of add, delete, and change
	 *     operations needed to convert a to b).
	 * <p>
	 * Runs in time O(nD) and space O(n+D^2), where n is the length of the length of lists and
	 *    D is the edit distance
	 */
	public static <T> int editDist (List<T> a, List<T> b, DiffEquals<T> comp) {
		DistWork<T> distwork = new DistWork<T>(a,b,comp);
		return distwork.dist();
	}
	/**
	 * @param a input list for the comparison.  The list should support efficient random access (a.get(i))
	 * @param b input list for the comparison.  The list should support efficient random access (b.get(i))
	 * @return the edit distance between a and b (the number of add, delete, and change
	 *     operations needed to convert a to b).
	 * <p>
	 * Runs in time O(nD) and space O(n+D^2), where n is the length of the length of lists and
	 *    D is the edit distance
	 * <p>
	 * Uses a.equals(b) to determine if entries from a and b are equal
	 */
	public static <T> int editDist (List<T> a, List<T> b) {
		return editDist(a,b,new DiffEqualsDefault<T>());
	}
	/**
	 * @param a input list for the comparison.  The list should support efficient random access (a.get(i))
	 * @param b input list for the comparison.  The list should support efficient random access (b.get(i))
	 * @param comp a Comparator used to determine if entries from a and b are equal
	 * @return the edit distance between a and b (the number of add, delete, and change
	 *     operations needed to convert a to b).
	 * <p>
	 * Runs in time O(nD) and space O(n+D^2), where n is the length of the length of lists and
	 *    D is the edit distance
	 */
	public static <T> int editDist (List<T> a, List<T> b, Comparator<T> comp) {
		return editDist(a,b,new DiffEqualsFromCompare<T>(comp));
	}
	/**
	 * @param a input list for the comparison.  The list should support efficient random access (a.get(i))
	 * @param b input list for the comparison.  The list should support efficient random access (b.get(i))
	 * @param comp a Comparator used to determine if entries from a and b are equal
	 * @param canChange a Comparator used to determine if an entry from a can be changed to an entry from b
	 * @return the edit distance between a and b (the number of add, delete, and change
	 *     operations needed to convert a to b).
	 * <p>
	 * Runs in time O(nD) and space O(n+D^2), where n is the length of the length of lists and
	 *    D is the edit distance
	 */
	public static <T> int editDist (List<T> a, List<T> b, Comparator<T> comp, Comparator<T> canChange) {
		return editDist(a,b,new DiffEqualsCompareChange<T>(comp, canChange));
	}
	
	/**
	 * @param a input list for the comparison.  The list should support efficient random access (a.get(i))
	 * @param b input list for the comparison.  The list should support efficient random access (b.get(i))
	 * @param comp an DiffEquals (equality tester) used to determine if entries from a and b are equal
	 * @return the edit distance between a and b (the number of add, delete, and change
	 *     operations needed to convert a to b).
	 * <p>
	 * Runs in time O(nD) and space O(n+D^2), where n is the length of the length of lists and
	 *    D is the edit distance
	 * <p>
	 * This version outputs (to System.out) a line of statistics about the objects used
	 */
	protected static <T> int editDistStats (List<T> a, List<T> b, DiffEquals<T> comp) {
		Date start = new Date();
		DistWork<T> distwork = new DistWork<T>(a,b,comp);
		int d = distwork.dist();
		Date end = new Date();
		double elapsed = (end.getTime() - start.getTime())/1000.0;
		distwork.dumpStats("length " + a.size() + "/" + b.size() + " distance="+d+" elapsed="+elapsed);
		return d;
	}
	/**
	 * @param a input list for the comparison.  The list should support efficient random access (a.get(i))
	 * @param b input list for the comparison.  The list should support efficient random access (b.get(i))
	 * @return the edit distance between a and b (the number of add, delete, and change
	 *     operations needed to convert a to b).
	 * <p>
	 * Runs in time O(nD) and space O(n+D^2), where n is the length of the length of lists and
	 *    D is the edit distance
	 * <p>
	 * Uses a.equals(b) to determine if entries from a and b are equal
	 * <p>
	 * This version outputs (to System.out) a line of statistics about the objects used
	 */
	protected static <T> int editDistStats (List<T> a, List<T> b) {
		return editDistStats(a,b,new DiffEqualsDefault<T>());
	}
	/**
	 * @param a input list for the comparison.  The list should support efficient random access (a.get(i))
	 * @param b input list for the comparison.  The list should support efficient random access (b.get(i))
	 * @param comp a Comparator used to determine if entries from a and b are equal
	 * @return the edit distance between a and b (the number of add, delete, and change
	 *     operations needed to convert a to b).
	 * <p>
	 * Runs in time O(nD) and space O(n+D^2), where n is the length of the length of lists and
	 *    D is the edit distance
	 * <p>
	 * This version outputs (to System.out) a line of statistics about the objects used
	 */
	protected static <T> int editDistStats (List<T> a, List<T> b, Comparator<T> comp) {
		return editDistStats(a,b,new DiffEqualsFromCompare<T>(comp));
	}

	/**
	 * @param a input list for the comparison.  The list should support efficient random access (a.get(i))
	 * @param b input list for the comparison.  The list should support efficient random access (b.get(i))
	 * @param comp an DiffEquals (equality tester) used to determine if entries from a and b are equal
	 * @return A list of edit operations converting a to b
	 * <p>
	 * Runs in time O(nD) and space O(n+D^2), where n is the length of the length of lists and
	 *    D is the edit distance
	 */
	public static <T> List<Edit<T>> editScript (List<T> a, List<T> b, DiffEquals<T> comp) {
		DistWork<T> distwork = new DistWork<T>(a,b,comp);
		return distwork.script();
	}
	/**
	 * @param a input list for the comparison.  The list should support efficient random access (a.get(i))
	 * @param b input list for the comparison.  The list should support efficient random access (b.get(i))
	 * @return A list of edit operations converting a to b
	 * <p>
	 * Runs in time O(nD) and space O(n+D^2), where n is the length of the length of lists and
	 *    D is the edit distance
	 * <p>
	 * Uses a.equals(b) to determine if entries from a and b are equal
	 */
	public static <T> List<Edit<T>> editScript (List<T> a, List<T> b) {
		return editScript(a,b,new DiffEqualsDefault<T>());
	}
	/**
	 * @param a input list for the comparison.  The list should support efficient random access (a.get(i))
	 * @param b input list for the comparison.  The list should support efficient random access (b.get(i))
	 * @param comp a Comparator used to determine if entries from a and b are equal
	 * @return A list of edit operations converting a to b
	 * <p>
	 * Runs in time O(nD) and space O(n+D^2), where n is the length of the length of lists and
	 *    D is the edit distance
	 */
	public static <T> List<Edit<T>> editScript (List<T> a, List<T> b, Comparator<T> comp) {
		return editScript(a,b,new DiffEqualsFromCompare<T>(comp));
	}
	/**
	 * @param a input list for the comparison.  The list should support efficient random access (a.get(i))
	 * @param b input list for the comparison.  The list should support efficient random access (b.get(i))
	 * @param comp a Comparator used to determine if entries from a and b are equal
	 * @param canChange a Comparator used to determine if an entry from a can be changed to an entry from b
	 * @return A list of edit operations converting a to b
	 * <p>
	 * Runs in time O(nD) and space O(n+D^2), where n is the length of the length of lists and
	 *    D is the edit distance
	 */
	public static <T> List<Edit<T>> editScript (List<T> a, List<T> b, Comparator<T> comp, Comparator<T> canChange) {
		return editScript(a,b,new DiffEqualsCompareChange<T>(comp, canChange));
	}

	/**
	 * @param a input list for the comparison.  a cannot contain nulls
	 * @param b input list for the comparison.  b cannot contain nulls
	 * @param comp a DiffCompare used to order the entries from a and b.
	 * @return A list of edit operations converting a to b
	 * <p>
	 * Runs in time O(n) and space O(n), where n is the length of the length of lists
	 */
	public static <T> List<Edit<T>> editScriptSorted (Collection<T> a, Collection<T> b, DiffCompare<T> comp) {
		DistWorkSorted<T> distwork = new DistWorkSorted<T>(a,b,comp);
		return distwork.script();
	}
	/**
	 * @param a input list for the comparison.  a cannot contain nulls
	 * @param b input list for the comparison.  b cannot contain nulls
	 * @return A list of edit operations converting a to b
	 * <p>
	 * Runs in time O(n) and space O(n), where n is the length of the length of lists
	 * <p>
	 * Uses a.compareTo(b) to determine item ordering, and allows all changes
	 */
	public static <T extends Comparable<T>> List<Edit<T>> editScriptSorted (Collection<T> a, Collection<T> b) {
		return editScriptSorted(a,b,new DiffComparable<T>());
	}
	/**
	 * @param a input list for the comparison.  a cannot contain nulls
	 * @param b input list for the comparison.  b cannot contain nulls
	 * @param comp a Comparator used to order the entries from a and b.
	 * @return A list of edit operations converting a to b
	 * <p>
	 * Runs in time O(n) and space O(n), where n is the length of the length of lists
	 */
	public static <T> List<Edit<T>> editScriptSorted (Collection<T> a, Collection<T> b, Comparator<T> comp) {
		return editScriptSorted(a,b,new DiffComparator<T>(comp));
	}
	/**
	 * @param a input list for the comparison.  a cannot contain nulls
	 * @param b input list for the comparison.  b cannot contain nulls
	 * @param comp a Comparator used to order the entries from a and b.
	 * @param canChange a Comparator used to decide if entries from a can be changed to entries from b
	 * @return A list of edit operations converting a to b
	 * <p>
	 * Runs in time O(n) and space O(n), where n is the length of the length of lists
	 */
	public static <T> List<Edit<T>> editScriptSorted (Collection<T> a, Collection<T> b, Comparator<T> comp, Comparator<T> canChange) {
		return editScriptSorted(a,b,new DiffCompareChange<T>(comp, canChange));
	}
	/**
	 * Combines pairs of edits of type {@link EditType#OnlyInA} and {@link EditType#OnlyInB}
	 * into edits of type {@link EditType#ReplaceAwithB}.
	 * <p>
	 * The edits will be paired in the order they occur, but it pass over intermediate edits
	 * of type {@link EditType#ChangeAtoB}.  Thus, for example, an edit list of
	 * {@code OnlyInB, ChangeAtoB, OnlyIn B, OnlyInA} will be merged into
	 * {@code ChangeAtoB, OnlyInB, ReplaceAwithB}.
	 * 
	 * @param edits the input edit script
	 * @return the merged edit script
	 */
	public static <T> List<Edit<T>> mergeReplaces (Collection<Edit<T>> edits) {
		List<Edit<T>> ret = new LinkedList<Edit<T>>();
		LinkedList<Edit<T>> alist = new LinkedList<Edit<T>>();
		LinkedList<Edit<T>> blist = new LinkedList<Edit<T>>();
		int acount = 0;
		int bcount = 0;
		
		for (Edit<T> edit : edits) {
			switch (edit.type) {
			case OnlyInA: acount++; break;
			case OnlyInB: bcount++; break;
			default: break;
			}
		}
		
		for (Edit<T> edit : edits) {
			switch (edit.type) {
			case OnlyInA:
				if (!blist.isEmpty()) {
					Edit<T> b = blist.removeFirst();
					ret.add(new Edit<T>(EditType.ReplaceAwithB, edit.a_index, b.b_index, edit.a_value, b.b_value));
				} else if (bcount > 0) {
					acount--;
					bcount--;
					alist.add(edit);
				} else {
					acount--;
					ret.add(edit);
				}
				break;
			case OnlyInB:
				if (!alist.isEmpty()) {
					Edit<T> a = alist.removeFirst();
					ret.add(new Edit<T>(EditType.ReplaceAwithB, a.a_index, edit.b_index, a.a_value, edit.b_value));
				} else if (acount > 0) {
					acount--;
					bcount--;
					blist.add(edit);
				} else {
					bcount--;
					ret.add(edit);
				}
				break;
			default:
				ret.add(edit);
				break;
			}
		}
		
		if (!alist.isEmpty() || !blist.isEmpty()) {
			throw new RuntimeException("temporary list not empty in DiffUtil.mergeReplaces");
		}
		
		return ret;
	}

	private static class DistWorkSimple<T> {
		private final List<T> a;
		private final List<T> b;
		private final DiffEquals<T> comp;
		private final int[] dists;
		public DistWorkSimple(List<T> a, List<T> b, DiffEquals<T> comp) {
			this.a = a;
			this.b = b;
			this.comp = comp;
			dists = new int[(a.size()+1) * (b.size()+1)];
		}
		public int dist() {
			for (int j=0; j<=b.size(); j++) {
				dists[j] = j;
			}
			for (int i=1; i<=a.size(); i++) {
				int row = i*(b.size()+1);
				int prow = (i-1)*(b.size()+1);
				dists[row] = i;
				for (int j=1; j<=b.size(); j++) {
					if (comp.equals(a.get(i-1),b.get(j-1))) {
						dists[row+j] = dists[prow+j-1];
					} else {
						int d = dists[prow+j];
						if (dists[row+j-1] < d) d = dists[row+j-1];
						if (dists[prow+j-1] < d && comp.canChange(a.get(i-1), b.get(j-1))) {
							d = dists[prow+j-1];
						}
						dists[row+j] = d+1;
					}
				}
			}
			return dists[a.size() * (b.size()+1) + b.size()];
		}
	}
	protected static <T> int editDistSimple (List<T> a, List<T> b, DiffEquals<T> comp) {
		DistWorkSimple<T> distwork = new DistWorkSimple<T>(a,b,comp);
		return distwork.dist();
	}
	protected static <T> int editDistSimple (List<T> a, List<T> b) {
		return editDistSimple(a,b,new DiffEqualsDefault<T>());
	}
	protected static <T> int editDistSimple (List<T> a, List<T> b, Comparator<T> comp) {
		return editDistSimple(a,b,new DiffEqualsFromCompare<T>(comp));
	}
}
