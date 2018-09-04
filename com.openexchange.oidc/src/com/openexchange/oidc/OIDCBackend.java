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

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest.Builder;
import com.nimbusds.openid.connect.sdk.LogoutRequest;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.authentication.Authenticated;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;
import com.openexchange.session.Session;

/**
 * Determines all features an OpenID backend must have to function correctly.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public interface OIDCBackend {

    /**
     * Get the configuration of the OpenID feature
     *
     * @return The configuration, never <code>null</code>
     */
    OIDCConfig getOIDCConfig();

    /**
     * Get this backends configuration.
     * 
     * @return the configuration
     */
    OIDCBackendConfig getBackendConfig();

    /**
     * Get the OpenID part of this backends servlet path.
     *
     * @return The path, never <code>null</code>
     */
    String getPath();

    /**
     * Get the Exception handler for this backend.
     *
     * @return The exception handler
     */
    OIDCExceptionHandler getExceptionHandler();

    /**
     * Process a given {@link HTTPRequest}
     * 
     * @param request The {@link HTTPRequest} to process
     * @return the processed request
     */
    HTTPRequest getHttpRequest(HTTPRequest request);

    /**
     * Load the {@link ClientAuthentication} for this backend.
     * 
     * @return the {@link ClientAuthentication} object
     */
    ClientAuthentication getClientAuthentication();

    /**
     * Process the given {@link TokenRequest}
     * 
     * @param tokenRequest The {@link TokenRequest} to process
     * @return the processed request
     */
    TokenRequest getTokenRequest(TokenRequest tokenRequest);

    /**
     * Load this backends used {@link JWSAlgorithm}.
     * 
     * @return The used {@link JWSAlgorithm}
     * @throws OXException
     */
    JWSAlgorithm getJWSAlgorithm() throws OXException;

    /**
     * Process the given {@link AuthenticationRequest.Builder} with the information from
     * the given {@link HttpServletRequest}.
     * 
     * @param requestBuilder The {@link AuthenticationRequest.Builder} to process
     * @param request The additional information in {@link HttpServletRequest} form
     * @return The processed {@link AuthorizationRequest}
     */
    AuthorizationRequest getAuthorisationRequest(Builder requestBuilder, HttpServletRequest request);

    /**
     * Validate the given idToken with the given nounce String.
     * 
     * @param idToken The {@link JWT} to validate
     * @param nounce The nounce to be used
     * @return A valid {@link IDTokenClaimsSet}
     * @throws OXException If something fails
     */
    IDTokenClaimsSet validateIdToken(JWT idToken, String nounce) throws OXException;

    /**
     * Load this backends scope.
     * 
     * @return The {@link Scope}
     */
    Scope getScope();

    /**
     * Build the login request to login the user on the OXServer.
     * 
     * @param request The {@link HttpServletRequest}
     * @param userID The userID of the user to login
     * @param contextID The contextID of the user to login
     * @param loginConfiguration The {@link LoginConfiguration} to use
     * @return The {@link LoginRequest} that can be used to login the user
     * @throws OXException If something fails
     */
    LoginRequest getLoginRequest(HttpServletRequest request, int userID, int contextID, LoginConfiguration loginConfiguration) throws OXException;

    /**
     * Resolve the given {@link OIDCTokenResponse} to an {@link AuthenticationInfo} with all needed
     * user information.
     * 
     * @param request The {@link HttpServletRequest} with additional information
     * @param tokens The {@link OIDCTokenResponse} with additional information
     * @return The {@link AuthenticationInfo}
     * @throws OXException If something fails
     */
    AuthenticationInfo resolveAuthenticationResponse(HttpServletRequest request, OIDCTokenResponse tokens) throws OXException;

    /**
     * Enhance the given {@link Authenticated} with potential given states.
     * 
     * @param defaultAuthenticated The {@link Authenticated} to enhance
     * @param state The {@link Map} with potential information
     * @return The enhanced {@link Authenticated}
     */
    Authenticated enhanceAuthenticated(Authenticated defaultAuthenticated, Map<String, String> state);

    /**
     * Get the OP logout request for the given {@link Session}.
     * 
     * @param session The {@link Session} that should be terminated.
     * @return The {@link LogoutRequest} to logout a user from the OP
     * @throws OXException If anything fails
     */
    LogoutRequest getLogoutFromIDPRequest(Session session) throws OXException;
    
    /**
     * Finishing touches that should be applied after a logout.
     * 
     * @param request The {@link HttpServletRequest}
     * @param response The {@link HttpServletResponse}
     * @throws IOException If anything fails
     */
    void finishLogout(HttpServletRequest request, HttpServletResponse response) throws IOException;
    
    /**
     * Update the given {@link Session} with the given Access and Refresh OAuth tokens.
     * 
     * @param session The {@link Session}, that should be updated
     * @param tokenMap The {@link Map} with the tokens that should be wrote into the session
     * @throws OXException If anything fails
     */
    void updateSession(Session session, Map<String, String> tokenMap) throws OXException;

    /**
     * Triggers the OAuth token update mechanism for the given {@link Session}.
     * 
     * @param session The {@link Session} which OAuth tokens should be updated.
     * @return true if everything went fine, false otherwise
     * @throws OXException If anything fails
     */
    boolean updateOauthTokens(Session session) throws OXException;

    /**
     * Determines if the given {@link Session}s {@link AccessToken} is expired.
     * 
     * @param session The {@link Session} which OAuth tokens should be checked
     * @return true, if the {@link AccessToken} expired. false otherwise
     * @throws OXException If anything fails
     */
    boolean isTokenExpired(Session session) throws OXException;

    /**
     * Set this backends {@link LoginConfiguration}.
     * 
     * @param loginConfiguration The {@link LoginConfiguration} to set
     */
    void setLoginConfiguration(LoginConfiguration loginConfiguration);

    /**
     * Logout the user to whom the given {@link Session} belongs.
     * 
     * @param session The {@link Session} that should be terminated.
     * @param request The {@link HttpServletRequest}
     * @param response The {@link HttpServletResponse}
     * @throws OXException
     */
    void logoutCurrentUser(Session session, HttpServletRequest request, HttpServletResponse response) throws OXException;

    /**
     * Perform an Appsuite login. If autologin is enabled, the user will be logged in into his session. The
     * autologin mechanism will check if a valid OIDCCookie is available with all needed information. The 
     * method will redirect the user to the Appsuite UI afterwards. If <code>respondWithJson</code> is set true,
     * the redirect location will be wrapped into a JSON Object.
     * 
     * @param request The {@link HttpServletRequest}
     * @param response The {@link HttpServletResponse}
     * @param respondWithJson Should the UI location should be wrapped into a JSON Object or not
     * @throws IOException
     * @throws OXException
     * @throws JSONException
     */
    void performLogin(HttpServletRequest request, HttpServletResponse response, boolean respondWithJson) throws IOException, OXException, JSONException;
}
