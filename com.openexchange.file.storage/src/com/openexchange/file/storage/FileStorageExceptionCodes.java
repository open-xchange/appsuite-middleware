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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.file.storage.exception.FileStorageExceptionFactory;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * {@link FileStorageExceptionCodes} - Enumeration of all {@link FileStorageException}s.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public enum FileStorageExceptionCodes implements OXErrorMessage {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(FileStorageExceptionMessages.UNEXPECTED_ERROR_MSG, Category.CODE_ERROR, 1),
    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR(FileStorageExceptionMessages.SQL_ERROR_MSG, Category.CODE_ERROR, 2),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(FileStorageExceptionMessages.IO_ERROR_MSG, Category.CODE_ERROR, 3),
    /**
     * An I/O error occurred: %1$s
     */
    JSON_ERROR(FileStorageExceptionMessages.JSON_ERROR_MSG, Category.CODE_ERROR, 14),
    /**
     * File storage account %1$s of service "%2$s" could not be found for user %3$s in context %4$s.
     */
    ACCOUNT_NOT_FOUND(FileStorageExceptionMessages.ACCOUNT_NOT_FOUND_MSG, Category.CODE_ERROR, 4),
    /**
     * The operation is not supported by service %1$s.
     */
    OPERATION_NOT_SUPPORTED(FileStorageExceptionMessages.OPERATION_NOT_SUPPORTED_MSG, Category.CODE_ERROR, 6),
    /**
     * The folder "%1$s" cannot be found in account %2$s of service "%3$s" of user %4$s in context %5$s.
     */
    FOLDER_NOT_FOUND(FileStorageExceptionMessages.FOLDER_NOT_FOUND_MSG, Category.CODE_ERROR, 7),
    /**
     * Invalid file identifier: %1$s
     */
    INVALID_FILE_IDENTIFIER(FileStorageExceptionMessages.INVALID_FILE_IDENTIFIER_MSG, Category.CODE_ERROR, 8),
    /**
     * Invalid header "%1$s": %2$s
     */
    INVALID_HEADER(FileStorageExceptionMessages.INVALID_HEADER_MSG, Category.CODE_ERROR, 9),
    /**
     * Unknown action to perform: %1$s.
     */
    UNKNOWN_ACTION(FileStorageExceptionMessages.UNKNOWN_ACTION_MSG, Category.CODE_ERROR, 10),
    /**
     * A file error occurred: %1$s
     */
    FILE_ERROR(FileStorageExceptionMessages.FILE_ERROR_MSG, Category.CODE_ERROR, 11),
    /**
     * Wrongly formatted address: %1$s.
     */
    ADDRESS_ERROR(FileStorageExceptionMessages.ADDRESS_ERROR_MSG, Category.CODE_ERROR, 12),
    /**
     * Unknown file content: %1$s.
     */
    UNKNOWN_FILE_CONTENT(FileStorageExceptionMessages.UNKNOWN_FILE_CONTENT_MSG, Category.CODE_ERROR, 14),
    /**
     * Unknown file storage service: %1$s.
     */
    UNKNOWN_FILE_STORAGE_SERVICE(FileStorageExceptionMessages.UNKNOWN_FILE_STORAGE_SERVICE_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 15),
    /**
     * Missing parameter: %1$s.
     */
    MISSING_PARAMETER(FileStorageExceptionMessages.MISSING_PARAMETER_MSG, Category.USER_INPUT, 16),
    /**
     * Invalid parameter: %1$s with value '%2$s'.
     */
    INVALID_PARAMETER(FileStorageExceptionMessages.INVALID_PARAMETER_MSG, Category.USER_INPUT, 17),
    /**
     * File part is read-only: %1$s
     */
    READ_ONLY(FileStorageExceptionMessages.READ_ONLY_MSG, Category.USER_INPUT, 18),
    /**
     * Unknown color label index: %1$s
     */
    UNKNOWN_COLOR_LABEL(FileStorageExceptionMessages.UNKNOWN_COLOR_LABEL_MSG, Category.USER_INPUT, 19),
    /**
     * A duplicate folder named "%1$s" already exists below parent folder "%2$s".
     */
    DUPLICATE_FOLDER(FileStorageExceptionMessages.DUPLICATE_FOLDER_MSG, Category.CODE_ERROR, 20),
    /**
     * No create access on folder %1$s.
     */
    NO_CREATE_ACCESS(FileStorageExceptionMessages.NO_CREATE_ACCESS_MSG, Category.PERMISSION, 21),
    /**
     * Not connected
     */
    NOT_CONNECTED(FileStorageExceptionMessages.NOT_CONNECTED_MSG, Category.PERMISSION, 22), 
    /**
     * Invalid sorting column. Cannot sort by %1$s.
     */
    INVALID_SORTING_COLUMN(FileStorageExceptionMessages.INVALID_SORTING_COLUMN_MSG, Category.USER_INPUT, 23),
    /**
     * No attachment found with section identifier %1$s in file %2$s in folder %3$s.
     */
    ATTACHMENT_NOT_FOUND(FileStorageExceptionMessages.ATTACHMENT_NOT_FOUND_MSG, Category.CODE_ERROR, 24),
    /**
     * File %1$s not found in folder %2$s.
     */
    FILE_NOT_FOUND(FileStorageExceptionMessages.FILE_NOT_FOUND_MSG, Category.CODE_ERROR, 25),
    /**
     * No account manager could be found for service: %1$s.
     */
    NO_ACCOUNT_MANAGER_FOR_SERVICE(FileStorageExceptionMessages.NO_ACCOUNT_MANAGER_FOR_SERVICE_MSG, Category.CODE_ERROR, 26),
    /**
     * Invalid URL "%1$s". Error: %2$s.
     */
    INVALID_URL(FileStorageExceptionMessages.INVALID_URL_MSG, Category.USER_INPUT, 27);

    private final Category category;

    private final int detailNumber;

    private final String message;

    private FileStorageExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    public int getDetailNumber() {
        return detailNumber;
    }

    public String getHelp() {
        return null;
    }

    private static final Object[] EMPTY = new Object[0];

    /**
     * Creates a new file storage exception of this error type with no message arguments.
     * 
     * @return A new twitter exception
     */
    public FileStorageException create() {
        return FileStorageExceptionFactory.getInstance().create(this, EMPTY);
    }

    /**
     * Creates a new file storage exception of this error type with specified message arguments.
     * 
     * @param messageArgs The message arguments
     * @return A new twitter exception
     */
    public FileStorageException create(final Object... messageArgs) {
        return FileStorageExceptionFactory.getInstance().create(this, messageArgs);
    }

    /**
     * Creates a new file storage exception of this error type with specified cause and message arguments.
     * 
     * @param cause The cause
     * @param messageArgs The message arguments
     * @return A new twitter exception
     */
    public FileStorageException create(final Throwable cause, final Object... messageArgs) {
        return FileStorageExceptionFactory.getInstance().create(this, cause, messageArgs);
    }
}
