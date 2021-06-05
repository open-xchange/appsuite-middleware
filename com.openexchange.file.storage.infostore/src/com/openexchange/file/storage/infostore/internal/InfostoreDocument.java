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

package com.openexchange.file.storage.infostore.internal;

import java.io.InputStream;
import java.util.function.Function;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.infostore.FileConverter;
import com.openexchange.groupware.infostore.DocumentAndMetadata;
import com.openexchange.groupware.infostore.DocumentMetadata;

/**
 * {@link InfostoreDocument}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class InfostoreDocument extends Document {

    private final DocumentAndMetadata documentAndMetadata;

    /**
     * Initializes a new {@link InfostoreDocument}.
     *
     * @param documentAndMetadata The underlying document and metadata
     */
    public InfostoreDocument(DocumentAndMetadata documentAndMetadata) {
        this(documentAndMetadata, new FileConverter());
    }

    /**
     * Initializes a new {@link InfostoreDocument}.
     *
     * @param documentAndMetadata The underlying document and metadata
     * @param converter The converter to get the file from the document metadata
     */
    public InfostoreDocument(DocumentAndMetadata documentAndMetadata, Function<DocumentMetadata, File> converter) {
        super();
        this.documentAndMetadata = documentAndMetadata;
        setEtag(documentAndMetadata.getETag());
        DocumentMetadata metadata = documentAndMetadata.getMetadata();
        if (null != metadata) {
            setFile(converter.apply(metadata));
            setMimeType(metadata.getFileMIMEType());
            setName(metadata.getFileName());
            setSize(metadata.getFileSize());
            if (null != metadata.getLastModified()) {
                setLastModified(metadata.getLastModified().getTime());
            }
        }
    }

    @Override
    public InputStream getData() throws OXException {
        return documentAndMetadata.getData();
    }

}
