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

package com.openexchange.snippet;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link SnippetExceptionCodes} - Enumeration of all {@link OXException}s known in snippet module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum SnippetExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", CATEGORY_ERROR, 2),
    /**
     * No such snippet found for identifier: %1$s
     */
    SNIPPET_NOT_FOUND("No such snippet found for identifier: %1$s", CATEGORY_ERROR, 3),
    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR("A JSON error occurred: %1$s", CATEGORY_ERROR, 4),
    /**
     * Illegal state: %1$s
     */
    ILLEGAL_STATE("Illegal state: %1$s", CATEGORY_ERROR, 5),
    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR("A SQL error occurred: %1$s", CATEGORY_ERROR, 6),
    /**
     * No such snippet attachment found for identifier %1$s in snippet %2$s
     */
    ATTACHMENT_NOT_FOUND("No such snippet attachment found for identifier %1$s in snippet %2$s", CATEGORY_ERROR, 7),
    /**
     * Maximum number of '%1$s' for signature images reached.
     */
    MAXIMUM_IMAGES_COUNT("Maximum number of '%1$s' for signature images reached.", SnippetStrings.MAXIMUM_IMAGES_COUNT_MSG, CATEGORY_ERROR, 8),
    /**
     * The signature image exceeds the maximum allowed size of '%1$s' (%2$s bytes).
     */
    MAXIMUM_IMAGE_SIZE("The signature image exceeds the maximum allowed size of '%1$s' (%2$s bytes).", SnippetStrings.MAXIMUM_IMAGE_SIZE_MSG, CATEGORY_ERROR, 9),
    /**
     * Invalid or harmful image data detected.
     */
    INVALID_IMAGE_DATA("Invalid or harmful image data detected.", SnippetStrings.INVALID_IMAGE_DATA_MSG, CATEGORY_ERROR, 10),
    /**
     * Snippet %1$s must not be changed by user %2$s in context %3$s
     */
    UPDATE_DENIED("Snippet %1$s must not be changed by user %2$s in context %3$s", SnippetStrings.UPDATE_DENIED_MSG, CATEGORY_ERROR, 11),
    /**
     * The signature size exceeds the maximum allowed size of '%1$s' (%2$s bytes).
     */
    MAXIMUM_SNIPPET_SIZE("The signature size exceeds the maximum allowed size of '%1$s' (%2$s bytes).", SnippetStrings.MAXIMUM_SNIPPET_SIZE_MSG, CATEGORY_ERROR, 12),
    /**
     * Unable to process signature: the signature raw size exceeds the maximum allowed size of '%1$s' (%2$s bytes).
     */
    MAXIMUM_RAW_SNIPPET_SIZE("Unable to process signature: the signature raw size exceeds the maximum allowed size of '%1$s' (%2$s bytes).", CATEGORY_ERROR, 13),
    /**
     * The entered display name is too long. Please use a shorter one.
     */
    DISPLAY_NAME_TOO_LONG("The entered display name is too long. Please use a shorter one.", SnippetStrings.DISPLAY_NAME_TOO_LONG_MSG, CATEGORY_USER_INPUT, 14),
    /**
     * The entered identifier is too long. Please use a shorter one.
     */
    ID_TOO_LONG("The entered identifier is too long. Please use a shorter one.", SnippetStrings.ID_TOO_LONG_MSG, CATEGORY_USER_INPUT, 15),
    /**
     * The entered module identifier is too long. Please use a shorter one.
     */
    MODULE_TOO_LONG("The entered module identifier is too long. Please use a shorter one.", SnippetStrings.MODULE_TOO_LONG_MSG, CATEGORY_USER_INPUT, 16),
    /**
     * The entered type identifier is too long. Please use a shorter one.
     */
    TYPE_TOO_LONG("The entered type identifier is too long. Please use a shorter one.", SnippetStrings.TYPE_TOO_LONG_MSG, CATEGORY_USER_INPUT, 17),
    /**
     * One or more input fields specified for snippet are too long
     */
    DATA_TRUNCATION_ERROR("One or more input fields specified for snippet are too long", CATEGORY_ERROR, 18),
    ;

    /**
     * The error code prefix for snippet module.
     */
    public static String PREFIX = "SNIPPET";

    private Category category;

    private int detailNumber;

    private String message;

    private String displayMessage;

    private SnippetExceptionCodes(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    private SnippetExceptionCodes(String message, Category category, int detailNumber) {
        this(message, null, category, detailNumber);
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
    public boolean equals(OXException e) {
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
