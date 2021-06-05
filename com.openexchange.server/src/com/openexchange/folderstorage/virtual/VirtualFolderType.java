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

package com.openexchange.folderstorage.virtual;

import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.outlook.OutlookFolderStorage;

/**
 * {@link VirtualFolderType} - The virtual folder type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VirtualFolderType implements FolderType {

    private static final VirtualFolderType INSTANCE = new VirtualFolderType();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static VirtualFolderType getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link VirtualFolderType}.
     */
    private VirtualFolderType() {
        super();
    }

    @Override
    public boolean servesFolderId(final String folderId) {
        // A virtual storage serves every folder ID except null
        return (null != folderId);
    }

    private static final Set<String> KNOWN_TREES = ImmutableSet.of(
        FolderStorage.REAL_TREE_ID,
        OutlookFolderStorage.OUTLOOK_TREE_ID);

    @Override
    public boolean servesTreeId(final String treeId) {
        return !KNOWN_TREES.contains(treeId);
    }

    @Override
    public boolean servesParentId(final String parentId) {
        return servesFolderId(parentId);
    }

}
