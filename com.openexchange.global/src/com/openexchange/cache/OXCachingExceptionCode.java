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

package com.openexchange.cache;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * Error codes for the caching exception
 */
public enum OXCachingExceptionCode implements DisplayableOXExceptionCode {
    /**
     * A put into the cache failed.
     */
    FAILED_PUT("A put into the cache failed.", CATEGORY_ERROR, 1),
    /**
     * The default element attributes could not be retrieved
     */
    FAILED_ATTRIBUTE_RETRIEVAL("The default element attributes could not be retrieved", CATEGORY_ERROR, 2),
    /**
     * Remove on cache failed
     */
    FAILED_REMOVE("Remove on cache failed", CATEGORY_ERROR, 3),
    /**
     * Cache %s could not be initialized due to following error: %s
     */
    FAILED_INIT("Cache %s could not be initialized due to following error: %s", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 4);

    /**
     * Message of the exception.
     */
    final String message;

    /**
     * Category of the exception.
     */
    final Category category;

    /**
     * Detail number of the exception.
     */
    final int detailNumber;
    
    /**
     * Message displayed to the user
     */
    private final String displayMessage;

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private OXCachingExceptionCode(final String message, final Category category, final int detailNumber) {
        this(message, null, category, detailNumber);
    }
    
    private OXCachingExceptionCode(final String message, final String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
        this.displayMessage = (displayMessage == null) ? OXExceptionStrings.MESSAGE_RETRY : displayMessage;
    }

    @Override
    public String getPrefix() {
        return "CAC";
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return detailNumber;
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

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }
}
