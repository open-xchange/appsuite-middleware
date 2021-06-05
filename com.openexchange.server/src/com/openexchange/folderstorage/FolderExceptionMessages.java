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

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link FolderExceptionMessages} - Locale-sensitive strings for folder exceptions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderExceptionMessages implements LocalizableStrings {

    // You do not have a valid session. Please login again.
    public static final String MISSING_SESSION_MSG_DISPLAY = "You do not have a valid session. Please login again.";

    // You do not have appropriate permissions to view the folder.
    public static final String FOLDER_NOT_VISIBLE_MSG_DISPLAY = "You do not have appropriate permissions to view the folder.";

    // "The content type you provided is not allowed for the given folder.";
    public static final String INVALID_CONTENT_TYPE_MSG_DISPLAY = "The content type you provided is not allowed for the given folder.";

    // You do not have appropriate permissions to move the folder.
    public static final String MOVE_NOT_PERMITTED_MSG_DISPLAY = "You do not have appropriate permissions to move the folder.";

    // A folder named "%1$s" already exists.
    public static final String EQUAL_NAME_MSG_DISPLAY = "A folder named \"%1$s\" already exists.";

    // The folder you requested does not exist.
    public static final String NOT_FOUND_MSG_DISPLAY = "The folder you requested does not exist.";

    // You do not have the appropriate permissions to delete the folder.
    public static final String FOLDER_NOT_DELETEABLE_MSG_DISPLAY = "You do not have the appropriate permissions to delete the folder.";

    // You do not have the appropriate permissions to move the folder.
    public static final String FOLDER_NOT_MOVEABLE_MSG_DISPLAY = "You do not have the appropriate permissions to move the folder.";

    // You do not have the appropriate permissions to create a subfolder.
    public static final String NO_CREATE_SUBFOLDERS_MSG_DISPLAY = "You do not have the appropriate permissions to create a subfolder.";

    // It is not allowed to create a mail folder allowed below a public folder.
    public static final String NO_PUBLIC_MAIL_FOLDER_MSG_DISPLAY = "It is not allowed to create a mail folder below a public folder.";

    // The folder name "%1$s" is reserved. Please choose another name.
    public static final String RESERVED_NAME_MSG_DISPLAY = "The folder name \"%1$s\" is reserved. Please choose another name.";

    // Found two folders named "%1$s" located below the parent folder. Please rename one of the folders. There should be no two folders with the same name.
    public static final String DUPLICATE_NAME_MSG_DISPLAY = "Found two folders named \"%1$s\" located below the parent folder. Please rename one of the folders. There should be no two folders with the same name.";

    // Failed to delete all folders
    public static final String FOLDER_DELETION_FAILED_MSG_DISPLAY = "Failed to delete all folders";

    // The folder was not updated. Please review the warnings for details.
    public static final String FOLDER_UPDATE_ABORTED_MSG_DISPLAY = "The folder was not updated. Please review the warnings for details.";

    // Folder name contains not allowed characters: \"%1$s\"
    public static final String ILLEGAL_CHARACTERS_MSG = "Folder name contains illegal characters: \"%1$s\"";

    // User tries to restore a folders from trash, but that functionality is not supported
    public static final String NO_RESTORE_SUPPORT_MSG = "Restore from trash is not supported";

    // User selected one or more permissions that cannot be applied to target folder; e.g. selected guest permission for a mail folder.
    public static final String INVALID_PERMISSIONS_MSG = "The chosen permission(s) cannot be set";

    // With moving the shared folder \"%1$s\" people will lose access.
    public static final String MOVE_TO_NOT_SHARED_WARNING = "With moving the shared folder \"%1$s\" people will lose access.";

    // This folder  will be shared with everyone who has access to \"%3$s\". Everyone who can see \"%1$s\" will lose access.
    public static final String MOVE_TO_ANOTHER_SHARED_WARNING = "This folder will be shared with everyone who has access to \"%3$s\". Everyone who can see \"%1$s\" will lose access.";

    // This folder will be shared with everyone who has access to \"%3$s\.
    public static final String MOVE_TO_SHARED_WARNING = "This folder will be shared with everyone who has access to \"%3$s\".";

    // You are moving a folder that contains shares. People will lose access.
    public static final String MOVE_SHARED_SUBFOLDERS_TO_NOT_SHARED_WARNING = "You are moving a folder that contains shares. People will lose access.";

    // This folder will be shared with everyone who has access to \"%3$s\". Everyone who can see the subfolders of \"%1$s\" will lose access.
    public static final String MOVE_SHARED_SUBFOLDERS_TO_SHARED_WARNING = "This folder will be shared with everyone who has access to \"%3$s\". Everyone who can see the subfolders of \"%1$s\" will lose access.";

    // User tries to search folder by folder name, but that functionality is not supported
    public static final String NO_SEARCH_SUPPORT_MSG = "Searching folder by folder name is not supported";

    /**
     * Initializes a new {@link FolderExceptionMessages}
     */
    private FolderExceptionMessages() {
        super();
    }

}
