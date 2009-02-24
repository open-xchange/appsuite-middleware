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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;

/**
 * {@link OXFolderException} - OX-Folder related exceptions
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXFolderException extends OXException {

    private static final long serialVersionUID = -9109199682724280886L;

    public static final int DETAIL_NUMBER_CONCURRENT_MODIFICATION = 1;

    public static enum FolderCode {
        /**
         * User %1$s has no access to module %2$s in context %3$s due to user configuration
         * <p>
         * Requested operation was canceled because underlying user configuration denies folder access due to module restrictions
         * </p>
         */
        NO_MODULE_ACCESS("User %1$s has no access to module %2$s in context %3$s due to user configuration", Category.USER_CONFIGURATION, 2),
        /**
         * Folder \"%1$s\" not visible to user %2$s in context %3$s
         * <p>
         * Either underlying user configuration or folder permission setting denies visibility of folder in question
         * </p>
         */
        NOT_VISIBLE("Folder \"%1$s\" not visible to user %2$s in context %3$s", Category.PERMISSION, 3),
        /**
         * User %1$s has no access on shared folder %2$s in context %3$s due to user configuration
         * <p>
         * Underlying user configuration denies access to shared folder(s)
         * </p>
         */
        NO_SHARED_FOLDER_ACCESS("User %1$s has no access on shared folder %2$s in context %3$s due to user configuration", Category.USER_CONFIGURATION, 4),
        /**
         * User %1$s has no write access on public folder %2$s in context %3$s due to user configuration
         * <p>
         * Underlying user configuration denies write access to public folder(s)
         * </p>
         */
        NO_PUBLIC_FOLDER_WRITE_ACCESS("User %1$s has no write access on public folder %2$s in context %3$s due to user configuration", Category.USER_CONFIGURATION, 5),
        /**
         * User %1$s has no create-subfolder permission on folder %2$s in context %3$s
         * <p>
         * Folder permission setting denies subfolder creation beneath folder in question
         * </p>
         */
        NO_CREATE_SUBFOLDER_PERMISSION("User %1$s has no create-subfolder permission on folder %2$s in context %3$s", Category.PERMISSION, 6),
        /**
         * Missing field %1$s in folder %2$s in context %3$s
         * <p>
         * Operation was canceled due to a missing folder attribute
         * </p>
         */
        MISSING_FOLDER_ATTRIBUTE("Missing field %1$s in folder %2$s in context %3$s", Category.CODE_ERROR, 7),
        /**
         * Folder %1$s does not exist in context %2$s
         * <p>
         * Folder does not exist
         * </p>
         */
        NOT_EXISTS("Folder %1$s does not exist in context %2$s", Category.CODE_ERROR, 8),
        /**
         * Folder %1$s has been modified after last sync timestamp in context %2$s
         * <p>
         * Client timestamp is before folder's last-changed timestamp
         * </p>
         */
        CONCURRENT_MODIFICATION("Folder %1$s has been modified after last sync timestamp in context %2$s", Category.CONCURRENT_MODIFICATION, 9),
        /**
         * User %1$s has no admin access to folder %2$s in context %3$s
         * <p>
         * No necessary admin access granted for update operation
         * </p>
         */
        NO_ADMIN_ACCESS("User %1$s has no admin access to folder %2$s in context %3$s", Category.PERMISSION, 10),
        /**
         * Default folder %1$s cannot be renamed in context %2$s
         * <p>
         * Default folder must not be renamed
         * </p>
         */
        NO_DEFAULT_FOLDER_RENAME("Default folder %1$s cannot be renamed in context %2$s", Category.PERMISSION, 11),
        /**
         * A duplicate folder exists in parent folder %1$s in context %2$s
         * <p>
         * Rename aborted cause a duplicate folder exists beneath parent folder
         * </p>
         */
        NO_DUPLICATE_FOLDER("A duplicate folder exists in parent folder %1$s in context %2$s", Category.PERMISSION, 12),
        /**
         * Parent folder %1$s does not allow folder's type setting %2$s in context %3$s
         * <p>
         * Folder's type setting is invalid
         * </p>
         */
        INVALID_TYPE("Parent folder %1$s does not allow folder's type setting %2$s in context %3$s", Category.CODE_ERROR, 13),
        /**
         * Parent folder %1$s does not allow folder's module setting (%2$s) in context %3$s
         * <p>
         * Folder's module setting is invalid
         * </p>
         */
        INVALID_MODULE("Parent folder %1$s does not allow folder's module setting (%2$s) in context %3$s", Category.CODE_ERROR, 14),
        /**
         * Only one admin permission is allowed on a private folder
         * <p>
         * Only one admin is allowed on a private folder
         * </p>
         */
        ONLY_ONE_PRIVATE_FOLDER_ADMIN("Only one admin permission is allowed on a private folder", Category.CODE_ERROR, 15),
        /**
         * A group must not hold admin permission on a private folder
         * <p>
         * No admin group permission is allowed on a private folder
         * </p>
         */
        NO_PRIVATE_FOLDER_ADMIN_GROUP("A group must not hold admin permission on a private folder", Category.CODE_ERROR, 16),
        /**
         * Only folder owner may hold admin permission on a private folder
         * <p>
         * Only folder owner may hold admin permission on a private folder
         * </p>
         */
        ONLY_PRIVATE_FOLDER_OWNER_ADMIN("Only folder owner may hold admin permission on a private folder", Category.CODE_ERROR, 17),
        /**
         * Administration rights required. In the Rights tab add at least one user with administration rights.
         * <p>
         * No entity has been defined as admin
         * </p>
         */
        NO_FOLDER_ADMIN("Administration rights required. In the Rights tab add at least one user with administration rights.", Category.CODE_ERROR, 18),
        /**
         * Invalid object ID %1$s
         * <p>
         * An invalid object ID
         * </p>
         */
        INVALID_OBJECT_ID("Invalid object ID %1$s", Category.CODE_ERROR, 19),
        /**
         * Not allowed to change parent id of folder %1$s through an update call. Use move method instead
         * <p>
         * Folder's parent id is changed during an update operation
         * </p>
         */
        NO_MOVE_THROUGH_UPDATE("Not allowed to change parent id of folder %1$s through an update call. Use move method instead", Category.CODE_ERROR, 20),
        /**
         * Not allowed to move default folder %1$s in context %2$s
         * <p>
         * Default folder must not be moved
         * </p>
         */
        NO_DEFAULT_FOLDER_MOVE("Not allowed to move default folder %1$s in context %2$s", Category.PERMISSION, 21),
        /**
         * Target folder %1$s contains a duplicate folder in context %2$s
         * <p>
         * Target folder contains a duplicate folder
         * </p>
         */
        TARGET_FOLDER_CONTAINS_DUPLICATE("Target folder %1$s contains a duplicate folder in context %2$s", Category.PERMISSION, 22),
        /**
         * Shared folder %1$s cannot be moved in context %2$s
         * <p>
         * A shared folder must not be moved
         * </p>
         */
        NO_SHARED_FOLDER_MOVE("Shared folder %1$s cannot be moved in context %2$s", Category.PERMISSION, 23),
        /**
         * Shared folder %1$s cannot be target of move operation in context %2$s
         * <p>
         * A shared folder must not be target of a move operation
         * </p>
         */
        NO_SHARED_FOLDER_TARGET("Shared folder %1$s cannot be target of move operation in context %2$s", Category.PERMISSION, 24),
        /**
         * System folder %1$s cannot be moved in context %2$s
         * <p>
         * A system folder must not be moved
         * </p>
         */
        NO_SYSTEM_FOLDER_MOVE("System folder %1$s cannot be moved in context %2$s", Category.PERMISSION, 25),
        /**
         * Private folder %1$s can only be moved to a private folder in context %2$s
         * <p>
         * A private folder may only be moved to a private folder
         * </p>
         */
        ONLY_PRIVATE_TO_PRIVATE_MOVE("Private folder %1$s can only be moved to a private folder in context %2$s", Category.PERMISSION, 26),
        /**
         * Public folder %1$s may only be moved to a public folder in context %2$s
         * <p>
         * A public folder may only be moved to a public folder
         * </p>
         */
        ONLY_PUBLIC_TO_PUBLIC_MOVE("Public folder %1$s can only be moved to a public folder in context %2$s", Category.PERMISSION, 27),
        /**
         * Target and source folder must not be equal in context %1$s
         * <p>
         * Target and source folder must not be equal
         * </p>
         */
        NO_EQUAL_MOVE("Target and source folder cannot be equal in context %1$s", Category.PERMISSION, 28),
        /**
         * User %1$s is not allowed to move all subfolders of folder %2$s in context %3$s
         * <p>
         * User is not allowed to move source folder's subfolder
         * </p>
         */
        NO_SUBFOLDER_MOVE_ACCESS("User %1$s is not allowed to move all subfolders of folder %2$s in context %3$s", Category.PERMISSION, 29),
        /**
         * User %1$s is not allowed to delete shared folder %2$s in context %3$s
         * <p>
         * A shared folder must not be deleted
         * </p>
         */
        NO_SHARED_FOLDER_DELETION("User %1$s is not allowed to delete shared folder %2$s in context %3$s", Category.PERMISSION, 30),
        /**
         * User %1$s is not allowed to delete default folder %2$s in context %3$s
         * <p>
         * Default folder(s) must not be deleted
         * </p>
         */
        NO_DEFAULT_FOLDER_DELETION("User %1$s is not allowed to delete default folder %2$s in context %3$s", Category.PERMISSION, 31),
        /**
         * User %1$s is not allowed to delete all contained objects in folder %2$s in context %3$s
         * <p>
         * User is not allowed to delete all objects contained in folder in question
         * </p>
         */
        NOT_ALL_OBJECTS_DELETION("User %1$s is not allowed to delete all contained objects in folder %2$s in context %3$s", Category.PERMISSION, 32),
        /**
         * No admin user found in context %1$s
         * <p>
         * No admin user was found in context in question
         * </p>
         */
        NO_ADMIN_USER_FOUND_IN_CONTEXT("No admin user found in context %1$s", Category.CODE_ERROR, 33),
        /**
         * No default folder could be found in module %1$s for user %2$s in context %3$s
         * <p>
         * No default folder was found for current user in given module and context
         * </p>
         */
        NO_DEFAULT_FOLDER_FOUND("No default folder could be found in module %1$s for user %2$s in context %3$s", Category.CODE_ERROR, 34),
        /**
         * Folder %1$s could not be loaded in context %2$s
         * <p>
         * Folder could not be loaded from storage
         * </p>
         */
        FOLDER_COULD_NOT_BE_LOADED("Folder %1$s could not be loaded in context %2$s", Category.CODE_ERROR, 35),
        /**
         * Folder %1$s could not be put into cache in context %2$s
         * <p>
         * Folder could not be put into cache
         * </p>
         */
        FOLDER_COULD_NOT_BE_PUT_INTO_CACHE("Folder %1$s could not be put into cache in context %2$s", Category.INTERNAL_ERROR, 36),
        /**
         * Effective permission of folder %1$s could not be determined for user %2$s in context %3$s
         * <p>
         * User's effective permission on folder could not be determined
         * </p>
         */
        NO_EFFECTIVE_PERMISSION("Effective permission of folder %1$s could not be determined for user %2$s in context %3$s", Category.CODE_ERROR, 37),
        /**
         * A SQL error occurred in context %1$s
         */
        SQL_ERROR("An SQL error occurred in context %1$s", Category.CODE_ERROR, 38),
        /**
         * A DBPool error occurred in context %1$s
         */
        DBPOOLING_ERROR("An SQL error occurred in context %1$s", Category.CODE_ERROR, 39),
        /**
         * Delivered sequence id %1$s from database is less than allowed min. folder id of %2$s in context %3$s
         */
        INVALID_SEQUENCE_ID("Delivered sequence id %1$s from database is less than allowed min. folder id of %2$s in context %3$s", Category.CODE_ERROR, 41),
        /**
         * Module %1$s is unknown in context %2$s
         */
        UNKNOWN_MODULE("Module %1$s is unknown in context %2$s", Category.CODE_ERROR, 42),
        /**
         * Folder %1$s could not be updated in context %2$s
         * <p>
         * Folder update failed for any reason
         * </p>
         */
        UPDATE_FAILED("Folder %1$s could not be updated in context %2$s", Category.CODE_ERROR, 43),
        /**
         * Invalid entity id %1$s detected in permissions of folder %2$s in context %3$s
         */
        INVALID_ENTITY("Invalid entity id %1$s detected in permissions of folder %2$s in context %3$s", Category.CODE_ERROR, 44),
        /**
         * Folder %1$s must not be moved to one of its subfolders in context %2$s
         */
        NO_SUBFOLDER_MOVE("Folder %1$s must not be moved to one of its subfolders in context %2$s", Category.CODE_ERROR, 45),
        /**
         * Inserted for those exception that will turn to an <code>OXException</code> in future
         */
        UNKNOWN_EXCEPTION("UNKNOWN EXCEPTION", Category.CODE_ERROR, 46),
        /**
         * A LDAP error occurred in context %1$s
         */
        LDAP_ERROR("A LDAP error occurred in context %1$s", Category.CODE_ERROR, 47),
        /**
         * Attribute \"%1$s\" is not set in FolderObject instance %2$s in context %3$s
         */
        ATTRIBUTE_NOT_SET("Attribute \"%1$s\" is not set in FolderObject instance %2$s in context %3$s", Category.CODE_ERROR, 48),
        /**
         * A source folder of module %1$s must not be moved to a target folder of module %2$s
         */
        INCOMPATIBLE_MODULES("A source folder of module %1$s must not be moved to a target folder of module %2$s", Category.PERMISSION, 49),
        /**
         * Operation not executable on folder %1$s in context %2$s
         */
        UNSUPPORTED_OPERATION("Operation not executable on folder %1$s in context %2$s", Category.CODE_ERROR, 50),
        /**
         * Folder cache (region name = %1$s) could not be initialized due to following reason: %2$s
         */
        FOLDER_CACHE_INITIALIZATION_FAILED("Folder cache (region name = %1$s) could not be initialized due to following reason: %2$s", Category.CODE_ERROR, 51),
        /**
         * Folder cache has not been enabled in config file %1$s
         */
        CACHE_NOT_ENABLED("Folder cache has not been enabled in config file %1$s", Category.CODE_ERROR, 52),
        /**
         * Folder %1$s could not be removed from folder cache
         */
        CACHE_ERROR_ON_REMOVE("Folder %1$s could not be removed from folder cache", Category.CODE_ERROR, 53),
        /**
         * User %1$s has no write permission on folder %2$s in context %3$s
         */
        NO_WRITE_PERMISSION("User %1$s has no write permission on folder %2$s in context %3$s", Category.PERMISSION, 54),
        /**
         * A JSON error occurred: %1$s
         */
        JSON_ERROR("A JSON error occurred: %1$s", Category.CODE_ERROR, 55),
        /**
         * Unknown parameter container type: %1$s
         */
        UNKNOWN_PARAMETER_CONTAINER_TYPE("Unknown parameter container type: %1$s", Category.CODE_ERROR, 56),
        /**
         * Missing parameter %1$s
         */
        MISSING_PARAMETER("Missing parameter %1$s", Category.CODE_ERROR, 57),
        /**
         * Bad value %1$s in parameter %2$s
         */
        BAD_PARAM_VALUE("Bad value %1$s in parameter %2$s", Category.USER_INPUT, 58),
        /**
         * Unknown field: %1$s
         */
        UNKNOWN_FIELD("Unknown field: %1$s", Category.CODE_ERROR, 59),
        /**
         * Parameter %1$s does not match JSON key %2$s
         */
        PARAMETER_MISMATCH("Parameter %1$s does not match JSON key %2$s", Category.CODE_ERROR, 60),
        /**
         * Invalid permission values: fp=%1$s orp=%2$s owp=%3$s odp=%4$s
         */
        INVALID_PERMISSION("Invalid permission values: fp=%1$s orp=%2$s owp=%3$s odp=%4$s", Category.CODE_ERROR, 61),
        /**
         * Unknown action: %1$s
         */
        UNKNOWN_ACTION("Unknown action: %1$s", Category.CODE_ERROR, 62),
        /**
         * Shared folder %1$s MUST NOT be updated in context %2$s
         */
        NO_SHARED_FOLDER_UPDATE("Shared folder %1$s MUST NOT be updated in context %2$s", Category.PERMISSION, 63),
        /**
         * The attribute %1$s contains too much characters. Current length %3$s is more than allowed length of %2$s characters.
         */
        TRUNCATED("The attribute %1$s contains too much characters. Current " + "length %3$d is more than allowed length of %2$d characters.", Category.TRUNCATED, 64),
        /**
         * Unable to map OCL permission value %1$s to a JSON permission value
         */
        MAP_PERMISSION_FAILED("Unable to map OCL permission value %1$s to a JSON permission value", Category.CODE_ERROR, 65),
        /**
         * Folder existence cannot be checked due to insufficient folder information
         */
        UNSUFFICIENT_FOLDER_INFORMATION("Folder existence cannot be checked due to insufficient folder information)", Category.CODE_ERROR, 66),
        /**
         * A runtime error occurred in context %1$s
         */
        RUNTIME_ERROR("A runtime error occurred in context %1$s", Category.INTERNAL_ERROR, 67),
        /**
         * This method is not applicable to an IMAP permission
         */
        ACL_PERMISSION_ERROR("This method is not applicable to an IMAP permission", Category.CODE_ERROR, 68),
        /**
         * No subfolder creation underneath shared folder %1$s in context %2$s
         */
        NO_SUBFOLDER_BELOW_SHARED_FOLDER("No subfolder creation underneath shared folder %1$s in context %2$s", Category.PERMISSION, 69),
        /**
         * User %1$s grants invalid permissions on shared folder %2$s in context %3$s. Only owner of parental shared folder %4$s may be
         * folder admin
         */
        INVALID_SHARED_FOLDER_SUBFOLDER_PERMISSION("User %1$s grants invalid permissions on shared folder %2$s in context %3$s." + " Only owner of parental shared folder %4$s may be folder admin", Category.PERMISSION, 70),
        /**
         * Owner %1$s of default folder %2$s must keep the folder admin permission
         */
        CREATOR_IS_NOT_ADMIN("Owner %1$s of default folder %2$s must keep the folder admin permission", Category.USER_INPUT, 71),
        /**
         * User %1$s is not allowed to share folder %2$s in context %3$s due to user configuration
         */
        SHARE_FORBIDDEN("User %1$s is not allowed to share folder %2$s in context %3$s due to user configuration", Category.USER_CONFIGURATION, 72),
        /**
         * Defined permissions for folder %1$s in context %2$s are not applicable to user %3$s due to user configuration
         */
        UNAPPLICABLE_FOLDER_PERM("Defined permissions for folder %1$s in context %2$s are not applicable to user %3$s due to user configuration", Category.USER_CONFIGURATION, 73),
        /**
         * Folder %1$s in context %2$s contains a hidden subfolder. User %3$s has no delete rights for this subfolder and consequently
         * cannot delete its parent folder.
         */
        HIDDEN_FOLDER_ON_DELETION("Folder %1$s in context %2$s contains a hidden subfolder." + " User %3$s has no delete rights for this subfolder and consequently cannot delete its parent folder.", Category.PERMISSION, 74),
        /**
         * An infostore folder named %1$s already exists below folder %2$s (%3$s) in context %4$s. Please choose another display name.
         */
        NO_DEFAULT_INFOSTORE_CREATE("An infostore folder named %1$s already exists below folder %2$s (%3$s) in context %4$s." + " Please choose another display name.", Category.PERMISSION, 75),
        /**
         * Folder contains invalid data: "%1$s"
         */
        INVALID_DATA("Folder contains invalid data: \"%1$s\"", Category.USER_INPUT, 76),
        /**
         * A private folder with the same name %1$s has already been shared to identical user(s) (Either direct or affected user(s) are
         * members of a group to whom the folder is shared).\nPlease enter another name to share the folder.
         */
        SIMILAR_NAMED_SHARED_FOLDER("A private folder with the same name %1$s has already been shared to identical user(s) " + "(Either direct or affected user(s) are members of a group to whom the folder is shared)." + "\nPlease enter another name to share the folder.", Category.USER_INPUT, 77),
        /**
         * Folder module cannot be updated since folder is not empty
         */
        NO_FOLDER_MODULE_UPDATE("Folder module cannot be updated since folder is not empty", Category.USER_INPUT, 78),
        /**
         * Duplicate permission defined for user %1$s. Only one permission per user is allowed.
         */
        DUPLICATE_USER_PERMISSION("Duplicate permission defined for user %1$s. Only one permission per user is allowed.", Category.USER_INPUT, 79),
        /**
         * Duplicate permission defined for group %1$s. Only one permission per group is allowed.
         */
        DUPLICATE_GROUP_PERMISSION("Duplicate permission defined for group %1$s. Only one permission per group is allowed.", Category.USER_INPUT, 80);

        /**
         * Message of the exception.
         */
        private final String message;

        /**
         * Category of the exception.
         */
        private final Category category;

        /**
         * Detail number of the exception.
         */
        private final int detailNumber;

        private FolderCode(final String message, final Category category, final int detailNumber) {
            this.message = message;
            this.category = category;
            this.detailNumber = detailNumber;
        }

        /**
         * Gets the (detail) number.
         * 
         * @return The (detail) number.
         */
        public int getNumber() {
            return detailNumber;
        }

        /**
         * Gets the (unformatted) message.
         * 
         * @return The (unformatted) message.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Gets the category.
         * 
         * @return The category.
         */
        public Category getCategory() {
            return category;
        }
    }

    private static final transient Object[] EMPTY_ARGS = new Object[0];

    /**
     * Constructs a new {@link OXFolderException} from specified {@link AbstractOXException init cause}.
     * 
     * @param e The init cause
     */
    public OXFolderException(final AbstractOXException e) {
        super(e);
    }

    /**
     * Constructs a new {@link OXFolderException} from specified {@link FolderCode folder error code} only.
     * 
     * @param folderCode The folder error code
     */
    public OXFolderException(final FolderCode folderCode) {
        super(EnumComponent.FOLDER, folderCode.category, folderCode.detailNumber, folderCode.message, null, EMPTY_ARGS);
    }

    /**
     * Constructs a new {@link OXFolderException}
     * 
     * @param folderCode The folder error code
     * @param messageArgs The error code's message arguments
     */
    public OXFolderException(final FolderCode folderCode, final Object... messageArgs) {
        super(EnumComponent.FOLDER, folderCode.category, folderCode.detailNumber, folderCode.message, null, messageArgs);
    }

    /**
     * Constructs a new {@link OXFolderException}
     * 
     * @param folderCode The folder error code
     * @param cause The init cause
     * @param messageArgs The error code's message arguments
     */
    public OXFolderException(final FolderCode folderCode, final Throwable cause, final Object... messageArgs) {
        super(EnumComponent.FOLDER, folderCode.category, folderCode.detailNumber, folderCode.message, cause, messageArgs);
    }

    /**
     * Constructs a new {@link OXFolderException}
     * 
     * @param folderCode The folder error code
     * @param category The error's category
     * @param messageArgs The error code's message arguments
     */
    public OXFolderException(final FolderCode folderCode, final Category category, final Object... messageArgs) {
        super(EnumComponent.FOLDER, category, folderCode.detailNumber, folderCode.message, null, messageArgs);
    }
}
