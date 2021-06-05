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

/**
 * {@link FolderStorageDiscoverer} - The folder storage discovery.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FolderStorageDiscoverer {

    /**
     * Gets the folder storage for specified tree-folder-pair.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @return The folder storage for specified tree-folder-pair or <code>null</code>
     */
    FolderStorage getFolderStorage(String treeId, String folderId);

    /**
     * Gets the folder storages for specified tree-parent-pair.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @return The folder storages for specified tree-parent-pair or an empty array if none available
     */
    FolderStorage[] getFolderStoragesForParent(String treeId, String parentId);

    /**
     * Gets the folder storages for specified tree identifier.
     *
     * @param treeId The tree identifier
     * @return The folder storages for specified tree identifier or an empty array if none available
     */
    FolderStorage[] getFolderStoragesForTreeID(String treeId);

    /**
     * Gets the tree folder storages. No cache folder storage is returned.
     *
     * @param treeId The tree identifier
     * @return The tree folder storages or an empty array if none available
     */
    FolderStorage[] getTreeFolderStorages(String treeId);

    /**
     * Gets the folder storage capable to handle given content type in specified tree.
     *
     * @param treeId The tree identifier
     * @param contentType The content type
     * @return The folder storage capable to handle given content type in specified tree
     */
    FolderStorage getFolderStorageByContentType(String treeId, ContentType contentType);

}
