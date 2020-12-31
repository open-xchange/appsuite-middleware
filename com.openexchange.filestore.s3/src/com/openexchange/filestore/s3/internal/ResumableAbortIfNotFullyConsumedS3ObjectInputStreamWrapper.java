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
import javax.servlet.http.HttpServletResponse;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link ResumableAbortIfNotFullyConsumedS3ObjectInputStreamWrapper} - Resumes reading an S3 object's content on premature EOF and ensures
 * underlying S3 object't content stream is aborted if this gets closed even though not all bytes have been read, yet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class ResumableAbortIfNotFullyConsumedS3ObjectInputStreamWrapper extends AbortIfNotFullyConsumedS3ObjectInputStreamWrapper {

    private final AmazonS3Client s3Client;
    private final String bucketName;
    private final String key;
    private long numberOfReadBytes;
    private long mark;
    private long fileSize;

    /**
     * Initializes a new {@link ResumableAbortIfNotFullyConsumedS3ObjectInputStreamWrapper}.
     *
     * @param objectContent The input stream containing the contents of an object
     * @param bucketName The name of the bucket containing the desired object
     * @param key The key in the specified bucket under which the object is stored
     */
    public ResumableAbortIfNotFullyConsumedS3ObjectInputStreamWrapper(S3ObjectInputStream objectContent, String bucketName, String key, AmazonS3Client s3Client) {
        super(objectContent);
        this.bucketName = bucketName;
        this.key = key;
        this.s3Client = s3Client;
        numberOfReadBytes = 0;
        mark = -1;
        fileSize = -1;
    }

    @Override
    public int read() throws IOException {
        try {
            int bite = objectContent.read();
            if (bite >= 0) {
                numberOfReadBytes++;
            }
            return bite;
        } catch (IOException e) {
            if (isNotPrematureEof(e)) {
                throw e;
            }

            // Initialize new object stream after preamture EOF
            initNewObjectStreamAfterPrematureEof();

            // Repeat with new S3ObjectInputStream instance
            return read();
        }
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        try {
            int result = objectContent.read(b, off, len);
            if (result >= 0) {
                numberOfReadBytes += result;
            }
            return result;
        } catch (IOException e) {
            if (isNotPrematureEof(e)) {
                throw e;
            }

            // Initialize new object stream after preamture EOF
            initNewObjectStreamAfterPrematureEof();

            // Repeat with new S3ObjectInputStream instance
            return read(b, off, len);
        }
    }

    @Override
    public long skip(long n) throws IOException {
        long result = objectContent.skip(n);
        numberOfReadBytes += result;
        return result;
    }

    @Override
    public void mark(int readlimit) {
        objectContent.mark(readlimit);
        mark = numberOfReadBytes;
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
        numberOfReadBytes = mark;
    }

    private void initNewObjectStreamAfterPrematureEof() throws IOException {
        // Close existent stream from which -1 was prematurely read
        Streams.close(objectContent);
        objectContent = null;

        // Issue Get-Object request with appropriate range
        try {
            long fileSize = getFileSize();
            long offset = numberOfReadBytes;
            GetObjectRequest request = new GetObjectRequest(bucketName, key);
            request.setRange(offset, fileSize - 1);
            objectContent = s3Client.getObject(request).getObjectContent();
        } catch (AmazonClientException ce) {
            throw wrap(ce, key);
        }
    }

    private long getFileSize() throws IOException {
        long fileSize = this.fileSize;
        if (fileSize < 0) {
            fileSize = getContentLength();
            this.fileSize = fileSize;
        }
        return fileSize;
    }

    /**
     * Extracts the effective content length from the S3 object metadata, which is the length of the unencrypted content if specified, or the plain content length, otherwise.
     *
     * @return The length of the unencrypted content if specified, or the plain content length, otherwise
     * @throws IOException If content length cannot be returned
     */
    private long getContentLength() throws IOException {
        ObjectMetadata metadata = getObject().getObjectMetadata();
        String unencryptedContentLength = metadata.getUserMetaDataOf(com.amazonaws.services.s3.Headers.UNENCRYPTED_CONTENT_LENGTH);
        if (Strings.isNotEmpty(unencryptedContentLength)) {
            try {
                return Long.parseLong(unencryptedContentLength.trim());
            } catch (NumberFormatException e) {
                throw new IOException("Header for the original, unencrypted size of an encrypted object is not a number: " + unencryptedContentLength);
            }
        }
        return metadata.getContentLength();
    }

    /**
     * Gets the S3 object.
     *
     * @throws IOException If S3 object cannot be returned
     */
    private S3Object getObject() throws IOException {
        try {
            return s3Client.getObject(bucketName, key);
        } catch (AmazonClientException e) {
            throw wrap(e, key);
        }
    }

    private static boolean isNotPrematureEof(IOException e) {
        return isPrematureEof(e) == false;
    }

    private static boolean isPrematureEof(IOException e) {
        if ("org.apache.http.ConnectionClosedException".equals(e.getClass().getName())) {
            // HTTP connection has been closed unexpectedly
            String message = e.getMessage();
            if (message != null && message.startsWith("Premature end of Content-Length delimited message body")) {
                return true;
            }
        }
        return false;
    }

    private static IOException wrap(AmazonClientException e, String key) {
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
