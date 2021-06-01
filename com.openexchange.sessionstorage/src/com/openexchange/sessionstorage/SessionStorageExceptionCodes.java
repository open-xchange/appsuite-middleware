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

package com.openexchange.sessionstorage;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link SessionStorageExceptionCodes} - Error codes for <b><code>com.openexchange.sessionstorage</code></b>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum SessionStorageExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", 1, CATEGORY_ERROR),
    /**
     * No session found for identifier: %1$s
     */
    NO_SESSION_FOUND("No session found for identifier: %1$s", SessionStorageExceptionMessages.NO_SESSION_FOUND, 2, CATEGORY_USER_INPUT),
    /**
     * Saving session with session identifier %1$s failed.
     */
    SAVE_FAILED("Saving session with session identifier %1$s failed.", 3, Category.CATEGORY_ERROR),
    /**
     * Lookup for session with session identifier %1$s failed.
     */
    LOOKUP_FAILED("Lookup for session with session identifier %1$s failed.", 4, Category.CATEGORY_ERROR),
    /**
     * Removing session with session identifier %1$s failed.
     */
    REMOVE_FAILED("Removing session with session identifier %1$s failed.", 5, Category.CATEGORY_ERROR),
    /**
     * Authentication identifier duplicate found. Existing session login: %1$s. Current denied login request: %2$s.
     */
    DUPLICATE_AUTHID("Authentication identifier duplicate found. Existing session login: %1$s. Current denied login request: %2$s.", 6,
        Category.CATEGORY_ERROR),
    /**
     * Operation %1$s not supported.
     */
    UNSUPPORTED_OPERATION("Operation %1$s not supported.", 7, Category.CATEGORY_ERROR),
    /**
     * Lookup for session with alternative identifier %1$s failed.
     */
    ALTID_NOT_FOUND("Lookup for session with alternative identifier %1$s failed.", 8, Category.CATEGORY_ERROR),
    /**
     * No sessions found for user %1$s in context %2$s.
     */
    NO_USERSESSIONS("No sessions found for user %1$s in context %2$s.", 9, Category.CATEGORY_ERROR),
    /**
     * No sessions found for context %1$s.
     */
    NO_CONTEXTESSIONS("No sessions found for context %1$s.", 10, Category.CATEGORY_ERROR),
    /**
     * No sessions found by random token %1$s,
     */
    RANDOM_NOT_FOUND("No sessions found by random token %1$s.", 11, Category.CATEGORY_ERROR),
    /**
     * The session storage service %1$s is down.
     */
    SESSION_STORAGE_DOWN("The session storage service %1$s is down.", 12, Category.CATEGORY_ERROR),
    /**
     * The operation for session storage service has been interrupted.
     */
    INTERRUPTED("The operation for session storage service has been interrupted.", 13, Category.CATEGORY_ERROR),

    ;

    private static String PREFIX = "SST";

    /**
     * Message of the exception.
     */
    private String message;

    /**
     * Display message of the exception.
     */
    private String displayMessage;

    /**
     * Category of the exception.
     */
    private Category category;

    /**
     * Detail number of the exception.
     */
    private int detailNumber;

    /**
     * Initializes a new {@link SessionStorageExceptionCodes}.
     */
    private SessionStorageExceptionCodes(String message, String displayMessage, int detailNumber, Category category) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    private SessionStorageExceptionCodes(String message, int detailNumber, Category category) {
        this(message, null, detailNumber, category);
    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public int getNumber() {
        return detailNumber;
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
    public OXException create(Object... args) {
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
