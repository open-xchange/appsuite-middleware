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

package com.openexchange.gdpr.dataexport.impl;

import java.io.InputStream;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.gdpr.dataexport.DataExportDownload;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.FileLocation;

/**
 * {@link DataExportImplementation}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class FileStorageDataExportDownload implements DataExportDownload {

    private final FileLocation fileLocation;
    private final String fileName;
    private final DataExportTask task;

    /**
     * Initializes a new {@link FileStorageDataExportDownload}.
     *
     * @param fileLocation The file location
     * @param task The associated task
     * @param fileName The name of the ZIP archive
     */
    public FileStorageDataExportDownload(FileLocation fileLocation, DataExportTask task, String fileName) {
        this.fileLocation = fileLocation;
        this.fileName = fileName;
        this.task = task;
    }

    @Override
    public UUID getTaskId() {
        return task.getId();
    }

    @Override
    public InputStream getInputStream() throws OXException {
        // Determine the file storage to use
        FileStorage fileStorage = DataExportUtility.getFileStorageFor(task);

        // Grab file
        return fileStorage.getFile(fileLocation.getFileStorageLocation());
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return "application/zip";
    }
}