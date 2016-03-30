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

package com.openexchange.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * {@link ByteBuffers} - Utility methods for {@link ByteBuffer} instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ByteBuffers {

    /**
     * No instantiation.
     */
    private ByteBuffers() {
        super();
    }

    /**
     * Creates a new output stream for specified {@link ByteBuffer byte buffer}.
     *
     * @param buf The byte buffer from which the output stream shall be created
     * @return The newly created output stream
     */
    public static OutputStream newOutputStream(final ByteBuffer buf) {
        return new OutputStream() {

            @Override
            public synchronized void write(final int b) throws IOException {
                buf.put((byte) b);
            }

            @Override
            public synchronized void write(final byte[] bytes, final int off, final int len) throws IOException {
                buf.put(bytes, off, len);
            }
        };
    }

    /**
     * Creates a new unsynchronized output stream for specified {@link ByteBuffer byte buffer}.
     *
     * @param buf The byte buffer from which the unsynchronized output stream shall be created
     * @return The newly created unsynchronized output stream
     */
    public static OutputStream newUnsynchronizedOutputStream(final ByteBuffer buf) {
        return new OutputStream() {

            @Override
            public void write(final int b) throws IOException {
                buf.put((byte) b);
            }

            @Override
            public void write(final byte[] bytes, final int off, final int len) throws IOException {
                buf.put(bytes, off, len);
            }
        };
    }

    /**
     * Gets a new input stream for specified {@link ByteBuffer byte buffer}.
     *
     * @param buf The byte buffer for which the input stream shall be created
     * @return The newly created input stream
     */
    public static InputStream newInputStream(final ByteBuffer buf) {
        return new InputStream() {

            @Override
            public synchronized int read() throws IOException {
                if (!buf.hasRemaining()) {
                    return -1;
                }
                return buf.get();
            }

            @Override
            public synchronized int read(final byte[] bytes, final int off, final int len) throws IOException {
                final int l = Math.min(len, buf.remaining());
                buf.get(bytes, off, l);
                return l;
            }
        };
    }

    /**
     * Gets a new unsynchronized input stream for specified {@link ByteBuffer byte buffer}.
     *
     * @param buf The byte buffer for which the unsynchronized input stream shall be created
     * @return The newly created unsynchronized input stream
     */
    public static InputStream newUnsynchronizedInputStream(final ByteBuffer buf) {
        return new InputStream() {

            @Override
            public int read() throws IOException {
                if (!buf.hasRemaining()) {
                    return -1;
                }
                return buf.get();
            }

            @Override
            public int read(final byte[] bytes, final int off, final int len) throws IOException {
                final int l = Math.min(len, buf.remaining());
                buf.get(bytes, off, l);
                return l;
            }
        };
    }

}
