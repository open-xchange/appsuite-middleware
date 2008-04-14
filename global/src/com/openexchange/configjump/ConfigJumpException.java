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

package com.openexchange.configjump;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * Exception that can appear when creating setup links.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ConfigJumpException extends AbstractOXException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 6223798727641357672L;

    /**
     * Initializes a new exception using the information provided by the cause.
     * @param cause the cause of the exception.
     */
    public ConfigJumpException(final AbstractOXException cause) {
        super(cause);
    }

    /**
     * Initializes a new exception using the information provided by the code.
     * @param code code for the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public ConfigJumpException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provided by the code.
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public ConfigJumpException(final Code code, final Throwable cause,
        final Object... messageArgs) {
        super(Component.LOGIN, code.category, code.number, code.message, cause);
        setMessageArgs(messageArgs);
    }

    /**
     * Error codes for login exceptions.
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     */
    public enum Code {
        /**
         * Unknown problem: "%s".
         */
        UNKNOWN("Unknown problem: \"%s\".", Category.CODE_ERROR, 1),
        /**
         * Too few (%d) login attributes.
         */
        MISSING_ATTRIBUTES("Too few (%d) login attributes.",
            Category.USER_INPUT, 2),
        /**
         * Problem while communicating with external authorization.
         */
        COMMUNICATION("Problem while communicating with external "
            + "authorization.", Category.SUBSYSTEM_OR_SERVICE_DOWN, 3),
       /**
         * Instantiating the class failed.
         */
        INSTANTIATION_FAILED("Instantiating the class failed.",
            Category.CODE_ERROR, 4),
        /**
         * Class %1$s can not be found.
         */
        CLASS_NOT_FOUND("Class %1$s can not be found.", Category.SETUP_ERROR,
            5),
        /**
         * Missing property %1$s in system.properties.
         */
        MISSING_SETTING("Missing property %1$s in system.properties.",
            Category.SETUP_ERROR, 6),
        /**
         * URL "%s" is malformed.
         */
        MALFORMED_URL("URL \"%s\" is malformed.",
            Category.CODE_ERROR, 7),
        /**
         * Link is not implemented.
         */
        NOT_IMPLEMENTED("Extras link is not implemented.", Category.SETUP_ERROR, 8);

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
         * @param detailNumber detail number.
         */
        private Code(final String message, final Category category,
            final int detailNumber) {
            this.message = message;
            this.category = category;
            this.number = detailNumber;
        }

        /**
         * @return the category.
         */
        public Category getCategory() {
            return category;
        }

        /**
         * @return the message.
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return the number.
         */
        public int getNumber() {
            return number;
        }
    }
}
