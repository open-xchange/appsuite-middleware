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

import java.io.InputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultChunkedUpload} - The default implementation for {@link ChunkedUpload}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DefaultChunkedUpload extends ChunkedUpload<InputStream, UploadChunk> {

    /**
     * Initializes a new {@link DefaultChunkedUpload}.
     *
     * @param data  The input stream to read from
     * @param minChunkSize The minimum chunk size
     */
    public DefaultChunkedUpload(InputStream data, long minChunkSize) {
        super(data, minChunkSize);
    }

    /**
     * Initializes a new {@link DefaultChunkedUpload} with {@link ChunkedUpload#DEFAULT_MIN_CHUNK_SIZE default minimum chunk size}.
     *
     * @param data The input stream to read from
     */
    public DefaultChunkedUpload(InputStream data) {
        super(data);
    }

    @Override
    protected UploadChunk createChunkWith(ThresholdFileHolder fileHolder, boolean eofReached) throws OXException {
        return new UploadChunk(fileHolder);
    }

}
