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
 * {@link FileStorageAutoRenameFoldersAccess} - Marks whether file storage automatically renames folder names to prevent from conflicts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface FileStorageAutoRenameFoldersAccess {

    /**
     * Creates a new file storage folder with attributes taken from given file storage folder description
     *
     * @param toCreate The file storage folder to create
     * @param autoRename <code>true</code> to rename the folder (e.g. adding <code>" (1)"</code> appendix) to avoid conflicts; otherwise <code>false</code>
     * @return The identifier of the created file storage folder
     * @throws OXException If creation fails
     */
    String createFolder(FileStorageFolder toCreate, boolean autoRename) throws OXException;

    /**
     * Moves the folder identified through given identifier to the parent specified through argument <code>newParentId</code>, renaming
     * it to the supplied new name.
     * <p>
     * E.g.:
     *
     * <pre>
     * my.path.to.folder -&gt; my.newpath.to.folder
     * </pre>
     *
     * @param folderId The folder identifier
     * @param newParentId The identifier of the new parent to move to
     * @param newName The new name to use for the folder, or <code>null</code> to keep the existing name
     * @param autoRename <code>true</code> to rename the folder (e.g. adding <code>" (1)"</code> appendix) to avoid conflicts; otherwise <code>false</code>
     * @return The new identifier where the folder has been moved
     * @throws OXException If either folder does not exist or cannot be moved
     */
    String moveFolder(String folderId, String newParentId, String newName, boolean autoRename) throws OXException;

}
