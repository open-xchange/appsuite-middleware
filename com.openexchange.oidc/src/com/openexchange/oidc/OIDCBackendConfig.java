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

/**
 * {@link OIDCBackendConfig}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.4
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
     * Get the redirect URI that should be used by the frontend to start the authentication
     * process. Pointing to the init-Servlet. {@link InitService}
     *
     * @return the redirect URI
     */
    String getRpRedirectURIInit();

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
     * The used OIDC scope.
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
     * How long before an Oauth Access token expires a new token is supposed to be
     * requested. Time in milliseconds.
     *
     * @return the time
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
}
