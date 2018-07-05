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
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
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
import com.openexchange.ajax.login.LoginRequestImpl;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
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
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.EnhancedAuthenticated;
import com.openexchange.session.reservation.Reservation;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;

/**
 * Reference implementation of an OpenID backend.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public abstract class AbstractOIDCBackend implements OIDCBackend {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractOIDCBackend.class);

    protected static final String AUTH_RESPONSE = "auth_response";

    private final AtomicReference<LoginConfiguration> loginConfigurationReference;

    protected AbstractOIDCBackend() {
        super();
        loginConfigurationReference = new AtomicReference<LoginConfiguration>(null);
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
    public IDTokenClaimsSet validateIdToken(JWT idToken, String nounce) throws OXException {
        LOG.trace("IDTokenClaimsSet validateIdToken(JWT idToken: {},String nounce: {})", idToken.getParsedString(), nounce);
        IDTokenClaimsSet result = null;
        JWSAlgorithm expectedJWSAlg = this.getJWSAlgorithm();
        try {
            IDTokenValidator idTokenValidator = new IDTokenValidator(new Issuer(this.getBackendConfig().getOpIssuer()), new ClientID(this.getBackendConfig().getClientID()), expectedJWSAlg, new URL(this.getBackendConfig().getOpJwkSetEndpoint()));
            result = idTokenValidator.validate(idToken, new Nonce(nounce));
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
        String scopes = this.getBackendConfig().getScope().toLowerCase();
        String[] scopeArray = Strings.splitBySemiColon(scopes);
        return new Scope(scopeArray);
    }

    @Override
    public LoginRequest getLoginRequest(HttpServletRequest request, int userID, int contextID, LoginConfiguration loginConfiguration) throws OXException {
        String login = userID + "@" + contextID;
        LOG.trace("getLoginRequest(...) login: {}", login);
        String defaultClient = loginConfiguration.getDefaultClient();
        LoginRequestImpl parseLogin = LoginTools.parseLogin(request, login, null, false, defaultClient, loginConfiguration.isCookieForceHTTPS(), false);
        return parseLogin;
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
        if (!afterLogoutURI.isEmpty() && !response.isCommitted()) {
            response.sendRedirect(afterLogoutURI);
        }
    }

    @Override
    public AuthenticationInfo resolveAuthenticationResponse(HttpServletRequest request, OIDCTokenResponse tokenResponse) throws OXException {
        JWT idToken = tokenResponse.getOIDCTokens().getIDToken();
        if (null == idToken) {
            throw OXException.general("Missing IDToken");
        }
        
        String subject = "";
        try {
            JWTClaimsSet jwtClaimsSet = idToken.getJWTClaimsSet();
            if (null == jwtClaimsSet) {
                throw OIDCExceptionCode.UNABLE_TO_LOAD_USERINFO.create("Failed to get the JWTClaimSet from idToken.");
            }
            subject = jwtClaimsSet.getSubject();
        } catch (java.text.ParseException e) {
            throw OIDCExceptionCode.UNABLE_TO_LOAD_USERINFO.create(e, "Failed to get the JWTClaimSet from idToken.");
        }

        if (Strings.isEmpty(subject)) {
            throw OIDCExceptionCode.UNABLE_TO_LOAD_USERINFO.create("unable to get a valid subject.");
        }
        AuthenticationInfo resultInfo = this.loadUserFromServer(subject);
        resultInfo.getProperties().put(AUTH_RESPONSE, tokenResponse.toJSONObject().toJSONString());
        return resultInfo;
    }

    private AuthenticationInfo loadUserFromServer(String subject) throws OXException {
        LOG.trace("loadUserFromServer(String subject: {})", subject);
        ContextService contextService = Services.getService(ContextService.class);
        String[] userData = subject.split("@");
        if (userData.length != 2) {
            throw OIDCExceptionCode.BAD_SUBJECT.create(subject);
        }
        int contextId = contextService.getContextId(userData[1]);
        return new AuthenticationInfo(contextId, Integer.parseInt(userData[0]));
    }

    @Override
    public void updateSession(Session session, Map<String, String> tokenMap) throws OXException {
        LOG.trace("updateSession(Session session: {}, Map<String, String> tokenMap.size(): {})", session.getSessionID(), tokenMap.size());
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
        LOG.trace("updateOauthTokens(Session session: {})", session.getSessionID());
        AccessTokenResponse accessToken = this.loadAccessToken(session);
        if (accessToken == null || accessToken.getTokens() == null || accessToken.getTokens().getAccessToken() == null || accessToken.getTokens().getRefreshToken() == null) {
            return false;
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(OIDCTools.ACCESS_TOKEN, accessToken.getTokens().getAccessToken().getValue());
        tokenMap.put(OIDCTools.REFRESH_TOKEN, accessToken.getTokens().getRefreshToken().getValue());
        long expiryDate = new Date().getTime();
        expiryDate += accessToken.getTokens().getAccessToken().getLifetime() * 1000;
        tokenMap.put(OIDCTools.ACCESS_TOKEN_EXPIRY, String.valueOf(expiryDate));
        this.updateSession(session, tokenMap);
        return true;
    }

    private AccessTokenResponse loadAccessToken(Session session) throws OXException {
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
        } catch (com.nimbusds.oauth2.sdk.ParseException e) {
            throw OIDCExceptionCode.UNABLE_TO_RELOAD_ACCESSTOKEN.create(e);
        } catch (IOException e) {
            throw OIDCExceptionCode.UNABLE_TO_RELOAD_ACCESSTOKEN.create(e);
        }
    }

    @Override
    public boolean isTokenExpired(Session session) throws OXException {
        LOG.trace("isTokenExpired(Session session: {})", session.getSessionID());
        long oauthRefreshTime = this.getBackendConfig().getOauthRefreshTime();
        if (!session.containsParameter(Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE)) {
            return false;
        }
        try {
            long expiryDate = Long.parseLong((String) session.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE));
            return System.currentTimeMillis() >= (expiryDate - oauthRefreshTime);
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            return true;
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
            SessionUtility.removeCookie(autologinCookie, "", autologinCookie.getDomain(), response);
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
        LOG.trace("handleException (HttpServletResponse response, boolean respondWithJson {}, OXException oxException {}, int sc {})", respondWithJson, oxException, sc);
        if (respondWithJson) {
            throw oxException;
        }
        response.sendError(sc);
    }

    private boolean performCookieLogin(HttpServletRequest request, HttpServletResponse response, Reservation reservation, boolean respondWithJson) throws IOException {
        LOG.trace("performCookieLogin(HttpServletRequest request: {}, HttpServletResponse response, Reservation reservation.token: {}, boolean respondWithJson)", request.getRequestURI(), reservation.getToken(), respondWithJson);
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

    private boolean getAutologinByCookieURL(HttpServletRequest request, HttpServletResponse response, Reservation reservation, Cookie oidcAtologinCookie, boolean respondWithJson) throws OXException, IOException {
        LOG.trace("getAutologinByCookieURL(HttpServletRequest request: {}, HttpServletResponse response, Reservation reservation.token: {}, Cookie oidcAtologinCookie: {}, boolean respondWithJson)", request.getRequestURI(), reservation.getToken(), oidcAtologinCookie != null ? oidcAtologinCookie.getValue() : "null", respondWithJson);
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
        json.putSafe("user_id", session.getUserId());
        json.putSafe("context_id", session.getContextId());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(Charsets.UTF_8_NAME);
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        json.write(writer);
        writer.flush();
    }

    private LoginResult loginUser(HttpServletRequest request, final Context context, final User user, final Map<String, String> state, final String oidcAutologinCookieValue) throws OXException {
        LOG.trace("loginUser(HttpServletRequest request: {}, final Context context: {}, final User user: {}, final Map<String, String> state.size: {}, final String oidcAutologinCookieValue: {})", request.getRequestURI(), context.getContextId(), user.getId(), state.size(), oidcAutologinCookieValue);
        final LoginRequest loginRequest = this.getLoginRequest(request, user.getId(), context.getContextId(), getLoginConfiguration());

        LoginResult loginResult = LoginPerformer.getInstance().doLogin(loginRequest, new HashMap<String, Object>(), new LoginMethodClosure() {

            @Override
            public Authenticated doAuthentication(LoginResultImpl loginResult) throws OXException {
                Authenticated authenticated = enhanceAuthenticated(getDefaultAuthenticated(context, user), state);

                EnhancedAuthenticated enhanced = new EnhancedAuthenticated(authenticated) {

                    @Override
                    protected void doEnhanceSession(Session session) {
                        LOG.trace("doEnhanceSession(Session session: {})", session.getSessionID());
                        if (oidcAutologinCookieValue != null) {
                            session.setParameter(OIDCTools.SESSION_COOKIE, oidcAutologinCookieValue);
                        }
                        session.setParameter(OIDCTools.IDTOKEN, state.get(OIDCTools.IDTOKEN));
                        OIDCTools.addParameterToSession(session, state, OIDCTools.ACCESS_TOKEN, Session.PARAM_OAUTH_ACCESS_TOKEN);
                        OIDCTools.addParameterToSession(session, state, OIDCTools.REFRESH_TOKEN, Session.PARAM_OAUTH_REFRESH_TOKEN);
                        OIDCTools.addParameterToSession(session, state, OIDCTools.ACCESS_TOKEN_EXPIRY, Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE);
                        session.setParameter(OIDCTools.BACKEND_PATH, getPath());
                    }
                };

                return enhanced;
            }
        });

        return loginResult;
    }

    private Authenticated getDefaultAuthenticated(final Context context, final User user) {
        LOG.trace("getDefaultAuthenticated(final Context context: {}, final User user: {})", context.getContextId(), user.getId());
        return new Authenticated() {

            @Override
            public String getUserInfo() {
                return user.getLoginInfo();
            }

            @Override
            public String getContextInfo() {
                return context.getLoginInfo()[0];
            }
        };
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
        LOG.trace("sendRedirect(Session session: {}, HttpServletRequest request: {}, HttpServletResponse response, boolean respondWithJson {})", session.getSessionID(), request.getRequestURI(), respondWithJson);
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
