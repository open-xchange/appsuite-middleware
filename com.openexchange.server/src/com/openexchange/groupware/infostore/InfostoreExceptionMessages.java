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

package com.openexchange.groupware.infostore;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link InfostoreExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class InfostoreExceptionMessages implements LocalizableStrings {

    // The document you requested does not exist.
    public static final String NOT_EXIST_MSG_DISPLAY = "The document you requested does not exist.";

    // Unable to load the document(s). Please try again.
    public static final String COULD_NOT_LOAD_MSG_DISPLAY = "Unable to load the document(s). Please try again.";

    // The folder %1$s you requested is not an Infostore folder.
    public static final String NOT_INFOSTORE_FOLDER_MSG_DISPLAY = "The folder %1$s you requested is not an Infostore folder.";

    // You do not have the appropriate permissions to read the document.
    public static final String NO_READ_PERMISSION_MSG_DISPLAY = "You do not have the appropriate permissions to read the document.";

    // You do not have the appropriate permissions to create a document in this folder.
    public static final String NO_CREATE_PERMISSION_MSG_DISPLAY = "You do not have the appropriate permissions to create a document in this folder.";

    // You do not have the appropriate permissions to update the document.
    public static final String NO_WRITE_PERMISSION_MSG_DISPLAY = "You do not have the appropriate permissions to update the document.";

    // Unable to delete all documents. Please try again.
    public static final String NOT_ALL_DELETED_MSG_DISPLAY = "Unable to delete all documents. Please try again.";

    // You do not have the appropriate permissions to delete this version.
    public static final String NO_DELETE_PERMISSION_FOR_VERSION_MSG_DISPLAY = "You do not have the appropriate permissions to delete this version.";

    // The document is already locked. Please try again later.
    public static final String CURRENTLY_LOCKED_MSG_DISPLAY = "The document is currently locked. Please try again later.";

    // You do not have the appropriate permissions to unlock the document.
    public static final String WRITE_PERMS_FOR_UNLOCK_MISSING_MSG_DISPLAY = "You do not have the appropriate permissions to unlock the document.";

    // You do not have the appropriate permissions to lock a document.
    public static final String WRITE_PERMS_FOR_LOCK_MISSING_MSG_DISPLAY = "You do not have the appropriate permissions to lock a document.";

    // You do not have the appropriate permissions to move the document.
    public static final String NO_SOURCE_DELETE_PERMISSION_MSG_DISPLAY = "You do not have the appropriate permissions to move the document.";

    // The document you requested does not exist.
    public static final String DOCUMENT_NOT_EXISTS_MSG_DISPLAY = "The document you requested does not exist.";

    // The document name \"%1$s\" already exists. Please choose another.
    public static final String FILENAME_NOT_UNIQUE_MSG_DISPLAY = "The document name \"%1$s\" already exists. Please choose another.";

    // You do not have the appropriate permissions to delete a document.
    public static final String NO_DELETE_PERMISSION_MSG_DISPLAY = "You do not have the appropriate permissions to delete a document.";

    // In order to accomplish the search, %1$s or more characters are required.
    public static final String PATTERN_NEEDS_MORE_CHARACTERS_MSG_DISPLAY = "In order to accomplish the search, %1$s or more characters are required.";

    // The document could not be updated because it was modified. Please try again.
    public static final String MODIFIED_CONCURRENTLY_MSG_DISPLAY = "The document could not be updated because it was modified. Please try again.";

    // The file name must not contain slashes.
    public static final String VALIDATION_FAILED_SLASH_MSG_DISPLAY = "The file name must not contain slashes.";

    // The file name contains invalid characters.
    public static final String VALIDATION_FAILED_CHARACTERS_MSG_DISPLAY = "The file name contains invalid characters.";

    // New file versions can't be saved with an offset.
    public static final String NO_OFFSET_FOR_NEW_VERSIONS_MSG_DISPLAY = "New file versions can't be saved with an offset.";

    // This folder is a virtual folder that cannot contain documents. Please choose another folder.
    public static final String NO_DOCUMENTS_IN_VIRTUAL_FOLDER_MSG_DISPLAY = "This folder is a virtual folder that cannot contain documents. Please choose another folder.";

    // Unsupported character "%1$s" in field "%2$s". Please remove that character.
    public static final String INVALID_CHARACTER_MSG_DISPLAY = "Unsupported character \"%1$s\" in field \"%2$s\". Please remove that character.";

    // Unsupported character. Please correct your input.
    public static final String INVALID_CHARACTER_SIMPLE_MSG_DISPLAY = "Unsupported character. Please correct your input.";

    // Due to limited capabilities of user \"%1$s\", it is not possible to apply the permission changes.
    public static final String VALIDATION_FAILED_INAPPLICABLE_PERMISSIONS_MSG_DISPLAY = "Due to limited capabilities of user \"%1$s\", it is not possible to apply the permission changes.";

    // Group %1$s can't be used for object permissions.
    public static final String VALIDATION_FAILED_INAPPLICABLE_PERMISSIONS_GUEST_GROUP_MSG_DISPLAY = "Group %1$s can't be used for object permissions.";

    // Concurrent write attempt. Please await the previous save operation to terminate.
    public static final String CONCURRENT_VERSION_CREATION_MSG_DISPLA = "Concurrent write attempt. Please await the previous save operation to terminate.";

    // Your search took too long in order to be accomplished.
    public static final String SEARCH_TOOK_TOO_LONG_MSG_DISPLAY = "Your search took too long in order to be accomplished.";

    // The folder you requested does not exist.
    public static final String FOLDER_NOT_EXISTS_MSG = "The folder you requested does not exist.";

    // The file contains too many permissions. Please reduce the number or use groups instead.
    public static final String TOO_MANY_PERMISSIONS = "The file contains too many permissions. Please reduce the number of permissions or use group permissions instead.";

    private InfostoreExceptionMessages() {
        super();
    }
}
