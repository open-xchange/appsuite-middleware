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
 * {@link FolderPermissionType} defines available permission types for folders.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public enum FolderPermissionType {
    /**
     * The normal permission type
     */
    NORMAL(0),
    /**
     * Permissions of this type are going to be handed down to sub-folders
     */
    LEGATOR(1),
    /**
     * Permissions of this type are inherited permissions of a {@link FolderPermissionType#LEGATOR} permission
     */
    INHERITED(2);

    private final int type;

    /**
     * Initializes a new {@link FolderPermissionType}.
     */
    private FolderPermissionType(int type) {
        this.type = type;
    }

    /**
     * Returns the identifying number of this type
     *
     * @return the type number
     */
    public int getTypeNumber() {
        return type;
    }

    /**
     * Return the corresponding {@link FolderPermissionType} with the given type number or the {@link FolderPermissionType#NORMAL} type in case the given type number is unknown.
     * @param type The type number
     * @return The {@link FolderPermissionType}
     */
    public static FolderPermissionType getType(int type) {
        switch (type) {
            case 0:
                return NORMAL;
            case 1:
                return LEGATOR;
            case 2:
                return INHERITED;
            default:
                return NORMAL;
        }
    }
}
