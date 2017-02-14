
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
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

public class ByteBufferInputStream implements ByteBufferStream {
	private final InputStream in;
	private byte[] buf;
	private ByteBuffer bbuf;
	boolean eof;
	private int offset;
	
	private void require (int len) throws IOException {
		if (bbuf.remaining() >= len) return;
		int remain = bbuf.remaining();
		offset += bbuf.position();
		if (len > buf.length) {
			byte[] newbuf = new byte[len];
			System.arraycopy (buf, bbuf.position(), newbuf, 0, remain);
			buf = newbuf;
		} else {
			System.arraycopy (buf, bbuf.position(), buf, 0, remain);
		}
		fill_buf (remain);
		while (bbuf.remaining() < len) {
			if (eof) {
				throw new BufferUnderflowException();
			}
			fill_buf (bbuf.remaining());
		}
	}
	
	private void fill_buf (int start) throws IOException {
		if (eof) return;
		int len = in.read(buf, start, buf.length - start);
		if (len == -1) {
			bbuf = ByteBuffer.wrap(buf, 0, 0);
			eof = true;
		} else {
			bbuf = ByteBuffer.wrap(buf, 0, start + len);
		}
	}
	
	public ByteBufferInputStream (InputStream _in) throws IOException {
		in = _in;
		buf = new byte[65536];
		eof = false;
		offset = 0;
		fill_buf (0);
	}

	public ByteBufferInputStream (InputStream _in, int init_length) throws IOException {
		in = _in;
		buf = new byte[init_length];
		eof = false;
		offset = 0;
		fill_buf (0);
	}

	@Override
	public ByteBufferStream clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ByteBufferStream flip() {
		throw new ReadOnlyBufferException();
	}

	@Override
	public ByteBufferStream rewind() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ByteBufferStream mark() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ByteBufferStream reset() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int capacity() {
		return bbuf.capacity();
	}

	@Override
	public boolean hasRemaining() throws IOException {
		if (bbuf.hasRemaining()) return true;
		if (eof) return false;
		fill_buf (0);
		return bbuf.hasRemaining();
	}

	@Override
	public int remaining() throws IOException {
		if (bbuf.hasRemaining()) return bbuf.remaining();
		if (eof) return 0;
		fill_buf (0);
		return bbuf.remaining();
	}

	@Override
	public int position() {
		return offset + bbuf.position();
	}

	@Override
	public ByteBufferStream position(int newPosition) throws IOException {
		if (newPosition < offset) {
			throw new UnsupportedOperationException("position backwards");
		}
		require (newPosition - position());
		bbuf.position (newPosition - offset);
		return this;
	}
	
	@Override
	public byte get() throws IOException {
		require (1);
		return bbuf.get();
	}

	@Override
	public byte get(int position) throws IOException {
		require (position - (offset + bbuf.position()) + 1);
		return bbuf.get(position - offset);
	}

	@Override
	public ByteBufferStream get(byte[] dst) throws IOException {
		require (dst.length);
		bbuf.get(dst);
		return this;
	}

	@Override
	public ByteBufferStream get(byte[] dst, int offset, int length) throws IOException {
		require (length);
		bbuf.get (dst, offset, length);
		return this;
	}

	@Override
	public char getChar() throws IOException {
		require (2);
		return bbuf.getChar();
	}

	@Override
	public double getDouble() throws IOException {
		require (8);
		return bbuf.getDouble();
	}

	@Override
	public float getFloat() throws IOException {
		require (4);
		return bbuf.getFloat();
	}

	@Override
	public int getInt() throws IOException {
		require (4);
		return bbuf.getInt();
	}

	@Override
	public long getLong() throws IOException {
		require (8);
		return bbuf.getLong();
	}

	@Override
	public short getShort() throws IOException {
		require (2);
		return bbuf.getShort();
	}

	@Override
	public short getShort(int position) throws IOException {
		require (position - (offset + bbuf.position()) + 2);
		return bbuf.getShort(position - offset);
	}

	@Override
	public ByteBufferStream put(byte b) {
		throw new ReadOnlyBufferException();
	}

	@Override
	public ByteBufferStream put(byte[] src) {
		throw new ReadOnlyBufferException();
	}

	@Override
	public ByteBufferStream put(byte[] src, int offset, int length) {
		throw new ReadOnlyBufferException();
	}

	@Override
	public ByteBufferStream putChar(char value) {
		throw new ReadOnlyBufferException();
	}

	@Override
	public ByteBufferStream putDouble(double value) {
		throw new ReadOnlyBufferException();
	}

	@Override
	public ByteBufferStream putFloat(float value) {
		throw new ReadOnlyBufferException();
	}

	@Override
	public ByteBufferStream putInt(int value) {
		throw new ReadOnlyBufferException();
	}

	@Override
	public ByteBufferStream putLong(long value) {
		throw new ReadOnlyBufferException();
	}

	@Override
	public ByteBufferStream putShort(short value) {
		throw new ReadOnlyBufferException();
	}

}
