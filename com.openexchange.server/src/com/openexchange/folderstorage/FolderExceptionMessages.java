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

package com.openexchange.folderstorage;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link FolderExceptionMessages} - Locale-sensitive strings for folder exceptions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderExceptionMessages implements LocalizableStrings {

    // Unexpected error: %1$s
    public static final String UNEXPECTED_ERROR_MSG = "Unexpected error: %1$s";

    // I/O error: %1$s
    public static final String IO_ERROR_MSG = "I/O error: %1$s";

    // SQL error: %1$s
    public static final String SQL_ERROR_MSG = "SQL error: %1$s";

    // No appropriate folder storage for tree identifier "%1$s" and folder identifier "%2$s".
    public static final String NO_STORAGE_FOR_ID_MSG =
        "No appropriate folder storage for tree identifier \"%1$s\" and folder identifier \"%2$s\".";

    // No appropriate folder storage for tree identifier "%1$s" and content type "%2$s".
    public static final String NO_STORAGE_FOR_CT_MSG =
        "No appropriate folder storage for tree identifier \"%1$s\" and content type \"%2$s\".";

    // Missing session.
    public static final String MISSING_SESSION_MSG = "Missing session.";

    // Folder "%1$s" is not visible to user "%2$s" in context "%3$s"
    public static final String FOLDER_NOT_VISIBLE_MSG = "Folder \"%1$s\" is not visible to user \"%2$s\" in context \"%3$s\"";

    // JSON error: %1$s
    public static final String JSON_ERROR_MSG = "JSON error: %1$s";

    // Missing tree identifier.
    public static final String MISSING_TREE_ID_MSG = "Missing tree identifier.";

    // Missing parent folder identifier.
    public static final String MISSING_PARENT_ID_MSG = "Missing parent folder identifier.";

    // Missing folder identifier.
    public static final String MISSING_FOLDER_ID_MSG = "Missing folder identifier.";

    // Parent folder "%1$s" does not allow folder content type "%2$s" in tree "%3$s" for user %4$s in context %5$s.
    public static final String INVALID_CONTENT_TYPE_MSG =
        "Parent folder \"%1$s\" does not allow folder content type \"%2$s\" in tree \"%3$s\" for user %4$s in context %5$s.";

    // Move operation not permitted.
    public static final String MOVE_NOT_PERMITTED_MSG = "Move operation not permitted.";

    // A folder named "%1$s" already exists below parent folder "%2$s" in tree "%3$s".
    public static final String EQUAL_NAME_MSG = "A folder named \"%1$s\" already exists below parent folder \"%2$s\" in tree \"%3$s\".";

    // Subscribe operation not permitted on tree "%1$s".
    public static final String NO_REAL_SUBSCRIBE_MSG = "Subscribe operation not permitted on tree \"%1$s\".";

    // Unsubscribe operation not permitted on tree "%1$s".
    public static final String NO_REAL_UNSUBSCRIBE_MSG = "Unsubscribe operation not permitted on tree \"%1$s\".";

    // Unsubscribe operation not permitted on folder "%1$s" in tree "%2$s". Delete subfolders first.
    public static final String NO_UNSUBSCRIBE_MSG = "Unsubscribe operation not permitted on folder \"%1$s\" in tree \"%2$s\". Unsubscribe subfolders first.";

    // Unknown content type: %1$s.
    public static final String UNKNOWN_CONTENT_TYPE_MSG = "Unknown content type: %1$s.";

    // Missing parameter: %1$s.
    public static final String MISSING_PARAMETER_MSG = "Missing parameter: %1$s.";

    // Missing property: %1$s.
    public static final String MISSING_PROPERTY_MSG = "Missing property: %1$s.";

    // Unsupported storage type: %1$s.
    public static final String UNSUPPORTED_STORAGE_TYPE_MSG = "Unsupported storage type: %1$s.";

    // The object has been changed in the meantime.
    public static final String CONCURRENT_MODIFICATION_MSG = "The object has been changed in the meantime.";

    // Folder "%1$s" could not be found in tree "%2$s".
    public static final String NOT_FOUND_MSG = "Folder \"%1$s\" could not be found in tree \"%2$s\".";

    // No default folder available for content type "%1$s" in tree "%2$s".
    public static final String NO_DEFAULT_FOLDER_MSG = "No default folder available for content type \"%1$s\" in tree \"%2$s\".";

    // Invalid folder identifier: %1$s.
    public static final String INVALID_FOLDER_ID_MSG = "Invalid folder identifier: %1$s.";

    // Folder "%1$s" must not be deleted by user "%2$s" in context "%3$s".
    public static final String FOLDER_NOT_DELETEABLE_MSG = "Folder \"%1$s\" must not be deleted by user \"%2$s\" in context \"%3$s\".";

    // Folder "%1$s" must not be moved by user "%2$s" in context "%3$s".
    public static final String FOLDER_NOT_MOVEABLE_MSG = "Folder \"%1$s\" must not be moved by user \"%2$s\" in context \"%3$s\".";

    // A temporary error occurred. Please retry.
    public static final String TEMPORARY_ERROR_MSG = "A temporary error occurred. Please retry.";

    // User "%2$s" must not create subfolders below folder "%2$s" in context "%3$s".
    public static final String NO_CREATE_SUBFOLDERS_MSG = "User \"%2$s\" must not create subfolders below folder \"%2$s\" in context \"%3$s\".";

    // No mail folder allowed below a public folder.
    public static final String NO_PUBLIC_MAIL_FOLDER_MSG = "No mail folder allowed below a public folder.";

    // No such tree with identifier "%1$s".
    public static final String TREE_NOT_FOUND_MSG = "No such tree with identifier \"%1$s\".";

    // A tree with identifier "%1$s" already exists.
    public static final String DUPLICATE_TREE_MSG = "A tree with identifier \"%1$s\" already exists.";

    // The folder name "%1$s" is reserved. Please choose another name.
    public static final String RESERVED_NAME_MSG = "The folder name \"%1$s\" is reserved. Please choose another name.";

    // Found two folders named "%1$s" located below the parent folder "%2$s". Please rename one of the folders. There should be no two folders with the same name.
    public static final String DUPLICATE_NAME_MSG = "Found two folders named \"%1$s\" located below the parent folder \"%2$s\". Please rename one of the folders. There should be no two folders with the same name.";

    // An unexpected error occurred: %1$s. Please try again.
    public static final String TRY_AGAIN_MSG = "An unexpected error occurred: %1$s. Please try again.";

    /**
     * Initializes a new {@link FolderExceptionMessages}
     */
    private FolderExceptionMessages() {
        super();
    }

}
