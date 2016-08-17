/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.tools.oxfolder;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link OXFolderExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class OXFolderExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link OXFolderExceptionMessage}.
     */
    private OXFolderExceptionMessage() {
        super();
    }

    public final static String NO_MODULE_ACCESS_MSG = "You do not have appropriate permissions for module \"%2$s\".";
    public final static String NOT_VISIBLE_MSG = "You do not have appropriate permissions to view the folder.";
    public final static String NO_SHARED_FOLDER_ACCESS_MSG = "Shared folders are not available for your account.";
    public final static String NO_PUBLIC_FOLDER_WRITE_ACCESS_MSG = "You do not have appropriate permissions for the public folder.";
    public final static String NO_CREATE_SUBFOLDER_PERMISSION_MSG = "You do not have appropriate permissions to create subfolders.";
    public final static String NOT_EXISTS_MSG = "Such a folder does not exist.";
    public final static String CONCURRENT_MODIFICATION_MSG = "The folder has been changed in the meantime. Please reload the view and try again.";
    public final static String NO_ADMIN_ACCESS_MSG = "This operation requires administrative permissions for the folder.";
    public final static String NO_DEFAULT_FOLDER_RENAME_MSG = "The folder is a default folder and cannot be renamed.";
    public final static String NO_DUPLICATE_FOLDER_MSG = "The folder already contains a folder with such a name.";
    public final static String INVALID_TYPE_MSG = "Folders of type \"%2$s\" can't be created below this folder.";
    public final static String INVALID_MODULE_MSG = "Folders of module \"%2$s\" can't be created below this folder.";
    public final static String ONLY_ONE_PRIVATE_FOLDER_ADMIN_MSG = "Only one user may have administrative permission roles in a private folder.";
    public final static String NO_PRIVATE_FOLDER_ADMIN_GROUP_MSG = "Administrative permissions for a group are not allowed in a private folder.";
    public final static String ONLY_PRIVATE_FOLDER_OWNER_ADMIN_MSG = "Only the owner of a private folder may hold administrative permissions.";
    public final static String NO_FOLDER_ADMIN_MSG = "At least one user with administrative permissions is required.";
    public final static String NO_DEFAULT_FOLDER_MOVE_MSG = "The folder is a default folder and cannot be moved.";
    public final static String NO_SHARED_FOLDER_MOVE_MSG = "The folder is shared and cannot be moved.";
    public final static String NO_SHARED_FOLDER_TARGET_MSG = "The folder cannot be moved to a shared folder.";
    public final static String NO_SYSTEM_FOLDER_MOVE_MSG = "The folder is a system folder and cannot be moved.";
    public final static String ONLY_PRIVATE_TO_PRIVATE_MOVE_MSG = "The private folder can only be moved to another private folder.";
    public final static String ONLY_PUBLIC_TO_PUBLIC_MOVE_MSG = "The public folder can only be moved to another public folder.";
    public final static String NO_EQUAL_MOVE_MSG = "The destination folder is the same as the source folder.";
    public final static String NO_SUBFOLDER_MOVE_ACCESS_MSG = "You do not have appropriate permissions to move all subfolders.";
    public final static String NO_SHARED_FOLDER_DELETION_MSG = "You do not have appropriate permissions to delete the shared folder.";
    public final static String NO_DEFAULT_FOLDER_DELETION_MSG = "The folder is a default folder and cannot be deleted.";
    public final static String NOT_ALL_OBJECTS_DELETION_MSG = "You do not have appropriate permissions to delete all contained items.";
    public final static String NO_SUBFOLDER_MOVE_MSG = "The folder cannot be be moved to one of its subfolders.";
    public final static String INCOMPATIBLE_MODULES_MSG = "Folders of module \"%1$s\" cannot be moved to folders of module \"%2$s\".";
    public final static String NO_SHARED_FOLDER_UPDATE_MSG = "The shared folder cannot be updated.";
    public final static String TRUNCATED_MSG = "The value for attribute %1$s contains more than the allowed number of %2$d characters. Current length: %3$d characters.";
    public final static String TRUNCATED_FOLDERNAME_MSG = "The specified folder name exceeds the limit of %2$d characters. Please shorten the name and try again.";
    public final static String INVALID_SHARED_FOLDER_SUBFOLDER_PERMISSION_MSG = "Only the owner of the parent shared folder may hold administrative permissions.";
    public final static String CREATOR_IS_NOT_ADMIN_MSG = "The folder is a default folder. Hence its owner needs to hold administrative permissions.";
    public final static String SHARE_FORBIDDEN_MSG = "You do not have appropriate permissions to share the folder.";
    public final static String UNAPPLICABLE_FOLDER_PERM_MSG = "It is not possible to apply the changes due to limited permissions";
    public final static String UNAPPLICABLE_FOLDER_PERM_MSG_EXTENDED = "It is not possible to apply the changes as a result of restricted permissions of the user \"%1$s\".";
    public final static String HIDDEN_FOLDER_ON_DELETION_MSG = "The folder cannot be deleted since it contains a hidden subfolder you don't have the appropriate permissions for.";
    public final static String NO_DEFAULT_INFOSTORE_CREATE_MSG = "The folder already contains a folder with such a name. Please choose a different name and try again.";
    public final static String INVALID_DATA_MSG = "The folder name contains invalid characters.";
    public final static String SIMILAR_NAMED_SHARED_FOLDER_MSG = "You already share a personal folder named \"%1$s\" with the same user. You can not share two folders with exactly the same name with a user. Please rename the folder before sharing it with this user.";
    public final static String DUPLICATE_USER_PERMISSION_MSG = "Duplicate permission defined. Only one permission per user is allowed.";
    public final static String DUPLICATE_GROUP_PERMISSION_MSG = "Duplicate permission defined. Only one permission per group is allowed.";
    public final static String FOLDER_VISIBILITY_PERMISSION_ONLY_MSG = "Only the folder visibility permission is allowed to be changed.";
    public final static String NO_GROUP_PERMISSION_MSG = "Only individual user permissions, but no group permissions are allowed.";
    public final static String NO_INDIVIDUAL_PERMISSION_MSG = "No individual user permissions are allowed.";
    public final static String NO_RENAME_ACCESS_MSG = "You do not have appropriate permissions to rename the folder.";
    public final static String DELETE_FAILED_LOCKED_DOCUMENTS_MSG = "The folder cannot be deleted as it contains locked documents.";
    public final static String ADMIN_OP_ONLY_MSG = "Operation may only be performed for context administrator.";
    public static final String DELETE_DENIED_MSG = "The folder cannot be deleted.";
    public static final String CREATOR_STAYS_ADMIN_MSG = "The administrative permissions were restored.";
    public static final String NO_FOLDER_NAME_MSG = "You must enter a folder name.";
    public final static String NO_RESERVED_FOLDER_MSG = "The folder name \"%1$s\" is reserved. Please choose another name.";

    // Unsupported character "%1$s" in field "%2$s". Please remove that character.
    public static final String INVALID_CHARACTER_MSG = "Unsupported character \"%1$s\" in field \"%2$s\". Please remove that character.";

    // Unsupported character. Please correct your input.
    public static final String INVALID_CHARACTER_SIMPLE_MSG = "Unsupported character. Please correct your input.";

    // You are not allowed to share the folder to "%2$s"; e.g. 'You are not allowed to share the folder to "Guests"'
    public static final String INVALID_ENTITY_FROM_USER_MSG = "You are not allowed to share the folder to \"%2$s\"";

    // You are not allowed to change the permissions of the trash folder or any of its subfolders
    public static final String NO_TRASH_PERMISSIONS_CHANGE_ALLOWED_MSG = "You are not allowed to change the permissions of the trash folder or any of its subfolders";

}
