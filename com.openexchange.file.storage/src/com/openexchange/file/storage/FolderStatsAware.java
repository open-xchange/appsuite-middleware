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

package com.openexchange.file.storage;

import com.openexchange.exception.OXException;

/**
 * {@link FolderStatsAware}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public interface FolderStatsAware {

    /**
     * Gets the total number of files in a folder.
     *
     * @param folderId The folder identifier
     * @return The number of files, or <code>-1</code> if unknown
     */
    long getNumFiles(String folderId) throws OXException;

    /**
     * Gets the total size of all files (and file versions) in a folder.
     *
     * @param folderId The folder identifier
     * @return The total size of all document versions in a folder, or <code>-1</code> if unknown
     */
    long getTotalSize(String folderId) throws OXException;

}
