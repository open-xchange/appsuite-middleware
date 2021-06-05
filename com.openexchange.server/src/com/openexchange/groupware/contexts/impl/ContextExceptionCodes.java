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

package com.openexchange.groupware.contexts.impl;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * Error codes for context exceptions.
 */
public enum ContextExceptionCodes implements DisplayableOXExceptionCode {
    /**
     * Mailadmin for a context is missing.
     */
    NO_MAILADMIN("Cannot resolve mailadmin for context %d.", OXExceptionStrings.MESSAGE, Category.CATEGORY_CONFIGURATION, 1),
    /**
     * Cannot find context %d.
     */
    NOT_FOUND("Cannot find context %d.", ContextExceptionMessage.NOT_FOUND_MSG, Category.CATEGORY_CONFIGURATION, 2),
    /**
     * No connection to database.
     */
    NO_CONNECTION("Cannot get connection to database.", ContextExceptionMessage.NO_CONNECTION_TO_CONTEXT_MSG, Category.CATEGORY_SERVICE_DOWN, 5),
    /**
     * SQL problem: %1$s.
     */
    SQL_ERROR("SQL problem: %1$s.", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 6),
    /**
     * Updating database... Try again later.
     */
    UPDATE("Updating database ... Try again later.", ContextExceptionMessage.UPDATE_MSG, Category.CATEGORY_TRY_AGAIN, 7),
    /**
     * Cannot find context "%s".
     */
    NO_MAPPING("Context \"%s\" cannot be found.", ContextExceptionMessage.NO_MAPPING_MSG, Category.CATEGORY_USER_INPUT, 10),
    /**
     * Denied concurrent update for context attributes for context %1$d.
     */
    CONCURRENT_ATTRIBUTES_UPDATE("Denied concurrent update for context attributes for context %1$d.", ContextExceptionMessage.CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY, Category.CATEGORY_ERROR, 11),
    /**
     * The context %d is located in server with id %d
     */
    LOCATED_IN_ANOTHER_SERVER("The context %d is located in server with id %d", OXExceptionStrings.MESSAGE, Category.CATEGORY_ERROR, 12),
    /**
     * Database update needed.
     * <p>
     * <b>Note</b>: This error code intentionally uses the same display message as {@link ContextExceptionCodes#UPDATE}
     */
    UPDATE_NEEDED("Database update needed.", ContextExceptionMessage.UPDATE_MSG, Category.CATEGORY_SERVICE_DOWN, 13),
    ;

    /**
     * (Log) Message of the exception.
     */
    private final String message;

    /**
     * Display message of the exception.
     */
    private final String displayMessage;

    /**
     * Category of the exception.
     */
    private final Category category;

    /**
     * Detail number of the exception.
     */
    private final int number;

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param number detail number.
     */
    private ContextExceptionCodes(final String message, String displayMessage, final Category category, final int number) {
        this.message = message;
        this.displayMessage = displayMessage;
        this.category = category;
        this.number = number;
    }

    @Override
    public String getPrefix() {
        return "CTX";
    }

    /**
     * @return the message
     */
    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    /**
     * @return the category
     */
    @Override
    public Category getCategory() {
        return category;
    }

    /**
     * @return the number
     */
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
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }

}
