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

import com.openexchange.exceptions.LocalizableStrings;

/**
 * {@link InfostoreExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class InfostoreExceptionMessages implements LocalizableStrings {

    public static final String TOO_LONG_VALUES_MSG = "Some fields have values, that are too long";
    public static final String TOO_LONG_VALUES_HELP = "The User entered values that are to long for the database schema.";

    public static final String INVALID_SQL_QUERY_MSG = "Invalid SQL Query: %s";
    public static final String INVALID_SQL_QUERY_HELP = "A faulty SQL Query was sent to the SQL server. This can only be fixed in R&D";

    public static final String PREFETCH_FAILED_MSG = "Cannot pre-fetch results.";
    public static final String PREFETCH_FAILED_HELP = "Thrown when a result cannot be prefetched. This indicates a problem with the DB Connection. Have a look at the underlying SQLException";

    public static final String NOT_EXIST_MSG = "The requested item does not exist.";
    public static final String NOT_EXIST_HELP = "The infoitem does not exist, so the permissions cannot be loaded.";

    public static final String COULD_NOT_LOAD_MSG = "Could not load documents to check the permissions";
    public static final String COULD_NOT_LOAD_HELP = "To check permissions infoitems must be loaded to find their folderId and creator.";

    public static final String NOT_INFOSTORE_FOLDER_MSG = "The folder %d is not an Infostore folder";
    public static final String NOT_INFOSTORE_FOLDER_HELP = "The client tries to put an infoitem into a non infoitem folder.";

    public static final String NO_READ_PERMISSION_MSG = "You do not have sufficient read permissions to read objects in this folder..";
    public static final String NO_READ_PERMISSION_HELP = "The user does not have read permissions on the requested Infoitem.";

    public static final String NO_CREATE_PERMISSION_MSG = "You do not have sufficient permissions to create objects in this folder.";
    public static final String NO_CREATE_PERMISSION_HELP = "The user may not create objects in the given folder.";

    public static final String NO_WRITE_PERMISSION_MSG = "You are not allowed to update this item.";
    public static final String NO_WRITE_PERMISSION_HELP = "The user doesn't have the required write permissions to update the infoitem.";

    public static final String NO_TARGET_CREATE_PERMISSION_MSG = "You are not allowed to create objects in the target folder.";
    public static final String NO_TARGET_CREATE_PERMISSION_HELP = "The user isn't allowed to create objects in the target folder when moving an infoitem.";

    public static final String NOT_ALL_DELETED_MSG = "Could not delete all objects.";
    public static final String NOT_ALL_DELETED_HELP = "Not all infoitems in the given folder could be deleted. This may be due to the infoitems being modified since the last request, or the objects might not even exist anymore or the user doesn't have enough delete permissions on certain objects.";

    public static final String NO_DELETE_PERMISSION_FOR_VERSION_MSG = "You do not have sufficient permission to delete this version.";
    public static final String NO_DELETE_PERMISSION_FOR_VERSION_HELP = "The user must be allowed to delete the object in order to delete a version of it.";

    public static final String ITERATE_FAILED_MSG = "Could not iterate result.";
    public static final String ITERATE_FAILED_HELP = "The system couldn't iterate the result dataset. This can have numerous exciting causes.";

    public static final String ALREADY_LOCKED_MSG = "This document is locked.";
    public static final String ALREADY_LOCKED_HELP = "The infoitem was locked by some other user. Only the user that locked the item (the one that modified the entry) can modify a locked infoitem.";

    public static final String LOCKED_BY_ANOTHER_MSG = "You cannot unlock this document.";
    public static final String LOCKED_BY_ANOTHER_HELP = "The infoitem was locked by some other user. Only the user that locked the item and the creator of the item can unlock a locked infoitem.";

    public static final String WRITE_PERMS_FOR_LOCK_MISSING_MSG = "You need write permissions to lock a document.";
    public static final String WRITE_PERMS_FOR_LOCK_MISSING_HELP = "The user does not have sufficient write permissions to lock this infoitem.";

    public static final String NEW_ID_FAILED_MSG = "Could not generate new ID.";
    public static final String NEW_ID_FAILED_HELP = "The IDGenerator threw an SQL Exception look at that one to find out what's wrong.";

    public static final String NO_SOURCE_DELETE_PERMISSION_MSG = "You are not allowed to delete objects in the source folder, so this document cannot be moved.";
    public static final String NO_SOURCE_DELETE_PERMISSION_HELP = "Need delete permissions in original folder to move an item.";

    public static final String WRITE_PERMS_FOR_UNLOCK_MISSING_MSG = "You need write permissions to unlock a document.";
    public static final String WRITE_PERMS_FOR_UNLOCK_MISSING_HELP = "The user does not have sufficient write permissions to unlock this infoitem.";

    public static final String DOCUMENT_NOT_EXISTS_MSG = "The document you requested doesn't exist.";
    public static final String DOCUMENT_NOT_EXISTS_HELP = "The document could not be loaded because it doesn't exist.";

    public static final String FILENAME_NOT_UNIQUE_MSG = "Files attached to InfoStore items must have unique names. Filename: %s. The other document with this file name is %s.";
    public static final String FILENAME_NOT_UNIQUE_HELP = "To remain consistent in WebDAV no two current versions in a given folder may contain a file with the same filename. The user must either choose a different filename, or switch the other file to a version with a different filename.";

    public static final String NUMBER_OF_VERSIONS_FAILED_MSG = "Could not determine number of versions for infoitem %s in context %s. Invalid Query: %s";
    public static final String NUMBER_OF_VERSIONS_FAILED_HELP = "The query to cound the versions in a document failed.";

    public static final String NO_DELETE_PERMISSION_MSG = "You do not have the permissions to delete at least one of the info items.";
    public static final String NO_DELETE_PERMISSION_HELP = "You do not have the permissions to delete at least one of the info items.";

    public static final String DOCUMENT_CONTAINS_NO_FILE_MSG = "Illegal argument: Document %d contains no file";
    public static final String DOCUMENT_CONTAINS_NO_FILE_HELP = "A WebdavPath for a document without an attached file was requested. In WebDAV only infoitems with files are visible. This points to a problem with the cola supply for the developer and can only be fixed by R&D.";

    public static final String DUPLICATE_SUBFOLDER_MSG = "Folder %d has two subfolders named %s. The database for context %d is not consistent.";
    public static final String DUPLICATE_SUBFOLDER_HELP = "A folder contains two folders with the same folder name. This points to an inconsistency in the database, as the second folder by the same name should not have been created. This will certainly cause some headaches in R&D.";

    public static final String PATTERN_NEEDS_MORE_CHARACTERS_MSG = "In order to accomplish the search, %1$d or more characters are required.";
    public static final String PATTERN_NEEDS_MORE_CHARACTERS_HELP = "The administrator configured a minimum length for a search pattern and the users pattern is shorter than this minimum.";

    public static final String DELETE_FAILED_MSG = "Could not delete DocumentMetadata %d. Please try again.";
    public static final String DELETE_FAILED_HELP = "The DocumentMetadata entry in the DB for the given resource could not be created. This is mostly due to someone else modifying the entry. This can also mean, that the entry has been deleted already.";

    public static final String MODIFIED_CONCURRENTLY_MSG = "The document could not be updated because it was modified. Reload the view.";
    public static final String MODIFIED_CONCURRENTLY_HELP = "The document could not be updated because it was modified.";

    public static final String UPDATED_BETWEEN_DO_AND_UNDO_MSG = "The document was updated in between do and undo. The Database is now probably inconsistent.";
    public static final String UPDATED_BETWEEN_DO_AND_UNDO_HELP = "The document was updated in between do and undo. The Database is now probalby inconsistent.";

    public static final String NO_DOCUMENTS_IN_VIRTUAL_FOLDER_MSG = "This folder is a virtual folder. It cannot contain documents.";
    public static final String NO_DOCUMENTS_IN_VIRTUAL_FOLDER_HELP = "The folders to which this user has access, but that belong to other users, are collected in a virtual folder. This virtual folder cannot contain documents itself.";

    public static final String VALIDATION_FAILED_MSG = "Validation failed: %s";
    public static final String VALIDATION_FAILED_HELP = "";

    private InfostoreExceptionMessages() {
        super();
    }
}
