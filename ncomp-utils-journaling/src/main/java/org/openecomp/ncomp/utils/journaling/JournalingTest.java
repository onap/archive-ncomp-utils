
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

import junit.framework.TestCase;

public class JournalingTest extends TestCase {
	private static File clean(File f) {
		if (f.isDirectory()) {
			for (File x: f.listFiles()) {
				clean(x);
			}
		}
		f.delete();
		return(f);
	}
	@SuppressWarnings("unchecked")
	public void test_case1() {
		File f = clean(new File("testdata/journal/case1"));
		JournalingHashMap<String> m = JournalingHashMap.create(f);
		m.put("foo", "bar");
		m.save();
		m.close();
		for (int i = 0; i < 3; i++) {
			// overwrite object with new. Can test that it has the correct
			// state.
			m = JournalingHashMap.create(f);
			assertEquals("bar", m.get("foo"));
			m.close();
		}
	}

	public void test_case2() {
		File f = clean(new File("testdata/journal/case2"));
		MyTestJObject o = MyTestJObject.create(f);
		o.setS("foo");
		o.setS("foo2");
		o.m.put("a", "b");
		o.l.add("foo1");
		o.l.add("foo2");
		o.l.add("foo3");
		o.l.remove(0);
		o.save();
		o.setS("bar");
		o.close();
		for (int i = 0; i < 3; i++) {
			o = MyTestJObject.create(f);
			assertEquals("foo2", o.getS());
			assertEquals("b", o.m.get("a"));
			assertEquals("foo2", o.l.get(0));
			o.close();
		}
	}

	public void test_case3() {
		File f = clean(new File("testdata/journal/case3"));
		MyTestJObject2 o = MyTestJObject2.create(f);
		o.setS("foo");
		// hashmap
		MyTestJObject oo = o.m.newKey("x", MyTestJObject.class);
		oo.m.put("a", "b");
		oo.setS("jjj");
		// list
		MyTestJObject ooo = o.l.addNew(MyTestJObject.class);
		ooo = o.l.addNew(MyTestJObject.class);
		ooo.m.put("aa", "bb");
		ooo.setS("kkk");
		// hashmaplist
		JournalingList<MyTestJObject> x = o.ml.newList("x");
		MyTestJObject xx = x.addNew(MyTestJObject.class);
		xx.m.put("aa", "bb");
		xx.setS("kkk");
		o.save();
		o.setS("bar");
		o.close();
		for (int i = 0; i < 3; i++) {
			// System.out.println("Round: " + i);
			o = MyTestJObject2.create(f);
			oo = o.m.get("x");
			assertEquals("foo", o.getS());
			assertEquals("jjj", oo.getS());
			assertEquals("b", oo.m.get("a"));
			assertEquals(2, o.l.size());
			ooo = o.l.get(1);
			assertEquals("kkk", ooo.getS());
			assertEquals("bb", ooo.m.get("aa"));
			assertEquals(0, o.l.get(0).m.size());
			xx = o.ml.get("x").get(0);
			assertEquals("kkk", xx.getS());
			assertEquals("bb", xx.m.get("aa"));
			assertEquals(0, o.l.get(0).m.size());

			o.close();
		}
	}

	public void test_case4() {
		File f = clean(new File("testdata/journal/case4"));
		MyTestJObject o = MyTestJObject.create(f);
		// o.setSnapshotInterval(1); // 1ms snapshots. This should force
		// snapshots on each save.
		o.setS("foo:1");
		o.setS("foo:2");
		o.save();
		// assertEquals(2,o.getLogSize());
		o.createSnapshot();
		// assertEquals(0,o.getLogSize());
		o.setS("foo:3");
		o.save();
		o.createSnapshot();
		o.l.add("foo1");
		o.save();
		o.close();

		for (int i = 0; i < 3; i++) {
			o = MyTestJObject.create(f);
			System.out.println("Round: " + i + " " + o);
			assertEquals("foo:3", o.getS());
			assertEquals("foo1", o.l.get(0));
			o.close();
		}
	}

	public void test_case5() {
		File f = clean(new File("testdata/journal/case5"));
		MyTestJObject2 o = MyTestJObject2.create(f);
		// o.setSnapshotInterval(1); // 1ms snapshots. This should force
		// snapshots on each save.
		o.setS("foo");
		o.save();
		for (int i = 0; i < 10; i++) {
			o.setS("foo:" + i);
		}
		assertTrue(o.getLogSize() <= 2);
		o.close();

		for (int i = 0; i < 3; i++) {
			o = MyTestJObject2.create(f);
			System.out.println("Round: " + i + " " + o);
			assertEquals("foo", o.getS());
			o.close();
		}
	}
}

class MyTestJObject extends JournalingObject implements Serializable {
	@Override
	public String toString() {
		return "MyTestJObject [s=" + s + ", m=" + m + ", l=" + l + "]";
	}

	private static final long serialVersionUID = 1L;
	private String s;
	JournalingHashMap<String> m;
	JournalingList<String> l;

	public MyTestJObject(String context, JournalingObject parent) {
		super(context, parent);
	}

	public MyTestJObject() {
		// TODO Auto-generated constructor stub
	}

	public String getS() {
		return s;
	}

	public void setS(String str) {
		logAttributeValue("s", str);
		this.s = str;
	}

	@Override
	public void init() {
		if (m == null)
			m = new JournalingHashMap<String>("m", this);
		else
			initChild("m", m);
		if (l == null)
			l = new JournalingList<String>("l", this);
		else
			initChild("l", l);
	}

	static public MyTestJObject create(File dir) {
		return (MyTestJObject) create2(dir, new MyTestJObject());
	}
}

class MyTestJObject2 extends JournalingObject implements Serializable {
	@Override
	public String toString() {
		return "MyTestJObject2 [s=" + s + ", m=" + m + ", l=" + l + ", ml="
				+ ml + "]";
	}

	private static final long serialVersionUID = 1L;
	private String s;
	JournalingHashMap<MyTestJObject> m;
	JournalingList<MyTestJObject> l;
	JournalingHashMap<JournalingList<MyTestJObject>> ml;

	public String getS() {
		return s;
	}

	public void setS(String str) {
		logAttributeValue("s", str);
		this.s = str;
	}

	@Override
	public void init() {
		if (m == null)
			m = new JournalingHashMap<MyTestJObject>("m", this);
		else
			initChild("m", m);
		if (l == null)
			l = new JournalingList<MyTestJObject>("l", this);
		else 
			initChild("l", l);
		if (ml == null)
			ml = new JournalingHashMap<JournalingList<MyTestJObject>>("ml", this);
		else 
			initChild("ml", ml);
	}

	static public MyTestJObject2 create(File dir) {
		return (MyTestJObject2) create2(dir, new MyTestJObject2());
	}

}
