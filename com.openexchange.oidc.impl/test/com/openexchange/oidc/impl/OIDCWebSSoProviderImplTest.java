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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.LogoutRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.state.AuthenticationRequestInfo;
import com.openexchange.oidc.state.LogoutRequestInfo;
import com.openexchange.oidc.state.StateManagement;
import com.openexchange.oidc.state.impl.DefaultLogoutRequestInfo;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.SessionReservationService;

/**
 * {@link OIDCWebSSoProviderImplTest} Testclass for {@link OIDCWebSSOProviderImpl}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ LoginConfiguration.class, Services.class, OIDCWebSSOProviderImpl.class, OIDCTokenResponseParser.class, OIDCTools.class })
public class OIDCWebSSoProviderImplTest {

    private static final String STATE_VALUE = "stateValue";

    @Mock
    private OIDCBackendConfig mockedBackendConfig;

    @Mock
    private StateManagement mockedStateManagement;

    @Mock
    private ServiceLookup mockedServices;

    @Mock
    private LoginConfiguration mockedLoginConfiguration;

    @Mock
    private OIDCBackend mockedBackend;

    @Mock
    private HttpServletRequest mockedRequest;

    @Mock
    private HttpServletResponse mockedResponse;

    @Mock
    private SessionReservationService mockedSessionReservation;

    @Mock
    private AuthenticationRequestInfo mockedAuthRequestInfo;

    @Mock
    private TokenRequest mockedTokenRequest;

    @Mock
    private Session mockedSession;

    private OIDCWebSSOProviderImpl provider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);
        PowerMockito.mockStatic(OIDCTools.class);
        PowerMockito.when(Services.getService(SessionReservationService.class)).thenReturn(mockedSessionReservation);

        PowerMockito.mockStatic(OIDCTokenResponseParser.class);
        Mockito.when(mockedBackend.getBackendConfig()).thenReturn(mockedBackendConfig);

        this.provider = PowerMockito.spy(new OIDCWebSSOProviderImpl(mockedBackend, mockedStateManagement, mockedServices, mockedLoginConfiguration));

    }

    @Test
    public void getLoginRedirectRequest_LoginRedirectSuccessTest() throws Exception {
        String loginRequest = "loginRequest";
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(null);

        PowerMockito.doReturn(loginRequest).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "buildLoginRequest", State.class, Nonce.class, HttpServletRequest.class)).withArguments(ArgumentMatchers.any(State.class), ArgumentMatchers.any(Nonce.class), ArgumentMatchers.any(HttpServletRequest.class));

        PowerMockito.doNothing().when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "addAuthRequestToStateManager", State.class, Nonce.class, HttpServletRequest.class)).withArguments(ArgumentMatchers.any(State.class), ArgumentMatchers.any(Nonce.class), ArgumentMatchers.any(HttpServletRequest.class));

        try {
            String result = this.provider.getLoginRedirectRequest(mockedRequest, mockedResponse);
            assertTrue("Result is not like expected", loginRequest.equals(result));
        } catch (OXException e) {
            e.printStackTrace();
            fail("No error should happen");
        }
    }

    @Test
    public void getLoginRedirectRequest_AutologinSuccessTest() throws Exception {

        String loginRequest = "loginRequest";
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.OX_DIRECT.getValue());

        PowerMockito.doReturn(loginRequest).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "getAutologinURLFromOIDCCookie", HttpServletRequest.class, HttpServletResponse.class)).withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class));

        try {
            String result = this.provider.getLoginRedirectRequest(mockedRequest, mockedResponse);
            assertTrue("Result is not like expected", loginRequest.equals(result));
        } catch (OXException e) {
            e.printStackTrace();
            fail("No error should happen");
        }
    }

    @Test
    public void getLoginRedirectRequest_LoginRequestEmptyTest() throws Exception {
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(null);

        PowerMockito.doReturn(null).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "buildLoginRequest", State.class, Nonce.class, HttpServletRequest.class)).withArguments(ArgumentMatchers.any(State.class), ArgumentMatchers.any(Nonce.class), ArgumentMatchers.any(HttpServletRequest.class));

        PowerMockito.doNothing().when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "addAuthRequestToStateManager", State.class, Nonce.class, HttpServletRequest.class)).withArguments(ArgumentMatchers.any(State.class), ArgumentMatchers.any(Nonce.class), ArgumentMatchers.any(HttpServletRequest.class));

        try {
            this.provider.getLoginRedirectRequest(mockedRequest, mockedResponse);
        } catch (OXException e) {
            assertTrue("Wrong error message thrown.", e.getExceptionCode() == OIDCExceptionCode.UNABLE_TO_CREATE_AUTHENTICATION_REQUEST);
            return;
        }
        fail("No error was thrown, but expected");
    }

    @Test
    public void getLoginRedirectRequest_AutologinNoCookieTest() throws Exception {
        Mockito.when(mockedBackend.getBackendConfig()).thenReturn(mockedBackendConfig);

        String loginRequest = "loginRequest";
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.OX_DIRECT.getValue());

        PowerMockito.doReturn(null).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "getAutologinURLFromOIDCCookie", HttpServletRequest.class, HttpServletResponse.class)).withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class));

        PowerMockito.doReturn(loginRequest).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "buildLoginRequest", State.class, Nonce.class, HttpServletRequest.class)).withArguments(ArgumentMatchers.any(State.class), ArgumentMatchers.any(Nonce.class), ArgumentMatchers.any(HttpServletRequest.class));

        PowerMockito.doNothing().when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "addAuthRequestToStateManager", State.class, Nonce.class, HttpServletRequest.class)).withArguments(ArgumentMatchers.any(State.class), ArgumentMatchers.any(Nonce.class), ArgumentMatchers.any(HttpServletRequest.class));

        try {
            String result = this.provider.getLoginRedirectRequest(mockedRequest, mockedResponse);
            assertTrue("Result is not like expected", loginRequest.equals(result));
        } catch (OXException e) {
            e.printStackTrace();
            fail("No error should happen");
        }
    }

    @Test
    public void authenticateUser_NoStoredAuthenticationTest() throws OXException{
        Mockito.when(this.mockedStateManagement.getAndRemoveAuthenticationInfo(ArgumentMatchers.anyString())).thenReturn(null);
        try {
            this.provider.authenticateUser(mockedRequest, mockedResponse);
        } catch (OXException e) {
            assertTrue("Wrong error message thrown.", e.getExceptionCode() == OIDCExceptionCode.INVALID_AUTHENTICATION_STATE_NO_USER);
            return;
        }
        fail("No error was thrown, but expected");
    }

    @Test
    public void authenticateUser_getTokenResponseFailTest() throws Exception {
        Mockito.when(mockedRequest.getParameter("state")).thenReturn(STATE_VALUE);
        Mockito.when(this.mockedStateManagement.getAndRemoveAuthenticationInfo(STATE_VALUE)).thenReturn(mockedAuthRequestInfo);

        PowerMockito.doReturn(mockedTokenRequest).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "createTokenRequest", HttpServletRequest.class)).withArguments(mockedRequest);
        
        
        HTTPRequest mockedHttpRequest = Mockito.mock(HTTPRequest.class);
        HTTPResponse mockedHttpResponse = Mockito.mock(HTTPResponse.class);
        Mockito.when(mockedTokenRequest.toHTTPRequest()).thenReturn(mockedHttpRequest);
        
        Mockito.when(mockedBackend.getHttpRequest(ArgumentMatchers.any(HTTPRequest.class))).thenReturn(mockedHttpRequest);
        Mockito.when(mockedHttpRequest.send()).thenReturn(mockedHttpResponse);

        String code = "100";
        String errorDescription = "Token response parsing failed";
        Mockito.when(OIDCTokenResponseParser.parse(mockedHttpResponse)).thenReturn(new TokenErrorResponse(new ErrorObject(code, errorDescription)));

        try {
            this.provider.authenticateUser(mockedRequest, mockedResponse);
        } catch (OXException e) {
            assertTrue("Wrong error message thrown. With wrong message", e.getExceptionCode() == OIDCExceptionCode.IDTOKEN_GATHERING_ERROR && e.getMessage().contains(errorDescription));
            return;
        }
        fail("No error was thrown, but expected");
    }

    @Test
    public void authenticateUser_TokenValidationFailTest() throws Exception {
        Mockito.when(mockedRequest.getParameter("state")).thenReturn(STATE_VALUE);
        Mockito.when(this.mockedStateManagement.getAndRemoveAuthenticationInfo(STATE_VALUE)).thenReturn(mockedAuthRequestInfo);

        PowerMockito.doReturn(mockedTokenRequest).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "createTokenRequest", HttpServletRequest.class)).withArguments(ArgumentMatchers.any(HttpServletRequest.class));

        JWT mockedIdToken = Mockito.mock(JWT.class);
        OIDCTokenResponse mockedTokenResponse = new OIDCTokenResponse(new OIDCTokens(mockedIdToken, new AccessToken(AccessTokenType.BEARER) {

            private static final long serialVersionUID = 8087263960430794478L;

            @Override
            public String toAuthorizationHeader() {
                return null;
            }
        }, new RefreshToken()));

        PowerMockito.doReturn(mockedTokenResponse).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "getTokenResponse", TokenRequest.class)).withArguments(mockedTokenRequest);

        Mockito.when(this.mockedBackend.validateIdToken(ArgumentMatchers.any(JWT.class), ArgumentMatchers.anyString())).thenReturn(null);

        try {
            this.provider.authenticateUser(mockedRequest, mockedResponse);
        } catch (OXException e) {
            assertTrue("Wrong error message thrown. With wrong message", e.getExceptionCode() == OIDCExceptionCode.IDTOKEN_GATHERING_ERROR && e.getMessage().contains("IDToken validation failed, no claim set could be extracted"));
            return;
        }
        fail("No error was thrown, but expected");
    }

    @Test
    public void authenticateUser_SuccessTest() throws Exception {
        Mockito.when(mockedRequest.getParameter("state")).thenReturn(STATE_VALUE);
        Mockito.when(this.mockedStateManagement.getAndRemoveAuthenticationInfo(STATE_VALUE)).thenReturn(mockedAuthRequestInfo);

        PowerMockito.doReturn(mockedTokenRequest).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "createTokenRequest", HttpServletRequest.class)).withArguments(mockedRequest);

        JWT mockedIdToken = Mockito.mock(JWT.class);
        OIDCTokenResponse mockedTokenResponse = new OIDCTokenResponse(new OIDCTokens(mockedIdToken, new AccessToken(AccessTokenType.BEARER) {

            private static final long serialVersionUID = 8087263960430794478L;

            @Override
            public String toAuthorizationHeader() {
                return null;
            }
        }, new RefreshToken()));

        PowerMockito.doReturn(mockedTokenResponse).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "getTokenResponse", TokenRequest.class)).withArguments(mockedTokenRequest);

        IDTokenClaimsSet mockedClaimSet = Mockito.mock(IDTokenClaimsSet.class);
        Mockito.when(mockedAuthRequestInfo.getNonce()).thenReturn("nonce");
        Mockito.when(this.mockedBackend.validateIdToken(ArgumentMatchers.any(JWT.class), ArgumentMatchers.anyString())).thenReturn(mockedClaimSet);
        Mockito.when(mockedAuthRequestInfo.getDomainName()).thenReturn("domainname");

        PowerMockito.doNothing().when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "sendLoginRequestToServer", HttpServletRequest.class, HttpServletResponse.class, OIDCTokenResponse.class, AuthenticationRequestInfo.class)).withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(OIDCTokenResponse.class), ArgumentMatchers.any(AuthenticationRequestInfo.class));

        try {
            this.provider.authenticateUser(mockedRequest, mockedResponse);
        } catch (OXException e) {
            e.printStackTrace();
            fail("Unexpected error thrown.");
        }
    }

    @Test
    public void getLogoutRedirectRequest_viaSSOLogoutTest() throws Exception {
        Mockito.when(mockedBackendConfig.isSSOLogout()).thenReturn(true);
        Session mockedSession = Mockito.mock(Session.class);
        PowerMockito.doReturn(mockedSession).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "extractSessionFromRequest", HttpServletRequest.class)).withArguments(ArgumentMatchers.any(HttpServletRequest.class));
        String logoutRequest = "firstRequest";
        Mockito.when(mockedBackendConfig.getRpRedirectURILogout()).thenReturn(logoutRequest);
        LogoutRequest backendLogout = Mockito.mock(LogoutRequest.class);
        Mockito.when(backendLogout.getState()).thenReturn(new State());
        Mockito.when(mockedBackend.getLogoutFromIDPRequest(mockedSession)).thenReturn(backendLogout);
        PowerMockito.doNothing().when(mockedStateManagement).addLogoutRequest(ArgumentMatchers.any(DefaultLogoutRequestInfo.class), ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class));
        URI resultUri = new URI("logoutRequest");
        PowerMockito.when(backendLogout.toURI()).thenReturn(resultUri);
        String result = provider.getLogoutRedirectRequest(mockedRequest, mockedResponse);
        PowerMockito.doReturn("path").when(OIDCTools.class, "buildFrontendRedirectLocation", ArgumentMatchers.any(Session.class), ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        assertTrue("Wrong request as result", result.equals("logoutRequest"));
    }

    @Test
    public void getLogoutRedirectRequest_NoSSOLogoutTest() throws Exception {
        String logoutRequest = "correctLogoutRequest";
        Mockito.when(mockedBackendConfig.isSSOLogout()).thenReturn(false);
        PowerMockito.doReturn(mockedSession).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "extractSessionFromRequest", HttpServletRequest.class)).withArguments(mockedRequest);
        PowerMockito.doReturn(logoutRequest).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "getRedirectForLogoutFromOXServer", Session.class, HttpServletRequest.class, HttpServletResponse.class, LogoutRequestInfo.class)).withArguments(mockedSession, mockedRequest, mockedResponse, null);
        String result = provider.getLogoutRedirectRequest(mockedRequest, mockedResponse);

        assertTrue("Wrong request as result", result.equals(logoutRequest));
    }

    @Test
    public void getLogoutRedirectRequest_NoSessionIDFailTest() {
        Mockito.when(mockedRequest.getParameter("session")).thenReturn(null);
        try {
            this.provider.getLogoutRedirectRequest(mockedRequest, mockedResponse);
        } catch (OXException e) {
            assertTrue("Wrong error message thrown. With wrong message", e.getExceptionCode() == OIDCExceptionCode.INVALID_LOGOUT_REQUEST && e.getMessage().contains("No session parameter set."));
            return;
        }
        fail("No error was thrown, but expected");
    }

    @Test
    public void getLogoutRedirectRequest_NoSessionFailTest() throws Exception {
        Mockito.when(mockedRequest.getParameter("session")).thenReturn("sessionID");
        PowerMockito.doReturn(null).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "getSessionFromId", String.class)).withArguments(ArgumentMatchers.anyString());

        try {
            this.provider.getLogoutRedirectRequest(mockedRequest, mockedResponse);
        } catch (OXException e) {
            assertTrue("Wrong error message thrown. With wrong message", e.getExceptionCode() == OIDCExceptionCode.INVALID_LOGOUT_REQUEST && e.getMessage().contains("Invalid session parameter, no session found."));
            return;
        }
        fail("No error was thrown, but expected");
    }

    @Test
    public void logoutSSOUser_NoStateFailTest() {
        Mockito.when(mockedRequest.getParameter(OIDCTools.STATE)).thenReturn(null);
        try {
            this.provider.logoutSSOUser(mockedRequest, mockedResponse);
        } catch (OXException e) {
            assertTrue("Wrong error message thrown. With wrong message", e.getExceptionCode() == OIDCExceptionCode.INVALID_LOGOUT_REQUEST && e.getMessage().contains("missing state parameter in response from the OP."));
            return;
        }
        fail("No error was thrown, but expected");
    }

    @Test
    public void logoutSSOUser_NoStoredStateFailTest() throws OXException {
        String state = "state";
        Mockito.when(mockedRequest.getParameter(OIDCTools.STATE)).thenReturn(state);
        Mockito.when(this.mockedStateManagement.getAndRemoveLogoutRequestInfo(state)).thenReturn(null);
        try {
            this.provider.logoutSSOUser(mockedRequest, mockedResponse);
        } catch (OXException e) {
            assertTrue("Wrong error message thrown. With wrong message", e.getExceptionCode() == OIDCExceptionCode.INVALID_LOGOUT_REQUEST && e.getMessage().contains("wrong state in response from the OP."));
            return;
        }
        fail("No error was thrown, but expected");
    }

    @Test
    public void logoutSSOUser_SuccessTest() throws Exception {
        String state = "state";
        Mockito.when(mockedRequest.getParameter(OIDCTools.STATE)).thenReturn(state);
        LogoutRequestInfo logoutRequestInfo = Mockito.mock(LogoutRequestInfo.class);
        Mockito.when(this.mockedStateManagement.getAndRemoveLogoutRequestInfo(state)).thenReturn(logoutRequestInfo);
        String sessionID = "sessionId";
        Mockito.when(logoutRequestInfo.getSessionId()).thenReturn(sessionID);
        PowerMockito.doReturn(mockedSession).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "getSessionFromId", String.class)).withArguments(sessionID);
        String logoutRequest = "logoutRequest";
        PowerMockito.doReturn(logoutRequest).when(this.provider, PowerMockito.method(OIDCWebSSOProviderImpl.class, "getRedirectForLogoutFromOXServer", Session.class, HttpServletRequest.class, HttpServletResponse.class, LogoutRequestInfo.class)).withArguments(mockedSession, mockedRequest, mockedResponse, logoutRequestInfo);
        
        String result = this.provider.logoutSSOUser(mockedRequest, mockedResponse);
        assertTrue("Wrong request as result", result.equals(logoutRequest));
    }
    
    @Test
    public void validateThirdPartyRequest_NoIssParameterTest() {
        Mockito.when(mockedRequest.getParameter("iss")).thenReturn(null);
        boolean result = provider.validateThirdPartyRequest(mockedRequest);
        assertFalse("Input should not have passed validation", result);
        Mockito.when(mockedRequest.getParameter("iss")).thenReturn("");
        result = provider.validateThirdPartyRequest(mockedRequest);
        assertFalse("Input should not have passed validation", result);
    }
    
    @Test
    public void validateThirdPartyRequest_NoMatchingIssParameterTest() {
        Mockito.when(mockedRequest.getParameter("iss")).thenReturn("wrong");
        Mockito.when(mockedBackendConfig.getOpIssuer()).thenReturn("correct");
        boolean result = provider.validateThirdPartyRequest(mockedRequest);
        assertFalse("Input should not have passed validation", result);
    }
    
    @Test
    public void validateThirdPartyRequest_PassTest() {
        Mockito.when(mockedRequest.getParameter("iss")).thenReturn("correct");
        Mockito.when(mockedBackendConfig.getOpIssuer()).thenReturn("correct");
        boolean result = provider.validateThirdPartyRequest(mockedRequest);
        assertTrue("Input should have passed validation", result);
    }
}
