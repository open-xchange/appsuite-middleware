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

package com.openexchange.find;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public enum FindExceptionCode implements DisplayableOXExceptionCode {

    /**
     * Did not find a search driver in module %1$s for user %2$d in context %3$d.
     */
    MISSING_DRIVER("Did not find a search driver in module %1$s for user %2$d in context %3$d.", Category.CATEGORY_TRY_AGAIN, 1, FindExceptionMessages.SERVICE_NOT_AVAILABLE),
    /**
     * No visible/readable folder found in module %1$s for user %2$d in context %3$d.
     */
    NO_READABLE_FOLDER("No visible/readable folder found in module %1$s for user %2$d in context %3$d.", Category.CATEGORY_TRY_AGAIN, 2, FindExceptionMessages.SERVICE_NOT_AVAILABLE),
    /**
     * The filter field \"%1$s\" is not supported.
     */
    UNSUPPORTED_FILTER_FIELD("The filter field \"%1$s\" is not supported.", Category.CATEGORY_ERROR, 3),
    /**
     * The filter query \"%1$s\" is not supported for field \"%2$s\".
     */
    UNSUPPORTED_FILTER_QUERY("The filter query \"%1$s\" is not supported for field \"%2$s\".", Category.CATEGORY_ERROR, 4),
    /**
     * A filter for field \"%1$s\" is missing but is required to search in module %2$s.
     */
    MISSING_SEARCH_FILTER(FindExceptionMessages.MISSING_SEARCH_FILTER, Category.CATEGORY_USER_INPUT, 5, FindExceptionMessages.MISSING_SEARCH_FILTER),
    /**
     * A search filter did not contain a field to filter on: \"%1$s\".
     */
    INVALID_FILTER_NO_FIELDS("A search filter did not contain a field to filter on: '%1$s'.", Category.CATEGORY_USER_INPUT, 6, FindExceptionMessages.INVALID_FILTER_NO_FIELDS),
    /**
     * A search filter did not contain a query to search for: \"%1$s\".
     */
    INVALID_FILTER_NO_QUERIES("A search filter did not contain a query to search for: '%1$s'.", Category.CATEGORY_USER_INPUT, 7, FindExceptionMessages.INVALID_FILTER_NO_QUERIES),
    /**
     * The facet \"%1$s\" is not supported by module \"%2$s\".
     */
    UNSUPPORTED_FACET("The facet \"%1$s\" is not supported by module \"%2$s\".", Category.CATEGORY_ERROR, 8),
    /**
     * Null field on filter
     */
    NULL_FIELD("A search filter contains a 'null' field: '%1$s'.", Category.CATEGORY_ERROR, 9),
    /**
     * In order to accomplish the search, %1$d or more characters are required.
     */
    QUERY_TOO_SHORT(FindExceptionMessages.QUERY_TOO_SHORT, Category.CATEGORY_USER_INPUT, 10, FindExceptionMessages.QUERY_TOO_SHORT),
    /**
     * The facet \"%1$s\" is mandatory and has to be set.
     */
    MISSING_MANDATORY_FACET(FindExceptionMessages.MISSING_MANDATORY_FACET, Category.CATEGORY_USER_INPUT, 11, FindExceptionMessages.MISSING_MANDATORY_FACET),
    /**
     * Could not parse \"%1$s\".
     */
    PARSING_ERROR(FindExceptionMessages.PARSING_ERROR, Category.CATEGORY_USER_INPUT, 12, FindExceptionMessages.PARSING_ERROR),
    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", CATEGORY_ERROR, 13, OXExceptionStrings.MESSAGE),
    /**
     * The folder id \"%1$s\" is invalid for module \"%2$s\".
     */
    INVALID_FOLDER_ID(FindExceptionMessages.INVALID_FOLDER_ID, Category.CATEGORY_USER_INPUT, 14, FindExceptionMessages.INVALID_FOLDER_ID),
    /**
     * The folder type \"%1$s\" is not supported.
     */
    INVALID_FOLDER_TYPE(FindExceptionMessages.INVALID_FOLDER_TYPE, Category.CATEGORY_USER_INPUT, 15, FindExceptionMessages.INVALID_FOLDER_TYPE),
    /**
     * The facet types \"%1$s\" and \"%2$s\" conflict with each other and must not be used within one request.
     */
    FACET_CONFLICT(FindExceptionMessages.FACET_CONFLICT, Category.CATEGORY_USER_INPUT, 16, FindExceptionMessages.FACET_CONFLICT),
    /**
     * Searching in module \"%1$s\" is disabled for user %2$d in context %3$d.
     */
    MODULE_DISABLED("Searching in module \"%1$s\" is disabled for user %2$d in context %3$d.", Category.CATEGORY_PERMISSION_DENIED, 17, FindExceptionMessages.MODULE_DISABLED),

    INVALID_ACCOUNT_ID("The account id \"%1$s\" is invalid for module \"%2$s\".", Category.CATEGORY_USER_INPUT, 18),
    /**
     * The query yielded too many results to be displayed. Please refine your search options and try again.
     */
    TOO_MANY_RESULTS(FindExceptionMessages.TOO_MANY_RESULTS, Category.CATEGORY_USER_INPUT, 19, FindExceptionMessages.TOO_MANY_RESULTS),

    ;

    public static final String PREFIX = "FIND";

    private final String message;

    private final String displayMessage;

    private final int number;

    private final Category category;

    private FindExceptionCode(final String message, final Category category, final int number) {
        this(message, category, number, null);
    }

    private FindExceptionCode(final String message, final Category category, final int number, final String displayMessage) {
        this.message = message;
        this.number = number;
        this.category = category;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
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
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public int getNumber() {
        return number;
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

}
