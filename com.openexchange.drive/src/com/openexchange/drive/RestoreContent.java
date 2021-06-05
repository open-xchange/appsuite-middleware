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

package com.openexchange.drive;

import java.util.Map;
import com.openexchange.file.storage.FileStorageFolder;

/**
 * {@link RestoreContent}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class RestoreContent {

    private final Map<String, FileStorageFolder[]> restoredFolders;
    private final Map<String, FileStorageFolder[]> restoredFiles;

    /**
     * Initializes a new {@link RestoreContent}.
     */
    public RestoreContent(Map<String, FileStorageFolder[]> restoredFolders, Map<String, FileStorageFolder[]> restoredFiles) {
        super();
        this.restoredFolders = restoredFolders;
        this.restoredFiles = restoredFiles;
    }

    /**
     * Gets the restoredFolders
     *
     * @return The restoredFolders
     */
    public Map<String, FileStorageFolder[]> getRestoredFolders() {
        return restoredFolders;
    }

    /**
     * Gets the restoredFiles
     *
     * @return The restoredFiles
     */
    public Map<String, FileStorageFolder[]> getRestoredFiles() {
        return restoredFiles;
    }



}
