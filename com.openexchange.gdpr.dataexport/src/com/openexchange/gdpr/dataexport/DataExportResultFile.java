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

package com.openexchange.gdpr.dataexport;

import java.util.UUID;

/**
 * {@link DataExportResultFile} - A data export for a certain user.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public interface DataExportResultFile {

    /**
     * Gets the task identifier.
     *
     * @return The task identifier
     */
    UUID getTaskId();

    /**
     * Gets the content type; e.g. <code>"application/zip"</code>.
     *
     * @return The content type or <code>null</code> if unknown
     */
    String getContentType();

    /**
     * Gets the package number.
     *
     * @return The package number
     */
    int getNumber();

    /**
     * Gets the file name; e.g. <code>"export_0001.zip"</code>.
     *
     * @return The file name
     */
    String getFileName();

    /**
     * Gets the identifier for the location in the file storage.
     *
     * @return The file storage location
     */
    String getFileStorageLocation();

    /**
     * Gets the size in bytes of the result file.
     *
     * @return The size
     */
    long getSize();

}
