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

package com.openexchange.mail.compose.json.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.StreamedUploadFile;
import com.openexchange.groupware.upload.StreamedUploadFileInputStream;
import com.openexchange.groupware.upload.StreamedUploadFileIterator;
import com.openexchange.groupware.upload.UploadFile;

/**
 * {@link UploadFileFileIterator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class UploadFileFileIterator implements StreamedUploadFileIterator {

    private final Iterator<UploadFile> uploadFilesIter;

    private final long totalSize;

    /**
     * Initializes a new {@link UploadFileFileIterator}.
     *
     * @param uploadFiles The upload files
     */
    public UploadFileFileIterator(List<UploadFile> uploadFiles) {
        super();
        this.uploadFilesIter = uploadFiles.iterator();
        if (uploadFiles.isEmpty()) {
            this.totalSize = 0L;
        } else if (uploadFiles.stream().allMatch(f -> f.getSize() >= 0)) {
            this.totalSize = uploadFiles.stream().collect(Collectors.summingLong(f -> f.getSize()));
        } else {
            this.totalSize = -1L;
        }
    }

    @Override
    public boolean hasNext() throws OXException {
        return uploadFilesIter.hasNext();
    }

    @Override
    public StreamedUploadFile next() throws OXException {
        UploadFile uploadFile = uploadFilesIter.next();
        return new StreamedUploadFileImpl(uploadFile);
    }

    @Override
    public long getRawTotalBytes() {
        return totalSize;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class StreamedUploadFileImpl implements StreamedUploadFile {

        private final UploadFile uploadFile;

        StreamedUploadFileImpl(UploadFile uploadFile) {
            super();
            this.uploadFile = uploadFile;
        }

        @Override
        public long getSize() {
            return uploadFile.getSize();
        }

        @Override
        public String getPreparedFileName() {
            return uploadFile.getPreparedFileName();
        }

        @Override
        public String getFileName() {
            return uploadFile.getFileName();
        }

        @Override
        public String getFieldName() {
            return uploadFile.getFieldName();
        }

        @Override
        public String getContentType() {
            return uploadFile.getContentType();
        }

        @Override
        public String getContentId() {
            return uploadFile.getContentId();
        }

        @Override
        public StreamedUploadFileInputStream getStream() throws IOException {
            return StreamedUploadFileInputStream.streamFor(uploadFile.openStream());
        }
    }

}
