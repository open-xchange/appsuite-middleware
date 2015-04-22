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

package com.openexchange.oauth.provider;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link OAuthProviderExceptionMessages} - Exception messages that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class OAuthProviderExceptionMessages implements LocalizableStrings {

    // Account not found.
    public static final String ACCOUNT_NOT_FOUND_MSG = "Account not found";

    // Another process already revoked the secret for a client
    public static final String CONCURRENT_SECRET_REVOKE_MSG = "Another process already revoked the secret of your client";

    // '%1$s' is not a valid redirect URI.
    public static final String INVALID_REDIRECT_URI_MSG = "'%1$s' is not a valid redirect URI.";

    // The client '%1$s' could not be enabled.
    public static final String FAILED_ENABLEMENT_MSG = "The client '%1$s' could not be enabled.";

    // The client '%1$s' could not be disabled.
    public static final String FAILED_DISABLEMENT_MSG = "The client '%1$s' could not be disabled.";

    // You reached the max. number of %3$d possible grants for 3rd party applications. Please revoke access for the ones you don't longer need.
    public static final String GRANTS_EXCEEDED_MSG = "You reached the max. number of %3$d possible grants for 3rd party applications. Please revoke access for the ones you don't longer need.";

    // ------------------------------------------------------------------------
    // Messages below are used to be send to the client within redirects
    // ------------------------------------------------------------------------

    public static final String PARAMETER_MISSING_MSG = "Request was missing a required parameter: '%1$s'";

    public static final String PARAMETER_INVALID_MSG = "Request has an invalid parameter: '%1$s'";

    public static final String NO_SECURE_CONNECTION_MSG = "";

    public static final String WRONG_RESPONSE_TYPE_MSG = "Response type not supported";

    public static final String REQUEST_DENIED_MSG = "The user denied your request";

    public static final String NOT_ALLOWED_MSG = "You are not allowed to grant access via OAuth to 3rd party applications.";

    public static final String INVALID_REFERER_HEADER_MSG = "Request contained no or invalid referer header";

    public static final String INVALID_CSRF_TOKEN_MSG = "Request contained no or invalid CSRF token. Ensure that cookies are allowed";

    /**
     * Initializes a new {@link OAuthProviderExceptionMessages}.
     */
    private OAuthProviderExceptionMessages() {
        super();
    }

}
