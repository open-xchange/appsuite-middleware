/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
    UNKNOWN_OPERATION(SearchExceptionMessages.UNKNOWN_OPERATION_MSG, CATEGORY_ERROR, 6);

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
