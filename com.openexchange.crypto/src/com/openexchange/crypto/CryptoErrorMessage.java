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

package com.openexchange.crypto;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public enum CryptoErrorMessage implements DisplayableOXExceptionCode {
    /**
     * Bad password.
     */
    BadPassword(CATEGORY_USER_INPUT, 1, "Wrong Password.", CryptoExceptionMessage.BAD_PASSWORD_DISPLAY),
    /**
     * Encoding error.
     */
    EncodingException(CATEGORY_ERROR, 2, "Error during encoding operation."),
    /**
     * Security Exception.
     */
    SecurityException(CATEGORY_ERROR, 3, "General Security Exception occurred."),
    /**
     * Arbitrary byte sequence is missing to generate a secure key.
     */
    NoSalt(CATEGORY_USER_INPUT, 4, "Arbitrary byte sequence is missing to generate a secure key."),
    /**
     * Empty password.
     */
    EmptyPassword(CATEGORY_CONFIGURATION, 5, "The passed password is empty."),
    ;

    private static final String PREFIX = "CRP";

    /**
     * Gets the prefix.
     *
     * @return The prefix
     */
    public static String getPrefixx() {
        return PREFIX;
    }

    private final Category category;
    private final int errorCode;
    private final String message;
    private final String displayMessage;

    private CryptoErrorMessage(Category category, int errorCode, String message) {
        this(category, errorCode, message, OXExceptionStrings.MESSAGE);
    }

    private CryptoErrorMessage(final Category category, final int errorCode, final String message, String displayMessage) {
        this.category = category;
        this.errorCode = errorCode;
        this.message = message;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
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
    public int getNumber() {
        return errorCode;
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
