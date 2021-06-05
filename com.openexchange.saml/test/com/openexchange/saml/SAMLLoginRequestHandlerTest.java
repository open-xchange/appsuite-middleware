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

package com.openexchange.saml;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.saml.utils.SAMLTestUtils.buildAddSessionParameter;
import static com.openexchange.saml.utils.SAMLTestUtils.prepareHTTPRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.sim.SimHttpServletRequest;
import javax.servlet.http.sim.SimHttpServletResponse;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestContext;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.saml.impl.LoginConfigurationLookup;
import com.openexchange.saml.impl.SAMLLoginRequestHandler;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.SAMLBackend;
import com.openexchange.saml.tools.SAMLLoginTools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.SimpleServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.session.reservation.SimSessionReservationService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SimSessiondService;
import com.openexchange.user.SimUserService;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link SAMLLoginRequestHandlerTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class SAMLLoginRequestHandlerTest {

    private TestConfig config;
    private TestCredentials testCredentials;
    private CredentialProvider credentialProvider;
    private SimSessionReservationService sessionReservationService;
    private SimSessiondService sessiondService;
    private SimpleServiceLookup services;
    private SimUserService userService;
    private TestLoginRequestHandler handler;
    private TestLoginConfigurationLookup loginConfigurationLookup;
    private static ContextService contextService;
    private Context context;
    private User user;

    @BeforeClass
    public static void beforeClass() {
        // static dependency of c.o.ajax.SessionUtility
        SimConfigurationService simConfigurationService = new SimConfigurationService();
        ServerServiceRegistry.getInstance().addService(ConfigurationService.class, simConfigurationService);

        contextService = Mockito.mock(ContextService.class);
        ServerServiceRegistry.getInstance().addService(ContextService.class, contextService);
    }

    @Before
    public void setUp() throws Exception {
        testCredentials = new TestCredentials();
        credentialProvider = testCredentials.getSPCredentialProvider();

        services = new SimpleServiceLookup();
        sessionReservationService = new SimSessionReservationService();
        services.add(SessionReservationService.class, sessionReservationService);
        services.add(DispatcherPrefixService.class, new DispatcherPrefixService() {

            @Override
            public String getPrefix() {
                return "/appsuite/api/";
            }
        });

        config = new TestConfig();
        TestSAMLBackend samlBackend = new TestSAMLBackend(credentialProvider, config);
        services.add(SAMLBackend.class, samlBackend);

        sessiondService = new SimSessiondService();
        services.add(SessiondService.class, sessiondService);

        userService = new SimUserService();
        services.add(UserService.class, userService);
        user = Mockito.mock(User.class);
        Mockito.when(I(user.getId())).thenReturn(I(1));
        Mockito.when(user.getLoginInfo()).thenReturn("user");
        Mockito.when(B(user.isMailEnabled())).thenReturn(B(true));
        userService.addUser(user, 1);

        services.add(ContextService.class, contextService);
        context = Mockito.mock(Context.class);
        Mockito.when(I(context.getContextId())).thenReturn(I(1));
        Mockito.when(context.getLoginInfo()).thenReturn(new String[] {"example.com"});
        Mockito.when(B(context.isEnabled())).thenReturn(B(true));
        Mockito.when(contextService.getContext(1)).thenReturn(context);

        loginConfigurationLookup = new TestLoginConfigurationLookup();
        handler = new TestLoginRequestHandler(samlBackend, loginConfigurationLookup, services);
    }

    @Test
    public void deepLinkWithNewSession() throws Exception {
        Session session = Mockito.mock(Session.class);
        Mockito.when(I(session.getContextId())).thenReturn(I(1));

        LoginResultImpl loginResult = new LoginResultImpl(session, context, user);
        handler.setResult(loginResult);

        String sessionToken = sessionReservationService.reserveSessionFor(1, 1, 10, TimeUnit.SECONDS, Collections.emptyMap());
        String deepLinkParams = "!!&app=io.ox/mail&folder=default0/INBOX";
        URI uri = new URIBuilder()
            .setScheme("https")
            .setHost("webmail.example.com")
            .setPath("/appsuite/api/login")
            .setParameter("action", "samlLogin")
            .setParameter(SAMLLoginTools.PARAM_TOKEN, sessionToken)
            .setParameter(SAMLLoginTools.PARAM_URI_FRAGMENT, deepLinkParams)
            .build();
        SimHttpServletRequest loginHTTPRequest = prepareHTTPRequest("GET", uri);

        SimHttpServletResponse loginResponse = new SimHttpServletResponse();
        handler.handleRequest(loginHTTPRequest, loginResponse, new LoginRequestContext());
        Assert.assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, loginResponse.getStatus());
        String redirectLocation = loginResponse.getHeader("location");
        Assert.assertNotNull(redirectLocation);
        Matcher sessionMatcher = Pattern.compile("session=([a-z0-9]+)").matcher(redirectLocation);
        Assert.assertTrue(sessionMatcher.find());

        Matcher deepLinkMatcher = Pattern.compile(Pattern.quote("&" + deepLinkParams)).matcher(redirectLocation);
        Assert.assertTrue(deepLinkMatcher.find());
    }

    @Test
    public void deepLinkWithAutoLogin() throws Exception {
        Session session = sessiondService.addSession(buildAddSessionParameter());

        String sessionToken = sessionReservationService.reserveSessionFor(1, 1, 10, TimeUnit.SECONDS, Collections.emptyMap());

        String deepLinkParams = "!!&app=io.ox/mail&folder=default0/INBOX";
        URI uri = new URIBuilder()
            .setScheme("https")
            .setHost("webmail.example.com")
            .setPath("/appsuite/api/login")
            .setParameter("action", "samlLogin")
            .setParameter(SAMLLoginTools.PARAM_TOKEN, sessionToken)
            .setParameter(SAMLLoginTools.PARAM_URI_FRAGMENT, deepLinkParams)
            .build();
        SimHttpServletRequest loginHTTPRequest = prepareHTTPRequest("GET", uri);

        String cookieHash = HashCalculator.getInstance().getHash(
            loginHTTPRequest,
            LoginTools.parseUserAgent(loginHTTPRequest),
            LoginTools.parseClient(loginHTTPRequest, false, loginConfigurationLookup.getLoginConfiguration().getDefaultClient()));
        List<Cookie> cookies = new ArrayList<>();
        cookies.add(new Cookie(LoginServlet.SECRET_PREFIX + cookieHash, session.getSecret()));
        cookies.add(new Cookie(LoginServlet.SESSION_PREFIX + cookieHash, session.getSessionID()));
        loginHTTPRequest.setCookies(cookies);

        SimHttpServletResponse loginResponse = new SimHttpServletResponse();
        handler.handleRequest(loginHTTPRequest, loginResponse, new LoginRequestContext());
        Assert.assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, loginResponse.getStatus());
        String redirectLocation = loginResponse.getHeader("location");
        Assert.assertNotNull(redirectLocation);
        Matcher sessionMatcher = Pattern.compile("session=([a-z0-9]+)").matcher(redirectLocation);
        Assert.assertTrue(sessionMatcher.find());
        Assert.assertEquals(session.getSessionID(), sessionMatcher.group(1));

        Matcher deepLinkMatcher = Pattern.compile(Pattern.quote("&" + deepLinkParams)).matcher(redirectLocation);
        Assert.assertTrue(deepLinkMatcher.find());
    }

    @Test
    public void deepLinkWithSessionIndexLogin() throws Exception {
        final String samlSessionIndex = UUIDs.getUnformattedString(UUID.randomUUID());
        Session session = sessiondService.addSession(buildAddSessionParameter(new SessionEnhancement() {
            @Override
            public void enhanceSession(Session session) {
                session.setParameter(SAMLSessionParameters.SESSION_INDEX, samlSessionIndex);
            }
        }));

        Map<String, String> params = ImmutableMap.<String, String>builder().put(SAMLSessionParameters.SESSION_INDEX, samlSessionIndex).build();
        String sessionToken = sessionReservationService.reserveSessionFor(1, 1, 10, TimeUnit.SECONDS, params);

        String deepLinkParams = "!!&app=io.ox/mail&folder=default0/INBOX";
        URI uri = new URIBuilder()
            .setScheme("https")
            .setHost("webmail.example.com")
            .setPath("/appsuite/api/login")
            .setParameter("action", "samlLogin")
            .setParameter(SAMLLoginTools.PARAM_TOKEN, sessionToken)
            .setParameter(SAMLLoginTools.PARAM_URI_FRAGMENT, deepLinkParams)
            .build();
        SimHttpServletRequest loginHTTPRequest = prepareHTTPRequest("GET", uri);
        String cookieHash = HashCalculator.getInstance().getHash(
            loginHTTPRequest,
            LoginTools.parseUserAgent(loginHTTPRequest),
            LoginTools.parseClient(loginHTTPRequest, false, loginConfigurationLookup.getLoginConfiguration().getDefaultClient()));
        List<Cookie> cookies = new ArrayList<>();
        cookies.add(new Cookie(LoginServlet.SECRET_PREFIX + cookieHash, session.getSecret()));
        loginHTTPRequest.setCookies(cookies);

        SimHttpServletResponse loginResponse = new SimHttpServletResponse();
        handler.handleRequest(loginHTTPRequest, loginResponse, new LoginRequestContext());
        Assert.assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, loginResponse.getStatus());
        String redirectLocation = loginResponse.getHeader("location");
        Assert.assertNotNull(redirectLocation);
        Matcher sessionMatcher = Pattern.compile("session=([a-z0-9]+)").matcher(redirectLocation);
        Assert.assertTrue(sessionMatcher.find());
        Assert.assertEquals(session.getSessionID(), sessionMatcher.group(1));

        Matcher deepLinkMatcher = Pattern.compile(Pattern.quote("&" + deepLinkParams)).matcher(redirectLocation);
        Assert.assertTrue(deepLinkMatcher.find());
    }

    private static final class TestLoginRequestHandler extends SAMLLoginRequestHandler {

        private volatile LoginResult result;

        /**
         * Initializes a new {@link TestLoginRequestHandler}.
         * @param backend
         * @param loginConfigurationLookup
         * @param services
         */
        public TestLoginRequestHandler(SAMLBackend backend, LoginConfigurationLookup loginConfigurationLookup, ServiceLookup services) {
            super(backend, loginConfigurationLookup, services);
        }

        @Override
        protected LoginResult login(HttpServletRequest httpRequest, Context context, User user, Map<String, String> optState, LoginConfiguration loginConfiguration) throws OXException {
            LoginResult result = this.result;
            if (result == null) {
                Assert.fail("No login result expected");
            }
            this.result = null;
            return result;
        }

        public void setResult(LoginResult result) {
            this.result = result;
        }
    }

}
