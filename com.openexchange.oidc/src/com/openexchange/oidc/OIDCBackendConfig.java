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
package com.openexchange.oidc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.NamePart;

/**
 * {@link OIDCBackendConfig}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public interface OIDCBackendConfig {

    /**
     * {@link AutologinMode}
     *
     * All valid auto-login modes for a given backend. The session information is stored in an
     * own OIDC-Cookie. Therefore the name of the property is 'com.openexchange.oidc.autologinCookieMode'.
     *
     * off: No auto-login at all
     * sso_redirect: Means the user is redirected to the OP first and asked for confirmation
     * ox_direct: The current user session is terminated immediately
     */
    public static enum AutologinMode {
        /**
         * OFF - No auto-login at all
         */
        OFF("off"),
        /**
         * SSO_REDIRECT - Redirect to OP for confirmation of a valid session
         * Needed properties:
         * <br>
         * - autologinCookieMode <br>
         */
        SSO_REDIRECT("sso_redirect"),
        /**
         * OX_DIRECT - Directly login into a valid OXSession
         * Needed properties:
         * <br>
         * - autologinCookieMode <br>
         */
        OX_DIRECT("ox_direct");

        private final String value;

        private AutologinMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        private static final Map<String, AutologinMode> lookup;

        static {
            Map<String, AutologinMode> tmp = new HashMap<>();
            for (AutologinMode mode : AutologinMode.values()) {
                tmp.put(mode.getValue(), mode);
            }
            lookup = ImmutableMap.copyOf(tmp);
        }

        public static AutologinMode get(String mode) {
            return lookup.get(mode);
        }
    }

    /**
     * The id, which the backend client received from the OP on client registration.
     *
     * @return the client id.
     */
    String getClientID();

    /**
     * Get the redirect URI that the OP should redirect to after token generation. Pointing to
     * the authentication servlet. {@link AuthenticationService}
     *
     * @return the redirect URI
     */
    String getRpRedirectURIAuth();

    /**
     * The path to the authorization endpoint of the OP
     *
     * @return the path to the endpoint
     */
    String getOpAuthorizationEndpoint();

    /**
     * The path to the token endpoint of the OP
     *
     * @return the path to the endpoint
     */
    String getOpTokenEndpoint();

    /**
     * The secret, which the backend client received from the OP on client registration.
     *
     * @return the secret
     */
    String getClientSecret();

    /**
     * The path to the JWK Set endpoint of the OP.
     *
     * @return the path to the endpoint
     */
    String getOpJwkSetEndpoint();

    /**
     * The used algorithm to encrypt communication with the OP.
     *
     * @return the used algorithm
     */
    String getJWSAlgortihm();

    /**
     * The scope to request during OIDC authorization flow. This is a space-separated list
     * of scope values, e.g. <code>openid offline</<code>. Scope values are case-sensitive!
     *
     * @return the scope
     */
    String getScope();

    /**
     * The path to the issuer endpoint of the OP
     *
     * @return the path to the endpoint
     */
    String getOpIssuer();

    /**
     * The response type used by the OP
     *
     * @return the used type
     */
    String getResponseType();

    /**
     * The path to the logout endpoint of the OP
     *
     * @return the path to the endpoint
     */
    String getOpLogoutEndpoint();

    /**
     * The path to the post OP logout location that should be used. Pointing to the
     * logout servlet. {@link LogoutService}
     *
     * @return the redirect URI
     */
    String getRpRedirectURIPostSSOLogout();

    /**
     * Is SSO logout enabled, triggers the confirmation procedure via OP if enabled.
     *
     * @return true or false
     */
    boolean isSSOLogout();

    /**
     * Where is the user supposed to be redirected to, after a successful logout.
     *
     * @return the redirect URI
     */
    String getRpRedirectURILogout();

    /**
     * Which logout mode is selected. Potential content can be found here {@link OIDCBackendConfig.AutologinMode}
     *
     * @return the selected mode.
     */
    String autologinCookieMode();

    /**
     * Is auto-login at all enabled or not
     *
     * @return true or false
     */
    boolean isAutologinEnabled();

    /**
     * How long before an OAuth access token expires a new token is supposed to be
     * requested, usually by exchanging a refresh token. In case refresh tokens are
     * not issued, this value should be 0 to align max. allowed session use time to
     * access token expiry.
     * <p>
     * <strong>Note that OAuth tokens contained in token responses are always set
     * as session parameters and internally validated on each request. If the
     * Authorization Server issues at least an access token with expiry date, this
     * date will determine how long the session can be used!</strong>
     * <p>A session containing an access token will be terminated on the first request
     * that happens after <code>expires_in - (getOauthRefreshTime() / 1000)</code>
     * seconds.
     * <p>
     * If a refresh token is contained in the token response, access tokens will be
     * refreshed during the first request that happens after <code>(expires_in - (getOauthRefreshTime() / 1000))</code>
     * seconds. A failed refreshed due an invalid/revoked refresh token or an
     * <code>invalid_grant</code> response will lead to the session being terminated.
     * <p>
     * If <code>expires_in</code> is not contained in the token response, the session
     * will live as long as the configured short-term session lifetime, no matter
     * if a refresh token is contained or not. The access token will never be refreshed
     * in that case.
     *
     * @return Time in milliseconds.
     */
    int getOauthRefreshTime();

    /**
     * Load the web ui web path for this backend.
     *
     * @return the ui web path.
     */
    String getUIWebpath();

    /**
     * Load this backends path, which is appended to the default /oidc/ path.
     *
     * @return
     */
    String getBackendPath();

    /**
     * Load all hosts separated by a comma, that this backend supports.
     *
     * @return
     */
    List<String> getHosts();

    /**
     * Load the redirect URI that should be used if a server error occurs that can be ignored.
     * For example a redirect to the IDP for another login attempt.
     *
     * @return
     */
    String getFailureRedirect();

    /**
     * Gets the name of the ID token claim that will be used by the
     * default backend to resolve a context.
     */
    String getContextLookupClaim();

    /**
     * Gets the {@link NamePart} of the ID token claim value used for
     * determining the context of a user.
     *
     * @see #getContextLookupClaim()
     */
    NamePart getContextLookupNamePart();

    /**
     * Gets the name of the ID token claim that will be used by the
     * default backend to resolve a user.
     *
     */
    String getUserLookupClaim();

    /**
     * Gets the {@link NamePart} of the ID token claim value used for
     * determining a user.
     *
     * @see #getUserLookupClaim()
     */
    NamePart getUserLookupNamePart();

    /**
     * Gets the {@link NamePart} to be used for an issued Resource
     * Owner Password Credentials Grant ({@link https://tools.ietf.org/html/rfc6749#section-4.3}).
     * The part is taken from the user-provided login name.
     *
     * @see OIDCProperty#enablePasswordGrant
     */
    NamePart getPasswordGrantUserNamePart();

    /**
     * Lock timeout before giving up trying to refresh an access token for
     * a session. If multiple threads try to check or refresh the access token
     * at the same time, only one gets a lock and blocks the others. In case
     * of a timeout, this is logged as a temporary issue and the request continued
     * as usual.
     */
    long getTokenLockTimeoutSeconds();

    /**
     * Gets whether token refresh should try to recover valid tokens from
     * the session instance that is present in {@link SessionStorageService}.
     * This is only tried as a fall-back, after token refresh failed with an
     * {@code invalid_grant} error.
     */
    boolean tryRecoverStoredTokens();

}
