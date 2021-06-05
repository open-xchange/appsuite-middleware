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

package com.openexchange.file.storage.webdav;

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.FileStorageExceptionCodes;

/**
 * {@link WebDAVDocument} - An efficient document view on a file
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class WebDAVDocument extends Document {

    @FunctionalInterface
    interface InputStreamClosure {

        InputStream newStream() throws OXException, IOException;
    }

    private final InputStreamClosure isClosure;

    /**
     * Initializes a new {@link WebDAVDocument}.
     *
     * @param file The meta data as {@link WebDAVFile}
     * @param The {@link InputStreamClosure} pointing to the document's data
     * @param etag The document's ETag
     */
    public WebDAVDocument(WebDAVFile file, InputStreamClosure isClosure, String etag) {
        this.isClosure = isClosure;
        if (file != null) {
            setFile(file);
            setMimeType(file.getFileMIMEType());
            setName(file.getFileName());
            setSize(file.getFileSize());
            if (file.getLastModified() != null) {
                setLastModified(file.getLastModified().getTime());
            }
        }
        setEtag(etag);
    }

    @Override
    public InputStream getData() throws OXException {
        try {
            return isClosure.newStream();
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }
}
