/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.drive.impl;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
        Field.SEQUENCE_NUMBER, Field.FILE_MIMETYPE, Field.CREATED, Field.CURRENT_VERSION, Field.META, Field.CREATED_BY
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
     * A random number generator
     */
    public static final Random RANDOM = new Random();

    /**
     * The maximum allowed length of any synchronized path segment, i.e. the parts in a path separated by the {@link #PATH_SEPARATOR}
     * character.
     */
    public static final int MAX_PATH_SEGMENT_LENGTH = 255;

    /**
     * The API version the backend supports currently.
     * 1: initial api version
     * 2: client defined exclusion filters
     * 3: .drive-meta
     * 4: subfolders action, sharing
     * 5: push for multiple root folders (listen/subscribe), inline .drive-meta, trash stats, empty trash
     * 6: moveFile/moveFolder
     * 7: update directory w/ cascadePermissions
     * 8: syncfolder action, path to root, separate push notifications
     */
    public static final int SUPPORTED_API_VERSION = 8;

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

}
