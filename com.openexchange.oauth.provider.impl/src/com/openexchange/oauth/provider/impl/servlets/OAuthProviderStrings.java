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
