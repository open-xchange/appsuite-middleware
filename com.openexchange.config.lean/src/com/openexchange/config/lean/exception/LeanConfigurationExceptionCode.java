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

package com.openexchange.config.lean.exception;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link LeanConfigurationExceptionCode}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum LeanConfigurationExceptionCode implements DisplayableOXExceptionCode {
    /**
     * The value '%1$s' cannot be parsed as '%2$s'
     */
    CANNOT_PARSE_VALUE("The value '%1$s' cannot be parsed as '%2$s'", Category.CATEGORY_ERROR, 1),
    /**
     * A parsing error occurred: '%1$s'
     */
    PARSING_ERROR("A parsing error occurred: '%1$s'", Category.CATEGORY_ERROR, 2),
    ;

    /**
     * LEAN_CONFIGURATION
     */
    private static final String PREFIX = "LEAN_CONFIGURATION";

    private final String message;

    private final String displayMessage;

    private final int detailNumber;

    private final Category category;

    /**
     * Initialises a new {@link LeanConfigurationExceptionCode}.
     *
     * @param message The (technical) error message
     * @param category The category
     * @param detailNumber The detail number
     */
    private LeanConfigurationExceptionCode(final String message, final Category category, final int detailNumber) {
        this(message, OXExceptionStrings.MESSAGE, category, detailNumber);
    }

    /**
     * Initialises a new {@link LeanConfigurationExceptionCode} containing a display message for the user.
     *
     * @param message The (technical) error message
     * @param displayMessage The display message for the enduser
     * @param category The category
     * @param detailNumber The detail number
     */
    private LeanConfigurationExceptionCode(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage;
        this.detailNumber = detailNumber;
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
    public int getNumber() {
        return detailNumber;
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
