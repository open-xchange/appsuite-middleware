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

package com.openexchange.oauth.provider.impl;

/**
 * {@link OAuthProviderConstants} - Constants for OAuth provider.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface OAuthProviderConstants {

    /**
     * Servlet alias of the authorization endpoint without leading and trailing slashes
     */
    public static final String AUTHORIZATION_SERVLET_ALIAS = "oauth/provider/authorization";

    /**
     * Servlet alias of the access token endpoint without leading and trailing slashes
     */
    public static final String ACCESS_TOKEN_SERVLET_ALIAS = "oauth/provider/accessToken";

    /**
     * Servlet alias of the endpoint for grant revocation by client applications
     */
    public static final String REVOKE_SERVLET_ALIAS = "oauth/provider/revoke";

    /**
     * Servlet alias of the endpoint for token validation
     */
    public static final String TOKEN_INFO_SERVLET_ALIAS = "oauth/provider/tokeninfo";

    /**
     * Servlet alias of the endpoint for token introspection
     */
    public static final String TOKEN_INTROSPECTION_SERVLET_ALIAS = "oauth/provider/introspect";

    // ---------------- Parameters ---------------------------------------------------------------------------

    /** Required. See {@link #RESPONSE_TYPE_AUTH_CODE} */
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

    /** Required. The session ID */
    public static final String PARAM_SESSION = "session";

    /** Required. The user password */
    public static final String PARAM_ACCESS_DENIED = "access_denied";

    /** Required. The CSRF token */
    public static final String PARAM_CSRF_TOKEN = "csrf_token";

    /** The error */
    public static final String PARAM_ERROR = "error";

    /** The error description */
    public static final String PARAM_ERROR_DESCRIPTION = "error_description";

    /** The language parameter defining a locale for i18n */
    public static final String PARAM_LANGUAGE = "language";

    /** The access token parameter */
    public static final String PARAM_ACCESS_TOKEN = "access_token";

    // ---------------- Common parameter values --------------------------------------------------------------

    /** Response type <b><code>code</code></b> */
    public static final String RESPONSE_TYPE_AUTH_CODE = "code";

    /** Grant type <b><code>refresh_token</code></b> */
    public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

    /** Grant type <b><code>authorization_code</code></b> */
    public static final String GRANT_TYPE_AUTH_CODE = "authorization_code";

}
