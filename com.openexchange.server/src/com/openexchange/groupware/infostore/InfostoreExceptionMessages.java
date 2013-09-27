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

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link InfostoreExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class InfostoreExceptionMessages implements LocalizableStrings {

    public static final String TOO_LONG_VALUES_MSG = "Some field values are too long.";

    public static final String SQL_PROBLEM_MSG = "Invalid SQL Query: %1$s";

    public static final String PREFETCH_FAILED_MSG = "Cannot pre-fetch results.";

    public static final String NOT_EXIST_MSG = "The requested item does not exist.";

    public static final String COULD_NOT_LOAD_MSG = "Could not load documents to check the permissions";

    public static final String NOT_INFOSTORE_FOLDER_MSG = "The folder %1$s is not an Infostore folder";

    public static final String NO_READ_PERMISSION_MSG = "You do not have sufficient read permissions to read objects in this folder..";

    public static final String NO_CREATE_PERMISSION_MSG = "You do not have sufficient permissions to create objects in this folder.";

    public static final String NO_WRITE_PERMISSION_MSG = "You are not allowed to update this item.";

    public static final String NO_TARGET_CREATE_PERMISSION_MSG = "You are not allowed to create objects in the target folder.";

    public static final String NOT_ALL_DELETED_MSG = "Could not delete all objects.";

    public static final String NO_DELETE_PERMISSION_FOR_VERSION_MSG = "You do not have sufficient permissions to delete this version.";

    public static final String ITERATE_FAILED_MSG = "Could not iterate result.";

    public static final String ALREADY_LOCKED_MSG = "This document is locked.";

    public static final String LOCKED_BY_ANOTHER_MSG = "You cannot unlock this document.";

    public static final String WRITE_PERMS_FOR_LOCK_MISSING_MSG = "You need write permissions to lock a document.";

    public static final String NEW_ID_FAILED_MSG = "Could not generate new ID.";

    public static final String NO_SOURCE_DELETE_PERMISSION_MSG = "You are not allowed to delete objects in the source folder. This document cannot be moved.";

    public static final String WRITE_PERMS_FOR_UNLOCK_MISSING_MSG = "You need write permissions to unlock a document.";

    public static final String DOCUMENT_NOT_EXISTS_MSG = "The document you requested does not exist.";

    public static final String FILENAME_NOT_UNIQUE_MSG = "Files attached to InfoStore items must have unique names. File name: %1$s. The other document with this file name is %2$s.";

    public static final String NUMBER_OF_VERSIONS_FAILED_MSG = "Could not determine number of versions for info item %1$s in context %s. Invalid query: %2$s";

    public static final String NO_DELETE_PERMISSION_MSG = "You do not have the permissions to delete at least one of the info items.";

    public static final String DOCUMENT_CONTAINS_NO_FILE_MSG = "Illegal argument: document %1$s contains no file";

    public static final String DUPLICATE_SUBFOLDER_MSG = "Folder %1$s has two subfolders named %2$s. The database for context %3$s is not consistent.";

    public static final String PATTERN_NEEDS_MORE_CHARACTERS_MSG = "In order to accomplish the search, %1$s or more characters are required.";

    public static final String DELETE_FAILED_MSG = "DocumentMetadata %1$s could not be deleted. Please try again.";

    public static final String MODIFIED_CONCURRENTLY_MSG = "The document could not be updated because it was modified. Reload the view.";

    public static final String UPDATED_BETWEEN_DO_AND_UNDO_MSG = "The document was updated in between do and undo. The database is now probably inconsistent.";

    public static final String NO_DOCUMENTS_IN_VIRTUAL_FOLDER_MSG = "This folder is a virtual folder. It cannot contain documents.";

    public static final String VALIDATION_FAILED_MSG = "Validation failed: %1$s";

    // File name must not contain slashes.
    public static final String VALIDATION_FAILED_SLASH_MSG = "File name must not contain slashes.";

    // File name contains illegal characters.
    public static final String VALIDATION_FAILED_CHARACTERS_MSG = "File name contains illegal characters.";

    public static final String NO_OFFSET_FOR_NEW_VERSIONS_MSG = "New file versions can't be saved with an offset.";

    private InfostoreExceptionMessages() {
        super();
    }
}
