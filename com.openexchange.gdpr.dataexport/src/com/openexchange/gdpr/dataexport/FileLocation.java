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
 * {@link FileLocation} - Represents a result file of a completed data export task.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public interface FileLocation {

    /**
     * Gets the size in bytes of the result file.
     *
     * @return The size
     */
    long getSize();

    /**
     * Gets this result file's number.
     *
     * @return The number
     */
    int getNumber();

    /**
     * Gets the identifier for the location in the file storage.
     *
     * @return The file storage location
     */
    String getFileStorageLocation();

    /**
     * Gets the identifier of the associated data export task.
     *
     * @return The task identifier
     */
    UUID getTaskId();
}
