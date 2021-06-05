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

package com.openexchange.tools.servlet;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * Error codes for JSON exceptions.
 */
public enum OXJSONExceptionCodes implements DisplayableOXExceptionCode {
    /**
     * Exception while writing JSON.
     */
    JSON_WRITE_ERROR(OXJSONExceptionCodes.JSON_WRITE_ERROR_MSG, Category.CATEGORY_ERROR, 1),
    /**
     * Exception while parsing JSON: "%s".
     */
    JSON_READ_ERROR(OXJSONExceptionCodes.JSON_READ_ERROR_MSG, Category.CATEGORY_ERROR, 2),
    /**
     * Invalid cookie.
     */
    INVALID_COOKIE(OXJSONExceptionCodes.INVALID_COOKIE_MSG, Category.CATEGORY_TRY_AGAIN, 3, OXJSONExceptionMessage.INVALID_COOKIE_MSG_DISPLAY),
    /**
     * Exception while building JSON.
     */
    JSON_BUILD_ERROR(OXJSONExceptionCodes.JSON_BUILD_ERROR_MSG, Category.CATEGORY_ERROR, 4),
    /**
     * Value "%1$s" of attribute %s contains non digit characters.
     */
    CONTAINS_NON_DIGITS(OXJSONExceptionCodes.CONTAINS_NON_DIGITS_MSG, Category.CATEGORY_USER_INPUT, 5, OXJSONExceptionMessage.CONTAINS_NON_DIGITS_MSG_DISPLAY),
    /**
     * Too many digits within field %1$s.
     */
    TOO_BIG_NUMBER(OXJSONExceptionCodes.TOO_BIG_NUMBER_MSG, Category.CATEGORY_USER_INPUT, 6, OXJSONExceptionMessage.TOO_BIG_NUMBER_MSG_DISPLAY),
    /**
     * Unable to parse value "%1$s" within field %2$s as a number.
     */
    NUMBER_PARSING(OXJSONExceptionCodes.NUMBER_PARSING_MSG, Category.CATEGORY_ERROR, 7),
    /**
     * Invalid value \"%2$s\" in JSON attribute \"%1$s\".
     */
    INVALID_VALUE(OXJSONExceptionCodes.INVALID_VALUE_MSG, Category.CATEGORY_USER_INPUT, 8),
    /**
     * Missing field "%1$s" in JSON data.
     */
    MISSING_FIELD(OXJSONExceptionCodes.MISSING_FIELD_MSG, Category.CATEGORY_ERROR, 9);

    /**
     * Exception while writing JSON.
     */
    private static final String JSON_WRITE_ERROR_MSG = "Exception while writing JSON.";

    /**
     * Exception while parsing JSON: "%s".
     */
    private static final String JSON_READ_ERROR_MSG = "Exception while parsing JSON: \"%s\".";

    /**
     * Exception while building JSON.
     */
    private static final String JSON_BUILD_ERROR_MSG = "Exception while building JSON.";

    /**
     * Unable to parse value "%1$s" within field %2$s as a number.
     */
    private static final String NUMBER_PARSING_MSG = "Unable to parse value \"%1$s\" within field %2$s as a number.";

    /**
     * Invalid value \"%2$s\" in JSON attribute \"%1$s\".
     */
    private static final String INVALID_VALUE_MSG = "Invalid value \"%2$s\" in JSON attribute \"%1$s\".";

    /**
     * Missing field "%1$s" in JSON data.
     */
    private static final String MISSING_FIELD_MSG = "Missing field \"%1$s\" in JSON data.";

    /**
     * Invalid cookie.
     */
    private static final String INVALID_COOKIE_MSG = "Invalid cookie.";

    /**
     * Value "%1$s" of attribute %s contains non digit characters.
     */
    private static final String CONTAINS_NON_DIGITS_MSG = "Value \"%1$s\" of attribute %2$s contains non digit characters.";

    /**
     * Too many digits within field %1$s.
     */
    private static final String TOO_BIG_NUMBER_MSG = "Too many digits within field %1$s.";

    private static final String PREFIX = "SVL";

    private final String message;
    private final Category category;
    private final int number;

    /**
     * Message displayed to the user
     */
    private String displayMessage;

    /**
     * Initializes a new {@link OXJSONExceptionCodes}.
     * 
     * @param message
     * @param category
     * @param detailNumber
     */
    private OXJSONExceptionCodes(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    /**
     * Initializes a new {@link OXJSONExceptionCodes}.
     * 
     * @param message
     * @param category
     * @param detailNumber
     * @param displayMessage
     */
    private OXJSONExceptionCodes(final String message, final Category category, final int detailNumber, final String displayMessage) {
        this.message = message;
        this.category = category;
        number = detailNumber;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
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
    public String getPrefix() {
        return PREFIX;
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
