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

package com.openexchange.contact.vcard.impl.internal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link VCardInputStream}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.org">Tobias Friedrich</a>
 */
public class VCardInputStream extends FilterInputStream {

    private final long maxSize;

    private long currentSize;

    /**
     * Initializes a new {@link VCardInputStream}.
     *
     * @param delegate The underlying stream
     * @param maxSize The maximum allowed size in bytes, or a value smaller or equal to <code>0</code> if there are no restrictions
     */
    public VCardInputStream(InputStream delegate, long maxSize) {
        super(delegate);
        this.maxSize = maxSize;
    }

    /**
     * Resets the current number of read bytes.
     */
    public void resetCurrentSize() {
        this.currentSize = 0;
    }

    @Override
    public int read() throws IOException {
        int r = in.read();
        if (r != -1) {
            currentSize++;
            checkSize(currentSize);
        }
        return r;
    }

    @Override
    public int read(byte[] arg0, int arg1, int arg2) throws IOException {
        int r = in.read(arg0, arg1, arg2);
        if (r > 0) {
            currentSize += r;
            checkSize(currentSize);
        }
        return r;
    }

    @Override
    public int read(byte[] arg0) throws IOException {
        return read(arg0, 0, arg0.length);
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public long skip(long arg0) throws IOException {
        return in.skip(arg0);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    private void checkSize(long size) throws IOException {
        if (0 < maxSize && currentSize > maxSize) {
            throw new IOException(VCardExceptionCodes.MAXIMUM_SIZE_EXCEEDED.create(Long.valueOf(maxSize)));
        }
    }

}
