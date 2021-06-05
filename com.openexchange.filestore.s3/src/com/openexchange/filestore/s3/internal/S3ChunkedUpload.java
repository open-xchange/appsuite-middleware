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
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageCodes;

/**
 * {@link S3ChunkedUpload}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class S3ChunkedUpload extends com.openexchange.filestore.utils.ChunkedUpload<DigestInputStream, S3UploadChunk> {

    private static final int CIPHER_BLOCK_SIZE = 16;

    private static DigestInputStream digestStreamFor(InputStream data, boolean encrypted) throws OXException {
        try {
            DigestInputStream digestStream = new DigestInputStream(data, MessageDigest.getInstance("MD5"));
            if (encrypted) {
                // Disable updating digest if encrypted
                digestStream.on(false);
            }
            return digestStream;
        } catch (NoSuchAlgorithmException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------- //

    private final boolean encrypted;

    /**
     * Initializes a new {@link S3ChunkedUpload}.
     *
     * @param data The underlying input stream
     * @param encrypted Whether encryption is enabled
     * @param minChunkSize The minimum chunk size to fill in bytes
     * @throws OXException If initialization fails
     */
    public S3ChunkedUpload(InputStream data, boolean encrypted, long minChunkSize) throws OXException {
        super(digestStreamFor(data, encrypted), minChunkSize);
        this.encrypted = encrypted;
    }

    @Override
    protected S3UploadChunk createChunkWith(ThresholdFileHolder fileHolder, boolean eofReached) throws OXException {
        if (eofReached) {
            if (encrypted) {
                return new S3UploadChunk(fileHolder, null);
            }

            DigestInputStream digestStream = getInputStream();
            byte[] digest = digestStream.getMessageDigest().digest();
            return new S3UploadChunk(fileHolder, digest);
        }

        if (!encrypted) {
            DigestInputStream digestStream = getInputStream();
            byte[] digest = digestStream.getMessageDigest().digest();
            return new S3UploadChunk(fileHolder, digest);
        }

        // chunk sizes for encrypted multipart uploads must be multiples of the cipher block size (16) with the exception of the last part.
        int res = (int) (fileHolder.getCount() % CIPHER_BLOCK_SIZE);
        if (0 == res) {
            return new S3UploadChunk(fileHolder, null);
        }

        // Try to read missing bytes to have multiples of the cipher block size (16)
        try {
            DigestInputStream digestStream = getInputStream();
            res = CIPHER_BLOCK_SIZE - res;
            byte[] buf = new byte[res];
            int rd = digestStream.read(buf, 0, res);
            if (rd >= res) {
                fileHolder.write(buf, 0, rd);
                return new S3UploadChunk(fileHolder, null);
            }

            // No more data available - fall-through
            setHasNext(false);
            return new S3UploadChunk(fileHolder, null);
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        }
    }

}
