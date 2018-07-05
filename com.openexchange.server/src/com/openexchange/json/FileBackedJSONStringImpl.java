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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import org.json.FileBackedJSONString;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link FileBackedJSONStringImpl} - The default implementation for {@link FileBackedJSONString}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class FileBackedJSONStringImpl implements FileBackedJSONString {

    private static final Charset UTF8 = Charsets.UTF_8;

    private static final String PREFIX = "openexchange-jsonstring-" + com.openexchange.exception.OXException.getServerId() + "-";

    private final File tmpFile;
    private final OutputStreamWriter se;
    private long length;

    /**
     * Initializes a new {@link FileBackedJSONStringImpl}.
     *
     * @throws OXException If initialization fails
     */
    public FileBackedJSONStringImpl() throws OXException {
        super();
        tmpFile = com.openexchange.ajax.container.TmpFileFileHolder.newTempFile(PREFIX, false);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tmpFile);
            se = new OutputStreamWriter(fos, UTF8);
            fos = null;
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(fos);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        // Drop the file when this instance if garbage-collected
        try {
            tmpFile.delete();
        } catch (Exception e) {
            // Ignore
        }
        super.finalize();
    }

    @Override
    public String toJSONString() {
        InputStream stream = null; InputStreamReader reader = null;
        try {
            reader = new InputStreamReader((stream = new FileInputStream(tmpFile)), UTF8);
            StringBuilder sb = new StringBuilder((int) tmpFile.length());

            char[] cbuf = new char[8192];
            for (int read; (read = reader.read(cbuf, 0, cbuf.length)) > 0;) {
                sb.append(cbuf, 0, read);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Streams.close(reader, stream);
        }
    }

    @Override
    public String toString() {
        return toJSONString();
    }

    @Override
    public File getTempFile() {
        return tmpFile;
    }

    @Override
    public void write(int c) throws IOException {
        se.write(c);
        length++;
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        write(cbuf, 0, cbuf.length);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        se.write(cbuf, off, len);
        length += len;
    }

    @Override
    public void write(String str) throws IOException {
        se.write(str);
        length += str.length();
    }

    @Override
    public void flush() throws IOException {
        se.flush();
    }

    @Override
    public void close() throws IOException {
        Streams.close(se);
    }

    @Override
    public int length() {
        return (int) length;
    }

    @Override
    public char charAt(int index) {
        InputStream stream = null; InputStreamReader reader = null;
        try {
            reader = new InputStreamReader((stream = new FileInputStream(tmpFile)), UTF8);

            char[] cbuf = new char[8192];
            int off = 0;
            for (int read; (read = reader.read(cbuf, 0, cbuf.length)) > 0;) {
                int end = off + read;
                if (index >= off && index < end) {
                    return cbuf[index - off];
                }

                off = end;
            }
            throw new IndexOutOfBoundsException();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Streams.close(reader, stream);
        }
    }

    @Override
    public CharSequence subSequence(int startIndex, int endIndex) {
        InputStream stream = null; InputStreamReader reader = null;
        try {
            reader = new InputStreamReader((stream = new FileInputStream(tmpFile)), UTF8);
            int len = endIndex - startIndex;
            StringBuilder sb = null;

            char[] cbuf = new char[8192];
            int off = 0;
            for (int read; (read = reader.read(cbuf, 0, cbuf.length)) > 0;) {
                int end = off + read;
                if (null == sb) {
                    // Not yet capturing
                    if (startIndex >= off && startIndex < end) {
                        int startPos = startIndex - off;
                        if (endIndex <= end) {
                            return new String(cbuf, startPos, len);
                        }

                        sb = new StringBuilder(len);
                        sb.append(cbuf, startPos, read - startPos);
                    }
                } else {
                    if (endIndex >= off && endIndex <= end) {
                        int endPos = endIndex - off;
                        sb.append(cbuf, 0, endPos);
                        return sb.toString();
                    }

                    sb.append(cbuf, 0, read);
                }


                off = end;
            }
            throw new IndexOutOfBoundsException();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Streams.close(reader, stream);
        }
    }

}
