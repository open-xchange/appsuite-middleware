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

package com.openexchange.ajax.fileholder;

import java.io.IOException;

/**
 * {@link PushbackReadable} - Adds the functionality to another {@link Readable}, namely the ability to "push back" or "unread" bytes.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PushbackReadable implements Readable {

    private Readable readable;
    private byte[] buf;
    private int pos;

    /**
     * Initializes a new {@link PushbackReadable}.
     */
    public PushbackReadable(Readable readable) {
        this(readable, 1);
    }

    /**
     * Initializes a new {@link PushbackReadable}.
     */
    public PushbackReadable(Readable readable, int size) {
        super();
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        this.readable = readable;
        this.buf = new byte[size];
        this.pos = size;
    }

    /**
     * Check to make sure that this stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (readable == null) {
            throw new IOException("Readable closed");
        }
    }

    @Override
    public void close() throws IOException {
        if (readable == null) {
            return;
        }
        readable.close();
        readable = null;
        buf = null;
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        ensureOpen();
        if (b == null) {
            throw new NullPointerException();
        } else if (offset < 0 || length < 0 || length > b.length - offset) {
            throw new IndexOutOfBoundsException();
        } else if (length == 0) {
            return 0;
        }

        int off = offset;
        int len = length;
        int avail = buf.length - pos;
        if (avail > 0) {
            if (len < avail) {
                avail = len;
            }
            System.arraycopy(buf, pos, b, off, avail);
            pos += avail;
            off += avail;
            len -= avail;
        }
        if (len > 0) {
            len = readable.read(b, off, len);
            if (len == -1) {
                return avail == 0 ? -1 : avail;
            }
            return avail + len;
        }
        return avail;
    }

    /**
     * Pushes back a byte by copying it to the front of the pushback buffer.
     * After this method returns, the next byte to be read will have the value
     * <code>(byte)b</code>.
     *
     * @param b The <code>int</code> value whose low-order byte is to be pushed back.
     * @throws IOException If there is not enough room in the pushback
     *             buffer for the byte, or this input stream has been closed by
     *             invoking its {@link #close()} method.
     */
    public void unread(int b) throws IOException {
        ensureOpen();
        if (pos == 0) {
            throw new IOException("Push back buffer is full");
        }
        buf[--pos] = (byte) b;
    }

    /**
     * Pushes back a portion of an array of bytes by copying it to the front
     * of the pushback buffer. After this method returns, the next byte to be
     * read will have the value <code>b[off]</code>, the byte after that will
     * have the value <code>b[off+1]</code>, and so forth.
     *
     * @param b The byte array to push back.
     * @param off The start offset of the data.
     * @param len The number of bytes to push back.
     * @throws IOException If there is not enough room in the pushback
     *             buffer for the specified number of bytes,
     *             or this input stream has been closed by
     *             invoking its {@link #close()} method.
     */
    public void unread(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (len > pos) {
            throw new IOException("Push back buffer is full");
        }
        pos -= len;
        System.arraycopy(b, off, buf, pos, len);
    }

    /**
     * Pushes back an array of bytes by copying it to the front of the
     * pushback buffer. After this method returns, the next byte to be read
     * will have the value <code>b[0]</code>, the byte after that will have the
     * value <code>b[1]</code>, and so forth.
     *
     * @param b The byte array to push back
     * @throws IOException If there is not enough room in the pushback
     *             buffer for the specified number of bytes,
     *             or this input stream has been closed by
     *             invoking its {@link #close()} method.
     */
    public void unread(byte[] b) throws IOException {
        unread(b, 0, b.length);
    }

}
