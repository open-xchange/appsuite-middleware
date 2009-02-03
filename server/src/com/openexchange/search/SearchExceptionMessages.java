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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * {@link SearchExceptionMessages} - The error messages for search exceptions.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum SearchExceptionMessages implements OXErrorMessage {

    /**
     * Search failed: %1$s.
     */
    SEARCH_FAILED(SearchExceptionStrings.SEARCH_FAILED_MSG, Category.CODE_ERROR, 1),
    /**
     * Missing operation in JSON object.
     */
    PARSING_FAILED_MISSING_OPERATION(SearchExceptionStrings.PARSING_FAILED_MISSING_OPERATION_MSG, Category.CODE_ERROR, 2),
    /**
     * Missing operands in JSON object.
     */
    PARSING_FAILED_MISSING_OPERANDS(SearchExceptionStrings.PARSING_FAILED_MISSING_OPERANDS_MSG, Category.CODE_ERROR, 3),
    /**
     * Invalid search term in JSON object.
     */
    PARSING_FAILED_INVALID_SEARCH_TERM(SearchExceptionStrings.PARSING_FAILED_INVALID_SEARCH_TERM_MSG, Category.CODE_ERROR, 4),
    /**
     * Missing field "%1$s" in JSON object.
     */
    PARSING_FAILED_MISSING_FIELD(SearchExceptionStrings.PARSING_FAILED_MISSING_FIELD_MSG, Category.CODE_ERROR, 5),
    /**
     * Unknown operation: %1$s.
     */
    UNKNOWN_OPERATION(SearchExceptionStrings.UNKNOWN_OPERATION_MSG, Category.CODE_ERROR, 6);

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
     * Default constructor.
     * 
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private SearchExceptionMessages(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        number = detailNumber;
    }

    public Category getCategory() {
        return category;
    }

    public int getErrorCode() {
        return number;
    }

    public String getHelp() {
        return null;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Creates a new search exception instance with specified message arguments.
     * 
     * @param messageArgs The message arguments.
     * @return A new search exception instance with specified message arguments.
     */
    public SearchException create(final Object... messageArgs) {
        return SearchExceptionFactory.getInstance().create(this, messageArgs);
    }

    /**
     * Creates a new search exception instance with specified message arguments. <br>
     * Exception's init cause is set to provided {@link Throwable} instance.
     * 
     * @param cause The init cause.
     * @param messageArgs The message arguments.
     * @return A new search exception instance with specified message arguments and init cause.
     */
    public SearchException create(final Throwable cause, final Object... messageArgs) {
        return SearchExceptionFactory.getInstance().create(this, cause, messageArgs);
    }
}
