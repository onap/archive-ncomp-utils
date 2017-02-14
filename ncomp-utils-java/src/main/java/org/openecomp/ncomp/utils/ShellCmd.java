
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import org.openecomp.ncomp.webservice.utils.FileUtils;

public class ShellCmd extends Thread {
	public static final Logger logger = Logger.getLogger(ShellCmd.class);
	private final Process process;
	private Integer exitStatus;
	private Thread t1;
	private Thread t2;
	private ByteArrayOutputStream e;
	private ByteArrayOutputStream o;
	
	public static void main(String[] args) throws Exception {
		ShellCmd s = new ShellCmd("/home/ncomp/test.sh");
		System.out.println(s.result(600000));
		System.err.println(s.error());
	}

	public ShellCmd(String cmd) throws IOException {
		this.process = Runtime.getRuntime().exec(cmd);
		o = new ByteArrayOutputStream();
		e = new ByteArrayOutputStream();
		t1 = FileUtils.copyStreamThread(process.getInputStream(), o);
		t2 = FileUtils.copyStreamThread(process.getErrorStream(), e);
	}
	
	public String error() {
		return e.toString();
	}

	public String result(long wait) throws TimeoutException, InterruptedException {
		try {
			start();
			join(wait);
		} catch (InterruptedException ex) {
			interrupt();
			Thread.currentThread().interrupt();
			throw ex;
		} finally {
			if (process != null) {
				try {
					if (process.getOutputStream() != null)
						process.getOutputStream().close();
				} catch (IOException e1) {
				}
				try {
					if (process.getErrorStream() != null)
						process.getErrorStream().close();
				} catch (IOException e1) {
				}
				try {
					if (process.getInputStream() != null)
						process.getInputStream().close();
				} catch (IOException e1) {
				}
				process.destroy();
			}
		}

		if (exitStatus == null) {
			t1.interrupt();
			t2.interrupt();
			System.out.println(o.toString());
			System.err.println(e.toString());
			throw new TimeoutException();
		}
		if (exitStatus != 0) {
			logger.warn("return error: exit status = " + exitStatus);
			throw new RuntimeException("Non-Zero return status:" + exitStatus);
		}
		return o.toString();
	}

	public void run() {
		try {
			exitStatus = process.waitFor();
			t1.join();
			t2.join();
		} catch (InterruptedException ignore) {
			return;
		}
	}
}

