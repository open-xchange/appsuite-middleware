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

package com.openexchange.authentication;

import static com.openexchange.authentication.LoginExceptionMessages.ACCOUNT_LOCKED_MSG;
import static com.openexchange.authentication.LoginExceptionMessages.ACCOUNT_NOT_READY_YET_MSG;
import static com.openexchange.authentication.LoginExceptionMessages.CLIENT_DENIED_MSG;
import static com.openexchange.authentication.LoginExceptionMessages.COMMUNICATION_MSG;
import static com.openexchange.authentication.LoginExceptionMessages.INVALID_CREDENTIALS_MSG;
import static com.openexchange.authentication.LoginExceptionMessages.LOGINS_WITHOUT_PASSWORD_EXCEEDED_MSG;
import static com.openexchange.authentication.LoginExceptionMessages.NEW_PASSWORD_REQUIRED_MSG;
import static com.openexchange.authentication.LoginExceptionMessages.NOT_SUPPORTED_MSG;
import static com.openexchange.authentication.LoginExceptionMessages.PASSWORD_EXPIRED_MSG;
import static com.openexchange.authentication.LoginExceptionMessages.REDIRECT_MSG;
import static com.openexchange.authentication.LoginExceptionMessages.USER_NOT_ACTIVE_MSG;
import static com.openexchange.exception.OXExceptionStrings.MESSAGE;
import static com.openexchange.exception.OXExceptionStrings.MESSAGE_RETRY;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Defines all error messages/codes for login-related errors.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum LoginExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * Account "%s" is locked.
     */
    ACCOUNT_LOCKED("Account \"%s\" is locked.", ACCOUNT_LOCKED_MSG, Category.CATEGORY_PERMISSION_DENIED, 1),
    /**
     * Account "%1$s" is currently being created. This can take a while. Please try again later.
     */
    ACCOUNT_NOT_READY_YET("Account \"%1$s\" is currently being created.", ACCOUNT_NOT_READY_YET_MSG, Category.CATEGORY_TRY_AGAIN, 2),
    /**
     * Unknown problem: "%s".
     */
    UNKNOWN("Unknown problem: \"%s\".", MESSAGE, Category.CATEGORY_ERROR, 3),
    /**
     * Login not possible at the moment. Please try again later.
     */
    COMMUNICATION("Login not possible at the moment.", COMMUNICATION_MSG, Category.CATEGORY_SERVICE_DOWN, 5),
    /**
     * Invalid credentials.
     */
    INVALID_CREDENTIALS("Invalid credentials.", INVALID_CREDENTIALS_MSG, Category.CATEGORY_USER_INPUT, 6),
    /**
     * Missing user mapping for user "%1$s". Login failed.
     */
    INVALID_CREDENTIALS_MISSING_USER_MAPPING("Missing user mapping for user \"%1$s\". Login failed.", INVALID_CREDENTIALS_MSG, Category.CATEGORY_USER_INPUT, 6),
    /**
     * Missing context mapping for context "%1$s". Login failed.
     */
    INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING("Missing context mapping for context \"%1$s\". Login failed.", INVALID_CREDENTIALS_MSG, Category.CATEGORY_USER_INPUT, 6),
    /**
     * Missing property %1$s.
     */
    MISSING_PROPERTY("Missing property %1$s.", MESSAGE, Category.CATEGORY_CONFIGURATION, 9),
    /**
     * database down.
     */
    DATABASE_DOWN("Database down.", MESSAGE_RETRY, Category.CATEGORY_SERVICE_DOWN, 10),
    /**
     * Your password expired.
     */
    PASSWORD_EXPIRED("Your password has expired. In order to change it, please log in to %1$s.", PASSWORD_EXPIRED_MSG, Category.CATEGORY_PERMISSION_DENIED, 11),
    /**
     * User is not activated.
     */
    USER_NOT_ACTIVE("User is not activated.", USER_NOT_ACTIVE_MSG, Category.CATEGORY_PERMISSION_DENIED, 13),
    /**
     * Client "%1$s" is not activated.
     */
    CLIENT_DENIED("Client \"%1$s\" is not activated.", CLIENT_DENIED_MSG, Category.CATEGORY_PERMISSION_DENIED, 14),
    /**
     * Method "%1$s" in HTTP header authorization is not supported.
     */
    UNKNOWN_HTTP_AUTHORIZATION("Method \"%1$s\" in HTTP header authorization is not supported.", MESSAGE, Category.CATEGORY_ERROR, 15),
    /**
     * Only used as workaround for a redirection, This is <b>no</b> real error.
     */
    REDIRECT("%1$s", REDIRECT_MSG, Category.CATEGORY_USER_INPUT, 16),
    /**
     * This exception code should be used for a not supported {@link AuthenticationService#handleAutoLoginInfo(LoginInfo)} method.
     * Message: %s does not support an auto login authentication.
     * Add class name as parameter when creating the exception.
     */
    NOT_SUPPORTED("%s does not support an auto login authentication.", NOT_SUPPORTED_MSG, Category.CATEGORY_SERVICE_DOWN, 19),
    /**
     * Server side token for token login was not created.
     */
    SERVER_TOKEN_NOT_CREATED("Server side token for token login was not created.", MESSAGE, Category.CATEGORY_ERROR, 20),
    /**
     * Value of User-Agent header must not be used as value for the client parameter. Please use a string identifying the client software.
     */
    DONT_USER_AGENT("Value of User-Agent header must not be used as value for the client parameter. Please use a string identifying the client software.", MESSAGE, Category.CATEGORY_USER_INPUT, 21),
    /**
     * You exceeded the maximum count of logins without password.
     */
    LOGINS_WITHOUT_PASSWORD_EXCEEDED("You exceeded the maximum count of logins without password.", LOGINS_WITHOUT_PASSWORD_EXCEEDED_MSG, Category.CATEGORY_USER_INPUT, 22),
    /**
     * A password is required to continue. Please choose one and try again.
     */
    NEW_PASSWORD_REQUIRED("A password is required to continue. Please choose one and try again.", NEW_PASSWORD_REQUIRED_MSG, Category.CATEGORY_USER_INPUT, 23),
    /**
     * Thrown on login attempts that target a disabled authentication mechanism. I.e. a proprietary login mechanism is used that bypasses
     * the authentication service. In those cases an authentication service can be registered that always throws this exception, which
     * in turn leads to responses that denote the unavailability of the used login mechanism (e.g. '403 Forbidden' for WebDAV requests).
     *
     * Authentication via this method is disabled.
     */
    AUTHENTICATION_DISABLED("Authentication via this method is disabled.", LoginExceptionMessages.AUTHENTICATION_DISABLED_MSG, Category.CATEGORY_PERMISSION_DENIED, 24),
    /**
     * The password is incorrect.
     */
    INVALID_GUEST_PASSWORD("Invalid credentials.", LoginExceptionMessages.INVALID_GUEST_PASSWORD_MSG, Category.CATEGORY_USER_INPUT, 25),
    /**
     * Thrown in case login attempts is denied by server or any 3rd party component that controls login flow.
     * <p>
     * Login denied
     */
    LOGIN_DENIED("Login denied.", LoginExceptionMessages.LOGIN_DENIED_MSG, Category.CATEGORY_PERMISSION_DENIED, 26),
    ;

    private static final String PREFIX = "LGI";

    /**
     * Gets the error code prefix (<code>"LGI"</code>) for login-related errors.
     *
     * @return The prefix
     */
    public static String prefix() {
        return PREFIX;
    }

    private final String message;
    private final String displayMessage;
    private final Category category;
    private final int number;

    private LoginExceptionCodes(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage;
        this.category = category;
        number = detailNumber;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
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
    public String getDisplayMessage() {
        return displayMessage;
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
