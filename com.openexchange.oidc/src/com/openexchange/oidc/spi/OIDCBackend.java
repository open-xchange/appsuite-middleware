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

package com.openexchange.oidc.spi;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest.Builder;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.authentication.Authenticated;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCConfig;
import com.openexchange.oidc.state.AuthenticationRequestInfo;
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

    OIDCBackendConfig getBackendConfig();

    /**
     * Get the OpenID part of this backends servlet path.
     *
     * @return The path, never <code>null</code>
     */
    String getPath();

    /**
     * TODO QS-VS kommentieren
     *
     * @return The exception handler
     */
    OIDCExceptionHandler getExceptionHandler();

    HTTPRequest getHttpRequest(HTTPRequest request);

    ClientAuthentication getClientAuthentication();

    TokenRequest getTokenRequest(TokenRequest tokenRequest);

    JWSAlgorithm getJWSAlgorithm() throws OXException;

    AuthorizationRequest getAuthorisationRequest(Builder requestBuilder, HttpServletRequest request);

    IDTokenClaimsSet validateIdToken(JWT idToken, String nounce) throws OXException;

    Scope getScope();

    LoginRequest getLoginRequest(HttpServletRequest request, int userID, int contextID, LoginConfiguration loginConfiguration) throws OXException;

    AuthenticationInfo resolveAuthenticationResponse(HttpServletRequest request, OIDCTokenResponse tokens) throws OXException;

    Authenticated enhanceAuthenticated(Authenticated defaultAuthenticated, Map<String, String> state);

    String getLogoutFromIDPRequest(Session session) throws OXException;
    
    void finishLogout(HttpServletRequest request, HttpServletResponse response) throws IOException;
    
    void updateSession(Session session, Map<String, String> tokenMap) throws OXException;

    boolean updateOauthTokens(Session session) throws OXException;

    boolean tokensExpired(Session session) throws OXException;
}
