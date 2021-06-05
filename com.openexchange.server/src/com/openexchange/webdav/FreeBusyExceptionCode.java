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

package com.openexchange.webdav;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.groupware.EnumComponent;

/**
 * {@link FreeBusyExceptionCode}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.4
 */
public enum FreeBusyExceptionCode implements OXExceptionCode {

    /**
     * Parameter \"%1$s\" is invalid: %2$s.
     */
    INVALID_PARAMETER("Parameter \"%1$s\" is invalid: %2$s.", CATEGORY_ERROR, 1),
    /**
     * Missing parameter %1$s.
     */
    MISSING_PARAMETER("Missing parameter %1$s.", CATEGORY_ERROR, 2),
    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR("Unexpected error: %1$s", CATEGORY_ERROR, 3),
    /**
     * Unable to find user %1$s in context %2$s.
     */
    USER_NOT_FOUND("Unable to find user %1$s in context %2$s.", CATEGORY_USER_INPUT, 4);

    private final Category category;
    private final int code;
    private final String message;

    /**
     * Initializes a new {@link FreeBusyExceptionCode}.
     *
     * @param message The error message
     * @param category The {@link Category}
     * @param code The exception code
     */
    private FreeBusyExceptionCode(final String message, final Category category, final int code) {
        this.message = message;
        this.code = code;
        this.category = category;
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

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
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
        return code;
    }

    @Override
    public String getPrefix() {
        return EnumComponent.FREEBUSY.getAbbreviation();
    }

}
