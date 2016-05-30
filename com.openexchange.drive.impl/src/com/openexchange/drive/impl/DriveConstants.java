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

package com.openexchange.drive.impl;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.TimeZones;


/**
 * {@link DriveConstants}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveConstants {

    /**
     * The root path
     */
    public static final String ROOT_PATH = "/";

    /**
     * The path separator character
     */
    public static final char PATH_SEPARATOR = '/';

    /**
     * The name of the drive temp folder
     */
    public static final String TEMP_FOLDER_NAME = ".drive";

    /**
     * The file extension for partly uploaded files
     */
    public static final String FILEPART_EXTENSION = ".drivepart";

    /**
     * The filename used for metadata files
     */
    public static final String METADATA_FILENAME = ".drive-meta";

    /**
     * The used fields when retrieving file metadata
     */
    public static final List<Field> FILE_FIELDS = Arrays.asList(new Field[] {
        Field.ID, Field.FOLDER_ID, Field.LAST_MODIFIED, Field.TITLE, Field.FILENAME, Field.FILE_MD5SUM, Field.FILE_SIZE, Field.VERSION,
        Field.SEQUENCE_NUMBER, Field.FILE_MIMETYPE, Field.CREATED
    });

    /**
     * The MD5 checksum for an empty input
     */
    public static final String EMPTY_MD5 = "d41d8cd98f00b204e9800998ecf8427e";

    /**
     * The number of maximum retries in case of recoverable exceptions during execution of server actions
     */
    public static final int MAX_RETRIES = 5;

    /**
     * The base number of milliseconds to wait until retrying
     */
    public static final int RETRY_BASEDELAY = 1000;

    /**
     * The maximum allowed length of any synchronized path segment, i.e. the parts in a path separated by the {@link #PATH_SEPARATOR}
     * character.
     */
    public static final int MAX_PATH_SEGMENT_LENGTH = 255;

    /**
     * The minimum expected API version the client has to support that is used if no other one is specified via configuration.
     */
    public static final int DEFAULT_MIN_API_VERSION = 1;

    /**
     * The API version the backend supports currently.
     * 1: initial api version
     * 2: client defined exclusion filters
     * 3: .drive-meta
     * 4: subfolders action, sharing
     * 5: push for multiple root folders (listen/subscribe)
     */
    public static final int SUPPORTED_API_VERSION = 5;

    /**
     * The files module identifier.
     */
    public static final int FILES_MODULE = FolderObject.INFOSTORE;

    /**
     * Thread local {@link SimpleDateFormat} using "yyyy-MM-dd HH:mm:ss.SSS" as pattern.
     */
    public static final ThreadLocal<SimpleDateFormat> LOG_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue()
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            dateFormat.setTimeZone(TimeZones.UTC);
            return dateFormat;
        }
    };

    /**
     * Pattern to match valid MD5 checksums.
     */
    public static final Pattern CHECKSUM_VALIDATION_PATTERN = Pattern.compile("^[0-9a-f]{32}\\z");

    /**
     * Pattern to match valid path names as used by the drive module, based on
     * http://msdn.microsoft.com/en-us/library/aa365247%28v=vs.85%29.aspx#file_and_directory_names
     */
    public static final Pattern PATH_VALIDATION_PATTERN = Pattern.compile(
        "^(?:/|(?:/[^<>:/?*\"\\\\|\\x00-\\x1F]*[^<>:/?*\"\\\\|\\x00-\\x1F\\ .])+)$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

//    /**
//     * Pattern to match filenames that are excluded from synchronization, i.e. system files like "Thumbs.db" or ".DS_Store".
//     */
//    public static final Pattern EXCLUDED_FILENAMES_PATTERN;
//
//    static {
//        ConfigurationService configService = DriveServiceLookup.getService(ConfigurationService.class);
//        String excludedFilesPattern = "thumbs\\.db|desktop\\.ini|\\.ds_store|icon\\\r";
//        if (null != configService) {
//            excludedFilesPattern = configService.getProperty("com.openexchange.drive.excludedFilesPattern", excludedFilesPattern);
//        }
//        EXCLUDED_FILENAMES_PATTERN = Pattern.compile(excludedFilesPattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
//    }
}
