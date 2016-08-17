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

import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocalizableStrings;

/**
 * Exception messages for the {@link OXException} that must be translated.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class LoginExceptionMessages implements LocalizableStrings {

    // The authentication mechanism is completely replaceable. Some hoster need to ban users. This message is used therefore.
    // %s is replaced with some login name.
    public static final String ACCOUNT_LOCKED_MSG = "The account \"%s\" is locked.";

    // Provisioning of some account may take some time although the login is already possible. If creating the account has not finished on
    // OX side the login mechanism can use this message to prevent the login.
    // %s is replaced with some login name.
    public static final String ACCOUNT_NOT_READY_YET_MSG = "Account \"%1$s\" is currently being created. This can take a while. Please try again later.";

    // This message can be used if the authentication systems are not reachable. The customer should try some time later again.
    public static final String COMMUNICATION_MSG = "Login not possible at the moment. Please try again later.";

    // The supplied credentials for the authentication are invalid.
    public static final String INVALID_CREDENTIALS_MSG = "The user name or password is incorrect.";

    // This message is used if the password of the user expired and must be changed.
    // %1$s is replaced by an URL for changing the password or by the name of the system that is able to do that.
    public static final String PASSWORD_EXPIRED_MSG = "Your password has expired. In order to change it, please log in to %1$s.";

    // Indicates whether indicated client is allowed to perform a login
    // E.g. 'Client "OLOX20" is not activated.'
    public static final String CLIENT_DENIED_MSG = "You do not have the appropriate permissions to login with client \"%1$s\".";

    // This message is thrown when the login request with a HTTP authorization header contains a authorization method that is not supported.
    // %1$s is replaved with the not supported HTTP authorization header method.
    public static final String UNKNOWN_HTTP_AUTHORIZATION_MSG = "Method \"%1$s\" in HTTP header authorization is not supported.";

    // Missing client capabilities.
    public static final String MISSING_CAPABILITIES_MSG = "Missing client capabilities.";

    // This simple message does not need to be translated. It is used to transport data to the frontend. Just put the same value in it for
    // all translations or remove it from the PO file.
    public static final String REDIRECT_MSG = "%1$s";

    // Thrown when an AuthenticationService implementation does not support the auto login method.
    // %s is replaced with the implementations name.
    public static final String NOT_SUPPORTED_MSG = "Automatic login is not supported.";

    // Indicates a deactivated user
    public static final String USER_NOT_ACTIVE_MSG = "The user account is not activated.";

    // You exceeded the maximum count of logins without password.
    public static final String LOGINS_WITHOUT_PASSWORD_EXCEEDED_MSG = "You exceeded the maximum count of logins without password.";

    // A password is required to continue. Please choose one and try again.
    public static final String NEW_PASSWORD_REQUIRED_MSG = "A password is required to continue. Please choose one and try again.";

    // Authentication via this method is disabled.
    public static final String AUTHENTICATION_DISABLED_MSG = "Authentication via this method is disabled.";

    // The password is incorrect.
    public static final String INVALID_GUEST_PASSWORD_MSG = "The password is incorrect.";

    // Login denied
    public static final String LOGIN_DENIED_MSG = "Login denied.";

    private LoginExceptionMessages() {
        super();
    }
}
