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

package com.openexchange.proxy;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link ProxyExceptionCodes} - Enumeration about all {@link OXException}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum ProxyExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", CATEGORY_ERROR, 1, ProxyExceptionMessages.PROXY_ERROR_MSG),
    /**
     * Duplicate proxy registration: %1$s
     */
    DUPLICATE_REGISTRATION("Duplicate proxy registration: %1$s", CATEGORY_ERROR, 2, ProxyExceptionMessages.PROXY_ERROR_MSG),
    /**
     * Malformed URL: %1$s
     */
    MALFORMED_URL("Malformed URL: %1$s", CATEGORY_ERROR, 3, ProxyExceptionMessages.PROXY_ERROR_MSG),
    /**
     * HTTP request to VoipNow server %1$s failed. Status line: %2$s
     */
    HTTP_REQUEST_FAILED("HTTP request to VoipNow server %1$s failed. Status line: %2$s", CATEGORY_ERROR, 4,
        ProxyExceptionMessages.PROXY_ERROR_MSG),
    /**
     * Malformed URI: %1$s
     */
    MALFORMED_URI("Malformed URI: %1$s", CATEGORY_ERROR, 5, ProxyExceptionMessages.PROXY_ERROR_MSG),
    /**
     * Invalid session identifier: %1$s
     */
    INVALID_SESSION_ID("Invalid session identifier: %1$s", CATEGORY_ERROR, 6, ProxyExceptionMessages.PROXY_ERROR_MSG);

    private final Category category;

    private final int detailNumber;

    private final String message;
    
    private final String displayMessage;

    private ProxyExceptionCodes(final String message, final Category category, final int detailNumber, String displayMessage) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public String getPrefix() {
        return "PROXY";
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
