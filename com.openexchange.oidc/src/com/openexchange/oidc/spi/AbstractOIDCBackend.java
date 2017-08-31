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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest.Builder;
import com.nimbusds.openid.connect.sdk.LogoutRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestImpl;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.authentication.Authenticated;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.impl.OIDCBackendConfigImpl;
import com.openexchange.oidc.impl.OIDCConfigImpl;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageService;

/**
 * Reference implementation of an OpenID backend.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public abstract class AbstractOIDCBackend implements OIDCBackend {

    protected static final String AUTH_RESPONSE = "auth_response";
    private static final Logger LOG = LoggerFactory.getLogger(AbstractOIDCBackend.class);
    private LoginConfiguration loginConfiguration;

    @Override
    public OIDCConfig getOIDCConfig() {
        return new OIDCConfigImpl(Services.getService(LeanConfigurationService.class));
    }

    @Override
    public OIDCBackendConfig getBackendConfig() {
        return new OIDCBackendConfigImpl(Services.getService(LeanConfigurationService.class));
    }
    
    @Override
    public void setLoginConfiguration(LoginConfiguration loginConfiguration) {
        this.loginConfiguration = loginConfiguration;
    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public OIDCExceptionHandler getExceptionHandler() {
        return new OIDCCoreExceptionHandler();
    }

    @Override
    public HTTPRequest getHttpRequest(HTTPRequest request) {
        return request;
    }

    @Override
    public ClientAuthentication getClientAuthentication() {
        ClientID clientID = new ClientID(this.getBackendConfig().getClientID());
        Secret clientSecret = new Secret(this.getBackendConfig().getClientSecret());
        return new ClientSecretBasic(clientID, clientSecret);
    }

    @Override
    public TokenRequest getTokenRequest(TokenRequest tokenRequest) {
        return tokenRequest;
    }

    @Override
    public JWSAlgorithm getJWSAlgorithm() throws OXException {
        JWSAlgorithm algorithm = JWSAlgorithm.RS256;
        String algorithmString = this.getBackendConfig().getJWSAlgortihm();
        if (algorithmString != null && !algorithmString.isEmpty()) {
            algorithm = this.getAlgorithmFromString(algorithmString);
        }
        return algorithm;
    }

    protected JWSAlgorithm getAlgorithmFromString(String algorithmString) throws OXException {
        JWSAlgorithm algorithm = JWSAlgorithm.parse(algorithmString);
        if (algorithm == null) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_JWS_ALGORITHM.create(algorithmString);
        }
        return algorithm;
    }

    @Override
    public AuthorizationRequest getAuthorisationRequest(Builder requestBuilder, HttpServletRequest request) {
        return requestBuilder.build();
    }

    @Override
    public IDTokenClaimsSet validateIdToken(JWT idToken, String nounce) throws OXException {
        IDTokenClaimsSet result = null;
        JWSAlgorithm expectedJWSAlg = this.getJWSAlgorithm();
        try {
            IDTokenValidator idTokenValidator = new IDTokenValidator(new Issuer(this.getBackendConfig().getIssuer()), new ClientID(this.getBackendConfig().getClientID()), expectedJWSAlg, new URL(this.getBackendConfig().getJwkSet()));
            result = idTokenValidator.validate(idToken, new Nonce(nounce));
        } catch (BadJOSEException e) {
            throw OIDCExceptionCode.IDTOKEN_VALIDATON_FAILED_CONTENT.create(e, "");
        } catch (JOSEException e) {
            throw OIDCExceptionCode.IDTOKEN_VALIDATON_FAILED.create(e, "");
        } catch (MalformedURLException e) {
            throw OIDCExceptionCode.IDTOKEN_VALIDATON_FAILED.create(e, "Unable to parse JWKSet URL");
        }
        return result;
    }

    @Override
    public Scope getScope() {
        String scopes = getBackendConfig().getScope();
        String[] scopeArray = scopes.split(";");
        return new Scope(scopeArray);
    }

    @Override
    public LoginRequest getLoginRequest(HttpServletRequest request, int userID, int contextID, LoginConfiguration loginConfiguration) throws OXException {
        String login = userID + "@" + contextID;
        String defaultClient = loginConfiguration.getDefaultClient();
        LoginRequestImpl parseLogin = LoginTools.parseLogin(request, login, null, false, defaultClient, loginConfiguration.isCookieForceHTTPS(), false);
        return parseLogin;
    }

    @Override
    public Authenticated enhanceAuthenticated(Authenticated defaultAuthenticated, Map<String, String> state) {
        return defaultAuthenticated;
    }

    @Override
    public String getLogoutFromIDPRequest(Session session) throws OXException {
        URI endSessionEndpoint = OIDCTools.getURIFromPath(this.getBackendConfig().getLogoutEndpoint());

        JWT idToken = null;
        try {
            idToken = JWTParser.parse((String) session.getParameter(OIDCTools.IDTOKEN));
        } catch (ParseException e) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_SESSIONS_IDTOKEN.create(e);
        }

        URI postLogoutTarget = OIDCTools.getURIFromPath(this.getBackendConfig().getRedirectURIPostSSOLogout());
        LogoutRequest logoutRequest = new LogoutRequest(endSessionEndpoint, idToken, postLogoutTarget, null);
        return logoutRequest.toURI().toString();
    }

    @Override
    public void finishLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String afterLogoutURI = this.getBackendConfig().getRedirectURILogout();
        if (!afterLogoutURI.isEmpty() && !response.isCommitted()) {
            response.sendRedirect(afterLogoutURI);
        }

    }

    @Override
    public AuthenticationInfo resolveAuthenticationResponse(HttpServletRequest request, OIDCTokenResponse tokenResponse) throws OXException {
        JWT idToken = tokenResponse.getOIDCTokens().getIDToken();
        String subject = "";
        try {
            JWTClaimsSet jwtClaimsSet = idToken.getJWTClaimsSet();
            subject = jwtClaimsSet.getSubject();
        } catch (java.text.ParseException e) {
            // TODO QS-VS: catch exception
            e.printStackTrace();
        }

        if (subject.isEmpty()) {
            throw OIDCExceptionCode.UNABLE_TO_LOAD_USERINFO.create();
        }
        AuthenticationInfo resultInfo = this.loadUserFromServer(subject);
        resultInfo.getProperties().put(AUTH_RESPONSE, tokenResponse.toJSONObject().toJSONString());
        return resultInfo;
    }

    private AuthenticationInfo loadUserFromServer(String subject) throws OXException {
        ContextService contextService = Services.getService(ContextService.class);
        //String[] userData = subject.split("@");
        //TODO QS-VS: auf die auskommentierte Abhandlung umstellen
        String[] userData = { "3", "wonderland.net" };
        if (userData.length != 2) {
            throw OIDCExceptionCode.BAD_SUBJECT.create(subject);
        }
        int contextId = contextService.getContextId(userData[1]);
        return new AuthenticationInfo(contextId, Integer.parseInt(userData[0]));
    }

    @Override
    public void updateSession(Session session, Map<String, String> tokenMap) throws OXException {
        Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
        if (lock == null) {
            lock = Session.EMPTY_LOCK;
        }
        lock.lock();
        try {
            OIDCTools.addParameterToSession(session, tokenMap, OIDCTools.IDTOKEN, OIDCTools.IDTOKEN);
            OIDCTools.addParameterToSession(session, tokenMap, OIDCTools.ACCESS_TOKEN, Session.PARAM_OAUTH_ACCESS_TOKEN);
            OIDCTools.addParameterToSession(session, tokenMap, OIDCTools.REFRESH_TOKEN, Session.PARAM_OAUTH_REFRESH_TOKEN);
            OIDCTools.addParameterToSession(session, tokenMap, OIDCTools.ACCESS_TOKEN_EXPIRY, Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE);

            SessionStorageService sessionStorageService = Services.getService(SessionStorageService.class);
            if (sessionStorageService != null) {
                sessionStorageService.addSession(session);
            } else {
                SessiondService sessiondService = Services.getService(SessiondService.class);
                sessiondService.storeSession(session.getSessionID());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean updateOauthTokens(Session session) throws OXException {
        AccessTokenResponse accessToken = loadAccessToken(session);
        if (accessToken.getTokens().getAccessToken() == null || accessToken.getTokens().getRefreshToken() == null) {
            return false;
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(OIDCTools.ACCESS_TOKEN, accessToken.getTokens().getAccessToken().getValue());
        tokenMap.put(OIDCTools.REFRESH_TOKEN, accessToken.getTokens().getRefreshToken().getValue());
        this.updateSession(session, tokenMap);
        return true;
    }

    //TODO QS-VS: Die Authentifizierung des Client wird öfters gebraucht, sollte ausgelagert werden
    private AccessTokenResponse loadAccessToken(Session session) throws OXException {
        RefreshToken refreshToken = new RefreshToken((String) session.getParameter(Session.PARAM_OAUTH_REFRESH_TOKEN));
        AuthorizationGrant refreshTokenGrant = new RefreshTokenGrant(refreshToken);
        ClientID clientID = new ClientID(this.getBackendConfig().getClientID());
        Secret clientSecret = new Secret(this.getBackendConfig().getClientSecret());
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        try {
            URI tokenEndpoint = new URI(this.getBackendConfig().getTokenEndpoint());

            TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, refreshTokenGrant);

            TokenResponse response = null;
            response = TokenResponse.parse(request.toHTTPRequest().send());

            if (!response.indicatesSuccess()) {
                TokenErrorResponse errorResponse = (TokenErrorResponse) response;
                throw OIDCExceptionCode.UNABLE_TO_RELOAD_ACCESSTOKEN.create(errorResponse.getErrorObject().getHTTPStatusCode() + " " + errorResponse.getErrorObject().getDescription());
            }
            return (AccessTokenResponse) response;
        } catch (URISyntaxException e) {
            throw OIDCExceptionCode.UNABLE_TO_RELOAD_ACCESSTOKEN.create(e);
        } catch (com.nimbusds.oauth2.sdk.ParseException e) {
            throw OIDCExceptionCode.UNABLE_TO_RELOAD_ACCESSTOKEN.create(e);
        } catch (IOException e) {
            throw OIDCExceptionCode.UNABLE_TO_RELOAD_ACCESSTOKEN.create(e);
        }
    }

    @Override
    public boolean tokensExpired(Session session) throws OXException {
        //TODO QS-VS: später mit echtem Code tauschen
        return true;
//        long oauthRefreshTime = this.getBackendConfig().getOauthRefreshTime();
//        long expiryDate = (long) session.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE);
//        return System.currentTimeMillis() >= (expiryDate - oauthRefreshTime);
    }
    
    private LoginConfiguration getLoginConfiguration() {
        if (this.loginConfiguration == null) {
            this.loginConfiguration = LoginServlet.getLoginConfiguration();
        }
        return this.loginConfiguration;
    }
    
    @Override
    public void logoutCurrentUser(Session session, HttpServletRequest request, HttpServletResponse response, LoginConfiguration loginConfiguration) throws OXException {
        LOG.debug("Try to Logout user for session {}", session.getSessionID());
        OIDCTools.validateSession(session, request);
        LoginPerformer.getInstance().doLogout(session.getSessionID());
        SessionUtility.removeOXCookies(session, request, response);
        SessionUtility.removeJSESSIONID(request, response);
        if (this.getBackendConfig().isAutologinEnabled()) {
            Cookie autologinCookie = OIDCTools.loadAutologinCookie(request, loginConfiguration != null ? loginConfiguration : this.getLoginConfiguration());
            SessionUtility.removeCookie(autologinCookie, "", autologinCookie.getDomain(), response);
        }
    }
}
