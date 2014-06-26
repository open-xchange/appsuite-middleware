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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajp13.servlet;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;

/**
 * OXServletException
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXServletException extends OXException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3931776129684819019L;

    private static final String PREFIX = "SVL";

    /**
     * The Open-Xchange servlet error code enumeration.
     */
    public static enum Code {

        /**
         * Missing property %s in 'system.properties'
         */
        MISSING_SERVLET_DIR("Missing property %s in 'system.properties'", Category.CATEGORY_CONFIGURATION, 1),
        /**
         * Servlet mapping directory does not exist: %s
         */
        DIR_NOT_EXISTS("Servlet mapping directory does not exist: %s", Category.CATEGORY_CONFIGURATION, 2),
        /**
         * File is not a directory: %s
         */
        NO_DIRECTORY("File is not a directory: %s", Category.CATEGORY_CONFIGURATION, 3),
        /**
         * Servlet mappings could not be loaded due to following error: %s
         */
        SERVLET_MAPPINGS_NOT_LOADED("Servlet mappings could not be loaded due to following error: %s", Category.CATEGORY_ERROR, 4),
        /**
         * No servlet class name found for key "%s". Please check servlet mappings.
         */
        NO_CLASS_NAME_FOUND("No servlet class name found for key \"%s\". Please check servlet mappings.", Category.CATEGORY_ERROR, 5),
        /**
         * Name "%s" already mapped to "%s". Ignoring servlet class "%s"
         */
        ALREADY_PRESENT("Name \"%s\" already mapped to \"%s\". Ignoring servlet class \"%s\"", Category.CATEGORY_CONFIGURATION, 6),
        /**
         * SecurityException while loading servlet class "%s"
         */
        SECURITY_ERR("SecurityException while loading servlet class \"%s\"", Category.CATEGORY_ERROR, 7),
        /**
         * Couldn't find servlet class "%s"
         */
        CLASS_NOT_FOUND("Couldn't find servlet class \"%s\"", Category.CATEGORY_ERROR, 8),
        /**
         * No default constructor specified in servlet class "%s"
         */
        NO_DEFAULT_CONSTRUCTOR("No default constructor specified in servlet class \"%s\"", Category.CATEGORY_ERROR, 9);

        private static final Object[] EMPTY_ARGS = new Object[0];

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
        private Code(final String message, final Category category, final int detailNumber) {
            this.message = message;
            this.category = category;
            number = detailNumber;
        }

        /**
         * Creates an {@link OXException} instance using this error code.
         *
         * @return The newly created {@link OXException} instance.
         */
        public OXException create() {
            return create(EMPTY_ARGS);
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
         * @param logArguments The arguments for log message.
         * @return The newly created {@link OXException} instance.
         */
        public OXException create(final Throwable cause, final Object... logArguments) {
            return new OXException(number, OXExceptionStrings.MESSAGE, cause).setPrefix(PREFIX).addCategory(category).setLogMessage(
                message,
                logArguments);
        }
    }

    private OXServletException() {
        super();
    }

}
