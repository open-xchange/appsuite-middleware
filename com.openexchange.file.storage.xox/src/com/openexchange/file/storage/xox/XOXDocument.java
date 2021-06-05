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

package com.openexchange.file.storage.xox;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.FileStorageExceptionCodes;

/**
 * {@link XOXDocument} - A document shared from another OX instance
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class XOXDocument extends Document {

    @FunctionalInterface
    interface InputStreamClosure {

        InputStream newStream() throws OXException, IOException;
    }

    private final InputStreamClosure data;

    /**
     * Initializes a new {@link XOXDocument}.
     *
     * @param file The meta data as {@link XOXFile}
     * @param data The data as {@link InputStreamClosure} which allows lazy loading
     * @param eTag The ETag of the document, or null
     */
    public XOXDocument(XOXFile file, InputStreamClosure data, String eTag) {
        this.data = Objects.requireNonNull(data, "data must not be null");
        setEtag(eTag);
        if (file != null) {
            setFile(file);
            setMimeType(file.getFileMIMEType());
            setName(file.getFileName());
            if (file.getLastModified() != null) {
                setLastModified(file.getLastModified().getTime());
            }
            setSize(file.getFileSize());
        }
    }

    @Override
    public boolean isRepetitive() {
        return false;
    }

    @Override
    public InputStream getData() throws OXException {
        try {
            return data.newStream();
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }
}
