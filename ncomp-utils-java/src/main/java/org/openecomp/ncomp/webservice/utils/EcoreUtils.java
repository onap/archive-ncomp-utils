
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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;


public class EcoreUtils<T extends EObject> {
	static String ecore2str(EObject e, int i, String[] delim) {
		EStructuralFeature f = e.eClass().getEStructuralFeature(i);
		return e.eGet(f).toString();
//		switch (f.getEType().getClassifierID()) {
//		case EcorePackage.ESTRING:
//		case EcorePackage.EINT:
//			return e.eGet(f).toString();
//		}
//		throw new RuntimeException("EcoreUtils::ecore2str: Unknow feature");
		
	}
	static String ecore2str(EObject e, int[] i, String delim[]) {
		StringBuffer res = new StringBuffer();
		for (int j = 0; j<i.length ; j++) {
			if (j>0) res.append(delim[0]);
			res.append(ecore2str(e,i[j],delim));
		}
		return res.toString();
	}
	static String ecore2str(EObject e, int[][] i, String delim[]) {
		throw new RuntimeException("TODO");		
	}
	public String ecorelist2str(EList<T> list) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for (T e : list) {
			if (!first) { 
				buf.append(":");
			}
			first = false;
			buf.append(e.toString());
		}
		return buf.toString();
	}
	
}
