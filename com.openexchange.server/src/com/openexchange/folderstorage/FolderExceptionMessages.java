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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

    // You do not have appropriate permissions to view the folder \"%1$s\".
    public static final String FOLDER_NOT_VISIBLE_MSG_DISPLAY = "You do not have appropriate permissions to view the folder \"%1$s\".";

    public static final String FOLDER_NOT_VISIBLE_MSG = "Folder \"%1$s\" is not visible to user \"%2$s\" in context \"%3$s\"";


    // "The content type you provided is not allowed for the given folder \"%1$s\".";
    public static final String INVALID_CONTENT_TYPE_MSG_DISPLAY = "The content type you provided is not allowed for the given folder \"%1$s\".";

    // Move of the folder \"%1$s\" is not supported.
    public static final String MOVE_NOT_PERMITTED_MSG_DISPLAY = "Move of the folder \"%1$s\" is not permitted.";

    // A folder named "%1$s" already exists below the parent folder "%2$s".
    public static final String EQUAL_NAME_MSG_DISPLAY = "A folder named \"%1$s\" already exists below the parent folder \"%2$s\".";

    // The folder you requested does not exist.
    public static final String NOT_FOUND_MSG_DISPLAY = "The folder you requested does not exist.";

    // You do not have the appropriate permissions to delete the folder \"%1$s\".
    public static final String FOLDER_NOT_DELETEABLE_MSG_DISPLAY = "You do not have the appropriate permissions to delete the folder \"%1$s\".";

    // You do not have the appropriate permissions to move the folder \"%1$s\".
    public static final String FOLDER_NOT_MOVEABLE_MSG_DISPLAY = "You do not have the appropriate permissions to move the folder \"%1$s\".";

    // You do not have the appropriate permissions to create a subfolder below the folder \"%1$s\".
    public static final String NO_CREATE_SUBFOLDERS_MSG_DISPLAY = "You do not have the appropriate permissions to create a subfolder below the folder \"%1$s\".";

    // It is not allowed to create a mail folder allowed below a public folder.
    public static final String NO_PUBLIC_MAIL_FOLDER_MSG_DISPLAY = "It is not allowed to create a mail folder below a public folder.";

    // The folder name "%1$s" is reserved. Please choose another name.
    public static final String RESERVED_NAME_MSG_DISPLAY = "The folder name \"%1$s\" is reserved. Please choose another name.";

    // Found two folders named "%1$s" located below the parent folder "%2$s". Please rename one of the folders. There should be no two
    // folders with the same name.
    public static final String DUPLICATE_NAME_MSG_DISPLAY = "Found two folders named \"%1$s\" located below the parent folder \"%2$s\". Please rename one of the folders. There should be no two folders with the same name.";

    // Failed to delete following folder/s: %1$s
    public static final String FOLDER_DELETION_FAILED_MSG_DISPLAY = "Failed to delete following folder/s: %1$s";

    /**
     * Initializes a new {@link FolderExceptionMessages}
     */
    private FolderExceptionMessages() {
        super();
    }

}
