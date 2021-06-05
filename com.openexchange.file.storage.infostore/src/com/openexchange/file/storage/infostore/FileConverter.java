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

package com.openexchange.file.storage.infostore;

import java.util.function.Function;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.infostore.internal.InfostoreDocument;
import com.openexchange.groupware.infostore.DocumentAndMetadata;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.ConvertingSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link FileConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class FileConverter implements Function<DocumentMetadata, File> {

    /**
     * Initializes a new {@link FileConverter}.
     */
    public FileConverter() {
        super();
    }

    @Override
    public File apply(DocumentMetadata metadata) {
        return getFile(metadata);
    }

    public File getFile(DocumentMetadata metadata) {
        return null == metadata ? null : new InfostoreFile(metadata);
    }

    public Document getFileDocument(DocumentAndMetadata metadataDocument) {
        return new InfostoreDocument(metadataDocument, this);
    }

    public TimedResult<File> getFileTimedResult(TimedResult<DocumentMetadata> metadataTimedResult) {
        return new TimedResult<File>() {

            @Override
            public SearchIterator<File> results() throws OXException {
                return getFileSearchIterator(metadataTimedResult.results());
            }

            @Override
            public long sequenceNumber() throws OXException {
                return metadataTimedResult.sequenceNumber();
            }
        };
    }

    public Delta<File> getFileDelta(Delta<DocumentMetadata> metadataDelta) {
        return new Delta<File>() {

            @Override
            public SearchIterator<File> results() throws OXException {
                return getFileSearchIterator(metadataDelta.results());
            }

            @Override
            public long sequenceNumber() throws OXException {
                return metadataDelta.sequenceNumber();
            }

            @Override
            public SearchIterator<File> getNew() {
                return getFileSearchIterator(metadataDelta.getNew());
            }

            @Override
            public SearchIterator<File> getModified() {
                return getFileSearchIterator(metadataDelta.getModified());
            }

            @Override
            public SearchIterator<File> getDeleted() {
                return getFileSearchIterator(metadataDelta.getDeleted());
            }
        };
    }

    public SearchIterator<File> getFileSearchIterator(SearchIterator<DocumentMetadata> metadataSearchIterator) {
        return null == metadataSearchIterator ? null : new ConvertingSearchIterator<DocumentMetadata, File>(metadataSearchIterator, this);
    }

    public DocumentMetadata getMetadata(File file) throws OXException {
        return new FileMetadata(file);
    }

}
