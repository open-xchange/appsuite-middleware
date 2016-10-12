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

package com.openexchange.oauth;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link OAuthExceptionMessages} - Exception messages that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class OAuthExceptionMessages implements LocalizableStrings {

    // There is no associated OAuth account for identifier %1$s.
    public static final String ACCOUNT_NOT_FOUND_MSG = "There is no associated OAuth account for identifier %1$s.";

    // Missing argument: %1$s
    public static final String MISSING_ARGUMENT_MSG = "Missing argument: %1$s";

    // Your '%1$s' password changed. You have to authorize the server to use your account with the new password. To do so, go to Configuration -> My Social Configuration -> Accounts. Then try again.
    public static final String TOKEN_EXPIRED_MSG = "Your '%1$s' password changed. You have to authorize the server to use your account with the new password. To do so, go to Configuration -> My Social Configuration -> Accounts. Then try again.";

    public static final String NOT_A_WHITELISTED_URL_MSG = "The address %1$s is not white-listed as for the %2$s OAuth API";

	public static final String MISSING_BODY_MSG = "The request sent was missing its body";

	// The account is invalid, please recreate it.
	public static final String INVALID_ACCOUNT_MSG = "The account is invalid, please recreate it.";

    // The account "%1$s" is invalid, please recreate it.
    public static final String INVALID_ACCOUNT_EXTENDED_MSG = "The account \"%1$s\" is invalid, please recreate it.";

	// Please provide a display name.
    public static final String MISSING_DISPLAY_NAME_MSG = "Please provide a display name.";

    // Please provide at least one scope.
    public static final String MISSING_SCOPE_MSG = "Please provide at least one scope.";

    // The associated OAuth provider denied the request: %1$s.
    public static final String DENIED_BY_PROVIDER_MSG = "The associated OAuth provider denied the request: %1$s.";

    // The OAuth authentication process has been canceled.
    public static final String CANCELED_BY_USER_MSG = "The OAuth authentication process has been canceled.";

    // Could not get a valid response from the associated OAuth provider.
    public static final String NOT_A_VALID_RESPONSE_MSG = "Could not get a valid response from the associated OAuth provider.";

    // There was a problem while creating a connection to the remote service.
    public static final String CONNECT_ERROR_MSG = "There was a problem while creating a connection to the remote service.";

    // You need to grant additional %1$s permissions to access the resource.
    public static final String OAUTH_PROBLEM_ADDITIONAL_AUTHORIZATION_REQUIRED_MSG = "You need to grant additional %1$s permissions to access the resource.";

    // Your '%1$s' access was rejected. You have to reauthorize the server to use your account with a new password. To do so, go to Configuration -> My Social Configuration -> Accounts. Then try again.
    public static final String OAUTH_PROBLEM_TOKEN_REJECTED_MSG = "Your '%1$s' access was rejected. You have to reauthorize the server to use your account with a new password. To do so, go to Configuration -> My Social Configuration -> Accounts. Then try again.";

    // Your '%1$s' access was revoked. You have to reauthorize the server to use your account with a new password. To do so, go to Configuration -> My Social Configuration -> Accounts. Then try again.
    public static String OAUTH_PROBLEM_TOKEN_REVOKED_MSG = "Your '%1$s' access was revoked. You have to reauthorize the server to use your account with a new password. To do so, go to Configuration -> My Social Configuration -> Accounts. Then try again.";

    // Your access token for '%1$s' is invalid. You have to reauthorize your account.
    public static String OAUTH_TOKEN_INVALID = "Your access token for '%1$s' is invalid. You have to reauthorize your account.";

    // You need to explicitly authorise %1$s to subscribe to %2$s
    public static final String NO_SCOPE_PERMISSION = "You need to explicitly authorise %1$s to subscribe to %2$s";

    // Thrown in case an OAuth provider is requested to access certain data, but such data is not available at all. For instance Dropbox is requested to access E-Mails.
    public static final String NO_SUCH_SCOPE_AVAILABLE = "The scope %1$s is not supported by the provider";


    /**
     * Initializes a new {@link OAuthExceptionMessages}.
     */
    private OAuthExceptionMessages() {
        super();
    }

}
