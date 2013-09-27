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

package com.openexchange.authentication;

import static com.openexchange.authentication.LoginExceptionMessages.*;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Defines all error messages for the OXException.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum LoginExceptionCodes implements OXExceptionCode {

    /** Account "%s" is locked. */
    ACCOUNT_LOCKED(ACCOUNT_LOCKED_MSG, Category.CATEGORY_PERMISSION_DENIED, 1),
    /** Account "%1$s" is currently being created. This can take a while. Please try again later. */
    ACCOUNT_NOT_READY_YET(ACCOUNT_NOT_READY_YET_MSG, Category.CATEGORY_TRY_AGAIN, 2),
    /** Unknown problem: "%s". */
    UNKNOWN(UNKNOWN_MSG, Category.CATEGORY_ERROR, 3),
    /** Login not possible at the moment. Please try again later. */
    COMMUNICATION(COMMUNICATION_MSG, Category.CATEGORY_SERVICE_DOWN, 5),
    /** Invalid credentials. */
    INVALID_CREDENTIALS(INVALID_CREDENTIALS_MSG, Category.CATEGORY_USER_INPUT, 6),
    /** Instantiating the class failed. */
    /** Missing property %1$s. */
    MISSING_PROPERTY(MISSING_PROPERTY_MSG, Category.CATEGORY_CONFIGURATION, 9),
    /** database down. */
    DATABASE_DOWN(DATABASE_DOWN_MSG, Category.CATEGORY_SERVICE_DOWN, 10),
    /** Your password expired. */
    PASSWORD_EXPIRED(PASSWORD_EXPIRED_MSG, Category.CATEGORY_PERMISSION_DENIED, 11),
    /** User %1$s could not be found in context %2$s. */
    USER_NOT_FOUND(USER_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 12),
    /** User is not activated. */
    USER_NOT_ACTIVE(USER_NOT_ACTIVE_MSG, Category.CATEGORY_PERMISSION_DENIED, 13),
    /** Client "%1$s" is not activated. */
    CLIENT_DENIED(CLIENT_DENIED_MSG, Category.CATEGORY_PERMISSION_DENIED, 14),
    /** Method "%1$s" in HTTP header authorization is not supported. */
    UNKNOWN_HTTP_AUTHORIZATION(UNKNOWN_HTTP_AUTHORIZATION_MSG, Category.CATEGORY_TRY_AGAIN, 15),
    /** Only used as workaround for a redirection, This is <b>no</b> real error. */
    REDIRECT(REDIRECT_MSG, Category.CATEGORY_USER_INPUT, 16),
    /** No session found. */
    NO_SESSION_FOUND(NO_SESSION_FOUND_MSG, Category.CATEGORY_USER_INPUT, 17),
    /** Missing client capabilities. */
    MISSING_CAPABILITIES(MISSING_CAPABILITIES_MSG, Category.CATEGORY_WARNING, 18),
    /**
     * This exception code should be used for a not supported {@link AuthenticationService#handleAutoLoginInfo(LoginInfo)} method.
     * Message: %s does not support an auto login authentication.
     * Add class name as parameter when creating the exception.
     */
    NOT_SUPPORTED(NOT_SUPPORTED_MSG, Category.CATEGORY_SERVICE_DOWN, 19),
    /** Server side token for token login was not created. */
    SERVER_TOKEN_NOT_CREATED(SERVER_TOKEN_NOT_CREATED_MSG, Category.CATEGORY_ERROR, 20),
    /** Value of User-Agent header must not be used as value for the client parameter. Please use a string identifying the client software. */
    DONT_USER_AGENT(DONT_USER_AGENT_MSG, Category.CATEGORY_USER_INPUT, 21);

    private final String message;

    private final Category category;

    private final int number;

    private LoginExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        number = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "LGI";
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getNumber() {
        return number;
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
