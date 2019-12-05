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
