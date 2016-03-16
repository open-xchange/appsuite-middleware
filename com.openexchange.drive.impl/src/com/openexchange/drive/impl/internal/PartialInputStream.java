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

package com.openexchange.drive.impl.internal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.tools.io.IOTools;

/**
 * {@link PartialInputStream}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PartialInputStream extends FilterInputStream {

    private final long length;
    private long bytesRead;

    /**
     * Initializes a new {@link PartialInputStream}.
     *
     * @param in the underlying input stream
     * @param offset The offset where to start reading
     * @param length The number of bytes to read from the offset, or <code>-1</code> to read until the end
     * @throws IOException
     */
    public PartialInputStream(InputStream in, long offset, long length) throws IOException {
        super(in);
        this.length = length;
        IOTools.reallyBloodySkip(this, offset);
        this.bytesRead = 0;
    }

    /**
     * Initializes a new {@link PartialInputStream}.
     *
     * @param in the underlying input stream
     * @param offset The offset where to start reading
     * @throws IOException
     */
    public PartialInputStream(InputStream in, long offset) throws IOException {
        this(in, offset, -1);
    }

    @Override
    public int read() throws IOException {
        if (-1 != length && bytesRead >= length) {
            return -1;
        }
        int read = super.read();
        if (-1 != read) {
            bytesRead++;
        }
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (-1 != length && bytesRead >= length) {
            return -1;
        }
        int toRead = -1 == length ? len : Math.min((int)(length - bytesRead), len);
        int read = super.read(b, off, toRead);
        if (-1 != read) {
            this.bytesRead += read;
        }
        return read;
    }

}
