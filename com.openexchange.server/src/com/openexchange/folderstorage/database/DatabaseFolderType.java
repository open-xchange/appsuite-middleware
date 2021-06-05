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

package com.openexchange.folderstorage.database;

import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;

/**
 * {@link DatabaseFolderType} - The database folder type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DatabaseFolderType implements FolderType {

    private static final String STRING = "DB-Type";

    private static final DatabaseFolderType instance = new DatabaseFolderType();

    /**
     * Gets the {@link DatabaseFolderType} instance.
     *
     * @return The {@link DatabaseFolderType} instance
     */
    public static DatabaseFolderType getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link DatabaseFolderType}.
     */
    private DatabaseFolderType() {
        super();
    }

    @Override
    public boolean servesTreeId(final String treeId) {
        return FolderStorage.REAL_TREE_ID.equals(treeId);
    }

    @Override
    public boolean servesFolderId(final String folderId) {
        return com.openexchange.java.util.Tools.getUnsignedInteger(folderId) >= 0 || DatabaseFolderStorageUtility.hasSharedPrefix(folderId);
    }

    @Override
    public boolean servesParentId(final String parentId) {
        return servesFolderId(parentId);
    }

    @Override
    public String toString() {
        return STRING;
    }

}
