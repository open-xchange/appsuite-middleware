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

package com.openexchange.java;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.java.ExceptionAwarePipedInputStream;

/**
 * {@link ExceptionForwardingPipedOutputStream} - Forwards possible exceptions to linked instance of <code>ExceptionAwarePipedInputStream</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class ExceptionForwardingPipedOutputStream extends PipedOutputStream {

    private final AtomicReference<IOException> exception;
    private final ExceptionAwarePipedInputStream eapis;

    /**
     * Creates a piped output stream connected to the specified piped input stream.
     * <p>
     * Data bytes written to this stream will then be available as input from <code>snk</code>.
     *
     * @param snk The piped input stream to connect to
     * @throws IOException If an I/O error occurs.
     */
    public ExceptionForwardingPipedOutputStream(ExceptionAwarePipedInputStream snk) throws IOException {
        super(snk);
        exception = new AtomicReference<IOException>();
        this.eapis = snk;
    }

    /**
     * Sets specified exception that gets re-thrown on further write attempts.
     *
     * @param e The exception to set
     */
    public void setException(final Exception e) {
        exception.set((e instanceof IOException) ? (IOException) e : new IOException("Error while reading from connected piped input stream", e));
    }

    private void checkForException() throws IOException {
        IOException exception = this.exception.get();
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public void write(int b) throws IOException {
        checkForException();
        try {
            super.write(b);
        } catch (IOException e) {
            eapis.setException(e);
            throw e;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkForException();
        try {
            super.write(b, off, len);
        } catch (IOException e) {
            eapis.setException(e);
            throw e;
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        checkForException();
        try {
            super.flush();
        } catch (Exception e) {
            eapis.setException(e);
            throw e;
        }
    }

    /**
     * Forwards given exception to connected piped input stream.
     *
     * @param e The exception to forward
     */
    public void forwardException(Exception e) {
        eapis.setException(e);
    }

}