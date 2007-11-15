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



package com.openexchange.sessiond.impl;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * Exception for all problems appearing during the login of a user. Currently
 * we have a lot of different exceptions for all possible exception types but
 * this seems to be not handy anymore. So all different states should be
 * consolidated here.
 * TODO move to com.openexchange.groupware.login
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginException extends AbstractOXException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 5167438141225256336L;

    /**
     * Source of the exception.
     */
    private final Source source;

    /**
     * Initializes a new exception using the information provided by the cause.
     * @param cause the cause of the exception.
     */
    public LoginException(final AbstractOXException cause) {
        super(cause);
        this.source = Source.SYSTEM;
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public LoginException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public LoginException(final Code code, final Throwable cause,
        final Object... messageArgs) {
        super(Component.LOGIN, code.category, code.number, code.message, cause);
        this.source = code.source;
        setMessageArgs(messageArgs);
    }

    /**
     * @return the source
     */
    public Source getSource() {
        return source;
    }

    /**
     * Source types of the login exception.
     */
    public enum Source {
        /**
         * The user caused the login exception due to invalid credentials or
         * something else.
         */
        USER,
        /**
         * The system cause the login exception.
         */
        SYSTEM
    }

    /**
     * Error codes for login exceptions.
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     */
    public enum Code {
        /**
         * Account "%s" is locked.
         */
        ACCOUNT_LOCKED("Account \"%s\" is locked.", Category.PERMISSION,
            Source.SYSTEM, 1),
        /**
         * Account "%s" is not ready yet.
         */
        ACCOUNT_NOT_READY_YET("Account \"%s\" is not ready yet.",
            Category.TRY_AGAIN, Source.SYSTEM, 2),
        /**
         * Unknown problem: "%s".
         */
        UNKNOWN("Unknown problem: \"%s\".", Category.CODE_ERROR,
            Source.SYSTEM, 3),
        /**
         * Too few (%d) login attributes.
         */
        MISSING_ATTRIBUTES("Too few (%d) login attributes.",
            Category.USER_INPUT, Source.USER, 4),
        /**
         * Login not possible at the moment. Please try again later.
         */
        COMMUNICATION("Login not possible at the moment. Please try again "
            + "later.", Category.SUBSYSTEM_OR_SERVICE_DOWN, Source.SYSTEM, 5),
        /**
         * Invalid credentials.
         */
        INVALID_CREDENTIALS("Invalid credentials.", Category.USER_INPUT,
            Source.USER, 6),
        /**
         * Instantiating the class failed.
         */
        INSTANTIATION_FAILED("Instantiating the class failed.",
            Category.CODE_ERROR, Source.SYSTEM, 7),
        /**
         * Class %1$s can not be found.
         */
        CLASS_NOT_FOUND("Class %1$s can not be found.", Category.SETUP_ERROR,
            Source.SYSTEM, 8),
        /**
         * Missing property %1$s in server.properties.
         */
        MISSING_SETTING("Missing property %1$s in server.properties.",
            Category.SETUP_ERROR, Source.SYSTEM, 9),
        /**
         * database down.
         */
        DATABASE_DOWN("Database down.", Category.SUBSYSTEM_OR_SERVICE_DOWN,
            Source.SYSTEM, 10);

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
         * Source of the exception.
         */
        private final Source source;

        /**
         * Default constructor.
         * @param message message.
         * @param category category.
         * @param source source.
         * @param detailNumber detail number.
         */
        private Code(final String message, final Category category,
            final Source source, final int detailNumber) {
            this.message = message;
            this.category = category;
            this.source = source;
            this.number = detailNumber;
        }

        /**
         * @return the category
         */
        public Category getCategory() {
            return category;
        }

        /**
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return the number
         */
        public int getNumber() {
            return number;
        }
    }
}
