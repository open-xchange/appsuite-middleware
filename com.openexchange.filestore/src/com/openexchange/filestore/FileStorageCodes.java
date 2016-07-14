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

package com.openexchange.filestore;

import static com.openexchange.exception.OXExceptionStrings.MESSAGE;
import static com.openexchange.exception.OXExceptionStrings.MESSAGE_RETRY;
import static com.openexchange.exception.OXExceptionStrings.SQL_ERROR_MSG;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error codes for the file storage exception.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum FileStorageCodes implements DisplayableOXExceptionCode {

    /** An I/O error occurred: %1$s */
    IOERROR("An I/O error occurred: %1$s", MESSAGE, Category.CATEGORY_SERVICE_DOWN, 3),
    /** May be used to turn the IOException of getInstance into a proper OXException */
    INSTANTIATIONERROR("File store could not be accessed: %s", MESSAGE, Category.CATEGORY_SERVICE_DOWN, 4),
    /** Cannot create directory "%1$s" in file storage. */
    CREATE_DIR_FAILED("Cannot create directory \"%1$s\" in file storage.", MESSAGE, Category.CATEGORY_CONFIGURATION, 6),
    /** Unsupported encoding. */
    ENCODING("Unsupported encoding.", MESSAGE, Category.CATEGORY_ERROR, 9),
    /** Number parsing problem. */
    NO_NUMBER("Number parsing problem.", MESSAGE, Category.CATEGORY_ERROR, 10),
    /** File storage is full. */
    STORE_FULL("File storage is full.", MESSAGE, Category.CATEGORY_CAPACITY, 11),
    /** Depth mismatch while computing next entry. */
    DEPTH_MISMATCH("'Depth' mismatch while computing next entry.", MESSAGE, Category.CATEGORY_ERROR, 12),
    /** Cannot remove lock file. */
    UNLOCK("Cannot remove lock file.", MESSAGE, Category.CATEGORY_SERVICE_DOWN, 13),
    /** Cannot create lock file here %1$s. Please check for a stale .lock file, inappropriate permissions or usage of the file store for too long a time. */
    LOCK("Cannot create lock file here %1$s. Please check for a stale .lock file, inappropriate permissions or usage of the file store for too long a time.", MESSAGE, Category.CATEGORY_SERVICE_DOWN, 14),
    /** Eliminating the file storage failed. */
    NOT_ELIMINATED("Eliminating the file storage failed.", MESSAGE, Category.CATEGORY_SERVICE_DOWN, 16),
    /** File does not exist in file storage "%1$s". Consider running consistency tool. */
    FILE_NOT_FOUND("File does not exist in file store \"%1$s\". Consider running the consistency tool.", MESSAGE, Category.CATEGORY_SERVICE_DOWN, 17),
    /** The requested range (offset: %1$d, length: %2$d) for the file \"%3$s\" (current size: %4$d) is invalid. */
    INVALID_RANGE("The requested range (offset: %1$d, length: %2$d) for the file \"%3$s\" (current size: %4$d) is invalid.", MESSAGE_RETRY, Category.CATEGORY_USER_INPUT, 18),
    /** The specified offset %1$d for the file \"%2$s\" (current size: %3$d) is invalid. */
    INVALID_OFFSET("The specified offset %1$d for the file \"%2$s\" (current size: %3$d) is invalid.", MESSAGE_RETRY, Category.CATEGORY_USER_INPUT, 19),
    /** The specified length %1$d for the file \"%2$s\" (current size: %3$d) is invalid. */
    INVALID_LENGTH("The specified length %1$d for the file \"%2$s\" (current size: %3$d) is invalid.", MESSAGE_RETRY, Category.CATEGORY_USER_INPUT, 20),
    /** No such file storage: %1$s */
    NO_SUCH_FILE_STORAGE("No such file storage: %1$s", MESSAGE, Category.CATEGORY_SERVICE_DOWN, 21),
    /** An end of stream has been reached unexpectedly during reading input. */
    CONNECTION_CLOSED("An end of stream has been reached unexpectedly during reading input.", FileStorageStrings.CONNECTION_CLOSED_MSG, Category.CATEGORY_CONNECTIVITY, 22),

    /**
     * "Wrong filestore %1$d for context %2$d needing filestore %3$d.
     */
    FILESTORE_MIXUP("Wrong file store %1$d for context %2$d. Correct file store: %3$d.", MESSAGE, Category.CATEGORY_ERROR, 201),
    /**
     * Cannot create URI from "%1$s".
     */
    URI_CREATION_FAILED("Cannot create URI from \"%1$s\".", MESSAGE, Category.CATEGORY_ERROR, 304),
    /**
     * SQL Problem: "%s".
     */
    SQL_PROBLEM("SQL problem: \"%s\".", SQL_ERROR_MSG, Category.CATEGORY_ERROR, 306),

    ;

    private static final String PREFIX = "FLS";

    /**
     * Gets the <code>"FLS"</code> prefix for this error code.
     *
     * @return The prefix
     */
    public static String prefix() {
        return PREFIX;
    }

    private final String message;
    private final String displayMessage;
    private final Category category;
    private final int number;

    /**
     * Initializes a new {@link FileStorageCodes}.
     */
    private FileStorageCodes(final String message, String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage;
        this.category = category;
        this.number = detailNumber;
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
    public int getNumber() {
        return number;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
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
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }

}
