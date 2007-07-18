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
import com.openexchange.groupware.Component;

public class OXFolderException extends OXException {

	private static final long serialVersionUID = -9109199682724280886L;

	public static final int DETAIL_NUMBER_CONCURRENT_MODIFICATION = 1;

	public static enum FolderCode {
		/**
		 * User %s has no access to module %s in context %s due to user
		 * configuration
		 * <p>
		 * Requested operation was canceled because underlying user
		 * configuration denies folder access due to module restrictions
		 * </p>
		 */
		NO_MODULE_ACCESS("User %s has no access to module %s in context %s due to user configuration",
				Category.USER_CONFIGURATION, 2),
		/**
		 * Folder %s not visible to user %s in context %s
		 * <p>
		 * Either underlying user configuration or folder permission setting
		 * denies visibility of folder in question
		 * </p>
		 */
		NOT_VISIBLE("Folder %s not visible to user %s in context %s", Category.PERMISSION, 3),
		/**
		 * User %s has no access on shared folder %s in context %s due to user
		 * configuration
		 * <p>
		 * Underlying user configuration denies access to shared folder(s)
		 * </p>
		 */
		NO_SHARED_FOLDER_ACCESS("User %s has no access on shared folder %s in context %s due to user configuration",
				Category.USER_CONFIGURATION, 4),
		/**
		 * User %s has no write access on public folder %s in context %s due to
		 * user configuration
		 * <p>
		 * Underlying user configuration denies write access to public folder(s)
		 * </p>
		 */
		NO_PUBLIC_FOLDER_WRITE_ACCESS(
				"User %s has no write access on public folder %s in context %s due to user configuration",
				Category.USER_CONFIGURATION, 5),
		/**
		 * User %s has no create-subfolder permission on folder %s in context %s
		 * <p>
		 * Folder permission setting denies subfolder creation beneath folder in
		 * question
		 * </p>
		 */
		NO_CREATE_SUBFOLDER_PERMISSION("User %s has no create-subfolder permission on folder %s in context %s",
				Category.PERMISSION, 6),
		/**
		 * Missing field %s in folder %s in context %s
		 * <p>
		 * Operation was canceled due to a missing folder attribute
		 * </p>
		 */
		MISSING_FOLDER_ATTRIBUTE("Missing field %s in folder %s in context %s", Category.CODE_ERROR, 7),
		/**
		 * Folder %s does not exist in context %s
		 * <p>
		 * Folder does not exist
		 * </p>
		 */
		NOT_EXISTS("Folder %s does not exist in context %s", Category.CODE_ERROR, 8),
		/**
		 * Folder %s has been modified after last sync timestamp in context %s
		 * <p>
		 * Client timestamp is before folder's last-changed timestamp
		 * </p>
		 */
		CONCURRENT_MODIFICATION("Folder %s has been modified after last sync timestamp in context %s",
				Category.CONCURRENT_MODIFICATION, 9),
		/**
		 * User %s has no admin access to folder %s in context %s
		 * <p>
		 * No necessary admin access granted for update operation
		 * </p>
		 */
		NO_ADMIN_ACCESS("User %s has no admin access to folder %s in context %s", Category.PERMISSION, 10),
		/**
		 * Default folder %s cannot be renamed in context %s
		 * <p>
		 * Default folder must not be renamed
		 * </p>
		 */
		NO_DEFAULT_FOLDER_RENAME("Default folder %s cannot be renamed in context %s", Category.PERMISSION, 11),
		/**
		 * A duplicate folder exists in parent folder %s in context %s
		 * <p>
		 * Rename aborted cause a duplicate folder exists beneath parent folder
		 * </p>
		 */
		NO_DUPLICATE_FOLDER("A duplicate folder exists in parent folder %s in context %s", Category.PERMISSION, 12),
		/**
		 * Parent folder %s does not allow folder's type setting %s in context
		 * %s
		 * <p>
		 * Folder's type setting is invalid
		 * </p>
		 */
		INVALID_TYPE("Parent folder %s does not allow folder's type setting %s in context %s", Category.CODE_ERROR, 13),
		/**
		 * Parent folder %s does not allow folder's module setting (%s) in
		 * context %s
		 * <p>
		 * Folder's module setting is invalid
		 * </p>
		 */
		INVALID_MODULE("Parent folder %s does not allow folder's module setting (%s) in context %s",
				Category.CODE_ERROR, 14),
		/**
		 * Only one admin permission is allowed on a private folder
		 * <p>
		 * Only one admin is allowed on a private folder
		 * </p>
		 */
		ONLY_ONE_PRIVATE_FOLDER_ADMIN("Only one admin permission is allowed on a private folder", Category.CODE_ERROR,
				15),
		/**
		 * A group must not hold admin permission on a private folder
		 * <p>
		 * No admin group permission is allowed on a private folder
		 * </p>
		 */
		NO_PRIVATE_FOLDER_ADMIN_GROUP("A group must not hold admin permission on a private folder",
				Category.CODE_ERROR, 16),
		/**
		 * Only folder owner may hold admin permission on a private folder
		 * <p>
		 * Only folder owner may hold admin permission on a private folder
		 * </p>
		 */
		ONLY_PRIVATE_FOLDER_OWNER_ADMIN("Only folder owner may hold admin permission on a private folder",
				Category.CODE_ERROR, 17),
		/**
		 * No admin permission found
		 * <p>
		 * No entity has been defined as admin
		 * </p>
		 */
		NO_FOLDER_ADMIN("No admin permission found", Category.CODE_ERROR, 18),
		/**
		 * Invalid object ID %s
		 * <p>
		 * An invalid object ID
		 * </p>
		 */
		INVALID_OBJECT_ID("Invalid object ID %s", Category.CODE_ERROR, 19),
		/**
		 * Not allowed to change parent id of folder %s through an update call.
		 * Use move method instead
		 * <p>
		 * Folder's parent id is changed during an update operation
		 * </p>
		 */
		NO_MOVE_THROUGH_UPDATE(
				"Not allowed to change parent id of folder %s through an update call. Use move method instead",
				Category.CODE_ERROR, 20),
		/**
		 * Not allowed to move default folder %s in context %s
		 * <p>
		 * Default folder must not be moved
		 * </p>
		 */
		NO_DEFAULT_FOLDER_MOVE("Not allowed to move default folder %s in context %s", Category.PERMISSION, 21),
		/**
		 * Target folder %s contains a duplicate folder in context %s
		 * <p>
		 * Target folder contains a duplicate folder
		 * </p>
		 */
		TARGET_FOLDER_CONTAINS_DUPLICATE("Target folder %s contains a duplicate folder in context %s",
				Category.PERMISSION, 22),
		/**
		 * Shared folder %s cannot be moved in context %s
		 * <p>
		 * A shared folder must not be moved
		 * </p>
		 */
		NO_SHARED_FOLDER_MOVE("Shared folder %s cannot be moved in context %s", Category.PERMISSION, 23),
		/**
		 * Shared folder %s cannot be target of move operation in context %s
		 * <p>
		 * A shared folder must not be target of a move operation
		 * </p>
		 */
		NO_SHARED_FOLDER_TARGET("Shared folder %s cannot be target of move operation in context %s",
				Category.PERMISSION, 24),
		/**
		 * System folder %s cannot be moved in context %s
		 * <p>
		 * A system folder must not be moved
		 * </p>
		 */
		NO_SYSTEM_FOLDER_MOVE("System folder %s cannot be moved in context %s", Category.PERMISSION, 25),
		/**
		 * Private folder %s can only be moved to a private folder in context %s
		 * <p>
		 * A private folder may only be moved to a private folder
		 * </p>
		 */
		ONLY_PRIVATE_TO_PRIVATE_MOVE("Private folder %s can only be moved to a private folder in context %s",
				Category.PERMISSION, 26),
		/**
		 * Public folder %s may only be moved to a public folder in context %s
		 * <p>
		 * A public folder may only be moved to a public folder
		 * </p>
		 */
		ONLY_PUBLIC_TO_PUBLIC_MOVE("Public folder %s can only be moved to a public folder in context %s",
				Category.PERMISSION, 27),
		/**
		 * Target and source folder must not be equal in context %s
		 * <p>
		 * Target and source folder must not be equal
		 * </p>
		 */
		NO_EQUAL_MOVE("Target and source folder cannot be equal in context %s", Category.PERMISSION, 28),
		/**
		 * User %s is not allowed to move all subfolders of folder %s in context
		 * %s
		 * <p>
		 * User is not allowed to move source folder's subfolder
		 * </p>
		 */
		NO_SUBFOLDER_MOVE_ACCESS("User %s is not allowed to move all subfolders of folder %s in context %s",
				Category.PERMISSION, 29),
		/**
		 * User %s is not allowed to delete shared folder %s in context %s
		 * <p>
		 * A shared folder must not be deleted
		 * </p>
		 */
		NO_SHARED_FOLDER_DELETION("User %s is not allowed to delete shared folder %s in context %s",
				Category.PERMISSION, 30),
		/**
		 * User %s is not allowed to delete default folder %s in context %s
		 * <p>
		 * Default folder(s) must not be deleted
		 * </p>
		 */
		NO_DEFAULT_FOLDER_DELETION("User %s is not allowed to delete default folder %s in context %s",
				Category.PERMISSION, 31),
		/**
		 * User %s is not allowed to delete all contained objects in folder %s
		 * in context %s
		 * <p>
		 * User is not allowed to delete all objects contained in folder in
		 * question
		 * </p>
		 */
		NOT_ALL_OBJECTS_DELETION("User %s is not allowed to delete all contained objects in folder %s in context %s",
				Category.PERMISSION, 32),
		/**
		 * No admin user found in context %s
		 * <p>
		 * No admin user was found in context in question
		 * </p>
		 */
		NO_ADMIN_USER_FOUND_IN_CONTEXT("No admin user found in context %s", Category.CODE_ERROR, 33),
		/**
		 * No default folder could be found in module %s for user %s in context
		 * %s
		 * <p>
		 * No default folder was found for current user in given module and
		 * context
		 * </p>
		 */
		NO_DEFAULT_FOLDER_FOUND("No default folder could be found in module %s for user %s in context %s",
				Category.CODE_ERROR, 34),
		/**
		 * Folder %s could not be loaded in context %s
		 * <p>
		 * Folder could not be loaded from storage
		 * </p>
		 */
		FOLDER_COULD_NOT_BE_LOADED("Folder %s could not be loaded in context %s", Category.CODE_ERROR, 35),
		/**
		 * Folder %s could not be put into cache in context %s
		 * <p>
		 * Folder could not be put into cache
		 * </p>
		 */
		FOLDER_COULD_NOT_BE_PUT_INTO_CACHE("Folder %s could not be put into cache in context %s",
				Category.INTERNAL_ERROR, 36),
		/**
		 * Effective permission of Folder %s could not determined for user %s in
		 * context %s
		 * <p>
		 * User's effective permission on folder could not be determined
		 * </p>
		 */
		NO_EFFECTIVE_PERMISSION("Effective permission of Folder %s could not determined for user %s in context %s",
				Category.CODE_ERROR, 37),
		/**
		 * A SQL error occurred in context %s
		 */
		SQL_ERROR("An SQL error occurred in context %s", Category.CODE_ERROR, 38),
		/**
		 * A DBPool error occurred in context %s
		 */
		DBPOOLING_ERROR("An SQL error occurred in context %s", Category.CODE_ERROR, 39),
		/**
		 * Caller does not allow to fetch a connection from pool in context %s
		 */
		NO_CONNECTION_FETCH("Caller does not allow to fetch a connection from pool in context %s", Category.CODE_ERROR,
				40),
		/**
		 * Delivered sequence id %s from database is less than allowed min
		 * folder id of %s in context %s
		 */
		INVALID_SEQUENCE_ID(
				"Delivered sequence id %s from database is less than allowed min folder id of %s in context %s",
				Category.CODE_ERROR, 41),
		/**
		 * Module %s is unknown in context %s
		 */
		UNKNOWN_MODULE("Module %s is unknown in context %s", Category.CODE_ERROR, 42),
		/**
		 * Folder %s could not be updated in context %s
		 * <p>
		 * Folder update failed for any reason
		 * </p>
		 */
		UPDATE_FAILED("Folder %s could not be updated in context %s", Category.CODE_ERROR, 43),
		/**
		 * Invalid entity id %s detected in permissions of folder %s in context
		 * %s
		 */
		INVALID_ENTITY("Invalid entity id %s detected in permissions of folder %s in context %s", Category.CODE_ERROR,
				44),
		/**
		 * Folder %s must not be moved to one of its subfolders in context %s
		 */
		NO_SUBFOLDER_MOVE("Folder %s must not be moved to one of its subfolders in context %s", Category.CODE_ERROR, 45),
		/**
		 * Inserted for those exception that will turn to an
		 * <code>OXException</code> in future
		 */
		UNKNOWN_EXCEPTION("UNKNOWN EXCEPTION", Category.CODE_ERROR, 46),
		/**
		 * A LDAP error occurred in context %s
		 */
		LDAP_ERROR("A LDAP error occurred in context %s", Category.CODE_ERROR, 47),
		/**
		 * Attribute \"%s\" is not set in FolderObject instance %s in context %s
		 */
		ATTRIBUTE_NOT_SET("Attribute \"%s\" is not set in FolderObject instance %s in context %s", Category.CODE_ERROR,
				48),
		/**
		 * A source folder of module %s must not be moved to a target folder of
		 * module %s
		 */
		INCOMPATIBLE_MODULES("A source folder of module %s must not be moved to a target folder of module %s",
				Category.PERMISSION, 49),
		/**
		 * Operation not executeable on folder %s in context %sResponse
		 */
		UNSUPPORTED_OPERATION("Operation not executeable on folder %s in context %s", Category.CODE_ERROR, 50),
		/**
		 * Folder cache (region name = %s) could not be initialized due to
		 * following reason: %s
		 */
		FOLDER_CACHE_INITIALIZATION_FAILED(
				"Folder cache (region name = %s) could not be initialized due to following reason: %s",
				Category.CODE_ERROR, 51),
		/**
		 * Folder cache has not been enabled in config file %s
		 */
		CACHE_NOT_ENABLED("Folder cache has not been enabled in config file %s", Category.CODE_ERROR, 52),
		/**
		 * Folder %s could not be removed from folder cache
		 */
		CACHE_ERROR_ON_REMOVE("Folder %s could not be removed from folder cache", Category.CODE_ERROR, 53),
		/**
		 * User %s has no write permission on folder %s in context %s
		 */
		NO_WRITE_PERMISSION("User %s has no write permission on folder %s in context %s", Category.PERMISSION, 54),
		/**
		 * A JSON error occurred: %s
		 */
		JSON_ERROR("A JSON error occurred: %s", Category.CODE_ERROR, 55),
		/**
		 * Unknown parameter container type: %d
		 */
		UNKNOWN_PARAMETER_CONTAINER_TYPE("Unknown parameter container type: %d", Category.CODE_ERROR, 56),
		/**
		 * Missing parameter %s
		 */
		MISSING_PARAMETER("Missing parameter %s", Category.CODE_ERROR, 57),
		/**
		 * Bad value %s in parameter %s
		 */
		BAD_PARAM_VALUE("Bad value %s in parameter %s", Category.USER_INPUT, 58),
		/**
		 * Unknown field: %d
		 */
		UNKNOWN_FIELD("Unknown field: %d", Category.CODE_ERROR, 59),
		/**
		 * Parameter %s does not match JSON key %s
		 */
		PARAMETER_MISMATCH("Parameter %s does not match JSON key %s", Category.CODE_ERROR, 60),
		/**
		 * Invalid permission values: fp=%d orp=%d owp=%d odp=%d
		 */
		INVALID_PERMISSION("Invalid permission values: fp=%d orp=%d owp=%d odp=%d", Category.CODE_ERROR, 61),
		/**
		 * Unknown action: %s
		 */
		UNKNOWN_ACTION("Unknown action: %s", Category.CODE_ERROR, 62),
		/**
		 * Shared folder %s MUST NOT be updated in context %s
		 */
		NO_SHARED_FOLDER_UPDATE("Shared folder %s MUST NOT be updated in context %s", Category.PERMISSION, 63),
		/**
		 * Specified folder name is too long
		 */
		TRUNCATED("Specified folder name is too long", Category.TRUNCATED, 64),
		/**
		 * Unable to map OCL permission value %s to a JSON permission value
		 */
		MAP_PERMISSION_FAILED("Unable to map OCL permission value %s to a JSON permission value", Category.CODE_ERROR,
				65),
		/**
		 * Folder existence cannot be checked due to unsufficient folder
		 * information
		 */
		UNSUFFICIENT_FOLDER_INFORMATION("Folder existence cannot be checked due to unsufficient folder information)",
				Category.CODE_ERROR, 66),
		/**
		 * A runtime error occurred in context %s
		 */
		RUNTIME_ERROR("A runtime error occurred in context %s", Category.INTERNAL_ERROR, 67),
		/**
		 * This method is not applicable to an IMAP permission
		 */
		IMAP_PERMISSION_ERROR("This method is not applicable to an IMAP permission", Category.CODE_ERROR, 68),
		/**
		 * No subfolder creation underneath shared folder %s in context %s
		 */
		NO_SUBFOLDER_BELOW_SHARED_FOLDER("No subfolder creation underneath shared folder %s in context %s",
				Category.PERMISSION, 69),
		/**
		 * User %s grants invalid permissions on shared folder %s in context %s. Only owner of parental shared folder %s may be folder admin
		 */
		INVALID_SHARED_FOLDER_SUBFOLDER_PERMISSION(
				"User %s grants invalid permissions on shared folder %s in context %s. Only owner of parental shared folder %s may be folder admin",
				Category.PERMISSION, 70),
		/**
		 * Owner %s of default folder %s must keep the folder admin permission
		 */
		CREATOR_IS_NOT_ADMIN("Owner %s of default folder %s must keep the folder admin permission",
				Category.USER_INPUT, 71),
		/**
		 * User %s is not allowed to share folder %s in context %s due to user
		 * configuration
		 */
		SHARE_FORBIDDEN("User %s is not allowed to share folder %s in context %s due to user configuration",
				Category.USER_CONFIGURATION, 72),
		/**
		 * Defined permissions for folder %s in context %s are not applicable to
		 * user %s due to user configuration
		 */
		UNAPPLICABLE_FOLDER_PERM(
				"Defined permissions for folder %s in context %s are not applicable to user %s due to user configuration",
				Category.USER_CONFIGURATION, 73),
		/**
		 * Folder %s in context %s contains a hidden subfolder. User %s has no
		 * delete rights for this subfolder and consequently cannot delete its
		 * parent folder.
		 */
		HIDDEN_FOLDER_ON_DELETION(
				"Folder %s in context %s contains a hidden subfolder. User %s has no delete rights for this subfolder and consequently cannot delete its parent folder.",
				Category.PERMISSION, 74);

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

		/**
		 * Default constructor.
		 * 
		 * @param message
		 *            message.
		 * @param category
		 *            category.
		 * @param detailNumber
		 *            detail number.
		 */
		private FolderCode(final String message, final Category category, final int detailNumber) {
			this.message = message;
			this.category = category;
			this.detailNumber = detailNumber;
		}

		public int getNumber() {
			return detailNumber;
		}

		public String getMessage() {
			return message;
		}

		public Category getCategory() {
			return category;
		}
	}

	private static final transient Object[] EMPTY_ARGS = new Object[0];

	public OXFolderException(final FolderCode folderCode) {
		this(folderCode, (Throwable) null, EMPTY_ARGS);
	}

	public OXFolderException(final FolderCode folderCode, final Object... messageArgs) {
		this(folderCode, (Throwable) null, messageArgs);
	}

	public OXFolderException(final FolderCode folderCode, final Throwable cause, final Object... messageArgs) {
		super(Component.FOLDER, folderCode.category, folderCode.detailNumber, folderCode.message, cause, messageArgs);
	}

	public OXFolderException(final FolderCode folderCode, final Category category, final Object... messageArgs) {
		super(Component.FOLDER, category, folderCode.detailNumber, folderCode.message, null, messageArgs);
	}
}
