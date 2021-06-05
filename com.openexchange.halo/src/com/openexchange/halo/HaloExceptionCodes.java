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

package com.openexchange.halo;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link HaloExceptionCodes} - Enumeration of all {@link OXException}s known in halo module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum HaloExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(HaloExceptionCodes.UNEXPECTED_ERROR_MSG, CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(HaloExceptionCodes.IO_ERROR_MSG, CATEGORY_ERROR, 2),
    /**
     * Unknown provider: %1$s
     */
    UNKNOWN_PROVIDER(HaloExceptionCodes.UNKNOWN_PROVIDER_MSG, CATEGORY_USER_INPUT, 3),
    /**
     * Unavailable provider: %1$s
     */
    UNAVAILABLE_PROVIDER(HaloExceptionCodes.UNAVAILABLE_PROVIDER_MSG, CATEGORY_USER_INPUT, 4),
    /**
     * Cannot search a contact that is neither an internal user nor has an e-mail address.
     */
    INVALID_CONTACT(HaloExceptionCodes.INVALID_CONTACT_MSG, HaloExceptionMessages.INVALID_CONTACT_MSG, CATEGORY_USER_INPUT, 5),

    ;

    // An error occurred: %1$s
    private static final String UNEXPECTED_ERROR_MSG = "An error occurred: %1$s";

    // An I/O error occurred: %1$s
    private static final String IO_ERROR_MSG = "An I/O error occurred: %1$s";

    // Unknown provider: %1$s
    private static final String UNKNOWN_PROVIDER_MSG = "Unknown provider: %1$s";

    // Unavailable provider: %1$s
    private static final String UNAVAILABLE_PROVIDER_MSG = "Unavailable provider: %1$s";

    // Cannot search a contact that is neither an internal user nor has an e-mail address.
    private static final String INVALID_CONTACT_MSG = "Cannot search a contact that is neither an internal user nor has an e-mail address.";

    /**
     * The error code prefix for halo module.
     */
    public static final String PREFIX = "HALO";

    private final Category category;

    private final int detailNumber;

    private final String message;
    
    private String displayMessage;

    private HaloExceptionCodes(final String message, final Category category, final int detailNumber) {
        this(message, OXExceptionStrings.MESSAGE, category, detailNumber);
    }
    
    private HaloExceptionCodes(final String message, String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
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
    public String getDisplayMessage() {
        return displayMessage;
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
