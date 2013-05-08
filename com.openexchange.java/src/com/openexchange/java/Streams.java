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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;

/**
 * {@link Streams} - A utility class for streams.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Streams {

    /**
     * No initialization.
     */
    private Streams() {
        super();
    }

    /**
     * Reads the content from given reader.
     *
     * @param reader The reader
     * @return The reader's content
     * @throws IOException If an I/O error occurs
     */
    public static String reader2string(final Reader reader) throws IOException {
        if (null == reader) {
            return null;
        }
        final int buflen = 2048;
        final char[] cbuf = new char[buflen];
        final StringBuilder builder = new StringBuilder(8192);
        for (int read = reader.read(cbuf, 0, buflen); read > 0; read = reader.read(cbuf, 0, buflen)) {
            builder.append(cbuf, 0, read);
        }
        if (0 == builder.length()) {
            return null;
        }
        return builder.toString();
    }

    /**
     * Creates an appropriate <tt>ByteArrayInputStream</tt> carrying given <tt>ByteArrayOutputStream</tt>'s valid bytes.
     * <p>
     * <b>Note</b>: The byte array from specified <tt>ByteArrayOutputStream</tt> is possibly shared to <tt>ByteArrayInputStream</tt>.
     *
     * @param baos The <tt>ByteArrayOutputStream</tt> instance
     * @return The associated <tt>ByteArrayInputStream</tt> instance
     */
    public static ByteArrayInputStream asInputStream(final ByteArrayOutputStream baos) {
        if (null == baos) {
            return null;
        }
        if (baos instanceof UnsynchronizedByteArrayOutputStream) {
            return ((UnsynchronizedByteArrayOutputStream) baos).toByteArrayInputStream();
        }
        return new UnsynchronizedByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Creates an appropriate <tt>ByteArrayInputStream</tt> carrying given <tt>InputStream</tt>'s bytes.
     *
     * @param in The <tt>InputStream</tt> instance
     * @return The associated <tt>ByteArrayInputStream</tt> instance
     * @throws IOException If an I/O error occurs
     */
    public static ByteArrayInputStream asInputStream(final InputStream in) throws IOException {
        if (null == in) {
            return null;
        }
        return newByteArrayInputStream(in);
    }

    /**
     * Writes specified input stream's content to a <code>ByteArrayOutputStream</code> array.
     *
     * @param is The input stream to read from
     * @return A newly created <code>byte</code> array carrying input stream's bytes.
     * @throws IOException If an I/O error occurs
     */
    public static ByteArrayOutputStream stream2ByteArrayOutputStream(final InputStream is) throws IOException {
        if (null == is) {
            return null;
        }
        try {
            final ByteArrayOutputStream bos = newByteArrayOutputStream(4096);
            final int buflen = 2048;
            final byte[] buf = new byte[buflen];
            for (int read; (read = is.read(buf, 0, buflen)) > 0;) {
                bos.write(buf, 0, read);
            }
            return bos;
        } finally {
            close(is);
        }
    }

    /**
     * Converts specified input stream to a <code>byte</code> array.
     *
     * @param is The input stream to read from
     * @return A newly created <code>byte</code> array carrying input stream's bytes.
     * @throws IOException If an I/O error occurs
     */
    public static byte[] stream2bytes(final InputStream is) throws IOException {
        if (null == is) {
            return new byte[0];
        }
        try {
            final ByteArrayOutputStream bos = newByteArrayOutputStream(4096);
            final int buflen = 2048;
            final byte[] buf = new byte[buflen];
            for (int read; (read = is.read(buf, 0, buflen)) > 0;) {
                bos.write(buf, 0, read);
            }
            return bos.toByteArray();
        } finally {
            close(is);
        }
    }

    /**
     * Creates a new non-thread-safe {@link Reader} whose source is the specified string.
     *
     * @param s The string to read from
     * @return The reader
     */
    @SuppressWarnings("resource")
    public static Reader newStringReader(final String s) {
        return null == s ? null : new UnsynchronizedStringReader(s);
    }

    /**
     * Creates a new non-thread-safe {@link Writer} that collects its output in a string allocator, which can then be used to construct a
     * string.
     *
     * @return A new writer
     */
    public static Writer newStringWriter() {
        return new UnsynchronizedStringWriter(32);
    }

    /**
     * Creates a new non-thread-safe {@link Writer} that collects its output in a string allocator, which can then be used to construct a
     * string.
     *
     * @param initial The initial capacity
     * @return A new writer
     */
    public static Writer newStringWriter(final int initial) {
        return new UnsynchronizedStringWriter(initial);
    }

    /**
     * Creates a new non-thread-safe {@link ByteArrayOutputStream} instance with default initial capacity of <code>32</code>.
     *
     * @return A new non-thread-safe {@link ByteArrayOutputStream} instance
     */
    public static ByteArrayOutputStream newByteArrayOutputStream() {
        return new UnsynchronizedByteArrayOutputStream(32);
    }

    /**
     * Creates a new non-thread-safe {@link ByteArrayOutputStream} instance.
     *
     * @param capacity The initial capacity
     * @return A new non-thread-safe {@link ByteArrayOutputStream} instance
     */
    public static ByteArrayOutputStream newByteArrayOutputStream(final int capacity) {
        return new UnsynchronizedByteArrayOutputStream(capacity);
    }

    /**
     * Creates a new non-thread-safe {@link ByteArrayInputStream} instance carrying specified input stream's data.
     *
     * @param inputStream The input stream
     * @return A new non-thread-safe {@link ByteArrayInputStream} instance
     * @throws IOException If an I/O error occurs
     */
    public static ByteArrayInputStream newByteArrayInputStream(final InputStream inputStream) throws IOException {
        try {
            @SuppressWarnings("resource")
            final UnsynchronizedByteArrayOutputStream bos = new UnsynchronizedByteArrayOutputStream(4096);
            final int buflen = 2048;
            final byte[] buf = new byte[buflen];
            for (int read; (read = inputStream.read(buf, 0, buflen)) > 0;) {
                bos.write(buf, 0, read);
            }
            return bos.toByteArrayInputStream();
        } finally {
            close(inputStream);
        }
    }

    /**
     * Creates a new non-thread-safe {@link ByteArrayInputStream} instance carrying specified bytes.
     *
     * @param bytes The bytes
     * @return A new non-thread-safe {@link ByteArrayInputStream} instance
     */
    public static ByteArrayInputStream newByteArrayInputStream(final byte[] bytes) {
        return new UnsynchronizedByteArrayInputStream(bytes);
    }

    /**
     * Creates a new non-thread-safe {@link ByteArrayInputStream} instance carrying specified bytes.
     *
     * @param bytes The bytes
     * @param offset The offset in the buffer of the first byte to read
     * @param length The maximum number of bytes to read from the buffer
     * @return A new non-thread-safe {@link ByteArrayInputStream} instance
     */
    public static ByteArrayInputStream newByteArrayInputStream(final byte[] bytes, final int offset, final int length) {
        return new UnsynchronizedByteArrayInputStream(bytes, offset, length);
    }

    /**
     * Safely closes specified {@link Closeable} instance.
     *
     * @param toClose The {@link Closeable} instance
     */
    public static void close(final Closeable toClose) {
        if (null != toClose) {
            try {
                toClose.close();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Safely closes specified {@link Closeable} instances.
     *
     * @param closeables The {@link Closeable} instances
     */
    public static void close(final Closeable... closeables) {
        if (null != closeables) {
            for (final Closeable toClose : closeables) {
                if (null != toClose) {
                    try {
                        toClose.close();
                    } catch (final Exception e) {
                        // Ignore
                    }
                }
            }
        }
    }

    /**
     * Safely closes specified {@link Closeable} instances.
     *
     * @param closeables The {@link Closeable} instances
     */
    public static void close(final Collection<Closeable> closeables) {
        if (null != closeables) {
            for (final Closeable toClose : closeables) {
                if (null != toClose) {
                    try {
                        toClose.close();
                    } catch (final Exception e) {
                        // Ignore
                    }
                }
            }
        }
    }

    /**
     * Safely flushes specified {@link Flushable} instance.
     *
     * @param toFlush The {@link Flushable} instance
     */
    public static void flush(final Flushable toFlush) {
        if (null != toFlush) {
            try {
                toFlush.flush();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }
}
