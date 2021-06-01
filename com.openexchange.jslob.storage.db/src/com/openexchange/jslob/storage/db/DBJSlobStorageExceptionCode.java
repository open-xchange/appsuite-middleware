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

package com.openexchange.jslob.storage.db;

import com.openexchange.exception.Category;
import com.openexchange.exception.Category.EnumCategory;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link DBJSlobStorageExceptionCode}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum DBJSlobStorageExceptionCode implements DisplayableOXExceptionCode {

    /**
     * An unexpected error occurred: %1$s
     */
    UNEXPECTED_ERROR(DBJSlobStorageExceptionCode.UNEXPECTED_ERROR_MSG, EnumCategory.ERROR, 1),
    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR(DBJSlobStorageExceptionCode.SQL_ERROR_MSG, EnumCategory.ERROR, 2, OXExceptionStrings.SQL_ERROR_MSG),
    /**
     * No entry available for identifier: %1$s
     */
    NO_ENTRY(DBJSlobStorageExceptionCode.NO_ENTRY_MSG, EnumCategory.ERROR, 3),
    /**
     * Entry already locked for identifier: %1$s
     */
    ALREADY_LOCKED(DBJSlobStorageExceptionCode.ALREADY_LOCKED_MSG, EnumCategory.ERROR, 4),
    /**
     * Lock failed for entry with identifier: %1$s
     */
    LOCK_FAILED(DBJSlobStorageExceptionCode.LOCK_FAILED_MSG, EnumCategory.ERROR, 5),
    /**
     * Unlock failed for entry with identifier: %1$s
     */
    UNLOCK_FAILED(DBJSlobStorageExceptionCode.UNLOCK_FAILED_MSG, EnumCategory.ERROR, 6),
    /**
     * Connection failed to JSlob storage
     */
    CONNECTION_ERROR(DBJSlobStorageExceptionCode.NO_CONNECTION_MSG, EnumCategory.SERVICE_DOWN, 7, OXExceptionStrings.MESSAGE_RETRY);

    // An unexpected error occurred: %1$s
    private static final String UNEXPECTED_ERROR_MSG = "An unexpected error occurred: %1$s";

    // A SQL error occurred: %1$s
    private static final String SQL_ERROR_MSG = "A SQL error occurred: %1$s";

    // No entry available for identifier: %1$s
    private static final String NO_ENTRY_MSG = "No entry available for identifier: %1$s";

    // Entry already locked for identifier: %1$s
    private static final String ALREADY_LOCKED_MSG = "Entry already locked for identifier: %1$s";

    // Lock failed for entry with identifier: %1$s
    private static final String LOCK_FAILED_MSG = "Lock failed for entry with identifier: %1$s";

    // Unlock failed for entry with identifier: %1$s
    private static final String UNLOCK_FAILED_MSG = "Unlock failed for entry with identifier: %1$s";

    // Connection to database failed
    private static final String NO_CONNECTION_MSG = "Connection to database failed";
    /**
     * The error code prefix for JSlob exceptions.
     */
    public static final String PREFIX = "DB_JSNCON_STORAGE".intern();

    private final Category category;

    private final int number;

    private final String message;

    /**
     * Message displayed to the user
     */
    private String displayMessage;

    /**
     * Initializes a new {@link DBJSlobStorageExceptionCode}.
     * 
     * @param message
     * @param category
     * @param detailNumber
     */
    private DBJSlobStorageExceptionCode(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    /**
     * Initializes a new {@link DBJSlobStorageExceptionCode}.
     * 
     * @param message
     * @param category
     * @param detailNumber
     * @param displayMessage
     */
    private DBJSlobStorageExceptionCode(final String message, final Category category, final int detailNumber, final String displayMessage) {
        this.message = message;
        number = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
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
    public int getNumber() {
        return number;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayMessage() {
        return this.displayMessage;
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
