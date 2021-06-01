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

package com.openexchange.database;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * Generic error codes for the database exception.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum DatabaseExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An SQL error occurred: %1$s
     */
    SQL_ERROR("An SQL error occurred: %1$s", DatabaseExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 1),
    /**
     * An SQL error caused by an illegal or unsupported character string: %1$s
     */
    STRING_LITERAL_ERROR("An SQL error caused by an illegal or unsupported character string: %1$s", DatabaseExceptionStrings.STRING_LITERAL_ERROR_MSG, Category.CATEGORY_ERROR, 2),
    /**
     * The keystore could not be (re-)loaded: %1$s
     * <p>
     * Note: The display message must <b>not</b> be changed. Leaking information about keystore problems is of no interest for the user
     */
    KEYSTORE_UNAVAILABLE("The keystore could not be (re-)loaded: %1$s", DatabaseExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 3),
    /**
     * The given file path \"%1$s\" is invalid.
     * <p>
     * Note: The display message must <b>not</b> be changed. Leaking information about keystore problems is of no interest for the user
     */
    KEYSTORE_FILE_ERROR("The given file path \"%1$s\" is invalid.", DatabaseExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 4),

    ;

    private static final String PREFIX = "RDB";

    /**
     * Gets the <code>"RDB"</code> prefix for this error code class.
     *
     * @return The prefix
     */
    public static String prefix() {
        return PREFIX;
    }

    private final Category category;
    private final int number;
    private final String message;
    private final String displayMessage;

    private DatabaseExceptionCodes(String message, String displayMessage, Category category, int detailNumber) {
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
