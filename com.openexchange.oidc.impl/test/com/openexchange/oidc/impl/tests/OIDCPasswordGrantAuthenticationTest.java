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

package com.openexchange.oidc.impl.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.modules.junit4.PowerMockRunner;
import com.google.common.collect.ImmutableMap;
import com.nimbusds.jose.proc.BadJWSException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.DefaultLoginInfo;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.util.UUIDs;
import com.openexchange.oidc.AuthenticationInfo;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.osgi.OIDCBackendRegistry;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.spi.OIDCCoreBackend;
import com.openexchange.oidc.tools.MockablePasswordGrantAuthentication;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.oidc.tools.TestBackendConfig;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.EnhancedAuthenticated;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link OIDCPasswordGrantAuthenticationTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
@RunWith(PowerMockRunner.class)
public class OIDCPasswordGrantAuthenticationTest {

    @Mock
    private ServiceLookup mockedServices;

    @Mock
    private User user;

    @Mock
    private Context context;

    @Mock
    private UserService userService;

    @Mock
    private ContextService contextService;

    @Mock
    private OIDCBackendRegistry backendRegistry;

    @Spy
    private OIDCBackend backend = new OIDCCoreBackend();

    @Spy
    private OIDCBackendConfig config = new TestBackendConfig();

    private MockablePasswordGrantAuthentication authenticationService;

    private int userId;

    private int contextId;

    private String username;

    private String loginmapping;

    private String password;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        userId = ThreadLocalRandom.current().nextInt(1);
        contextId = ThreadLocalRandom.current().nextInt(1);
        username = "testuser";
        loginmapping = "example.com";
        password = "Secret123!";

        when(user.getId()).thenReturn(userId);
        when(user.getLoginInfo()).thenReturn(username);

        when(context.getContextId()).thenReturn(contextId);
        when(context.getLoginInfo()).thenReturn(new String[] { loginmapping });

        when(userService.getUser(userId, context)).thenReturn(user);
        when(contextService.getContext(contextId)).thenReturn(context);

        Services.services.set(mockedServices);
        when(mockedServices.getService(UserService.class)).thenReturn(userService);
        when(mockedServices.getService(ContextService.class)).thenReturn(contextService);

        doReturn(config).when(backend).getBackendConfig();
        when(backendRegistry.getAllRegisteredBackends()).thenReturn(Collections.singletonList(backend));

        authenticationService = Mockito.spy(new MockablePasswordGrantAuthentication(backendRegistry));
    }


    /**
     * Successful login with username/password
     */
    @Test
    public void loginWithUsernameAndPassword_Success() throws Exception {
        DefaultLoginInfo loginInfo = new DefaultLoginInfo(username, password);
        TokenRequest tokenRequest = Mockito.mock(TokenRequest.class);
        doReturn(tokenRequest).when(authenticationService).buildTokenRequest(backend, loginInfo.getUsername(), loginInfo.getPassword());

        String accessTokenValue = UUIDs.getUnformattedStringFromRandom();
        String refreshTokenValue = UUIDs.getUnformattedStringFromRandom();
        String idTokenValue = UUIDs.getUnformattedStringFromRandom();
        BearerAccessToken accessToken = new BearerAccessToken(accessTokenValue, 3600l, Scope.parse("openid"));
        RefreshToken refreshToken = new RefreshToken(refreshTokenValue);
        JWT mockedIdToken = Mockito.mock(JWT.class);
        when(mockedIdToken.serialize()).thenReturn(idTokenValue);
        OIDCTokenResponse tokenResponse = new OIDCTokenResponse(new OIDCTokens(mockedIdToken, accessToken, refreshToken));
        doReturn(tokenResponse).when(authenticationService).sendTokenRequest(backend, tokenRequest);

        IDTokenClaimsSet claimsSet = Mockito.mock(IDTokenClaimsSet.class);
        doReturn(claimsSet).when(backend).validateIdToken(mockedIdToken, null);
        doReturn(new AuthenticationInfo(contextId, userId)).when(backend).resolveAuthenticationResponse(loginInfo, tokenResponse);

        Authenticated authenticated = authenticationService.handleLoginInfo(loginInfo);
        assertNotNull(authenticated);
        assertEquals(loginmapping, authenticated.getContextInfo());
        assertEquals(username, authenticated.getUserInfo());
        verify(backend).validateIdToken(mockedIdToken, null);
        verify(backend).resolveAuthenticationResponse(loginInfo, tokenResponse);
        verify(backend).enhanceAuthenticated(ArgumentCaptor.forClass(Authenticated.class).capture(), ArgumentCaptor.forClass(Map.class).capture());

        Session session = Mockito.spy(Session.class);
        assertTrue(authenticated instanceof EnhancedAuthenticated);
        ((EnhancedAuthenticated) authenticated).enhanceSession(session);


        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
        verify(session, Mockito.atLeast(5)).setParameter(keyCaptor.capture(), valueCaptor.capture());

        ImmutableMap<Object, Object> sessionParamMatchers = ImmutableMap.builder()
            .put(OIDCTools.IDTOKEN, Matchers.equalTo(idTokenValue))
            .put(Session.PARAM_OAUTH_ACCESS_TOKEN, Matchers.equalTo(accessTokenValue))
            .put(Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE, TimestampStringMatcher.of(System.currentTimeMillis(), 5000l))
            .put(Session.PARAM_OAUTH_REFRESH_TOKEN, Matchers.equalTo(refreshTokenValue))
            .put(OIDCTools.BACKEND_PATH, Matchers.equalTo(""))
            .build();


        for (Entry<Object, Object> entry : sessionParamMatchers.entrySet()) {
            int indexOf = keyCaptor.getAllValues().indexOf(entry.getKey());
            assertTrue("Missing key in session properties: " + entry.getKey(), indexOf >= 0);
            Object value = valueCaptor.getAllValues().get(indexOf);
            assertThat((String) value, (Matcher<String>) entry.getValue());
        }
    }

    /**
     * Login with invalid username/password combination
     */
    @Test
    public void loginWithUsernameAndPassword_InvalidCredentials() throws Exception {
        DefaultLoginInfo loginInfo = new DefaultLoginInfo(username, "INVALID!");
        TokenRequest tokenRequest = Mockito.mock(TokenRequest.class);
        doReturn(tokenRequest).when(authenticationService).buildTokenRequest(backend, loginInfo.getUsername(), loginInfo.getPassword());

        TokenErrorResponse tokenResponse = new TokenErrorResponse(OAuth2Error.INVALID_GRANT);
        doReturn(tokenResponse).when(authenticationService).sendTokenRequest(backend, tokenRequest);

        OXException error = null;
        try {
            authenticationService.handleLoginInfo(loginInfo);
        } catch (OXException e) {
            error = e;
        }

        assertTrue(LoginExceptionCodes.INVALID_CREDENTIALS.equals(error));
    }

    /**
     * SSO returns token error response for any other reason than invalid credentials
     */
    @Test
    public void loginWithUsernameAndPassword_ConfigOrRuntimeIssue() throws Exception {
        DefaultLoginInfo loginInfo = new DefaultLoginInfo(username, password);
        TokenRequest tokenRequest = Mockito.mock(TokenRequest.class);
        doReturn(tokenRequest).when(authenticationService).buildTokenRequest(backend, loginInfo.getUsername(), loginInfo.getPassword());

        List<ErrorObject> possibleErrors = new LinkedList<>();
        for (Field field : OAuth2Error.class.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && field.getType().equals(ErrorObject.class)) {
                ErrorObject errorObject = (ErrorObject) field.get(null);
                if (!errorObject.equals(OAuth2Error.INVALID_GRANT)) {
                    possibleErrors.add(errorObject);
                }
            }
        }

        assertTrue("No error fields found for testing!", possibleErrors.size() > 0);

        for (ErrorObject errorObject : possibleErrors) {
            TokenErrorResponse tokenResponse = new TokenErrorResponse(errorObject);
            doReturn(tokenResponse).when(authenticationService).sendTokenRequest(backend, tokenRequest);

            OXException error = null;
            try {
                authenticationService.handleLoginInfo(loginInfo);
            } catch (OXException e) {
                error = e;
            }

            assertTrue(LoginExceptionCodes.UNKNOWN.equals(error));
        }
    }

    /**
     * Login with correct credentials, but invalid ID token was returned
     */
    @Test
    public void loginWithUsernameAndPassword_InvalidIDTokenSignature() throws Exception {
        DefaultLoginInfo loginInfo = new DefaultLoginInfo(username, password);
        TokenRequest tokenRequest = Mockito.mock(TokenRequest.class);
        doReturn(tokenRequest).when(authenticationService).buildTokenRequest(backend, loginInfo.getUsername(), loginInfo.getPassword());

        JWT mockedIdToken = Mockito.mock(JWT.class);
        when(mockedIdToken.serialize()).thenReturn("invalidIDToken");
        OIDCTokenResponse tokenResponse = new OIDCTokenResponse(new OIDCTokens(
            mockedIdToken,
            new BearerAccessToken(UUIDs.getUnformattedStringFromRandom()),
            new RefreshToken()));

        doReturn(tokenResponse).when(authenticationService).sendTokenRequest(backend, tokenRequest);
        BadJWSException invalidSignatureException = new BadJWSException("Signed JWT rejected: Invalid signature");
        OXException validationException = OIDCExceptionCode.IDTOKEN_VALIDATON_FAILED_CONTENT.create(
            invalidSignatureException,
            invalidSignatureException.getMessage());
        doThrow(validationException).when(backend).validateIdToken(mockedIdToken, null);

        OXException error = null;
        try {
            authenticationService.handleLoginInfo(loginInfo);
        } catch (OXException e) {
            error = e;
        }

        assertTrue(LoginExceptionCodes.LOGIN_DENIED.equals(error));
    }

    private static final class TimestampStringMatcher extends BaseMatcher<String> {

        private final long timestamp;

        private final long error;

        TimestampStringMatcher(long timestamp, long error) {
            this.timestamp = timestamp;
            this.error = error;
        }

        static TimestampStringMatcher of(long timestamp, long error) {
            return new TimestampStringMatcher(timestamp, error);
        }

        @Override
        public boolean matches(Object item) {
            if (item instanceof String) {
                try {
                    long actual = Long.parseLong((String) item);
                    return actual >= timestamp - error;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a timestamp string close to ").appendValue(timestamp);

        }
    }

}
