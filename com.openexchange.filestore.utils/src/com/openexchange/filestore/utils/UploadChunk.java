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
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link UploadChunk} - Represents an upload chunk.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class UploadChunk implements Closeable {

    /** The backing file holder */
    protected final ThresholdFileHolder fileHolder;

    /**
     * Initializes a new {@link UploadChunk} served by the supplied file holder.
     *
     * @param fileHolder The underlying file holder
     */
    public UploadChunk(ThresholdFileHolder fileHolder) {
        super();
        this.fileHolder = fileHolder;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public long getSize() {
        return fileHolder.getCount();
    }

    /**
     * Gets the data.
     *
     * @return The data
     * @throws OXException
     */
    public InputStream getData() throws OXException {
        return fileHolder.getClosingStream();
    }

    @Override
    public void close() throws IOException {
        Streams.close(fileHolder);
    }

}
