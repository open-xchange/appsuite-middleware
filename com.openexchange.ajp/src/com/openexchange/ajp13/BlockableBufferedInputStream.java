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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import com.openexchange.concurrent.Blockable;
import com.openexchange.concurrent.Blocker;
import com.openexchange.concurrent.ConcurrentBlocker;
import com.openexchange.concurrent.NonBlockingBlocker;
import com.openexchange.java.Streams;

/**
 * {@link BlockableBufferedInputStream} - A blockable version of {@link BufferedInputStream}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class BlockableBufferedInputStream extends BufferedInputStream implements Blockable {

    /**
     * Atomic updater to provide compareAndSet for buf. This is necessary because closes can be asynchronous. We use nullness of buf[] as
     * primary indicator that this stream is closed. (The "in" field is also nulled out on close.)
     */
    private static volatile AtomicReferenceFieldUpdater<BufferedInputStream, byte[]> bufUpdater;

    private static void initBufUpdater() {
        AtomicReferenceFieldUpdater<BufferedInputStream, byte[]> tmp = bufUpdater;
        if (null == tmp) {
            synchronized (BlockableBufferedInputStream.class) {
                tmp = bufUpdater;
                if (tmp == null) {
                    bufUpdater = AtomicReferenceFieldUpdater.newUpdater(BufferedInputStream.class, byte[].class, "buf");
                }
            }
        }
    }

    private final Blocker blocker;

    /**
     * Initializes a new {@link BlockableBufferedInputStream}.
     *
     * @param in The underlying input stream.
     * @param nonBlocking Whether a non-blocking or concurrent blocker will be used
     */
    public BlockableBufferedInputStream(final InputStream in, final boolean nonBlocking) {
        super(in);
        initBufUpdater();
        blocker = nonBlocking ? new NonBlockingBlocker() : new ConcurrentBlocker();
    }

    /**
     * Initializes a new {@link BlockableBufferedInputStream}.
     *
     * @param in The underlying input stream.
     * @param size The buffer size.
     * @param nonBlocking Whether a non-blocking or concurrent blocker will be used
     */
    public BlockableBufferedInputStream(final InputStream in, final int size, final boolean nonBlocking) {
        super(in, size);
        blocker = nonBlocking ? new NonBlockingBlocker() : new ConcurrentBlocker();
    }

    /**
     * Clears the input buffer.
     *
     * @throws IOException If an I/O error occurs
     */
    public void clearBuffer() throws IOException {
        /*
         * Suck input stream dry
         */
        {
            final InputStream in = this.in;
            final int available = in.available();
            if (available > 0) {
                final byte tmp[] = new byte[available];
                in.read(tmp, 0, available);
            }
        }
        /*
         * Clear buffer
         */
        markpos = -1;
        marklimit = 0;
        pos = 0;
        count = 0;
        final byte nbuf[] = new byte[8192];
        final byte[] buffer = getBufIfOpen();
        if (!bufUpdater.compareAndSet(this, buffer, nbuf)) {
            // Can't replace buf if there was an async close.
            // Note: This would need to be changed if fill()
            // is ever made accessible to multiple threads.
            // But for now, the only way CAS can fail is via close.
            // assert buf == null;
            throw new IOException("Stream closed");
        }
    }

    @Override
    public void block() {
        blocker.block();
    }

    @Override
    public void unblock() {
        blocker.unblock();
    }

    /**
     * Check to make sure that underlying input stream has not been nulled out due to close; if not return it;
     */
    private InputStream getInIfOpen() throws IOException {
        final InputStream input = in;
        if (input == null) {
            throw new IOException("Stream closed");
        }
        return input;
    }

    /**
     * Check to make sure that buffer has not been nulled out due to close; if not return it;
     */
    private byte[] getBufIfOpen() throws IOException {
        final byte[] buffer = buf;
        if (buffer == null) {
            throw new IOException("Stream closed");
        }
        return buffer;
    }

    /**
     * Fills the buffer with more data, taking into account shuffling and other tricks for dealing with marks. Assumes that it is being
     * called by a synchronized method. This method also assumes that all data has already been read in, hence pos > count.
     */
    private void fill() throws IOException {
        byte[] buffer = getBufIfOpen();
        if (markpos < 0) {
            pos = 0; /* no mark: throw away the buffer */
        } else if (pos >= buffer.length) {
            if (markpos > 0) { /* can throw away early part of the buffer */
                final int sz = pos - markpos;
                System.arraycopy(buffer, markpos, buffer, 0, sz);
                pos = sz;
                markpos = 0;
            } else if (buffer.length >= marklimit) {
                markpos = -1; /* buffer got too big, invalidate mark */
                pos = 0; /* drop buffer contents */
            } else { /* grow buffer */
                int nsz = pos << 1;
                if (nsz > marklimit) {
                    nsz = marklimit;
                }
                final byte nbuf[] = new byte[nsz];
                System.arraycopy(buffer, 0, nbuf, 0, pos);
                if (!bufUpdater.compareAndSet(this, buffer, nbuf)) {
                    // Can't replace buf if there was an async close.
                    // Note: This would need to be changed if fill()
                    // is ever made accessible to multiple threads.
                    // But for now, the only way CAS can fail is via close.
                    // assert buf == null;
                    throw new IOException("Stream closed");
                }
                buffer = nbuf;
            }
        }
        count = pos;
        final int n = getInIfOpen().read(buffer, pos, buffer.length - pos);
        if (n > 0) {
            count = n + pos;
        }
    }

    /**
     * See the general contract of the <code>read</code> method of <code>InputStream</code>.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the stream is reached.
     * @exception IOException if an I/O error occurs.
     * @see java.io.FilterInputStream#in
     */
    @Override
    public int read() throws IOException {
        blocker.acquire();
        try {
            if (pos >= count) {
                fill();
                if (pos >= count) {
                    return -1;
                }
            }
            return getBufIfOpen()[pos++] & 0xff;
        } finally {
            blocker.release();
        }
    }

    /**
     * Read characters into a portion of an array, reading from the underlying stream at most once if necessary.
     */
    private int read1(final byte[] b, final int off, final int len) throws IOException {
        int avail = count - pos;
        if (avail <= 0) {
            /*
             * If the requested length is at least as large as the buffer, and if there is no mark/reset activity, do not bother to copy the
             * bytes into the local buffer. In this way buffered streams will cascade harmlessly.
             */
            if (len >= getBufIfOpen().length && markpos < 0) {
                return getInIfOpen().read(b, off, len);
            }
            fill();
            avail = count - pos;
            if (avail <= 0) {
                return -1;
            }
        }
        final int cnt = (avail < len) ? avail : len;
        System.arraycopy(getBufIfOpen(), pos, b, off, cnt);
        pos += cnt;
        return cnt;
    }

    /**
     * Reads bytes from this byte-input stream into the specified byte array, starting at the given offset.
     * <p>
     * This method implements the general contract of the corresponding <code>{@link InputStream#read(byte[], int, int) read}</code> method
     * of the <code>{@link InputStream}</code> class. As an additional convenience, it attempts to read as many bytes as possible by
     * repeatedly invoking the <code>read</code> method of the underlying stream. This iterated <code>read</code> continues until one of the
     * following conditions becomes true:
     * <ul>
     * <li>The specified number of bytes have been read,
     * <li>The <code>read</code> method of the underlying stream returns <code>-1</code>, indicating end-of-file, or
     * <li>The <code>available</code> method of the underlying stream returns zero, indicating that further input requests would block.
     * </ul>
     * If the first <code>read</code> on the underlying stream returns <code>-1</code> to indicate end-of-file then this method returns
     * <code>-1</code>. Otherwise this method returns the number of bytes actually read.
     * <p>
     * Subclasses of this class are encouraged, but not required, to attempt to read as many bytes as possible in the same fashion.
     *
     * @param b destination buffer.
     * @param off offset at which to start storing bytes.
     * @param len maximum number of bytes to read.
     * @return the number of bytes read, or <code>-1</code> if the end of the stream has been reached.
     * @exception IOException if an I/O error occurs.
     */
    @Override
    public int read(final byte b[], final int off, final int len) throws IOException {
        blocker.acquire();
        try {
            getBufIfOpen(); // Check for closed stream
            if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            int n = 0;
            for (;;) {
                final int nread = read1(b, off + n, len - n);
                if (nread <= 0) {
                    return (n == 0) ? nread : n;
                }
                n += nread;
                if (n >= len) {
                    return n;
                }
                // if not closed but no bytes available, return
                final InputStream input = in;
                if (input != null && input.available() <= 0) {
                    return n;
                }
            }
        } finally {
            blocker.release();
        }
    }

    /**
     * See the general contract of the <code>skip</code> method of <code>InputStream</code>.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @exception IOException if an I/O error occurs.
     */
    @Override
    public long skip(final long n) throws IOException {
        blocker.acquire();
        try {
            getBufIfOpen(); // Check for closed stream
            if (n <= 0) {
                return 0;
            }
            long avail = count - pos;

            if (avail <= 0) {
                // If no mark position set then don't keep in buffer
                if (markpos < 0) {
                    return getInIfOpen().skip(n);
                }

                // Fill in buffer to save bytes for reset
                fill();
                avail = count - pos;
                if (avail <= 0) {
                    return 0;
                }
            }

            final long skipped = (avail < n) ? avail : n;
            pos += skipped;
            return skipped;
        } finally {
            blocker.release();
        }
    }

    /**
     * Returns the number of bytes that can be read from this input stream without blocking.
     * <p>
     * The <code>available</code> method of <code>BufferedInputStream</code> returns the sum of the number of bytes remaining to be read in
     * the buffer (<code>count&nbsp;- pos</code>) and the result of calling the <code>available</code> method of the underlying input
     * stream.
     *
     * @return the number of bytes that can be read from this input stream without blocking.
     * @exception IOException if an I/O error occurs.
     * @see java.io.FilterInputStream#in
     */
    @Override
    public int available() throws IOException {
        blocker.acquire();
        try {
            return getInIfOpen().available() + (count - pos);
        } finally {
            blocker.release();
        }
    }

    /**
     * See the general contract of the <code>mark</code> method of <code>InputStream</code>.
     *
     * @param readlimit the maximum limit of bytes that can be read before the mark position becomes invalid.
     * @see java.io.BufferedInputStream#reset()
     */
    @Override
    public void mark(final int readlimit) {
        blocker.acquire();
        try {
            marklimit = readlimit;
            markpos = pos;
        } finally {
            blocker.release();
        }
    }

    /**
     * See the general contract of the <code>reset</code> method of <code>InputStream</code>.
     * <p>
     * If <code>markpos</code> is <code>-1</code> (no mark has been set or the mark has been invalidated), an <code>IOException</code> is
     * thrown. Otherwise, <code>pos</code> is set equal to <code>markpos</code>.
     *
     * @exception IOException if this stream has not been marked or if the mark has been invalidated.
     * @see java.io.BufferedInputStream#mark(int)
     */
    @Override
    public void reset() throws IOException {
        blocker.acquire();
        try {
            getBufIfOpen(); // Cause exception if closed
            if (markpos < 0) {
                throw new IOException("Resetting to invalid mark");
            }
            pos = markpos;
        } finally {
            blocker.release();
        }
    }

    /**
     * Tests if this input stream supports the <code>mark</code> and <code>reset</code> methods. The <code>markSupported</code> method of
     * <code>BufferedInputStream</code> returns <code>true</code>.
     *
     * @return a <code>boolean</code> indicating if this stream type supports the <code>mark</code> and <code>reset</code> methods.
     * @see java.io.InputStream#mark(int)
     * @see java.io.InputStream#reset()
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Closes this input stream and releases any system resources associated with the stream.
     *
     * @exception IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        blocker.acquire();
        try {
            byte[] buffer;
            while ((buffer = buf) != null) {
                if (bufUpdater.compareAndSet(this, buffer, null)) {
                    final InputStream input = in;
                    in = null;
                    Streams.close(input);
                    return;
                }
                // Else retry in case a new buf was CASed in fill()
            }
        } finally {
            blocker.release();
        }
    }

}
