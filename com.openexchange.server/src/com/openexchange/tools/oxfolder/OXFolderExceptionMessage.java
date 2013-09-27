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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

    /**
     * User %1$s has no access to module %2$s in context %3$s due to user configuration
     * <p>
     * Requested operation was canceled because underlying user configuration denies folder access due to module restrictions
     * </p>
     */
    public final static String NO_MODULE_ACCESS_MSG = "User %1$s has no access to module %2$s in context %3$s due to user configuration";
    /**
     * Folder \"%1$s\" not visible to user %2$s in context %3$s
     * <p>
     * Either underlying user configuration or folder CATEGORY_PERMISSION_DENIED setting denies visibility of folder in question
     * </p>
     */
    public final static String NOT_VISIBLE_MSG = "Folder \"%1$s\" not visible to user %2$s in context %3$s";
    /**
     * User %1$s has no access on shared folder %2$s in context %3$s due to user configuration
     * <p>
     * Underlying user configuration denies access to shared folder(s)
     * </p>
     */
    public final static String NO_SHARED_FOLDER_ACCESS_MSG = "User %1$s has no access on shared folder %2$s in context %3$s due to user configuration";
    /**
     * User %1$s has no write access on public folder %2$s in context %3$s due to user configuration
     * <p>
     * Underlying user configuration denies write access to public folder(s)
     * </p>
     */
    public final static String NO_PUBLIC_FOLDER_WRITE_ACCESS_MSG = "User %1$s has no write access on public folder %2$s in context %3$s due to user configuration";
    /**
     * User %1$s has no create-subfolder permission on folder %2$s in context %3$s
     * <p>
     * Folder CATEGORY_PERMISSION_DENIED setting denies subfolder creation beneath folder in question
     * </p>
     */
    public final static String NO_CREATE_SUBFOLDER_PERMISSION_MSG = "User %1$s has no create-subfolder permission on folder %2$s in context %3$s";
    /**
     * Missing field %1$s in folder %2$s in context %3$s
     * <p>
     * Operation was canceled due to a missing folder attribute
     * </p>
     */
    public final static String MISSING_FOLDER_ATTRIBUTE_MSG = "Missing field %1$s in folder %2$s in context %3$s";
    /**
     * Folder %1$s does not exist in context %2$s
     * <p>
     * Folder does not exist
     * </p>
     */
    public final static String NOT_EXISTS_MSG = "Folder %1$s does not exist in context %2$s";
    /**
     * Folder %1$s has been modified after last sync timestamp in context %2$s
     * <p>
     * Client timestamp is before folder's last-changed timestamp
     * </p>
     */
    public final static String CONCURRENT_MODIFICATION_MSG = "Folder %1$s has been modified after last sync timestamp in context %2$s";
    /**
     * User %1$s has no admin access to folder %2$s in context %3$s
     * <p>
     * No necessary admin access granted for update operation
     * </p>
     */
    public final static String NO_ADMIN_ACCESS_MSG = "User %1$s has no admin access to folder %2$s in context %3$s";
    /**
     * Default folder %1$s cannot be renamed in context %2$s
     * <p>
     * Default folder must not be renamed
     * </p>
     */
    public final static String NO_DEFAULT_FOLDER_RENAME_MSG = "Default folder %1$s cannot be renamed in context %2$s";
    /**
     * A duplicate folder exists in parent folder %1$s in context %2$s
     * <p>
     * Rename aborted cause a duplicate folder exists beneath parent folder
     * </p>
     */
    public final static String NO_DUPLICATE_FOLDER_MSG = "A duplicate folder exists in parent folder %1$s in context %2$s";
    /**
     * Parent folder %1$s does not allow type %2$s in context %3$s
     * <p>
     * Folder's type setting is invalid
     * </p>
     */
    public final static String INVALID_TYPE_MSG = "Parent folder %1$s does not allow type %2$s in context %3$s";
    /**
     * Parent folder %1$s does not allow module %2$s in context %3$s
     * <p>
     * Folder's module setting is invalid
     * </p>
     */
    public final static String INVALID_MODULE_MSG = "Parent folder %1$s does not allow module %2$s in context %3$s";
    /**
     * Only one admin permission is allowed on a private folder
     * <p>
     * Only one admin is allowed on a private folder
     * </p>
     */
    public final static String ONLY_ONE_PRIVATE_FOLDER_ADMIN_MSG = "Only one admin permission is allowed on a private folder";
    /**
     * A group must not hold admin permission on a private folder
     * <p>
     * No admin group permission is allowed on a private folder
     * </p>
     */
    public final static String NO_PRIVATE_FOLDER_ADMIN_GROUP_MSG = "A group must not hold admin permission on a private folder";
    /**
     * Only folder owner may hold admin permission on a private folder
     * <p>
     * Only folder owner may hold admin permission on a private folder
     * </p>
     */
    public final static String ONLY_PRIVATE_FOLDER_OWNER_ADMIN_MSG = "Only folder owner may hold admin permission on a private folder";
    /**
     * Administration rights required. In the Rights tab add at least one user with administration rights.
     * <p>
     * No entity has been defined as admin
     * </p>
     */
    public final static String NO_FOLDER_ADMIN_MSG = "Administration rights required. In the Rights tab add at least one user with administration rights.";
    /**
     * Invalid object ID %1$s
     * <p>
     * An invalid object ID
     * </p>
     */
    public final static String INVALID_OBJECT_ID_MSG = "Invalid object ID %1$s";
    /**
     * Not allowed to change parent id of folder %1$s through an update call. Use move method instead
     * <p>
     * Folder's parent id is changed during an update operation
     * </p>
     */
    public final static String NO_MOVE_THROUGH_UPDATE_MSG = "Not allowed to change parent id of folder %1$s through an update call. Use move method instead.";
    /**
     * Not allowed to move default folder %1$s in context %2$s
     * <p>
     * Default folder must not be moved
     * </p>
     */
    public final static String NO_DEFAULT_FOLDER_MOVE_MSG = "Not allowed to move default folder %1$s in context %2$s";
    /**
     * Target folder %1$s contains a duplicate folder in context %2$s
     * <p>
     * Target folder contains a duplicate folder
     * </p>
     */
    public final static String TARGET_FOLDER_CONTAINS_DUPLICATE_MSG = "Target folder %1$s contains a duplicate folder in context %2$s";
    /**
     * Shared folder %1$s cannot be moved in context %2$s
     * <p>
     * A shared folder must not be moved
     * </p>
     */
    public final static String NO_SHARED_FOLDER_MOVE_MSG = "Shared folder %1$s cannot be moved in context %2$s";
    /**
     * Shared folder %1$s cannot be target of move operation in context %2$s
     * <p>
     * A shared folder must not be target of a move operation
     * </p>
     */
    public final static String NO_SHARED_FOLDER_TARGET_MSG = "Shared folder %1$s cannot be target of move operation in context %2$s";
    /**
     * System folder %1$s cannot be moved in context %2$s
     * <p>
     * A system folder must not be moved
     * </p>
     */
    public final static String NO_SYSTEM_FOLDER_MOVE_MSG = "System folder %1$s cannot be moved in context %2$s";
    /**
     * Private folder %1$s can only be moved to a private folder in context %2$s
     * <p>
     * A private folder may only be moved to a private folder
     * </p>
     */
    public final static String ONLY_PRIVATE_TO_PRIVATE_MOVE_MSG = "Private folder %1$s can only be moved to a private folder in context %2$s";
    /**
     * Public folder %1$s may only be moved to a public folder in context %2$s
     * <p>
     * A public folder may only be moved to a public folder
     * </p>
     */
    public final static String ONLY_PUBLIC_TO_PUBLIC_MOVE_MSG = "Public folder %1$s can only be moved to a public folder in context %2$s";
    /**
     * Target and source folder must not be equal in context %1$s
     * <p>
     * Target and source folder must not be equal
     * </p>
     */
    public final static String NO_EQUAL_MOVE_MSG = "Target and source folder cannot be equal in context %1$s";
    /**
     * User %1$s is not allowed to move all subfolders of folder %2$s in context %3$s
     * <p>
     * User is not allowed to move source folder's subfolder
     * </p>
     */
    public final static String NO_SUBFOLDER_MOVE_ACCESS_MSG = "User %1$s is not allowed to move all subfolders of folder %2$s in context %3$s";
    /**
     * User %1$s is not allowed to delete shared folder %2$s in context %3$s
     * <p>
     * A shared folder must not be deleted
     * </p>
     */
    public final static String NO_SHARED_FOLDER_DELETION_MSG = "User %1$s is not allowed to delete shared folder %2$s in context %3$s";
    /**
     * User %1$s is not allowed to delete default folder %2$s in context %3$s
     * <p>
     * Default folder(s) must not be deleted
     * </p>
     */
    public final static String NO_DEFAULT_FOLDER_DELETION_MSG = "User %1$s is not allowed to delete default folder %2$s in context %3$s";
    /**
     * User %1$s is not allowed to delete all contained objects in folder %2$s in context %3$s
     * <p>
     * User is not allowed to delete all objects contained in folder in question
     * </p>
     */
    public final static String NOT_ALL_OBJECTS_DELETION_MSG = "User %1$s is not allowed to delete all objects in folder %2$s in context %3$s";
    /**
     * No admin user found in context %1$s
     * <p>
     * No admin user was found in context in question
     * </p>
     */
    public final static String NO_ADMIN_USER_FOUND_IN_CONTEXT_MSG = "No admin user found in context %1$s";
    /**
     * No default folder could be found in module %1$s for user %2$s in context %3$s
     * <p>
     * No default folder was found for current user in given module and context
     * </p>
     */
    public final static String NO_DEFAULT_FOLDER_FOUND_MSG = "No default folder could be found in module %1$s for user %2$s in context %3$s";
    /**
     * Folder %1$s could not be loaded in context %2$s
     * <p>
     * Folder could not be loaded from storage
     * </p>
     */
    public final static String FOLDER_COULD_NOT_BE_LOADED_MSG = "Folder %1$s could not be loaded in context %2$s";
    /**
     * Folder %1$s could not be put into cache in context %2$s
     * <p>
     * Folder could not be put into cache
     * </p>
     */
    public final static String FOLDER_COULD_NOT_BE_PUT_INTO_CACHE_MSG = "Folder %1$s could not be put into cache in context %2$s";
    /**
     * Effective permission of folder %1$s could not be determined for user %2$s in context %3$s
     * <p>
     * User's effective permission on folder could not be determined
     * </p>
     */
    public final static String NO_EFFECTIVE_PERMISSION_MSG = "Effective permission of folder %1$s could not be determined for user %2$s in context %3$s";
    /**
     * A SQL error occurred: %1$s
     */
    public final static String SQL_ERROR_MSG = "A SQL error occurred: %1$s";
    /**
     * A DBPool error occurred in context %1$s
     */
    public final static String DBPOOLING_ERROR_MSG = "A DBPool error occurred in context %1$s";
    /**
     * Delivered sequence id %1$s from database is less than allowed min. folder id of %2$s in context %3$s
     */
    public final static String INVALID_SEQUENCE_ID_MSG = "Delivered sequence id %1$s from database is less than allowed min. folder id of %2$s in context %3$s";
    /**
     * Module %1$s is unknown in context %2$s
     */
    public final static String UNKNOWN_MODULE_MSG = "Module %1$s is unknown in context %2$s";
    /**
     * Folder %1$s could not be updated in context %2$s
     * <p>
     * Folder update failed for any reason
     * </p>
     */
    public final static String UPDATE_FAILED_MSG = "Folder %1$s could not be updated in context %2$s";
    /**
     * Invalid entity id %1$s detected in permissions of folder %2$s in context %3$s
     */
    public final static String INVALID_ENTITY_MSG = "Invalid entity id %1$s detected in permissions of folder %2$s in context %3$s";
    /**
     * Folder %1$s must not be moved to one of its subfolders in context %2$s
     */
    public final static String NO_SUBFOLDER_MOVE_MSG = "Folder %1$s must not be moved to one of its subfolders in context %2$s";
    /**
     * Inserted for those exception that will turn to an <code>OXException</code> in future
     */
    public final static String UNKNOWN_EXCEPTION_MSG = "An unexpected error occurred: %1$s";
    /**
     * A LDAP error occurred in context %1$s
     */
    public final static String LDAP_ERROR_MSG = "A LDAP error occurred in context %1$s";
    /**
     * Attribute \"%1$s\" is not set in FolderObject instance %2$s in context %3$s
     */
    public final static String ATTRIBUTE_NOT_SET_MSG = "Attribute \"%1$s\" is not set in FolderObject instance %2$s in context %3$s";
    /**
     * A source folder of module %1$s must not be moved to a target folder of module %2$s
     */
    public final static String INCOMPATIBLE_MODULES_MSG = "A source folder of module %1$s must not be moved to a target folder of module %2$s";
    /**
     * Operation not executable on folder %1$s in context %2$s
     */
    public final static String UNSUPPORTED_OPERATION_MSG = "Operation not executable on folder %1$s in context %2$s";
    /**
     * Folder cache (region name = %1$s) could not be initialized due to following reason: %2$s
     */
    public final static String FOLDER_CACHE_INITIALIZATION_FAILED_MSG = "Folder cache (region name = %1$s) could not be initialized due to the following reason: %2$s";
    /**
     * Folder cache has not been enabled in config file %1$s
     */
    public final static String CACHE_NOT_ENABLED_MSG = "Folder cache has not been enabled in config file %1$s";
    /**
     * Folder %1$s could not be removed from folder cache
     */
    public final static String CACHE_ERROR_ON_REMOVE_MSG = "Folder %1$s could not be removed from folder cache";
    /**
     * User %1$s has no write permission on folder %2$s in context %3$s
     */
    public final static String NO_WRITE_PERMISSION_MSG = "User %1$s has no write permission on folder %2$s in context %3$s";
    /**
     * A JSON error occurred: %1$s
     */
    public final static String JSON_ERROR_MSG = "A JSON error occurred: %1$s";
    /**
     * Unknown parameter container type: %1$s
     */
    public final static String UNKNOWN_PARAMETER_CONTAINER_TYPE_MSG = "Unknown parameter container type: %1$s";
    /**
     * Missing parameter %1$s
     */
    public final static String MISSING_PARAMETER_MSG = "Missing parameter %1$s";
    /**
     * Bad value %1$s in parameter %2$s
     */
    public final static String BAD_PARAM_VALUE_MSG = "Bad value %1$s in parameter %2$s";
    /**
     * Unknown field: %1$s
     */
    public final static String UNKNOWN_FIELD_MSG = "Unknown field: %1$s";
    /**
     * Parameter %1$s does not match JSON key %2$s
     */
    public final static String PARAMETER_MISMATCH_MSG = "Parameter %1$s does not match JSON key %2$s";
    /**
     * Invalid permission values: fp=%1$s orp=%2$s owp=%3$s odp=%4$s
     */
    public final static String INVALID_PERMISSION_MSG = "Invalid permission values: fp=%1$s orp=%2$s owp=%3$s odp=%4$s";
    /**
     * Unknown action: %1$s
     */
    public final static String UNKNOWN_ACTION_MSG = "Unknown action: %1$s";
    /**
     * Shared folder %1$s must not be updated in context %2$s
     */
    public final static String NO_SHARED_FOLDER_UPDATE_MSG = "Shared folder %1$s must not be updated in context %2$s";

    /**
     * The value for attribute %1$s contains more than the allowed number of %2$d characters. Current length: %3$d characters.
     */
    public final static String TRUNCATED_MSG = "The value for attribute %1$s contains more than the allowed number of %2$d characters. Current length: %3$d characters.";
    /**
     * Unable to map OCL permission value %1$s to a JSON permission value
     */
    public final static String MAP_PERMISSION_FAILED_MSG = "Unable to map OCL permission value %1$s to a JSON permission value";
    /**
     * Folder existence cannot be checked due to insufficient folder information
     */
    public final static String UNSUFFICIENT_FOLDER_INFORMATION_MSG = "Folder existence cannot be checked due to insufficient folder information";
    /**
     * A runtime error occurred in context %1$s
     */
    public final static String RUNTIME_ERROR_MSG = "A runtime error occurred in context %1$s";
    /**
     * This method is not applicable to an IMAP permission.
     */
    public final static String ACL_PERMISSION_ERROR_MSG = "This method is not applicable to an IMAP permission.";
    /**
     * No subfolder creation underneath shared folder %1$s in context %2$s
     */
    public final static String NO_SUBFOLDER_BELOW_SHARED_FOLDER_MSG = "No subfolder creation below shared folder %1$s in context %2$s";
    /**
     * User %1$s granted invalid permissions on shared folder %2$s in context %3$s. Only the owner of the parent shared folder %4$s can administrate the folder.
     */
    public final static String INVALID_SHARED_FOLDER_SUBFOLDER_PERMISSION_MSG = "User %1$s granted invalid permissions on shared folder %2$s in context %3$s. Only the owner of the parent shared folder %4$s can administrate the folder.";
    /**
     * Owner %1$s of default folder %2$s must keep the folder admin permission
     */
    public final static String CREATOR_IS_NOT_ADMIN_MSG = "Owner %1$s of default folder %2$s must keep the folder admin permission";
    /**
     * User %1$s is not allowed to share folder %2$s in context %3$s due to user configuration
     */
    public final static String SHARE_FORBIDDEN_MSG = "User %1$s is not allowed to share folder %2$s in context %3$s due to user configuration";
    /**
     * The permissions for the user %1$s are limited. It is not possible to grant this user the wanted permissions for the folder %2$s
     * in context %3$s.
     */
    public final static String UNAPPLICABLE_FOLDER_PERM_MSG = "The permissions for the user %1$s are limited. It is not possible to grant this user the wanted permissions for the folder %2$s in context %3$s.";
    /**
     * Folder %1$s in context %2$s contains a hidden subfolder. User %3$s has no delete rights for this subfolder and consequently
     * cannot delete its parent folder.
     */
    public final static String HIDDEN_FOLDER_ON_DELETION_MSG = "Folder %1$s in context %2$s contains a hidden subfolder." + " User %3$s has no delete rights for this subfolder and consequently cannot delete its parent folder.";
    /**
     * An infostore folder named %1$s already exists below folder %2$s (%3$s) in context %4$s. Please choose another display name.
     */
    public final static String NO_DEFAULT_INFOSTORE_CREATE_MSG = "An infostore folder named %1$s already exists below folder %2$s (%3$s) in context %4$s." + " Please choose another display name.";
    /**
     * Folder contains invalid data: "%1$s"
     */
    public final static String INVALID_DATA_MSG = "Folder contains invalid data: \"%1$s\"";
    /**
     * You already share a personal folder named "%1$s" with the same user. You can not share two folders with exactly the same name
     * with a user. Please rename the folder before sharing it with this user.
     */
    public final static String SIMILAR_NAMED_SHARED_FOLDER_MSG = "You already share a personal folder named \"%1$s\" with the same user. You can not share two folders with exactly the same name with a user. Please rename the folder before sharing it with this user.";
    /**
     * Folder module cannot be updated since folder is not empty
     */
    public final static String NO_FOLDER_MODULE_UPDATE_MSG = "Folder module cannot be updated since folder is not empty";
    /**
     * Duplicate permission defined for user %1$s. Only one permission per user is allowed.
     */
    public final static String DUPLICATE_USER_PERMISSION_MSG = "Duplicate permission defined for user %1$s. Only one permission per user is allowed.";
    /**
     * Duplicate permission defined for group %1$s. Only one permission per group is allowed.
     */
    public final static String DUPLICATE_GROUP_PERMISSION_MSG = "Duplicate permission defined for group %1$s. Only one permission per group is allowed.";
    /**
     * Only the folder visibility permission is allowed to be changed for folder %1$s in context %2$s.
     */
    public final static String FOLDER_VISIBILITY_PERMISSION_ONLY_MSG = "Only the folder visibility permission is allowed to be changed for folder %1$s in context %2$s.";
    /**
     * Only individual user permissions, but no group permissions are allowed for folder %1$s in context %2$s.
     */
    public final static String NO_GROUP_PERMISSION_MSG = "Only individual user permissions, but no group permissions are allowed for folder %1$s in context %2$s.";
    /**
     * No individual user permissions are allowed for folder %1$s in context %2$s.
     */
    public final static String NO_INDIVIDUAL_PERMISSION_MSG = "No individual user permissions are allowed for folder %1$s in context %2$s.";
    /**
     * Folder module must not be updated.
     */
    public final static String DENY_FOLDER_MODULE_UPDATE_MSG = "Folder module must not be updated.";
    /**
     * The module of a default folder must not be changed.
     */
    public final static String NO_DEFAULT_FOLDER_MODULE_UPDATE_MSG = "The module of a default folder must not be changed.";
    /**
     * User %1$s must not rename folder %2$s in context %3$s
     * <p>
     * No necessary rename access granted for update operation
     * </p>
     */
    public final static String NO_RENAME_ACCESS_MSG = "User %1$s must not rename folder %2$s in context %3$s";
    /**
     * Failed to create folder. Please retry.
     */
    public final static String CREATE_FAILED_MSG = "Failed to create folder. Please retry.";
    /**
     * Delete cannot be performed because of locked documents in folder %1$s in context %2$s.
     */
    public final static String DELETE_FAILED_LOCKED_DOCUMENTS_MSG = "Delete cannot be performed because of locked documents in folder %1$s in context %2$s.";
    /**
     * Operation may only be performed for context administrator.
     */
    public final static String ADMIN_OP_ONLY_MSG = "Operation may only be performed for context administrator.";

    // Folder %1$s must not be deleted in context %2$s.
    public static final String DELETE_DENIED_MSG = "Folder %1$s must not be deleted in context %2$s.";

    // Owner %1$s of folder %2$s has been restored to keep the folder admin permission
    public static final String CREATOR_STAYS_ADMIN_MSG = "Owner %1$s of folder %2$s has been restored to keep the folder admin permission";

}
