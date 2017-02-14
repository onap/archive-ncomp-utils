
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

import java.util.Iterator;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public class MonitoringDynamicMBean implements DynamicMBean {
	private EObject o;
	private String name;

	public MonitoringDynamicMBean(EObject o, String name) {
		super();
		this.o = o;
		this.name = name;
	}

	@Override
	public Object getAttribute(String arg0) throws AttributeNotFoundException, MBeanException, ReflectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AttributeList getAttributes(String[] names) {
		AttributeList list = new AttributeList();
		for (String name : names) {
			EStructuralFeature f = o.eClass().getEStructuralFeature(name);
			if (f == null || !(f instanceof EAttribute))
				continue;
			EAttribute attr = (EAttribute) f;
			Object value = o.eGet(attr);
			if (value == null)
				continue;
			list.add(new Attribute(name, value.toString()));
		}
		return list;
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		EList<MBeanAttributeInfo> attrs = new BasicEList<MBeanAttributeInfo>();
		for (EAttribute attr : o.eClass().getEAllAttributes()) {
			if (attr.getEType().getName().equals("EBoolean")) {
				attrs.add(new MBeanAttributeInfo(attr.getName(), "java.lang.Boolean", attr.getName(), true, true, true));
			} else {
				attrs.add(new MBeanAttributeInfo(attr.getName(), "java.lang.String", "NAME " + attr.getName(), true,
						true, false));
			}
		}
		// sample
		MBeanOperationInfo[] opers = { new MBeanOperationInfo("reload", "Reload properties from file", null, "void", MBeanOperationInfo.ACTION) };
		// This is need since attrs.toArray() does not work.
		MBeanAttributeInfo[] attrs2 = new MBeanAttributeInfo[attrs.size()];
		Iterator<MBeanAttributeInfo> it2 = attrs.iterator();
		for (int i = 0; i < attrs.size(); i++) {
			attrs2[i] = it2.next();
		}
		MBeanInfo m = new MBeanInfo(this.getClass().getName(), name, attrs2, null, opers, null);
		return m;
	}

	@Override
	public Object invoke(String arg0, Object[] arg1, String[] arg2) throws MBeanException, ReflectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(Attribute arg0) throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {
		// TODO Auto-generated method stub

	}

	@Override
	public AttributeList setAttributes(AttributeList arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
