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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

import static com.openexchange.groupware.infostore.InfostoreExceptionMessages.*;
import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * {@link InfostoreExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum InfostoreExceptionCodes implements OXErrorMessage {
    TOO_LONG_VALUES(TOO_LONG_VALUES_MSG, Category.TRUNCATED, 100, TOO_LONG_VALUES_HELP),
    /** Invalid SQL Query: %s */
    SQL_PROBLEM(INVALID_SQL_QUERY_MSG, Category.CODE_ERROR, 200, INVALID_SQL_QUERY_HELP),
    /** Cannot pre-fetch results. */
    PREFETCH_FAILED(PREFETCH_FAILED_MSG, Category.TRY_AGAIN, 219, PREFETCH_FAILED_HELP),
    /** The requested item does not exist. */
    NOT_EXIST(NOT_EXIST_MSG, Category.USER_INPUT, 300, NOT_EXIST_HELP),
    /** Could not load documents to check the permissions */
    COULD_NOT_LOAD(COULD_NOT_LOAD_MSG, Category.USER_INPUT, 301, COULD_NOT_LOAD_HELP),
    /** The folder %d is not an Infostore folder */
    NOT_INFOSTORE_FOLDER(NOT_INFOSTORE_FOLDER_MSG, Category.CODE_ERROR, 302, NOT_INFOSTORE_FOLDER_HELP),
    /** You do not have sufficient read permissions to read objects in this folder. */
    NO_READ_PERMISSION(NO_READ_PERMISSION_MSG, Category.PERMISSION, 400, NO_READ_PERMISSION_HELP),
    /** You do not have sufficient permissions to create objects in this folder. */
    NO_CREATE_PERMISSION(NO_CREATE_PERMISSION_MSG, Category.PERMISSION, 402, NO_CREATE_PERMISSION_HELP),
    /** You are not allowed to update this item. */
    NO_WRITE_PERMISSION(NO_WRITE_PERMISSION_MSG, Category.PERMISSION, 403, NO_WRITE_PERMISSION_HELP),
    /** You are not allowed to create objects in the target folder. */
    NO_TARGET_CREATE_PERMISSION(NO_TARGET_CREATE_PERMISSION_MSG, Category.PERMISSION, 404, NO_TARGET_CREATE_PERMISSION_HELP),
    /** Could not delete all objects. */
    NOT_ALL_DELETED(NOT_ALL_DELETED_MSG, Category.CONCURRENT_MODIFICATION, 405, NOT_ALL_DELETED_HELP),
    /** You do not have sufficient permission to delete this version. */
    NO_DELETE_PERMISSION_FOR_VERSION(NO_DELETE_PERMISSION_FOR_VERSION_MSG, Category.PERMISSION, 406, NO_DELETE_PERMISSION_FOR_VERSION_HELP),
    /** Could not iterate result. */
    ITERATE_FAILED(ITERATE_FAILED_MSG, Category.CODE_ERROR, 413, ITERATE_FAILED_HELP),
    /** This document is locked. */
    ALREADY_LOCKED(ALREADY_LOCKED_MSG, Category.CONCURRENT_MODIFICATION, 415, ALREADY_LOCKED_HELP),
    /** You cannot unlock this document. */
    LOCKED_BY_ANOTHER(LOCKED_BY_ANOTHER_MSG, Category.CONCURRENT_MODIFICATION, 416, LOCKED_BY_ANOTHER_HELP),
    /** You need write permissions to unlock a document. */
    WRITE_PERMS_FOR_UNLOCK_MISSING(WRITE_PERMS_FOR_UNLOCK_MISSING_MSG, Category.PERMISSION, 417, WRITE_PERMS_FOR_UNLOCK_MISSING_HELP),
    /** You need write permissions to lock a document. */
    WRITE_PERMS_FOR_LOCK_MISSING(WRITE_PERMS_FOR_LOCK_MISSING_MSG, Category.PERMISSION, 418, WRITE_PERMS_FOR_LOCK_MISSING_HELP),
    /** Could not generate new ID. */
    NEW_ID_FAILED(NEW_ID_FAILED_MSG, Category.CODE_ERROR, 420, NEW_ID_FAILED_HELP),
    /** You are not allowed to delete objects in the source folder, so this document cannot be moved. */
    NO_SOURCE_DELETE_PERMISSION(NO_SOURCE_DELETE_PERMISSION_MSG, Category.PERMISSION, 421, NO_SOURCE_DELETE_PERMISSION_HELP),
    /** The document you requested doesn't exist. */
    DOCUMENT_NOT_EXIST(DOCUMENT_NOT_EXISTS_MSG, Category.USER_INPUT, 438, DOCUMENT_NOT_EXISTS_HELP),
    /** Files attached to InfoStore items must have unique names. Filename: %s. The other document with this file name is %s. */
    FILENAME_NOT_UNIQUE(FILENAME_NOT_UNIQUE_MSG, Category.USER_INPUT, 441, FILENAME_NOT_UNIQUE_HELP),
    /** Could not determine number of versions for infoitem %s in context %s. Invalid Query: %s */
    NUMBER_OF_VERSIONS_FAILED(NUMBER_OF_VERSIONS_FAILED_MSG, Category.CODE_ERROR, 442, NUMBER_OF_VERSIONS_FAILED_HELP),
    /** You do not have the permissions to delete at least one of the info items. */
    NO_DELETE_PERMISSION(NO_DELETE_PERMISSION_MSG, Category.PERMISSION, 445, NO_DELETE_PERMISSION_HELP),
    /** Illegal argument: Document %d contains no file */
    DOCUMENT_CONTAINS_NO_FILE(DOCUMENT_CONTAINS_NO_FILE_MSG, Category.CODE_ERROR, 500, DOCUMENT_CONTAINS_NO_FILE_HELP),
    /** Folder %d has two subfolders named %s. The database for context %d is not consistent. */
    DUPLICATE_SUBFOLDER(DUPLICATE_SUBFOLDER_MSG, Category.CODE_ERROR, 501, DUPLICATE_SUBFOLDER_HELP),
    /** In order to accomplish the search, %1$d or more characters are required. */
    PATTERN_NEEDS_MORE_CHARACTERS(PATTERN_NEEDS_MORE_CHARACTERS_MSG, Category.USER_INPUT, 602, PATTERN_NEEDS_MORE_CHARACTERS_HELP),
    /** Could not delete DocumentMetadata %d. Please try again. */
    DELETE_FAILED(DELETE_FAILED_MSG, Category.CONCURRENT_MODIFICATION, 700, DELETE_FAILED_HELP),
    /** The document could not be updated because it was modified. Reload the view. */
    MODIFIED_CONCURRENTLY(MODIFIED_CONCURRENTLY_MSG, Category.CONCURRENT_MODIFICATION, 1302, MODIFIED_CONCURRENTLY_HELP),
    /** The document was updated in between do and undo. The Database is now probably inconsistent. */
    UPDATED_BETWEEN_DO_AND_UNDO(UPDATED_BETWEEN_DO_AND_UNDO_MSG, Category.CONCURRENT_MODIFICATION, 1303, UPDATED_BETWEEN_DO_AND_UNDO_HELP),
    /** This folder is a virtual folder. It cannot contain documents. */
    NO_DOCUMENTS_IN_VIRTUAL_FOLDER(NO_DOCUMENTS_IN_VIRTUAL_FOLDER_MSG, Category.USER_INPUT, 1700, NO_DOCUMENTS_IN_VIRTUAL_FOLDER_HELP),
    /** Validation failed: %s */
    VALIDATION_FAILED(VALIDATION_FAILED_MSG, Category.USER_INPUT, 2100, VALIDATION_FAILED_HELP),
    ;

    private final String message;
    private final Category category;
    private final int number;
    private final String help;

    private InfostoreExceptionCodes(String message, Category category, int number, String help) {
        this.message = message;
        this.category = category;
        this.number = number;
        this.help = help;
    }

    public int getDetailNumber() {
        return number;
    }

    public String getMessage() {
        return message;
    }

    public String getHelp() {
        return help;
    }

    public Category getCategory() {
        return category;
    }

    public InfostoreException create(Object... args) {
        return InfostoreExceptionFactory.getInstance().create(this, args);
    }

    public InfostoreException create(Throwable cause, Object... args) {
        return InfostoreExceptionFactory.getInstance().create(this, cause, args);
    }
}
