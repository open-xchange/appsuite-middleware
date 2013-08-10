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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.storage;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import com.openexchange.file.storage.File.Field;
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
     * The path to the drive temp folder
     */
    public static final String TEMP_PATH = "/.drive";

    /**
     * The file extension for partly uploaded files
     */
    public static final String FILEPART_EXTENSION = ".drivepart";

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
     * An array holding characters that are not allowed in (windows-) filenames.
     */
    public static final char[] ILLEGAL_FILENAME_CHARS = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 34, 42,
        47, 58, 60, 62, 63, 92, 124
    };

    /**
     * Pattern to match valid (Windows-) filenames, borrowed from http://stackoverflow.com/questions/6730009/ .
     */
    public static final Pattern FILENAME_VALIDATION_PATTERN = Pattern.compile(
        "# Match a valid Windows filename (unspecified file system).          \n" +
        "^                                # Anchor to start of string.        \n" +
        "(?!                              # Assert filename is not: CON, PRN, \n" +
        "  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n" +
        "    CON|PRN|AUX|CLOCK\\$|NUL|    # COM5, COM6, COM7, COM8, COM9,     \n" +
        "    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n" +
        "  )                              # LPT6, LPT7, LPT8, and LPT9...     \n" +
        "  (?:\\.[^.]*)?                  # followed by optional extension    \n" +
        "  $                              # and end of string                 \n" +
        ")                                # End negative lookahead assertion. \n" +
        "[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n" +
        "[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n" +
        "$                                # Anchor to end of string.            ",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);

}
