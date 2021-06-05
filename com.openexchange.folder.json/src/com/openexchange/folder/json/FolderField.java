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

package com.openexchange.folder.json;

/**
 * {@link FolderField} - Enumeration for folder fields.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum FolderField {

    /**
     * The folder identifier
     */
    ID(1, "id"),
    /**
     * The entity which created this folder
     */
    CREATED_BY(2, "created_by"),
    /**
     * The entity which modified this folder last time
     */
    MODIFIED_BY(3, "modified_by"),
    /**
     * The creation time stamp in requesting session's user time zone
     */
    CREATION_DATE(4, "creation_date"),
    /**
     * The last-modified time stamp in requesting session's user time zone
     */
    LAST_MODIFIED(5, "last_modified"),
    /**
     * The last-modified time stamp in UTC
     */
    LAST_MODIFIED_UTC(6, "last_modified_utc"),
    /**
     * The folder's parent folder identifier
     */
    FOLDER_ID(20, "folder_id"),
    /**
     * The folder name
     */
    FOLDER_NAME(300, "title"),
    /**
     * The folder module
     */
    MODULE(301, "module"),
    /**
     * The folder type
     */
    TYPE(302, "type"),
    /**
     * A boolean to indicate if folder contains subfolders
     */
    SUBFOLDERS(304, "subfolders"),
    /**
     * The rights for requesting session's user
     */
    OWN_RIGHTS(305, "own_rights"),
    /**
     * The permissions added to folder
     */
    PERMISSIONS_BITS(306, "permissions"),
    /**
     * The summary string
     */
    SUMMARY(307, "summary"),
    /**
     * A boolean indicating if this folder is a default folder
     */
    STANDARD_FOLDER(308, "standard_folder"),
    /**
     * The total number of objects held by this folder
     */
    TOTAL(309, "total"),
    /**
     * The number of new objects held by this folder
     */
    NEW(310, "new"),
    /**
     * The number of unread objects held by this folder
     */
    UNREAD(311, "unread"),
    /**
     * The number of deleted objects held by this folder
     */
    DELETED(312, "deleted"),
    /**
     * The folder's capabilities
     */
    CAPABILITIES(313, "capabilities"),
    /**
     * Folder's subscription
     */
    SUBSCRIBED(314, "subscribed"),
    /**
     * Subscribed subfolders
     */
    SUBSCR_SUBFLDS(315, "subscr_subflds"),
    /**
     * An integer denoting the default folder type.
     */
    STANDARD_FOLDER_TYPE(316, "standard_folder_type"),
    /**
     * The folder's supported capabilities
     */
    SUPPORTED_CAPABILITIES(317, "supported_capabilities"),
    /**
     * The folders account ID
     */
    ACCOUNT_ID(318, "account_id"),
    /**
     * The folders raw name
     */
    FOLDER_NAME_RAW(319, "folder_name"),
    /**
     * The folder's origin path
     */
    ORIGIN(320, "origin"),
    /**
     * The folder's origin path
     */
    USED_FOR_SYNC(321, "used_for_sync"),
    /**
     * The permissions bits
     */
    BITS(-1, "bits"),
    /**
     * The permission's identifier
     */
    IDENTIFIER(-1, "identifier"),
    /**
     * The permission's entity
     */
    ENTITY(-1, "entity"),
    /**
     * The permission's group flag
     */
    GROUP(-1, "group"),
    /**
     * Mail address for an external permission
     */
    EMAIL_ADDRESS(-1, "email_address"),
    /**
     * Contact id for an external permission
     */
    CONTACT_ID(-1, "contact_id"),
    /**
     * Contact folder id for an external permission
     */
    CONTACT_FOLDER_ID(-1, "contact_folder"),
    /**
     * The date when an external permission should expire
     */
    EXPIRY_DATE(-1, "expiry_date"),
    /**
     * The date when an external permission should become active
     */
    ACTIVATION_DATE(-1, "activation_date"),
    /**
     * Display name for an external permission
     */
    DISPLAY_NAME(-1, "display_name"),
    /**
     * Password for an external permission
     */
    PASSWORD(-1, "password"),
    /**
     * The meta field
     */
    META(23, "meta"),
    /**
     * The created_from field
     */
    CREATED_FROM(51, "created_from"),
    /**
     * The modified_from field
     */
    MODIFIED_FROM(52, "modified_from"),
    ;

    private final int column;

    private final String name;

    private FolderField(final int column, final String name) {
        this.column = column;
        this.name = name;
    }

    /**
     * Gets the column or <code>-1</code> if none available
     *
     * @return The column or <code>-1</code> if none available
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
