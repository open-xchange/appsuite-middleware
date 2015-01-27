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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

import java.util.concurrent.TimeUnit;

/**
 * {@link OAuthProviderConstants} - Constants for OAuth provider.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface OAuthProviderConstants {

    /**
     * Servlet alias of the authorization endpoint without leading and trailing slashes
     */
    public static final String AUTHORIZATION_SERVLET_ALIAS = "o/oauth2/authorization";

    /**
     * Servlet alias of the access token endpoint without leading and trailing slashes
     */
    public static final String ACCESS_TOKEN_SERVLET_ALIAS = "o/oauth2/accessToken";

    // -------------------------------------------------------------------------------------------------------

    /**
     * The default expiration time for a generated access token in milliseconds.
     */
    public static final long DEFAULT_EXPIRATION = TimeUnit.HOURS.toMillis(1L);

    // -------------------------------------------------------------------------------------------------------

    /** Required. Value is always <b><code>code</code></b> */
    public static final String PARAM_RESPONSE_TYPE = "response_type";

    /** Required. Value of your <b>API Key</b> given when you registered your application */
    public static final String PARAM_CLIENT_ID = "client_id";

    /** Required. Value of your <b>API Secret</b> given when you registered your application */
    public static final String PARAM_CLIENT_SECRET = "client_secret";

    /** Optional. Used to specify a list of needed member permissions */
    public static final String PARAM_SCOPE = "scope";

    /** Required. A long unique string value of that is hard to guess */
    public static final String PARAM_STATE = "state";

    /** Required. URI of the app used as redirect after authorization. */
    public static final String PARAM_REDIRECT_URI = "redirect_uri";

    /** Required. Value of <b>authorization_code</b>. */
    public static final String PARAM_CODE = "code";

    /** Required. Must always be <b>authorization_code</b>. */
    public static final String PARAM_GRANT_TYPE = "grant_type";

    /** Required. The refresh token */
    public static final String PARAM_REFRESH_TOKEN = "refresh_token";

    /** Required. The user login */
    public static final String PARAM_USER_LOGIN = "user_login";

    /** Required. The user password */
    public static final String PARAM_USER_PASSWORD = "user_password";

    /** Required. The user password */
    public static final String PARAM_ACCESS_DENIED = "access_denied";

    /** Required. The CSRF token */
    public static final String PARAM_CSRF_TOKEN = "csrf_token";

    /** The error */
    public static final String PARAM_ERROR = "error";

    /** The error description */
    public static final String PARAM_ERROR_DESCRIPTION = "error_description";

}
