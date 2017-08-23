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
package com.openexchange.oidc.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest.Builder;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.OIDCWebSSOProvider;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.spi.AuthenticationInfo;
import com.openexchange.oidc.spi.OIDCBackend;
import com.openexchange.oidc.state.AuthenticationRequestInfo;
import com.openexchange.oidc.state.DefaultAuthenticationRequestInfo;
import com.openexchange.oidc.state.DefaultLogoutRequestInfo;
import com.openexchange.oidc.state.LogoutRequestInfo;
import com.openexchange.oidc.state.StateManagement;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.http.Tools;


/**
 * Default implementation of the OpenID web SSO provider features
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


    public OIDCWebSSOProviderImpl(OIDCBackend backend, StateManagement stateManagement) {
        super();
        this.backend = backend;
        this.stateManagement = stateManagement;
        this.sessionReservationService = Services.getService(SessionReservationService.class);
    }

    @Override
    public String getLoginRedirectRequest(HttpServletRequest request) throws OXException{
        State state = new State();
        Nonce nonce = new Nonce();

        String loginRequest = this.buildLoginRequest(state, nonce, request);

        if (loginRequest.isEmpty()) {
            throw OIDCExceptionCode.UNABLE_TO_CREATE_AUTHENTICATION_REQUEST.create(backend.getPath());
        }

        this.addAuthRequestToStateManager(request, state, nonce);
        return loginRequest;
    }

    private String buildLoginRequest(State state, Nonce nonce, HttpServletRequest request) throws OXException {
        String requestString = "";
        OIDCBackendConfig backendConfig = this.backend.getBackendConfig();
        String authorizationEndpoint = backendConfig.getAuthorizationEndpoint();
        String redirectURI = backendConfig.getRedirectURIAuth();
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

    private void addAuthRequestToStateManager(HttpServletRequest request, State state, Nonce nonce) {
        String deepLink = request.getParameter("deep_link");
        String uiClientID = this.getUiClient(request);
        String hostname = OIDCTools.getDomainName(request);
        Map<String, String> additionalClientInformation = new HashMap<>();

        AuthenticationRequestInfo authenticationRequestInfo = new DefaultAuthenticationRequestInfo.Builder(state.getValue())
            .domainName(hostname)
            .deepLink(deepLink)
            .nonce(nonce.getValue())
            .additionalClientInformation(additionalClientInformation)
            .uiClientID(uiClientID)
            .build();
        this.stateManagement.addAuthenticationRequest(authenticationRequestInfo);
    }

    private String getUiClient(HttpServletRequest request) {
        String uiClientID = request.getParameter("client");

        if (uiClientID == null || uiClientID.isEmpty()) {

            LoginConfiguration loginConfiguration =  LoginServlet.getLoginConfiguration();
            uiClientID = loginConfiguration.getDefaultClient();
        }

        return uiClientID;
    }

    @Override
    public void authenticateUser(HttpServletRequest request, HttpServletResponse response) throws OXException{
        AuthenticationRequestInfo storedRequestInformation = this.stateManagement.getAndRemoveAuthenticationInfo(request.getParameter("state"));

        if (storedRequestInformation == null) {
            throw OIDCExceptionCode.INVALID_AUTHENTICATION_STATE_NO_USER.create();
        }

        try {
            TokenRequest tokenReq = this.createTokenRequest(request);
            OIDCTokenResponse tokenResponse = this.getTokenResponse(tokenReq);
            IDTokenClaimsSet validTokenResponse = this.validTokenResponse(tokenResponse, storedRequestInformation);
            if (validTokenResponse != null) {
                this.sendLoginRequestToServer(request, response, tokenResponse);
            }
        } catch (OXException e) {
            throw OIDCExceptionCode.IDTOKEN_GATHERING_ERROR.create(e.getMessage());
        }
    }

    private String sendLoginRequestToServer(HttpServletRequest request, HttpServletResponse response, OIDCTokenResponse tokenResponse) throws OXException {
        AuthenticationInfo authInfo = this.backend.resolveAuthenticationResponse(request, tokenResponse);
        authInfo.setProperty(OIDCTools.IDTOKEN, tokenResponse.getOIDCTokens().getIDTokenString());
        if (this.backend.getBackendConfig().isStoreOAuthTokensEnabled()) {
            authInfo.setProperty(OIDCTools.ACCESS_TOKEN, tokenResponse.getTokens().getBearerAccessToken().getValue());
            authInfo.setProperty(OIDCTools.REFRESH_TOKEN, tokenResponse.getTokens().getRefreshToken().getValue());
        }
        
        String sessionToken = sessionReservationService.reserveSessionFor(
            authInfo.getUserId(),
            authInfo.getContextId(),
            60l,
            TimeUnit.SECONDS,
            authInfo.getProperties());
        try {
            URIBuilder redirectLocation = new URIBuilder()
                .setScheme(this.getRedirectScheme(request))
                .setHost(OIDCTools.getDomainName(request))
                .setPath(getRedirectPathPrefix() + "login")
                .setParameter(OIDCTools.SESSION_TOKEN, sessionToken)
                .setParameter(LoginServlet.PARAMETER_ACTION, OIDCTools.LOGIN_ACTION + OIDCTools.getPathString(this.backend.getPath()));
            Tools.disableCaching(response);
            response.sendRedirect(redirectLocation.build().toString());
        } catch (URISyntaxException e) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_URI.create(e, "automated construction of login request URI");
        } catch (IOException e) {
            throw new OXException(e);
        }
        return null;
    }

    private String getRedirectScheme(HttpServletRequest request) {
        boolean secure = Tools.considerSecure(request);
        return secure ? "https" : "http";
    }

    private String getRedirectPathPrefix() {
        DispatcherPrefixService prefixService = Services.getService(DispatcherPrefixService.class);
        return prefixService.getPrefix();
    }

    private IDTokenClaimsSet validTokenResponse(OIDCTokenResponse tokenResponse, AuthenticationRequestInfo storedRequestInformation) throws OXException {
        return this.backend.validateIdToken(tokenResponse.getOIDCTokens().getIDToken(), storedRequestInformation);
    }

    private TokenRequest createTokenRequest(HttpServletRequest request) throws OXException {
        AuthorizationCode code = new AuthorizationCode(request.getParameter("code"));
        URI callback = OIDCTools.getURIFromPath(this.backend.getBackendConfig().getRedirectURIAuth());
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, callback);

        ClientAuthentication clientAuth = this.backend.getClientAuthentication();

        URI tokenEndpoint = OIDCTools.getURIFromPath(this.backend.getBackendConfig().getTokenEndpoint());

        TokenRequest tokenRequest = new TokenRequest(tokenEndpoint, clientAuth, codeGrant, this.backend.getScope());
        return this.backend.getTokenRequest(tokenRequest);
    }

    private OIDCTokenResponse getTokenResponse(TokenRequest tokenReq) throws OXException {
        HTTPRequest httpRequest = this.backend.getHttpRequest(tokenReq.toHTTPRequest());
        // TODO QS-VS: ISt die send Methode sicher genug?
        HTTPResponse httpResponse = null;
        try {
            httpResponse = httpRequest.send();
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

    @Override
    public String getLogoutRedirectRequest(HttpServletRequest request, HttpServletResponse response) throws OXException {
        Session session = this.extractSessionFromRequest(request);
        String logoutRequest = this.backend.getBackendConfig().getRedirectURILogout();
        if (this.backend.getBackendConfig().isSSOLogout()) {
            logoutRequest = this.backend.getLogoutFromIDPRequest(session);
            this.stateManagement.addLogoutRequest(new DefaultLogoutRequestInfo(new State().getValue(), OIDCTools.getDomainName(request), session.getSessionID()));
        } else {
            this.logoutCurrentUser(session, request, response);
        }
        return logoutRequest;
    }

    private Session extractSessionFromRequest(HttpServletRequest request) throws OXException {
        String sessionId = request.getParameter("session");
        if (sessionId == null) {
            throw OIDCExceptionCode.INVALID_LOGOUT_REQUEST.create("No session parameter set.");
        }

        Session session = getSessionFromId(sessionId);
        if (session == null) {
            throw OIDCExceptionCode.INVALID_LOGOUT_REQUEST.create("Invalid session parameter, no session found.");
        }
        return session;
    }

    private Session getSessionFromId(String sessionId) {
        SessiondService sessiondService = Services.getService(SessiondService.class);
        return sessiondService.getSession(sessionId);
    }

    @Override
    public void logoutSSOUser(HttpServletRequest request, HttpServletResponse response) throws OXException {
        //Check state
        String state = request.getParameter(OIDCTools.STATE);
        if (state == null) {
            throw OIDCExceptionCode.INVALID_LOGOUT_REQUEST.create("missing state parameter in response from the OP");
        }
        //load state
        LogoutRequestInfo loginRequestInfo = this.stateManagement.getAndRemoveLoginRequestInfo(state);
        if (loginRequestInfo == null) {
            throw OIDCExceptionCode.INVALID_LOGOUT_REQUEST.create("wrong state in response from the OP");
        }
        String sessionId = loginRequestInfo.getSessionId();
        //logout user
        this.logoutCurrentUser(getSessionFromId(sessionId), request, response);
    }
    
    private void logoutCurrentUser(Session session, HttpServletRequest request, HttpServletResponse response) throws OXException {
        OIDCTools.validateSession(session, request);
        LoginPerformer.getInstance().doLogout(session.getSessionID());
        SessionUtility.removeOXCookies(session, request, response);
        SessionUtility.removeJSESSIONID(request, response);
    }
}
