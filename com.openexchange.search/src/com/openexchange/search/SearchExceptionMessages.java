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

package com.openexchange.search;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link SearchExceptionMessages} - The error messages for search exceptions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum SearchExceptionMessages implements DisplayableOXExceptionCode {

    /**
     * Search failed: %1$s.
     */
    SEARCH_FAILED(SearchExceptionMessages.SEARCH_FAILED_MSG, CATEGORY_ERROR, 1),

    /**
     * Missing operation in JSON object.
     */
    PARSING_FAILED_MISSING_OPERATION(SearchExceptionMessages.PARSING_FAILED_MISSING_OPERATION_MSG, CATEGORY_ERROR, 2),

    /**
     * Missing operands in JSON object.
     */
    PARSING_FAILED_MISSING_OPERANDS(SearchExceptionMessages.PARSING_FAILED_MISSING_OPERANDS_MSG, CATEGORY_ERROR, 3),

    /**
     * Invalid search term in JSON object.
     */
    PARSING_FAILED_INVALID_SEARCH_TERM(SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM_MSG, CATEGORY_ERROR, 4),

    /**
     * Missing field "%1$s" in JSON object.
     */
    PARSING_FAILED_MISSING_FIELD(SearchExceptionMessages.PARSING_FAILED_MISSING_FIELD_MSG, CATEGORY_ERROR, 5),

    /**
     * Unknown operation: %1$s.
     */
    UNKNOWN_OPERATION(SearchExceptionMessages.UNKNOWN_OPERATION_MSG, CATEGORY_ERROR, 6),

    /**
     * The operand '%1$s' in the search term is not supported.
     */
    PARSING_FAILED_UNSUPPORTED_OPERAND(SearchExceptionMessages.PARSING_FAILED_UNSUPPORTED_OPERAND_MSG, CATEGORY_USER_INPUT, 7),

    ;

    // Search failed: %1$s.
    private static final String SEARCH_FAILED_MSG = "Search failed: %1$s.";

    // Missing operation in JSON object.
    private static final String PARSING_FAILED_MISSING_OPERATION_MSG = "Missing operation in JSON object.";

    // Missing operands in JSON object.
    private static final String PARSING_FAILED_MISSING_OPERANDS_MSG = "Missing operands in JSON object.";

    // Invalid search term in JSON object.
    private static final String PARSING_FAILED_INVALID_SEARCH_TERM_MSG = "Invalid search term in JSON object.";

    // Missing field "%1$s" in JSON object.
    private static final String PARSING_FAILED_MISSING_FIELD_MSG = "Missing field \"%1$s\" in JSON object.";

    // Unknown operation: %1$s.
    private static final String UNKNOWN_OPERATION_MSG = "Unknown operation: %1$s.";

    // The operand '%1$s' in the search term is not supported.
    private static final String PARSING_FAILED_UNSUPPORTED_OPERAND_MSG = "The operand '%1$s' in the search term is not supported.";

    /**
     * Message of the exception.
     */
    private final String message;

    /**
     * Category of the exception.
     */
    private final Category category;

    /**
     * Detail number of the exception.
     */
    private final int number;

    /**
     * Message displayed to the user
     */
    private String displayMessage;

    /**
     * Default constructor.
     * 
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private SearchExceptionMessages(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    /**
     * Default constructor.
     * 
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     * @param displayMessage the message to display the user.
     */
    private SearchExceptionMessages(final String message, final Category category, final int detailNumber, final String displayMessage) {
        this.message = message;
        this.category = category;
        number = detailNumber;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public String getPrefix() {
        return "SEARCH";
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayMessage() {
        return this.displayMessage;
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
