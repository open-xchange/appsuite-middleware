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

package com.sun.mail.smtp;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This stream sits on top of an already existing output stream (the underlying output stream) which it uses as its basic sink of data.
 * <p>
 * Prior to passing bytes to the underlying output stream, the bytes to write are counted and checked against given max. number of bytes
 * that may be written. If max. number of bytes is exceeded an {@link IOException} <code>"Maximum message size is exceeded."</code> is thrown.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class CountingOutputStream extends FilterOutputStream {

    /** The count of bytes that have passed. */
    private long count = 0;

    /** The max. number of bytes that may be written. */
    private final long maxMailSize;

    /**
     * Initializes a new {@link CountingOutputStream}.
     *
     * @param data The output stream to delegate to
     * @param maxMailSize The max. number of bytes that may be written
     */
    public CountingOutputStream(OutputStream out, long maxMailSize) {
        super(out);
        this.maxMailSize = maxMailSize;
    }

    /**
     * Updates the count with the number of bytes that are being written.
     *
     * @param n number of bytes to be written to the stream
     * @throws IOException
     */
    protected void beforeWrite(int n) throws IOException {
        count += n;
        if (count > maxMailSize) {
            try {
                throw new IOException("Maximum message size is exceeded.");
            } finally {
                this.close();
            }
        }
    }

    /**
     * Invokes the delegate's <code>write(int)</code> method.
     *
     * @param idx the byte to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(int idx) throws IOException {
        beforeWrite(1);
        out.write(idx);
    }

    /**
     * Invokes the delegate's <code>write(byte[])</code> method.
     *
     * @param bts the bytes to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(byte[] bts) throws IOException {
        if (null == bts) {
            return;
        }

        int len = bts.length;
        beforeWrite(len);
        out.write(bts);
    }

    /**
     * Invokes the delegate's <code>write(byte[])</code> method.
     *
     * @param bts the bytes to write
     * @param off the start offset in the data
     * @param len the number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(byte[] bts, int off, int len) throws IOException {
        if (null == bts) {
            return;
        }

        beforeWrite(len);
        out.write(bts, off, len);
    }

    /**
     * Invokes the delegate's <code>flush()</code> method.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Invokes the delegate's <code>close()</code> method.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        out.close();
    }

}
