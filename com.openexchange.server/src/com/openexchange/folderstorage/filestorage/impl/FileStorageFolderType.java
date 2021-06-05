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

package com.openexchange.folderstorage.filestorage.impl;

import java.util.List;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.filestorage.osgi.Services;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link FileStorageFolderType} - The folder type for file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStorageFolderType implements FolderType {

    /**
     * The private folder identifier.
     */
    private static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    private static final FileStorageFolderType instance = new FileStorageFolderType();

    /**
     * Gets the {@link FileStorageFolderType} instance.
     *
     * @return The {@link FileStorageFolderType} instance
     */
    public static FileStorageFolderType getInstance() {
        return instance;
    }

    /*-
     * Member stuff
     */

    /**
     * Initializes a new {@link FileStorageFolderType}.
     */
    private FileStorageFolderType() {
        super();
    }

    @Override
    public boolean servesTreeId(final String treeId) {
        return FolderStorage.REAL_TREE_ID.equals(treeId);
    }

    @Override
    public boolean servesFolderId(final String folderId) {
        if (null == folderId) {
            return false;
        }
        // Check if a real service is defined
        final List<String> components = IDMangler.unmangle(folderId);
        if (2 > components.size()) {
            return false;
        }
        /*
         * Check if service exists
         */
        String serviceID = new FolderID(folderId).getService();
        final FileStorageServiceRegistry registry = Services.getService(FileStorageServiceRegistry.class);
        return null != registry && registry.containsFileStorageService(serviceID);
    }

    @Override
    public boolean servesParentId(final String folderId) {
        if (null == folderId) {
            return false;
        }
        if (PRIVATE_FOLDER_ID.equals(folderId)) {
            return true;
        }
        return servesFolderId(folderId);
    }

}
