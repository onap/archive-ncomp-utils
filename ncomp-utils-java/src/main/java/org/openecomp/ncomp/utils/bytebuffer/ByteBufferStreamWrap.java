
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

import java.nio.ByteBuffer;


public class ByteBufferStreamWrap implements ByteBufferStream {
	private final ByteBuffer buf;

	public ByteBufferStreamWrap (ByteBuffer buf_) {
		buf = buf_;
	}

	@Override
	public ByteBufferStream clear() {
		buf.clear();
		return this;
	}

	@Override
	public ByteBufferStream flip() {
		buf.flip();
		return this;
	}

	@Override
	public ByteBufferStream rewind() {
		buf.rewind();
		return this;
	}

	@Override
	public ByteBufferStream mark () {
		buf.mark();
		return this;
	}

	@Override
	public ByteBufferStream reset() {
		buf.reset();
		return this;
	}

	@Override
	public int capacity() {
		return buf.capacity();
	}

	@Override
	public boolean hasRemaining() {
		return buf.hasRemaining();
	}

	@Override
	public int remaining() {
		return buf.remaining();
	}

	@Override
	public int position() {
		return buf.position();
	}

	@Override
	public ByteBufferStream position(int newPosition) {
		buf.position(newPosition);
		return this;
	}

	@Override
	public byte get() {
		return buf.get();
	}

	@Override
	public ByteBufferStream get(byte[] dst) {
		buf.get(dst);
		return this;
	}

	@Override
	public ByteBufferStream get(byte[] dst, int offset, int length) {
		buf.get (dst, offset, length);
		return this;
	}

	@Override
	public char getChar() {
		return buf.getChar();
	}

	@Override
	public double getDouble() {
		return buf.getDouble();
	}

	@Override
	public float getFloat() {
		return buf.getFloat();
	}

	@Override
	public int getInt() {
		return buf.getInt();
	}

	@Override
	public long getLong() {
		return buf.getLong();
	}

	@Override
	public short getShort() {
		return buf.getShort();
	}

	@Override
	public ByteBufferStream put(byte b) {
		buf.put(b);
		return this;
	}

	@Override
	public ByteBufferStream put(byte[] src) {
		buf.put(src);
		return this;
	}

	@Override
	public ByteBufferStream put(byte[] src, int offset, int length) {
		buf.put(src, offset, length);
		return this;
	}

	@Override
	public ByteBufferStream putChar(char value) {
		buf.putChar(value);
		return this;
	}

	@Override
	public ByteBufferStream putDouble(double value) {
		buf.putDouble(value);
		return this;
	}

	@Override
	public ByteBufferStream putFloat(float value) {
		buf.putFloat(value);
		return this;
	}

	@Override
	public ByteBufferStream putInt(int value) {
		buf.putInt(value);
		return this;
	}

	@Override
	public ByteBufferStream putLong(long value) {
		buf.putLong(value);
		return this;
	}

	@Override
	public ByteBufferStream putShort(short value) {
		buf.putShort(value);
		return this;
	}

	@Override
	public byte get(int position) {
		return buf.get(position);
	}

	@Override
	public short getShort(int position) {
		return buf.getShort(position);
	}
}
