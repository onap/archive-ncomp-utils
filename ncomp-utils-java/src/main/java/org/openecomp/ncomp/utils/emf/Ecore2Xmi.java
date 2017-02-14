
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

import java.io.IOException;
import java.lang.reflect.Field;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;

import org.openecomp.ncomp.webservice.utils.FileUtils;

public class Ecore2Xmi {

	public static void main(String[] args) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException {
		// Usage name of Package class file ....
		System.out.println(args.toString());
		for (int i = 0; i<args.length ; i += 2) {
			System.out.println(args[i]);
			System.out.println(args[i+1]);
			Class<?> cls = Class.forName(args[i]);
			Field f = cls.getDeclaredField("eINSTANCE");
			EPackage p = (EPackage) f.get(null);
			FileUtils.ecore2file(EcorePackage.eINSTANCE, p, args[i+1]);
		}
		System.exit(0);
	}

}
