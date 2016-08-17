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
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link FastBufferedInputStream} - Extends {@link BufferedInputStream} and provides an improved implementation for {@link #available()}:
 * <p>
 * This method returns positive value if something is available, otherwise it will return zero.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FastBufferedInputStream extends BufferedInputStream {

    /**
     * Creates a <code>FastBufferedInputStream</code> and saves its argument, the input stream <code>in</code>, for later use.
     * <p>
     * An internal buffer array of size <code>65536</code> is created and stored in <code>buf</code>.
     *
     * @param in the underlying input stream.
     */
    public FastBufferedInputStream(final InputStream in) {
        super(in, 65536);
    }

    /**
     * Creates a <code>FastBufferedInputStream</code> with the specified buffer size, and saves its argument, the input stream <code>in</code>,
     * for later use. An internal buffer array of length <code>size</code> is created and stored in <code>buf</code>.
     *
     * @param in the underlying input stream.
     * @param size the buffer size.
     * @exception IllegalArgumentException If <code>size &lt;= 0</code>.
     */
    public FastBufferedInputStream(final InputStream in, final int size) {
        super(in, size);
    }

    @Override
    public int available() throws IOException {
        // This method returns positive value if something is available, otherwise it will return zero.
        if (in == null) {
            throw new IOException("Stream closed");
        }
        final int n = count - pos;
        return n > 0 ? n : in.available();
    }

}
