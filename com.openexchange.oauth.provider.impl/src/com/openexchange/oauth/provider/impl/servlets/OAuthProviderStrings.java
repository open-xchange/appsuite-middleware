/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.oauth.provider.impl.servlets;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link OAuthProviderStrings}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthProviderStrings implements LocalizableStrings {

    // Application example.com would like to connect with your account
    public static final String POPUP_TITLE = "Application %1$s would like to connect with your account";

    // Sign in to OX App Suite to connect example.com with your account
    public static final String LOGIN_FORM_HEADLINE = "Sign in to %1$s to connect %2$s with your account";

    // The application example.com would like to access your account, and is requesting following permissions:
    public static final String AUTHORIZATION_INTRO = "The application #split# would like to access your account, and is requesting following permissions:";

    // Only allow access for applications you trust. You may revoke access for this application at any time, by visiting your settings page.
    public static final String AUTHORIZATION_FOOTER = "Only allow access for applications you trust. You may revoke access for this application at any time, by visiting your settings page.";

    // label of cancel button
    public static final String CANCEL = "Cancel";

    // label of sign in button
    public static final String LOGIN = "Sign in";

    // label of deny button
    public static final String DENY = "Deny";

    // label of allow button
    public static final String ALLOW = "Allow";

    // label of username input field
    public static final String PASSWORD = "Password";

    // label of password input field
    public static final String USERNAME = "Username";

    // An error occurred
    public static final String ERROR_PAGE_TITLE = "An error occurred";

    // There seems to be a problem with this app.
    public static final String ERROR_HEADLINE = "There seems to be a problem with this app";

    // Don't worry, your data is perfectly safe.
    public static final String ERROR_MESSAGE = "Don't worry, your data is perfectly safe.";

    // View error details:
    public static final String ERROR_DETAILS_SUMMARY = "View error details:";

    // Close this popup window
    public static final String CLOSE = "Close";

}
