/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.tools.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * <p>
 * UnsynchronizedByteArrayOutputStream - an implementation of
 * <code>ByteArrayOutputStream</code> that does not use synchronized methods
 * </p>
 * 
 * <p>
 * This class implements an output stream in which the data is written into a
 * byte array. The buffer automatically grows as data is written to it. The data
 * can be retrieved using <code>toByteArray()</code> and
 * <code>toString()</code>.
 * <p>
 * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in this
 * class can be called after the stream has been closed without generating an
 * <tt>IOException</tt>.
 * </p>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class UnsynchronizedByteArrayOutputStream extends OutputStream {

	/**
	 * The buffer where data is stored.
	 */
	private byte buf[];

	/**
	 * The number of valid bytes in the buffer.
	 */
	private int count;

	/**
	 * Creates a new byte array output stream. The buffer capacity is initially
	 * 32 bytes, though its size increases if necessary.
	 */
	public UnsynchronizedByteArrayOutputStream() {
		this(32);
	}

	/**
	 * Creates a new byte array output stream, with a buffer capacity of the
	 * specified size, in bytes.
	 * 
	 * @param size
	 *            the initial size.
	 * @exception IllegalArgumentException
	 *                if size is negative.
	 */
	public UnsynchronizedByteArrayOutputStream(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Negative initial size: " + size);
		}
		buf = new byte[size];
	}

	/**
	 * Writes the specified byte to this byte array output stream.
	 * 
	 * @param b
	 *            the byte to be written.
	 */
	@Override
	public void write(final int b) {
		final int newcount = count + 1;
		if (newcount > buf.length) {
			final byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
			System.arraycopy(buf, 0, newbuf, 0, count);
			buf = newbuf;
		}
		buf[count] = (byte) b;
		count = newcount;
	}

	/**
	 * Writes <code>len</code> bytes from the specified byte array starting at
	 * offset <code>off</code> to this byte array output stream.
	 * 
	 * @param b
	 *            the data.
	 * @param off
	 *            the start offset in the data.
	 * @param len
	 *            the number of bytes to write.
	 */
	@Override
	public void write(final byte b[], final int off, final int len) {
		if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		final int newcount = count + len;
		if (newcount > buf.length) {
			final byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
			System.arraycopy(buf, 0, newbuf, 0, count);
			buf = newbuf;
		}
		System.arraycopy(b, off, buf, count, len);
		count = newcount;
	}

	/**
	 * Writes the complete contents of this byte array output stream to the
	 * specified output stream argument, as if by calling the output stream's
	 * write method using <code>out.write(buf, 0, count)</code>.
	 * 
	 * @param out
	 *            the output stream to which to write the data.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	public void writeTo(final OutputStream out) throws IOException {
		out.write(buf, 0, count);
	}

	/**
	 * Resets the <code>count</code> field of this byte array output stream to
	 * zero, so that all currently accumulated output in the output stream is
	 * discarded. The output stream can be used again, reusing the already
	 * allocated buffer space.
	 * 
	 * @see java.io.ByteArrayInputStream#count
	 */
	public void reset() {
		count = 0;
	}

	/**
	 * Creates a newly allocated byte array. Its size is the current size of
	 * this output stream and the valid contents of the buffer have been copied
	 * into it.
	 * 
	 * @return the current contents of this output stream, as a byte array.
	 * @see java.io.ByteArrayOutputStream#size()
	 */
	public byte toByteArray()[] {
		final byte newbuf[] = new byte[count];
		System.arraycopy(buf, 0, newbuf, 0, count);
		return newbuf;
	}

	/**
	 * Returns the current size of the buffer.
	 * 
	 * @return the value of the <code>count</code> field, which is the number
	 *         of valid bytes in this output stream.
	 * @see java.io.ByteArrayOutputStream#count
	 */
	public int size() {
		return count;
	}

	/**
	 * Converts the buffer's contents into a string, translating bytes into
	 * characters according to the platform's default character encoding.
	 * 
	 * @return String translated from the buffer's contents.
	 * @since JDK1.1
	 */
	@Override
	public String toString() {
		return new String(buf, 0, count);
	}

	/**
	 * Converts the buffer's contents into a string, translating bytes into
	 * characters according to the specified character encoding.
	 * 
	 * @param enc
	 *            a character-encoding name.
	 * @return String translated from the buffer's contents.
	 * @throws UnsupportedEncodingException
	 *             If the named encoding is not supported.
	 * @since JDK1.1
	 */
	public String toString(final String enc) throws UnsupportedEncodingException {
		return new String(buf, 0, count, enc);
	}

	/**
	 * Creates a newly allocated string. Its size is the current size of the
	 * output stream and the valid contents of the buffer have been copied into
	 * it. Each character <i>c</i> in the resulting string is constructed from
	 * the corresponding element <i>b</i> in the byte array such that:
	 * <blockquote>
	 * 
	 * <pre>
	 * c == (char) (((hibyte &amp; 0xff) &lt;&lt; 8) | (b &amp; 0xff))
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @deprecated This method does not properly convert bytes into characters.
	 *             As of JDK&nbsp;1.1, the preferred way to do this is via the
	 *             <code>toString(String enc)</code> method, which takes an
	 *             encoding-name argument, or the <code>toString()</code>
	 *             method, which uses the platform's default character encoding.
	 * 
	 * @param hibyte
	 *            the high byte of each resulting Unicode character.
	 * @return the current contents of the output stream, as a string.
	 * @see java.io.ByteArrayOutputStream#size()
	 * @see java.io.ByteArrayOutputStream#toString(String)
	 * @see java.io.ByteArrayOutputStream#toString()
	 */
	@Deprecated
	public String toString(final int hibyte) {
		return new String(buf, hibyte, 0, count);
	}

	/**
	 * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in
	 * this class can be called after the stream has been closed without
	 * generating an <tt>IOException</tt>.
	 * <p>
	 * 
	 */
	@Override
	public void close() throws IOException {
	}

}
