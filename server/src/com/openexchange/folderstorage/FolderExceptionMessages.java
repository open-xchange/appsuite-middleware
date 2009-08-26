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

package com.openexchange.folderstorage;

import com.openexchange.exceptions.LocalizableStrings;

/**
 * {@link FolderExceptionMessages} - Locale-sensitive strings for folder exceptions.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderExceptionMessages implements LocalizableStrings {

    /**
     * Unexpected error: %1$s
     */
    public static final String UNEXPECTED_ERROR_MSG = "Unexpected error: %1$s";

    /**
     * I/O error: %1$s
     */
    public static final String IO_ERROR_MSG = "I/O error: %1$s";

    /**
     * SQL error: %1$s
     */
    public static final String SQL_ERROR_MSG = "SQL error: %1$s";

    /**
     * No appropriate folder storage for tree identifier "%1$s" and folder identifier "%2$s".
     */
    public static final String NO_STORAGE_FOR_ID_MSG = "No appropriate folder storage for tree identifier \"%1$s\" and folder identifier \"%2$s\".";

    /**
     * No appropriate folder storage for tree identifier "%1$s" and content type "%2$s".
     */
    public static final String NO_STORAGE_FOR_CT_MSG = "No appropriate folder storage for tree identifier \"%1$s\" and content type \"%2$s\".";

    /**
     * Missing session.
     */
    public static final String MISSING_SESSION_MSG = "Missing session.";

    /**
     * Folder "%1$s" is not visible to user "%2$s" in context "%3$s"
     */
    public static final String FOLDER_NOT_VISIBLE_MSG = "Folder \"%1$s\" is not visible to user \"%2$s\" in context \"%3$s\"";

    /**
     * JSON error: %1$s
     */
    public static final String JSON_ERROR_MSG = "JSON error: %1$s";

    /**
     * Missing tree identifier.
     */
    public static final String MISSING_TREE_ID_MSG = "Missing tree identifier.";

    /**
     * Missing parent folder identifier.
     */
    public static final String MISSING_PARENT_ID_MSG = "Missing parent folder identifier.";

    /**
     * Missing folder identifier.
     */
    public static final String MISSING_FOLDER_ID_MSG = "Missing folder identifier.";

    /**
     * Parent folder "%1$s" does not allow folder content type "%2$s" in tree "%3$s" for user %4$s in context %5$s.
     */
    public static final String INVALID_CONTENT_TYPE_MSG = "Parent folder \"%1$s\" does not allow folder content type \"%2$s\" in tree \"%3$s\" for user %4$s in context %5$s.";

    /**
     * Move operation not permitted.
     */
    public static final String MOVE_NOT_PERMITTED_MSG = "Move operation not permitted.";

    /**
     * A folder named "%1$s" already exists below parent folder "%2$s" in tree "%3$s".
     */
    public static final String EQUAL_NAME_MSG = "A folder named \"%1$s\" already exists below parent folder \"%2$s\" in tree \"%3$s\".";

    /**
     * Subscribe operation not permitted on tree "%1$s".
     */
    public static final String NO_REAL_SUBSCRIBE_MSG = "Subscribe operation not permitted on tree \"%1$s\".";

    /**
     * Un-Subscribe operation not permitted on tree "%1$s".
     */
    public static final String NO_REAL_UNSUBSCRIBE_MSG = "Un-Subscribe operation not permitted on tree \"%1$s\".";

    /**
     * Un-Subscribe operation not permitted on folder "%1$s" in tree "%2$s".
     */
    public static final String NO_UNSUBSCRIBE_MSG = "Un-Subscribe operation not permitted on folder \"%1$s\" in tree \"%2$s\".";

    /**
     * Unknown content type: %1$s.
     */
    public static final String UNKNOWN_CONTENT_TYPE_MSG = "Unknown content type: %1$s.";

    /**
     * Missing parameter: %1$s.
     */
    public static final String MISSING_PARAMETER_MSG = "Missing parameter: %1$s.";

    /**
     * Missing property: %1$s.
     */
    public static final String MISSING_PROPERTY_MSG = "Missing property: %1$s.";

    /**
     * Unsupported storage type: %1$s.
     */
    public static final String UNSUPPORTED_STORAGE_TYPE_MSG = "Unsupported storage type: %1$s.";

    /**
     * Initializes a new {@link FolderExceptionMessages}
     */
    private FolderExceptionMessages() {
        super();
    }

}
