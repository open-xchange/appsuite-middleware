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

package com.openexchange.filestore.s3.internal;

import java.io.IOException;
import java.io.InputStream;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.openexchange.java.Streams;

/**
 * {@link AbortIfNotConsumedInputStream} - Ensures underlying S3 object't content stream is aborted if this instance is closed but
 * not all bytes have been read, yet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class AbortIfNotConsumedInputStream extends InputStream {

    /** The underlying input stream representing the content of an S3 object */
    protected S3ObjectInputStream objectContent;

    private boolean closed;

    /**
     * Initializes a new {@link AbortIfNotConsumedInputStream}.
     *
     * @param objectContent The input stream containing the contents of an object
     */
    public AbortIfNotConsumedInputStream(S3ObjectInputStream objectContent) {
        super();
        this.objectContent = objectContent;
        closed = false;
    }

    @Override
    public int read() throws IOException {
        return objectContent.read();
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        return objectContent.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return objectContent.skip(n);
    }

    @Override
    public int available() throws IOException {
        return objectContent.available();
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            closeContentStream(objectContent);
        }
    }

    @Override
    public void mark(int readlimit) {
        objectContent.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        objectContent.reset();
    }

    @Override
    public boolean markSupported() {
        return objectContent.markSupported();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Closes given S3 object's content stream with respect to possibly non-consumed bytes.
     *
     * @param objectContent The input stream representing the content of an S3 object, which is supposed to be closed
     */
    protected static void closeContentStream(S3ObjectInputStream objectContent) {
        if (objectContent != null) {
            try {
                if (objectContent.read() >= 0) {
                    // Abort HTTP connection in case not all bytes were read from the S3ObjectInputStream
                    objectContent.abort();
                }
            } catch (IOException e) {
                //
            } finally {
                Streams.close(objectContent);
            }
        }
    }

}
