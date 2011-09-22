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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link SearchIteratorException} - The xception for {@link SearchIterator}.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class SearchIteratorException extends OXException {

    private static final long serialVersionUID = -4303608920163984898L;

    private SearchIteratorException() {
        super();
    }

    public static enum Code implements OXExceptionCode {

        /**
         * A SQL error occurred: %1$s
         */
        SQL_ERROR("A SQL error occurred: %1$s", Category.CATEGORY_ERROR, 1),
        /**
         * A DBPool error occurred: %1$s
         */
        DBPOOLING_ERROR("A DBPool error occurred: 1$%s", Category.CATEGORY_ERROR, 2),
        /**
         * Operation not allowed on a closed SearchIterator
         */
        CLOSED("Operation not allowed on a closed SearchIterator", Category.CATEGORY_ERROR, 3),
        /**
         * Mapping for %1$d not implemented
         */
        NOT_IMPLEMENTED("Mapping for %1$d not implemented", Category.CATEGORY_ERROR, 4),

        /**
         * FreeBusyResults calculation problem with oid: %1$d
         */
        CALCULATION_ERROR("FreeBusyResults calculation problem with oid: %1$d", Category.CATEGORY_ERROR, 5),
        /**
         * Invalid constructor argument. Instance of %1$s not supported
         */
        INVALID_CONSTRUCTOR_ARG("Invalid constructor argument. Instance of %1$s not supported", Category.CATEGORY_ERROR, 6),
        /**
         * No such element.
         */
        NO_SUCH_ELEMENT("No such element.", Category.CATEGORY_ERROR, 7),
        /**
         * An unexpected error occurred: %1$s
         */
        UNEXPECTED_ERROR("An unexpected error occurred: %1$s", Category.CATEGORY_ERROR, 8);

        private final String message;

        private final int detailNumber;

        private final Category category;

        private final boolean display;

        private Code(final String message, final Category category, final int detailNumber) {
            this.message = message;
            this.category = category;
            this.detailNumber = detailNumber;
            display = category.getLogLevel().implies(LogLevel.DEBUG);
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
        public String getPrefix() {
            return null;
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

}
