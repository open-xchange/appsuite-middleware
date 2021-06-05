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

package com.openexchange.oauth.provider.impl.jwt;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link OAuthJWTExceptionCode} - defines exception codes for {@link OAuthJWTExceptionCode}
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since v7.10.5
 */
public enum OAuthJWTExceptionCode implements OXExceptionCode {
    /**
     * Service configuration failed because of internal errors: '%1$s'
     */
    SERVICE_CONFIGURATION_FAILED("Oauth JWT service configuration failed because of internal errors: '%1$s'", Category.CATEGORY_ERROR, 4),

    /**
     * Unable to parse JWT claim: '%1$s'
     */
    UNABLE_TO_PARSE_CLAIM("Unable to parse claim: '%1$s'", Category.CATEGORY_ERROR, 5);

    private final String message;
    private final String displayMessage;
    private final int detailNumber;
    private final Category category;

    /**
     * Initializes a new {@link OAuthJWTExceptionCode}.
     *
     * @param message
     * @param category
     * @param detailNumber
     */
    private OAuthJWTExceptionCode(final String message, final Category category, final int detailNumber) {
        this(message, null, category, detailNumber);
    }

    /**
     * Initializes a new {@link OAuthJWTExceptionCode}.
     *
     * @param message
     * @param displayMessage
     * @param category
     * @param detailNumber
     */
    private OAuthJWTExceptionCode(final String message, final String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public int getNumber() {
        return this.detailNumber;
    }

    @Override
    public Category getCategory() {
        return this.category;
    }

    @Override
    public String getPrefix() {
        return "OAUTH_JWT";
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public String getDisplaymessage() {
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
