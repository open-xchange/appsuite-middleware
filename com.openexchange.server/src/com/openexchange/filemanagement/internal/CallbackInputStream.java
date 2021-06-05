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

package com.openexchange.filemanagement.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link CallbackInputStream} - Touches given managed file on every access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class CallbackInputStream extends InputStream implements FileRemovedListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CallbackInputStream.class);

    private final InputStream delegate;

    private final FileRemovedRegistry reg;

    private volatile boolean closed;

    /**
     * Initializes a new {@link CallbackInputStream}
     *
     * @param delegate The delegate input stream
     * @param reg The file-removed registry
     */
    CallbackInputStream(final InputStream delegate, final FileRemovedRegistry reg) {
        super();
        this.delegate = delegate;
        this.reg = reg;
    }

    @Override
    public int read() throws IOException {
        if (closed) {
            throw new IOException("Input stream closed.");
        }
        reg.touch();
        return delegate.read();
    }

    @Override
    public int read(final byte b[], final int off, final int len) throws IOException {
        if (closed) {
            throw new IOException("Input stream closed.");
        }
        reg.touch();
        return delegate.read(b, off, len);
    }

    @Override
    public long skip(final long n) throws IOException {
        if (closed) {
            throw new IOException("Input stream closed.");
        }
        reg.touch();
        return delegate.skip(n);
    }

    @Override
    public int available() throws IOException {
        if (closed) {
            throw new IOException("Input stream closed.");
        }
        reg.touch();
        return delegate.available();
    }

    @Override
    public boolean markSupported() {
        if (closed) {
            return false;
        }
        reg.touch();
        return delegate.markSupported();
    }

    @Override
    public void mark(final int readAheadLimit) {
        if (closed) {
            return;
        }
        delegate.mark(readAheadLimit);
        reg.touch();
    }

    @Override
    public void reset() throws IOException {
        if (closed) {
            throw new IOException("Input stream closed.");
        }
        delegate.reset();
        reg.touch();
    }

    @Override
    public void close() throws IOException {
        reg.removeListener(this);
        close0();
    }

    @Override
    public void removePerformed(final File file) {
        try {
            close0();
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    private void close0() throws IOException {
        if (closed) {
            return;
        }
        try {
            delegate.close();
        } finally {
            closed = true;
        }
    }
}
