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

package com.openexchange.pns;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link PushExceptionCodes} - The error codes for push notification service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public enum PushExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR("Unexpected error: %1$s", null, Category.CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", null, Category.CATEGORY_ERROR, 2),
    /**
     * An SQL error occurred: %1$s
     */
    SQL_ERROR("An SQL error occurred: %1$s", null, Category.CATEGORY_ERROR, 3),
    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR("A JSON error occurred: %1$s", null, Category.CATEGORY_ERROR, 4),
    /**
     * No such transport: %1$s
     */
    NO_SUCH_TRANSPORT("No such transport: %1$s", null, Category.CATEGORY_ERROR, 5),
    /**
     * Unsupported transport: %1$s
     */
    UNSUPPORTED_TRANSPORT("Unsupported transport: %1$s", null, Category.CATEGORY_ERROR, 6),
    /**
     * Invalid topic: %1$s
     */
    INVALID_TOPIC("Invalid topic: %1$s", PushExceptionMessages.INVALID_TOPIC_MSG, Category.CATEGORY_USER_INPUT, 7),
    /**
     * No such generator for client: %1$s
     */
    NO_SUCH_GENERATOR("No such generator for client: %1$s", null, Category.CATEGORY_ERROR, 8),
    /**
     * Unsupported message class: %1$s
     */
    UNSUPPORTED_MESSAGE_CLASS("Unsupported message class: %1$s", null, Category.CATEGORY_ERROR, 9),
    /**
     * The message is too big. Allowed is %1$s, but is %2$s.
     */
    MESSAGE_TOO_BIG("The message is too big. Allowed is %1$s, but is %2$s.", null, Category.CATEGORY_ERROR, 10),
    /**
     * Message generation failed: %1$s
     */
    MESSAGE_GENERATION_FAILED("Message generation failed: %1$s", null, Category.CATEGORY_ERROR, 11),

    ;

    /** The error code prefix for push notification service */
    public static final String PREFIX = "PNS";

    private final Category category;
    private final int number;
    private final String message;
    private final String displayMessage;

    private PushExceptionCodes(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
        this.number = detailNumber;
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
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
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
    public OXException create(Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }

}
