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
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest.Builder;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
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


/**
 * Default implementation of the OpenID web SSO provider features
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCWebSSOProviderImpl implements OIDCWebSSOProvider {

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
        
        String requestString = getRequestString(state, nonce);
        
        if (requestString.isEmpty()) {
            throw OIDCExceptionCode.UNABLE_TO_CREATE_AUTHENTICATION_REQUEST.create(backend.getPath());
        }
        
        this.addRequestToStateManager(httpRequest, state, nonce);
        return requestString;
    }
    
    private String getRequestString(State state, Nonce nonce) throws OXException {
        String requestString = "";
        OIDCBackendConfig backendConfig = this.backend.getBackendConfig();
        String authorizationEndpoint = backendConfig.getAuthorizationEndpoint();
        String redirectURI = backendConfig.getRedirectURI();
        try {
            Builder requestBuilder = new Builder(new ResponseType(backendConfig.getResponseType()), Scope.parse(backendConfig.getScope()), new ClientID(backendConfig.getClientID()), new URI(redirectURI));
            requestBuilder.nonce(nonce);
            requestBuilder.state(state);
            requestBuilder.endpointURI(new URI(authorizationEndpoint));
            //TODO QS-VS: remove
//            AuthenticationRequest request = new AuthenticationRequest(
//                new URI(authorizationEndpoint),
//                new ResponseType(backend.getBackendConfig().getResponseType()),
//                Scope.parse(backend.getBackendConfig().getScope()),
//                new ClientID(backend.getBackendConfig().getClientID()),
//                new URI(redirectURI),
//                state,
//                nonce);
            requestString = this.backend.getAuthorisationRequest(requestBuilder).toURI().toString();
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
        try {
            tokenReq = createTokenRequest(httpRequest);
            OIDCTokenResponse tokenResponse = getTokenResponse(tokenReq);
            if(this.validTokenResponse(tokenResponse)) {
                redirectionString = this.loginUserAndRedirect();
            }
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return redirectionString;
    }
    
    private String loginUserAndRedirect() {
        return null;
    }

    private boolean validTokenResponse(OIDCTokenResponse tokenResponse) throws OXException {
        return this.backend.validateIdToken(tokenResponse.getOIDCTokens().getIDToken(), null);
    }
    
    private TokenRequest createTokenRequest(HttpServletRequest httpRequest) throws URISyntaxException {
        AuthorizationCode code = new AuthorizationCode(httpRequest.getParameter("code"));
        URI callback = new URI(this.backend.getBackendConfig().getRedirectURI());
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, callback);
    
        ClientAuthentication clientAuth = this.backend.getClientAuthentication();
    
        URI tokenEndpoint = new URI(this.backend.getBackendConfig().getTokenEndpoint());
    
        TokenRequest tokenRequest = new TokenRequest(tokenEndpoint, clientAuth, codeGrant);
        return this.backend.getTokenRequest(tokenRequest);
    }
    
    private OIDCTokenResponse getTokenResponse(TokenRequest tokenReq) throws IOException, ParseException {
        HTTPRequest httpRequest = this.backend.getHttpRequest(tokenReq.toHTTPRequest());
        // TODO QS-VS: ISt die send Methode sicher genug?
        HTTPResponse httpResponse = httpRequest.send();
        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(httpResponse);
        
        if (tokenResponse instanceof TokenErrorResponse) {
            ErrorObject error = ((TokenErrorResponse) tokenResponse).getErrorObject();
            //TODO QS-VS: Fehler werfen
        }
        
        return (OIDCTokenResponse) tokenResponse;
    }
}
