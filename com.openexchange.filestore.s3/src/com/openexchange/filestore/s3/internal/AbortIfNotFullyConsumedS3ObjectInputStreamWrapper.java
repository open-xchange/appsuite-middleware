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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.filestore.s3.internal;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.openexchange.java.Streams;

/**
 * {@link AbortIfNotFullyConsumedS3ObjectInputStreamWrapper} - Ensures underlying S3 object't content stream is aborted if this gets closed
 * even though not all bytes have been read, yet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class AbortIfNotFullyConsumedS3ObjectInputStreamWrapper extends InputStream {

    /** The underlying S3 object's content stream to read from */
    protected S3ObjectInputStream objectContent;

    private boolean closed;

    /**
     * Initializes a new {@link AbortIfNotFullyConsumedS3ObjectInputStreamWrapper}.
     *
     * @param objectContent The input stream containing the contents of an object
     */
    public AbortIfNotFullyConsumedS3ObjectInputStreamWrapper(S3ObjectInputStream objectContent) {
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
     * @param objectContent The content stream to close
     */
    protected static void closeContentStream(S3ObjectInputStream objectContent) {
        if (objectContent == null) {
            return;
        }

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
