package com.mcreations.log4j;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.util.Constants;

/**
 * Wraps a {@link ByteBuffer} and allocates a new one, if you try to write more
 * data than the current size of {@link #buf}.
 * 
 * The old buffer will only be garbage collected under memory pressure as it
 * relies on finalizers for GC as described in the corresponding bug description
 * (from 2001!) [1]. This can lead to 
 * By invoking System.gc(), we ensure that GC will run at some
 * appropriate time before high memory pressure occurs.
 * 
 * [1] http://bugs.java.com/view_bug.do?bug_id=4469299
 * 
 * @author darabi
 *
 */
public class ByteBufferOutputStream extends OutputStream implements ByteBufferDestination {

	private ByteBuffer buf;

	public ByteBufferOutputStream() {
		this(Constants.ENCODER_BYTE_BUFFER_SIZE);
	}

	public ByteBufferOutputStream(int size) {
		this(ByteBuffer.allocateDirect(size));
	}

	public ByteBufferOutputStream(ByteBuffer buffer) {
		buf = buffer;
	}

	@Override
	public void write(int b) throws IOException {
		if (!buf.hasRemaining())
			grow(buf.capacity() / 2);
		buf.put((byte) b);
	}

	@Override
	public void write(byte[] bytes, int offset, int length) throws IOException {
		// if bytes cannot be written fully, then fill up buf and then grow its
		// size
		if (buf.remaining() < length) {
			int remaining = buf.remaining();
			buf.put(bytes, offset, remaining);
			offset += remaining;
			length -= remaining;
			grow(buf.capacity() / 2);
			System.gc();
		}
		buf.put(bytes, offset, length);
	}

	/**
	 * Increase the size of the buf by adding <tt>additionalCapacity</tt> to the
	 * current size. This method allocates a new buf and copies the current
	 * content of buf into it.
	 * 
	 * @param i
	 */
	private void grow(int additionalCapacity) {
		buf = grow(buf, additionalCapacity);
	}

	private ByteBuffer grow(ByteBuffer tooSmall, int additionalCapacity) {
		ByteBuffer newBuf = ByteBuffer.allocateDirect(tooSmall.capacity() + additionalCapacity);
		newBuf.put(tooSmall);
		return newBuf;
	}

	/**
	 * Returns the <em>current</em> underlying {@link ByteBuffer} which might
	 * change during write operations, if its capacity limit is reached.
	 */
	public ByteBuffer getByteBuffer() {
		return buf;
	}

	public ByteBuffer drain(ByteBuffer buf) {
		if (buf != this.buf)
			throw new IllegalStateException("ByteBufferOutputStream.drain was called with an unknown buffer!");
		return grow(buf, buf.capacity() / 2);
	}
}
