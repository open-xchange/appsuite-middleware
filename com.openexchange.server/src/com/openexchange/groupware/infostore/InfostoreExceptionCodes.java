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

package com.openexchange.groupware.infostore;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.groupware.EnumComponent;

/**
 * {@link InfostoreExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum InfostoreExceptionCodes implements DisplayableOXExceptionCode {
    /**
     * Some field values are too long.
     */
    TOO_LONG_VALUES("Some field values are too long.", CATEGORY_TRUNCATED, 100),
    /**
     * Unexpected database error: \"%1$s\"
     */
    SQL_PROBLEM("Unexpected database error: \"%1$s\"", CATEGORY_ERROR, 200, OXExceptionStrings.SQL_ERROR_MSG),
    /**
     * Cannot pre-fetch results.
     */
    PREFETCH_FAILED("Cannot pre-fetch results.", CATEGORY_TRY_AGAIN, 219),
    /**
     * The requested item does not exist.
     */
    NOT_EXIST("The requested item does not exist.", CATEGORY_USER_INPUT, 300, InfostoreExceptionMessages.NOT_EXIST_MSG_DISPLAY),
    /**
     * Could not load documents to check the permissions
     */
    COULD_NOT_LOAD("Could not load documents to check the permissions", CATEGORY_USER_INPUT, 301, InfostoreExceptionMessages.COULD_NOT_LOAD_MSG_DISPLAY),
    /**
     * The folder %d is not an Infostore folder
     */
    NOT_INFOSTORE_FOLDER("The folder %1$s is not an Infostore folder", CATEGORY_ERROR, 302, InfostoreExceptionMessages.NOT_INFOSTORE_FOLDER_MSG_DISPLAY),
    /**
     * You do not have sufficient read permissions to read objects in this folder.
     */
    NO_READ_PERMISSION("You do not have sufficient read permissions to read objects in this folder..", CATEGORY_PERMISSION_DENIED, 400, InfostoreExceptionMessages.NO_READ_PERMISSION_MSG_DISPLAY),
    /**
     * You do not have sufficient permissions to create objects in this folder.
     */
    NO_CREATE_PERMISSION("You do not have sufficient permissions to create objects in this folder.", CATEGORY_PERMISSION_DENIED, 402, InfostoreExceptionMessages.NO_CREATE_PERMISSION_MSG_DISPLAY),
    /**
     * You are not allowed to update this item.
     */
    NO_WRITE_PERMISSION("You are not allowed to update this item.", CATEGORY_PERMISSION_DENIED, 403, InfostoreExceptionMessages.NO_WRITE_PERMISSION_MSG_DISPLAY),
    /**
     * Could not delete all objects.
     */
    NOT_ALL_DELETED("Could not delete all objects.", CATEGORY_ERROR, 405, InfostoreExceptionMessages.NOT_ALL_DELETED_MSG_DISPLAY),
    /**
     * You do not have sufficient permission to delete this version.
     */
    NO_DELETE_PERMISSION_FOR_VERSION("You do not have sufficient permissions to delete this version.", CATEGORY_PERMISSION_DENIED, 406, InfostoreExceptionMessages.NO_DELETE_PERMISSION_FOR_VERSION_MSG_DISPLAY),
    /**
     * Could not iterate result.
     */
    ITERATE_FAILED("Could not iterate result.", CATEGORY_ERROR, 413),
    /**
     * This document is locked.
     */
    CURRENTLY_LOCKED("This document is locked.", CATEGORY_CONFLICT, 415, InfostoreExceptionMessages.CURRENTLY_LOCKED_MSG_DISPLAY),
    /**
     * This document is locked.
     */
    ALREADY_LOCKED("This document is locked.", CATEGORY_CONFLICT, 415, InfostoreExceptionMessages.CURRENTLY_LOCKED_MSG_DISPLAY), // Copy for legacy reasons
    /**
     * You cannot unlock this document.
     */
    LOCKED_BY_ANOTHER("You cannot unlock this document.", CATEGORY_CONFLICT, 416),
    /**
     * You need write permissions to unlock a document.
     */
    WRITE_PERMS_FOR_UNLOCK_MISSING("You need write permissions to unlock a document.", CATEGORY_PERMISSION_DENIED, 417, InfostoreExceptionMessages.WRITE_PERMS_FOR_UNLOCK_MISSING_MSG_DISPLAY),
    /**
     * You need write permissions to lock a document.
     */
    WRITE_PERMS_FOR_LOCK_MISSING("You need write permissions to lock a document.", CATEGORY_PERMISSION_DENIED, 418, InfostoreExceptionMessages.WRITE_PERMS_FOR_LOCK_MISSING_MSG_DISPLAY),
    /**
     * Could not generate new ID.
     */
    NEW_ID_FAILED("Could not generate new ID.", CATEGORY_ERROR, 420),
    /**
     * You are not allowed to delete objects in the source folder, so this document cannot be moved.
     */
    NO_SOURCE_DELETE_PERMISSION("You are not allowed to delete objects in the source folder. This document cannot be moved.", CATEGORY_PERMISSION_DENIED, 421, InfostoreExceptionMessages.NO_SOURCE_DELETE_PERMISSION_MSG_DISPLAY),
    /**
     * The document you requested does not exist.
     */
    DOCUMENT_NOT_EXIST("The document you requested does not exist.", CATEGORY_USER_INPUT, 438, InfostoreExceptionMessages.DOCUMENT_NOT_EXISTS_MSG_DISPLAY),
    /**
     * Files attached to InfoStore items must have unique names. File name: %s. The other document with this file name is %s.
     */
    FILENAME_NOT_UNIQUE("Files attached to InfoStore items must have unique names. File name: %1$s. The other document with this file name is %2$s.", CATEGORY_USER_INPUT, 441, InfostoreExceptionMessages.FILENAME_NOT_UNIQUE_MSG_DISPLAY),
    /**
     * Could not determine number of versions for infoitem %s in context %s. Invalid Query: %s
     */
    NUMBER_OF_VERSIONS_FAILED("Could not determine number of versions for info item %1$s in context %s. Invalid query: %2$s", CATEGORY_ERROR, 442),
    /**
     * You do not have the permissions to delete at least one of the info items.
     */
    NO_DELETE_PERMISSION("You do not have the permissions to delete at least one of the info items.", CATEGORY_PERMISSION_DENIED, 445, InfostoreExceptionMessages.NO_DELETE_PERMISSION_MSG_DISPLAY),
    /**
     * Illegal argument: Document %d contains no file
     */
    DOCUMENT_CONTAINS_NO_FILE("Illegal argument: document %1$s contains no file", CATEGORY_ERROR, 500),
    /**
     * Folder %d has two subfolders named %s. The database for context %d is not consistent.
     */
    DUPLICATE_SUBFOLDER("Folder %1$s has two subfolders named %2$s. The database for context %3$s is not consistent.", CATEGORY_ERROR, 501),
    /**
     * In order to accomplish the search, %1$d or more characters are required.
     */
    PATTERN_NEEDS_MORE_CHARACTERS("In order to accomplish the search, %1$s or more characters are required.", CATEGORY_USER_INPUT, 602, InfostoreExceptionMessages.PATTERN_NEEDS_MORE_CHARACTERS_MSG_DISPLAY),
    /**
     * Could not delete DocumentMetadata %d. Please try again.
     */
    DELETE_FAILED("DocumentMetadata %1$s could not be deleted. Please try again.", CATEGORY_CONFLICT, 700),
    /**
     * The document could not be updated because it was modified. Reload the view.
     */
    MODIFIED_CONCURRENTLY("The document could not be updated because it was modified. Reload the view.", CATEGORY_CONFLICT, 1302, InfostoreExceptionMessages.MODIFIED_CONCURRENTLY_MSG_DISPLAY),
    /**
     * The document was updated in between do and undo. The Database is now probably inconsistent.
     */
    UPDATED_BETWEEN_DO_AND_UNDO("The document was updated in between do and undo. The database is now probably inconsistent.", CATEGORY_CONFLICT, 1303),
    /**
     * This folder is a virtual folder. It cannot contain documents.
     */
    NO_DOCUMENTS_IN_VIRTUAL_FOLDER("This folder is a virtual folder. It cannot contain documents.", CATEGORY_USER_INPUT, 1700, InfostoreExceptionMessages.NO_DOCUMENTS_IN_VIRTUAL_FOLDER_MSG_DISPLAY),
    /**
     * Validation failed: %1$s
     */
    VALIDATION_FAILED("Validation failed: %1$s", CATEGORY_USER_INPUT, 2100),
    /**
     * File name must not contain slashes.
     */
    VALIDATION_FAILED_SLASH("File name must not contain slashes.", CATEGORY_USER_INPUT, 2101, InfostoreExceptionMessages.VALIDATION_FAILED_SLASH_MSG_DISPLAY),
    /**
     * File name contains illegal characters.
     */
    VALIDATION_FAILED_CHARACTERS("File name contains invalid characters.", CATEGORY_USER_INPUT, 2101, InfostoreExceptionMessages.VALIDATION_FAILED_CHARACTERS_MSG_DISPLAY),
    /**
     * New file versions can't be saved with an offset.
     */
    NO_OFFSET_FOR_NEW_VERSIONS("New file versions can't be saved with an offset.", CATEGORY_USER_INPUT, 2102, InfostoreExceptionMessages.NO_OFFSET_FOR_NEW_VERSIONS_MSG_DISPLAY),
    /**
     * Unsupported character "%1$s" in field "%2$s".
     */
    INVALID_CHARACTER("Unsupported character \"%1$s\" in field \"%2$s\".", CATEGORY_USER_INPUT, 2103, InfostoreExceptionMessages.INVALID_CHARACTER_MSG_DISPLAY),
    /**
     * Unsupported character.
     */
    INVALID_CHARACTER_SIMPLE("Unsupported character", CATEGORY_USER_INPUT, 2103 /* Yes, the same as INVALID_CHARACTER */, InfostoreExceptionMessages.INVALID_CHARACTER_SIMPLE_MSG_DISPLAY),
    /**
     * Due to limited capabilities of user \"%1$s\", it is not possible to apply the permission changes.
     */
    VALIDATION_FAILED_INAPPLICABLE_PERMISSIONS("Inapplicable permissions of user \"%1$s\"", CATEGORY_USER_INPUT, 2104, InfostoreExceptionMessages.VALIDATION_FAILED_INAPPLICABLE_PERMISSIONS_MSG_DISPLAY),
    /**
     * Concurrent attempt to write the same version %1$s of document %2$s. Please await the previous save operation to terminate.
     */
    CONCURRENT_VERSION_CREATION("Concurrent attempt to write the same version %1$s of document %2$s. Please await the previous save operation to terminate.", CATEGORY_USER_INPUT, 2105, InfostoreExceptionMessages.CONCURRENT_VERSION_CREATION_MSG_DISPLA),
    /**
     * The search took too long to accomplish the following query for user %1$s in context %2$s: %3$s
     */
    SEARCH_TOOK_TOO_LONG("The search took too long to accomplish the following query for user %1$s in context %2$s: %3$s", CATEGORY_USER_INPUT, 2106, InfostoreExceptionMessages.SEARCH_TOOK_TOO_LONG_MSG_DISPLAY),
    /**
     * Group %1$s can't be used for object permissions.
     */
    VALIDATION_FAILED_INAPPLICABLE_PERMISSIONS_GUEST_GROUP("Group %1$s can't be used for object permissions.", CATEGORY_USER_INPUT, 2107, InfostoreExceptionMessages.VALIDATION_FAILED_INAPPLICABLE_PERMISSIONS_GUEST_GROUP_MSG_DISPLAY),
    ;

    private final String message;
    private final Category category;
    private final int number;
    private final String displayMessage;

    /**
     * Initializes a new {@link InfostoreExceptionCodes}.
     */
    private InfostoreExceptionCodes(final String message, final Category category, final int number) {
        this(message, category, number, null);
    }

    /**
     * Initializes a new {@link InfostoreExceptionCodes}.
     */
    private InfostoreExceptionCodes(final String message, final Category category, final int number, final String displayMessage) {
        this.message = message;
        this.category = category;
        this.number = number;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public String getPrefix() {
        return EnumComponent.INFOSTORE.getAbbreviation();
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayMessage() {
        return this.displayMessage;
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
