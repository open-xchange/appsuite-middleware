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

package com.openexchange.file.storage.googledrive;

import com.openexchange.file.storage.FileStorageConstants;

/**
 * {@link GoogleDriveConstants} - Provides useful constants for Google Drive file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GoogleDriveConstants implements FileStorageConstants {

    /**
     * Initializes a new {@link GoogleDriveConstants}.
     */
    private GoogleDriveConstants() {
        super();
    }

    /**
     * The identifier for Google Drive file storage service.
     */
    public static final String ID = "googledrive";

    /**
     * The special MIME type marking a file as a directory.
     * <p>
     * A folder is a file with the MIME type <code>application/vnd.google-apps.folder</code> and with no extension.
     */
    public static final String MIME_TYPE_DIRECTORY = "application/vnd.google-apps.folder";

    // ------------------------------------------------------------------------------------------------------------------------------ //

    /**
     * The query string for selecting only files: <code>"mimeType != 'application/vnd.google-apps.folder'"</code>
     */
    public static final String QUERY_STRING_FILES_ONLY = "mimeType != '" + MIME_TYPE_DIRECTORY + "'";

    /**
     * The query string for selecting only files: <code>"mimeType != 'application/vnd.google-apps.folder' and trashed=false"</code>
     */
    public static final String QUERY_STRING_FILES_ONLY_EXCLUDING_TRASH = "mimeType != '" + MIME_TYPE_DIRECTORY + "' and trashed=false";

    /**
     * The query string for selecting only directories: <code>"mimeType = 'application/vnd.google-apps.folder'"</code>
     */
    public static final String QUERY_STRING_DIRECTORIES_ONLY = "mimeType = '" + MIME_TYPE_DIRECTORY + "'";

    /**
     * The query string for selecting only directories: <code>"mimeType = 'application/vnd.google-apps.folder' and trashed=false"</code>
     */
    public static final String QUERY_STRING_DIRECTORIES_ONLY_EXCLUDING_TRASH = "mimeType = '" + MIME_TYPE_DIRECTORY + "' and trashed=false";

    // ------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * The default fields to query.
     */
    public static final String FIELDS_DEFAULT = "id,parents/id,modifiedDate,mimeType,explicitlyTrashed,labels/trashed";

}
