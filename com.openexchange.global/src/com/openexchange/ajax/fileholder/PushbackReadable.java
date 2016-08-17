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

package com.openexchange.ajax.fileholder;

import java.io.IOException;
import com.openexchange.ajax.fileholder.Readable;

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
