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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.SerializeException;
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
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.OIDCWebSSOProvider;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.spi.OIDCBackend;
import com.openexchange.oidc.state.AuthenticationRequestInfo;
import com.openexchange.oidc.state.DefaultAuthenticationRequestInfo;
import com.openexchange.oidc.state.StateManagement;
import net.minidev.json.JSONObject;


/**
 * Default implementation of the OpenID web SSO provider features
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCWebSSOProviderImpl implements OIDCWebSSOProvider {

    private static final String GET_THE_ID_TOKEN = "get the IDToken";
    private static final String LOAD_EMAIL_ADDRESS = "load email address";
    private static final Logger LOG = LoggerFactory.getLogger(OIDCWebSSOProviderImpl.class);
    private final OIDCBackend backend;
    private final StateManagement stateManagement;
    
    
    public OIDCWebSSOProviderImpl(OIDCBackend backend, StateManagement stateManagement) {
        super();
        this.backend = backend;
        this.stateManagement = stateManagement;
    }
    
    @Override
    public String getLoginRedirectRequest(HttpServletRequest httpRequest) throws OXException{
        State state = new State();
        Nonce nonce = new Nonce();
        
        String requestString = getRequestString(state, nonce, httpRequest);
        
        if (requestString.isEmpty()) {
            throw OIDCExceptionCode.UNABLE_TO_CREATE_AUTHENTICATION_REQUEST.create(backend.getPath());
        }
        
        this.addRequestToStateManager(httpRequest, state, nonce);
        return requestString;
    }
    
    private String getRequestString(State state, Nonce nonce, HttpServletRequest httpRequest) throws OXException {
        String requestString = "";
        OIDCBackendConfig backendConfig = this.backend.getBackendConfig();
        String authorizationEndpoint = backendConfig.getAuthorizationEndpoint();
        String redirectURI = backendConfig.getRedirectURIAuth();
        try {
            Builder requestBuilder = new Builder(new ResponseType(backendConfig.getResponseType()), this.backend.getScope(), new ClientID(backendConfig.getClientID()), new URI(redirectURI));
            requestBuilder.nonce(nonce);
            requestBuilder.state(state);
            requestBuilder.endpointURI(new URI(authorizationEndpoint));
            requestString = this.backend.getAuthorisationRequest(requestBuilder, httpRequest).toURI().toString();
        } catch (URISyntaxException e) {
            throw OIDCExceptionCode.CORRUPTED_URI.create(e, authorizationEndpoint, redirectURI);
        }
        return requestString;
    }
    
    private void addRequestToStateManager(HttpServletRequest httpRequest, State state, Nonce nonce) {
        String deepLink = httpRequest.getParameter("deep_link");
        String uiClientID = this.getUiClient(httpRequest);
        String hostname = this.getDomainName(Services.getService(HostnameService.class), httpRequest);
        Map<String, String> additionalClientInformation = new HashMap<>();
        
        AuthenticationRequestInfo authenticationRequestInfo = new DefaultAuthenticationRequestInfo(state, hostname, deepLink, nonce, additionalClientInformation, uiClientID);
        this.stateManagement.addAuthenticationRequest(authenticationRequestInfo);
    }
    
    private String getUiClient(HttpServletRequest httpRequest) {
        String uiClientID = httpRequest.getParameter("client");
        
        if (uiClientID == null || uiClientID.isEmpty()) {
           
            LoginConfiguration loginConfiguration =  LoginServlet.getLoginConfiguration();
            uiClientID = loginConfiguration.getDefaultClient();
        }
        
        return uiClientID;
    }
    
    private String getDomainName(HostnameService hostnameService, HttpServletRequest httpRequest) {
        if (hostnameService == null) {
            return httpRequest.getServerName();
        }

        String hostname = hostnameService.getHostname(-1, -1);
        if (hostname == null) {
            return httpRequest.getServerName();
        }

        return hostname;
    }

    @Override
    public String authenticateUser(HttpServletRequest httpRequest) throws OXException{
        String redirectionString = "";
        TokenRequest tokenReq;
        
        AuthenticationRequestInfo storedRequestInformation = this.stateManagement.getAndRemoveAuthenticationInfo(httpRequest.getParameter("state"));
        
        if (storedRequestInformation == null) {
            throw OIDCExceptionCode.INVALID_AUTHENTICATION_STATE_NO_USER.create();
        }
        
        try {
            tokenReq = createTokenRequest(httpRequest);
            OIDCTokenResponse tokenResponse = getTokenResponse(tokenReq);
            IDTokenClaimsSet idTokenClaimsSet = this.validTokenResponse(tokenResponse, storedRequestInformation);
            BearerAccessToken bearerAccessToken = tokenResponse.getTokens().getBearerAccessToken();
            if(idTokenClaimsSet != null && bearerAccessToken != null) {
                String emailAddress = this.loadEmailAddressFromIDP(bearerAccessToken);
                redirectionString = this.loginUserAndRedirect(emailAddress);
            } else {
                throw OIDCExceptionCode.INVALID_IDTOKEN_GENERAL.create();
            }
        } catch (OXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return redirectionString;
    }
    
    private String loadEmailAddressFromIDP(BearerAccessToken bearerAccessToken) throws OXException {
        UserInfoRequest userInfoReq = null;
        String userInfoEndpoint = this.backend.getBackendConfig().getUserInfoEndpoint();
        try {
            userInfoReq = new UserInfoRequest(new URI(userInfoEndpoint), bearerAccessToken);
        } catch (URISyntaxException e1) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_URI.create(e1, userInfoEndpoint);
        }

        HTTPResponse userInfoHTTPResp = null;
        try {
          userInfoHTTPResp = userInfoReq.toHTTPRequest().send();
        } catch (SerializeException | IOException e) {
            throw OIDCExceptionCode.UNABLE_TO_SEND_REQUEST.create(e, LOAD_EMAIL_ADDRESS);
        }

        UserInfoResponse userInfoResponse = null;
        try {
          userInfoResponse = UserInfoResponse.parse(userInfoHTTPResp);
        } catch (ParseException e) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_RESPONSE_FROM_IDP.create(e, LOAD_EMAIL_ADDRESS);
        }

        if (userInfoResponse instanceof UserInfoErrorResponse) {
          ErrorObject error = ((UserInfoErrorResponse) userInfoResponse).getErrorObject();
          throw OIDCExceptionCode.UNABLE_TO_LOAD_USERINFO.create(error.getCode() + " " + error.getDescription());
        }

        UserInfoSuccessResponse successResponse = (UserInfoSuccessResponse) userInfoResponse;
        if (successResponse == null) {
            throw OIDCExceptionCode.UNABLE_TO_LOAD_USERINFO.create();
        }
        return successResponse.getUserInfo().getEmailAddress();
    }
    
    private String loginUserAndRedirect(String emailAddress) throws OXException {
        
        return null;
    }

    private IDTokenClaimsSet validTokenResponse(OIDCTokenResponse tokenResponse, AuthenticationRequestInfo storedRequestInformation) throws OXException {
        return this.backend.validateIdToken(tokenResponse.getOIDCTokens().getIDToken(), storedRequestInformation);
    }
    
    private TokenRequest createTokenRequest(HttpServletRequest httpRequest) throws OXException {
        AuthorizationCode code = new AuthorizationCode(httpRequest.getParameter("code"));
        String redirectURIAuth = this.backend.getBackendConfig().getRedirectURIAuth();
        URI callback = null;
        try {
            callback = new URI(redirectURIAuth);
        } catch (URISyntaxException e) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_URI.create(e, redirectURIAuth);
        }
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, callback);
    
        ClientAuthentication clientAuth = this.backend.getClientAuthentication();
    
        URI tokenEndpoint = null;
        String tokenEndpointConfig = this.backend.getBackendConfig().getTokenEndpoint();
        try {
            tokenEndpoint = new URI(tokenEndpointConfig);
        } catch (URISyntaxException e) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_URI.create(e, tokenEndpointConfig);
        }
    
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
}
