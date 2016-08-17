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
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class CountingOutputStream extends FilterOutputStream {

    /** The count of bytes that have passed. */
    private long count = 0;

    private long maxMailSize;

    /**
     * Constructs a new CountingOutputStream.
     * 
     * @param out the OutputStream to write to
     */
    public CountingOutputStream(OutputStream out) {
        super(out);
    }

    // -----------------------------------------------------------------------

    /**
     * Initializes a new {@link CountingOutputStream}.
     * 
     * @param data
     * @param maxMailSize
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
        try {
            beforeWrite(1);
            out.write(idx);
            afterWrite(1);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>write(byte[])</code> method.
     * 
     * @param bts the bytes to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(byte[] bts) throws IOException {
        try {
            int len = bts != null ? bts.length : 0;
            beforeWrite(len);
            out.write(bts);
            afterWrite(len);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>write(byte[])</code> method.
     * 
     * @param bts the bytes to write
     * @param st The start offset
     * @param end The number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(byte[] bts, int st, int end) throws IOException {
        try {
            beforeWrite(end);
            out.write(bts, st, end);
            afterWrite(end);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>flush()</code> method.
     * 
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void flush() throws IOException {
        try {
            out.flush();
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>close()</code> method.
     * 
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        try {
            out.close();
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invoked by the write methods after the proxied call has returned successfully. The number of bytes written (1 for the
     * {@link #write(int)} method, buffer length for {@link #write(byte[])}, etc.) is given as an argument.
     * <p>
     * Subclasses can override this method to add common post-processing functionality without having to override all the write methods. The
     * default implementation does nothing.
     * 
     * @since 2.0
     * @param n number of bytes written
     * @throws IOException if the post-processing fails
     */
    protected void afterWrite(int n) throws IOException {
    }

    /**
     * Handle any IOExceptions thrown.
     * <p>
     * This method provides a point to implement custom exception handling. The default behaviour is to re-throw the exception.
     * 
     * @param e The IOException thrown
     * @throws IOException if an I/O error occurs
     * @since 2.0
     */
    protected void handleIOException(IOException e) throws IOException {
        throw e;
    }

}
