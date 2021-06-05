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

import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.filestore.utils.UploadChunk;
import com.openexchange.tools.encoding.Base64;


/**
 * {@link S3UploadChunk} - An AWS upload chunk.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class S3UploadChunk extends UploadChunk {

    private final String md5digest;

    /**
     * Initializes a new {@link S3UploadChunk} served by the supplied file holder.
     *
     * @param fileHolder The underlying file holder
     * @param md5digest The message digest
     */
    public S3UploadChunk(ThresholdFileHolder fileHolder, byte[] md5digest) {
        super(fileHolder);
        this.md5digest = null == md5digest ? null : Base64.encode(md5digest);
    }

    /**
     * Gets the MD5 digest,
     *
     * @return The MD5 digest or <code>null</code>
     */
    public String getMD5Digest() {
        return md5digest;
    }

    @Override
    public String toString() {
        return "S3UploadChunk [md5=" + md5digest + ", size=" + getSize() + "]";
    }

}
