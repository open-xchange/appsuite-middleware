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
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest.Builder;
import com.nimbusds.openid.connect.sdk.LogoutRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestImpl;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.authentication.Authenticated;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.impl.OIDCBackendConfigImpl;
import com.openexchange.oidc.impl.OIDCConfigImpl;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.state.AuthenticationRequestInfo;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Session;

/**
 * Reference implementation of an OpenID backend.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public abstract class AbstractOIDCBackend implements OIDCBackend {
    
    protected static final String AUTH_RESPONSE = "auth_response";
    
    @Override
    public OIDCConfig getOIDCConfig() {
        return new OIDCConfigImpl(Services.getService(LeanConfigurationService.class));
    }
    
    @Override
    public OIDCBackendConfig getBackendConfig() {
        return new OIDCBackendConfigImpl(Services.getService(LeanConfigurationService.class));
    }
    
    @Override
    public String getPath() {
        return "";
    }
    
    @Override
    public OIDCExceptionHandler getExceptionHandler() {
        return null;
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
    public JWKSet getJwkSet() throws OXException {
         JWKSet jwkSet = null;
        try {
            jwkSet = JWKSet.load(new URL(this.getBackendConfig().getJwkSet()));
        } catch (IOException | ParseException e) {
            throw OIDCExceptionCode.UNABLE_TO_GET_JWKSET_WITH_URL.create(e, this.getBackendConfig().getJwkSet());
        }
        return jwkSet;
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
    public IDTokenClaimsSet validateIdToken(JWT idToken, AuthenticationRequestInfo storedRequestInformation) throws OXException {
        IDTokenClaimsSet result = null;
        JWKSet jwkSet = this.getJwkSet();
        JWSAlgorithm expectedJWSAlg = this.getJWSAlgorithm();

        IDTokenValidator idTokenValidator = new IDTokenValidator(new Issuer(this.getBackendConfig().getIssuer()), new ClientID(this.getBackendConfig().getClientID()), expectedJWSAlg, jwkSet);
        try {
            result = idTokenValidator.validate(idToken, new Nonce(storedRequestInformation.getNonce()));
        } catch (BadJOSEException e) {
            throw OIDCExceptionCode.IDTOKEN_VALIDATON_FAILED_CONTENT.create(e, "");
        } catch (JOSEException e) {
            throw OIDCExceptionCode.IDTOKEN_VALIDATON_FAILED.create(e, "");
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
            //TODO QS-VS: Eventuell das Token mit rausgeben?
            throw OIDCExceptionCode.UNABLE_TO_PARSE_SESSIONS_IDTOKEN.create(e);
        }

        URI postLogoutTarget = OIDCTools.getURIFromPath(this.getBackendConfig().getRedirectURIPostSSOLogout());
        LogoutRequest logoutRequest = new LogoutRequest(endSessionEndpoint, idToken, postLogoutTarget, null);
        return logoutRequest.toURI().toString();
    }
    
    //TODO QS-VS: Cache JWKSet HttpCaching, Header müssen angeben wann eine Aktualisierung des Caches notwendig ist
}
