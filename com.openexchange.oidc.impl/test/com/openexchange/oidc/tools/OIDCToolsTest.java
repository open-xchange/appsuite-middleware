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

package com.openexchange.oidc.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.tools.servlet.http.Cookies;

/**
 * {@link OIDCToolsTest} Testclass for {@link OIDCTools}.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ SessionUtility.class, Cookies.class, LoginConfiguration.class, LoginServlet.class, OIDCTools.class })
public class OIDCToolsTest {

    private static final String HTTPS_REDIRECT_URI = "https://redirectURI";
    private static final String REDIRECT_JSON ="{\"redirect\":\"https://redirectURI\"}";
    private static String HOSTNAME = "Hostname";
    private static String REQUEST_URI = "https://localhost";
    public static String TESTFILES_PATH = "./test/testfiles";
    private static String UI_WEBPATH = "/ui/web/path";

    @Mock
    private HostnameService mockedHostnameService;

    @Mock
    private HttpServletRequest mockedRequest;

    @Mock
    private Session mockedSession;

    @Mock
    private Cookie mockedCookie;

    @Mock
    private OIDCBackendConfig mockedBackendConfig;

    @Mock
    private OIDCBackend mockedBackend;

    @Mock
    LoginConfiguration mockedLoginConfig;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(mockedHostnameService.getHostname(-1, -1)).thenReturn(HOSTNAME);
        Mockito.when(mockedRequest.getRequestURI()).thenReturn(REQUEST_URI);
    }

    @After
    public void cleanUp() {
        File files = new File(TESTFILES_PATH);
        if (files.isDirectory() && files.exists()) {
            for (File file : files.listFiles()) {
                file.delete();
            }
        }
    }

    @Test
    public void getDomainName_FromRequestOnServiceErrorTest() {
        Mockito.when(mockedRequest.getServerName()).thenReturn(HOSTNAME);
        Mockito.when(mockedHostnameService.getHostname(-1, -1)).thenReturn(null);
        String result = OIDCTools.getDomainName(mockedRequest, null);
        assertEquals(HOSTNAME, result);
    }

    @Test
    public void getDomainName_FromRequestTest() {
        Mockito.when(mockedRequest.getServerName()).thenReturn(HOSTNAME);
        String result = OIDCTools.getDomainName(mockedRequest, null);
        assertEquals(HOSTNAME, result);
    }

    @Test
    public void getDomainName_FromServiceTest() {
        String result = OIDCTools.getDomainName(mockedRequest, mockedHostnameService);
        assertEquals(HOSTNAME, result);
    }

    @Test
    public void validateSession_ValidTest() throws Exception {
        Mockito.when(mockedSession.getSessionID()).thenReturn("SessionID");
        PowerMockito.mockStatic(SessionUtility.class);
        PowerMockito.doNothing().when(SessionUtility.class, "checkIP", Matchers.any(Session.class), Matchers.any(String.class));
        PowerMockito.when(mockedSession.getHash()).thenReturn("");
        PowerMockito.mockStatic(Cookies.class);
        Map<String, Cookie> cookieMap = new HashMap<>();
        cookieMap.put(LoginServlet.SECRET_PREFIX, mockedCookie);
        PowerMockito.when(mockedCookie.getValue()).thenReturn("Secret");
        PowerMockito.when(mockedSession.getSecret()).thenReturn("Secret");
        PowerMockito.when(Cookies.cookieMapFor(mockedRequest)).thenReturn(cookieMap);
        try {
            OIDCTools.validateSession(mockedSession, mockedRequest);
        } catch (@SuppressWarnings("unused") OXException e) {
            fail();
        }
        assertTrue(true);
    }

    @Test
    public void validateSession_SecretCookieNullTest() throws Exception {
        Mockito.when(mockedSession.getSessionID()).thenReturn("SessionID");
        PowerMockito.mockStatic(SessionUtility.class);
        PowerMockito.doNothing().when(SessionUtility.class, "checkIP", Matchers.any(Session.class), Matchers.any(String.class));
        try {
            OIDCTools.validateSession(mockedSession, mockedRequest);
        } catch (OXException e) {
            assertEquals(SessionExceptionCodes.SESSION_EXPIRED, e.getExceptionCode());
        }
    }

    @Test
    public void validateSession_WrongSecretTest() throws Exception {
        Mockito.when(mockedSession.getSessionID()).thenReturn("SessionID");
        PowerMockito.mockStatic(SessionUtility.class);
        PowerMockito.doNothing().when(SessionUtility.class, "checkIP", Matchers.any(Session.class), Matchers.any(String.class));
        PowerMockito.when(mockedSession.getHash()).thenReturn("");
        PowerMockito.mockStatic(Cookies.class);
        Map<String, Cookie> cookieMap = new HashMap<>();
        cookieMap.put(LoginServlet.SECRET_PREFIX, mockedCookie);
        PowerMockito.when(mockedCookie.getValue()).thenReturn("Cookie Secret");
        PowerMockito.when(mockedSession.getSecret()).thenReturn("Session Secret");
        PowerMockito.when(Cookies.cookieMapFor(mockedRequest)).thenReturn(cookieMap);
        try {
            OIDCTools.validateSession(mockedSession, mockedRequest);
        } catch (OXException e) {
            assertEquals(SessionExceptionCodes.SESSION_EXPIRED, e.getExceptionCode());
        }
    }

    @Test
    public void buildRedirectResponse_RespondWithRedirectTest() {
        MockedHttpResponse mockedHttpResponse = new MockedHttpResponse();
        try {
            OIDCTools.buildRedirectResponse(mockedHttpResponse, HTTPS_REDIRECT_URI, "true");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to build a correct redirect response");
        }
        assertEquals(HTTPS_REDIRECT_URI, mockedHttpResponse.responseURI);
    }

    @Test
    public void buildRedirectResponse_RespondWithJSONTest() throws IOException {
        MockedHttpResponse mockedHttpResponse = new MockedHttpResponse();
        try {
            OIDCTools.buildRedirectResponse(mockedHttpResponse, HTTPS_REDIRECT_URI, "false");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to build a correct redirect response");
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(TESTFILES_PATH + "/" + MockedHttpResponse.WRITER_FILE));) {
            assertEquals("Stored JSON String is not like expected", REDIRECT_JSON, reader.readLine());
        }
    }

    @Test
    public void addParameterToSession_ParameterInGivenMapTest() {
        Session session = new MockedSession();
        String newKey = "new";
        String oldKey = "old";
        Map<String, String> map = new HashMap<>();
        map.put(oldKey, "value");
        OIDCTools.addParameterToSession(session, map, "old", "new");
        assertTrue("Failed to store parameter from map into session parameter list.", ((String) session.getParameter(newKey)).equals(map.get(oldKey)));
    }

    @Test
    public void addParameterToSession_ParameterNOTInGivenMapTest() {
        Session session = new MockedSession();
        String newKey = "new";
        String oldKey = "old";
        Map<String, String> map = new HashMap<>();
        map.put(oldKey + "1", "value");
        OIDCTools.addParameterToSession(session, map, "old", "new");
        assertTrue("There was a parameter found, where none should be.", ((String) session.getParameter(newKey) == null));

    }

    @Test
    public void getUIWebPath_FromBackendTest() {
        Mockito.when(mockedBackendConfig.getUIWebpath()).thenReturn(UI_WEBPATH);
        String result = OIDCTools.getUIWebPath(null, mockedBackendConfig);
        assertTrue("Failed to laod web ui path from backend configuration", result.equals(UI_WEBPATH));
    }

    @Test
    public void getUIWebPath_FromLoginConfigTest() {
        Mockito.when(mockedBackendConfig.getUIWebpath()).thenReturn("");
        Mockito.when(mockedLoginConfig.getUiWebPath()).thenReturn(UI_WEBPATH);
        String result = OIDCTools.getUIWebPath(mockedLoginConfig, mockedBackendConfig);
        assertTrue("Failed to laod web ui path from backend configuration", result.equals(UI_WEBPATH));
    }

    @Test
    public void validatePath_ValidTest() {
        try {
            OIDCTools.validatePath("abcd");
        } catch (@SuppressWarnings("unused") OXException e) {
            fail("Legit path was not accepted.");
        }
    }

    @Test
    public void validatePath_InvalidTest() {
        try {
            OIDCTools.validatePath("abcd!EFGH");
        } catch (OXException e) {
            assertEquals(OIDCExceptionCode.INVALID_BACKEND_PATH, e.getExceptionCode());
        }
    }

    @Test
    public void getPrefix_BackendConfigTest() {
        PowerMockito.stub(PowerMockito.method(OIDCTools.class, "getRedirectPathPrefix")).toReturn("dispatcher/");
        Mockito.when(mockedBackend.getPath()).thenReturn("backend");
        String result = OIDCTools.getPrefix(mockedBackend);
        assertTrue("Wrong prefix build, dispatcher/oidc/backend/ expected", result.equals("dispatcher/oidc/backend/"));
    }

    @Test
    public void getPrefix_NoBackendConfigTest() throws Exception {
        PowerMockito.stub(PowerMockito.method(OIDCTools.class, "getRedirectPathPrefix")).toReturn("dispatcher/");
        Mockito.when(mockedBackend.getPath()).thenReturn("");
        String result = OIDCTools.getPrefix(mockedBackend);
        assertTrue("Wrong prefix build, dispatcher/oidc/ expected", result.equals("dispatcher/oidc/"));
    }

    @Test
    public void getUiClient_FromRequestTest() {
        PowerMockito.mockStatic(LoginServlet.class);
        Mockito.when(LoginServlet.getLoginConfiguration()).thenReturn(mockedLoginConfig);
        String testClient = "TestClient";
        Mockito.when(mockedLoginConfig.getDefaultClient()).thenReturn("");
        Mockito.when(mockedRequest.getParameter("client")).thenReturn(testClient);
        String result = OIDCTools.getUiClient(mockedRequest);
        assertTrue("Wrong UI client loaded", testClient.equals(result));
    }

    @Test
    public void getUiClient_FromConfigTest() {
        PowerMockito.mockStatic(LoginServlet.class);
        Mockito.when(LoginServlet.getLoginConfiguration()).thenReturn(mockedLoginConfig);
        String testClient = "TestClient";
        Mockito.when(mockedLoginConfig.getDefaultClient()).thenReturn(testClient);
        Mockito.when(mockedRequest.getParameter("client")).thenReturn(null);
        String result = OIDCTools.getUiClient(mockedRequest);
        assertTrue("Wrong UI client loaded", testClient.equals(result));
    }
}
