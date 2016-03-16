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

package com.openexchange.java;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * {@link UnsynchronizedPushbackReader} - Copy of {@link java.io.PushbackReader}. but without synchronization.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnsynchronizedPushbackReader extends FilterReader {

    /** Pushback buffer */
    private char[] buf;

    /** Current position in buffer */
    private int pos;

    /**
     * Creates a new pushback reader with a pushback buffer of the given size.
     *
     * @param in The reader from which characters will be read
     * @param size The size of the pushback buffer
     * @exception IllegalArgumentException if size is <= 0
     */
    public UnsynchronizedPushbackReader(Reader in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        this.buf = new char[size];
        this.pos = size;
    }

    /**
     * Creates a new pushback reader with a one-character pushback buffer.
     *
     * @param in The reader from which characters will be read
     */
    public UnsynchronizedPushbackReader(Reader in) {
        this(in, 1);
    }

    /** Checks to make sure that the stream has not been closed. */
    private void ensureOpen() throws IOException {
        if (buf == null) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * Reads a single character.
     *
     * @return The character read, or -1 if the end of the stream has been reached
     * @exception IOException If an I/O error occurs
     */
    @Override
    public int read() throws IOException {
        ensureOpen();
        if (pos < buf.length) {
            return buf[pos++];
        }
        return super.read();
    }

    /**
     * Reads characters into a portion of an array.
     *
     * @param cbuf Destination buffer
     * @param off Offset at which to start writing characters
     * @param len Maximum number of characters to read
     * @return The number of characters read, or -1 if the end of the stream has been reached
     * @exception IOException If an I/O error occurs
     */
    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
        ensureOpen();
        if (len <= 0) {
            if (len < 0) {
                throw new IndexOutOfBoundsException();
            } else if ((off < 0) || (off > cbuf.length)) {
                throw new IndexOutOfBoundsException();
            }
            return 0;
        }
        int avail = buf.length - pos;
        if (avail > 0) {
            if (len < avail) {
                avail = len;
            }
            System.arraycopy(buf, pos, cbuf, off, avail);
            pos += avail;
            off += avail;
            len -= avail;
        }
        if (len > 0) {
            len = super.read(cbuf, off, len);
            if (len == -1) {
                return (avail == 0) ? -1 : avail;
            }
            return avail + len;
        }
        return avail;
    }

    /**
     * Pushes back a single character by copying it to the front of the pushback buffer. After this method returns, the next character to be
     * read will have the value <code>(char)c</code>.
     *
     * @param c The int value representing a character to be pushed back
     * @exception IOException If the pushback buffer is full, or if some other I/O error occurs
     */
    public void unread(int c) throws IOException {
        ensureOpen();
        if (pos == 0) {
            throw new IOException("Pushback buffer overflow");
        }
        buf[--pos] = (char) c;
    }

    /**
     * Pushes back a portion of an array of characters by copying it to the front of the pushback buffer. After this method returns, the
     * next character to be read will have the value <code>cbuf[off]</code>, the character after that will have the value
     * <code>cbuf[off+1]</code>, and so forth.
     *
     * @param cbuf Character array
     * @param off Offset of first character to push back
     * @param len Number of characters to push back
     * @exception IOException If there is insufficient room in the pushback buffer, or if some other I/O error occurs
     */
    public void unread(char cbuf[], int off, int len) throws IOException {
        ensureOpen();
        if (len > pos) {
            throw new IOException("Pushback buffer overflow");
        }
        pos -= len;
        System.arraycopy(cbuf, off, buf, pos, len);
    }

    /**
     * Pushes back an array of characters by copying it to the front of the pushback buffer. After this method returns, the next character
     * to be read will have the value <code>cbuf[0]</code>, the character after that will have the value <code>cbuf[1]</code>, and so forth.
     *
     * @param cbuf Character array to push back
     * @exception IOException If there is insufficient room in the pushback buffer, or if some other I/O error occurs
     */
    public void unread(char cbuf[]) throws IOException {
        unread(cbuf, 0, cbuf.length);
    }

    /**
     * Tells whether this stream is ready to be read.
     *
     * @exception IOException If an I/O error occurs
     */
    @Override
    public boolean ready() throws IOException {
        ensureOpen();
        return (pos < buf.length) || super.ready();
    }

    /**
     * Marks the present position in the stream. The <code>mark</code> for class <code>PushbackReader</code> always throws an exception.
     *
     * @exception IOException Always, since mark is not supported
     */
    @Override
    public void mark(int readAheadLimit) throws IOException {
        throw new IOException("mark/reset not supported");
    }

    /**
     * Resets the stream. The <code>reset</code> method of <code>PushbackReader</code> always throws an exception.
     *
     * @exception IOException Always, since reset is not supported
     */
    @Override
    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    /**
     * Tells whether this stream supports the mark() operation, which it does not.
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Closes the stream and releases any system resources associated with it. Once the stream has been closed, further read(), unread(),
     * ready(), or skip() invocations will throw an IOException. Closing a previously closed stream has no effect.
     *
     * @exception IOException If an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        super.close();
        buf = null;
    }

    /**
     * Skips characters. This method will block until some characters are available, an I/O error occurs, or the end of the stream is
     * reached.
     *
     * @param n The number of characters to skip
     * @return The number of characters actually skipped
     * @exception IllegalArgumentException If <code>n</code> is negative.
     * @exception IOException If an I/O error occurs
     */
    @Override
    public long skip(long n) throws IOException {
        if (n < 0L) {
            throw new IllegalArgumentException("skip value is negative");
        }
        ensureOpen();
        int avail = buf.length - pos;
        if (avail > 0) {
            if (n <= avail) {
                pos += n;
                return n;
            }
            pos = buf.length;
            n -= avail;
        }
        return avail + super.skip(n);
    }

}
