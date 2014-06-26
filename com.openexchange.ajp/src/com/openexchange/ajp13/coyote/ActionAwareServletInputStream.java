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

package com.openexchange.ajp13.coyote;

import java.io.IOException;
import javax.servlet.ServletInputStream;
import com.openexchange.ajp13.coyote.util.ByteChunk;

/**
 * {@link ActionAwareServletInputStream}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ActionAwareServletInputStream extends ServletInputStream {

    private final InputBuffer inputBuffer;

    private final HttpServletRequestImpl request;

    private final ByteChunk byteChunk;

    /**
     * Initializes a new {@link ActionAwareServletInputStream}.
     */
    public ActionAwareServletInputStream(final InputBuffer inputBuffer, final HttpServletRequestImpl request) {
        super();
        this.inputBuffer = inputBuffer;
        this.request = request;
        byteChunk = new ByteChunk(8192);
    }

    @Override
    public void close() throws IOException {
        // Nothing to do
    }

    /**
     * Recycles this stream.
     */
    public void recycle() {
        byteChunk.recycle();
    }

    /**
     * Dump specified bytes into buffer.
     *
     * @param bytes The bytes
     */
    public void dumpToBuffer(final byte[] bytes) {
        byteChunk.setBytes(bytes, 0, bytes.length);
    }

    /**
     * Append specified bytes to buffer.
     *
     * @param bytes The bytes
     */
    public void appendToBuffer(final byte[] bytes) {
        byteChunk.appendBytes(bytes, 0, bytes.length);
    }

    @Override
    public int read() throws IOException {
        if (byteChunk.getLength() > 0) {
            return byteChunk.substract();
        }
        /*
         * Byte chunk is empty; request next one
         */
        final int read = inputBuffer.doRead(byteChunk, request);
        if (read <= 0) { // eof
            return -1;
        }
        return byteChunk.substract();
    }

    @Override
    public int read(final byte[] b) throws IOException {
        if (b == null) {
            throw new NullPointerException("buffer is null");
        }
        final int len = b.length;
        if (len == 0) {
            return 0;
        }
        return markEofIfZero(read0(b, 0, len));
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (b == null) {
            throw new NullPointerException("buffer is null");
        }
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException("off=" + off + ", len=" + len + ", available=" + (b.length - off));
        }
        if (len == 0) {
            return 0;
        }
        return markEofIfZero(read0(b, off, len));
    }

    /**
     * Fills up to <code>len</code> bytes into specified byte array starting from given <code>off</code>.
     *
     * @param b The byte array
     * @param off The offset
     * @param len The max. number of bytes to fill
     * @return The number of bytes filled into byte array
     * @throws IOException OIf an I/O error occurs
     */
    private int read0(final byte[] b, final int off, final int len) throws IOException {
        /*-
         *
         * ----- /!\ Don't return zero, because sun.nio.cs.StreamDecoder doesn't like that /!\ ------
         *
         */
        final int bLength = byteChunk.getLength();
        if (bLength >= len) {
            return byteChunk.substract(b, off, len);
        }
        /*
         * Write available bytes into array
         */
        byteChunk.substract(b, off, bLength);
        /*
         * More data needs to be requested
         */
        int read = bLength;
        int res = 0;
        while (read < len) {
            if (inputBuffer.doRead(byteChunk, request) <= 0) {
                return 0 == read ? -1 : read;
            }
            res = byteChunk.substract(b, read + off, len - read);
            read += res;
        }
        return len;
    }

    private static int markEofIfZero(final int result) {
        return result <= 0 ? -1 : result;
    }

}
