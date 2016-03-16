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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link ExceptionAwarePipedInputStream} - Extends {@link PipedInputStream} class by {@link #setException(Exception)} that gets re-thrown
 * on further read attempts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ExceptionAwarePipedInputStream extends PipedInputStream {

    /** The (wrapping) I/O exception */
    private final AtomicReference<IOException> exception;

    /**
     * Initializes a new {@link ExceptionAwarePipedInputStream}.
     */
    public ExceptionAwarePipedInputStream() {
        super();
        exception = new AtomicReference<IOException>();
    }

    /**
     * Initializes a new {@link ExceptionAwarePipedInputStream}.
     *
     * @param pipeSize The size of the pipe's buffer
     */
    public ExceptionAwarePipedInputStream(final int pipeSize) {
        super(pipeSize);
        exception = new AtomicReference<IOException>();
    }

    /**
     * Initializes a new {@link ExceptionAwarePipedInputStream}.
     *
     * @param src The stream to connect to
     * @param pipeSize The size of the pipe's buffer
     * @throws IOException If initialization fails
     */
    public ExceptionAwarePipedInputStream(final PipedOutputStream src, final int pipeSize) throws IOException {
        super(src, pipeSize);
        exception = new AtomicReference<IOException>();
    }

    /**
     * Initializes a new {@link ExceptionAwarePipedInputStream}.
     *
     * @param src The stream to connect to
     * @throws IOException If initialization fails
     */
    public ExceptionAwarePipedInputStream(final PipedOutputStream src) throws IOException {
        super(src);
        exception = new AtomicReference<IOException>();
    }

    @Override
    public synchronized int available() throws IOException {
        final IOException exception = this.exception.get();
        if (exception != null) {
            throw exception;
        }
        return super.available();
    }

    @Override
    public synchronized int read() throws IOException {
        final IOException exception = this.exception.get();
        if (exception != null) {
            throw exception;
        }
        return super.read();
    }

    @Override
    public synchronized int read(final byte[] b, final int off, final int len) throws IOException {
        final IOException exception = this.exception.get();
        if (exception != null) {
            throw exception;
        }
        return super.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        final IOException exception = this.exception.get();
        if (exception != null) {
            throw exception;
        }
        super.close();
    }

    /**
     * Sets specified exception that gets re-thrown on further read attempts.
     *
     * @param e The exception to set
     */
    public void setException(final Exception e) {
        exception.set((e instanceof IOException) ? (IOException) e : new IOException("Error while writing to connected OutputStream", e));
    }

}
