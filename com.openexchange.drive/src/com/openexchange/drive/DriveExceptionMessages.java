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

package com.openexchange.drive;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link DriveExceptionMessages}
 *
 * Translatable messages for {@link DriveExceptionCodes}.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class DriveExceptionMessages implements LocalizableStrings {

    public static final String PATH_NOT_FOUND_MSG = "The path \"%1$s\" was not found. Please retry the synchronization.";
    public static final String FILE_NOT_FOUND_MSG = "The file \"%1$s\" was not found at \"%2$s\". Please retry the synchronization.";
    public static final String INVALID_FILE_OFFSET_MSG = "Unable to access the file at the requested position. Please retry the synchronization.";
    public static final String FILEVERSION_NOT_FOUND_MSG = "The file version \"%1$s\" was not found at \"%3$s\". Please retry the synchronization.";
    public static final String UPLOADED_FILE_CHECKSUM_ERROR_MSG = "Integrity checks failed for uploaded file \"%2$s\". Please try again.";
    public static final String DIRECTORYVERSION_NOT_FOUND_MSG = "The directory version \"%1$s\" was not found. Please retry the synchronization.";
    public static final String NO_DELETE_FILE_PERMISSION_MSG = "You are not allowed to delete the file \"%1$s\" at \"%2$s\".";
    public static final String NO_CREATE_FILE_PERMISSION_MSG = "You are not allowed to create files at \"%1$s\".";
    public static final String NO_MODIFY_FILE_PERMISSION_MSG = "You are not allowed to modify the file \"%1$s\" at \"%2$s\".";
    public static final String NO_DELETE_DIRECTORY_PERMISSION_MSG = "You are not allowed to delete the directory \"%1$s\".";
    public static final String NO_CREATE_DIRECTORY_PERMISSION_MSG = "You are not allowed to create directories at \"%1$s\".";
    public static final String QUOTA_REACHED_MSG = "The allowed quota is reached.";
    public static final String INVALID_PATH_MSG = "The path \"%1$s\" is invalid.";
    public static final String INVALID_FILENAME_MSG = "The file name \"%1$s\" is invalid.";
    public static final String IGNORED_FILENAME_MSG = "The file name \"%1$s\" is ignored.";
    public static final String CONFLICTING_PATH_MSG = "The path \"%1$s\" is conflicting.";
    public static final String INVALID_FILEVERSION_MSG = "The file version \"%1$s\" is invalid.";
    public static final String INVALID_DIRECTORYVERSION_MSG = "The directory version \"%1$s\" is invalid.";
    public static final String LONG_POLLING_NOT_AVAILABLE_MSG = "Push updates are not available.";
    public static final String SERVER_BUSY_MSG = "The server is too busy at the moment - please try again later.";
    public static final String IGNORED_PATH_MSG = "The path \"%1$s\" is ignored.";
    public static final String CONFLICTING_FILENAME_MSG = "The file name \"%1$s\" is conflicting.";
    public static final String REPEATED_SYNC_PROBLEMS_MSG = "The directory \"%1$s\" caused repeated synchronization problems.";
    public static final String CLIENT_OUTDATED_MSG = "The client application you're using is outdated and no longer supported - please upgrade to a newer version.";
    public static final String CLIENT_VERSION_UPDATE_AVAILABLE_MSG = "A newer version of your client application is available for download.";
    public static final String INVALID_PATTERN_MSG = "The pattern \"%1$s\" is invalid. Please use a valid pattern and try again.";
    public static final String LEVEL_CONFLICTING_FILENAME_MSG = "The file name \"%1$s\" is conflicting with an equally named directory at \"%2$s\".";
    public static final String LEVEL_CONFLICTING_PATH_MSG = "The path \"%1$s\" is conflicting with an equally named file at \"%2$s\".";
    public static final String NOT_SYNCHRONIZABLE_DIRECTORY_MSG = "The directory \"%1$s\" is not synchronizable. Please select another directory.";
    public static final String METDATA_PARSE_ERROR_MSG = "The supplied metadata could not be parsed: \"%1$s\"";
    public static final String TOO_MANY_DIRECTORIES_MSG = "The maximum number of synchronizable directories (%1$d) is exceeded. Please remove some directories and try again, or select another root folder.";
    public static final String TOO_MANY_FILES_MSG = "The maximum number of files (%1$d) is exceeded in directory \"%2$s\". Please remove some files and try again.";
    public static final String TOKEN_ALREADY_REGISTERED_MSG = "The token \"%1$s\" is already registered.";
    public static final String ZERO_BYTE_FILES_MSG = "Empty files indicated for directory \"%1$s\".";
    public static final String FILE_ALREADY_EXISTS_MSG = "A file named \"%1$s\" already exists at \"%2$s\".";
    public static final String DIRECTORY_ALREADY_EXISTS_MSG = "A directory named \"%1$s\" already exists at \"%2$s\".";
    public static final String LONG_POLLING_DISABLED_MSG = "Push via long polling is disabled for user \"%1$s\" in context \"%1$s\"";
    public static final String NOT_ACCESSIBLE_DIRECTORY_MSG = "The directory is no longer accessible. Please select another directory.";

    /**
     * Prevent instantiation.
     */
    private DriveExceptionMessages() {
        super();
    }
}
