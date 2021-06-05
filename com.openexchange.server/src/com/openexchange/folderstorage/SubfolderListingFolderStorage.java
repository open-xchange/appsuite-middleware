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

import com.openexchange.exception.OXException;

/**
 * {@link SubfolderListingFolderStorage} - Extends folder storage by a listing of sub-folder instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface SubfolderListingFolderStorage extends FolderStorage {

    /**
     * Gets the subfolder instances for specified parent which are visible to storage parameter's entity.
     *
     * @param treeId The tree identifier
     * @param parentId The parent identifier
     * @param storageParameters The storage parameters
     * @return The subfolder instances for specified parent or an empty array if parent identifier cannot be served
     * @throws OXException If returning the subfolder identifiers fails
     */
    Folder[] getSubfolderObjects(String treeId, String parentId, StorageParameters storageParameters) throws OXException;

}
