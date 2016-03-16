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

package com.openexchange.drive.impl.internal.throttle;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.java.Streams;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BucketInputStream}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class BucketInputStream extends InputStream {

    private static final AtomicReference<DriveTokenBucket> tokenBucketReference = new AtomicReference<DriveTokenBucket>();

    /**
     * Sets the drive token bucket reference to use, or terminates the token bucket by passing <code>null</code>.
     *
     * @param tokenBucket The token bucket, or <code>null</code> to disable.
     */
    public static void setTokenBucket(DriveTokenBucket tokenBucket) {
        if (null == tokenBucket) {
            DriveTokenBucket previousTokenBucket = tokenBucketReference.get();
            if (null != previousTokenBucket) {
                previousTokenBucket.stop();
            }
        }
        tokenBucketReference.set(tokenBucket);
    }

    private final InputStream delegate;
    private final ServerSession session;

    /**
     * Initializes a new {@link BucketInputStream} that acquires the requested number of bytes to read from the supplied token bucket.
     *
     * @param delegate The underlying input stream
     * @param session The session
     */
    public BucketInputStream(InputStream delegate, ServerSession session) {
        super();
        this.delegate = delegate;
        this.session = session;
    }

    @Override
    public int read() throws IOException {
        blockingWait(1);
        return delegate.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        blockingWait(len);
        return delegate.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        Streams.close(delegate);
        super.close();
    }

    private void blockingWait(int length) throws IOException {
        DriveTokenBucket tokenBucket = tokenBucketReference.get();
        if (null != tokenBucket && tokenBucket.isEnabled()) {
            try {
                tokenBucket.takeBlocking(session, length);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            }
        }
    }

}
