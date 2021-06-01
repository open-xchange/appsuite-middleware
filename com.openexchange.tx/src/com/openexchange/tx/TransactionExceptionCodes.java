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

package com.openexchange.tx;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * The error code enumeration for transaction errors.
 */
public enum TransactionExceptionCodes implements DisplayableOXExceptionCode {

    /** This transaction could not be fully undone. Some components are probably not consistent anymore. Run the recovery tool! */
    NO_COMPLETE_ROLLBACK(
        "This transaction could not be fully undone. Some components are probably not consistent anymore. Run the recovery tool.",
        OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 1),
    /** Cannot commit transaction to write DB */
    CANNOT_COMMIT("Cannot commit transaction to write DB", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_SERVICE_DOWN, 400),
    /** Cannot rollback transaction in write DB */
    CANNOT_ROLLBACK("Cannot roll back transaction in write DB", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_SERVICE_DOWN, 401),
    /** Cannot finish transaction */
    CANNOT_FINISH("Cannot finish transaction", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_SERVICE_DOWN, 402);

    private final String message;
    private final String displayMessage;
    private final Category category;
    private final int number;

    private TransactionExceptionCodes(final String message, final String displayMessage, final Category category, final int number) {
        this.message = message;
        this.displayMessage = displayMessage;
        this.category = category;
        this.number = number;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getPrefix() {
        return "TX";
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
