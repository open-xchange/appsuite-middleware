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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.icap;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import com.openexchange.java.Charsets;

/**
 * {@link ICAPInputStream} - A wrapper class for the ICAP server input stream.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPInputStream extends FilterInputStream {

    private static final byte[] CRLF = ICAPCommunicationStrings.CRLF.getBytes(Charsets.UTF_8);
    private static final byte[] TERMINATOR = ICAPCommunicationStrings.HTTP_TERMINATOR.getBytes(Charsets.UTF_8);
    private final Socket socket;
    private boolean eofReached = false;

    /**
     * Initialises a new {@link ICAPInputStream}. It wraps
     * the specified {@link InputStream}
     * 
     * @param in the {@link InputStream}
     * @param socket The {@link Socket}
     */
    public ICAPInputStream(InputStream in, Socket socket) {
        super(in);
        this.socket = socket;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.FilterInputStream#read()
     */
    @Override
    public int read() throws IOException {
        return super.read();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.FilterInputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        
        //int startOffset = off;
        int readBytes = -1;
        int mark = off;
        while (!eofReached && off < len && (readBytes = super.read(b, off, 1)) != -1) {
            off += readBytes;
            if (off < CRLF.length) {
                continue;
            }
            byte[] candidate = Arrays.copyOfRange(b, off - CRLF.length, off);
            if (Arrays.equals(CRLF, candidate)) {
                int contentLength = 0;
                byte[] contentLengthBytes = Arrays.copyOfRange(b, mark, off - CRLF.length);
                try {
                    if ((off - CRLF.length) - mark < 4) {
                        String s = new String(contentLengthBytes, "UTF-8");
                        contentLength = Integer.parseInt(s, 16);
                    }
                } catch (NumberFormatException e) {
                    // We probably reached portion of the null-byte
                }
                if (contentLength > 0) {
                    off = mark + off - CRLF.length - contentLengthBytes.length;
                    // Check if the current buffer can hold all data of the new chunk
                    if (mark + contentLength >= len) {
                        return mark + super.read(b, mark, len - mark);
                    }
                    off += super.read(b, mark, contentLength);
                    super.read(new byte[CRLF.length], 0, CRLF.length);
                    mark = off;
                    continue;
                }
            }
            if (off < TERMINATOR.length) {
                continue;
            }
            candidate = Arrays.copyOfRange(b, off - TERMINATOR.length, off);
            if (Arrays.equals(TERMINATOR, candidate)) {
                for (int i = off - TERMINATOR.length; i < len; i++) {
                    b[i] = '\0';
                }
                //b = Arrays.copyOfRange(b, startOffset, off - TERMINATOR.length - 1);
                eofReached = true;
                return off - TERMINATOR.length;  
            }
        }
        return eofReached ? - 1 : off;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.FilterInputStream#read(byte[])
     */
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.FilterInputStream#close()
     */
    @Override
    public void close() throws IOException {
        if (socket == null) {
            return;
        }
        IOUtils.closeQuietly(socket);
        super.close();
    }
}
