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

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
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
import com.nimbusds.oauth2.sdk.id.State;
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
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginMethodClosure;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.oidc.AuthenticationInfo;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCBackendConfig.AutologinMode;
import com.openexchange.oidc.OIDCConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.OIDCExceptionHandler;
import com.openexchange.oidc.impl.OIDCBackendConfigImpl;
import com.openexchange.oidc.impl.OIDCConfigImpl;
import com.openexchange.oidc.impl.OIDCTokenRefresher;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Session;
import com.openexchange.session.SessionDescription;
import com.openexchange.session.oauth.OAuthTokens;
import com.openexchange.session.oauth.RefreshResult;
import com.openexchange.session.oauth.SessionOAuthTokenService;
import com.openexchange.session.oauth.TokenRefreshConfig;
import com.openexchange.session.reservation.EnhancedAuthenticated;
import com.openexchange.session.reservation.Reservation;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * Reference implementation of an OpenID backend.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public abstract class AbstractOIDCBackend implements OIDCBackend {

    /** The logger constant */
    static final Logger LOG = LoggerFactory.getLogger(AbstractOIDCBackend.class);

    protected static final String AUTH_RESPONSE = "auth_response";

    private final AtomicReference<LoginConfiguration> loginConfigurationReference;

    protected AbstractOIDCBackend() {
        super();
        loginConfigurationReference = new AtomicReference<>(null);
    }

    @Override
    public OIDCConfig getOIDCConfig() {
        return new OIDCConfigImpl(Services.getService(LeanConfigurationService.class));
    }

    @Override
    public OIDCBackendConfig getBackendConfig() {
        return new OIDCBackendConfigImpl(Services.getService(LeanConfigurationService.class), "");
    }

    @Override
    public void setLoginConfiguration(LoginConfiguration loginConfiguration) {
        loginConfigurationReference.set(loginConfiguration);
    }

    @Override
    public String getPath() {
        return this.getBackendConfig().getBackendPath();
    }

    @Override
    public OIDCExceptionHandler getExceptionHandler() {
        return new OIDCCoreExceptionHandler(this.getBackendConfig());
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
        if (algorithmString != null && Strings.isNotEmpty(algorithmString)) {
            algorithm = this.getAlgorithmFromString(algorithmString);
        }
        LOG.trace("getJWSAlgorithm() result: {}", algorithm.getName());
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
    public IDTokenClaimsSet validateIdToken(JWT idToken, String nonce) throws OXException {
        LOG.trace("IDTokenClaimsSet validateIdToken(JWT idToken: {},String nonce: {})", idToken.getParsedString(), nonce);
        IDTokenClaimsSet result = null;
        JWSAlgorithm expectedJWSAlg = this.getJWSAlgorithm();
        try {
            IDTokenValidator idTokenValidator = new IDTokenValidator(new Issuer(this.getBackendConfig().getOpIssuer()), new ClientID(this.getBackendConfig().getClientID()), expectedJWSAlg, new URL(this.getBackendConfig().getOpJwkSetEndpoint()));
            result = idTokenValidator.validate(idToken, Strings.isEmpty(nonce) ? null : new Nonce(nonce));
        } catch (BadJOSEException e) {
            throw OIDCExceptionCode.IDTOKEN_VALIDATON_FAILED_CONTENT.create(e, e.getMessage());
        } catch (JOSEException e) {
            throw OIDCExceptionCode.IDTOKEN_VALIDATON_FAILED.create(e, "");
        } catch (MalformedURLException e) {
            throw OIDCExceptionCode.IDTOKEN_VALIDATON_FAILED.create(e, "Unable to parse JWKSet URL");
        }
        return result;
    }

    @Override
    public Scope getScope() {
        String scope = this.getBackendConfig().getScope();
        if (scope.contains(";")) {
            // legacy compatibility
            String[] scopeValues = Strings.splitBySemiColon(scope.toLowerCase());
            return new Scope(scopeValues);
        }

        return Scope.parse(scope);
    }

    @Override
    public LoginRequest getLoginRequest(HttpServletRequest request, int userID, int contextID, LoginConfiguration loginConfiguration) throws OXException {
        String login = userID + "@" + contextID;
        LOG.trace("getLoginRequest(...) login: {}", login);
        String defaultClient = loginConfiguration.getDefaultClient();
        return LoginTools.parseLogin(request, login, null, false, defaultClient, loginConfiguration.isCookieForceHTTPS(), false);
    }

    @Override
    public Authenticated enhanceAuthenticated(Authenticated defaultAuthenticated, Map<String, String> state) {
        return defaultAuthenticated;
    }

    @Override
    public LogoutRequest getLogoutFromIDPRequest(Session session) throws OXException {
        LOG.trace("getLogoutFromIDPRequest(Session session: {})", session.getSessionID());
        URI endSessionEndpoint = OIDCTools.getURIFromPath(this.getBackendConfig().getOpLogoutEndpoint());

        JWT idToken = null;
        try {
            idToken = JWTParser.parse((String) session.getParameter(OIDCTools.IDTOKEN));
        } catch (ParseException e) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_SESSIONS_IDTOKEN.create(e);
        }

        URI postLogoutTarget = OIDCTools.getURIFromPath(this.getBackendConfig().getRpRedirectURIPostSSOLogout());
        LogoutRequest logoutRequest = new LogoutRequest(endSessionEndpoint, idToken, postLogoutTarget, new State());
        LOG.trace("final logout request: {}", logoutRequest.toURI().toString());
        return logoutRequest;
    }

    @Override
    public void finishLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String afterLogoutURI = this.getBackendConfig().getRpRedirectURILogout();
        if (Strings.isNotEmpty(afterLogoutURI) && !response.isCommitted()) {
            response.sendRedirect(afterLogoutURI);
        }
    }

    @Override
    public AuthenticationInfo resolveAuthenticationResponse(HttpServletRequest request, OIDCTokenResponse tokenResponse) throws OXException {
        return resolveAuthenticationResponse(tokenResponse);
    }

    @Override
    public AuthenticationInfo resolveAuthenticationResponse(LoginInfo loginInfo, OIDCTokenResponse tokenResponse) throws OXException {
        return resolveAuthenticationResponse(tokenResponse);
    }

    private AuthenticationInfo resolveAuthenticationResponse(OIDCTokenResponse tokenResponse) throws OXException {
        JWT idToken = tokenResponse.getOIDCTokens().getIDToken();
        if (null == idToken) {
            throw OIDCExceptionCode.UNABLE_TO_LOAD_USERINFO.create("Missing IDToken");
        }

        LOG.debug("Trying to resolve user for ID token: {}", idToken.serialize());
        OIDCBackendConfig config = getBackendConfig();
        String contextClaim;
        String userClaim;
        try {
            JWTClaimsSet jwtClaimsSet = idToken.getJWTClaimsSet();
            if (null == jwtClaimsSet) {
                throw OIDCExceptionCode.UNABLE_TO_LOAD_USERINFO.create("Failed to get the JWTClaimSet from idToken.");
            }

            contextClaim = jwtClaimsSet.getStringClaim(config.getContextLookupClaim());
            if (Strings.isEmpty(contextClaim)) {
                throw OIDCExceptionCode.UNABLE_TO_LOAD_USERINFO.create("Unable to get a valid context claim: " + config.getContextLookupClaim());
            }

            userClaim = jwtClaimsSet.getStringClaim(config.getUserLookupClaim());
            if (Strings.isEmpty(userClaim)) {
                throw OIDCExceptionCode.UNABLE_TO_LOAD_USERINFO.create("Unable to get a valid user claim: " + config.getUserLookupClaim());
            }
        } catch (java.text.ParseException e) {
            throw OIDCExceptionCode.UNABLE_TO_LOAD_USERINFO.create(e, "Failed to parse claim from idToken.");
        }

        String contextName = config.getContextLookupNamePart().getFrom(contextClaim, Authenticated.DEFAULT_CONTEXT_INFO);
        String userName = config.getUserLookupNamePart().getFrom(userClaim, userClaim);

        AuthenticationInfo resultInfo = this.loadUserFromServer(contextName, userName);
        resultInfo.setProperty(AUTH_RESPONSE, tokenResponse.toJSONObject().toJSONString());
        return resultInfo;
    }

    private AuthenticationInfo loadUserFromServer(String contextInfo, String userName) throws OXException {
        LOG.trace("loadUserFromServer(String contextName: {}, String userName: {})", contextInfo, userName);
        ContextService contextService = Services.getService(ContextService.class);
        UserService userService = Services.getService(UserService.class);
        int contextId = contextService.getContextId(contextInfo);
        if (contextId < 0) {
            LOG.debug("Unknown context for login mapping '{}'", contextInfo);
            throw LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING.create(contextInfo);
        }

        int userId = userService.getUserId(userName, contextService.getContext(contextId));
        AuthenticationInfo authInfo = new AuthenticationInfo(contextId, userId);
        authInfo.setProperty(OIDCTools.CONTEXT_LOGIN_INFO, contextInfo);
        authInfo.setProperty(OIDCTools.USER_LOGIN_INFO, userName);
        return authInfo;
    }

    @Override
    public void updateSession(Session session, Map<String, String> tokenMap) throws OXException {
        LOG.trace("updateSession(Session session: {}, Map<String, String> tokenMap.size(): {})", session.getSessionID(), I(tokenMap.size()));
        Optional<OAuthTokens> tokens = OIDCTools.convertTokenMap(tokenMap);
        if (tokens.isPresent()) {
            SessionOAuthTokenService tokenService = Services.getService(SessionOAuthTokenService.class);
            try {
                tokenService.setInSessionAtomic(session, tokens.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw OXException.general("Interrupted", e);
            }
        } else {
            LOG.warn("Cannot update OAuth tokens session due to missing access token");
        }

        OIDCTools.addParameterToSession(session, tokenMap, OIDCTools.IDTOKEN, OIDCTools.IDTOKEN);
        String backendPath = tokenMap.get(OIDCTools.BACKEND_PATH);
        if (backendPath != null) {
            session.setParameter(OIDCTools.BACKEND_PATH, backendPath);
        }

        SessionStorageService sessionStorageService = Services.getService(SessionStorageService.class);
        if (sessionStorageService != null) {
            sessionStorageService.addSession(session);
        } else {
            SessiondService sessiondService = Services.getService(SessiondService.class);
            sessiondService.storeSession(session.getSessionID());
        }
    }

    /**
     * @deprecated Use {@link SessionOAuthTokenService} and {@link OIDCTokenRefresher}
     */
    @Deprecated
    @Override
    public boolean updateOauthTokens(Session session) throws OXException {
        LOG.trace("updateOauthTokens(Session session: {})", session.getSessionID());
        SessionOAuthTokenService tokenService = Services.getService(SessionOAuthTokenService.class);
        try {
            OIDCTokenRefresher refresher = new OIDCTokenRefresher(this, session);
            TokenRefreshConfig refreshConfig = OIDCTools.getTokenRefreshConfig(getBackendConfig());
            RefreshResult result = tokenService.checkOrRefreshTokens(session, refresher, refreshConfig);
            if (result.isSuccess()) {
                return true;
            }

            if (result.hasException()) {
                LOG.warn("Error while refreshing oauth tokens for session '{}': {}", session.getSessionID(), result.getErrorDesc(), result.getException());
            } else {
                LOG.warn("Error while refreshing oauth tokens for session '{}': {}", session.getSessionID(), result.getErrorDesc());
            }

            return false;
        } catch (InterruptedException e) {
            LOG.warn("Thread was interrupted while checking session oauth tokens");
            // keep interrupted state
            Thread.currentThread().interrupt();
            throw OXException.general("Interrupted", e);
        }
    }

    /**
     * @deprecated Use {@link SessionOAuthTokenService} and {@link OIDCTokenRefresher}
     */
    @Deprecated
    protected AccessTokenResponse loadAccessToken(Session session) throws OXException {
        LOG.trace("loadAccessToken(Session session: {})", session.getSessionID());
        RefreshToken refreshToken = new RefreshToken((String) session.getParameter(Session.PARAM_OAUTH_REFRESH_TOKEN));
        AuthorizationGrant refreshTokenGrant = new RefreshTokenGrant(refreshToken);
        ClientAuthentication clientAuth = this.getClientAuthentication();
        try {
            URI tokenEndpoint = OIDCTools.getURIFromPath(this.getBackendConfig().getOpTokenEndpoint());

            TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, refreshTokenGrant);
            HTTPRequest httpRequest = getHttpRequest(request.toHTTPRequest());
            LOG.trace("Build TokenRequest to get tokens from OP: {} {}", httpRequest.getURL().toString(), httpRequest.getQuery());
            TokenResponse response = null;
            response = TokenResponse.parse(httpRequest.send());

            if (!response.indicatesSuccess()) {
                TokenErrorResponse errorResponse = (TokenErrorResponse) response;
                throw OIDCExceptionCode.UNABLE_TO_RELOAD_ACCESSTOKEN.create(errorResponse.getErrorObject().getHTTPStatusCode() + " " + errorResponse.getErrorObject().getDescription());
            }
            return (AccessTokenResponse) response;
        } catch (com.nimbusds.oauth2.sdk.ParseException | IOException e) {
            throw OIDCExceptionCode.UNABLE_TO_RELOAD_ACCESSTOKEN.create(e, e.getMessage());
        }
    }

    @Override
    public boolean isTokenExpired(Session session) throws OXException {
        LOG.trace("isTokenExpired(Session session: {})", session.getSessionID());
        SessionOAuthTokenService tokenService = Services.getService(SessionOAuthTokenService.class);
        try {
            Optional<OAuthTokens> optTokens = tokenService.getFromSessionAtomic(session);
            if (!optTokens.isPresent()) {
                return false;
            }

            return optTokens.get().accessExpiresWithin(this.getBackendConfig().getOauthRefreshTime(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
         // keep interrupted state
            Thread.currentThread().interrupt();
            throw OXException.general("Interrupted", e);
        }
    }

    /**
     * Gets the assigned login configuration or falls-back to {@link LoginServlet#getLoginConfiguration()}.
     *
     * @return The effective login configuration
     */
    protected LoginConfiguration getLoginConfiguration() {
        LoginConfiguration loginConfiguration = loginConfigurationReference.get();
        return null == loginConfiguration ? LoginServlet.getLoginConfiguration() : loginConfiguration;
    }

    @Override
    public void logoutCurrentUser(Session session, HttpServletRequest request, HttpServletResponse response) throws OXException {
        LOG.debug("Try to Logout user for session {}", session.getSessionID());
        OIDCTools.validateSession(session, request);
        LoginPerformer.getInstance().doLogout(session.getSessionID());
        SessionUtility.removeOXCookies(session, request, response);
        SessionUtility.removeJSESSIONID(request, response);
        if (this.getBackendConfig().isAutologinEnabled()) {
            Cookie autologinCookie = OIDCTools.loadAutologinCookie(request, getLoginConfiguration());
            if (autologinCookie != null) {
                SessionUtility.removeCookie(autologinCookie, "", autologinCookie.getDomain(), response);
            }
        }
    }

    @Override
    public void performLogin(HttpServletRequest request, HttpServletResponse response, boolean respondWithJson) throws IOException, OXException, JSONException {
        LOG.trace("performLogin(HttpServletRequest request: {}, HttpServletResponse response)", request.getRequestURI());
        String sessionToken = request.getParameter(OIDCTools.SESSION_TOKEN);
        LOG.trace("Login user with session token: {}", sessionToken);
        if (Strings.isEmpty(sessionToken)) {
            handleException(response, respondWithJson, AjaxExceptionCodes.BAD_REQUEST.create(), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        SessionReservationService sessionReservationService = Services.getService(SessionReservationService.class);
        Reservation reservation = sessionReservationService.removeReservation(sessionToken);
        if (null == reservation) {
            handleException(response, respondWithJson, LoginExceptionCodes.INVALID_CREDENTIALS.create(), HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idToken = reservation.getState().get(OIDCTools.IDTOKEN);
        if (Strings.isEmpty(idToken)) {
            handleException(response, respondWithJson, AjaxExceptionCodes.BAD_REQUEST.create(), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        ContextService contextService = Services.getService(ContextService.class);
        Context context = contextService.getContext(reservation.getContextId());
        if (!context.isEnabled()) {
            handleException(response, respondWithJson, LoginExceptionCodes.INVALID_CREDENTIALS.create(), HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        UserService userService = Services.getService(UserService.class);
        User user = userService.getUser(reservation.getUserId(), context);
        if (!user.isMailEnabled()) {
            handleException(response, respondWithJson, LoginExceptionCodes.INVALID_CREDENTIALS.create(), HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        boolean autologinEnabled = this.getBackendConfig().isAutologinEnabled();

        String autologinCookieValue = null;
        if (autologinEnabled) {
            LOG.trace("Try OIDC auto-login with a cookie");
            if (this.performCookieLogin(request, response, reservation, respondWithJson)) {
                return;
            }
            autologinCookieValue = UUIDs.getUnformattedString(UUID.randomUUID());
        }

        LoginResult result = this.loginUser(request, context, user, reservation.getState(), autologinCookieValue);
        Session session = this.performSessionAdditions(result, request, response, idToken);

        if (autologinEnabled) {
            response.addCookie(this.createOIDCAutologinCookie(request, session, autologinCookieValue));
        }

        this.sendRedirect(session, request, response, respondWithJson);
    }

    private void handleException(HttpServletResponse response, boolean respondWithJson, OXException oxException, int sc) throws OXException, IOException {
        LOG.trace("handleException (HttpServletResponse response, boolean respondWithJson {}, OXException oxException {}, int sc {})", respondWithJson ? Boolean.TRUE : Boolean.FALSE, oxException, I(sc));
        if (respondWithJson) {
            throw oxException;
        }
        response.sendError(sc);
    }

    private boolean performCookieLogin(HttpServletRequest request, HttpServletResponse response, Reservation reservation, boolean respondWithJson) throws IOException {
        LOG.trace("performCookieLogin(HttpServletRequest request: {}, HttpServletResponse response, Reservation reservation.token: {}, boolean respondWithJson)", request.getRequestURI(), reservation.getToken(), respondWithJson ? Boolean.TRUE : Boolean.FALSE);
        AutologinMode autologinMode = OIDCBackendConfig.AutologinMode.get(this.getBackendConfig().autologinCookieMode());

        if (autologinMode == OIDCBackendConfig.AutologinMode.SSO_REDIRECT) {
            try {
                Cookie autologinCookie = OIDCTools.loadAutologinCookie(request, getLoginConfiguration());
                return this.getAutologinByCookieURL(request, response, reservation, autologinCookie, respondWithJson);
            } catch (OXException e) {
                LOG.debug("Ignoring OIDC auto-login attempt due to failed IP or secret check", e);
            }
        }
        return false;
    }

    private boolean getAutologinByCookieURL(HttpServletRequest request, HttpServletResponse response, Reservation reservation, Cookie oidcAtologinCookie, boolean respondWithJson) throws IOException {
        LOG.trace("getAutologinByCookieURL(HttpServletRequest request: {}, HttpServletResponse response, Reservation reservation.token: {}, Cookie oidcAtologinCookie: {}, boolean respondWithJson)", request.getRequestURI(), reservation.getToken(), oidcAtologinCookie != null ? oidcAtologinCookie.getValue() : "null", respondWithJson ? Boolean.TRUE : Boolean.FALSE);
        if (oidcAtologinCookie != null) {
            try {
                Session session = OIDCTools.getSessionFromAutologinCookie(oidcAtologinCookie, request);
                if (session != null) {
                    this.updateSession(session, reservation.getState());
                    // the getRedirectLocationForSession does also the validation check of the session
                    if (respondWithJson) {
                        this.writeSessionDataAsJson(session, response);
                    } else {
                        String redirectLocationForSession = this.getRedirectLocationForSession(request, session, reservation);
                        response.sendRedirect(redirectLocationForSession);
                    }
                    return true;
                }
                //No session found, log that
                LOG.debug("No session found for OIDC Cookie with value: {}", oidcAtologinCookie.getValue());
            } catch (OXException | JSONException e) {
                LOG.debug("Ignoring OIDC auto-login attempt due to failed IP or secret check", e);
            }

            Cookie toRemove = (Cookie) oidcAtologinCookie.clone();
            toRemove.setMaxAge(0);
            response.addCookie(toRemove);
        }
        return false;
    }

    private String getRedirectLocationForSession(HttpServletRequest request, Session session, Reservation reservation) throws OXException, IOException {
        LOG.trace("getRedirectLocationForSession(HttpServletRequest request: {}, Session session: {}, Reservation reservation: {})", request.getRequestURI(), session.getSessionID(), reservation.getToken());
        OIDCTools.validateSession(session, request);
        if (session.getContextId() != reservation.getContextId() && session.getUserId() != reservation.getUserId()) {
            this.handleException(null, true, LoginExceptionCodes.LOGIN_DENIED.create(), 0);
        }
        return OIDCTools.buildFrontendRedirectLocation(session, OIDCTools.getUIWebPath(getLoginConfiguration(), this.getBackendConfig()), request.getParameter(OIDCTools.PARAM_DEEP_LINK));
    }

    private void writeSessionDataAsJson(Session session, HttpServletResponse response) throws JSONException, IOException {
        LOG.trace("writeSessionDataAsJson(Session session {}, HttpServletResponse response)", session.getSessionID());
        JSONObject json = new JSONObject();
        json.putSafe("session", session.getSessionID());
        json.putSafe("user_id", I(session.getUserId()));
        json.putSafe("context_id", I(session.getContextId()));
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(Charsets.UTF_8_NAME);
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        json.write(writer);
        writer.flush();
    }

    private LoginResult loginUser(HttpServletRequest request, final Context context, final User user, final Map<String, String> state, final String oidcAutologinCookieValue) throws OXException {
        LOG.trace("loginUser(HttpServletRequest request: {}, final Context context: {}, final User user: {}, final Map<String, String> state.size: {}, final String oidcAutologinCookieValue: {})", request.getRequestURI(), I(context.getContextId()), I(user.getId()), I(state.size()), oidcAutologinCookieValue);
        final LoginRequest loginRequest = this.getLoginRequest(request, user.getId(), context.getContextId(), getLoginConfiguration());

        return LoginPerformer.getInstance().doLogin(loginRequest, new HashMap<String, Object>(), new LoginMethodClosure() {

            @Override
            public Authenticated doAuthentication(LoginResultImpl loginResult) throws OXException {
                Authenticated authenticated = enhanceAuthenticated(OIDCTools.getDefaultAuthenticated(context, user, state), state);

                return new EnhancedAuthenticated(authenticated) {

                    @Override
                    protected void doEnhanceSession(Session ses) {
                        LOG.trace("doEnhanceSession(Session session: {})", ses.getSessionID());
                        SessionDescription session = (SessionDescription) ses;
                        session.setStaySignedIn(false);
                        if (oidcAutologinCookieValue != null) {
                            session.setParameter(OIDCTools.SESSION_COOKIE, oidcAutologinCookieValue);
                        }

                        Map<String, String> params = new HashMap<>(state);
                        params.put(OIDCTools.BACKEND_PATH, getPath());
                        OIDCTools.setSessionParameters(session, params);
                    }
                };
            }
        });
    }

    private Session performSessionAdditions(LoginResult loginResult, HttpServletRequest request, HttpServletResponse response, String idToken) throws OXException {
        LOG.trace("performSessionAdditions(LoginResult loginResult.sessionID: {}, HttpServletRequest request: {}, HttpServletResponse response, String idToken: {})", loginResult.getSession().getSessionID(), request.getRequestURI(), idToken);
        Session session = loginResult.getSession();

        LoginServlet.addHeadersAndCookies(loginResult, response);

        SessionUtility.rememberSession(request, new ServerSessionAdapter(session));

        LoginServlet.writeSecretCookie(request, response, session, session.getHash(), request.isSecure(), request.getServerName(), getLoginConfiguration());

        return session;
    }

    private Cookie createOIDCAutologinCookie(HttpServletRequest request, Session session, String uuid) throws OXException {
        LOG.trace("createOIDCAutologinCookie(HttpServletRequest request: {}, Session session: {}, String uuid: {})", request.getRequestURI(), session.getSessionID(), uuid);
        String hash = OIDCTools.calculateHash(request, getLoginConfiguration());
        Cookie oidcAutologinCookie = new Cookie(OIDCTools.AUTOLOGIN_COOKIE_PREFIX + hash, uuid);
        oidcAutologinCookie.setPath("/");
        oidcAutologinCookie.setSecure(Tools.considerSecure(request));
        oidcAutologinCookie.setMaxAge(-1);

        String domain = OIDCTools.getDomainName(request, Services.getOptionalService(HostnameService.class));
        String cookieDomain = Cookies.getDomainValue(domain);
        if (cookieDomain != null) {
            oidcAutologinCookie.setDomain(cookieDomain);
        }
        return oidcAutologinCookie;
    }

    private void sendRedirect(Session session, HttpServletRequest request, HttpServletResponse response, boolean respondWithJson) throws IOException, JSONException {
        LOG.trace("sendRedirect(Session session: {}, HttpServletRequest request: {}, HttpServletResponse response, boolean respondWithJson {})", session.getSessionID(), request.getRequestURI(), respondWithJson ? Boolean.TRUE : Boolean.FALSE);
        if (respondWithJson) {
            this.writeSessionDataAsJson(session, response);
        } else {
            String uiWebPath = OIDCTools.getUIWebPath(getLoginConfiguration(), this.getBackendConfig());
            // get possible deeplink
            String frontendRedirectLocation = OIDCTools.buildFrontendRedirectLocation(session, uiWebPath, request.getParameter(OIDCTools.PARAM_DEEP_LINK));
            response.sendRedirect(frontendRedirectLocation);
        }
    }
}
