
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

import java.net.InetAddress;

import org.openecomp.ncomp.webservice.utils.IpUtils;

public class InetPrefix {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + maskLength;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InetPrefix other = (InetPrefix) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (maskLength != other.maskLength)
			return false;
		return true;
	}
	public InetPrefix(InetAddress ip, int maskLength) {
		this.ip = IpUtils.mask(ip, maskLength);
		this.maskLength = maskLength;
	}
	public InetPrefix(String s) {
		String a[] = s.split("/");
		if (a.length == 2 && IpUtils.isIp(a[0])) {
			this.maskLength = Integer.parseInt(a[1]);
			this.ip = IpUtils.mask(IpUtils.toInetAddress(a[0]), maskLength);
			return;
		}
		throw new RuntimeException("bad prefix" + s);
	}
	public InetAddress getIp() {
		return ip;
	}
	protected void setIp(InetAddress ip) {
		this.ip = ip;
	}
	public int getMaskLength() {
		return maskLength;
	}
	protected void setMaskLength(int maskLength) {
		this.maskLength = maskLength;
	}
	private InetAddress ip;
	private int maskLength;
	@Override
	public String toString() {
		return ip.getHostAddress() + "/" + maskLength;
	}
	

}
