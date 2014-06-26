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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajp13;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import com.openexchange.concurrent.Blocker;
import com.openexchange.concurrent.ConcurrentBlocker;
import com.openexchange.concurrent.NonBlockingBlocker;

/**
 * {@link BlockableBufferedOutputStream} - A blockable version of {@link BufferedOutputStream} which keeps track of last write access time
 * stamp.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class BlockableBufferedOutputStream extends BufferedOutputStream implements Blocker {

    private final Blocker blocker;

    private volatile long lastAccessed;

    /**
     * Initializes a new {@link BlockableBufferedOutputStream}.
     *
     * @param out The underlying output stream.
     * @param nonBlocking Whether a non-blocking or concurrent blocker will be used
     */
    public BlockableBufferedOutputStream(final OutputStream out, final boolean nonBlocking) {
        super(out);
        blocker = nonBlocking ? new NonBlockingBlocker() : new ConcurrentBlocker();
    }

    /**
     * Initializes a new {@link BlockableBufferedOutputStream}.
     *
     * @param out The underlying output stream.
     * @param size The buffer size.
     * @param nonBlocking Whether a non-blocking or concurrent blocker will be used
     */
    public BlockableBufferedOutputStream(final OutputStream out, final int size, final boolean nonBlocking) {
        super(out, size);
        blocker = nonBlocking ? new NonBlockingBlocker() : new ConcurrentBlocker();
    }

    @Override
    public void block() {
        blocker.block();
    }

    @Override
    public void unblock() {
        blocker.unblock();
    }

    @Override
    public void acquire() {
        blocker.acquire();
    }

    @Override
    public void release() {
        blocker.release();
    }

    /**
     * Drops the buffer.
     */
    public void dropBuffer() {
        count = 0;
        Arrays.fill(buf, (byte) 0);
    }

    /**
     * Flush the internal buffer
     *
     * @throws IOException
     */
    private void flushBuffer() throws IOException {
        if (count > 0) {
            out.write(buf, 0, count);
            count = 0;
        }
    }

    /**
     * Writes the specified byte to this buffered output stream.
     *
     * @param b the byte to be written.
     * @exception IOException if an I/O error occurs.
     */
    @Override
    public void write(final int b) throws IOException {
        if (count >= buf.length) {
            flushBuffer();
        }
        buf[count++] = (byte) b;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array starting at offset <code>off</code> to this buffered output stream without touching dirty flag.
     * <p>
     * Ordinarily this method stores bytes from the given array into this stream's buffer, flushing the buffer to the underlying output
     * stream as needed. If the requested length is at least as large as this stream's buffer, however, then this method will flush the
     * buffer and write the bytes directly to the underlying output stream. Thus redundant <code>BufferedOutputStream</code>s will not copy
     * data unnecessarily.
     *
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @exception IOException if an I/O error occurs.
     */
    @Override
    public void write(final byte b[], final int off, final int len) throws IOException {
        if (len >= buf.length) {
            /*
             * If the request length exceeds the size of the output buffer, flush the output buffer and then write the data directly. In
             * this way buffered streams will cascade harmlessly.
             */
            flushBuffer();
            out.write(b, off, len);
            return;
        }
        if (len > buf.length - count) {
            flushBuffer();
        }
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    /**
     * Flushes this buffered output stream. This forces any buffered output bytes to be written out to the underlying output stream.
     *
     * @exception IOException if an I/O error occurs.
     * @see java.io.FilterOutputStream#out
     */
    @Override
    public void flush() throws IOException {
        flushBuffer();
        out.flush();
        lastAccessed = System.currentTimeMillis();
    }

    /**
     * Sets the last-accessed time stamp.
     *
     * @param lastAccessed The time stamp to set
     */
    public void setLastAccessed(final long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    /**
     * Gets the last-accessed time stamp of this output stream.
     * <p>
     * The last-accessed time stamp reflects when data was lastly flushed.
     *
     * @return The last-accessed time stamp.
     */
    public long getLastAccessed() {
        return lastAccessed;
    }

}
