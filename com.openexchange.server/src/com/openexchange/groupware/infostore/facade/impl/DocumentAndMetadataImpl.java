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

package com.openexchange.groupware.infostore.facade.impl;

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajax.fileholder.IFileHolder.InputStreamClosure;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.groupware.infostore.DocumentAndMetadata;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.java.SizeKnowingInputStream;


/**
 * {@link DocumentAndMetadataImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DocumentAndMetadataImpl implements DocumentAndMetadata {

    private final DocumentMetadata metadata;
    private final InputStreamClosure isClosure;
    private final String eTag;

    /**
     * Initializes a new {@link DocumentAndMetadataImpl}.
     *
     * @param metadata The metadata
     * @param isClosure The input stream closure, or <code>null</code> if not set
     * @param eTag The E-Tag
     */
    public DocumentAndMetadataImpl(DocumentMetadata metadata, InputStreamClosure isClosure, String eTag) {
        super();
        this.metadata = metadata;
        this.isClosure = isClosure;
        this.eTag = eTag;
    }

    @Override
    public DocumentMetadata getMetadata() {
        return metadata;
    }

    @Override
    public String getETag() {
        return eTag;
    }

    @Override
    public InputStream getData() throws OXException {
        try {
            return new SizeKnowingInputStream(isClosure.newStream(), metadata.getFileSize());
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

}
