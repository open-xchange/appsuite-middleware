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

package com.openexchange.tools.io;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

public class IOTools {

    private IOTools() {
        super();
    }

    public static final void reallyBloodySkip(final InputStream is, long bytes) throws IOException {
        while (bytes > 0) {
            final long skipped = is.skip(bytes);
            if (skipped < 0) {
                return;
            }
            bytes -= skipped;
        }
    }

    /**
     * Copies content from <code>in</code> to <code>out</code>.
     * <p>
     * &nbsp;&nbsp;&nbsp;<strong>Note: Passed streams do <i>not</i> get {@link Closeable#close() closed}!</strong>
     *
     * @param in The stream source
     * @param out The stream sink
     * @throws IOException If an I/O error occurs
     */
    public static final void copy(final InputStream in, final OutputStream out) throws IOException {
        final InputStream inputStream = Streams.bufferedInputStreamFor(in);
        final OutputStream outputStream = Streams.bufferedOutputStreamFor(out);

        for(int i; (i = inputStream.read()) >= 0;) {
            outputStream.write(i);
        }
        outputStream.flush();
    }

    public static final byte[] getBytes(final InputStream stream) throws IOException {
        final InputStream in = Streams.bufferedInputStreamFor(stream);
        try {
            final ByteArrayOutputStream out = Streams.newByteArrayOutputStream();
            for(int i; (i = in.read()) >= 0;) {
                out.write(i);
            }
            return out.toByteArray();
        } finally {
            in.close();
        }

    }

    public static final String getFileContents(final File file) throws FileNotFoundException {
        final StringBuilder sb = new StringBuilder();
        Scanner scanner = null;
        try {
            scanner = new Scanner(file, Charsets.UTF_8_NAME);
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
                sb.append('\n');
            }
        } finally {
            if (null != scanner) {
                scanner.close();
            }
        }
        return sb.toString();
    }
}
