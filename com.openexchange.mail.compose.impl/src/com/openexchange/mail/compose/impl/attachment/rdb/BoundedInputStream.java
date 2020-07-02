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

package com.openexchange.mail.compose.impl.attachment.rdb;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link BoundedInputStream} - Allows that only a certain number of bytes can be read from passed input stream.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class BoundedInputStream extends FilterInputStream {

    private final long boundary;
    private long count;

    /**
     * Initializes a new {@link BoundedInputStream}.
     */
    public BoundedInputStream(InputStream inputStream, long boundary) {
        super(inputStream);
        this.boundary = boundary;
        count = 0L;
    }

    @Override
    public int read() throws IOException {
        if (count + 1 > boundary) {
            return -1;
        }
        count++;

        int res = super.read();
        if (res < 0) {
            // No more bytes available from underlying stream
            count--;
        }
        return res;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytesToRead = len;
        if (count + bytesToRead > boundary) {
            bytesToRead = (int) (boundary - count);
            if (bytesToRead <= 0) {
                return -1;
            }
        }
        count += bytesToRead;

        int res = super.read(b, off, bytesToRead);
        if (res < 0) {
            // No more bytes available from underlying stream
            count -= bytesToRead;
        }
        return res;
    }

    /**
     * Gets the number of bytes that were read from this stream.
     *
     * @return The number of read bytes
     */
    public long getCount() {
        return count;
    }

}
