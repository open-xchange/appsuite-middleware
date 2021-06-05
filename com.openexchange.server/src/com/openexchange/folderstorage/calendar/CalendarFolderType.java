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

package com.openexchange.folderstorage.calendar;

import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;

/**
 * {@link CalendarFolderType}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarFolderType implements FolderType {

    private static final String CAL_PREFIX = "cal://";

    /**
     * Initializes a new {@link CalendarFolderType}.
     */
    CalendarFolderType() {
        super();
    }

    @Override
    public boolean servesTreeId(String treeId) {
        return FolderStorage.REAL_TREE_ID.equals(treeId);
    }

    @Override
    public boolean servesParentId(String folderId) {
        return FolderStorage.PRIVATE_ID.equals(folderId) || FolderStorage.SHARED_ID.equals(folderId) || FolderStorage.PUBLIC_ID.equals(folderId) || servesFolderId(folderId);
    }

    @Override
    public boolean servesFolderId(String folderId) {
        if (null == folderId) {
            return false;
        }
        /*
         * Check if a real provider is defined
         */
        return folderId.startsWith(CAL_PREFIX);
    }

}
