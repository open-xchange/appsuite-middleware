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

import java.util.EnumSet;
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

    // ------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Status code (400) indicating the request sent by the client was syntactically incorrect.
     */
    public static final int SC_BAD_REQUEST = 400;

    /**
     * Status code (401) indicating that the request requires HTTP authentication.
     */
    public static final int SC_UNAUTHORIZED = 401;

    /**
     * Status code (403) indicating the server understood the request but refused to fulfill it.
     */
    public static final int SC_FORBIDDEN = 403;

    /**
     * Status code (404) indicating that the requested resource is not available.
     */
    public static final int SC_NOT_FOUND = 404;

    /**
     * Status code (409) indicating that the request could not be completed due to a conflict with the current state of the resource.
     */
    public static final int SC_CONFLICT = 409;

    // ------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * The identifier for Google Drive file storage service.
     */
    public static final String ID = "googledrive";

    /**
     * The display name for Google Drive file storage service.
     */
    public static final String DISPLAY_NAME = "Google Drive File Storage Service";

    /**
     * The special MIME type marking a file as a directory.
     * <p>
     * A folder is a file with the MIME type <code>application/vnd.google-apps.folder</code> and with no extension.
     */
    public static final String MIME_TYPE_DIRECTORY = "application/vnd.google-apps.folder";

    /**
     * The root folder identifier
     */
    public static final String ROOT_ID = "root";

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
    public static final String FIELDS_DEFAULT = getFields(GoogleFileFields.defaults());

    /**
     * The minimal set of fields to query a file with.
     */
    public static final String FIELDS_MINIMAL = getFields(GoogleFileFields.ID, GoogleFileFields.NAME, GoogleFileFields.MIME_TYPE, GoogleFileFields.EXPLICITLY_TRASHED);

    // ------------------------------------------------------------------------------------------------------------------------------- //

    public enum GoogleFileFields {
        ID("id"),
        NAME("name"),
        PARENTS("parents"),
        MODIFIED("modifiedTime"),
        MIME_TYPE("mimeType"),
        TRASHED("trashed"),
        EXPLICITLY_TRASHED("explicitlyTrashed"),
        THUMBNAIL("thumbnailLink"),
        ;

        private final String fieldName;

        /**
         * Initializes a new {@link GoogleDriveConstants.GoogleFileFields}.
         * 
         */
        private GoogleFileFields(String fieldName) {
            this.fieldName = fieldName;
        }

        /**
         * Gets the fieldName
         *
         * @return The fieldName
         */
        public String getField() {
            return fieldName;
        }

        // ------------------------------------------------------------------------------------- //

        public static EnumSet<GoogleFileFields> defaults() {
            return EnumSet.of(ID, NAME, PARENTS, MODIFIED, MIME_TYPE, TRASHED, EXPLICITLY_TRASHED);
        }
    }

    public static String getFields(EnumSet<GoogleFileFields> fields) {
        return getFields(fields.toArray(new GoogleFileFields[0]));
    }

    public static String getFields(GoogleFileFields... fields) {
        StringBuilder builder = new StringBuilder();
        for (GoogleFileFields field : fields) {
            builder.append(field.getField()).append(",");
        }
        return builder.substring(0, builder.length() - 1);
    }
}
