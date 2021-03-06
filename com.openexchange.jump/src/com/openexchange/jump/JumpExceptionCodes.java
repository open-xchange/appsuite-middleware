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

package com.openexchange.jump;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link JumpExceptionCodes} - Error codes for jump module.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum JumpExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(JumpExceptionCodes.UNEXPECTED_ERROR_MSG, CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(JumpExceptionCodes.IO_ERROR_MSG, CATEGORY_ERROR, 2),
    /**
     * No such end-point: %1$s
     */
    NO_SUCH_ENDPOINT(JumpExceptionCodes.NO_SUCH_ENDPOINT_MSG, CATEGORY_ERROR, 3),
    /**
     * No such end-point handler: %1$s
     */
    NO_SUCH_ENDPOINT_HANDLER(JumpExceptionCodes.NO_SUCH_ENDPOINT_HANDLER_MSG, CATEGORY_ERROR, 4),

    ;

    // An error occurred: %1$s
    private static final String UNEXPECTED_ERROR_MSG = "An error occurred: %1$s";

    // An I/O error occurred: %1$s
    private static final String IO_ERROR_MSG = "An I/O error occurred: %1$s";

    // No such end-point: %1$s
    private static final String NO_SUCH_ENDPOINT_MSG = "No such end-point: %1$s";

    // No such end-point handler: %1$s
    private static final String NO_SUCH_ENDPOINT_HANDLER_MSG = "No end-point handler for: %1$s";

    /**
     * The error code prefix for token-login module.
     */
    public static final String PREFIX = "JUMP";

    /**
     * Message of the exception.
     */
    private final String message;

    /**
     * Human readable message.
     */
    private String displayableMessage;

    /**
     * Category of the exception.
     */
    private final Category category;

    /**
     * Detail number of the exception.
     */
    private final int detailNumber;

    /**
     * Initializes a new {@link JumpExceptionCodes}.
     */
    private JumpExceptionCodes(String message, Category category, int detailNumber) {
        this(message, OXExceptionStrings.MESSAGE, category, detailNumber);
    }

    /**
     * Initializes a new {@link JumpExceptionCodes}.
     */
    private JumpExceptionCodes(String message, String displayableMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayableMessage = displayableMessage == null ? OXExceptionStrings.MESSAGE : displayableMessage;
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
        return displayableMessage;
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
