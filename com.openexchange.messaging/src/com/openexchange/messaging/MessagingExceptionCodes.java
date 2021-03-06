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

package com.openexchange.messaging;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link MessagingExceptionCodes} - Enumeration of all {@link OXException}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public enum MessagingExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", CATEGORY_ERROR, 1, null),
    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR("A SQL error occurred: %1$s", CATEGORY_ERROR, 2, OXExceptionStrings.SQL_ERROR_MSG),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", CATEGORY_ERROR, 3, null),
    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR("A JSON error occurred: %1$s", CATEGORY_ERROR, 14, null),
    /**
     * Messaging account %1$s of service "%2$s" could not be found for user %3$s in context %4$s.
     */
    ACCOUNT_NOT_FOUND("Messaging account %1$s of service \"%2$s\" could not be found for user %3$s in context %4$s.", CATEGORY_ERROR, 4,
        null),
    /**
     * The operation is not supported by service %1$s.
     */
    OPERATION_NOT_SUPPORTED("The operation is not supported by service %1$s.", CATEGORY_ERROR, 6, null),
    /**
     * The folder "%1$s" cannot be found in account %2$s of service "%3$s" of user %4$s in context %5$s.
     */
    FOLDER_NOT_FOUND("The folder \"%1$s\" cannot be found in account %2$s of service \"%3$s\" of user %4$s in context %5$s.",
        CATEGORY_ERROR, 7, null),
    /**
     * Invalid message identifier: %1$s
     */
    INVALID_MESSAGE_IDENTIFIER("Invalid message identifier: %1$s", CATEGORY_ERROR, 8, null),
    /**
     * Invalid header "%1$s": %2$s
     */
    INVALID_HEADER("Invalid header \"%1$s\": %2$s", CATEGORY_ERROR, 9, null),
    /**
     * Unknown action to perform: %1$s.
     */
    UNKNOWN_ACTION("Unknown action to perform: %1$s.", CATEGORY_ERROR, 10, null),
    /**
     * A messaging error occurred: %1$s
     */
    MESSAGING_ERROR("A messaging error occurred: %1$s", CATEGORY_ERROR, 11, MessagingExceptionMessages.MESSAGING_ERROR_MSG),
    /**
     * Wrongly formatted address: %1$s.
     */
    ADDRESS_ERROR("Wrongly formatted address: %1$s.", CATEGORY_ERROR, 12, MessagingExceptionMessages.ADDRESS_ERROR_MSG),
    /**
     * Unknown messaging content: %1$s.
     */
    UNKNOWN_MESSAGING_CONTENT("Unknown messaging content: %1$s.", CATEGORY_ERROR, 14,
        MessagingExceptionMessages.UNKNOWN_MESSAGING_CONTENT_MSG),
    /**
     * Unknown messaging service: %1$s.
     */
    UNKNOWN_MESSAGING_SERVICE("Unknown messaging service: %1$s.", CATEGORY_SERVICE_DOWN, 15, null),
    /**
     * Missing parameter: %1$s.
     */
    MISSING_PARAMETER("Missing parameter: %1$s.", CATEGORY_USER_INPUT, 16, null),
    /**
     * Invalid parameter: %1$s with value '%2$s'.
     */
    INVALID_PARAMETER("Invalid parameter: %1$s with value '%2$s'.", CATEGORY_USER_INPUT, 17,
        MessagingExceptionMessages.INVALID_PARAMETER_MSG),
    /**
     * Messaging part is read-only: %1$s
     */
    READ_ONLY("Messaging part is read-only.: %1$s", CATEGORY_USER_INPUT, 18, MessagingExceptionMessages.READ_ONLY_MSG),
    /**
     * Unknown color label index: %1$s
     */
    UNKNOWN_COLOR_LABEL("Unknown color label index: %1$s", CATEGORY_USER_INPUT, 19, MessagingExceptionMessages.UNKNOWN_COLOR_LABEL_MSG),
    /**
     * A duplicate folder named "%1$s" already exists below parent folder "%2$s".
     */
    DUPLICATE_FOLDER("A duplicate folder named \"%1$s\" already exists below parent folder \"%2$s\".", CATEGORY_ERROR, 20,
        MessagingExceptionMessages.DUPLICATE_FOLDER_MSG),
    /**
     * No create access on mail folder %1$s.
     */
    NO_CREATE_ACCESS("No create access on mail folder %1$s.", CATEGORY_PERMISSION_DENIED, 21,
        MessagingExceptionMessages.NO_CREATE_ACCESS_MSG),
    /**
     * Not connected
     */
    NOT_CONNECTED("Not connected", CATEGORY_PERMISSION_DENIED, 22, MessagingExceptionMessages.NOT_CONNECTED_MSG),
    /**
     * Invalid sorting column. Cannot sort by %1$s.
     */
    INVALID_SORTING_COLUMN("Invalid sorting column. Cannot sort by %1$s.", CATEGORY_USER_INPUT, 23,
        MessagingExceptionMessages.INVALID_SORTING_COLUMN_MSG),
    /**
     * No attachment found with section identifier %1$s in message %2$s in folder %3$s.
     */
    ATTACHMENT_NOT_FOUND("No attachment found with section identifier %1$s in message %2$s in folder %3$s.", CATEGORY_ERROR, 24,
        MessagingExceptionMessages.ATTACHMENT_NOT_FOUND_MSG),
    /**
     * Message %1$s not found in folder %2$s.
     */
    MESSAGE_NOT_FOUND("Message %1$s not found in folder %2$s.", CATEGORY_ERROR, 25, MessagingExceptionMessages.MESSAGE_NOT_FOUND_MSG),
    /**
     * Invalid OAuth account specified. OAuth account of type '%1$s' cannot be mapped to messaging service '%2$s'.
     */
    INVALID_OAUTH_ACCOUNT("Invalid OAuth account specified. OAuth account of type '%1$s' cannot be mapped to messaging service '%2$s'.", CATEGORY_ERROR, 26, MessagingExceptionMessages.INVALID_OAUTH_ACCOUNT_MSG),

    /**
     * The account configuration is invalid: '%1$s'.
     */
    INVALID_ACCOUNT_CONFIGURATION("The account configuration is invalid: %1$s.", CATEGORY_USER_INPUT, 27, MessagingExceptionMessages.INVALID_OAUTH_ACCOUNT_MSG),


    ;


    private final Category category;

    private final int detailNumber;

    private final String message;

    private String displayMessage;

    private MessagingExceptionCodes(final String message, final Category category, final int detailNumber, String displayMessage) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public String getPrefix() {
        return "MESSAGING";
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
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public int getNumber() {
        return detailNumber;
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
