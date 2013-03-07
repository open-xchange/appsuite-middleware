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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.tools.iterator;

import static com.openexchange.tools.iterator.SearchIteratorExceptionMessages.CALCULATION_ERROR_MSG;
import static com.openexchange.tools.iterator.SearchIteratorExceptionMessages.CLOSED_MSG;
import static com.openexchange.tools.iterator.SearchIteratorExceptionMessages.DBPOOLING_ERROR_MSG;
import static com.openexchange.tools.iterator.SearchIteratorExceptionMessages.INVALID_CONSTRUCTOR_ARG_MSG;
import static com.openexchange.tools.iterator.SearchIteratorExceptionMessages.NOT_IMPLEMENTED_MSG;
import static com.openexchange.tools.iterator.SearchIteratorExceptionMessages.NO_SUCH_ELEMENT_MSG;
import static com.openexchange.tools.iterator.SearchIteratorExceptionMessages.SQL_ERROR_MSG;
import static com.openexchange.tools.iterator.SearchIteratorExceptionMessages.UNEXPECTED_ERROR_MSG;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * The {@link SearchIterator} error code enumeration.
 */
public enum SearchIteratorExceptionCodes implements OXExceptionCode {

    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR(SQL_ERROR_MSG, Category.CATEGORY_ERROR, 1),
    /**
     * A DBPool error occurred: %1$s
     */
    DBPOOLING_ERROR(DBPOOLING_ERROR_MSG, Category.CATEGORY_ERROR, 2),
    /**
     * Operation not allowed on a closed SearchIterator
     */
    CLOSED(CLOSED_MSG, Category.CATEGORY_ERROR, 3),
    /**
     * Mapping for %1$d not implemented
     */
    NOT_IMPLEMENTED(NOT_IMPLEMENTED_MSG, Category.CATEGORY_ERROR, 4),

    /**
     * FreeBusyResults calculation problem with oid: %1$d
     */
    CALCULATION_ERROR(CALCULATION_ERROR_MSG, Category.CATEGORY_ERROR, 5),
    /**
     * Invalid constructor argument. Instance of %1$s not supported
     */
    INVALID_CONSTRUCTOR_ARG(INVALID_CONSTRUCTOR_ARG_MSG, Category.CATEGORY_ERROR, 6),
    /**
     * No such element.
     */
    NO_SUCH_ELEMENT(NO_SUCH_ELEMENT_MSG, Category.CATEGORY_ERROR, 7),
    /**
     * An unexpected error occurred: %1$s
     */
    UNEXPECTED_ERROR(UNEXPECTED_ERROR_MSG, Category.CATEGORY_ERROR, 8);

    private final String message;

    private final int detailNumber;

    private final Category category;

    private SearchIteratorExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    private static final String PREFIX = "FLD";

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
        return detailNumber;
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
     * Creates an {@link OXException} instance using this error code.
     *
     * @return The newly created {@link OXException} instance.
     */
    public OXException create() {
        return create(new Object[0]);
    }

    /**
     * Creates an {@link OXException} instance using this error code.
     *
     * @param logArguments The arguments for log message.
     * @return The newly created {@link OXException} instance.
     */
    public OXException create(final Object... logArguments) {
        return create(null, logArguments);
    }

    /**
     * Creates an {@link OXException} instance using this error code.
     *
     * @param cause The initial cause for {@link OXException}
     * @param arguments The arguments for message.
     * @return The newly created {@link OXException} instance.
     */
    public OXException create(final Throwable cause, final Object... arguments) {
        return OXExceptionFactory.getInstance().create(this, cause, arguments);
    }
}
