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

package com.openexchange.api.client;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link ApiClientExceptions}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public enum ApiClientExceptions implements DisplayableOXExceptionCode {

    /** Unable to access the link %1$s */
    NO_ACCESS("Unable to access the link %1$s", Category.CATEGORY_ERROR, 1, ApiClientExceptionMessages.NO_ACCESS_MSG),

    /**
     * <li>Credentials to access the link are missing.</li>
     * <li>{@value ApiClientExceptionMessages#MISSING_CREDENTIALS_MSG}.</li>
     */
    MISSING_CREDENTIALS("Credentials to access the link are missing.", Category.CATEGORY_ERROR, 2, ApiClientExceptionMessages.MISSING_CREDENTIALS_MSG),

    /** An I/O error occurred: %1$s */
    IO_ERROR("An I/O error occurred: %1$s", CATEGORY_ERROR, 3),

    /** The link %1$s is invalid */
    INVALID_TARGET("The link \"%1$s\" is invalid.", Category.CATEGORY_ERROR, 4, OXExceptionStrings.BAD_REQUEST),

    /** The targeted host %1$s is not covered by the same origin policy for %2$s . */
    NOT_SAME_ORIGIN("The targeted host %1$s is not covered by the same origin policy for %2$s.", Category.CATEGORY_ERROR, 6),

    /** The requested URL doesn't contain characteristics that identifies a certain API module. Can't communicate with unknown API module. */
    UNKOWN_API("The requested URL doesn't contain characteristics that identifies a certain API module. Can't communicate with unknown API module.", Category.CATEGORY_ERROR, 7),

    /** Unexpected error [%1$s] */
    UNEXPECTED_ERROR("Unexpected error [%1$s]", Category.CATEGORY_ERROR, 8),

    /** JSON error: [%1$s] */
    JSON_ERROR("JSON error: %1$s", Category.CATEGORY_ERROR, 9),

    /** A parameter for the request is missing. */
    MISSING_PARAMETER("A parameter for the request is missing.", Category.CATEGORY_ERROR, 10, OXExceptionStrings.BAD_REQUEST),

    /**
     * <li>The access to the share was revoked</li>
     * <li>{@value ApiClientExceptionMessages#ACCESS_REVOKED_MSG}.</li>
     */
    ACCESS_REVOKED("The access to the share was revoked", Category.CATEGORY_ERROR, 11, ApiClientExceptionMessages.ACCESS_REVOKED_MSG),

    /** The remote server responded with a client error: %1$s. */
    CLIENT_ERROR("The remote server responded with a client error %1$s.", Category.CATEGORY_USER_INPUT, 400),

    /** The remote server responded with a server error: %1$s. */
    REMOTE_SERVER_ERROR("The remote server responded with a server error %1$s.", Category.CATEGORY_SERVICE_DOWN, 500, OXExceptionStrings.MESSAGE_RETRY),

    /** The remote session is expired **/
    SESSION_EXPIRED("The remote session expired", Category.CATEGORY_TRY_AGAIN, 600, OXExceptionStrings.MESSAGE_RETRY),

    ;

    /**
     * The error code prefix for password-change module.
     */
    public static final String PREFIX = "ACE";

    private final Category category;

    private final int detailNumber;

    private final String message;

    /**
     * Message displayed to the user
     */
    private String displayMessage;

    /**
     * Initializes a new {@link ApiClientExceptions}.
     *
     * @param message The message
     * @param category The category
     * @param detailNumber The exception number
     */
    private ApiClientExceptions(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    /**
     * Initializes a new {@link ApiClientExceptions}.
     *
     * @param message The message
     * @param category The category
     * @param detailNumber The exception number
     * @param displayMessage The display message to send to the client
     */
    private ApiClientExceptions(final String message, final Category category, final int detailNumber, final String displayMessage) {
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
