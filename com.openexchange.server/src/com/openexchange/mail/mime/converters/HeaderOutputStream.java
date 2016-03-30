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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.mail.mime.converters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * {@link HeaderOutputStream} - Reads until two subsequent CR?LF sequences are detected
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HeaderOutputStream extends ByteArrayOutputStream {

    private int lfCount;

    private boolean discard;

    /**
     * Creates a new byte array output stream. The buffer capacity is initially 32 bytes, though its size increases if necessary.
     */
    public HeaderOutputStream() {
        this(32);
        lfCount = 0;
        discard = false;
    }

    /**
     * Creates a new byte array output stream, with a buffer capacity of the specified size, in bytes.
     *
     * @param size The initial size.
     * @exception IllegalArgumentException If size is negative.
     */
    public HeaderOutputStream(final int size) {
        super(size);
        lfCount = 0;
        discard = false;
    }

    /**
     * Writes the specified byte to this byte array output stream.
     *
     * @param b The byte to be written.
     */
    @Override
    public void write(final int b) {
        if (discard) {
            return;
        }
        final int newcount = count + 1;
        if (newcount > buf.length) {
            final byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
        buf[count] = (byte) b;
        count = newcount;
        // Check byte
        if ('\n' == b) {
            if (++lfCount >= 2) {
                discard = true;
            }
        } else if ('\r' != b) {
            lfCount = 0;
        }
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array starting at offset <code>off</code> to this byte array output stream.
     *
     * @param b The data.
     * @param off The start offset in the data.
     * @param len The number of bytes to write.
     */
    @Override
    public void write(final byte b[], final int off, final int len) {
        if (off < 0 || (off > b.length) || (len < 0)) {
            throw new IndexOutOfBoundsException();
        }
        final int bLen = off + len;
        if (bLen > b.length || (bLen < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0 || discard) {
            return;
        }
        int pos = -1;
        for (int i = off; pos < 0 && i < bLen; i++) {
            // Check byte
            if ('\n' == b[i]) {
                if (++lfCount >= 2) {
                    discard = true;
                    pos = i;
                }
            } else if ('\r' != b[i]) {
                lfCount = 0;
            }
        }
        final int nlen = pos < 0 ? len : pos - off + 1;
        final int newcount = count + nlen;
        if (newcount > buf.length) {
            final byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
        System.arraycopy(b, off, buf, count, nlen);
        count = newcount;
    }

    /**
     * Writes the complete contents of this byte array output stream to the specified output stream argument, as if by calling the output
     * stream's write method using <code>out.write(buf, 0, count)</code>.
     *
     * @param out The output stream to which to write the data.
     * @exception IOException If an I/O error occurs.
     */
    @Override
    public void writeTo(final OutputStream out) throws IOException {
        if (null != out) {
            out.write(buf, 0, count);
        }
    }

    /**
     * Resets the <code>count</code> field of this byte array output stream to zero, so that all currently accumulated output in the output
     * stream is discarded. The output stream can be used again, reusing the already allocated buffer space.
     */
    @Override
    public void reset() {
        count = 0;
    }

    /**
     * Creates a newly allocated byte array. Its size is the current size of this output stream and the valid contents of the buffer have
     * been copied into it.
     *
     * @return The current contents of this output stream, as a byte array.
     */
    @Override
    public byte toByteArray()[] {
        final byte newbuf[] = new byte[count];
        System.arraycopy(buf, 0, newbuf, 0, count);
        return newbuf;
    }

    /**
     * Creates a newly allocated byte array. Its size is specified <code>size</code> and the valid contents starting from specified offset
     * <code>off</code> are going to be copied into it.
     *
     * @param off The offset in valid contents
     * @param size The demanded size
     * @return The current contents of this output stream, as a byte array.
     */
    public byte toByteArray(final int off, final int size)[] {
        if ((off < 0) || (off > count) || (size < 0) || ((off + size) > count) || ((off + size) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (size == 0) {
            return new byte[0];
        }
        final byte newbuf[] = new byte[size];
        System.arraycopy(buf, off, newbuf, 0, size);
        return newbuf;
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return The value of the <code>count</code> field, which is the number of valid bytes in this output stream.
     */
    @Override
    public int size() {
        return count;
    }

    /**
     * Converts the buffer's contents into a string, translating bytes into characters according to the platform's default character
     * encoding.
     *
     * @return A string translated from the buffer's contents.
     */
    @Override
    public String toString() {
        return new String(buf, 0, count);
    }

    /**
     * Converts the buffer's contents into a string, translating bytes into characters according to the specified character encoding.
     *
     * @param enc The character-encoding name.
     * @return A string translated from the buffer's contents.
     * @throws UnsupportedEncodingException If the named encoding is not supported.
     */
    @Override
    public String toString(final String enc) throws UnsupportedEncodingException {
        return new String(buf, 0, count, enc);
    }

    /**
     * Creates a newly allocated string. Its size is the current size of the output stream and the valid contents of the buffer have been
     * copied into it. Each character <i>c</i> in the resulting string is constructed from the corresponding element <i>b</i> in the byte
     * array such that: <blockquote>
     *
     * <pre>
     * c == (char) (((hibyte &amp; 0xff) &lt;&lt; 8) | (b &amp; 0xff))
     * </pre>
     *
     * </blockquote>
     *
     * @deprecated This method does not properly convert bytes into characters. As of JDK&nbsp;1.1, the preferred way to do this is via the
     *             <code>toString(String enc)</code> method, which takes an encoding-name argument, or the <code>toString()</code> method,
     *             which uses the platform's default character encoding.
     * @param hibyte The high byte of each resulting Unicode character.
     * @return The current contents of the output stream, as a string.
     */
    @Override
    @Deprecated
    public String toString(final int hibyte) {
        return new String(buf, hibyte, 0, count);
    }

    /**
     * Closing a byte array output stream has no effect. The methods in this class can be called after the stream has been closed without
     * generating an <tt>IOException</tt>.
     */
    @Override
    public void close() throws IOException {
        // Nothing to do
    }

}
