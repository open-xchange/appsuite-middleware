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

    /**
     * Sets specified exception that gets re-thrown on further read attempts.
     *
     * @param e The exception to set
     */
    public void setException(final Exception e) {
        exception.set((e instanceof IOException) ? (IOException) e : new IOException("Error while writing to connected piped output stream", e));
    }

    private void checkForException() throws IOException {
        IOException exception = this.exception.get();
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public synchronized int available() throws IOException {
        checkForException();
        return super.available();
    }

    @Override
    public synchronized int read() throws IOException {
        checkForException();
        return super.read();
    }

    @Override
    public synchronized int read(final byte[] b, final int off, final int len) throws IOException {
        checkForException();
        return super.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

}
