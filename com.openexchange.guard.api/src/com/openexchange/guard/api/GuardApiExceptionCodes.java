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

package com.openexchange.guard.api;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link GuardApiExceptionCodes} - Enumeration of all errors.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum GuardApiExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", Category.CATEGORY_ERROR, 1),
    /**
     * A Guard error occurred: %1$s
     */
    GUARD_ERROR("A Guard error occurred: %1$s", GuardApiExceptionMessages.GUARD_ERROR_MSG, Category.CATEGORY_ERROR, 2),
    /**
     * A Guard error occurred: %1$s
     */
    GUARD_SERVER_ERROR("A Guard server error occurred. Status code: %1$s. Error message: %2$s", GuardApiExceptionMessages.GUARD_SERVER_ERROR_MSG, Category.CATEGORY_ERROR, 3),
    /**
     * Invalid Guard URL: %1$s
     */
    INVALID_GUARD_URL("Invalid Guard URL: %1$s", GuardApiExceptionMessages.INVALID_GUARD_URL_MSG, Category.CATEGORY_ERROR, 4),
    /**
     * The Guard resource does not exist: %1$s
     */
    NOT_FOUND("The Guard resource does not exist: %1$s", GuardApiExceptionMessages.NOT_FOUND_MSG, Category.CATEGORY_ERROR, 5),
    /**
     * The Guard resource does not exist
     */
    NOT_FOUND_SIMPLE("The Guard resource does not exist", GuardApiExceptionMessages.NOT_FOUND_SIMPLE_MSG, NOT_FOUND.getCategory(), NOT_FOUND.getNumber()),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", GuardApiExceptionMessages.IO_ERROR_MSG, Category.CATEGORY_ERROR, 6),
    /**
     * Authentication failed: %1$s
     */
    AUTH_ERROR("Authentication failed: %1$s", GuardApiExceptionMessages.AUTH_ERROR_MSG, Category.CATEGORY_ERROR, 7),
    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR("A JSON error occurred: %1$s", GuardApiExceptionMessages.JSON_ERROR_MSG, Category.CATEGORY_ERROR, 8),

    ;

    private final Category category;

    private final int detailNumber;

    private final String message;

    private final String displayMessage;

    private GuardApiExceptionCodes(final String message, final Category category, final int detailNumber) {
        this(message, null, category, detailNumber);
    }

    private GuardApiExceptionCodes(final String message, final String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
        this.displayMessage = (displayMessage == null) ? OXExceptionStrings.MESSAGE : displayMessage;
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
        return detailNumber;
    }

    @Override
    public String getPrefix() {
        return "GUARD";
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

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }
}
