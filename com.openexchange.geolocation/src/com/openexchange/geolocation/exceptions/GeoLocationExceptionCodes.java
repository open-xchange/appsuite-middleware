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

package com.openexchange.geolocation.exceptions;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link GeoLocationExceptionCodes}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum GeoLocationExceptionCodes implements DisplayableOXExceptionCode {
    /**
     * An unexpected error occurred: %1$s
     */
    UNEXPECTED_ERROR("An unexpected error occurred: %1$s", CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", CATEGORY_ERROR, 2),
    /**
     * The specified address '%1$s' was not found in the Geo Database
     */
    ADDRESS_NOT_FOUND("The specified address '%1$s' was not found in the Geo Database", CATEGORY_ERROR, 3),
    /**
     * The specified host '%1$s' cannot be resolved to an IP address
     */
    UNABLE_TO_RESOLVE_HOST("The specified host '%1$s' cannot be resolved to an IP address", CATEGORY_ERROR, 4),
    /**
     * Storage service provider with id '%1$s' is not registered.
     */
    UNKNOWN_STORAGE_SERVICE_PROVIDER("Storage service provider with id '%1$s' is not registered.", CATEGORY_CONFIGURATION, 5),
    /**
     * The property '%1$s' is empty! No geo location storage service provider is registered therefore none was selected for context with id '%2$s'
     */
    STORAGE_SERVICE_PROVIDER_NOT_CONFIGURED_FOR_CONTEXT("The property '%1$s' is empty! No geo location storage service provider is registered therefore none was selected for context with id '%2$s'", CATEGORY_CONFIGURATION, 6),
    /**
     * The property '%1$s' is empty! No geo location storage service provider is registered.
     */
    STORAGE_SERVICE_PROVIDER_NOT_CONFIGURED("The property '%1$s' is empty! No geo location storage service provider is registered.", CATEGORY_CONFIGURATION, 6),
    /**
     * The IPv6 address '%1$s' is not convertible to IPv4.
     */
    UNABLE_TO_CONVERT_TO_IPV4("The IPv6 address '%1$s' is not convertible to IPv4", CATEGORY_ERROR, 7);
    ;

    private final int number;
    private final Category category;
    public static final String PREFIX = "GLS";
    private final String message;
    private final String displayMessage;

    /**
     * Initialises a new {@link GeoLocationExceptionCodes}.
     */
    private GeoLocationExceptionCodes(final String message, final Category category, final int number) {
        this.message = message;
        this.displayMessage = message;
        this.category = category;
        this.number = number;
    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public int getNumber() {
        return number;
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

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }
}
