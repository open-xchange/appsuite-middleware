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
 * {@link SearchableFolderNameFolderAccess} - Extends the folder access by searching folders by name.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public interface SearchableFolderNameFolderAccess extends FileStorageFolderAccess {

    /**
     * Searches a folder below given folder identifier by folder name
     *
     * @param query The query to search
     * @param folderId The 'root' folder for search operation
     * @param date The time stamp to limit search result to folders that are newer
     * @param includeSubfolders Include all subfolders below given folder identifier
     * @param all Whether all or only subscribed subfolders shall be returned. If underlying file storage system does not support folder
     *            subscription, this argument should always be treated as <code>true</code>
     * @param start A start index (inclusive) for the search results. Useful for paging.
     * @param end An end index (exclusive) for the search results. Useful for paging.
     * @return Array of {@link FileStorageFolder} sorted by name
     * @throws OXException If search fails
     */
    FileStorageFolder[] searchFolderByName(String query, String folderId, long date, boolean includeSubfolders, boolean all, int start, int end) throws OXException;

}
