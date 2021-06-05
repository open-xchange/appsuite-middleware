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

package com.openexchange.tools.file;

import java.io.InputStream;
import com.openexchange.exception.OXException;

/**
 * {@link AppendFileAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AppendFileAction extends FileStreamAction {

    private final String fileStorageID;
    private final long offset;
    private long fileSize;

    /**
     * Initializes a new {@link AppendFileAction}.
     *
     * @param storage The storage to save the stream to
     * @param data The input stream
     * @param fileStorageID The ID of the file to append the data to
     * @param sizeHint A size hint about the expected stream length in bytes, or <code>-1</code> if unknown
     * @param offset The offset in bytes where to append the data
     */
    public AppendFileAction(com.openexchange.filestore.FileStorage storage, InputStream data, String fileStorageID, long sizeHint, long offset) {
        super(storage, data, sizeHint, false);
        this.fileStorageID = fileStorageID;
        this.offset = offset;
    }

    /**
     * Gets the resulting filesize as reported by the storage after the stream was processed.
     *
     * @return The file size in bytes
     */
    public long getFileSize() {
        return fileSize;
    }

    @Override
    public String getFileStorageID() {
        return fileStorageID;
    }

    @Override
    protected void store(com.openexchange.filestore.FileStorage storage, InputStream stream) throws OXException {
        fileSize = storage.appendToFile(stream, fileStorageID, offset);
    }

    @Override
    protected void store(com.openexchange.filestore.QuotaFileStorage storage, InputStream stream, long sizeHint) throws OXException {
        fileSize = storage.appendToFile(stream, fileStorageID, offset, sizeHint);
    }

    @Override
    protected void undo(com.openexchange.filestore.FileStorage storage) throws OXException {
        storage.setFileLength(offset, fileStorageID);
    }

}
