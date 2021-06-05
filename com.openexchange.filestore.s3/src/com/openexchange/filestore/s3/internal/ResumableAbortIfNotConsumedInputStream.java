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
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.openexchange.java.Strings;

/**
 * {@link ResumableAbortIfNotConsumedInputStream} - Resumes reading an S3 object's content on premature EOF and ensures
 * underlying S3 object't content stream is aborted if this gets closed even though not all bytes have been read, yet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class ResumableAbortIfNotConsumedInputStream extends AbstractResumableAbortIfNotConsumedInputStream {

    private long numberOfReadBytes;
    private long contentLength;

    /**
     * Initializes a new {@link ResumableAbortIfNotConsumedInputStream}.
     *
     * @param objectContent The input stream containing the contents of an object
     * @param bucketName The name of the bucket containing the desired object
     * @param key The key in the specified bucket under which the object is stored
     * @param s3Client The S3 client
     */
    public ResumableAbortIfNotConsumedInputStream(S3ObjectInputStream objectContent, String bucketName, String key, AmazonS3Client s3Client) {
        super(objectContent, bucketName, key, s3Client);
        numberOfReadBytes = 0;
        contentLength = -1;
    }

    @Override
    protected void onBytesRead(long numberOfBytes) {
        numberOfReadBytes += numberOfBytes;
    }

    @Override
    protected long getCurrentMark() {
        return numberOfReadBytes;
    }

    @Override
    protected void resetMark(long mark) {
        numberOfReadBytes = mark;
    }

    @Override
    protected void initNewObjectStreamAfterPrematureEof() throws IOException {
        // Issue Get-Object request with appropriate range
        try {
            long rangeEnd = getContentLength() - 1;
            long rangeStart = numberOfReadBytes;
            GetObjectRequest request = new GetObjectRequest(bucketName, key);
            request.setRange(rangeStart, rangeEnd);
            objectContent = s3Client.getObject(request).getObjectContent();
        } catch (AmazonClientException ce) {
            throw wrap(ce, key);
        }
    }

    /**
     * Extracts the effective content length from the S3 object metadata, which is the length of the unencrypted content if specified, or the plain content length, otherwise.
     *
     * @return The length of the unencrypted content if specified, or the plain content length, otherwise
     * @throws IOException If content length cannot be returned
     */
    private long getContentLength() throws IOException {
        long contentLength = this.contentLength;
        if (contentLength < 0) {
            ObjectMetadata metadata = getObject().getObjectMetadata();
            String unencryptedContentLength = metadata.getUserMetaDataOf(com.amazonaws.services.s3.Headers.UNENCRYPTED_CONTENT_LENGTH);
            if (Strings.isEmpty(unencryptedContentLength)) {
                contentLength = metadata.getContentLength();
            } else {
                try {
                    contentLength = Long.parseLong(unencryptedContentLength.trim());
                } catch (NumberFormatException e) {
                    throw new IOException("Header for the original, unencrypted size of an encrypted object is not a number: " + unencryptedContentLength);
                }
            }
            this.contentLength = contentLength;
        }
        return contentLength;
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

}
