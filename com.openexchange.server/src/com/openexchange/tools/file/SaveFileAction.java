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
 * {@link SaveFileAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SaveFileAction extends FileStreamAction {

    private String fileStorageID;

    /**
     * Initializes a new {@link SaveFileAction}.
     *
     * @param storage The storage to save the stream to
     * @param data The input stream
     * @param sizeHint A size hint about the expected stream length in bytes, or <code>-1</code> if unknown
     */
    public SaveFileAction(com.openexchange.filestore.FileStorage storage, InputStream data, long sizeHint) {
        this(storage, data, sizeHint, true);
    }

    /**
     * Initializes a new {@link SaveFileAction}.
     *
     * @param storage The storage to save the stream to
     * @param data The input stream
     * @param sizeHint A size hint about the expected stream length in bytes, or <code>-1</code> if unknown
     * @param calculateChecksum <code>true</code> to calculate a checksum for the saved data, <code>false</code>, otherwise
     */
    public SaveFileAction(com.openexchange.filestore.FileStorage storage, InputStream data, long sizeHint, boolean calculateChecksum) {
        super(storage, data, sizeHint, calculateChecksum);
    }

    @Override
    public String getFileStorageID() {
        return fileStorageID;
    }

    @Override
    protected void store(com.openexchange.filestore.FileStorage storage, InputStream stream) throws OXException {
        fileStorageID = storage.saveNewFile(stream);
    }

    @Override
    protected void store(com.openexchange.filestore.QuotaFileStorage storage, InputStream stream, long sizeHint) throws OXException {
        fileStorageID = storage.saveNewFile(stream, sizeHint);
    }

    @Override
    protected void undo(com.openexchange.filestore.FileStorage storage) throws OXException {
        storage.deleteFile(fileStorageID);
    }

}
