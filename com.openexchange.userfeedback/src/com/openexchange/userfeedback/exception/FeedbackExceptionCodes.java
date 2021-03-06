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

package com.openexchange.userfeedback.exception;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link FeedbackExceptionCodes}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public enum FeedbackExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 1),

    /**
     * SQL problem: %1$s.
     */
    SQL_ERROR("SQL problem: %1$s", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 2),

    /**
     * Unknown feedback type: %1$s
     */
    INVALID_FEEDBACK_TYPE("Unknown feedback type: %1$s", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 3),

    /**
     * Unknown data type for feedback. Please provide %1$s
     */
    INVALID_DATA_TYPE("Unknown data type for feedback. Please provide %1$s", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 4),

    /**
     * No global database configured.
     */
    GLOBAL_DB_NOT_CONFIGURED("No global database configured.", OXExceptionStrings.MESSAGE, CATEGORY_CONFIGURATION, 5),

    /**
     * Provided value(s) for parameter(s) '%1$s' is/are invalid.
     */
    INVALID_PARAMETER_VALUE("Provided value(s) for parameter(s) '%1$s' is/are invalid.", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 6),

    /**
     * Provided SMTP configuration is invalid, unable to connect to server
     */
    INVALID_SMTP_CONFIGURATION("Provided SMTP configuration is invalid, unable to connect to server.", OXExceptionStrings.MESSAGE, CATEGORY_CONFIGURATION, 7),

    /**
     * Provided addresses are invalid.
     */
    INVALID_EMAIL_ADDRESSES("Provided addresses are invalid.", OXExceptionStrings.MESSAGE, CATEGORY_USER_INPUT, 8),

    /**
     * Provided value(s) for parameter(s) '%1$s' is/are too big.
     */
    INVALID_PARAMETER_VALUE_SIZE("Provided value(s) for parameter(s) '%1$s' is/are too big.", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 9),

    /**
     * Provided PGP configuration is invalid, unable to sign mail.
     */
    INVALID_PGP_CONFIGURATION("Provided PGP configuration is invalid, unable to sign mail.", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 10),

    /**
     * No mail address with valid PGP public key, unable to send PGP-encrypted mail.
     */
    INVALID_EMAIL_ADDRESSES_PGP("No mail address with valid PGP public key found, unable to send PGP-encrypted mail.", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 11),

    ;

    /**
     * The error code prefix for capability module.
     */
    public static final String PREFIX = "FEEDBACK";

    private final Category category;

    private final int detailNumber;

    private final String displayMessage;

    private final String message;

    private FeedbackExceptionCodes(final String message, final String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.displayMessage = displayMessage;
        this.category = category;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
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
