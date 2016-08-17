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
import java.io.InputStream;
import java.io.InterruptedIOException;

/**
 * {@link InterruptibleInputStream} - Wraps an <code>InputStream</code> instance and makes it interruptable.
 * <p>
 * The reading process can be interrupted by calling {@link #interrupt} or {@link #interrupt(java.io.IOException)} which will throw an
 * exception on the next read attempt and close the decorated input stream.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class InterruptibleInputStream extends InputStream {

    /** The exception to be thrown. If <code>null</code> then the stream is not yet interrupted. */
    private volatile IOException interrupted;

    /** The decorated input stream. */
    private final InputStream in;

    /**
     * Initializes a new {@link InterruptibleInputStream}.
     *
     * @param in The delegate input stream
     */
    public InterruptibleInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        IOException interrupted = this.interrupted;
        if (interrupted != null) {
            throw interrupted;
        }
        return in.read();
    }

    @Override
    public int available() throws IOException {
        IOException interrupted = this.interrupted;
        if (interrupted != null) {
            throw interrupted;
        }
        return in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public void reset() throws IOException {
        IOException interrupted = this.interrupted;
        if (interrupted != null) {
            throw interrupted;
        }
        in.reset();
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    @Override
    public long skip(long n) throws IOException {
        IOException interrupted = this.interrupted;
        if (interrupted != null) {
            throw interrupted;
        }
        return in.skip(n);
    }

    @Override
    public int read(byte b[]) throws IOException {
        IOException interrupted = this.interrupted;
        if (interrupted != null) {
            throw interrupted;
        }
        return in.read(b);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        IOException interrupted = this.interrupted;
        if (interrupted != null) {
            throw interrupted;
        }
        return in.read(b, off, len);
    }

    /**
     * Signals if this input stream has already been interrupted.
     *
     * @return <code>true</code> if interrupted; otherwise <code>false</code>
     */
    public boolean isInterrupted() {
        return interrupted != null;
    }

    /**
     * Interrupts this input stream throw a newly created <code>java.io.InterruptedIOException</code> marker.
     */
    public void interrupt() {
        interrupt(new InterruptedIOException());
    }

    /**
     * Interrupts this input stream using given <code>java.io.IOException</code> instance.
     *
     * @param exc The <code>java.io.IOException</code> instance that marks this input stream as interrupted
     * @throws IllegalStateException If this instance has already been interrupted
     */
    public void interrupt(IOException exc) {
        // check if not already interrupted
        IOException interrupted = this.interrupted;
        if (null == interrupted) {
            synchronized (this) {
                interrupted = this.interrupted;
                if (interrupted != null) {
                    throw new IllegalStateException("Input stream already interrupted.");
                }
                this.interrupted = exc;
                // close the decorated stream
                Streams.close(in);
            }
        }
    }

}
