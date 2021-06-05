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

package com.openexchange.java;

import java.io.IOException;
import java.io.StringWriter;

/**
 * {@link UnsynchronizedStringWriter} - An unsynchronized {@link java.io.StringWriter}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UnsynchronizedStringWriter extends StringWriter {

    private final StringBuilder buf;

    /**
     * Create a new string writer, using the default initial string-buffer size.
     */
    public UnsynchronizedStringWriter() {
        this(16);
    }

    /**
     * Create a new string writer, using the specified initial string-buffer size.
     *
     * @param initialSize A non-negative <code>int</code> specifying the initial size of the buffer.
     * @throws IllegalArgumentException If initial size is negative
     */
    public UnsynchronizedStringWriter(int initialSize) {
        super();
        if (initialSize < 0) {
            throw new IllegalArgumentException("Negative buffer size");
        }
        buf = new StringBuilder(initialSize);
        lock = buf;
    }

    /**
     * Create a new string writer, using the specified {@code StringBuilder} instance.
     *
     * @param buf A non-null {@code StringBuilder} instance
     * @throws IllegalArgumentException If {@code StringBuilder} instance is <code>null</code>
     */
    public UnsynchronizedStringWriter(StringBuilder buf) {
        super();
        if (buf == null) {
            throw new IllegalArgumentException("Buffer is null");
        }
        this.buf = buf;
        lock = buf;
    }

    /**
     * Write a single character.
     */
    @Override
    public void write(final int c) {
        buf.append((char) c);
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param cbuf Array of characters
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     * @throws IndexOutOfBoundsException If invalid offset or length is specified
     */
    @Override
    public void write(final char cbuf[], final int off, final int len) {
        if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        buf.append(cbuf, off, len);
    }

    /**
     * Write a string.
     */
    @Override
    public void write(final String str) {
        buf.append(str);
    }

    /**
     * Write a portion of a string.
     *
     * @param str String to be written
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    @Override
    public void write(final String str, final int off, final int len) {
        buf.append(str.substring(off, off + len));
    }

    /**
     * Appends the specified character sequence to this writer.
     * <p>
     * An invocation of this method of the form <tt>out.append(csq)</tt> behaves in exactly the same way as the invocation
     *
     * <pre>
     * out.write(csq.toString())
     * </pre>
     * <p>
     * Depending on the specification of <tt>toString</tt> for the character sequence <tt>csq</tt>, the entire sequence may not be appended.
     * For instance, invoking the <tt>toString</tt> method of a character buffer will return a subsequence whose content depends upon the
     * buffer's position and limit.
     *
     * @param csq The character sequence to append. If <tt>csq</tt> is <tt>null</tt>, then the four characters <tt>"null"</tt> are appended
     *            to this writer.
     * @return This writer
     * @since 1.5
     */
    @Override
    public StringWriter append(final CharSequence csq) {
        if (csq == null) {
            write("null");
        } else {
            write(csq.toString());
        }
        return this;
    }

    /**
     * Appends a subsequence of the specified character sequence to this writer.
     * <p>
     * An invocation of this method of the form <tt>out.append(csq, start,
     * end)</tt> when <tt>csq</tt> is not <tt>null</tt>, behaves in exactly the same way as the invocation
     *
     * <pre>
     * out.write(csq.subSequence(start, end).toString())
     * </pre>
     *
     * @param csq The character sequence from which a subsequence will be appended. If <tt>csq</tt> is <tt>null</tt>, then characters will
     *            be appended as if <tt>csq</tt> contained the four characters <tt>"null"</tt>.
     * @param start The index of the first character in the subsequence
     * @param end The index of the character following the last character in the subsequence
     * @return This writer
     * @throws IndexOutOfBoundsException If <tt>start</tt> or <tt>end</tt> are negative, <tt>start</tt> is greater than <tt>end</tt>, or
     *             <tt>end</tt> is greater than <tt>csq.length()</tt>
     * @since 1.5
     */
    @Override
    public StringWriter append(final CharSequence csq, final int start, final int end) {
        final CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }

    /**
     * Appends the specified character to this writer.
     * <p>
     * An invocation of this method of the form <tt>out.append(c)</tt> behaves in exactly the same way as the invocation
     *
     * <pre>
     * out.write(c)
     * </pre>
     *
     * @param c The 16-bit character to append
     * @return This writer
     * @since 1.5
     */
    @Override
    public StringWriter append(final char c) {
        write(c);
        return this;
    }

    /**
     * Return the buffer's current value as a string.
     */
    @Override
    public String toString() {
        return buf.toString();
    }

    /**
     * Returns the string buffer itself.
     *
     * @return The {@link StringBuffer} holding the current buffer value.
     * @deprecated Use {@link #getBuilder()} instead
     */
    @Override
    @Deprecated
    public StringBuffer getBuffer() {
        return new StringBuffer(buf);
    }

    /**
     * Flushes the stream.
     */
    @Override
    public void flush() {
        // Nothing to do
    }

    /**
     * Closing a <tt>StringWriter</tt> has no effect. The methods in this class can be called after the stream has been closed without
     * generating an <tt>IOException</tt>.
     */
    @Override
    public void close() throws IOException {
        // Nothing to do
    }

}
