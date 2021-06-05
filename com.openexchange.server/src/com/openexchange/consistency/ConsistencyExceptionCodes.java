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

package com.openexchange.consistency;

import static com.openexchange.consistency.ConsistencyExceptionMessages.MALFORMED_POLICY_MSG_DISPLAY;
import static com.openexchange.consistency.ConsistencyExceptionMessages.UNKNOWN_POLICY_MSG_DISPLAY;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link ConsistencyExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum ConsistencyExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * Error communicating with mbean in server: %s
     */
    COMMUNICATION_PROBLEM("Error communicating with mbean in server: %s", CATEGORY_ERROR, 1),
    /**
     * Registration of consistency MBean failed.
     */
    REGISTRATION_FAILED("Registration of consistency MBean failed.", CATEGORY_CONFIGURATION, 2),
    /**
     * Unregistration of consistency MBean failed.
     */
    UNREGISTRATION_FAILED("Unregistration of consistency MBean failed.", CATEGORY_CONFIGURATION, 3),
    /**
     * Incorrectly formatted policy. Policies are formatted like "condition:action"
     */
    MALFORMED_POLICY("Incorrectly formatted policy. Policies are formatted like \"condition:action\"", CATEGORY_USER_INPUT, 4, MALFORMED_POLICY_MSG_DISPLAY),
    /**
     * Unknown/Unsupported policy
     */
    UNKNOWN_POLICY("Unknown policy %s.", CATEGORY_USER_INPUT, 5, UNKNOWN_POLICY_MSG_DISPLAY),
    /**
     * <li>An SQL error occurred: %1$s</li>
     */
    SQL_ERROR("An SQL error occurred: %1$s", Category.CATEGORY_ERROR, 5),
    ;

    private final String message;
    private final Category category;
    private final int number;

    /**
     * Message displayed to the user
     */
    private String displayMessage;

    /**
     * Initializes a new {@link ConsistencyExceptionCodes}.
     *
     * @param message
     * @param category
     * @param number
     */
    private ConsistencyExceptionCodes(final String message, final Category category, final int number) {
        this(message, category, number, null);
    }

    /**
     * Initializes a new {@link ConsistencyExceptionCodes}.
     *
     * @param message
     * @param category
     * @param number
     * @param displayMessage
     */
    private ConsistencyExceptionCodes(final String message, final Category category, final int number, final String displayMessage) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.category = category;
        this.number = number;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public String getPrefix() {
        return "CSTY";
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
    public Category getCategory() {
        return category;
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
