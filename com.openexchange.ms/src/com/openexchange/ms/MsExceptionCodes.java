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

package com.openexchange.ms;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link MsExceptionCodes} - Enumeration of all {@link OXException}s known in Messaging Service (MS) module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum MsExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 2),
    /**
     * No such queue or could not be created: %1$s
     */
    QUEUE_NOT_FOUND("No such queue or could not be created: %1$s", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 3),
    /**
     * No such topic or could not be created: %1$s
     */
    TOPIC_NOT_FOUND("No such topic or could not be created: %1$s", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 4),
    /**
     * Illegal state: %1$s
     */
    ILLEGAL_STATE("Illegal state: %1$s", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 5),
    /**
     * A filter expression has not been validated.
     */
    INVALID_SELECTOR("A filter expression has not been validated.", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 6),
    /**
     * Either no such queue or a topic or could not be created: %1$s
     */
    DESTINATION_NOT_FOUND("Either no such queue or a topic or could not be created: %1$s", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 7),
    /**
     * A security problem occurred: %1$s
     */
    SECURITY_ERROR("A security problem occurred: %1$s", OXExceptionStrings.MESSAGE, CATEGORY_PERMISSION_DENIED, 8),

    ;

    /**
     * The error code prefix for Messaging Service (MS) module.
     */
    public static final String MS = "MS";

    private final Category category;
    private final int detailNumber;
    private final String message;
    private final String displayMessage;

    private MsExceptionCodes(final String message, final String displayMessage, final Category category, final int detailNumber) {
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
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getPrefix() {
        return MS;
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
