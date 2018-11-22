
package com.openexchange.oidc.spi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
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
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.Reservation;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.user.UserService;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AbstractOIDCBackend.class, OIDCTools.class, LoginPerformer.class, SessionUtility.class, LoginConfiguration.class, Services.class})
public class AbstractOIDCBackendTest {

    private static final String HASH = "hash";
    private static final int USER_ID = 1;
    private static final String SESSION_ID = "SessionID";
    private static final int CONTEXT_ID = 1;
    private static final String ID_TOKEN = "IDToken";
    private static final String SESSION_TOKEN = "SessionToken";
    private static final String SCOPE_ONE = "scopeone";
    private static final String SCOPE_TWO = "scopetwo";

    @Mock
    private OIDCBackendConfig mockedBackendConfig;
    
    @Mock
    private Session mockedSession;
    
    @Mock
    private HttpServletRequest mockedRequest;
    
    @Mock
    private HttpServletResponse mockedResponse;
    
    @Mock
    private SessionReservationService mockedSessionReservationService;
    
    @Mock
    private Reservation mockedReservation;
    
    @Mock
    private ContextService mockedContextService;
    
    @Mock
    private Context mockedContext;
    
    @Mock
    private UserService mockedUserService;
    
    @Mock
    private User mockedUser;
    
    @Mock
    private LoginResult mockedLoginResult;
    
    @Mock
    private Cookie mockedCookie;
    
    @Mock
    private SessionStorageService mockedSessionStorage;
    
    @Mock
    private LoginConfiguration mockedLoginConfig;
    
    @Mock
    private SessiondService mockedSessiondService;
    
    private OIDCBackend testBackend;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);
        PowerMockito.mockStatic(OIDCTools.class);
        PowerMockito.stub(PowerMockito.method(AbstractOIDCBackend.class, "getBackendConfig")).toReturn(mockedBackendConfig);
        this.testBackend = new OIDCCoreBackend() {

            @Override
            public OIDCBackendConfig getBackendConfig() {
                return mockedBackendConfig;
            }

        };
    }

    @Test
    public void getJWSAlgorithm_StandardTest() throws OXException {
        Mockito.when(mockedBackendConfig.getJWSAlgortihm()).thenReturn(null);
        
        JWSAlgorithm jwsAlgorithm = this.testBackend.getJWSAlgorithm();
        assertTrue("Failed to use standard algorith", jwsAlgorithm.toString().equals(JWSAlgorithm.RS256.toString()));
    }

    @Test
    public void getJWSAlgorithm_FromConfigTest() throws OXException {
        Mockito.when(mockedBackendConfig.getJWSAlgortihm()).thenReturn(JWSAlgorithm.ES256.toString());
        JWSAlgorithm jwsAlgorithm = this.testBackend.getJWSAlgorithm();
        assertTrue("Failed to use standard algorith", jwsAlgorithm.toString().equals(JWSAlgorithm.ES256.toString()));

    }

    @Test
    public void getJWSAlgorithm_FailTest() {
        Mockito.when(mockedBackendConfig.getJWSAlgortihm()).thenReturn("12345");
        try {
            this.testBackend.getJWSAlgorithm();
        } catch (OXException e) {
            assertTrue("Wrong JWSAlgorithm was accepted", OIDCExceptionCode.UNABLE_TO_PARSE_JWS_ALGORITHM == e.getExceptionCode());
        }
    }

    @Test
    public void getScope_SingleTest() {
        Mockito.when(mockedBackendConfig.getScope()).thenReturn(SCOPE_ONE);
        List<String> scopeList = this.testBackend.getScope().toStringList();
        assertTrue("Not the number of scopes that were expected, should be one", scopeList.size() == 1);
        assertTrue("Scope is not what expected", scopeList.get(0).equals(SCOPE_ONE));
    }

    @Test
    public void getScope_MultipleTest() {
        Mockito.when(mockedBackendConfig.getScope()).thenReturn(SCOPE_ONE + ";" + SCOPE_TWO);
        List<String> scopeList = this.testBackend.getScope().toStringList();
        assertTrue("Not the number of scopes that were expected, should be two", scopeList.size() == 2);
        assertTrue("Scope is not what expected", scopeList.get(0).equals(SCOPE_ONE));
        assertTrue("Scope is not what expected", scopeList.get(1).equals(SCOPE_TWO));
    }

    @Test
    public void updateOauthTokens_TokenGatheringFailTest() throws Exception {
        
        // Spy on the AbstractOIDCBackend to mock private method
        AbstractOIDCBackend abstractBackend = PowerMockito.spy(new OIDCCoreBackend() {

            @Override
            public OIDCBackendConfig getBackendConfig() {
                return mockedBackendConfig;
            }

        });
        
        // Mock the token response to give null tokens back
        AccessTokenResponse tokenResponse = new AccessTokenResponse(new Tokens(new AccessToken(AccessTokenType.BEARER) {
            private static final long serialVersionUID = 1L;

            @Override
            public String toAuthorizationHeader() {
                return null;
            }
            
        }, new RefreshToken())) {
            @Override
            public Tokens getTokens() {
                return null;
            }
        };
        
        
        PowerMockito.doReturn(tokenResponse).when(abstractBackend, "loadAccessToken", this.mockedSession);
        
        try {
            boolean updateOauthTokens = abstractBackend.updateOauthTokens(this.mockedSession);
            assertFalse("False expected, got true", updateOauthTokens);
        } catch (OXException e) {
            e.printStackTrace();
            fail("No errors should occur");
        }
    }

    @Test
    public void updateOauthTokens_PassTest() throws Exception {
        // Spy on the AbstractOIDCBackend to mock private method
        AbstractOIDCBackend abstractBackend = PowerMockito.spy(new OIDCCoreBackend() {

            @Override
            public OIDCBackendConfig getBackendConfig() {
                return mockedBackendConfig;
            }

        });
        
        // Mock the token response to give null tokens back
        AccessTokenResponse tokenResponse = new AccessTokenResponse(new Tokens(new AccessToken(AccessTokenType.BEARER) {
            private static final long serialVersionUID = 1L;

            @Override
            public String toAuthorizationHeader() {
                return null;
            }
            
        }, new RefreshToken()));
        
        PowerMockito.doReturn(tokenResponse).when(abstractBackend, "loadAccessToken", this.mockedSession);
        PowerMockito.doNothing().when(abstractBackend, "updateSession", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(Map.class));
        try {
            boolean updateOauthTokens = abstractBackend.updateOauthTokens(this.mockedSession);
            assertTrue("True expected, got false", updateOauthTokens);
        } catch (OXException e) {
            e.printStackTrace();
            fail("No errors should occur");
        }
    }

    @Test
    public void isTokenExpired_TrueTest() {
        Mockito.when(mockedBackendConfig.getOauthRefreshTime()).thenReturn(10000);
        Mockito.when(mockedSession.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE)).thenReturn(String.valueOf(new Date().getTime()));
        Mockito.when(mockedSession.containsParameter(Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE)).thenReturn(true);
        try {
            boolean result = this.testBackend.isTokenExpired(mockedSession);
            assertTrue("True expected, got false", result);
        } catch (OXException e) {
            e.printStackTrace();
            fail("No error should happen");
        }
    }
    
    @Test
    public void isTokenExpired_FalseTest() {
        Mockito.when(mockedBackendConfig.getOauthRefreshTime()).thenReturn(10000);
        Mockito.when(mockedSession.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE)).thenReturn(String.valueOf(new Date().getTime() + 100000));
        Mockito.when(mockedSession.containsParameter(Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE)).thenReturn(true);
        try {
            boolean result = this.testBackend.isTokenExpired(mockedSession);
            assertFalse("False expected, got true", result);
        } catch (OXException e) {
            e.printStackTrace();
            fail("No error should happen");
        }
    }

    @Test
    public void isTokenExpired_NoParamTest() {
        Mockito.when(mockedBackendConfig.getOauthRefreshTime()).thenReturn(0);
        try {
            boolean result = this.testBackend.isTokenExpired(mockedSession);
            assertFalse("False expected, got true", result);
        } catch (OXException e) {
            e.printStackTrace();
            fail("No error should happen");
        }
    }

    @Test
    public void logoutCurrentUser_NoOIDCCookieDeletionTest() throws Exception {
        
        PowerMockito.mockStatic(LoginPerformer.class);
        PowerMockito.mockStatic(SessionUtility.class);
        PowerMockito.doNothing().when(OIDCTools.class, "validateSession", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class));
        LoginPerformer loginPerformer = Mockito.mock(LoginPerformer.class);
        PowerMockito.doReturn(loginPerformer).when(LoginPerformer.class, "getInstance");
        Mockito.when(loginPerformer.doLogout(ArgumentMatchers.anyString())).thenReturn(mockedSession);
        PowerMockito.doNothing().when(SessionUtility.class, "removeOXCookies", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class));
        PowerMockito.doNothing().when(SessionUtility.class, "removeJSESSIONID", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class));
        Mockito.when(mockedBackendConfig.isAutologinEnabled()).thenReturn(false);
        
        //PowerMockito.verifyStatic(Mockito.times(0));
        SessionUtility.removeCookie(ArgumentMatchers.any(javax.servlet.http.Cookie.class), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpServletResponse.class));
        
        HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse mockedResponse = Mockito.mock(HttpServletResponse.class);
        this.testBackend.logoutCurrentUser(mockedSession, mockedRequest, mockedResponse);
        
    }
    
    @Test
    public void performLogin_NoSessionTokenFailTest() {
        Mockito.when(mockedRequest.getParameter(OIDCTools.SESSION_TOKEN)).thenReturn(null);
            try {
                testBackend.performLogin(mockedRequest, mockedResponse, true);
            } catch (@SuppressWarnings("unused") IOException | JSONException e) {
                fail("Wrong exception thrown.");
                return;
            } catch (OXException e) {
                assertTrue("Wrong error message thrown", e.getExceptionCode() == AjaxExceptionCodes.BAD_REQUEST);
                return;
            } 
        fail("An error was expected but not thrown.");
    }
    
    @Test
    public void performLogin_NoReservationFailTest() throws Exception {
        Mockito.when(mockedRequest.getParameter(OIDCTools.SESSION_TOKEN)).thenReturn(SESSION_TOKEN);
        PowerMockito.doReturn(mockedSessionReservationService).when(Services.class, "getService", SessionReservationService.class);
        Mockito.when(mockedSessionReservationService.removeReservation(SESSION_TOKEN)).thenReturn(null);
        try {
            testBackend.performLogin(mockedRequest, mockedResponse, true);
        } catch (@SuppressWarnings("unused") IOException | JSONException e) {
            fail("Wrong exception thrown.");
            return;
        } catch (OXException e) {
            assertTrue("Wrong error message thrown", e.getExceptionCode() == LoginExceptionCodes.INVALID_CREDENTIALS);
            return;
        } 
        fail("An error was expected but not thrown.");
    }
    
    @Test
    public void performLogin_NoIDTokenFailTest() throws Exception {
        Mockito.when(mockedRequest.getParameter(OIDCTools.SESSION_TOKEN)).thenReturn(SESSION_TOKEN);
        PowerMockito.doReturn(mockedSessionReservationService).when(Services.class, "getService", SessionReservationService.class);
        Mockito.when(mockedSessionReservationService.removeReservation(SESSION_TOKEN)).thenReturn(mockedReservation);
        Mockito.when(mockedReservation.getState()).thenReturn(new HashMap<String,String>());
        try {
            testBackend.performLogin(mockedRequest, mockedResponse, true);
        } catch (@SuppressWarnings("unused") IOException | JSONException e) {
            fail("Wrong exception thrown.");
            return;
        } catch (OXException e) {
            assertTrue("Wrong error message thrown", e.getExceptionCode() == AjaxExceptionCodes.BAD_REQUEST);
            return;
        } 
        fail("An error was expected but not thrown.");
    }
    
    @Test
    public void performLogin_NoContextServiceFailTest() throws Exception {
        Mockito.when(mockedRequest.getParameter(OIDCTools.SESSION_TOKEN)).thenReturn(SESSION_TOKEN);
        PowerMockito.doReturn(mockedSessionReservationService).when(Services.class, "getService", SessionReservationService.class);
        Mockito.when(mockedSessionReservationService.removeReservation(SESSION_TOKEN)).thenReturn(mockedReservation);
        HashMap<String, String> idTokenMap = new HashMap<String,String>();
        idTokenMap.put(OIDCTools.IDTOKEN, ID_TOKEN);
        Mockito.when(mockedReservation.getState()).thenReturn(idTokenMap);
        PowerMockito.doReturn(mockedContextService).when(Services.class, "getService", ContextService.class);
        Mockito.when(mockedReservation.getContextId()).thenReturn(CONTEXT_ID);
        Mockito.when(mockedContextService.getContext(CONTEXT_ID)).thenReturn(mockedContext);
        Mockito.when(mockedContext.isEnabled()).thenReturn(false);
        
        try {
            testBackend.performLogin(mockedRequest, mockedResponse, true);
        } catch (@SuppressWarnings("unused") IOException | JSONException e) {
            fail("Wrong exception thrown.");
            return;
        } catch (OXException e) {
            assertTrue("Wrong error message thrown", e.getExceptionCode() == LoginExceptionCodes.INVALID_CREDENTIALS);
            return;
        } 
        fail("An error was expected but not thrown.");
    }
    
    @Test
    public void performLogin_NoUserServiceFailTest() throws Exception {
        Mockito.when(mockedRequest.getParameter(OIDCTools.SESSION_TOKEN)).thenReturn(SESSION_TOKEN);
        PowerMockito.doReturn(mockedSessionReservationService).when(Services.class, "getService", SessionReservationService.class);
        Mockito.when(mockedSessionReservationService.removeReservation(SESSION_TOKEN)).thenReturn(mockedReservation);
        HashMap<String, String> idTokenMap = new HashMap<String,String>();
        idTokenMap.put(OIDCTools.IDTOKEN, ID_TOKEN);
        Mockito.when(mockedReservation.getState()).thenReturn(idTokenMap);
        PowerMockito.doReturn(mockedContextService).when(Services.class, "getService", ContextService.class);
        Mockito.when(mockedReservation.getContextId()).thenReturn(CONTEXT_ID);
        Mockito.when(mockedContextService.getContext(CONTEXT_ID)).thenReturn(mockedContext);
        Mockito.when(mockedContext.isEnabled()).thenReturn(true);
        PowerMockito.doReturn(mockedUserService).when(Services.class, "getService", UserService.class);
        Mockito.when(mockedReservation.getUserId()).thenReturn(2);
        Mockito.when(mockedUserService.getUser(2, mockedContext)).thenReturn(mockedUser);
        Mockito.when(mockedUser.isMailEnabled()).thenReturn(false);
        
        try {
            testBackend.performLogin(mockedRequest, mockedResponse, true);
        } catch (@SuppressWarnings("unused") IOException | JSONException e) {
            fail("Wrong exception thrown.");
            return;
        } catch (OXException e) {
            assertTrue("Wrong error message thrown", e.getExceptionCode() == LoginExceptionCodes.INVALID_CREDENTIALS);
            return;
        } 
        fail("An error was expected but not thrown.");
    }
    
    /*
     * Shorter form for Autologin is AL, Only autologin from here on
     */
    
    private void setUpForLogin() throws Exception {
        Mockito.when(mockedRequest.getParameter(OIDCTools.SESSION_TOKEN)).thenReturn(SESSION_TOKEN);
        PowerMockito.doReturn(mockedSessionReservationService).when(Services.class, "getService", SessionReservationService.class);
        Mockito.when(mockedSessionReservationService.removeReservation(SESSION_TOKEN)).thenReturn(mockedReservation);
        HashMap<String, String> idTokenMap = new HashMap<String,String>();
        idTokenMap.put(OIDCTools.IDTOKEN, ID_TOKEN);
        Mockito.when(mockedReservation.getState()).thenReturn(idTokenMap);
        PowerMockito.doReturn(mockedContextService).when(Services.class, "getService", ContextService.class);
        Mockito.when(mockedReservation.getContextId()).thenReturn(CONTEXT_ID);
        Mockito.when(mockedContextService.getContext(CONTEXT_ID)).thenReturn(mockedContext);
        Mockito.when(mockedContext.isEnabled()).thenReturn(true);
        PowerMockito.doReturn(mockedUserService).when(Services.class, "getService", UserService.class);
        Mockito.when(mockedReservation.getUserId()).thenReturn(USER_ID);
        Mockito.when(mockedUserService.getUser(USER_ID, mockedContext)).thenReturn(mockedUser);
        Mockito.when(mockedUser.isMailEnabled()).thenReturn(true);
    }
    
    @Test
    public void performLogin_CookieALNoSSOCookieLoginTest() throws Exception {
        
        // Spy on the AbstractOIDCBackend to mock private method
        AbstractOIDCBackend abstractBackend = PowerMockito.spy(new OIDCCoreBackend() {

            @Override
            public OIDCBackendConfig getBackendConfig() {
                return mockedBackendConfig;
            }
        });
        
        setUpForLogin();
        Mockito.when(mockedBackendConfig.isAutologinEnabled()).thenReturn(true);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());
        PowerMockito.doReturn(null).when(OIDCTools.class, "loadAutologinCookie", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(LoginConfiguration.class));
        PowerMockito.doReturn(HASH).when(OIDCTools.class, "calculateHash", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(LoginConfiguration.class));

        PowerMockito.doNothing().when(abstractBackend, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));
        
        PowerMockito.doReturn(mockedLoginResult)
            .when(abstractBackend, PowerMockito
                .method(AbstractOIDCBackend.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(abstractBackend, PowerMockito
            .method(AbstractOIDCBackend.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        try {
             abstractBackend.performLogin(mockedRequest, mockedResponse, true);
             PowerMockito.verifyPrivate(abstractBackend, Mockito.times(1)).invoke("loginUser", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());
        } catch (@SuppressWarnings("unused") IOException | JSONException e) {
            fail("An error was not expected but thrown.");
            return;
        } catch (@SuppressWarnings("unused") OXException e) {
            fail("An error was not expected but thrown.");            
            return;
        } 
    }
    
    @Test
    public void performLogin_CookieALNoSessionInCookieFailTest() throws Exception {
        // Spy on the AbstractOIDCBackend to mock private method
        AbstractOIDCBackend abstractBackend = PowerMockito.spy(new OIDCCoreBackend() {

            @Override
            public OIDCBackendConfig getBackendConfig() {
                return mockedBackendConfig;
            }
        });
        
        setUpForLogin();
        Mockito.when(mockedBackendConfig.isAutologinEnabled()).thenReturn(true);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());
        PowerMockito.doReturn(mockedCookie).when(OIDCTools.class, "loadAutologinCookie", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(LoginConfiguration.class));
        PowerMockito.doNothing().when(abstractBackend, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));
        PowerMockito.doReturn(null).when(OIDCTools.class, "getSessionFromAutologinCookie", ArgumentMatchers.any(Cookie.class), ArgumentMatchers.any(HttpServletRequest.class));
        PowerMockito.doReturn(HASH).when(OIDCTools.class, "calculateHash", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(LoginConfiguration.class));

        Mockito.when(mockedCookie.clone()).thenReturn(mockedCookie);
        
        PowerMockito.doReturn(mockedLoginResult)
            .when(abstractBackend, PowerMockito
                .method(AbstractOIDCBackend.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(abstractBackend, PowerMockito
            .method(AbstractOIDCBackend.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        try {
             abstractBackend.performLogin(mockedRequest, mockedResponse, true);
             PowerMockito.verifyPrivate(abstractBackend, Mockito.times(1)).invoke("loginUser", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());
        } catch (@SuppressWarnings("unused") IOException | JSONException e) {
            fail("An error was not expected but thrown.");
            return;
        } catch (@SuppressWarnings("unused") OXException e) {
            fail("An error was not expected but thrown.");            
            return;
        }
    }
    
    @Test
    public void performLogin_CookieALUpdateSessionSessionStorageTest() throws Exception {
        // Spy on the AbstractOIDCBackend to mock private method
        AbstractOIDCBackend abstractBackend = PowerMockito.spy(new OIDCCoreBackend() {

            @Override
            public OIDCBackendConfig getBackendConfig() {
                return mockedBackendConfig;
            }
        });
        
        setUpForLogin();
        PowerMockito.doReturn(mockedSessionStorage).when(Services.class, "getService", SessionStorageService.class);

        Mockito.when(mockedBackendConfig.isAutologinEnabled()).thenReturn(true);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());
        
        
        
        PowerMockito.doReturn(mockedLoginConfig).when(abstractBackend, "getLoginConfiguration");
        PowerMockito.doReturn(mockedCookie).when(OIDCTools.class, "loadAutologinCookie", mockedRequest, mockedLoginConfig);
        PowerMockito.doNothing().when(abstractBackend, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));
        
        PowerMockito.doReturn(mockedSession).when(OIDCTools.class, "getSessionFromAutologinCookie", mockedCookie, mockedRequest);
        
        Mockito.when(mockedCookie.clone()).thenReturn(mockedCookie);
        
        PowerMockito.doNothing().when(abstractBackend, "writeSessionDataAsJson", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletResponse.class));

        PowerMockito.doReturn(mockedLoginResult)
            .when(abstractBackend, PowerMockito
                .method(AbstractOIDCBackend.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(abstractBackend, PowerMockito
            .method(AbstractOIDCBackend.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        try {
             abstractBackend.performLogin(mockedRequest, mockedResponse, true);
             PowerMockito.verifyPrivate(mockedSessionStorage, Mockito.times(1)).invoke("addSession", mockedSession);
        } catch (@SuppressWarnings("unused") IOException | JSONException e) {
            fail("An error was not expected but thrown.");
            return;
        } catch (@SuppressWarnings("unused") OXException e) {
            fail("An error was not expected but thrown.");            
            return;
        }
    }
    
    @Test
    public void performLogin_CookieALUpdateSessionSessionDServTest() throws Exception {
        // Spy on the AbstractOIDCBackend to mock private method
        AbstractOIDCBackend abstractBackend = PowerMockito.spy(new OIDCCoreBackend() {

            @Override
            public OIDCBackendConfig getBackendConfig() {
                return mockedBackendConfig;
            }
        });
        
        setUpForLogin();
        PowerMockito.doReturn(null).when(Services.class, "getService", SessionStorageService.class);
        PowerMockito.doReturn(mockedSessiondService).when(Services.class, "getService", SessiondService.class);

        Mockito.when(mockedBackendConfig.isAutologinEnabled()).thenReturn(true);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());
        PowerMockito.doReturn(mockedLoginConfig).when(abstractBackend, "getLoginConfiguration");
        PowerMockito.doReturn(mockedCookie).when(OIDCTools.class, "loadAutologinCookie", mockedRequest, mockedLoginConfig);
        PowerMockito.doNothing().when(abstractBackend, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));
        
        PowerMockito.doReturn(mockedSession).when(OIDCTools.class, "getSessionFromAutologinCookie", ArgumentMatchers.any(Cookie.class), ArgumentMatchers.any(HttpServletRequest.class));
        
        Mockito.when(mockedCookie.clone()).thenReturn(mockedCookie);
        
        PowerMockito.doNothing().when(abstractBackend, "writeSessionDataAsJson", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletResponse.class));

        PowerMockito.doReturn(mockedLoginResult)
            .when(abstractBackend, PowerMockito
                .method(AbstractOIDCBackend.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(abstractBackend, PowerMockito
            .method(AbstractOIDCBackend.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        Mockito.when(mockedSession.getSessionID()).thenReturn(SESSION_ID);
        try {
             abstractBackend.performLogin(mockedRequest, mockedResponse, true);
             PowerMockito.verifyPrivate(mockedSessiondService, Mockito.times(1)).invoke("storeSession", SESSION_ID);
        } catch (@SuppressWarnings("unused") IOException | JSONException e) {
            fail("An error was not expected but thrown.");
            return;
        } catch (@SuppressWarnings("unused") OXException e) {
            fail("An error was not expected but thrown.");            
            return;
        }
    }
    
    @Test
    public void performLogin_CookieALSessionAsJSONTest() throws Exception {
        // Spy on the AbstractOIDCBackend to mock private method
        AbstractOIDCBackend abstractBackend = PowerMockito.spy(new OIDCCoreBackend() {

            @Override
            public OIDCBackendConfig getBackendConfig() {
                return mockedBackendConfig;
            }
        });
        
        setUpForLogin();
        PowerMockito.doReturn(null).when(Services.class, "getService", SessionStorageService.class);
        PowerMockito.doReturn(mockedSessiondService).when(Services.class, "getService", SessiondService.class);

        Mockito.when(mockedBackendConfig.isAutologinEnabled()).thenReturn(true);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());
        
        PowerMockito.doReturn(mockedLoginConfig).when(abstractBackend, "getLoginConfiguration");
        PowerMockito.doReturn(mockedCookie).when(OIDCTools.class, "loadAutologinCookie", mockedRequest, mockedLoginConfig);
        
        PowerMockito.doNothing().when(abstractBackend, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));
        
        PowerMockito.doReturn(mockedSession).when(OIDCTools.class, "getSessionFromAutologinCookie", ArgumentMatchers.any(Cookie.class), ArgumentMatchers.any(HttpServletRequest.class));
        
        Mockito.when(mockedCookie.clone()).thenReturn(mockedCookie);
        
        PowerMockito.doNothing().when(abstractBackend, "updateSession", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(Map.class));
        
        PowerMockito.doNothing().when(abstractBackend, "writeSessionDataAsJson", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletResponse.class));

        PowerMockito.doReturn(mockedLoginResult)
            .when(abstractBackend, PowerMockito
                .method(AbstractOIDCBackend.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(abstractBackend, PowerMockito
            .method(AbstractOIDCBackend.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        Mockito.when(mockedSession.getSessionID()).thenReturn(SESSION_ID);
        try {
             abstractBackend.performLogin(mockedRequest, mockedResponse, true);
             PowerMockito.verifyPrivate(abstractBackend, Mockito.times(1)).invoke("writeSessionDataAsJson", mockedSession, mockedResponse);
        } catch (@SuppressWarnings("unused") IOException | JSONException e) {
            fail("An error was not expected but thrown.");
            return;
        } catch (@SuppressWarnings("unused") OXException e) {
            fail("An error was not expected but thrown.");            
            return;
        }
    }
    
    @Test
    public void performLogin_CookieALGetRedirectFailTest() throws Exception {
        // Spy on the AbstractOIDCBackend to mock private method
        AbstractOIDCBackend abstractBackend = PowerMockito.spy(new OIDCCoreBackend() {

            @Override
            public OIDCBackendConfig getBackendConfig() {
                return mockedBackendConfig;
            }
        });
        
        setUpForLogin();
        PowerMockito.doReturn(null).when(Services.class, "getService", SessionStorageService.class);
        PowerMockito.doReturn(mockedSessiondService).when(Services.class, "getService", SessiondService.class);

        Mockito.when(mockedBackendConfig.isAutologinEnabled()).thenReturn(true);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());
        
        PowerMockito.doReturn(mockedLoginConfig).when(abstractBackend, "getLoginConfiguration");
        PowerMockito.doReturn(mockedCookie).when(OIDCTools.class, "loadAutologinCookie", mockedRequest, mockedLoginConfig);
        PowerMockito.doNothing().when(abstractBackend, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));
        
        PowerMockito.doReturn(mockedSession).when(OIDCTools.class, "getSessionFromAutologinCookie", ArgumentMatchers.any(Cookie.class), ArgumentMatchers.any(HttpServletRequest.class));
        
        Mockito.when(mockedCookie.clone()).thenReturn(mockedCookie);
        
        PowerMockito.doNothing().when(abstractBackend, "updateSession", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(Map.class));
        
        Mockito.when(mockedSession.getContextId()).thenReturn(2);
        
        PowerMockito.doReturn(mockedLoginResult)
            .when(abstractBackend, PowerMockito
                .method(AbstractOIDCBackend.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(abstractBackend, PowerMockito
            .method(AbstractOIDCBackend.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        Mockito.when(mockedSession.getSessionID()).thenReturn(SESSION_ID);
        try {
             abstractBackend.performLogin(mockedRequest, mockedResponse, false);
             PowerMockito.verifyPrivate(abstractBackend, Mockito.times(1)).invoke("handleException", ArgumentMatchers.isNull(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.any(OXException.class), ArgumentMatchers.anyInt());
        } catch (@SuppressWarnings("unused") IOException | JSONException e) {
            fail("An error was not expected but thrown.");
            return;
        } catch (@SuppressWarnings("unused") OXException e) {
            fail("An error was not expected but thrown.");            
            return;
        }
    }
    
    @Test
    public void performLogin_CookieALGetRedirectTest() throws Exception {
        // Spy on the AbstractOIDCBackend to mock private method
        AbstractOIDCBackend abstractBackend = PowerMockito.spy(new OIDCCoreBackend() {

            @Override
            public OIDCBackendConfig getBackendConfig() {
                return mockedBackendConfig;
            }
        });
        
        setUpForLogin();
        PowerMockito.doReturn(null).when(Services.class, "getService", SessionStorageService.class);
        PowerMockito.doReturn(mockedSessiondService).when(Services.class, "getService", SessiondService.class);

        Mockito.when(mockedBackendConfig.isAutologinEnabled()).thenReturn(true);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());
        
        PowerMockito.doReturn(mockedLoginConfig).when(abstractBackend, "getLoginConfiguration");
        PowerMockito.doReturn(mockedCookie).when(OIDCTools.class, "loadAutologinCookie", mockedRequest, mockedLoginConfig);
        PowerMockito.doNothing().when(abstractBackend, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));
        
        PowerMockito.doReturn(mockedSession).when(OIDCTools.class, "getSessionFromAutologinCookie", ArgumentMatchers.any(Cookie.class), ArgumentMatchers.any(HttpServletRequest.class));
        
        Mockito.when(mockedCookie.clone()).thenReturn(mockedCookie);
        
        PowerMockito.doNothing().when(abstractBackend, "updateSession", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(Map.class));
        
        Mockito.when(mockedSession.getContextId()).thenReturn(CONTEXT_ID);
        Mockito.when(mockedReservation.getUserId()).thenReturn(USER_ID);
        Mockito.when(mockedSession.getUserId()).thenReturn(USER_ID);
        
        PowerMockito.doReturn(mockedLoginResult)
            .when(abstractBackend, PowerMockito
                .method(AbstractOIDCBackend.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(abstractBackend, PowerMockito
            .method(AbstractOIDCBackend.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        Mockito.when(mockedSession.getSessionID()).thenReturn(SESSION_ID);
        try {
             abstractBackend.performLogin(mockedRequest, mockedResponse, false);
             PowerMockito.verifyPrivate(OIDCTools.class, Mockito.times(1)).invoke("buildFrontendRedirectLocation", ArgumentMatchers.any(Session.class), ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
        } catch (@SuppressWarnings("unused") IOException | JSONException e) {
            fail("An error was not expected but thrown.");
            return;
        } catch (@SuppressWarnings("unused") OXException e) {
            fail("An error was not expected but thrown.");
            return;
        }
    }
    
    @Test
    public void performLogin_LoginAddALCookieToResponseTest() throws Exception {
        // Spy on the AbstractOIDCBackend to mock private method
        AbstractOIDCBackend abstractBackend = PowerMockito.spy(new OIDCCoreBackend() {

            @Override
            public OIDCBackendConfig getBackendConfig() {
                return mockedBackendConfig;
            }
        });
        
        setUpForLogin();
        Mockito.when(mockedBackendConfig.isAutologinEnabled()).thenReturn(true);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());
        PowerMockito.doReturn(mockedLoginConfig).when(abstractBackend, "getLoginConfiguration");
        PowerMockito.doReturn(null).when(OIDCTools.class, "loadAutologinCookie", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(LoginConfiguration.class));
        
        PowerMockito.doNothing().when(abstractBackend, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));
        
        PowerMockito.doReturn(mockedLoginResult)
            .when(abstractBackend, PowerMockito
                .method(AbstractOIDCBackend.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(abstractBackend, PowerMockito
            .method(AbstractOIDCBackend.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        try {
             abstractBackend.performLogin(mockedRequest, mockedResponse, true);
             Mockito.verify(mockedResponse, Mockito.times(1)).addCookie(ArgumentMatchers.any(Cookie.class));
             PowerMockito.verifyPrivate(abstractBackend, Mockito.times(1)).invoke("loginUser", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());
        } catch (@SuppressWarnings("unused") IOException | JSONException e) {
            fail("An error was not expected but thrown.");
            return;
        } catch (@SuppressWarnings("unused") OXException e) {
            fail("An error was not expected but thrown.");            
            return;
        } 
    }
    
    @Test
    public void performLogin_LoginNoALTest() throws Exception {
     // Spy on the AbstractOIDCBackend to mock private method
        AbstractOIDCBackend abstractBackend = PowerMockito.spy(new OIDCCoreBackend() {

            @Override
            public OIDCBackendConfig getBackendConfig() {
                return mockedBackendConfig;
            }
        });
        
        setUpForLogin();
        Mockito.when(mockedBackendConfig.isAutologinEnabled()).thenReturn(true);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());
        PowerMockito.doReturn(mockedLoginConfig).when(abstractBackend, "getLoginConfiguration");
        Mockito.when(mockedLoginConfig.getDefaultClient()).thenReturn("client");
        PowerMockito.doReturn(null).when(OIDCTools.class, "loadAutologinCookie", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(LoginConfiguration.class));
        
        PowerMockito.doNothing().when(abstractBackend, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));
        
        PowerMockito.doReturn(mockedLoginResult)
            .when(abstractBackend, PowerMockito
                .method(AbstractOIDCBackend.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.isNull());

        PowerMockito.doReturn(mockedSession)
        .when(abstractBackend, PowerMockito
            .method(AbstractOIDCBackend.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        Mockito.when(mockedBackendConfig.isAutologinEnabled()).thenReturn(false);
        
        try {
             abstractBackend.performLogin(mockedRequest, mockedResponse, false);
             Mockito.verify(mockedResponse, Mockito.times(0)).addCookie(ArgumentMatchers.any(Cookie.class));
             PowerMockito.verifyPrivate(abstractBackend, Mockito.times(1)).invoke("loginUser", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.isNull());
        } catch (@SuppressWarnings("unused") IOException | JSONException e) {
            fail("An error was not expected but thrown.");
            return;
        } catch (@SuppressWarnings("unused") OXException e) {
            fail("An error was not expected but thrown.");            
            return;
        } 
    }
    
}
