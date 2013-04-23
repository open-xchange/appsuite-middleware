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

import static com.openexchange.groupware.infostore.InfostoreExceptionMessages.*;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.groupware.EnumComponent;

/**
 * {@link InfostoreExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum InfostoreExceptionCodes implements OXExceptionCode {
    TOO_LONG_VALUES(TOO_LONG_VALUES_MSG, CATEGORY_TRUNCATED, 100),
    /** Invalid SQL Query: %s */
    SQL_PROBLEM(SQL_PROBLEM_MSG, CATEGORY_ERROR, 200),
    /** Cannot pre-fetch results. */
    PREFETCH_FAILED(PREFETCH_FAILED_MSG, CATEGORY_TRY_AGAIN, 219),
    /** The requested item does not exist. */
    NOT_EXIST(NOT_EXIST_MSG, CATEGORY_USER_INPUT, 300),
    /** Could not load documents to check the permissions */
    COULD_NOT_LOAD(COULD_NOT_LOAD_MSG, CATEGORY_USER_INPUT, 301),
    /** The folder %d is not an Infostore folder */
    NOT_INFOSTORE_FOLDER(NOT_INFOSTORE_FOLDER_MSG, CATEGORY_ERROR, 302),
    /** You do not have sufficient read permissions to read objects in this folder. */
    NO_READ_PERMISSION(NO_READ_PERMISSION_MSG, CATEGORY_PERMISSION_DENIED, 400),
    /** You do not have sufficient permissions to create objects in this folder. */
    NO_CREATE_PERMISSION(NO_CREATE_PERMISSION_MSG, CATEGORY_PERMISSION_DENIED, 402),
    /** You are not allowed to update this item. */
    NO_WRITE_PERMISSION(NO_WRITE_PERMISSION_MSG, CATEGORY_PERMISSION_DENIED, 403),
    /** You are not allowed to create objects in the target folder. */
    NO_TARGET_CREATE_PERMISSION(NO_TARGET_CREATE_PERMISSION_MSG, CATEGORY_PERMISSION_DENIED, 404),
    /** Could not delete all objects. */
    NOT_ALL_DELETED(NOT_ALL_DELETED_MSG, CATEGORY_CONFLICT, 405),
    /** You do not have sufficient permission to delete this version. */
    NO_DELETE_PERMISSION_FOR_VERSION(NO_DELETE_PERMISSION_FOR_VERSION_MSG, CATEGORY_PERMISSION_DENIED, 406),
    /** Could not iterate result. */
    ITERATE_FAILED(ITERATE_FAILED_MSG, CATEGORY_ERROR, 413),
    /** This document is locked. */
    ALREADY_LOCKED(ALREADY_LOCKED_MSG, CATEGORY_CONFLICT, 415),
    /** You cannot unlock this document. */
    LOCKED_BY_ANOTHER(LOCKED_BY_ANOTHER_MSG, CATEGORY_CONFLICT, 416),
    /** You need write permissions to unlock a document. */
    WRITE_PERMS_FOR_UNLOCK_MISSING(WRITE_PERMS_FOR_UNLOCK_MISSING_MSG, CATEGORY_PERMISSION_DENIED, 417),
    /** You need write permissions to lock a document. */
    WRITE_PERMS_FOR_LOCK_MISSING(WRITE_PERMS_FOR_LOCK_MISSING_MSG, CATEGORY_PERMISSION_DENIED, 418),
    /** Could not generate new ID. */
    NEW_ID_FAILED(NEW_ID_FAILED_MSG, CATEGORY_ERROR, 420),
    /** You are not allowed to delete objects in the source folder, so this document cannot be moved. */
    NO_SOURCE_DELETE_PERMISSION(NO_SOURCE_DELETE_PERMISSION_MSG, CATEGORY_PERMISSION_DENIED, 421),
    /** The document you requested does not exist. */
    DOCUMENT_NOT_EXIST(DOCUMENT_NOT_EXISTS_MSG, CATEGORY_USER_INPUT, 438),
    /** Files attached to InfoStore items must have unique names. Filename: %s. The other document with this file name is %s. */
    FILENAME_NOT_UNIQUE(FILENAME_NOT_UNIQUE_MSG, CATEGORY_USER_INPUT, 441),
    /** Could not determine number of versions for infoitem %s in context %s. Invalid Query: %s */
    NUMBER_OF_VERSIONS_FAILED(NUMBER_OF_VERSIONS_FAILED_MSG, CATEGORY_ERROR, 442),
    /** You do not have the permissions to delete at least one of the info items. */
    NO_DELETE_PERMISSION(NO_DELETE_PERMISSION_MSG, CATEGORY_PERMISSION_DENIED, 445),
    /** Illegal argument: Document %d contains no file */
    DOCUMENT_CONTAINS_NO_FILE(DOCUMENT_CONTAINS_NO_FILE_MSG, CATEGORY_ERROR, 500),
    /** Folder %d has two subfolders named %s. The database for context %d is not consistent. */
    DUPLICATE_SUBFOLDER(DUPLICATE_SUBFOLDER_MSG, CATEGORY_ERROR, 501),
    /** In order to accomplish the search, %1$d or more characters are required. */
    PATTERN_NEEDS_MORE_CHARACTERS(PATTERN_NEEDS_MORE_CHARACTERS_MSG, CATEGORY_USER_INPUT, 602),
    /** Could not delete DocumentMetadata %d. Please try again. */
    DELETE_FAILED(DELETE_FAILED_MSG, CATEGORY_CONFLICT, 700),
    /** The document could not be updated because it was modified. Reload the view. */
    MODIFIED_CONCURRENTLY(MODIFIED_CONCURRENTLY_MSG, CATEGORY_CONFLICT, 1302),
    /** The document was updated in between do and undo. The Database is now probably inconsistent. */
    UPDATED_BETWEEN_DO_AND_UNDO(UPDATED_BETWEEN_DO_AND_UNDO_MSG, CATEGORY_CONFLICT, 1303),
    /** This folder is a virtual folder. It cannot contain documents. */
    NO_DOCUMENTS_IN_VIRTUAL_FOLDER(NO_DOCUMENTS_IN_VIRTUAL_FOLDER_MSG, CATEGORY_USER_INPUT, 1700),
    /** Validation failed: %1$s */
    VALIDATION_FAILED(VALIDATION_FAILED_MSG, CATEGORY_USER_INPUT, 2100),
    /** File name must not contain slashes. */
    VALIDATION_FAILED_SLASH(InfostoreExceptionMessages.VALIDATION_FAILED_SLASH_MSG, CATEGORY_USER_INPUT, 2101),
    /** File name contains illegal characters. */
    VALIDATION_FAILED_CHARACTERS(InfostoreExceptionMessages.VALIDATION_FAILED_CHARACTERS_MSG, CATEGORY_USER_INPUT, 2101),
    ;

    private final String message;

    private final Category category;

    private final int number;

    private InfostoreExceptionCodes(final String message, final Category category, final int number) {
        this.message = message;
        this.category = category;
        this.number = number;
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

    public String getHelp() {
        return null;
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
