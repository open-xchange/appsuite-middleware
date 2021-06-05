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

package com.openexchange.groupware.infostore.media;

import java.io.InputStream;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;


/**
 * {@link FileStorageInputStreamProvider} - Provides an input stream from a file storage resource.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class FileStorageInputStreamProvider implements InputStreamProvider {

    private final String fileStorageLocation;
    private final FileStorage fileStorage;

    /**
     * Initializes a new {@link FileStorageInputStreamProvider}.
     *
     * @param fileStorageLocation The identifier of the file resource in the given storage
     * @param fileStorage The file storage
     */
    public FileStorageInputStreamProvider(String fileStorageLocation, FileStorage fileStorage) {
        super();
        this.fileStorageLocation = fileStorageLocation;
        this.fileStorage = fileStorage;

    }

    @Override
    public InputStream getInputStream() throws OXException {
        return fileStorage.getFile(fileStorageLocation);
    }

}
