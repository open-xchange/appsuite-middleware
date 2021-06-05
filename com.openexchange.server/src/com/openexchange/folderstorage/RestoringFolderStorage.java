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

package com.openexchange.folderstorage;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link RestoringFolderStorage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface RestoringFolderStorage extends FolderStorage {

    /**
     * Restores a folder from trash to its original location.
     *
     * @param treeId The tree identifier
     * @param folderIds The identifiers of the folders to restore
     * @param defaultDestFolderId The identifier of the default destination folder
     * @param storageParameters The storage parameters
     * @return A mapping of restored folder to new parent folder
     * @throws OXException If restore fails
     */
    Map<String, String> restoreFromTrash(String treeId, List<String> folderIds, String defaultDestFolderId, StorageParameters storageParameters) throws OXException;

}
