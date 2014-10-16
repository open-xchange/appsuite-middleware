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

package com.openexchange.file.storage.googledrive;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link GoogleDriveExceptionMessages} - Exception messages for errors that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GoogleDriveExceptionMessages implements LocalizableStrings {

    // A Google Drive error occurred: %1$s
    public static final String GOOGLE_DRIVE_ERROR_MSG = "A Google Drive error occurred: %1$s";

    // A Google Drive error occurred: %1$s
    public static final String HTTP_ERROR_MSG = "A Google Drive server error occurred with HTTP status code %1$s. Error message: %2$s";

    // Google Drive URL does not denote a directory: %1$s
    public static final String NOT_A_FOLDER_MSG = "The provided Google Drive URL does not denote a directory: %1$s";

    // The Google Drive resource does not exist: %1$s
    public static final String NOT_FOUND_MSG = "The provided Google Drive resource does not exist: %1$s";

    // Update denied for Google Drive resource: %1$s
    public static final String UPDATE_DENIED_MSG = "Update denied for the provided Google Drive resource: %1$s";

    // Delete denied for Google Drive resource: %1$s
    public static final String DELETE_DENIED_MSG = "Delete denied for the provided Google Drive resource: %1$s";

    // Google Drive URL does not denote a file: %1$s
    public static final String NOT_A_FILE_MSG = "The provided Google Drive URL does not denote a file: %1$s";

    // Missing file name.
    public static final String MISSING_FILE_NAME_MSG = "Missing file name. Please provide one and try again.";

    // Versioning not supported by Google Drive file storage.
    public static final String VERSIONING_NOT_SUPPORTED_MSG = "Versioning not supported by Google Drive file storage.";

    // Missing configuration for account "%1$s".
    public static final String MISSING_CONFIG_MSG = "Missing configuration for account \"%1$s\".";

    // Bad or expired access token. Need to re-authenticate user.
    public static final String UNLINKED_ERROR_MSG = "Bad or expired access token. Need to re-authenticate user.";

    // An I/O error occurred: %1$s
    public static final String IO_ERROR_MSG = "An I/O error occurred: %1$s";

    // The file %1$s doesn't have any content stored on Drive
    public static final String NO_CONTENT_MSG = "The file %1$s doesn't have any content stored on Drive";

    /**
     * Initializes a new {@link GoogleDriveExceptionMessages}.
     */
    private GoogleDriveExceptionMessages() {
        super();
    }

}
