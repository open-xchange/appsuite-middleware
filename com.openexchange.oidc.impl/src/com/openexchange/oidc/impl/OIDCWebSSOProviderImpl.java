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
package com.openexchange.oidc.impl;

import static com.openexchange.oidc.tools.OIDCTools.tokeRequestInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest.Builder;
import com.nimbusds.openid.connect.sdk.LogoutRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.nimbusds.oauth2.sdk.http.send.HTTPSender;
import com.openexchange.oidc.AuthenticationInfo;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCBackendConfig.AutologinMode;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.OIDCWebSSOProvider;
import com.openexchange.oidc.http.outbound.OIDCHttpClientConfig;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.state.AuthenticationRequestInfo;
import com.openexchange.oidc.state.LogoutRequestInfo;
import com.openexchange.oidc.state.StateManagement;
import com.openexchange.oidc.state.impl.DefaultAuthenticationRequestInfo;
import com.openexchange.oidc.state.impl.DefaultLogoutRequestInfo;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.oauth.RefreshResult;
import com.openexchange.session.oauth.SessionOAuthTokenService;
import com.openexchange.session.oauth.TokenRefreshConfig;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.ExpirationReason;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.http.Tools;


/**
 * Default implementation of the {@link OIDCWebSSOProvider} features
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCWebSSOProviderImpl implements OIDCWebSSOProvider {

    private static final String GET_THE_ID_TOKEN = "get the IDToken";
    private static final Logger LOG = LoggerFactory.getLogger(OIDCWebSSOProviderImpl.class);
    private final OIDCBackend backend;
    private final StateManagement stateManagement;
    private final SessionReservationService sessionReservationService;
    private final ServiceLookup services;
    private final LoginConfiguration loginConfiguration;

    /**
     * The number of milliseconds for which a LogoutRequest sent by us is considered valid (5 minutes).
     */
    private static final long LOGOUT_REQUEST_TIMEOUT = 5 * 60 * 1000l;

    /**
     * The number of milliseconds for which an AuthnRequestInfo is remembered (5 minutes).
     */
    private static final long AUTHN_REQUEST_TIMEOUT = 5 * 60 * 1000l;


    public OIDCWebSSOProviderImpl(OIDCBackend backend, StateManagement stateManagement, ServiceLookup services, LoginConfiguration loginConfiguration) {
        super();
        this.backend = backend;
        this.stateManagement = stateManagement;
        this.sessionReservationService = Services.getService(SessionReservationService.class);
        this.services = services;
        this.loginConfiguration = loginConfiguration;
    }

    @Override
    public String getLoginRedirectRequest(HttpServletRequest request, HttpServletResponse response) throws OXException{
        LOG.trace("getLoginRedirectRequest(HttpServletRequest request: {}, HttpServletResponse response)", request.getRequestURI());
        AutologinMode autologinMode = OIDCBackendConfig.AutologinMode.get(this.backend.getBackendConfig().autologinCookieMode());

        if (autologinMode == AutologinMode.OX_DIRECT) {
            String autologinRedirect = this.getAutologinURLFromOIDCCookie(request, response);
            if (!Strings.isEmpty(autologinRedirect)) {
                return autologinRedirect;
            }
        }

        State state = new State();
        Nonce nonce = new Nonce();

        String loginRequest = this.buildLoginRequest(state, nonce, request);
        if (Strings.isEmpty(loginRequest)) {
            throw OIDCExceptionCode.UNABLE_TO_CREATE_AUTHENTICATION_REQUEST.create(backend.getPath());
        }

        this.addAuthRequestToStateManager(state, nonce, request);
        LOG.trace("Login redirect request: {}", loginRequest);
        return loginRequest;
    }

    private String getAutologinURLFromOIDCCookie(HttpServletRequest request, HttpServletResponse response) {
        String redirectURL = "";
        LOG.trace("getAutologinURLFromOIDCCookie(HttpServletRequest request: {}, HttpServletResponse response)", request.getRequestURI());
        try {
            Cookie sessionCookie = OIDCTools.loadSessionCookie(request, this.loginConfiguration);

            if (sessionCookie == null) {
                return null;
            }
            redirectURL = this.getAutologinByCookieURL(request, response, sessionCookie);
        } catch (OXException e) {
            LOG.debug("Failed to load autologin url for request: {}", request.getRequestURI());
        }

        return redirectURL;
    }

    private String getAutologinByCookieURL(HttpServletRequest request, HttpServletResponse response, Cookie sessionCookie) {
        LOG.trace("getAutologinByCookieURL(HttpServletRequest request: {}, HttpServletResponse response, Cookie sessionCookie: {})", request.getRequestURI(), sessionCookie != null ? sessionCookie.getValue() : "null");
        if (sessionCookie != null) {
            Session session = OIDCTools.getSessionFromSessionCookie(sessionCookie, request);
            if (session != null) {
                try {
                    OIDCTools.validateSession(session, request);
                    SessionOAuthTokenService sessionOAuthTokenService = services.getOptionalService(SessionOAuthTokenService.class);
                    if (null != sessionOAuthTokenService) {
                        OIDCBackendConfig config = backend.getBackendConfig();
                        OIDCTokenRefresher refresher = new OIDCTokenRefresher(backend, session);
                        TokenRefreshConfig refreshConfig = OIDCTools.getTokenRefreshConfig(config);
                        try {
                            RefreshResult result = sessionOAuthTokenService.checkOrRefreshTokens(session, refresher, refreshConfig);
                            if (result.isSuccess()) {
                                return this.getRedirectLocationForSession(request, session);
                            }
                            LOG.debug("Error checking and refreshing tokens for session {}", session.getSessionID());
                            return null;
                        } catch (InterruptedException e) {
                            LOG.warn("Thread was interrupted while checking session oauth tokens", e);
                            // keep interrupted state
                            Thread.currentThread().interrupt();
                        }
                    }
                    LOG.debug("Session {} found but SessionOAuthTokenService unavailable.", session.getSessionID());
                } catch (OXException e) {
                    LOG.debug(e.getMessage(), e);
                }
                return null;
            }
            //No session found, log that
            LOG.debug("No valid session found for OIDC Cookie with value: {}", sessionCookie.getValue());
        }

        if (sessionCookie != null) {
            Cookie toRemove = (Cookie) sessionCookie.clone();
            toRemove.setMaxAge(0);
            response.addCookie(toRemove);
        }

        return null;
    }

    private String getRedirectLocationForSession(HttpServletRequest request, Session session) {
        LOG.trace("getRedirectLocationForSession(HttpServletRequest request: {}, Session session: {})", request.getRequestURI(), session.getSessionID());
        return OIDCTools.buildFrontendRedirectLocation(session, OIDCTools.getUIWebPath(this.loginConfiguration, this.backend.getBackendConfig()), OIDCTools.getDeepLink(request));
    }

    private String buildLoginRequest(State state, Nonce nonce, HttpServletRequest request) throws OXException {
        LOG.trace("buildLoginRequest(State state: {}, Nonce nonce: {}, HttpServletRequest request: {})", state.getValue(), nonce.getValue(), request.getRequestURI());
        String requestString = "";
        OIDCBackendConfig backendConfig = this.backend.getBackendConfig();
        String authorizationEndpoint = backendConfig.getOpAuthorizationEndpoint();
        String redirectURI = backendConfig.getRpRedirectURIAuth();
        try {
            Builder requestBuilder = new Builder(new ResponseType(backendConfig.getResponseType()), this.backend.getScope(), new ClientID(backendConfig.getClientID()), new URI(redirectURI));
            requestBuilder.nonce(nonce);
            requestBuilder.state(state);
            requestBuilder.endpointURI(new URI(authorizationEndpoint));
            requestString = this.backend.getAuthorisationRequest(requestBuilder, request).toURI().toString();
        } catch (URISyntaxException e) {
            throw OIDCExceptionCode.CORRUPTED_URI.create(e, authorizationEndpoint, redirectURI);
        }
        return requestString;
    }

    private void addAuthRequestToStateManager(State state, Nonce nonce, HttpServletRequest request)  throws OXException {
        LOG.trace("addAuthRequestToStateManager(State state: {}, Nonce nonce: {}, HttpServletRequest request: {})", state.getValue(), nonce.getValue(), request.getRequestURI());
        String deepLink = OIDCTools.getDeepLink(request);
        String uiClientID = OIDCTools.getUiClient(request);
        String hostname = OIDCTools.getDomainName(request, services.getOptionalService(HostnameService.class));
        Map<String, String> additionalClientInformation = Collections.emptyMap();

        AuthenticationRequestInfo authenticationRequestInfo = new DefaultAuthenticationRequestInfo.Builder(state.getValue())
            .domainName(hostname)
            .deepLink(deepLink)
            .nonce(nonce.getValue())
            .additionalClientInformation(additionalClientInformation)
            .uiClientID(uiClientID)
            .build();
        this.stateManagement.addAuthenticationRequest(authenticationRequestInfo, AUTHN_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Override
    public void authenticateUser(HttpServletRequest request, HttpServletResponse response) throws OXException{
        LOG.trace("authenticateUser(HttpServletRequest request: {}, HttpServletResponse response)", request.getRequestURI());

        String code = request.getParameter("code");
        AuthenticationRequestInfo storedRequestInformation = this.stateManagement.getAndRemoveAuthenticationInfo(request.getParameter("state"));

        if (storedRequestInformation == null && Strings.isEmpty(code)) {
            throw OIDCExceptionCode.INVALID_AUTHENTICATION_STATE_NO_USER.create();
        }

        try {
            TokenRequest tokenReq = this.createTokenRequest(request);
            OIDCTokenResponse tokenResponse = this.getTokenResponse(tokenReq);
            JWT idToken = tokenResponse.getOIDCTokens().getIDToken();
            if (null == idToken) {
                throw OXException.general("Missing IDToken");
            }

            String nonce = null;
            if (storedRequestInformation != null) {
                nonce = storedRequestInformation.getNonce();
            }
            IDTokenClaimsSet validTokenResponse = this.backend.validateIdToken(idToken, nonce);
            if (validTokenResponse == null) {
                throw OIDCExceptionCode.IDTOKEN_GATHERING_ERROR.create("IDToken validation failed, no claim set could be extracted");
            }
            this.sendLoginRequestToServer(request, response, tokenResponse, storedRequestInformation);
        } catch (OXException e) {
            if (e.getExceptionCode() instanceof OIDCExceptionCode) {
                throw e;
            }

            throw OIDCExceptionCode.IDTOKEN_GATHERING_ERROR.create(e, e.getMessage());
        }
    }

    private TokenRequest createTokenRequest(HttpServletRequest request) throws OXException {
        LOG.trace("createTokenRequest(HttpServletRequest request: {})", request.getRequestURI());
        AuthorizationCode code = new AuthorizationCode(request.getParameter("code"));
        URI callback = OIDCTools.getURIFromPath(this.backend.getBackendConfig().getRpRedirectURIAuth());
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, callback);

        ClientAuthentication clientAuth = this.backend.getClientAuthentication();

        URI tokenEndpoint = OIDCTools.getURIFromPath(this.backend.getBackendConfig().getOpTokenEndpoint());

        Scope scope = this.backend.getScope();
        if (codeGrant.getType() == GrantType.AUTHORIZATION_CODE) {
            scope = null;
        }

        return this.backend.getTokenRequest(new TokenRequest(tokenEndpoint, clientAuth, codeGrant, scope));
    }

    private OIDCTokenResponse getTokenResponse(TokenRequest tokenReq) throws OXException {
        LOG.trace("OIDCTokenResponse getTokenResponse(TokenRequest tokenReq {})", tokeRequestInfo(tokenReq));
        HTTPRequest httpRequest = this.backend.getHttpRequest(tokenReq.toHTTPRequest());
        HTTPResponse httpResponse = null;
        try {
            httpResponse = HTTPSender.send(httpRequest, () -> {
                HttpClientService httpClientService = Services.getOptionalService(HttpClientService.class);
                if (httpClientService == null) {
                    throw new IllegalStateException("Missing service " + HttpClientService.class.getName());
                }
                return httpClientService.getHttpClient(OIDCHttpClientConfig.getClientIdOidc());
            });
        } catch (IOException e) {
            throw OIDCExceptionCode.UNABLE_TO_SEND_REQUEST.create(e, GET_THE_ID_TOKEN);
        }
        TokenResponse tokenResponse = null;
        try {
            tokenResponse = OIDCTokenResponseParser.parse(httpResponse);
        } catch (ParseException e) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_RESPONSE_FROM_IDP.create(e, GET_THE_ID_TOKEN);
        }

        if (tokenResponse instanceof TokenErrorResponse) {
            ErrorObject error = ((TokenErrorResponse) tokenResponse).getErrorObject();
            throw OIDCExceptionCode.IDTOKEN_GATHERING_ERROR.create(error.getCode() + ", " + error.getDescription());
        }

        return (OIDCTokenResponse) tokenResponse;
    }

    private void sendLoginRequestToServer(HttpServletRequest request, HttpServletResponse response, OIDCTokenResponse tokenResponse, AuthenticationRequestInfo storedRequestInformation) throws OXException {
        LOG.trace("sendLoginRequestToServer(HttpServletRequest request: {}, HttpServletResponse response, OIDCTokenResponse tokenResponse: {}, AuthenticationRequestInfo storedRequestInformation: {})",
            request.getRequestURI(), tokenResponse.getOIDCTokens().toJSONObject().toJSONString(), storedRequestInformation);
        BearerAccessToken bearerAccessToken = tokenResponse.getTokens().getBearerAccessToken();
        if (bearerAccessToken == null) {
            // could occur, if token response contains access token of a different type than "bearer"
            throw OIDCExceptionCode.UNABLE_TO_PARSE_RESPONSE_FROM_IDP.create("get bearer access token");
        }

        AuthenticationInfo authInfo = this.backend.resolveAuthenticationResponse(request, tokenResponse);
        authInfo.setProperty(OIDCTools.IDTOKEN, tokenResponse.getOIDCTokens().getIDTokenString());
        authInfo.setProperty(OIDCTools.ACCESS_TOKEN, bearerAccessToken.getValue());
        long expiresIn = bearerAccessToken.getLifetime();
        if (expiresIn > 0) {
            authInfo.setProperty(OIDCTools.ACCESS_TOKEN_EXPIRY, String.valueOf(OIDCTools.expiresInToDate(expiresIn).getTime()));
        }

        RefreshToken refreshToken = tokenResponse.getTokens().getRefreshToken();
        if (refreshToken != null) {
            authInfo.setProperty(OIDCTools.REFRESH_TOKEN, refreshToken.getValue());
        }

        String domainName = OIDCTools.getDomainName(request, services.getOptionalService(HostnameService.class));

        String sessionToken = sessionReservationService.reserveSessionFor(
            authInfo.getUserId(),
            authInfo.getContextId(),
            60l,
            TimeUnit.SECONDS,
            authInfo.getProperties());
        String loginRedirect = "";
        try {
            URIBuilder redirectLocation = new URIBuilder()
                .setScheme(OIDCTools.getRedirectScheme(request))
                .setHost(storedRequestInformation != null ? storedRequestInformation.getDomainName() : domainName)
                .setPath(OIDCTools.getRedirectPathPrefix() + "login")
                .setParameter(OIDCTools.SESSION_TOKEN, sessionToken)
                .setParameter(LoginServlet.PARAMETER_ACTION, OIDCTools.OIDC_LOGIN + OIDCTools.getPathString(this.backend.getPath()))
                .setParameter(OIDCTools.PARAM_SHARD, SessionUtility.getShardCookieValue());
            Tools.disableCaching(response);

            String deepLink = storedRequestInformation != null ? storedRequestInformation.getDeepLink() : null;
            if (deepLink != null) {
                redirectLocation.setParameter(OIDCTools.PARAM_DEEP_LINK, deepLink);
            }

            String clientID = storedRequestInformation != null ? storedRequestInformation.getUiClientID() : null;
            if (clientID != null) {
                redirectLocation.setParameter(LoginFields.CLIENT_PARAM, clientID);
            }

            loginRedirect = redirectLocation.build().toString();
            LOG.trace("Login request to OXServer: {}",loginRedirect);
            response.sendRedirect(loginRedirect);
        } catch (URISyntaxException e) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_URI.create(e, "automated construction of login request URI");
        } catch (IOException e) {
            throw OIDCExceptionCode.UNABLE_TO_SEND_REDIRECT.create(e, loginRedirect);
        }
    }

    @Override
    public String getLogoutRedirectRequest(HttpServletRequest request, HttpServletResponse response) throws OXException {
        LOG.trace("getLogoutRedirectRequest(HttpServletRequest request: {}, HttpServletResponse response)", request.getRequestURI());
        Session session = this.extractSessionFromRequest(request);
        String logoutRequestString = "";
        if (this.backend.getBackendConfig().isSSOLogout()) {
            LogoutRequest logoutRequest = this.backend.getLogoutFromIDPRequest(session);
            String domainName = OIDCTools.getDomainName(request, services.getOptionalService(HostnameService.class));

            String redirectURI = getResumeURL(request, response, domainName, session);

            DefaultLogoutRequestInfo defaultLogoutRequestInfo = new DefaultLogoutRequestInfo(logoutRequest.getState().getValue(), domainName, session.getSessionID(), redirectURI);
            this.stateManagement.addLogoutRequest(defaultLogoutRequestInfo, LOGOUT_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
            logoutRequestString = logoutRequest.toURI().toString();
        } else {
            logoutRequestString = this.getRedirectForLogoutFromOXServer(session, request, response, null);
        }
        LOG.trace("Logout request: {}", logoutRequestString);
        return logoutRequestString;
    }

    private String getResumeURL(HttpServletRequest request, HttpServletResponse response, String domainName, Session session) {
        String redirectURI = "";
        String location = OIDCTools.buildFrontendRedirectLocation(session, OIDCTools.getUIWebPath(this.loginConfiguration, this.backend.getBackendConfig()), OIDCTools.getDeepLink(request));
        URIBuilder redirectLocation = new URIBuilder()
            .setScheme(OIDCTools.getRedirectScheme(request))
            .setHost(domainName);
        Tools.disableCaching(response);
        try {
            redirectURI = redirectLocation.build().toString() + location;
        } catch (URISyntaxException e) {
            //should not happen
            LOG.error("", e);
        }
        return redirectURI;
    }

    private Session extractSessionFromRequest(HttpServletRequest request) throws OXException {
        LOG.trace("extractSessionFromRequest(HttpServletRequest request: {})", request.getRequestURI());
        String sessionId = request.getParameter("session");
        if (sessionId == null) {
            throw OIDCExceptionCode.INVALID_LOGOUT_REQUEST.create("No session parameter set.");
        }

        Session session = this.getSessionFromId(sessionId);
        if (session == null) {
            throw OIDCExceptionCode.INVALID_LOGOUT_REQUEST.create("Invalid session parameter, no session found.");
        }
        OIDCTools.validateSession(session, request);

        return session;
    }

    private Session getSessionFromId(String sessionId) {
        LOG.trace("getSessionFromId(String sessionId: {})", sessionId);
        SessiondService sessiondService = Services.getService(SessiondService.class);
        return sessiondService.getSession(sessionId);
    }

    @Override
    public String logoutSSOUser(HttpServletRequest request, HttpServletResponse response) throws OXException {
        LOG.trace("logoutSSOUser(HttpServletRequest request: {}, HttpServletResponse response)", request.getRequestURI());
        LogoutRequestInfo logoutRequestInfo = this.loadLogoutRequestInfo(request);
        String sessionId = logoutRequestInfo.getSessionId();
        LOG.trace("Try to logout user, via OP with sessionId: {}", sessionId);
        //logout user
        Session session = this.getSessionFromId(sessionId);
        if (null == session) {
            OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
            oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, ExpirationReason.NO_SUCH_SESSION.getIdentifier());
            throw oxe;
        }
        return this.getRedirectForLogoutFromOXServer(session, request, response, logoutRequestInfo);
    }

    private String getRedirectForLogoutFromOXServer(Session session, HttpServletRequest request, HttpServletResponse response, LogoutRequestInfo logoutRequestInfo) throws OXException {
        LOG.trace("getRedirectForLogoutFromOXServer(Session session: {}, HttpServletRequest request: {}, HttpServletResponse response, LogoutRequestInfo logoutRequestInfo.domainname: {})", session.getSessionID(), request.getRequestURI(), logoutRequestInfo != null ? logoutRequestInfo.getDomainName() : "null");
        String domainName = OIDCTools.getDomainName(request, services.getOptionalService(HostnameService.class));
        String sessionId = session.getSessionID();

        if (logoutRequestInfo != null) {
            domainName = logoutRequestInfo.getDomainName();
            sessionId = logoutRequestInfo.getSessionId();
        }
        String redirectionURI = "";
        try {
            URIBuilder redirectLocation = new URIBuilder()
                .setScheme(OIDCTools.getRedirectScheme(request))
                .setHost(domainName)
                .setPath(OIDCTools.getRedirectPathPrefix() + "login")
                .setParameter(LoginServlet.PARAMETER_SESSION, sessionId)
                .setParameter(LoginServlet.PARAMETER_ACTION, OIDCTools.OIDC_LOGOUT + OIDCTools.getPathString(this.backend.getPath()));
            Tools.disableCaching(response);
            redirectionURI = redirectLocation.build().toString();
            LOG.trace("Logout URI for logout from OXServer: {}", redirectionURI);
            return redirectionURI;
        } catch (URISyntaxException e) {
            this.logoutInCaseOfError(sessionId, request, response);
            throw OIDCExceptionCode.UNABLE_TO_PARSE_URI.create(e, "automated construction of logout request URI");
        }
    }

    @Override
    public void logoutInCaseOfError(String sessionId, HttpServletRequest request, HttpServletResponse response) throws OXException{
        LOG.trace("logoutInCaseOfError(String sessionId: {}, HttpServletRequest request: {}, HttpServletResponse response)", sessionId, request.getRequestURI() );
        Session session = LoginPerformer.getInstance().lookupSession(sessionId);
        this.backend.logoutCurrentUser(session, request, response);
    }

    @Override
    public boolean validateThirdPartyRequest(HttpServletRequest request) {
        boolean result = false;
        String issuer = request.getParameter("iss");
        if (!Strings.isEmpty(issuer)) {
            result = this.backend.getBackendConfig().getOpIssuer().equals(issuer);
        }
        return result;
    }

    @Override
    public void resumeUser(HttpServletRequest request, HttpServletResponse response) throws OXException {
        LOG.trace("resumeUser(HttpServletRequest request: {}, HttpServletResponse response)", request.getRequestURI());
        LogoutRequestInfo logoutRequestInfo = this.loadLogoutRequestInfo(request);
        try {
            OIDCTools.buildRedirectResponse(response, logoutRequestInfo.getRequestURI(), Boolean.TRUE);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            this.logoutInCaseOfError(logoutRequestInfo.getSessionId(), request, response);
        }
    }

    private LogoutRequestInfo loadLogoutRequestInfo(HttpServletRequest request) throws OXException {
        LOG.trace("loadLogoutRequestInfo(HttpServletRequest request: {})", request.getRequestURI());
        // state
        String state = request.getParameter(OIDCTools.STATE);
        if (state == null) {
            throw OIDCExceptionCode.INVALID_LOGOUT_REQUEST.create("missing state parameter in response from the OP.");
        }
        LOG.trace("Try to load LogoutRequestInfo for user, with state: {}", state);
        //load state
        LogoutRequestInfo logoutRequestInfo = this.stateManagement.getAndRemoveLogoutRequestInfo(state);
        if (logoutRequestInfo == null) {
            throw OIDCExceptionCode.INVALID_LOGOUT_REQUEST.create("wrong state in response from the OP.");
        }
        return logoutRequestInfo;
    }
}
