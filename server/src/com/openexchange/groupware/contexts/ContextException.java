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

package com.openexchange.groupware.contexts;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * This exception will be thrown if error occur in the storage classes for the
 * contexts.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ContextException extends AbstractOXException {

    /**
     * Serialization.
     */
    private static final long serialVersionUID = -6369708132747242970L;

    /**
     * Initializes a new exception using the information provided by the cause.
     * @param cause the cause of the exception.
     */
    public ContextException(final AbstractOXException cause) {
        super(cause);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public ContextException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public ContextException(final Code code, final Throwable cause,
        final Object... messageArgs) {
        super(Component.CONTEXT, code.category, code.number, code.message,
            cause);
        setMessageArgs(messageArgs);
    }

    /**
     * Error codes for context exceptions.
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     */
    public enum Code {
        /**
         * Mailadmin for a context is missing.
         */
        NO_MAILADMIN("Cannot resolve mailadmin for context %d.",
            Category.SETUP_ERROR, 1),
        /**
         * Cannot find context %d.
         */
        NOT_FOUND("Cannot find context %d.", Category.SETUP_ERROR, 2),
        /**
         * Putting an object into the cache failed.
         */
        CACHE_PUT("Cannot put object %s into cache.", Category.INTERNAL_ERROR,
            3),
        /**
         * No connection to database.
         */
        NO_CONNECTION("Cannot get connection to database.",
            Category.SUBSYSTEM_OR_SERVICE_DOWN, 5),
        /**
         * SQL problem: %1$s.
         */
        SQL_ERROR("SQL problem: %1$s.", Category.CODE_ERROR, 6),
        /**
         * Updating database ... Try again later.
         */
        UPDATE("Updating database ... Try again later.", Category
            .TRY_AGAIN, 7),
        /**
         * Problem initializing the cache.
         */
        CACHE_INIT("Problem initializing the cache.", Category.SETUP_ERROR, 8),
        /**
         * Cannot remove object %s from cache.
         */
        CACHE_REMOVE("Cannot remove object %s from cache.", Category
            .INTERNAL_ERROR, 9),
        /**
         * Cannot find context "%s".
         */
        NO_MAPPING("Cannot find context \"%s\".", Category.USER_INPUT, 10);

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
         * @param message message.
         * @param category category.
         * @param number detail number.
         */
        private Code(final String message, final Category category,
            final int number) {
            this.message = message;
            this.category = category;
            this.number = number;
        }

        /**
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return the category
         */
        public Category getCategory() {
            return category;
        }

        /**
         * @return the number
         */
        public int getNumber() {
            return number;
        }
    }
}
