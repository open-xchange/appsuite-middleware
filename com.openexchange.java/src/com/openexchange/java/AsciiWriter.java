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

package com.openexchange.java;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * {@link AsciiWriter} - A simple ASCII byte writer.
 * <p>
 * This is an optimized writer for writing only 7-bit ASCII characters to byte streams.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AsciiWriter extends Writer {

    private final OutputStream out;

    /**
     * Initializes a new {@link AsciiWriter}.
     */
    public AsciiWriter(final OutputStream out) {
        super();
        this.out = out;
    }

    @Override
    public void write(final int c) throws IOException {
        out.write((byte) c);
    }

    @Override
    public void write(final char[] cbuf) throws IOException {
        if (null == cbuf) {
            throw new NullPointerException("cbuf is null.");
        }
        final int len = cbuf.length;
        if (len == 0) {
            return;
        }
        final byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) cbuf[i];
        }
        out.write(bytes, 0, len);
    }

    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        if (null == cbuf) {
            throw new NullPointerException("cbuf is null.");
        }
        if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        final byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) cbuf[off + i];
        }
        out.write(bytes, 0, len);
    }

    @Override
    public void write(final String str) throws IOException {
        if (null == str) {
            throw new NullPointerException("str is null.");
        }
        final int len = str.length();
        if (len == 0) {
            return;
        }
        final byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) str.charAt(i);
        }
        out.write(bytes, 0, len);
    }

    @Override
    public void write(final String str, final int off, final int len) throws IOException {
        if (null == str) {
            throw new NullPointerException("str is null.");
        }
        if ((off < 0) || (off > str.length()) || (len < 0) || ((off + len) > str.length()) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        final byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) str.charAt(off + i);
        }
        out.write(bytes, 0, len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

}
