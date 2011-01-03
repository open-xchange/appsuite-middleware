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

package com.openexchange.authentication;

import com.openexchange.authentication.exception.LoginExceptionFactory;
import com.openexchange.exceptions.ErrorMessage;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;

/**
 * Exception for all problems appearing during the login of a user. Currently we have a lot of different exceptions for all possible
 * exception types but this seems to be not handy anymore. So all different states should be consolidated here.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginException extends AbstractOXException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 5167438141225256336L;

    /**
     * Initializes a new exception using the information provided by the cause.
     * 
     * @param cause the cause of the exception.
     */
    public LoginException(final AbstractOXException cause) {
        super(cause);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * 
     * @param code code for the exception.
     * @param messageArgs arguments that will be formatted into the message.
     * @deprecated use {@link LoginExceptionCodes#create(Object...)} instead.
     */
    @Deprecated
    public LoginException(final Code code, final Object... messageArgs) {
        this(code.getCode(), null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * 
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     * @deprecated use {@link LoginExceptionCodes#create(Throwable, Object...)} instead.
     */
    @Deprecated
    public LoginException(final Code code, final Throwable cause, final Object... messageArgs) {
        this(code.getCode(), cause, messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * 
     * @param code code for the exception.
     * @param messageArgs arguments that will be formatted into the message.
     * @deprecated use {@link LoginExceptionFactory#create(int, Object...)}.
     */
    @Deprecated
    public LoginException(final LoginExceptionCodes code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * 
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     * @deprecated use {@link LoginExceptionFactory#create(int, Throwable, Object...)} instead.
     */
    @Deprecated
    public LoginException(final LoginExceptionCodes code, final Throwable cause, final Object... messageArgs) {
        super(EnumComponent.LOGIN, code.category, code.number, code.message, cause);
        setMessageArgs(messageArgs);
    }

    /**
     * Constructor for the {@link LoginExceptionFactory}. If you want to instantiate a {@link LoginException} use
     * {@link LoginExceptionCodes#create(Object...)} or {@link LoginExceptionCodes#create(Throwable, Object...)} methods.
     * 
     * @param message Parameters for filling the exception with all necessary data.
     * @param cause the initial cause of the exception.
     * @param messageArgs arguments for the exception message.
     */
    public LoginException(final ErrorMessage message, final Throwable cause, final Object... messageArgs) {
        super(message, cause);
        setMessageArgs(messageArgs);
    }

    /**
     * Error codes for login exceptions.
     * 
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     * @deprecated use {@link LoginExceptionCodes} instead.
     */
    @Deprecated
    public enum Code {
        /**
         * @deprecated use {@link LoginExceptionCodes#ACCOUNT_LOCKED} instead.
         */
        @Deprecated
        ACCOUNT_LOCKED(LoginExceptionCodes.ACCOUNT_LOCKED),
        /**
         * @deprecated use {@link LoginExceptionCodes#ACCOUNT_NOT_READY_YET} instead.
         */
        @Deprecated
        ACCOUNT_NOT_READY_YET(LoginExceptionCodes.ACCOUNT_NOT_READY_YET),
        /**
         * @deprecated use {@link LoginExceptionCodes#UNKNOWN} instead.
         */
        @Deprecated
        UNKNOWN(LoginExceptionCodes.UNKNOWN),
        /**
         * @deprecated use {@link LoginExceptionCodes#COMMUNICATION} instead.
         */
        @Deprecated
        COMMUNICATION(LoginExceptionCodes.COMMUNICATION),
        /**
         * @deprecated use {@link LoginExceptionCodes#INVALID_CREDENTIALS} instead.
         */
        @Deprecated
        INVALID_CREDENTIALS(LoginExceptionCodes.INVALID_CREDENTIALS),
        /**
         * @deprecated use {@link LoginExceptionCodes#INSTANTIATION_FAILED} instead.
         */
        @Deprecated
        INSTANTIATION_FAILED(LoginExceptionCodes.INSTANTIATION_FAILED),
        /**
         * @deprecated use {@link LoginExceptionCodes#CLASS_NOT_FOUND} instead.
         */
        @Deprecated
        CLASS_NOT_FOUND(LoginExceptionCodes.CLASS_NOT_FOUND),
        /**
         * @deprecated use {@link LoginExceptionCodes#MISSING_PROPERTY} instead.
         */
        @Deprecated
        MISSING_PROPERTY(LoginExceptionCodes.MISSING_PROPERTY),
        /**
         * @deprecated use {@link LoginExceptionCodes#DATABASE_DOWN} instead.
         */
        @Deprecated
        DATABASE_DOWN(LoginExceptionCodes.DATABASE_DOWN);

        private LoginExceptionCodes code;

        private Code(final LoginExceptionCodes code) {
            this.code = code;
        }

        public LoginExceptionCodes getCode() {
            return code;
        }
    }
}
