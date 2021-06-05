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
 * {@link PermissionAware} - Implementor is aware of permissions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface PermissionAware extends FileStorageFolderAccess {

    /**
     * Updates an existing file storage folder identified through given identifier. All attributes set in given file storage folder instance are
     * applied.
     * <p>
     * The currently known attributes that make sense being updated are:
     * <ul>
     * <li>permissions</li>
     * <li>subscription</li>
     * </ul>
     * Of course more folder attributes may be checked by implementation to enhance update operations.
     * <p>
     * <b>Note</b>: If underlying file storage system does not support the corresponding capability, the update is treated as a no-op.
     *
     * @param identifier The identifier of the file storage folder to update
     * @param toUpdate The file storage folder to update containing only the modified fields
     * @param cascadePermissions <code>true</code> to apply permission changes to all subfolders, <code>false</code>, otherwise
     * @return The identifier of the updated file storage folder
     * @throws OXException If either folder does not exist or cannot be updated
     */
    String updateFolder(String identifier, FileStorageFolder toUpdate, boolean cascadePermissions) throws OXException;

    /**
     * Updates an existing file storage folder identified through given identifier. All attributes set in given file storage folder instance are
     * applied.
     * <p>
     * The currently known attributes that make sense being updated are:
     * <ul>
     * <li>permissions</li>
     * <li>subscription</li>
     * </ul>
     * Of course more folder attributes may be checked by implementation to enhance update operations.
     * <p>
     * <b>Note</b>: If underlying file storage system does not support the corresponding capability, the update is treated as a no-op.
     *
     * @param ignoreWarnings indicates whether warnings should be ignored or not
     * @param identifier The identifier of the file storage folder to update
     * @param toUpdate The file storage folder to update containing only the modified fields
     * @param cascadePermissions <code>true</code> to apply permission changes to all subfolders, <code>false</code>, otherwise
     * @return The identifier of the updated file storage folder
     * @throws OXException If either folder does not exist or cannot be updated
     */
    default FileStorageResult<String> updateFolder(@SuppressWarnings("unused") boolean ignoreWarnings, String identifier, FileStorageFolder toUpdate, boolean cascadePermissions) throws OXException {
        return FileStorageResult.newFileStorageResult(updateFolder(identifier, toUpdate, cascadePermissions), null);
    }

    /**
     * Moves the folder identified through given identifier to the parent specified through argument <code>newParentId</code>.
     * <p>
     * E.g.:
     *
     * <pre>
     * my.path.to.folder -&gt; my.newpath.to.folder
     * </pre>
     *
     * @param ignoreWarnings true to force the folder move even if warnings are detected, false, otherwise
     * @param folderId The folder identifier
     * @param newParentId The identifier of the new parent to move to
     * @param newName The new name to use for the folder, or <code>null</code> to keep the existing name
     * @return The new identifier where the folder has been moved
     * @throws OXException If either folder does not exist or cannot be moved
     */
    default FileStorageResult<String> moveFolder(boolean ignoreWarnings, String folderId, String newParentId, String newName) throws OXException {
        return FileStorageResult.newFileStorageResult(moveFolder(folderId, newParentId, newName), null);
    }
}
