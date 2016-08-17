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

package com.openexchange.folderstorage;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link FolderExceptionMessages} - Locale-sensitive strings for folder exceptions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderExceptionMessages implements LocalizableStrings {

    // You do not have a valid session. Please login again.
    public static final String MISSING_SESSION_MSG_DISPLAY = "You do not have a valid session. Please login again.";

    // You do not have appropriate permissions to view the folder.
    public static final String FOLDER_NOT_VISIBLE_MSG_DISPLAY = "You do not have appropriate permissions to view the folder.";

    // "The content type you provided is not allowed for the given folder.";
    public static final String INVALID_CONTENT_TYPE_MSG_DISPLAY = "The content type you provided is not allowed for the given folder.";

    // You do not have appropriate permissions to move the folder.
    public static final String MOVE_NOT_PERMITTED_MSG_DISPLAY = "You do not have appropriate permissions to move the folder.";

    // A folder named "%1$s" already exists.
    public static final String EQUAL_NAME_MSG_DISPLAY = "A folder named \"%1$s\" already exists.";

    // The folder you requested does not exist.
    public static final String NOT_FOUND_MSG_DISPLAY = "The folder you requested does not exist.";

    // You do not have the appropriate permissions to delete the folder.
    public static final String FOLDER_NOT_DELETEABLE_MSG_DISPLAY = "You do not have the appropriate permissions to delete the folder.";

    // You do not have the appropriate permissions to move the folder.
    public static final String FOLDER_NOT_MOVEABLE_MSG_DISPLAY = "You do not have the appropriate permissions to move the folder.";

    // You do not have the appropriate permissions to create a subfolder.
    public static final String NO_CREATE_SUBFOLDERS_MSG_DISPLAY = "You do not have the appropriate permissions to create a subfolder.";

    // It is not allowed to create a mail folder allowed below a public folder.
    public static final String NO_PUBLIC_MAIL_FOLDER_MSG_DISPLAY = "It is not allowed to create a mail folder below a public folder.";

    // The folder name "%1$s" is reserved. Please choose another name.
    public static final String RESERVED_NAME_MSG_DISPLAY = "The folder name \"%1$s\" is reserved. Please choose another name.";

    // Found two folders named "%1$s" located below the parent folder. Please rename one of the folders. There should be no two folders with the same name.
    public static final String DUPLICATE_NAME_MSG_DISPLAY = "Found two folders named \"%1$s\" located below the parent folder. Please rename one of the folders. There should be no two folders with the same name.";

    // Failed to delete all folders
    public static final String FOLDER_DELETION_FAILED_MSG_DISPLAY = "Failed to delete all folders";

    // The folder was not updated due to possible data loss. Please review the warnings for details.
    public static final String FOLDER_UPDATE_ABORTED_MSG_DISPLAY = "The folder was not updated due to possible data loss. Please review the warnings for details.";

    // Folder name contains not allowed characters: \"%1$s\"
    public static final String ILLEGAL_CHARACTERS_MSG = "Folder name contains illegal characters: \"%1$s\"";
    /**
     * Initializes a new {@link FolderExceptionMessages}
     */
    private FolderExceptionMessages() {
        super();
    }

}
