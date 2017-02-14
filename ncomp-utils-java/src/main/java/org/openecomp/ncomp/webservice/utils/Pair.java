
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

public class Pair<T, U> {
	private final T first;
	private final U second;
	private transient final int hash;

	public Pair(T f, U s) {
		this.first = f;
		this.second = s;
		hash = (first == null ? 0 : first.hashCode() * 31) + (second == null ? 0 : second.hashCode());
	}

	public T getFirst() {
		return first;
	}

	public U getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object oth) {
		if (this == oth) {
			return true;
		}
		if (oth == null || !(getClass().isInstance(oth))) {
			return false;
		}
		Pair<T, U> other = getClass().cast(oth);
		return (first == null ? other.first == null : first.equals(other.first))
				&& (second == null ? other.second == null : second.equals(other.second));
	}

}