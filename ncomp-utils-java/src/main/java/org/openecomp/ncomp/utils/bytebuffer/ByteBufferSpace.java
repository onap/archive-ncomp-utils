
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
import java.nio.ByteBuffer;


public class ByteBufferSpace implements ByteBufferStream {
	private byte[] bytes;
	private ByteBuffer buf;
	
	public ByteBufferSpace() {
		bytes = new byte[32];
		buf = ByteBuffer.wrap(bytes);
	}
	public ByteBufferSpace(int size) {
		bytes = new byte[size];
		buf = ByteBuffer.wrap(bytes);
	}
	
	private ByteBufferSpace(byte[] bytes) {
		this.bytes = bytes;
		buf = ByteBuffer.wrap(bytes);		
	}
	
	public ByteBufferSpace dup() {
		return new ByteBufferSpace(bytes);
	}
	
	public void reserve (int size) {
		int pos = buf.position();
		if (pos + size > buf.capacity()) {
			int len = bytes.length;
			while (len < pos + size) len *= 2;
			byte[] oldbytes = bytes;
			bytes = new byte[len];
			buf = ByteBuffer.wrap(bytes);
			buf.put(oldbytes);
			buf.position(pos);
		}
	}

	public ByteBufferSpace clear() {
		buf.clear();
		return this;
	}
	public ByteBufferSpace flip() {
		buf.flip();
		return this;
	}

	public ByteBufferSpace rewind() {
		buf.rewind();
		return this;
	}

	public ByteBufferSpace mark() {
		buf.mark();
		return this;
	}

	public ByteBufferSpace reset() {
		buf.reset();
		return this;
	}

	public int capacity() {
		return buf.capacity();
	}

	public boolean hasRemaining() {
		return buf.hasRemaining();
	}
	public int remaining() {
		return buf.remaining();
	}

	public int position() {
		return buf.position();
	}
	public ByteBufferSpace position(int newPosition) {
		if (newPosition > buf.position()) {
			reserve (newPosition - buf.position());
		}
		buf.position(newPosition);
		return this;
	}

	public byte get() {
		return buf.get();
	}

	@Override
	public byte get(int position) throws IOException {
		return position(position).get();
	}

	public ByteBufferSpace get(byte[] dst) {
		buf.get(dst);
		return this;
	}
	public ByteBufferSpace get(byte[] dst, int offset, int length) {
		buf.get(dst, offset, length);
		return this;
	}
	public char getChar() {
		return buf.getChar();
	}
	public double getDouble() {
		return buf.getDouble();
	}
	public float getFloat() {
		return buf.getFloat();
	}
	public int getInt() {
		return buf.getInt();
	}
	public long getLong() {
		return buf.getLong();
	}
	public short getShort() {
		return buf.getShort();
	}
	@Override
	public short getShort(int position) {
		return position(position).getShort();
	}

	public ByteBufferSpace put(byte b) {
		reserve(1);
		buf.put(b);
		return this;
	}
	public ByteBufferSpace put(byte[] src) {
		reserve(src.length);
		buf.put(src);
		return this;
	}
	public ByteBufferSpace put(byte[] src, int offset, int length) {
		reserve(length);
		buf.put(src, offset, length);
		return this;
	}    public ByteBufferSpace put(ByteBuffer src) {
		reserve(src.remaining());
		buf.put(src);
		return this;
	}
	public ByteBufferSpace putChar(char value) {
		reserve(2);
		buf.putChar(value);
		return this;
	}
	public ByteBufferSpace putDouble(double value) {
		reserve(8);
		buf.putDouble(value);
		return this;
	}
	public ByteBufferSpace putFloat(float value) {
		reserve(4);
		buf.putFloat(value);
		return this;
	}
	public ByteBufferSpace putInt(int value) {
		reserve(4);
		buf.putInt(value);
		return this;
	}
	public ByteBufferSpace putLong(long value) {
		reserve(8);
		buf.putLong(value);
		return this;
	}
	public ByteBufferSpace putShort(short value) {
		reserve(2);
		buf.putShort(value);
		return this;
	}
	public String toString() {
		return buf.toString();
	}

	public int hashCode() {
		return buf.hashCode();
	}

	public boolean equals(Object ob) {
		if (! (ob instanceof ByteBufferSpace)) return false;
		return buf.equals(((ByteBufferSpace) ob).buf);
	}
	
	public void trimToSize() {
		int pos = buf.position();
		byte[] oldbytes = bytes;
		bytes = new byte[pos];
		buf = ByteBuffer.wrap(bytes);
		buf.put(oldbytes, 0, pos);
	}
	
	public int memSize() {
		return bytes.length + 160;
	}
	
	public static void main (String[] argv) {
		ByteBufferSpace b = new ByteBufferSpace(32);
		byte chk[] = new byte[2];
		chk[0] = (byte) 1;
		chk[1] = (byte) 2;
		for (int i=0; i<3000000; i++) {
			b.put((byte) 0).put(chk).putInt(3).putShort((short) 4).putFloat((float) 5.0).putDouble(6.0);
		}
		System.out.println("buffer size " + b.capacity());
		b.flip();
		for (int i=0; i<3000000; i++) {
			byte bchk = b.get();
			byte bchk2[] = new byte[2];
			b.get(bchk2);
			int ichk = b.getInt();
			short schk = b.getShort();
			float fchk = b.getFloat();
			double dchk = b.getDouble();
			if (bchk != 0 || bchk2[0] != 1 || bchk2[1] != 2 ||
					ichk != 3 || schk != 4 || fchk != 5.0 || dchk != 6.0) {
				System.out.println("i = " + i + ": bad data\n");
			}
		}
	}
}
