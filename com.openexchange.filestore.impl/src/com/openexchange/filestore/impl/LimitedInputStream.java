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

package com.openexchange.filestore.impl;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link LimitedInputStream}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class LimitedInputStream extends FilterInputStream {

    /**
     * The maximum size of an item, in bytes.
     */
    private final long sizeMax;

    /**
     * The current number of bytes.
     */
    private long count;

    /**
     * Creates a new instance.
     *
     * @param inputStream The input stream, which shall be limited.
     * @param pSizeMax The limit; no more than this number of bytes shall be returned by the source stream.
     */
    public LimitedInputStream(InputStream inputStream, long pSizeMax) {
        super(inputStream);
        sizeMax = pSizeMax;
    }

    /**
     * Called to indicate, that the input streams limit has been exceeded.
     *
     * @param numBytes The total number of bytes available from the underlying stream
     * @param maxNumBytes The input stream's limit, in bytes.
     * @throws IOException If the called method is expected to raise an I/O error
     */
    protected void raiseError(long numBytes, long maxNumBytes) throws IOException {
        throw new StorageFullIOException(numBytes, maxNumBytes);
    }

    /**
     * Called to check, whether the input stream's limit is reached.
     *
     * @throws IOException If the given limit is exceeded
     */
    private void checkLimit() throws IOException {
        if (count > sizeMax) {
            // Safely count remaining bytes to determine total number of bytes provided by underlying stream
            raiseError(count + countRemainingBytesSafely(), sizeMax);
        }
    }

    private long countRemainingBytesSafely() {
        long count = 0;
        int buflen = 0xFFFF;
        byte[] buf = new byte[buflen];
        int read;
        do {
            try {
                read = super.read(buf, 0, buflen);
            } catch (IOException e) {
                // Failed reading from stream
                read = 0;
            }
            if (read <= 0) {
                // No further bytes available
                return count;
            }
            count += read;
        } while (true);
    }

    @Override
    public int read() throws IOException {
        int res = super.read();
        if (res != -1) {
            count++;
            checkLimit();
        }
        return res;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int res = super.read(b, off, len);
        if (res > 0) {
            count += res;
            checkLimit();
        }
        return res;
    }

}
