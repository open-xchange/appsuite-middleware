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

package com.openexchange.ajp13.coyote;

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import com.openexchange.ajp13.coyote.util.ByteChunk;
import com.openexchange.tools.stream.Flagged;

/**
 * {@link ActionAwareServletOutputStream}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ActionAwareServletOutputStream extends ServletOutputStream implements Flagged {

    private final OutputBuffer outputBuffer;

    private final ByteChunk byteChunk;

    private final int packetSize;

    /**
     * Initializes a new {@link ActionAwareServletOutputStream}.
     */
    public ActionAwareServletOutputStream(final OutputBuffer outputBuffer) {
        super();
        this.outputBuffer = outputBuffer;
        packetSize = outputBuffer.getPacketSize();
        byteChunk = new ByteChunk(packetSize);
    }

    @Override
    public boolean isFlagged() {
        return outputBuffer.isFlagged();
    }

    @Override
    public void flush() throws IOException {
        outputBuffer.doWrite(byteChunk);
        byteChunk.recycle();
    }

    /**
     * Checks if there is buffered data.
     *
     * @return <code>true</code> if there is buffered data; otherwise <code>false</code>
     */
    public boolean hasBufferedData() {
        return byteChunk.getLength() > 0;
    }

    /**
     * Recycles this stream.
     */
    public void recycle() {
        byteChunk.recycle();
    }

    /**
     * Resets the underlying buffer.
     */
    public void resetBuffer() {
        byteChunk.recycle();
    }

    @Override
    public void write(final int b) throws IOException {
        if (packetSize <= byteChunk.getLength()) {  
            /*-
             * No remaining size
             *
             * Flush chunk
             */
            outputBuffer.doWrite(byteChunk);
            byteChunk.recycle();
        }
        byteChunk.append((byte) b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        append(b, off, len);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        final int len = b.length;
        if (len == 0) {
            return;
        }
        append(b, 0, len);
    }

    private void append(final byte[] b, final int offset, final int length) throws IOException {
        int off = offset;
        int len = length;
        while (len > 0) {
            final int rem = packetSize - byteChunk.getLength();
            if (len <= rem) {
                byteChunk.append(b, off, len);
                return;
            }
            /*-
             * Remaining size lower than len
             *
             * Flush fitting chunk
             */
            byteChunk.append(b, off, rem);
            outputBuffer.doWrite(byteChunk);
            byteChunk.recycle();
            off += rem;
            len -= rem;
        }
    }

}
