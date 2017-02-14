
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.common.util.Enumerator;



public class Ecore2JavaUtils {
	String prefix;
	String packageName;

	public Ecore2JavaUtils(String packageName, String prefix) {
		this.packageName = packageName;
		this.prefix = prefix;
	}

	public Object ecore2object(EObject e) {
		return ecore2object(e, true);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object ecore2object(EObject e, boolean recursive) {
		Class c;
		Object o;
		try {
			c = Class.forName(packageName + "." + prefix + e.eClass().getName());
			Constructor constructor = c.getConstructor();
			o = constructor.newInstance();
			for (EAttribute attr : e.eClass().getEAllAttributes()) {
				Field field = c.getField(attr.getName());
				if (attr.isMany()) {
					ArrayList a = new ArrayList();
					EList<Object> l = (EList<Object>) e.eGet(attr);
					for (Object oo : l) {
						a.add(evalue(e,attr,field.getType(),oo));
					}
					field.set(o, a);
				} else {
					field.set(o, evalue(e,attr,field.getType(),e.eGet(attr)));
				}
			}
			if (!recursive)
				return o;
			for (EReference ref : e.eClass().getEAllReferences()) {
				Field field = c.getField(ref.getName());
				if (ref.isMany()) {
					ArrayList a = new ArrayList();
					EList<EObject> l = (EList<EObject>) e.eGet(ref);
					for (EObject ee : l) {
						a.add(ecore2object(ee));
					}
					field.set(o, a);
				} else {
					field.set(o, ecore2object((EObject) e.eGet(ref)));
				}
			}
			return o;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object evalue(EObject e, EAttribute attr, Class c, Object oo) {
		// TODO handle EEnum case
		if (attr.getEType() instanceof EEnum) {
//			GuiView v = GuiView.BIRTREPORT;
//			XGuiView xv = XGuiView.valueOf(v.getLiteral());
			Enumerator e1 = (Enumerator) oo;
			try {
				Method m = c.getMethod("valueOf", String.class);
				return m.invoke(null,e1.getLiteral());
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			return null;
		}
		return oo;
	}

	public EObject object2ecore(EClass eClass, Object e) {
		// TODO
		return null;
	}

	public Object doOperation(EObject e, String opName, List<Object> args) {
		return null;
	}

}
