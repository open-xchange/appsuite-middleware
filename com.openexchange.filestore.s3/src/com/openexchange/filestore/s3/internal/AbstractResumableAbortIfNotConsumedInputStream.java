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
import javax.servlet.http.HttpServletResponse;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.openexchange.java.Streams;

/**
 * {@link AbstractResumableAbortIfNotConsumedInputStream} - Resumes reading an S3 object's content on premature EOF and ensures
 * underlying S3 object't content stream is aborted if this gets closed even though not all bytes have been read, yet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public abstract class AbstractResumableAbortIfNotConsumedInputStream extends AbortIfNotConsumedInputStream {

    /** The S3 client to access S3 resources */
    protected final AmazonS3Client s3Client;

    /** The name of the bucket containing the desired object */
    protected final String bucketName;

    /** The key in the specified bucket under which the object is stored */
    protected final String key;

    private long mark;

    /**
     * Initializes a new {@link AbstractResumableAbortIfNotConsumedInputStream}.
     *
     * @param objectContent The input stream containing the contents of an object
     * @param bucketName The name of the bucket containing the desired object
     * @param key The key in the specified bucket under which the object is stored
     * @param s3Client The S3 client
     */
    protected AbstractResumableAbortIfNotConsumedInputStream(S3ObjectInputStream objectContent, String bucketName, String key, AmazonS3Client s3Client) {
        super(objectContent);
        this.bucketName = bucketName;
        this.key = key;
        this.s3Client = s3Client;
        mark = -1;
    }

    /**
     * Notifies about specified number of currently consumed bytes.
     *
     * @param numberOfBytes The number of currently consumed bytes
     */
    protected abstract void onBytesRead(long numberOfBytes);

    /**
     * Initializes a new object stream after a premature EOF (<code>-1</code> was read) has been encountered.
     *
     * @throws IOException If initialization fails
     */
    protected abstract void initNewObjectStreamAfterPrematureEof() throws IOException;

    /**
     * Gets the position for the current mark.
     *
     * @return The current mark
     */
    protected abstract long getCurrentMark();

    /**
     * Resets to the given marked position.
     *
     * @param mark The position for the current mark
     */
    protected abstract void resetMark(long mark);

    /**
     * Handles given I/O exception that occurred while trying to read from S3 object's content stream.
     *
     * @param e The I/O exception to handle
     * @param errorOnPrematureEof Whether premature EOF should be handled through re-initializing S3 object's content stream
     * @throws IOException If an I/O exception should be advertised to caller
     */
    private void handleIOException(IOException e, boolean errorOnPrematureEof) throws IOException {
        if (errorOnPrematureEof || isNotPrematureEof(e)) {
            throw e;
        }

        // Close existent stream from which -1 was prematurely read
        Streams.close(objectContent);
        objectContent = null;

        // Initialize new object stream after premature EOF
        initNewObjectStreamAfterPrematureEof();
    }

    @Override
    public int read() throws IOException {
        return doRead(false);
    }

    private int doRead(boolean errorOnPrematureEof) throws IOException {
        try {
            int bite = objectContent.read();
            if (bite >= 0) {
                onBytesRead(1);
            }
            return bite;
        } catch (IOException e) {
            handleIOException(e, errorOnPrematureEof);

            // Repeat with new S3ObjectInputStream instance
            return doRead(true);
        }
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        return doRead(b, off, len, false);
    }

    private int doRead(byte b[], int off, int len, boolean errorOnPrematureEof) throws IOException {
        try {
            int result = objectContent.read(b, off, len);
            if (result >= 0) {
                onBytesRead(result);
            }
            return result;
        } catch (IOException e) {
            handleIOException(e, errorOnPrematureEof);

            // Repeat with new S3ObjectInputStream instance
            return doRead(b, off, len, true);
        }
    }

    @Override
    public long skip(long n) throws IOException {
        long result = objectContent.skip(n);
        onBytesRead(result);
        return result;
    }

    @Override
    public void mark(int readlimit) {
        objectContent.mark(readlimit);
        mark = getCurrentMark();
    }

    @Override
    public void reset() throws IOException {
        if (!objectContent.markSupported()) {
            throw new IOException("Mark not supported");
        }

        long mark = this.mark;
        if (mark == -1) {
            throw new IOException("Mark not set");
        }

        objectContent.reset();
        resetMark(mark);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if given I/O exception does <b>not</b> indicate premature EOF.
     *
     * @param e The I/O exception to examine
     * @return <code>true</code> if <b>no</b> premature EOF; otherwise <code>false</code>
     */
    protected static boolean isNotPrematureEof(IOException e) {
        return isPrematureEof(e) == false;
    }

    /**
     * Checks if given I/O exception indicates premature EOF.
     *
     * @param e The I/O exception to examine
     * @return <code>true</code> if premature EOF; otherwise <code>false</code>
     */
    protected static boolean isPrematureEof(IOException e) {
        if ("org.apache.http.ConnectionClosedException".equals(e.getClass().getName())) {
            // HTTP connection has been closed unexpectedly
            String message = e.getMessage();
            if (message != null && message.startsWith("Premature end of Content-Length delimited message body")) {
                /*-
                 * See org.apache.http.impl.io.ContentLengthInputStream.read(byte[], int, int)
                 *
                 * ...
                 * int readLen = this.in.read(b, off, chunk);
                 * if (readLen == -1 && pos < contentLength) {
                 *     throw new ConnectionClosedException(
                 *         "Premature end of Content-Length delimited message body (expected: %,d; received: %,d)",
                 *         contentLength, pos);
                 * }
                 * ...
                 *
                 * E.g. "Premature end of Content-Length delimited message body (expected: 52,428,800; received: 21,463,040)"
                 */
                return true;
            }
        }
        return false;
    }

    /**
     * Wraps given exception by an appropriate I/O exception.
     *
     * @param e The exception to wrap
     * @param key The key in the specified bucket under which the object is stored
     * @return The appropriate I/O exception
     */
    protected static IOException wrap(AmazonClientException e, String key) {
        if (AmazonServiceException.class.isInstance(e)) {
            AmazonServiceException serviceError = (AmazonServiceException) e;
            /*
             * Map to appropriate FileStorageCodes if possible
             */
            if (HttpServletResponse.SC_NOT_FOUND == serviceError.getStatusCode()) {
                return new IOException("File not found: " + key, e);
            }
            if (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE == serviceError.getStatusCode()) {
                return new IOException("Invalid range specified for file: " + key, e);
            }
        }
        return new IOException("Cannot read file: " + key + ". Reason: " + e.getMessage(), e);
    }

}
