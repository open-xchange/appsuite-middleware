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

/**
 * {@link OAuthConstants} - Constants for OAuth module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OAuthConstants {

    /**
     * Initializes a new {@link OAuthConstants}.
     */
    private OAuthConstants() {
        super();
    }

    /*-
     * ------------------- Arguments -------------------
     */

    /**
     * The argument name for PIN. <code>java.lang.String</code>
     */
    public static final String ARGUMENT_PIN = "pin";

    /**
     * The argument name for display name. <code>java.lang.String</code>
     */
    public static final String ARGUMENT_DISPLAY_NAME = "displayName";

    /**
     * The argument name for request token. <code>com.openexchange.oauth.OAuthToken</code>
     */
    public static final String ARGUMENT_REQUEST_TOKEN = "requestToken";

    /**
     * The argument name for call-back URL. <code>java.lang.String</code>
     */
    public static final String ARGUMENT_CALLBACK = "callbackURL";

    /**
     * The argument name for token string. <code>java.lang.String</code>
     */
    public static final String ARGUMENT_TOKEN = "token";

    /**
     * The argument name for secret string. <code>java.lang.String</code>
     */
    public static final String ARGUMENT_SECRET = "secret";

    /**
     * The argument name for session. <code>com.openexchange.session.Session</code>
     */
    public static final String ARGUMENT_SESSION = "session";

    /**
     * The argument name for current host name. <code>java.lang.String</code>
     */
    public static final String ARGUMENT_CURRENT_HOST = "current_host";

    /**
     * The argument name for authorization URL. <code>java.lang.String</code>
     */
    public static final String ARGUMENT_AUTH_URL = "auth_url";

    /*-
     * ------------------- URL parameters -------------------
     */

    /**
     * The URL parameter added to call-back URL providing an error description.
     */
    public static final String URLPARAM_ERROR = "error";

    /**
     * The URL parameter added to call-back URL providing the OAuth token according to specification.
     */
    public static final String URLPARAM_OAUTH_TOKEN = "oauth_token";

    /**
     * The URL parameter added to call-back URL providing the OAuth verifier according to specification.
     */
    public static final String URLPARAM_OAUTH_VERIFIER = "oauth_verifier";

    /**
     * The URL parameter for call-back URL.
     */
    public static final String URLPARAM_OAUTH_CALLBACK = "oauth_callback";

    /**
     * The URL parameter is a brief string identifying a problem. Permitted values are listed as OAUTH_PROBLEM_* constants below.
     */
    public static final String URLPARAM_OAUTH_PROBLEM = "oauth_problem";

    /**
     * The URL parameter consists of a set of parameter names, percent-encoded and then separated by '&'. Note that this string will be
     * percent-encoded again, because it's the value of oauth_parameters_absent. For example, a response body may contain:
     * oauth_problem=parameter_absent&oauth_parameters_absent=oauth_token%26oauth_nonce
     */
    public static final String URLPARAM_OAUTH_PARAMETERS_ABSENT = "oauth_parameters_absent";

    /**
     * The URL parameter consists of a set of parameters, encoded as they would be in a URL query string. These are parameters that the
     * sender recently received but didn't expect. Note that these parameters will be percent-encoded twice: once to form a query string and
     * again because the query string is the value of oauth_parameters_rejected.
     */
    public static final String URLPARAM_OAUTH_PARAMETERS_REJECTED = "oauth_parameters_rejected";

    /**
     * The URL parameter consists of two version numbers separated by '-' (hyphen). It's the range of versions acceptable to the sender.
     * That is, it means the sender will currently accept an oauth_version that's not less than the first number and not greater than the
     * second number. A version A.B is considered greater than C.D if either A > C, or A = C and B > D.
     */
    public static final String URLPARAM_OAUTH_ACCEPTABLE_VERSIONS = "oauth_acceptable_versions";

    /**
     * The URL parameter consists of two numbers in decimal notation, separated by '-' (hyphen). It's the range of timestamps acceptable to
     * the sender. That is, it means the sender will currently accept an oauth_timestamp that's not less than the first number and not
     * greater than the second number.
     */
    public static final String URLPARAM_OAUTH_ACCEPTABLE_TIMESTAMPS = "oauth_acceptable_timestamps";

    /*-
     * ------------------- Problem constants -------------------
     * see http://wiki.oauth.net/w/page/12238543/ProblemReporting
     */

    /**
     * The oauth_version isn't supported by the Service Provider. In this case, the response SHOULD also contain an
     * oauth_acceptable_versions parameter
     */
    public static final String OAUTH_PROBLEM_VERSION_REJECTED = "version_rejected";

    /**
     * A required parameter wasn't received. In this case, the response SHOULD also contain an oauth_parameters_absent parameter.
     */
    public static final String OAUTH_PROBLEM_PARAMETER_ABSENT = "parameter_absent";

    /**
     * An unexpected parameter was received. In this case, the response SHOULD also contain an oauth_parameters_rejected parameter.
     */
    public static final String OAUTH_PROBLEM_PARAMETER_REJECTED = "parameter_rejected";

    /**
     * The oauth_timestamp value is unacceptable to the Service Provider. In this case, the response SHOULD also contain an
     * oauth_acceptable_timestamps parameter
     */
    public static final String OAUTH_PROBLEM_TIMESTAMP_REFUSED = "timestamp_refused";

    /**
     * The oauth_nonce value was used in a previous request, and consequently can't be used now.
     */
    public static final String OAUTH_PROBLEM_NONCE_USED = "nonce_used";

    /**
     * The oauth_signature_method is unacceptable to the Service Provider.
     */
    public static final String OAUTH_PROBLEM_SIGNATURE_METHOD_REJECTED = "signature_method_rejected";

    /**
     * The oauth_signature is invalid. That is, it doesn't match the signature computed by the Service Provider.
     */
    public static final String OAUTH_PROBLEM_SIGNATURE_INVALID = "signature_invalid";

    /**
     * The oauth_consumer_key is unknown to the Service Provider.
     */
    public static final String OAUTH_PROBLEM_CONSUMER_KEY_UNKNOWN = "consumer_key_unknown";

    /**
     * The oauth_consumer_key is permanently unacceptable to the Service Provider. For example, the Consumer may be black listed.
     */
    public static final String OAUTH_PROBLEM_CONSUMER_KEY_REJECTED = "consumer_key_rejected";

    /**
     * The oauth_consumer_key is temporarily unacceptable to the Service Provider. For example, the Service Provider may be throttling the
     * Consumer.
     */
    public static final String OAUTH_PROBLEM_CONSUMER_KEY_REFUSED = "consumer_key_refused";

    /**
     * The oauth_token has been consumed. That is, it can't be used any more because it has already been used in a previous request or
     * requests.
     */
    public static final String OAUTH_PROBLEM_TOKEN_USED = "token_used";

    /**
     * The oauth_token has expired. That is, it was issued too long ago to be used now. If the ScalableOAuth extensions are supported by the
     * Consumer, it can pass on the oauth_session_handle it received in the previous Access Token request to obtain
     * a renewed Access token and secret.
     */
    public static final String OAUTH_PROBLEM_TOKEN_EXPIRED = "token_expired";

    /**
     * The oauth_token has been revoked. That is, the Service Provider has unilaterally decided it will never accept this token.
     */
    public static final String OAUTH_PROBLEM_TOKEN_REVOKED = "token_revoked";

    /**
     * The oauth_token is unacceptable to the Service Provider. The reason is unspecified. It might mean that the token was never issued,
     * or consumed or expired and then subsequently forgotten by the Service Provider.
     */
    public static final String OAUTH_PROBLEM_TOKEN_REJECTED = "token_rejected";

    /**
     * The oauth_verifier is incorrect.
     */
    public static final String OAUTH_PROBLEM_VERIFIER_INVALID = "verifier_invalid";

    /**
     * The user needs to give additional permissions before the Consumer is allowed access to the resource. If the ScalableOAuth extensions
     * are supported by the Consumer, it can use the oauth_token (access token) it already has as the request token to initiate the
     * authorization process again, in which case it must use the oauth_token_secret (access token secret) to sign the request for a new
     * access token once the user finishes giving authorization.
     */
    public static final String OAUTH_PROBLEM_ADDITIONAL_AUTHORIZATION_REQUIRED = "additional_authorization_required";

    /**
     * The User hasn't decided whether to permit this Consumer to access Protected Resources. Usually happens when the Consumer requests
     * Access Token before the user completes authorization process.
     */
    public static final String OAUTH_PROBLEM_PERMISSION_UNKNOWN = "permission_unknown";

    /**
     * The User refused to permit this Consumer to access Protected Resources.
     */
    public static final String OAUTH_PROBLEM_PERMISSION_DENIED = "permission_denied";

    /**
     * The OAuth provider refused to permit this Consumer to access Protected Resources.
     */
    public static final String OAUTH_PROBLEM_ACCESS_DENIED = "access_denied";

    /**
     * The User (in most cases it's just user's IP) is temporarily unacceptable to the Service Provider. For example, the Service Provider
     * may be rate limiting the IP based on number of requests. This error condition applies to the Authorization process where the user
     * interacts with Service Provider directly. The Service Provider might return this error in the authorization response or in the Access
     * Token request response.
     */
    public static final String OAUTH_PROBLEM_USER_REFUSED = "user_refused";

    /*-
     * ------------------- Session parameters -------------------
     */

    /**
     * The session parameter providing the UUID associated with OAuth state. Actually a <code>java.uitl.Map&lt;String, Object&gt;</code>
     * providing request token's secret, call-back URL, whatever.
     */
    public static final String SESSION_PARAM_UUID = "uuid";

    /*-
     * ------------------- ID type identifier for accounts -------------------
     */

    /**
     * The type constant used for generated IDs.
     */
    public static final String TYPE_ACCOUNT = "com.openexchange.oauth.account";

}
