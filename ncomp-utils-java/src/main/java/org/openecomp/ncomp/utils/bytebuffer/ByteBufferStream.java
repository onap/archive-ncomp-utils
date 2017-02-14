
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
	
package org.openecomp.ncomp.utils.bytebuffer;

import java.io.IOException;

public interface ByteBufferStream {
	public ByteBufferStream clear() throws IOException;
	public ByteBufferStream flip() throws IOException;
	public ByteBufferStream rewind() throws IOException;
	public ByteBufferStream mark() throws IOException;
	public ByteBufferStream reset() throws IOException;
	public int capacity() throws IOException;
	public boolean hasRemaining() throws IOException;
	public int remaining() throws IOException;
	public int position() throws IOException;
	public ByteBufferStream position(int newPosition) throws IOException;
	public byte get() throws IOException;
	public byte get(int position) throws IOException;
	public ByteBufferStream get(byte[] dst) throws IOException;
	public ByteBufferStream get(byte[] dst, int offset, int length) throws IOException;
	public char getChar() throws IOException;
	public double getDouble() throws IOException;
	public float getFloat() throws IOException;
	public int getInt() throws IOException;
	public long getLong() throws IOException;
	public short getShort() throws IOException;
	public short getShort(int position) throws IOException;
	public ByteBufferStream put(byte b) throws IOException;
	public ByteBufferStream put(byte[] src) throws IOException;
	public ByteBufferStream put(byte[] src, int offset, int length) throws IOException;
	public ByteBufferStream putChar(char value) throws IOException;
	public ByteBufferStream putDouble(double value) throws IOException;
	public ByteBufferStream putFloat(float value) throws IOException;
	public ByteBufferStream putInt(int value) throws IOException;
	public ByteBufferStream putLong(long value) throws IOException;
	public ByteBufferStream putShort(short value) throws IOException;
}
