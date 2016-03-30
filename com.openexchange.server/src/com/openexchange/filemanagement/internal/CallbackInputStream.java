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
        } catch (final Exception e) {
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
