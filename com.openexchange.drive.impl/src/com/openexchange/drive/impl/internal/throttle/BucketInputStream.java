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
