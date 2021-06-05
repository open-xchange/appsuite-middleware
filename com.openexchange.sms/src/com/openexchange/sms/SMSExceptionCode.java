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

package com.openexchange.sms;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;


/**
 * {@link SMSExceptionCode}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public enum SMSExceptionCode implements DisplayableOXExceptionCode {
    /**
     * Could not parse phone number %1$s
     */
    PARSING_ERROR("Could not parse phone number %1$s", Category.CATEGORY_USER_INPUT, 1, SMSExceptionMessages.PARSING_ERROR_MSG),
    /**
     * Unknown country tag: %1$s
     */
    UNKNOWN_COUNTRY("Unknown country tag: %1$s", Category.CATEGORY_USER_INPUT, 2, SMSExceptionMessages.UNKNOWN_COUNTRY_MSG),
    /**
     * Message could not be sent: %1$s
     */
    NOT_SENT("Message could not be sent: %1$s", Category.CATEGORY_ERROR, 3),
    /**
     * Message is too long (%1$s characters). Maximum size is %2$s characters.
     */
    MESSAGE_TOO_LONG("Message is too long (%1$s characters). Maximum size is %2$s characters.", Category.CATEGORY_USER_INPUT, 4, SMSExceptionMessages.MESSAGE_TOO_LONG_MSG),
    /**
     * Service %1$s is not available.
     */
    SERVICE_UNAVAILABLE("Service %1$s is not available.", Category.CATEGORY_ERROR, 5),
    ;

    public static final String PREFIX = "SMS";

    private final Category category;

    private final int detailNumber;

    private final String message;

    /**
     * Message displayed to the user
     */
    private String displayMessage;

    /**
     * Initializes a new {@link SMSExceptionCode}.
     *
     * @param message
     * @param category
     * @param detailNumber
     */
    private SMSExceptionCode(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    /**
     * Initializes a new {@link SMSExceptionCode}.
     *
     * @param message
     * @param category
     * @param detailNumber
     * @param displayMessage
     */
    private SMSExceptionCode(final String message, final Category category, final int detailNumber, final String displayMessage) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
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
