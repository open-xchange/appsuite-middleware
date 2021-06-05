/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.tools.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * {@link UnsynchronizedByteArrayInputStream} - A simple unsynchronized byte array input stream.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnsynchronizedByteArrayInputStream extends ByteArrayInputStream {

    /**
     * Creates a <code>UnsynchronizedByteArrayInputStream</code> so that it uses <code>buf</code> as its buffer array. The buffer array is
     * not copied. The initial value of <code>pos</code> is <code>0</code> and the initial value of <code>count</code> is the length of
     * <code>buf</code>.
     *
     * @param buf The input buffer.
     */
    public UnsynchronizedByteArrayInputStream(final byte buf[]) {
        super(buf);
    }

    /**
     * Creates <code>UnsynchronizedByteArrayInputStream</code> that uses <code>buf</code> as its buffer array. The initial value of
     * <code>pos</code> is <code>offset</code> and the initial value of <code>count</code> is the minimum of <code>offset+length</code> and
     * <code>buf.length</code>. The buffer array is not copied. The buffer's mark is set to the specified offset.
     *
     * @param buf The input buffer.
     * @param offset The offset in the buffer of the first byte to read.
     * @param length The maximum number of bytes to read from the buffer.
     */
    public UnsynchronizedByteArrayInputStream(final byte buf[], final int offset, final int length) {
        super(buf, offset, length);
    }

    /**
     * Reads the next byte of data from this input stream. The value byte is returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream has been reached, the value <code>-1</code> is returned.
     * <p>
     * This <code>read</code> method cannot block.
     *
     * @return The next byte of data, or <code>-1</code> if the end of the stream has been reached.
     */
    @Override
    public int read() {
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
    }

    /**
     * Reads up to <code>len</code> bytes of data into an array of bytes from this input stream. If <code>pos</code> equals
     * <code>count</code>, then <code>-1</code> is returned to indicate end of file. Otherwise, the number <code>k</code> of bytes read is
     * equal to the smaller of <code>len</code> and <code>count-pos</code>. If <code>k</code> is positive, then bytes <code>buf[pos]</code>
     * through <code>buf[pos+k-1]</code> are copied into <code>b[off]</code> through <code>b[off+k-1]</code> in the manner performed by
     * <code>System.arraycopy</code>. The value <code>k</code> is added into <code>pos</code> and <code>k</code> is returned.
     * <p>
     * This <code>read</code> method cannot block.
     *
     * @param b The buffer into which the data is read.
     * @param off The start offset in the destination array <code>b</code>
     * @param len The maximum number of bytes read.
     * @return The total number of bytes read into the buffer, or <code>-1</code> if there is no more data because the end of the stream has
     *         been reached.
     * @exception NullPointerException If <code>b</code> is <code>null</code>.
     * @exception IndexOutOfBoundsException If <code>off</code> is negative, <code>len</code> is negative, or <code>len</code> is greater
     *                than <code>b.length - off</code>
     */
    @Override
    public int read(final byte b[], final int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        if (pos >= count) {
            return -1;
        }
        int length = len;
        if (pos + length > count) {
            length = count - pos;
        }
        if (length <= 0) {
            return 0;
        }
        System.arraycopy(buf, pos, b, off, length);
        pos += length;
        return length;
    }

    /**
     * Skips <code>n</code> bytes of input from this input stream. Fewer bytes might be skipped if the end of the input stream is reached.
     * The actual number <code>k</code> of bytes to be skipped is equal to the smaller of <code>n</code> and <code>count-pos</code>. The
     * value <code>k</code> is added into <code>pos</code> and <code>k</code> is returned.
     *
     * @param n The number of bytes to be skipped.
     * @return The actual number of bytes skipped.
     */
    @Override
    public long skip(long n) {
        long toSkip = n;
        if (pos + toSkip > count) {
            toSkip = count - pos;
        }
        if (toSkip < 0) {
            return 0;
        }
        pos += toSkip;
        return toSkip;
    }

    /**
     * Returns the number of remaining bytes that can be read (or skipped over) from this input stream.
     * <p>
     * The value returned is <code>count&nbsp;- pos</code>, which is the number of bytes remaining to be read from the input buffer.
     *
     * @return The number of remaining bytes that can be read (or skipped over) from this input stream without blocking.
     */
    @Override
    public int available() {
        return count - pos;
    }

    /**
     * Tests if this <code>InputStream</code> supports mark/reset. The <code>markSupported</code> method of
     * <code>UnsynchronizedByteArrayInputStream</code> always returns <code>true</code>.
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Set the current marked position in the stream. ByteArrayInputStream objects are marked at position zero by default when constructed.
     * They may be marked at another position within the buffer by this method.
     * <p>
     * If no mark has been set, then the value of the mark is the offset passed to the constructor (or 0 if the offset was not supplied).
     * <p>
     * Note: The <code>readAheadLimit</code> for this class has no meaning.
     */
    @Override
    public void mark(final int readAheadLimit) {
        mark = pos;
    }

    /**
     * Resets the buffer to the marked position. The marked position is 0 unless another position was marked or an offset was specified in
     * the constructor.
     */
    @Override
    public void reset() {
        pos = mark;
    }

    /**
     * Closing a <tt>ByteArrayInputStream</tt> has no effect. The methods in this class can be called after the stream has been closed
     * without generating an <tt>IOException</tt>.
     */
    @Override
    public void close() throws IOException {
        // Nothing
    }

    /**
     * Gets this <tt>ByteArrayInputStream</tt>'s current position.
     *
     * @return The position
     */
    public int getPosition() {
        return pos;
    }

}
