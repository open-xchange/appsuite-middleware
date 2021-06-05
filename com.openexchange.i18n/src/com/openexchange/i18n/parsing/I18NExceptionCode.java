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

package com.openexchange.i18n.parsing;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public enum I18NExceptionCode implements DisplayableOXExceptionCode {

    UNEXPECTED_TOKEN(101, "Unexpected token %s in .po file %s:%s. Expected one of %s", CATEGORY_CONFIGURATION),
    UNEXPECTED_TOKEN_CONSUME(102, "Unexpected token %s in .po file %s:%s. Expected one of %s", CATEGORY_CONFIGURATION),
    EXPECTED_NUMBER(103, "Got %s, but expected a number in .po file %s:%s.", CATEGORY_CONFIGURATION),
    MALFORMED_TOKEN(104, "Malformed or unsupported token. Got %s but expected %s in .po file %s:%s.", CATEGORY_CONFIGURATION),
    IO_EXCEPTION(105, "An I/O error occurred reading .po file %s.", CATEGORY_CONFIGURATION);

    private Category category;

    private String message;

    private int errorCode;

    private I18NExceptionCode(final int errorCode, final String message, final Category category) {
        this.category = category;
        this.message = message;
        this.errorCode = errorCode;
    }

    @Override
    public String getPrefix() {
        return "I18N";
    }

    @Override
    public int getNumber() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
    
    @Override
    public String getDisplayMessage() {
        return OXExceptionStrings.MESSAGE;
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
