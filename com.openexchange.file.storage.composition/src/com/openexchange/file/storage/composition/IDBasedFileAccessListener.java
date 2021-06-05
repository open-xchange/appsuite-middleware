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

package com.openexchange.file.storage.composition;

import java.io.InputStream;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.osgi.annotation.Service;
import com.openexchange.session.Session;

/**
 * {@link IDBasedFileAccessListener} - A listener receiving call-backs on invocations of {@link IDBasedFileAccess} instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
@Service
public interface IDBasedFileAccessListener {

    /**
     * Invoked before a new file is created.
     *
     * @param document The file meta-data
     * @param data The binary content
     * @param sequenceNumber The sequence number
     * @param modifiedColumns The modified columns
     * @param fileAccess The file access which is supposed to be used
     * @param session The associated session
     * @return FileStorageFileAcess
     */
    FileStorageFileAccess onBeforeNewFile(File document, InputStream data, long sequenceNumber, List<Field> modifiedColumns, FileStorageFileAccess fileAccess, Session session) throws OXException;

    /**
     * Invoked before an existing file is modified.
     *
     * @param document The file meta-data
     * @param data The binary content
     * @param sequenceNumber The sequence number
     * @param modifiedColumns The modified columns
     * @param isMove <code>true</code> if update implies a move; otherwise <code>false</code>
     * @param fileAccess The file access which is supposed to be used
     * @param session The associated session
     * @return FileStorageFileAcess
     */
    FileStorageFileAccess onBeforeUpdateFile(File document, InputStream data, long sequenceNumber, List<Field> modifiedColumns, boolean isMove, FileStorageFileAccess fileAccess, Session session) throws OXException;

    /**
     * Invoked before multiple files are moved.
     *
     * @param sourceIds The file identifiers
     * @param sequenceNumber The sequence number
     * @param destFolderId The identifier of the destination folder
     * @param fileAccess The file access which is supposed to be used
     * @param session The associated session
     * @return FileStorageFileAcess
     */
    FileStorageFileAccess onBeforeMoveFiles(List<String> sourceIds, long sequenceNumber, String destFolderId, FileStorageFileAccess fileAccess, Session session) throws OXException;

    /**
     * Invoked before an existent file is copied.
     *
     * @param sourceId The file identifier
     * @param version The version to consider for copy
     * @param destFolderId The identifier of the destination folder
     * @param update The updated meta-data
     * @param newData The possibly new binary data
     * @param fields The modified fields
     * @param fileAccess The file access which is supposed to be used
     * @param session The associated session
     * @return FileStorageFileAcess
     */
    FileStorageFileAccess onBeforeCopyFile(String sourceId, String version, String destFolderId, File update, InputStream newData, List<Field> fields, FileStorageFileAccess fileAccess, Session session) throws OXException;

    /**
     * Invoked before multiple files are deleted.
     *
     * @param  The file identifiers
     * @param sequenceNumber The sequence number
     * @param hardDelete <code>true</code> for hard-delete; otherwise <code>false</code>
     * @param fileAccess The file access which is supposed to be used
     * @param session The associated session
     */
    void onBeforeDeleteFiles(List<IDTuple> ids, long sequenceNumber, boolean hardDelete, FileStorageFileAccess fileAccess, Session session) throws OXException;

    /**
     * Invoked before all files of a certain folder are deleted.
     *
     * @param folderId The folder identifier
     * @param sequenceNumber The sequence number
     * @param fileAccess The file access which is supposed to be used
     * @param session The associated session
     */
    void onBeforeDeleteAllFilesInFolder(String folderId, long sequenceNumber, FileStorageFileAccess fileAccess, Session session) throws OXException;

}
