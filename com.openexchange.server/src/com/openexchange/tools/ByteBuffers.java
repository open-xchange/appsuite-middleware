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
