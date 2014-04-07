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
    TOO_LONG_VALUES(InfostoreExceptionCodes.TOO_LONG_VALUES_MSG, CATEGORY_TRUNCATED, 100),
    /**
     * Invalid SQL Query: %s
     */
    SQL_PROBLEM(InfostoreExceptionCodes.SQL_PROBLEM_MSG, CATEGORY_ERROR, 200, OXExceptionStrings.SQL_ERROR_MSG),
    /**
     * Cannot pre-fetch results.
     */
    PREFETCH_FAILED(InfostoreExceptionCodes.PREFETCH_FAILED_MSG, CATEGORY_TRY_AGAIN, 219),
    /**
     * The requested item does not exist.
     */
    NOT_EXIST(InfostoreExceptionCodes.NOT_EXIST_MSG, CATEGORY_USER_INPUT, 300, InfostoreExceptionMessages.NOT_EXIST_MSG_DISPLAY),
    /**
     * Could not load documents to check the permissions
     */
    COULD_NOT_LOAD(InfostoreExceptionCodes.COULD_NOT_LOAD_MSG, CATEGORY_USER_INPUT, 301, InfostoreExceptionMessages.COULD_NOT_LOAD_MSG_DISPLAY),
    /**
     * The folder %d is not an Infostore folder
     */
    NOT_INFOSTORE_FOLDER(InfostoreExceptionCodes.NOT_INFOSTORE_FOLDER_MSG, CATEGORY_ERROR, 302, InfostoreExceptionMessages.NOT_INFOSTORE_FOLDER_MSG_DISPLAY),
    /**
     * You do not have sufficient read permissions to read objects in this folder.
     */
    NO_READ_PERMISSION(InfostoreExceptionCodes.NO_READ_PERMISSION_MSG, CATEGORY_PERMISSION_DENIED, 400, InfostoreExceptionMessages.NO_READ_PERMISSION_MSG_DISPLAY),
    /**
     * You do not have sufficient permissions to create objects in this folder.
     */
    NO_CREATE_PERMISSION(InfostoreExceptionCodes.NO_CREATE_PERMISSION_MSG, CATEGORY_PERMISSION_DENIED, 402, InfostoreExceptionMessages.NO_CREATE_PERMISSION_MSG_DISPLAY),
    /**
     * You are not allowed to update this item.
     */
    NO_WRITE_PERMISSION(InfostoreExceptionCodes.NO_WRITE_PERMISSION_MSG, CATEGORY_PERMISSION_DENIED, 403, InfostoreExceptionMessages.NO_WRITE_PERMISSION_MSG_DISPLAY),
    /**
     * Could not delete all objects.
     */
    NOT_ALL_DELETED(InfostoreExceptionCodes.NOT_ALL_DELETED_MSG, CATEGORY_ERROR, 405, InfostoreExceptionMessages.NOT_ALL_DELETED_MSG_DISPLAY),
    /**
     * You do not have sufficient permission to delete this version.
     */
    NO_DELETE_PERMISSION_FOR_VERSION(InfostoreExceptionCodes.NO_DELETE_PERMISSION_FOR_VERSION_MSG, CATEGORY_PERMISSION_DENIED, 406, InfostoreExceptionMessages.NO_DELETE_PERMISSION_FOR_VERSION_MSG_DISPLAY),
    /**
     * Could not iterate result.
     */
    ITERATE_FAILED(InfostoreExceptionCodes.ITERATE_FAILED_MSG, CATEGORY_ERROR, 413),
    /**
     * This document is locked.
     */
    ALREADY_LOCKED(InfostoreExceptionCodes.ALREADY_LOCKED_MSG, CATEGORY_CONFLICT, 415, InfostoreExceptionMessages.ALREADY_LOCKED_MSG_DISPLAY),
    /**
     * You cannot unlock this document.
     */
    LOCKED_BY_ANOTHER(InfostoreExceptionCodes.LOCKED_BY_ANOTHER_MSG, CATEGORY_CONFLICT, 416),
    /**
     * You need write permissions to unlock a document.
     */
    WRITE_PERMS_FOR_UNLOCK_MISSING(InfostoreExceptionCodes.WRITE_PERMS_FOR_UNLOCK_MISSING_MSG, CATEGORY_PERMISSION_DENIED, 417, InfostoreExceptionMessages.WRITE_PERMS_FOR_UNLOCK_MISSING_MSG_DISPLAY),
    /**
     * You need write permissions to lock a document.
     */
    WRITE_PERMS_FOR_LOCK_MISSING(InfostoreExceptionCodes.WRITE_PERMS_FOR_LOCK_MISSING_MSG, CATEGORY_PERMISSION_DENIED, 418, InfostoreExceptionMessages.WRITE_PERMS_FOR_LOCK_MISSING_MSG_DISPLAY),
    /**
     * Could not generate new ID.
     */
    NEW_ID_FAILED(InfostoreExceptionCodes.NEW_ID_FAILED_MSG, CATEGORY_ERROR, 420),
    /**
     * You are not allowed to delete objects in the source folder, so this document cannot be moved.
     */
    NO_SOURCE_DELETE_PERMISSION(InfostoreExceptionCodes.NO_SOURCE_DELETE_PERMISSION_MSG, CATEGORY_PERMISSION_DENIED, 421, InfostoreExceptionMessages.NO_SOURCE_DELETE_PERMISSION_MSG_DISPLAY),
    /**
     * The document you requested does not exist.
     */
    DOCUMENT_NOT_EXIST(InfostoreExceptionCodes.DOCUMENT_NOT_EXISTS_MSG, CATEGORY_USER_INPUT, 438, InfostoreExceptionMessages.DOCUMENT_NOT_EXISTS_MSG_DISPLAY),
    /**
     * Files attached to InfoStore items must have unique names. Filename: %s. The other document with this file name is %s.
     */
    FILENAME_NOT_UNIQUE(InfostoreExceptionCodes.FILENAME_NOT_UNIQUE_MSG, CATEGORY_USER_INPUT, 441, InfostoreExceptionMessages.FILENAME_NOT_UNIQUE_MSG_DISPLAY),
    /**
     * Could not determine number of versions for infoitem %s in context %s. Invalid Query: %s
     */
    NUMBER_OF_VERSIONS_FAILED(InfostoreExceptionCodes.NUMBER_OF_VERSIONS_FAILED_MSG, CATEGORY_ERROR, 442),
    /**
     * You do not have the permissions to delete at least one of the info items.
     */
    NO_DELETE_PERMISSION(InfostoreExceptionCodes.NO_DELETE_PERMISSION_MSG, CATEGORY_PERMISSION_DENIED, 445, InfostoreExceptionMessages.NO_DELETE_PERMISSION_MSG_DISPLAY),
    /**
     * Illegal argument: Document %d contains no file
     */
    DOCUMENT_CONTAINS_NO_FILE(InfostoreExceptionCodes.DOCUMENT_CONTAINS_NO_FILE_MSG, CATEGORY_ERROR, 500),
    /**
     * Folder %d has two subfolders named %s. The database for context %d is not consistent.
     */
    DUPLICATE_SUBFOLDER(InfostoreExceptionCodes.DUPLICATE_SUBFOLDER_MSG, CATEGORY_ERROR, 501),
    /**
     * In order to accomplish the search, %1$d or more characters are required.
     */
    PATTERN_NEEDS_MORE_CHARACTERS(InfostoreExceptionCodes.PATTERN_NEEDS_MORE_CHARACTERS_MSG, CATEGORY_USER_INPUT, 602, InfostoreExceptionMessages.PATTERN_NEEDS_MORE_CHARACTERS_MSG_DISPLAY),
    /**
     * Could not delete DocumentMetadata %d. Please try again.
     */
    DELETE_FAILED(InfostoreExceptionCodes.DELETE_FAILED_MSG, CATEGORY_CONFLICT, 700),
    /**
     * The document could not be updated because it was modified. Reload the view.
     */
    MODIFIED_CONCURRENTLY(InfostoreExceptionCodes.MODIFIED_CONCURRENTLY_MSG, CATEGORY_CONFLICT, 1302, InfostoreExceptionMessages.MODIFIED_CONCURRENTLY_MSG_DISPLAY),
    /**
     * The document was updated in between do and undo. The Database is now probably inconsistent.
     */
    UPDATED_BETWEEN_DO_AND_UNDO(InfostoreExceptionCodes.UPDATED_BETWEEN_DO_AND_UNDO_MSG, CATEGORY_CONFLICT, 1303),
    /**
     * This folder is a virtual folder. It cannot contain documents.
     */
    NO_DOCUMENTS_IN_VIRTUAL_FOLDER(InfostoreExceptionCodes.NO_DOCUMENTS_IN_VIRTUAL_FOLDER_MSG, CATEGORY_USER_INPUT, 1700, InfostoreExceptionMessages.NO_DOCUMENTS_IN_VIRTUAL_FOLDER_MSG_DISPLAY),
    /**
     * Validation failed: %1$s
     */
    VALIDATION_FAILED(InfostoreExceptionCodes.VALIDATION_FAILED_MSG, CATEGORY_USER_INPUT, 2100),
    /**
     * File name must not contain slashes.
     */
    VALIDATION_FAILED_SLASH(InfostoreExceptionCodes.VALIDATION_FAILED_SLASH_MSG, CATEGORY_USER_INPUT, 2101, InfostoreExceptionMessages.VALIDATION_FAILED_SLASH_MSG_DISPLAY),
    /**
     * File name contains illegal characters.
     */
    VALIDATION_FAILED_CHARACTERS(InfostoreExceptionCodes.VALIDATION_FAILED_CHARACTERS_MSG, CATEGORY_USER_INPUT, 2101, InfostoreExceptionMessages.VALIDATION_FAILED_CHARACTERS_MSG_DISPLAY),
    /**
     * New file versions can't be saved with an offset.
     */
    NO_OFFSET_FOR_NEW_VERSIONS(InfostoreExceptionCodes.NO_OFFSET_FOR_NEW_VERSIONS_MSG, CATEGORY_USER_INPUT, 2102, InfostoreExceptionMessages.NO_OFFSET_FOR_NEW_VERSIONS_MSG_DISPLAY);

    private static final String SQL_PROBLEM_MSG = "Invalid SQL Query: %1$s";

    private static final String TOO_LONG_VALUES_MSG = "Some field values are too long.";

    private static final String PREFETCH_FAILED_MSG = "Cannot pre-fetch results.";

    private static final String NOT_EXIST_MSG = "The requested item does not exist.";

    private static final String COULD_NOT_LOAD_MSG = "Could not load documents to check the permissions";

    private static final String NOT_INFOSTORE_FOLDER_MSG = "The folder %1$s is not an Infostore folder";

    private static final String NO_READ_PERMISSION_MSG = "You do not have sufficient read permissions to read objects in this folder..";

    private static final String NO_CREATE_PERMISSION_MSG = "You do not have sufficient permissions to create objects in this folder.";

    private static final String NO_WRITE_PERMISSION_MSG = "You are not allowed to update this item.";

    private static final String NOT_ALL_DELETED_MSG = "Could not delete all objects.";

    private static final String NO_DELETE_PERMISSION_FOR_VERSION_MSG = "You do not have sufficient permissions to delete this version.";

    private static final String ITERATE_FAILED_MSG = "Could not iterate result.";

    private static final String ALREADY_LOCKED_MSG = "This document is locked.";

    private static final String LOCKED_BY_ANOTHER_MSG = "You cannot unlock this document.";

    private static final String WRITE_PERMS_FOR_UNLOCK_MISSING_MSG = "You need write permissions to unlock a document.";

    private static final String WRITE_PERMS_FOR_LOCK_MISSING_MSG = "You need write permissions to lock a document.";

    private static final String NEW_ID_FAILED_MSG = "Could not generate new ID.";

    private static final String NO_SOURCE_DELETE_PERMISSION_MSG = "You are not allowed to delete objects in the source folder. This document cannot be moved.";

    private static final String DOCUMENT_NOT_EXISTS_MSG = "The document you requested does not exist.";

    private static final String FILENAME_NOT_UNIQUE_MSG = "Files attached to InfoStore items must have unique names. File name: %1$s. The other document with this file name is %2$s.";

    private static final String NUMBER_OF_VERSIONS_FAILED_MSG = "Could not determine number of versions for info item %1$s in context %s. Invalid query: %2$s";

    private static final String NO_DELETE_PERMISSION_MSG = "You do not have the permissions to delete at least one of the info items.";

    private static final String DOCUMENT_CONTAINS_NO_FILE_MSG = "Illegal argument: document %1$s contains no file";

    private static final String DUPLICATE_SUBFOLDER_MSG = "Folder %1$s has two subfolders named %2$s. The database for context %3$s is not consistent.";

    private static final String PATTERN_NEEDS_MORE_CHARACTERS_MSG = "In order to accomplish the search, %1$s or more characters are required.";

    private static final String DELETE_FAILED_MSG = "DocumentMetadata %1$s could not be deleted. Please try again.";

    private static final String MODIFIED_CONCURRENTLY_MSG = "The document could not be updated because it was modified. Reload the view.";

    private static final String UPDATED_BETWEEN_DO_AND_UNDO_MSG = "The document was updated in between do and undo. The database is now probably inconsistent.";

    private static final String NO_DOCUMENTS_IN_VIRTUAL_FOLDER_MSG = "This folder is a virtual folder. It cannot contain documents.";

    private static final String VALIDATION_FAILED_MSG = "Validation failed: %1$s";

    private static final String VALIDATION_FAILED_SLASH_MSG = "File name must not contain slashes.";

    private static final String VALIDATION_FAILED_CHARACTERS_MSG = "File name contains invalid characters.";

    private static final String NO_OFFSET_FOR_NEW_VERSIONS_MSG = "New file versions can't be saved with an offset.";

    private final String message;

    private final Category category;

    private final int number;

    /**
     * Message displayed to the user
     */
    private String displayMessage;

    /**
     * Initializes a new {@link InfostoreExceptionCodes}.
     * 
     * @param message
     * @param category
     * @param number
     */
    private InfostoreExceptionCodes(final String message, final Category category, final int number) {
        this(message, category, number, null);
    }

    /**
     * Initializes a new {@link InfostoreExceptionCodes}.
     * 
     * @param message
     * @param category
     * @param number
     * @param displayMessage
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
