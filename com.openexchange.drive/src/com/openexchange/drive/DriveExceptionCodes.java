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

package com.openexchange.drive;

import static com.openexchange.drive.DriveExceptionMessages.*;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link DriveExceptionCodes}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum DriveExceptionCodes implements DisplayableOXExceptionCode {

    /** The directory path \"%1$s\" is invalid */
    INVALID_PATH(INVALID_PATH_MSG, INVALID_PATH_MSG, Category.CATEGORY_USER_INPUT, 1),

    /** The directory path \"%1$s\" was not found */
    PATH_NOT_FOUND(PATH_NOT_FOUND_MSG, PATH_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 2),

    /** The file \"%1$s\" was not found at \"%2$s\" */
    FILE_NOT_FOUND(FILE_NOT_FOUND_MSG, FILE_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 3),

    /** An I/O error occurred: \"%1$s\" */
    IO_ERROR("An I/O error occurred: \"%1$s\"", OXExceptionStrings.MESSAGE_RETRY, Category.CATEGORY_CONNECTIVITY, 4),

    /** The file offset \"%1$d\" is invalid */
    INVALID_FILE_OFFSET("The file offset \"%1$d\" is invalid", INVALID_FILE_OFFSET_MSG, Category.CATEGORY_USER_INPUT, 5),

    /** Unexpected database error: \"%1$s\" */
    DB_ERROR("Unexpected database error: \"%1$s\"", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_WARNING, 6),

    /** The file \"%1$s\" with checksum \"%2$s\" was not found at \"%3$s\" */
    FILEVERSION_NOT_FOUND("The file \"%1$s\" with checksum \"%2$s\" was not found at \"%3$s\"", FILEVERSION_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 7),

    /** No checksum for file \"%1$s\" available */
    NO_CHECKSUM_FOR_FILE("No checksum for file \"%1$s\" available", OXExceptionStrings.MESSAGE, Category.CATEGORY_WARNING, 8),

    /** Checksum \"%1$s\" for uploaded file \"%2$s\" different from \"%3$s\" */
    UPLOADED_FILE_CHECKSUM_ERROR("Checksum \"%1$s\" for uploaded file \"%2$s\" different from \"%3$s\"", UPLOADED_FILE_CHECKSUM_ERROR_MSG, Category.CATEGORY_USER_INPUT, 9),

    /** The directory \"%1$s\" with checksum \"%2$s\" was not found" */
    DIRECTORYVERSION_NOT_FOUND("The directory \"%1$s\" with checksum \"%2$s\" was not found", DIRECTORYVERSION_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 10),

    /** You are not allowed to delete the file \"%1$s\" at \"%2$s\" */
    NO_DELETE_FILE_PERMISSION(NO_DELETE_FILE_PERMISSION_MSG, NO_DELETE_FILE_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 11),

    /** You are not allowed to create files at \"%1$s\" */
    NO_CREATE_FILE_PERMISSION(NO_CREATE_FILE_PERMISSION_MSG, NO_CREATE_FILE_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 12),

    /** You are not allowed to modify the file \"%1$s\" at \"%2$s\" */
    NO_MODIFY_FILE_PERMISSION(NO_MODIFY_FILE_PERMISSION_MSG, NO_MODIFY_FILE_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 13),

    /** You are not allowed to delete the directory \"%1$s\" */
    NO_DELETE_DIRECTORY_PERMISSION(NO_DELETE_DIRECTORY_PERMISSION_MSG, NO_DELETE_DIRECTORY_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 14),

    /** You are not allowed to create directories at \"%1$s\" */
    NO_CREATE_DIRECTORY_PERMISSION(NO_CREATE_DIRECTORY_PERMISSION_MSG, NO_CREATE_DIRECTORY_PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 15),

    /** The allowed Quota is reached. */
    QUOTA_REACHED(QUOTA_REACHED_MSG, QUOTA_REACHED_MSG, Category.CATEGORY_CAPACITY, 16),

    /** The file name \"%1$s\" is invalid. */
    INVALID_FILENAME(INVALID_FILENAME_MSG, INVALID_FILENAME_MSG, Category.CATEGORY_USER_INPUT, 17),

    /** The file name \"%1$s\" is ignored. */
    IGNORED_FILENAME(IGNORED_FILENAME_MSG, IGNORED_FILENAME_MSG, Category.CATEGORY_USER_INPUT, 18),

    /** The directory path \"%1$s\" is conflicting */
    CONFLICTING_PATH(CONFLICTING_PATH_MSG, CONFLICTING_PATH_MSG, Category.CATEGORY_CONFLICT, 19),

    /** The file version \"%1$s\" with checksum \"%2$s\" is invalid */
    INVALID_FILEVERSION("The file version \"%1$s\" with checksum \"%2$s\" is invalid", INVALID_FILEVERSION_MSG, Category.CATEGORY_USER_INPUT, 20),

    /** The directory version \"%1$s\" with checksum \"%2$s\" is invalid */
    INVALID_DIRECTORYVERSION("The directory version \"%1$s\" with checksum \"%2$s\" is invalid", INVALID_DIRECTORYVERSION_MSG, Category.CATEGORY_USER_INPUT, 21),

    /** Push via long polling is not available */
    LONG_POLLING_NOT_AVAILABLE("Push via long polling is not available", LONG_POLLING_NOT_AVAILABLE_MSG, Category.CATEGORY_SERVICE_DOWN, 22),

    /** The server is too busy at the moment - please try again later */
    SERVER_BUSY(SERVER_BUSY_MSG, SERVER_BUSY_MSG, Category.CATEGORY_TRY_AGAIN, 23),

    /** The directory path \"%1$s\" is ignored */
    IGNORED_PATH(IGNORED_PATH_MSG, IGNORED_PATH_MSG, Category.CATEGORY_USER_INPUT, 24),

    /** The file name \"%1$s\" is conflicting */
    CONFLICTING_FILENAME(CONFLICTING_FILENAME_MSG, CONFLICTING_FILENAME_MSG, Category.CATEGORY_CONFLICT, 25),

    /** The directory \"%1$s\" with checksum \"%2$s\" caused repeated synchronization problems */
    REPEATED_SYNC_PROBLEMS("The directory \"%1$s\" with checksum \"%2$s\" caused repeated synchronization problems", REPEATED_SYNC_PROBLEMS_MSG, Category.CATEGORY_WARNING, 26),

    /** The client application you're using is outdated and no longer supported - please upgrade to a newer version. */
    CLIENT_OUTDATED(CLIENT_OUTDATED_MSG, CLIENT_OUTDATED_MSG, Category.CATEGORY_WARNING, 27),

    /** The client application you're using is outdated and no longer supported - please upgrade to a newer version. */
    CLIENT_VERSION_OUTDATED("Client outdated - current: \"%1$s\", required: \"%2$s\"", CLIENT_OUTDATED_MSG, Category.CATEGORY_WARNING, 28),

    /** The client application you're using is outdated and no longer supported - please upgrade to a newer version. */
    CLIENT_VERSION_UPDATE_AVAILABLE("Client update available - current: \"%1$s\", available: \"%2$s\"", CLIENT_VERSION_UPDATE_AVAILABLE_MSG, Category.CATEGORY_WARNING, 29),

    /** The pattern \"%1$s\" is invalid: \"%2$s\" */
    INVALID_PATTERN("The pattern \"%1$s\" is invalid: \"%2$s\"", INVALID_PATTERN_MSG, Category.CATEGORY_USER_INPUT, 30),

    /** The file name \"%1$s\" is conflicting with an equally named directory at \"%2$s\". */
    LEVEL_CONFLICTING_FILENAME(LEVEL_CONFLICTING_FILENAME_MSG, LEVEL_CONFLICTING_FILENAME_MSG, Category.CATEGORY_CONFLICT, 31),

    /** The path \"%1$s\" is conflicting with an equally named file at \"%2$s\". */
    LEVEL_CONFLICTING_PATH(LEVEL_CONFLICTING_PATH_MSG, LEVEL_CONFLICTING_PATH_MSG, Category.CATEGORY_CONFLICT, 32),

    /** The directory \"%1$s\" is not synchronizable. Please select another directory. */
    NOT_SYNCHRONIZABLE_DIRECTORY(NOT_SYNCHRONIZABLE_DIRECTORY_MSG, NOT_SYNCHRONIZABLE_DIRECTORY_MSG, Category.CATEGORY_USER_INPUT, 33),

    /** The supplied metadata could not be parsed: \"%1$s\" */
    METDATA_PARSE_ERROR(METDATA_PARSE_ERROR_MSG, METDATA_PARSE_ERROR_MSG, Category.CATEGORY_USER_INPUT, 34),

    /** The maximum number of synchronizable directories (%1$d) is exceeded. Please remove some directories and try again, or select another root folder. */
    TOO_MANY_DIRECTORIES(TOO_MANY_DIRECTORIES_MSG, TOO_MANY_DIRECTORIES_MSG, Category.CATEGORY_CAPACITY, 35),

    /** The maximum number of files (%1$d) is exceeded in directory \"%2$s\". Please remove some files and try again. */
    TOO_MANY_FILES(TOO_MANY_FILES_MSG, TOO_MANY_FILES_MSG, Category.CATEGORY_CAPACITY, 36),

    /** The token \"%1$s\" is already registered. */
    TOKEN_ALREADY_REGISTERED(TOKEN_ALREADY_REGISTERED_MSG, TOKEN_ALREADY_REGISTERED_MSG, Category.CATEGORY_CONFLICT, 37),

    /** Client connection lost unexpectedly */
    CLIENT_CONNECTION_LOST("Client connection lost unexpectedly", OXExceptionStrings.MESSAGE, Category.CATEGORY_CONNECTIVITY, 38),

    /** Empty files indicated for directory \"%1$s\". */
    ZERO_BYTE_FILES(ZERO_BYTE_FILES_MSG, ZERO_BYTE_FILES_MSG, Category.CATEGORY_ERROR, 39),

    /** A file named \"%1$s\" already exists at \"%2$s\". */
    FILE_ALREADY_EXISTS(FILE_ALREADY_EXISTS_MSG, FILE_ALREADY_EXISTS_MSG, Category.CATEGORY_ERROR, 40),

    /** A directory named \"%1$s\" already exists at \"%2$s\". */
    DIRECTORY_ALREADY_EXISTS(DIRECTORY_ALREADY_EXISTS_MSG, DIRECTORY_ALREADY_EXISTS_MSG, Category.CATEGORY_ERROR, 41),

    /** Unexpected database error, try again: \"%1$s\" */
    DB_ERROR_RETRY("Unexpected database error, try again: \"%1$s\"", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_TRY_AGAIN, 42),

    ;

    private static final String PREFIX = "DRV";

    private final Category category;
    private final int number;
    private final String message;
    private final String displayMessage;

    private DriveExceptionCodes(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage;
        this.number = detailNumber;
        this.category = category;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return null != displayMessage ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }

}
