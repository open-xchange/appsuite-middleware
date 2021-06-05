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

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.groupware.results.TimedResult;

/**
 * {@link FileStorageVersionedFileAccess}.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface FileStorageVersionedFileAccess {

    /**
     * Removes a certain version of a file
     *
     * @param folderId The folder identifier
     * @param id The file id whose version is to be removed
     * @param versions The versions to be remvoed. The versions that couldn't be removed are returned again.
     * @return The IDs of versions that could not be deleted due to an edit-delete conflict
     * @throws OXException If operation fails
     */
    String[] removeVersion(String folderId, String id, String[] versions) throws OXException;

    /**
     * Lists all versions of a file
     *
     * @param folderId The folder identifier
     * @param id The file's identifier
     * @return All versions of a file
     * @throws OXException If operation fails
     */
    TimedResult<File> getVersions(String folderId, String id) throws OXException;

    /**
     * List all versions of a file loading the given fields
     *
     * @param folderId The folder identifier
     * @param id The file's identifier
     * @param fields The fields to load
     * @return All versions of a file with given fields loaded
     * @throws OXException If operation fails
     */
    TimedResult<File> getVersions(String folderId, String id, List<File.Field> fields) throws OXException;

    /**
     * Lists all versions of a file loading the given fields sorted according to the given field in a given order
     *
     * @param folderId The folder identifier
     * @param id The file's identifier
     * @param fields The fields to load
     * @return All sorted versions of a file with given fields loaded
     * @throws OXException If operation fails
     */
    TimedResult<File> getVersions(String folderId, String id, List<File.Field> fields, File.Field sort, SortDirection order) throws OXException;

}
