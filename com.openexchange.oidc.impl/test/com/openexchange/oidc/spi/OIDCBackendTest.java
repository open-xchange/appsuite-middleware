
package com.openexchange.oidc.spi;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.nimbusds.jose.JWSAlgorithm;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestContext;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.impl.OIDCLoginRequestHandler;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.Reservation;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AbstractOIDCBackend.class, OIDCTools.class, LoginPerformer.class, SessionUtility.class, LoginConfiguration.class, Services.class, OIDCLoginRequestHandler.OIDCHandler.class})
public class OIDCBackendTest {

    private static final String GET_SERVICE_METHOD = "getServiceSafe";
    private static final String HASH = "hash";
    private static final int USER_ID = 1;
    private static final String SESSION_ID = "SessionID";
    private static final int CONTEXT_ID = 1;
    private static final String ID_TOKEN = "IDToken";
    private static final String SESSION_TOKEN = "SessionToken";
    private static final String SCOPE_ONE = "scopeone";
    private static final String SCOPE_TWO = "scopetwo";

    @Mock
    OIDCBackendConfig mockedBackendConfig;

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

    private OIDCLoginRequestHandler testLoginHandler;

    int responseStatus;

    private LoginRequestContext loginRequestContext;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(SessiondService.class)).thenReturn(mockedSessiondService);
        PowerMockito.mockStatic(OIDCTools.class);
        PowerMockito.stub(PowerMockito.method(AbstractOIDCBackend.class, "getBackendConfig")).toReturn(mockedBackendConfig);
        this.testBackend = new OIDCCoreBackend() {

            @Override
            public OIDCBackendConfig getBackendConfig() {
                return mockedBackendConfig;
            }

        };
        this.testLoginHandler = new OIDCLoginRequestHandler(testBackend);
        Mockito.doAnswer(new Answer<Object>() {

            @SuppressWarnings("boxing")
            @Override
            public Object answer(InvocationOnMock a) throws Throwable {
                responseStatus = (int) a.getArgument(0);
                return null;
            }
        }).when(mockedResponse).sendError(Mockito.anyInt());
        Mockito.when(mockedSession.getParameter(Session.PARAM_LOCK)).thenReturn(new ReentrantLock());
        loginRequestContext = new LoginRequestContext();
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
        Mockito.when(mockedBackendConfig.getScope()).thenReturn(SCOPE_ONE + ' ' + SCOPE_TWO);
        List<String> scopeList = this.testBackend.getScope().toStringList();
        assertTrue("Not the number of scopes that were expected, should be two", scopeList.size() == 2);
        assertTrue("Scope is not what expected", scopeList.get(0).equals(SCOPE_ONE));
        assertTrue("Scope is not what expected", scopeList.get(1).equals(SCOPE_TWO));
    }

    @Test
    public void getScope_MultipleTest_Semicolon() {
        /*
         * Initially the config value was expected to have a semi-colon separated list
         * of scope values. As this is a very unusual separator, it has been replaced by a space (' ')
         * in newer releases, which matches the OAuth standard. Still we need to cope with semi-colons
         * for compatibility...
         */
        Mockito.when(mockedBackendConfig.getScope()).thenReturn(SCOPE_ONE + ";" + SCOPE_TWO);
        List<String> scopeList = this.testBackend.getScope().toStringList();
        assertTrue("Not the number of scopes that were expected, should be two", scopeList.size() == 2);
        assertTrue("Scope is not what expected", scopeList.get(0).equals(SCOPE_ONE));
        assertTrue("Scope is not what expected", scopeList.get(1).equals(SCOPE_TWO));
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
        Mockito.when(B(mockedBackendConfig.isAutologinEnabled())).thenReturn(B(false));

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
                testLoginHandler.handleRequest(mockedRequest, mockedResponse, loginRequestContext);
            } catch (@SuppressWarnings("unused") IOException e) {
                fail("Wrong exception thrown.");
                return;
            }
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseStatus);
    }

    @Test
    public void performLogin_NoReservationFailTest() throws Exception {
        Mockito.when(mockedRequest.getParameter(OIDCTools.SESSION_TOKEN)).thenReturn(SESSION_TOKEN);
        PowerMockito.doReturn(mockedSessionReservationService).when(Services.class, GET_SERVICE_METHOD, SessionReservationService.class);
        Mockito.when(mockedSessionReservationService.removeReservation(SESSION_TOKEN)).thenReturn(null);
        try {
            testLoginHandler.handleRequest(mockedRequest, mockedResponse, loginRequestContext);
        } catch (@SuppressWarnings("unused") IOException e) {
            fail("Wrong exception thrown.");
            return;
        }
        assertEquals(HttpServletResponse.SC_FORBIDDEN, responseStatus);
    }

    @Test
    public void performLogin_NoIDTokenFailTest() throws Exception {
        Mockito.when(mockedRequest.getParameter(OIDCTools.SESSION_TOKEN)).thenReturn(SESSION_TOKEN);
        PowerMockito.doReturn(mockedSessionReservationService).when(Services.class, GET_SERVICE_METHOD, SessionReservationService.class);
        Mockito.when(mockedSessionReservationService.removeReservation(SESSION_TOKEN)).thenReturn(mockedReservation);
        Mockito.when(mockedReservation.getState()).thenReturn(new HashMap<String,String>());
        try {
            testLoginHandler.handleRequest(mockedRequest, mockedResponse, loginRequestContext);
        } catch (@SuppressWarnings("unused") IOException e) {
            fail("Wrong exception thrown.");
            return;
        }
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseStatus);
    }

    @Test
    public void performLogin_NoContextServiceFailTest() throws Exception {
        Mockito.when(mockedRequest.getParameter(OIDCTools.SESSION_TOKEN)).thenReturn(SESSION_TOKEN);
        PowerMockito.doReturn(mockedSessionReservationService).when(Services.class, GET_SERVICE_METHOD, SessionReservationService.class);
        Mockito.when(mockedSessionReservationService.removeReservation(SESSION_TOKEN)).thenReturn(mockedReservation);
        HashMap<String, String> idTokenMap = new HashMap<String,String>();
        idTokenMap.put(OIDCTools.IDTOKEN, ID_TOKEN);
        Mockito.when(mockedReservation.getState()).thenReturn(idTokenMap);
        PowerMockito.doReturn(mockedContextService).when(Services.class, GET_SERVICE_METHOD, ContextService.class);
        Mockito.when(I(mockedReservation.getContextId())).thenReturn(I(CONTEXT_ID));
        Mockito.when(mockedContextService.getContext(CONTEXT_ID)).thenReturn(mockedContext);
        Mockito.when(B(mockedContext.isEnabled())).thenReturn(B(false));

        try {
            testLoginHandler.handleRequest(mockedRequest, mockedResponse, loginRequestContext);
        } catch (@SuppressWarnings("unused") IOException e) {
            fail("Wrong exception thrown.");
            return;
        }
        assertEquals(HttpServletResponse.SC_FORBIDDEN, responseStatus);
    }

    @Test
    public void performLogin_NoUserServiceFailTest() throws Exception {
        Mockito.when(mockedRequest.getParameter(OIDCTools.SESSION_TOKEN)).thenReturn(SESSION_TOKEN);
        PowerMockito.doReturn(mockedSessionReservationService).when(Services.class, GET_SERVICE_METHOD, SessionReservationService.class);
        Mockito.when(mockedSessionReservationService.removeReservation(SESSION_TOKEN)).thenReturn(mockedReservation);
        HashMap<String, String> idTokenMap = new HashMap<String,String>();
        idTokenMap.put(OIDCTools.IDTOKEN, ID_TOKEN);
        Mockito.when(mockedReservation.getState()).thenReturn(idTokenMap);
        PowerMockito.doReturn(mockedContextService).when(Services.class, GET_SERVICE_METHOD, ContextService.class);
        Mockito.when(I(mockedReservation.getContextId())).thenReturn(I(CONTEXT_ID));
        Mockito.when(mockedContextService.getContext(CONTEXT_ID)).thenReturn(mockedContext);
        Mockito.when(B(mockedContext.isEnabled())).thenReturn(B(true));
        PowerMockito.doReturn(mockedUserService).when(Services.class, GET_SERVICE_METHOD, UserService.class);
        Mockito.when(I(mockedReservation.getUserId())).thenReturn(I(2));
        Mockito.when(mockedUserService.getUser(2, mockedContext)).thenReturn(mockedUser);
        Mockito.when(B(mockedUser.isMailEnabled())).thenReturn(B(false));

        try {
            testLoginHandler.handleRequest(mockedRequest, mockedResponse, loginRequestContext);
        } catch (@SuppressWarnings("unused") IOException e) {
            fail("Wrong exception thrown.");
            return;
        }
        assertEquals(HttpServletResponse.SC_FORBIDDEN, responseStatus);
    }

    /*
     * Shorter form for Autologin is AL, Only autologin from here on
     */
    private void setUpForLogin() throws Exception {
        setUpForLogin(true);
    }

    private void setUpForLogin(boolean respondWithJson) throws Exception {
        Mockito.when(mockedRequest.getParameter(OIDCTools.SESSION_TOKEN)).thenReturn(SESSION_TOKEN);
        if(respondWithJson) {
            Mockito.when(mockedRequest.getHeader("Accept")).thenReturn("application/json");
        }
        else {
            Mockito.when(mockedRequest.getHeader("Accept")).thenReturn(null);
        }
        PowerMockito.doReturn(mockedSessionReservationService).when(Services.class, GET_SERVICE_METHOD, SessionReservationService.class);
        Mockito.when(mockedSessionReservationService.removeReservation(SESSION_TOKEN)).thenReturn(mockedReservation);
        HashMap<String, String> idTokenMap = new HashMap<String,String>();
        idTokenMap.put(OIDCTools.IDTOKEN, ID_TOKEN);
        Mockito.when(mockedReservation.getState()).thenReturn(idTokenMap);
        PowerMockito.doReturn(mockedContextService).when(Services.class, GET_SERVICE_METHOD, ContextService.class);
        Mockito.when(I(mockedReservation.getContextId())).thenReturn(I(CONTEXT_ID));
        Mockito.when(mockedContextService.getContext(CONTEXT_ID)).thenReturn(mockedContext);
        Mockito.when(B(mockedContext.isEnabled())).thenReturn(B(true));
        PowerMockito.doReturn(mockedUserService).when(Services.class, GET_SERVICE_METHOD, UserService.class);
        Mockito.when(I(mockedReservation.getUserId())).thenReturn(I(USER_ID));
        Mockito.when(mockedUserService.getUser(USER_ID, mockedContext)).thenReturn(mockedUser);
        Mockito.when(B(mockedUser.isMailEnabled())).thenReturn(Boolean.TRUE);
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

        setUpForLogin(true);
        OIDCLoginRequestHandler.OIDCHandler oidcHandler = PowerMockito.spy(new OIDCLoginRequestHandler.OIDCHandler(abstractBackend));
        testLoginHandler.setOIDCHandler(oidcHandler);
        Mockito.when(B(mockedBackendConfig.isAutologinEnabled())).thenReturn(Boolean.TRUE);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());
        PowerMockito.doReturn(null).when(OIDCTools.class, "loadAutologinCookie", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(LoginConfiguration.class));
        PowerMockito.doReturn(HASH).when(OIDCTools.class, "calculateHash", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(LoginConfiguration.class));

        PowerMockito.doNothing().when(oidcHandler, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));

        PowerMockito.doReturn(mockedLoginResult)
            .when(oidcHandler, PowerMockito
                .method(OIDCLoginRequestHandler.OIDCHandler.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(oidcHandler, PowerMockito
            .method(OIDCLoginRequestHandler.OIDCHandler.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        try {
             testLoginHandler.handleRequest(mockedRequest, mockedResponse, loginRequestContext);
             PowerMockito.verifyPrivate(oidcHandler, Mockito.times(1)).invoke("loginUser", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());
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
        OIDCLoginRequestHandler.OIDCHandler oidcHandler = PowerMockito.spy(new OIDCLoginRequestHandler.OIDCHandler(abstractBackend));
        testLoginHandler.setOIDCHandler(oidcHandler);
        Mockito.when(B(mockedBackendConfig.isAutologinEnabled())).thenReturn(Boolean.TRUE);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());
        PowerMockito.doReturn(mockedCookie).when(OIDCTools.class, "loadAutologinCookie", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(LoginConfiguration.class));
        PowerMockito.doNothing().when(oidcHandler, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));
        PowerMockito.doReturn(null).when(OIDCTools.class, "getSessionFromAutologinCookie", ArgumentMatchers.any(Cookie.class), ArgumentMatchers.any(HttpServletRequest.class));
        PowerMockito.doReturn(HASH).when(OIDCTools.class, "calculateHash", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(LoginConfiguration.class));

        Mockito.when(mockedCookie.clone()).thenReturn(mockedCookie);

        PowerMockito.doReturn(mockedLoginResult)
            .when(oidcHandler, PowerMockito
                .method(OIDCLoginRequestHandler.OIDCHandler.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(oidcHandler, PowerMockito
            .method(OIDCLoginRequestHandler.OIDCHandler.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        try {
             //abstractBackend.performLogin(mockedRequest, mockedResponse, true);
             testLoginHandler.handleRequest(mockedRequest, mockedResponse, loginRequestContext);
             PowerMockito.verifyPrivate(oidcHandler, Mockito.times(1)).invoke("loginUser", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());
        } catch (@SuppressWarnings("unused") IOException e ) {
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
        OIDCLoginRequestHandler.OIDCHandler oidcHandler = PowerMockito.spy(new OIDCLoginRequestHandler.OIDCHandler(abstractBackend));
        testLoginHandler.setOIDCHandler(oidcHandler);
        PowerMockito.doReturn(mockedSessionStorage).when(Services.class, "getService", SessionStorageService.class);

        Mockito.when(B(mockedBackendConfig.isAutologinEnabled())).thenReturn(Boolean.TRUE);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());



        PowerMockito.doReturn(mockedLoginConfig).when(abstractBackend, "getLoginConfiguration");
        PowerMockito.doReturn(mockedCookie).when(OIDCTools.class, "loadAutologinCookie", mockedRequest, mockedLoginConfig);
        PowerMockito.doNothing().when(oidcHandler, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));

        PowerMockito.doReturn(mockedSession).when(OIDCTools.class, "getSessionFromAutologinCookie", mockedCookie, mockedRequest);

        Mockito.when(mockedCookie.clone()).thenReturn(mockedCookie);

        PowerMockito.doNothing().when(oidcHandler, "writeSessionDataAsJson", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletResponse.class));

        PowerMockito.doReturn(mockedLoginResult)
            .when(oidcHandler, PowerMockito
                .method(OIDCLoginRequestHandler.OIDCHandler.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(oidcHandler, PowerMockito
            .method(OIDCLoginRequestHandler.OIDCHandler.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        try {
             testLoginHandler.handleRequest(mockedRequest, mockedResponse, loginRequestContext);
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
        OIDCLoginRequestHandler.OIDCHandler oidcHandler = PowerMockito.spy(new OIDCLoginRequestHandler.OIDCHandler(abstractBackend));
        testLoginHandler.setOIDCHandler(oidcHandler);
        PowerMockito.doReturn(null).when(Services.class, "getService", SessionStorageService.class);
        PowerMockito.doReturn(mockedSessiondService).when(Services.class, "getService", SessiondService.class);

        Mockito.when(B(mockedBackendConfig.isAutologinEnabled())).thenReturn(Boolean.TRUE);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());
        PowerMockito.doReturn(mockedLoginConfig).when(abstractBackend, "getLoginConfiguration");
        PowerMockito.doReturn(mockedCookie).when(OIDCTools.class, "loadAutologinCookie", mockedRequest, mockedLoginConfig);
        PowerMockito.doNothing().when(oidcHandler, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));

        PowerMockito.doReturn(mockedSession).when(OIDCTools.class, "getSessionFromAutologinCookie", ArgumentMatchers.any(Cookie.class), ArgumentMatchers.any(HttpServletRequest.class));

        Mockito.when(mockedCookie.clone()).thenReturn(mockedCookie);

        PowerMockito.doNothing().when(oidcHandler, "writeSessionDataAsJson", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletResponse.class));

        PowerMockito.doReturn(mockedLoginResult)
            .when(oidcHandler, PowerMockito
                .method(OIDCLoginRequestHandler.OIDCHandler.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(oidcHandler, PowerMockito
            .method(OIDCLoginRequestHandler.OIDCHandler.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        Mockito.when(mockedSession.getSessionID()).thenReturn(SESSION_ID);
        try {
             testLoginHandler.handleRequest(mockedRequest, mockedResponse, loginRequestContext);
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
        OIDCLoginRequestHandler.OIDCHandler oidcHandler = PowerMockito.spy(new OIDCLoginRequestHandler.OIDCHandler(abstractBackend));
        testLoginHandler.setOIDCHandler(oidcHandler);
        PowerMockito.doReturn(null).when(Services.class, "getService", SessionStorageService.class);
        PowerMockito.doReturn(mockedSessiondService).when(Services.class, "getService", SessiondService.class);

        Mockito.when(B(mockedBackendConfig.isAutologinEnabled())).thenReturn(Boolean.TRUE);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());

        PowerMockito.doReturn(mockedLoginConfig).when(abstractBackend, "getLoginConfiguration");
        PowerMockito.doReturn(mockedCookie).when(OIDCTools.class, "loadAutologinCookie", mockedRequest, mockedLoginConfig);

        PowerMockito.doNothing().when(oidcHandler, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));

        PowerMockito.doReturn(mockedSession).when(OIDCTools.class, "getSessionFromAutologinCookie", ArgumentMatchers.any(Cookie.class), ArgumentMatchers.any(HttpServletRequest.class));

        Mockito.when(mockedCookie.clone()).thenReturn(mockedCookie);

        PowerMockito.doNothing().when(abstractBackend, "updateSession", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(Map.class));

        PowerMockito.doNothing().when(oidcHandler, "writeSessionDataAsJson", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletResponse.class));

        PowerMockito.doReturn(mockedLoginResult)
            .when(oidcHandler, PowerMockito
                .method(OIDCLoginRequestHandler.OIDCHandler.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(oidcHandler, PowerMockito
            .method(OIDCLoginRequestHandler.OIDCHandler.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        Mockito.when(mockedSession.getSessionID()).thenReturn(SESSION_ID);
        try {
             testLoginHandler.handleRequest(mockedRequest, mockedResponse, loginRequestContext);
             PowerMockito.verifyPrivate(oidcHandler, Mockito.times(1)).invoke("writeSessionDataAsJson", mockedSession, mockedResponse);
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

        setUpForLogin(false);
        OIDCLoginRequestHandler.OIDCHandler oidcHandler = PowerMockito.spy(new OIDCLoginRequestHandler.OIDCHandler(abstractBackend));
        testLoginHandler.setOIDCHandler(oidcHandler);
        PowerMockito.doReturn(null).when(Services.class, GET_SERVICE_METHOD, SessionStorageService.class);

        Mockito.when(B(mockedBackendConfig.isAutologinEnabled())).thenReturn(Boolean.TRUE);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());

        PowerMockito.doReturn(mockedLoginConfig).when(abstractBackend, "getLoginConfiguration");
        PowerMockito.doReturn(mockedCookie).when(OIDCTools.class, "loadAutologinCookie", mockedRequest, mockedLoginConfig);
        PowerMockito.doNothing().when(oidcHandler, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));

        PowerMockito.doReturn(mockedSession).when(OIDCTools.class, "getSessionFromAutologinCookie", ArgumentMatchers.any(Cookie.class), ArgumentMatchers.any(HttpServletRequest.class));

        Mockito.when(mockedCookie.clone()).thenReturn(mockedCookie);

        PowerMockito.doNothing().when(abstractBackend, "updateSession", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(Map.class));

        Mockito.when(I(mockedSession.getContextId())).thenReturn(I(2));

        PowerMockito.doReturn(mockedLoginResult)
            .when(oidcHandler, PowerMockito
                .method(OIDCLoginRequestHandler.OIDCHandler.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(oidcHandler, PowerMockito
            .method(OIDCLoginRequestHandler.OIDCHandler.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        Mockito.when(mockedSession.getSessionID()).thenReturn(SESSION_ID);
        try {
             testLoginHandler.handleRequest(mockedRequest, mockedResponse, loginRequestContext);
             PowerMockito.verifyPrivate(oidcHandler, Mockito.times(1)).invoke("handleException", ArgumentMatchers.isNull(), B(ArgumentMatchers.anyBoolean()), ArgumentMatchers.any(OXException.class), I(ArgumentMatchers.anyInt()), ArgumentMatchers.any(LoginRequestContext.class));
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

        setUpForLogin(false);
        OIDCLoginRequestHandler.OIDCHandler oidcHandler = PowerMockito.spy(new OIDCLoginRequestHandler.OIDCHandler(abstractBackend));
        testLoginHandler.setOIDCHandler(oidcHandler);
        PowerMockito.doReturn(null).when(Services.class, GET_SERVICE_METHOD, SessionStorageService.class);
        PowerMockito.doReturn(mockedSessiondService).when(Services.class, GET_SERVICE_METHOD, SessiondService.class);

        Mockito.when(B(mockedBackendConfig.isAutologinEnabled())).thenReturn(Boolean.TRUE);
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());

        PowerMockito.doReturn(mockedLoginConfig).when(abstractBackend, "getLoginConfiguration");
        PowerMockito.doReturn(mockedCookie).when(OIDCTools.class, "loadAutologinCookie", mockedRequest, mockedLoginConfig);
        PowerMockito.doNothing().when(oidcHandler, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));

        PowerMockito.doReturn(mockedSession).when(OIDCTools.class, "getSessionFromAutologinCookie", ArgumentMatchers.any(Cookie.class), ArgumentMatchers.any(HttpServletRequest.class));

        Mockito.when(mockedCookie.clone()).thenReturn(mockedCookie);

        PowerMockito.doNothing().when(abstractBackend, "updateSession", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(Map.class));

        Mockito.when(I(mockedSession.getContextId())).thenReturn(I(CONTEXT_ID));
        Mockito.when(I(mockedReservation.getUserId())).thenReturn(I(USER_ID));
        Mockito.when(I(mockedSession.getUserId())).thenReturn(I(USER_ID));

        PowerMockito.doReturn(mockedLoginResult)
            .when(oidcHandler, PowerMockito
                .method(OIDCLoginRequestHandler.OIDCHandler.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(oidcHandler, PowerMockito
            .method(OIDCLoginRequestHandler.OIDCHandler.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        Mockito.when(mockedSession.getSessionID()).thenReturn(SESSION_ID);
        try {
             testLoginHandler.handleRequest(mockedRequest, mockedResponse, loginRequestContext);
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
        OIDCLoginRequestHandler.OIDCHandler oidcHandler = PowerMockito.spy(new OIDCLoginRequestHandler.OIDCHandler(abstractBackend));
        testLoginHandler.setOIDCHandler(oidcHandler);
        Mockito.when(B(mockedBackendConfig.isAutologinEnabled())).thenReturn(B(true));
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());
        PowerMockito.doReturn(mockedLoginConfig).when(abstractBackend, "getLoginConfiguration");
        PowerMockito.doReturn(null).when(OIDCTools.class, "loadAutologinCookie", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(LoginConfiguration.class));

        PowerMockito.doNothing().when(oidcHandler, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));

        PowerMockito.doReturn(mockedLoginResult)
            .when(oidcHandler, PowerMockito
                .method(OIDCLoginRequestHandler.OIDCHandler.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());

        PowerMockito.doReturn(mockedSession)
        .when(oidcHandler, PowerMockito
            .method(OIDCLoginRequestHandler.OIDCHandler.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        try {
             testLoginHandler.handleRequest(mockedRequest, mockedResponse, loginRequestContext);
             Mockito.verify(mockedResponse, Mockito.times(1)).addCookie(ArgumentMatchers.any(Cookie.class));
             PowerMockito.verifyPrivate(oidcHandler, Mockito.times(1)).invoke("loginUser", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.anyString());
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

        setUpForLogin(false);
        OIDCLoginRequestHandler.OIDCHandler oidcHandler = PowerMockito.spy(new OIDCLoginRequestHandler.OIDCHandler(abstractBackend));
        testLoginHandler.setOIDCHandler(oidcHandler);
        Mockito.when(B(mockedBackendConfig.isAutologinEnabled())).thenReturn(B(true));
        Mockito.when(mockedBackendConfig.autologinCookieMode()).thenReturn(OIDCBackendConfig.AutologinMode.SSO_REDIRECT.getValue());
        PowerMockito.doReturn(mockedLoginConfig).when(abstractBackend, "getLoginConfiguration");
        Mockito.when(mockedLoginConfig.getDefaultClient()).thenReturn("client");
        PowerMockito.doReturn(null).when(OIDCTools.class, "loadAutologinCookie", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(LoginConfiguration.class));

        PowerMockito.doNothing().when(oidcHandler, "sendRedirect", ArgumentMatchers.any(Session.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class), ArgumentMatchers.any(Boolean.class));

        PowerMockito.doReturn(mockedLoginResult)
            .when(oidcHandler, PowerMockito
                .method(OIDCLoginRequestHandler.OIDCHandler.class, "loginUser", HttpServletRequest.class, Context.class, User.class, Map.class, String.class))
                    .withArguments(ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.isNull());

        PowerMockito.doReturn(mockedSession)
        .when(oidcHandler, PowerMockito
            .method(OIDCLoginRequestHandler.OIDCHandler.class, "performSessionAdditions", LoginResult.class, HttpServletRequest.class, HttpServletResponse.class, String.class))
                .withArguments(ArgumentMatchers.any(LoginResult.class), ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(HttpServletResponse.class),  ArgumentMatchers.anyString());

        Mockito.when(B(mockedBackendConfig.isAutologinEnabled())).thenReturn(B(false));

        try {
             testLoginHandler.handleRequest(mockedRequest, mockedResponse, loginRequestContext);
             Mockito.verify(mockedResponse, Mockito.times(0)).addCookie(ArgumentMatchers.any(Cookie.class));
             PowerMockito.verifyPrivate(oidcHandler, Mockito.times(1)).invoke("loginUser", ArgumentMatchers.any(HttpServletRequest.class), ArgumentMatchers.any(Context.class), ArgumentMatchers.any(User.class), ArgumentMatchers.any(Map.class), ArgumentMatchers.isNull());
        } catch (@SuppressWarnings("unused") IOException | JSONException e) {
            fail("An error was not expected but thrown.");
            return;
        } catch (@SuppressWarnings("unused") OXException e) {
            fail("An error was not expected but thrown.");
            return;
        }
    }

}
