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
import java.io.PipedOutputStream;
import java.util.concurrent.atomic.AtomicReference;

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