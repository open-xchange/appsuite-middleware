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

package com.openexchange.groupware.delete;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * Error code for failed delete events.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum DeleteFailedExceptionCodes implements DisplayableOXExceptionCode {
    /**
     * Unknown delete event type: %1$d
     */
    UNKNOWN_TYPE(DeleteFailedExceptionCodes.UNKNOWN_TYPE_MSG, Category.CATEGORY_ERROR, 1),
    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR(DeleteFailedExceptionCodes.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 2, OXExceptionStrings.SQL_ERROR_MSG),
    /**
     * An error occurred: %1$s
     */
    ERROR(DeleteFailedExceptionCodes.ERROR_MSG, Category.CATEGORY_ERROR, 3);

    private final static String SQL_ERROR_MSG = "A SQL error occurred: %1$s";

    private final static String ERROR_MSG = "An error occurred: %1$s";

    private final static String UNKNOWN_TYPE_MSG = "Unknown delete event type: %1$d";

    private final String message;

    private final Category category;

    private final int detailNumber;

    /**
     * Message displayed to the user
     */
    private String displayMessage;

    /**
     * Initializes a new {@link DeleteFailedExceptionCodes}.
     * 
     * @param message
     * @param category
     * @param detailNumber
     */
    private DeleteFailedExceptionCodes(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    /**
     * Default constructor.
     * 
     * @param message
     * @param category
     * @param number
     * @param displayMessage
     */
    private DeleteFailedExceptionCodes(final String message, final Category category, final int detailNumber, final String displayMessage) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public final Category getCategory() {
        return category;
    }

    @Override
    public final int getNumber() {
        return detailNumber;
    }

    @Override
    public final String getMessage() {
        return message;
    }

    @Override
    public String getPrefix() {
        return "DEL";
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
