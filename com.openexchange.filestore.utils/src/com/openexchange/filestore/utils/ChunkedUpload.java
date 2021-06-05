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

package com.openexchange.filestore.utils;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.java.Streams;

/**
 * {@link ChunkedUpload} - Provides the data of passed input stream in chunks (aligned to {@link UploadChunk#MIN_CHUNK_SIZE}).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public abstract class ChunkedUpload<I extends InputStream, C extends UploadChunk> implements Closeable {

    /**
     * The default minimum allowed chunk size for multipart uploads, which is 5MB.
     */
    public static final long DEFAULT_MIN_CHUNK_SIZE = 5 * 1024 * 1024;

    /**
     * The buffer size of 1MB.
     */
    private static final int BUFFER_SIZE = 1 * 1024 * 1024;

    private final long minChunkSize;
    private final I inputStream;
    private boolean hasNext;

    /**
     * Initializes a new {@link ChunkedUpload} with {@link #DEFAULT_MIN_CHUNK_SIZE default minimum chunk size}.
     *
     * @param data The underlying input stream
     */
    protected ChunkedUpload(I data) {
        this(data, DEFAULT_MIN_CHUNK_SIZE);
    }

    /**
     * Initializes a new {@link ChunkedUpload}.
     *
     * @param data The underlying input stream
     */
    protected ChunkedUpload(I data, long minChunkSize) {
        super();
        this.inputStream = data;
        this.minChunkSize = minChunkSize;
        hasNext = true;
    }

    /**
     * Gets the input stream from which data is read
     *
     * @return The input stream
     */
    protected I getInputStream() {
        return inputStream;
    }

    /**
     * Sets the <code>hasNext</code> flag.
     *
     * @param The <code>hasNext</code> flag to set
     */
    protected void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    /**
     * Gets the next upload chunk.
     *
     * @return The next upload chunk
     * @throws OXException If next chunk cannot be returned due to an I/O error or because end of stream was already reached ({@link #hasNext()} signals <code>false</code>)
     */
    public C next() throws OXException {
        if (false == hasNext) {
            throw FileStorageCodes.IOERROR.create(new EOFException("End of input reached"), "End of input reached");
        }

        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        try {
            byte[] buffer = new byte[BUFFER_SIZE]; // 1MB
            for (int read; (read = inputStream.read(buffer, 0, buffer.length)) > 0;) {
                fileHolder.write(buffer, 0, read);
                if (fileHolder.getCount() >= minChunkSize) {
                    // Chunk size reached
                    C chunk = createChunkWith(fileHolder, false);
                    fileHolder = null;
                    return chunk;
                }
            }
            /*
             * end of input reached
             */
            hasNext = false;
            C chunk = createChunkWith(fileHolder, true);
            fileHolder = null;
            return chunk;
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } finally {
            Streams.close(fileHolder);
        }
    }

    /**
     * Invoked when specified minimum chunk size is reached when reading data from passed input stream.
     *
     * @param fileHolder The file holder holding chunk's data
     * @param eofReached <code>true</code> if end of stream has been reached; otherwise <code>false</code>
     * @return The upload chunk to return
     * @throws OXException If an I/O error occurs
     */
    protected abstract C createChunkWith(ThresholdFileHolder fileHolder, boolean eofReached) throws OXException;

    /**
     * Gets a value indicating whether further chunks are available or not.
     *
     * @return <code>true</code> if there are more, <code>false</code>, otherwise
     */
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public void close() {
        Streams.close(inputStream);
    }

}
