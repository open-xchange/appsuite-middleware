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
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.openexchange.java.Streams;

/**
 * {@link RangeSupportingResumableAbortIfNotFullyConsumedS3ObjectInputStreamWrapper} - Resumes reading an S3 object's content on premature EOF and ensures
 * underlying S3 object't content stream is aborted if this gets closed even though not all bytes have been read, yet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class RangeSupportingResumableAbortIfNotFullyConsumedS3ObjectInputStreamWrapper extends AbortIfNotFullyConsumedS3ObjectInputStreamWrapper {

    private final AmazonS3Client s3Client;
    private final String bucketName;
    private final String key;
    private final long rangeEnd;

    private long rangeStart;
    private long mark;

    /**
     * Initializes a new {@link RangeSupportingResumableAbortIfNotFullyConsumedS3ObjectInputStreamWrapper}.
     *
     * @param objectContent The input stream containing the contents of an object
     * @param range The optional range
     * @param bucketName The name of the bucket containing the desired object
     * @param key The key in the specified bucket under which the object is stored
     * @param s3Client The S3 client
     */
    public RangeSupportingResumableAbortIfNotFullyConsumedS3ObjectInputStreamWrapper(S3ObjectInputStream objectContent, long[] range, String bucketName, String key, AmazonS3Client s3Client) {
        super(objectContent);
        this.bucketName = bucketName;
        this.key = key;
        this.s3Client = s3Client;
        rangeEnd = range[1];
        rangeStart = range[0];
        mark = -1;
    }

    @Override
    public int read() throws IOException {
        try {
            int bite = objectContent.read();
            if (bite >= 0) {
                rangeStart++;
            }
            return bite;
        } catch (IOException e) {
            if (isNotPrematureEof(e)) {
                throw e;
            }

            // Initialize new object stream after premature EOF
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
                rangeStart += result;
            }
            return result;
        } catch (IOException e) {
            if (isNotPrematureEof(e)) {
                throw e;
            }

            // Initialize new object stream after premature EOF
            initNewObjectStreamAfterPrematureEof();

            // Repeat with new S3ObjectInputStream instance
            return read(b, off, len);
        }
    }

    @Override
    public long skip(long n) throws IOException {
        long result = objectContent.skip(n);
        rangeStart += result;
        return result;
    }

    @Override
    public void mark(int readlimit) {
        objectContent.mark(readlimit);
        mark = rangeStart;
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
        rangeStart = mark;
    }

    private void initNewObjectStreamAfterPrematureEof() throws IOException {
        // Close existent stream from which -1 was prematurely read
        Streams.close(objectContent);
        objectContent = null;

        // Issue Get-Object request with appropriate range
        try {
            GetObjectRequest request = new GetObjectRequest(bucketName, key);
            request.setRange(rangeStart, rangeEnd);
            objectContent = s3Client.getObject(request).getObjectContent();
        } catch (AmazonClientException ce) {
            throw wrap(ce, key);
        }
    }

}
