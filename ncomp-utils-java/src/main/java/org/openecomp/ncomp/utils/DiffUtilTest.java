
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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.openecomp.ncomp.utils.DiffUtil.Edit;
import org.openecomp.ncomp.utils.DiffUtil.EditType;

import junit.framework.TestCase;

public class DiffUtilTest extends TestCase {
    private void stringtest (String a, String b, int expect) {
		List<String> alist = Arrays.asList(a.split(""));
		List<String> blist = Arrays.asList(b.split(""));
		int got = DiffUtil.editDist(alist.subList(1, alist.size()), blist.subList(1,blist.size()));
		assertEquals("editDist " + a + " " + b, expect, got);
    }
    private void stringscript (String a, String b, int expect) {
		List<String> alist = Arrays.asList(a.split(""));
		List<String> blist = Arrays.asList(b.split(""));
		List<Edit<String>> script = DiffUtil.editScript(alist.subList(1, alist.size()), blist.subList(1,blist.size()));
		assertEquals("editScript " + a + " " + b, expect, script.size());
    }
    private void stringtest_nochange (String a, String b, int expect) {
		List<String> alist = Arrays.asList(a.split(""));
		List<String> blist = Arrays.asList(b.split(""));
		int got = DiffUtil.editDist(alist.subList(1, alist.size()), blist.subList(1,blist.size()),
				new DiffUtil.DiffEqualsNoChange<String>());
		assertEquals("editDist " + a + " " + b, expect, got);
    }
    public void test_simple() {
    	String a = "simpletest";
    	String b = "complextest"; 
    	stringtest (a, b, 3);
    	stringtest (b, a, 3);
    	stringtest ("simple", "dimple", 1);
    	stringtest ("simple", "sample", 1);
    	stringtest ("simple", "simply", 1);
    	stringscript (a, b, 3);
    	stringscript (b, a, 3);
    }
    public void test_nochange() {
    	String a = "simpletest";
    	String b = "complextest"; 
    	stringtest_nochange (a, b, 5);
    	stringtest_nochange (b, a, 5);
    	stringtest_nochange ("simple", "dimple", 2);
    	stringtest_nochange ("simple", "sample", 2);
    	stringtest_nochange ("simple", "simply", 2);
    }
   private int gentest (long seed, List<Integer> alist, List<Integer> blist, int n, double pa,
    		double pb, double pc) {
    	alist.clear();
    	blist.clear();
    	SecureRandom r = new SecureRandom();
    	r.setSeed(seed);
    	int dist = 0;
    	for (int i=0; i<n; i++) {
    		double p = r.nextDouble();
    		if (p < pa) {
    			blist.add(r.nextInt());
    			dist++;
    		} else if (p < pa+pb) {
    			alist.add(r.nextInt());
    			dist++;
    		} else if (p < pa+pb+pc) {
    			alist.add(r.nextInt());
    			blist.add(r.nextInt());
    			dist++;
    		} else {
    			int d = r.nextInt();
    			alist.add(d);
    			blist.add(d);
    		}
    	}
		return dist;
    }
    private List<Edit<Integer>> gentestscript (long seed, List<Integer> alist, List<Integer> blist, int n, double pa,
    		double pb, double pc) {
    	alist.clear();
    	blist.clear();
    	List<Edit<Integer>> ret = new ArrayList<Edit<Integer>>();
    	SecureRandom r = new SecureRandom();
    	r.setSeed(seed);
    	for (int i=0; i<n; i++) {
    		double p = r.nextDouble();
    		if (p < pa) {
    			blist.add(r.nextInt());
    			ret.add(new Edit<Integer>(EditType.OnlyInB, -1, blist.size()-1, null, blist.get(blist.size()-1)));
    		} else if (p < pa+pb) {
    			alist.add(r.nextInt());
    			ret.add(new Edit<Integer>(EditType.OnlyInA, alist.size()-1, -1, alist.get(alist.size()-1), null));
    		} else if (p < pa+pb+pc) {
    			alist.add(r.nextInt());
    			blist.add(r.nextInt());
    			ret.add(new Edit<Integer>(EditType.ChangeAtoB, alist.size()-1, blist.size()-1, alist.get(alist.size()-1), blist.get(blist.size()-1)));
    		} else {
    			int d = r.nextInt();
    			alist.add(d);
    			blist.add(d);
    		}
    	}
		return ret;
    }
	public void test_stats() {
		List<Integer> alist = new ArrayList<Integer>();
		List<Integer> blist = new ArrayList<Integer>();
		List<Edit<Integer>> script1;
		List<Edit<Integer>> script2;
		int d1;
		int d2;
		script1 = gentestscript(100, alist, blist, 100, 0.01, 0.01, 0.01);
		script2 = DiffUtil.editScript(alist, blist);
		assertTrue("editScript random 100 0.01", script1.equals(script2));
		d1 = script1.size();
		d2 = DiffUtil.editDistStats(alist, blist);
		assertEquals("editDist random 100 0.01", d1,d2);
		d2 = DiffUtil.editDistSimple(alist, blist);
		assertEquals("editDistSimple random 100 0.01", d1,d2);
		script1 = gentestscript(110, alist, blist, 1000, 0.01, 0.01, 0.01);
		script2 = DiffUtil.editScript(alist, blist);
		assertTrue("editScript random 1000 0.01", script1.equals(script2));
		d1 = script1.size();
		d2 = DiffUtil.editDistStats(alist, blist);
		assertEquals("editDist random 1000 0.01", d1,d2);
		d2 = DiffUtil.editDistSimple(alist, blist);
		assertEquals("editDistSimple random 1000 0.01", d1,d2);
		d1 = gentest(120, alist, blist, 1000, 0.1, 0.1, 0.1);
		d2 = DiffUtil.editDistStats(alist, blist);
		assertEquals("editDist random 1000 0.1", 265,d2);
		d2 = DiffUtil.editDistSimple(alist, blist);
		assertEquals("editDistSimple random 1000 0.1", 265,d2);
		d1 = gentest(130, alist, blist, 10000, 0.01, 0.01, 0.01);
		d2 = DiffUtil.editDistStats(alist, blist);
		assertEquals("editDist random 10000 0.01", 296, d2);
//		d2 = DiffUtil.editDistSimple(alist, blist);
//		assertEquals("editDistSimple random 10000 0.01", 296, d2);
		script1 = gentestscript(140, alist, blist, 10000, 0.001, 0.001, 0.001);
		script2 = DiffUtil.editScript(alist, blist);
		assertTrue("editScript random 10000 0.001", script1.equals(script2));
		d1 = script1.size();
		d2 = DiffUtil.editDistStats(alist, blist);
		assertEquals("editDist random 10000 0.001", d1, d2);
		script1 = gentestscript(150, alist, blist, 100000, 0.001, 0.001, 0.001);
		script2 = DiffUtil.editScript(alist, blist);
		assertTrue("editScript random 100000 0.001", script1.equals(script2));
		d1 = script1.size();
		d2 = DiffUtil.editDistStats(alist, blist);
		assertEquals("editDist random 100000 0.001", d1, d2);
	}
	public void test_fenceposts() {
		String a = "simpletest";
		String b = "asimpletest";
		String c = "simpletestx";
		stringtest (a, b, 1);
		stringtest (a, c, 1);
		stringtest (b, c, 2);
		stringtest (b, a, 1);
		stringtest (c, a, 1);
		stringtest (c, b, 2);
		String x = "aaaaaaaaaa";
		String y = "abababababababababab";
		String z = "babababababababababa";
		stringtest (x, y, 10);
		stringtest (y, x, 10);
		stringtest (x, z, 10);
		stringtest (z, x, 10);
		stringtest (y, z, 2);
		stringtest (z, y, 2);
		stringtest ("", "hello", 5);
		stringtest ("hello", "", 5);
		stringtest ("", "", 0);
		stringtest ("hello", "hello", 0);
	}
	private static class CompareFirst implements Comparator<String> {
		public int compare(String a, String b) {
			return a.substring(0,1).compareTo(b.substring(0,1));
		}
	}
	private static class CompareString implements Comparator<String> {
		public int compare(String a, String b) {
			return a.compareTo(b);
		}
	}

	public void test_sorted() {
		CompareFirst cf = new CompareFirst();
		CompareString cs = new CompareString();
		List<String> a = new ArrayList<String>();
		List<String> b = new ArrayList<String>();
		a.add("aaa");
		a.add("abc");
		a.add("cba");
		List<Edit<String>> script;
		script = DiffUtil.editScriptSorted(a,  b);
		assertEquals("sorted test 1", 3, script.size());
		script = DiffUtil.editScriptSorted(a,  b, cs, cf);
		assertEquals("sorted test 2", 3, script.size());
		b.add("baa");
		script = DiffUtil.editScriptSorted(a,  b);
		assertEquals("sorted test 3", 3, script.size());
		script = DiffUtil.mergeReplaces(script);
		assertEquals("merged test 3", 3, script.size());
		script = DiffUtil.editScriptSorted(a,  b, cs, cf);
		assertEquals("sorted test 4", 4, script.size());
		script = DiffUtil.mergeReplaces(script);
		assertEquals("merged test 4", 3, script.size());
		b.add("caa");
		b.add("cba");
		a.add("cca");
		script = DiffUtil.editScriptSorted(a, b, cs, cf);
		assertEquals("sorted test 5", 4, script.size());
		script = DiffUtil.mergeReplaces(script);
		assertEquals("merged test 5", 3, script.size());
	}
}
