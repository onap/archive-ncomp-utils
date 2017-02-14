
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

import java.lang.management.ManagementFactory;

public class JvmUtils {
	static long getProcessId() {
		long fallback = -1;
		final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		final int index = jvmName.indexOf('@');

		if (index < 1) {
			// part before '@' empty (index = 0) / '@' not found (index = -1)
			return fallback;
		}

		try {
			return Long.parseLong(jvmName.substring(0, index));
		} catch (NumberFormatException e) {
			// ignore
		}
		return fallback;
	}
}
