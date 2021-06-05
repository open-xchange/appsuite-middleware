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

package com.openexchange.net.ssl.management.exception;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link SSLCertificateManagementExceptionCode}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum SSLCertificateManagementExceptionCode implements DisplayableOXExceptionCode {
    /**
     * The SSL certificate with fingerprint '%1$s' was not found in cache
     */
    NOT_CACHED("The SSL certificate with fingerprint '%1$s' was not found in cache", OXExceptionStrings.MESSAGE_NOT_FOUND, CATEGORY_ERROR, 1),
    /**
     * An unexpected error occurred: %1$s
     */
    UNEXPECTED_ERROR("An unexpected error occurred: %1$s", OXExceptionStrings.DEFAULT_MESSAGE, CATEGORY_ERROR, 2),
    /**
     * Unexpected database error: %1$s
     */
    SQL_PROBLEM("Unexpected database error: %1$s", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 3),
    /**
     * The SSL certificate with fingerprint '%1$s' was not found for user '%2$s' in context '%3$s'
     */
    CERTIFICATE_NOT_FOUND("The SSL certificate with fingerprint '%1$s' was not found for user '%2$s' in context '%3$s'", SSLCertificateManagementExceptionMessages.CERTIFICATE_NOT_FOUND, Category.CATEGORY_ERROR, 4),
    ;

    public static final String PREFIX = "SSL-CERT-MGMT";

    private final Category category;
    private final int detailNumber;
    private final String message;
    private final String displayMessage;

    /**
     * Initialises a new {@link SSLCertificateManagementExceptionCode}.
     */
    private SSLCertificateManagementExceptionCode(final String message, String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage;
        this.detailNumber = detailNumber;
        this.category = category;
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
