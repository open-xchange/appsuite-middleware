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
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.groupware.results.TimedResult;


/**
 * An {@link AdministrativeFileStorageFileAccess} can be used for administrative tasks when no
 * user session is available. Implementations will perform the provided calls without any
 * further permission checks then.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface AdministrativeFileStorageFileAccess {

    /**
     * Load the metadata about a file
     *
     * @param folderId The folder identifier
     * @param id The id of the file
     * @param version The version number of the file. May pass in {@link FileStorageFileAccess#CURRENT_VERSION} to load the current version
     * @return The File Metadata
     * @throws OXException If the file doesn't exist or an error occurs
     */
    File getFileMetadata(String folderId, String id, String version) throws OXException;

    /**
     * Saves the file metadata. The fields to consider as modified have to be specified with the <code>modifiedColumns</code> parameter.
     * Therefore it is even possible to override the <code>modified by</code> field. Otherwise the context admin will be used. The document
     * must always have the fields <code>folder id</code> and <code>id</code> set.
     *
     * @param document The metadata to save
     * @param sequenceNumber The sequence number to catch concurrent modification. May pass {@link FileStorageFileAccess#UNDEFINED_SEQUENCE_NUMBER} for new files
     * or {@link FileStorageFileAccess#DISTANT_FUTURE} to circumvent the check.
     * @param modifiedColumns The fields to save. All other fields will be ignored. Use {@link FileStorageFileAccess#ALL_FIELDS} if all fields shall be considered.
     * @throws OXException If the file doesn't exist or an error occurs
     */
    void saveFileMetadata(File document, long sequenceNumber, List<File.Field> modifiedColumns) throws OXException;

    /**
     * Removes the file with the given identifier. This method always results in a hard delete
     * of the according file.
     *
     * @param folderId The folder identifier
     * @param id The identifier
     * @throws OXException If the file doesn't exist or an error occurs
     */
    void removeDocument(String folderId, String id) throws OXException;

    /**
     * Removes the files with the given identifiers. This method always results in a hard delete
     * of the according files.
     *
     * @param ids The identifiers
     * @throws OXException If one of the files doesn't exist or an error occurs
     */
    void removeDocuments(List<IDTuple> ids) throws OXException;

    /**
     * Checks whether a certain version of a certain file exists. If you need to know if a file exists at all,
     * you can pass {@link FileStorageFileAccess#CURRENT_VERSION} as version.
     *
     * @param folderId The folder identifier
     * @param id The file identifier
     * @param version The file version
     * @throws OXException If a storage error occurs
     */
    boolean exists(String folderId, String id, String version) throws OXException;

    /**
     * Checks whether an entity can read a certain file.
     *
     * @param folderId The folder identifier
     * @param id The file identifier
     * @param userId The user identifier
     * @throws OXException If one of the files doesn't exist or an error occurs
     */
    boolean canRead(String folderId, String id, int userId) throws OXException;

    /**
     * Checks whether an entity can write a certain file.
     *
     * @param folderId The folder identifier
     * @param id The file identifier
     * @param userId The user identifier
     * @throws OXException If one of the files doesn't exist or an error occurs
     */
    boolean canWrite(String folderId, String id, int userId) throws OXException;

    /**
     * Checks whether an entity can delete a certain file.
     *
     * @param folderId The folder identifier
     * @param id The file identifier
     * @param userId The user identifier
     * @throws OXException If one of the files doesn't exist or an error occurs
     */
    boolean canDelete(String folderId, String id, int userId) throws OXException;

    /**
     * Loads the metadata for all files in a folder, as seen by a specific user.
     *
     * @param folderId The folder identifier
     * @param userId The user id
     * @param fields The metadata to return
     * @param sort The field to sort by
     * @param order The sorting direction
     * @param range The optional range
     * @return The documents
     */
    TimedResult<File> getDocuments(String folderId, int userId, List<Field> fields, Field sort, SortDirection order, Range range) throws OXException;

    /**
     * Updates a files sequence number
     *
     * @param folderId The folder identifier
     * @param id The file whose sequence number should be updated
     * @throws OXException If operation fails
     */
    void touch(String folderId, String id) throws OXException;

}
