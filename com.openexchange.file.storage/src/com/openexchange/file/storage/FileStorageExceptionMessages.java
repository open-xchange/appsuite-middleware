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

package com.openexchange.file.storage;

import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link OXExceptionMessages} - Exception messages for {@link OXException} that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class FileStorageExceptionMessages implements LocalizableStrings {

    // The folder you requested does not exist.
    public static final String FOLDER_NOT_EXISTS_MSG = "The folder you requested does not exist.";

    // The file you requested does not exist.
    public static final String FILE_NOT_EXISTS_MSG = "The file you requested does not exist.";

    // A folder named "%1$s" already exists below the parent folder "%2$s".
    public static final String DUPLICATE_FOLDER_MSG = "A folder named \"%1$s\" already exists below the parent folder \"%2$s\".";

    // You do not have the appropriate permissions to create a subfolder below the folder "%1$s".
    public static final String NO_CREATE_ACCESS_MSG = "You do not have the appropriate permissions to create a subfolder below the folder \"%1$s\".";

    // In order to accomplish the search, %1$d or more characters are required.
    public static final String PATTERN_NEEDS_MORE_CHARACTERS_MSG = "In order to accomplish the search, %1$d or more characters are required.";

    // Invalid URL \"%1$s\". Please correct the value and try again.
    public static final String INVALID_URL_MSG = "Invalid URL \"%1$s\". Please correct the value and try again.";

    // The allowed quota is reached. Please delete some items in order to store new ones.
    public static final String QUOTA_REACHED_MSG = "The allowed quota is reached. Please delete some items in order to store new ones.";

    // ZIP archive exceeds max. allowed size of %1$s
    public static final String ARCHIVE_MAX_SIZE_EXCEEDED_MSG = "ZIP archive exceeds max. allowed size of %1$s";

    // The file storage account is currently not accessible.
    public static final String ACCOUNT_NOT_ACCESSIBLE_MSG = "The file storage account is currently not accessible.";

    /**
     * Initializes a new {@link OXExceptionMessages}.
     */
    private FileStorageExceptionMessages() {
        super();
    }

}
