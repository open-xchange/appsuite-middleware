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

package com.openexchange.api.client.common.calls.folders;

/**
 * {@link RemoteFolderField}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public enum RemoteFolderField {

    // See com.openexchange.groupware.container.DataObject
    /**
     * The ID of the folder
     */
    ID(1, "id"),

    /**
     * The ID of the creator
     */
    CREATED_BY(2, "created_by"),

    /**
     * User ID of the user who last modified this object.
     */
    MODIFIED_BY(3, "modified_by"),

    /**
     * The date of creation
     */
    CREATION_DATE(4, "creation_date"),

    /**
     * The date of modification
     */
    LAST_MODIFIED(5, "last_modified"),

    // See com.openexchange.groupware.container.FolderChildObject
    /**
     * The object ID of the parent folder
     */
    FODLER_ID(20, "folder_id"),

    // See com.openexchange.groupware.container.FolderObject
    /**
     * The folder's title
     */
    TITLE(300, "title"),

    /**
     * The name of the module which implements this folder
     */
    MODULE(301, "module"),

    /**
     * True, if this folder has subfolders
     */
    SUBFOLDERS(304, "subfolders"),

    /**
     * Permissions which apply to the current user
     */
    OWN_RIGHTS(305, "own_rights"),

    /**
     * The folder's permission
     */
    PERMISSIONS(306, "permissions"),

    /**
     * Folder's subscription
     */
    SUBSCRIBED(314, "subscribed"),

    /**
     * Subscribed subfolders
     */
    SUBSCR_SUBFLDS(315, "subscr_subflds"),

    /**
     * Extended folder permissions
     */
    EXTENDED_PERMISSIONS(3060, "com.openexchange.share.extendedPermissions"),

    /**
     * Information about the entity that created the folder
     */
    CREATED_FROM(51, "created_from"),

    /**
     * Information about the entity that modified the folder
     */
    MODIFIED_FROM(52, "modified_from"),

    ;

    private final int column;

    private final String name;

    /**
     * Initializes a new {@link RemoteFolderField}.
     *
     * @param column The column ID
     * @param name The name of the field
     */
    private RemoteFolderField(int column, String name) {
        this.column = column;
        this.name = name;
    }

    /**
     *
     * Gets the column ID
     *
     * @return The column ID
     */
    public int getColumn() {
        return column;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }
}
