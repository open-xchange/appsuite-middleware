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

package com.openexchange.groupware.datahandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.java.Streams;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link InputStreamCopy}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - extracted class
 * @since v7.10.0
 */
final class InputStreamCopy implements Closeable {

    private static final int DEFAULT_BUF_SIZE = 0x2000;

    private byte[] bytes;

    private File file;

    private final long size;

    private String prefix;

    public InputStreamCopy(final InputStream orig, final String prefix, final boolean createFile) throws IOException {
        super();
        this.prefix = prefix;
        if (createFile) {
            size = copy2File(orig);
        } else {
            size = copy2ByteArr(orig);
        }
    }

    public long getSize() {
        return size;
    }

    public InputStream getInputStream() throws IOException {
        return bytes == null ? (file == null ? null : (new BufferedInputStream(new FileInputStream(file), DEFAULT_BUF_SIZE))) : (new UnsynchronizedByteArrayInputStream(bytes));
    }

    @Override
    public void close() {
        if (file != null) {
            if (file.exists()) {
                file.delete();
            }
            file = null;
        }
        if (bytes != null) {
            bytes = null;
        }
    }

    private int copy2ByteArr(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(DEFAULT_BUF_SIZE << 1);
        final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
        int len;
        while ((len = in.read(bbuf)) > 0) {
            out.write(bbuf, 0, len);
        }
        out.flush();
        this.bytes = out.toByteArray();
        out.close();
        return bytes.length;
    }

    private long copy2File(final InputStream in) throws IOException {
        long totalBytes = 0;
        {
            final File tmpFile = File.createTempFile(prefix, null, new File(ServerConfig.getProperty(ServerConfig.Property.UploadDirectory)));
            tmpFile.deleteOnExit();
            OutputStream out = null;
            try {
                out = new BufferedOutputStream(new FileOutputStream(tmpFile), DEFAULT_BUF_SIZE);
                final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
                int len;
                while ((len = in.read(bbuf)) > 0) {
                    out.write(bbuf, 0, len);
                    totalBytes += len;
                }
                out.flush();
            } finally {
                Streams.close(out);
            }
            file = tmpFile;
        }
        return totalBytes;
    }
}
