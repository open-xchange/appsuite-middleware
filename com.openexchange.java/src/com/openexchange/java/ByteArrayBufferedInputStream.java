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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * {@link ByteArrayBufferedInputStream} - A {@link BufferedInputStream} backed by a {@link ByteArrayInputStream} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ByteArrayBufferedInputStream extends BufferedInputStream {

    private final ByteArrayInputStream bytes;

    /**
     * Creates a <code>ByteArrayBufferedInputStream</code> from given {@link ByteArrayInputStream} instance.
     *
     * @param bytes The underlying {@link ByteArrayInputStream} instance
     */
    public ByteArrayBufferedInputStream(ByteArrayInputStream bytes) {
        super(bytes, 1);
        this.bytes = bytes;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return bytes.read(b);
    }

    @Override
    public int read() {
        return bytes.read();
    }

    @Override
    public int read(byte[] b, int off, int len) {
        return bytes.read(b, off, len);
    }

    @Override
    public long skip(long n) {
        return bytes.skip(n);
    }

    @Override
    public int available() {
        return bytes.available();
    }

    @Override
    public boolean markSupported() {
        return bytes.markSupported();
    }

    @Override
    public void mark(int readAheadLimit) {
        bytes.mark(readAheadLimit);
    }

    @Override
    public void reset() {
        bytes.reset();
    }

    @Override
    public void close() throws IOException {
        bytes.close();
    }

    @Override
    public String toString() {
        return bytes.toString();
    }

}
