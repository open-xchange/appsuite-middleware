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

package com.openexchange.icap;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import com.openexchange.java.Charsets;
import com.openexchange.tools.io.IOUtils;

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

    @Override
    public int read() throws IOException {
        return super.read();
    }

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

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public void close() throws IOException {
        if (socket == null) {
            return;
        }
        IOUtils.closeQuietly(socket);
        super.close();
    }
}
