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

package com.openexchange.oidc.impl.tests;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.api.AuthType;
import com.openexchange.mail.api.AuthenticationFailedHandler.Service;
import com.openexchange.mail.api.AuthenticationFailureHandlerResult;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.impl.OIDCAuthenticationFailedHandler;
import com.openexchange.oidc.osgi.OIDCBackendRegistry;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.tools.MockablePasswordGrantAuthentication;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.oidc.tools.TestBackendConfig;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link OIDCAuthenticationFailedHandlerTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
@SuppressWarnings("deprecation")
@RunWith(PowerMockRunner.class)
public class OIDCAuthenticationFailedHandlerTest {

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
    private SessiondService sessiondService;

    @Mock
    private OIDCBackendRegistry backendRegistry;

    @Mock
    private OIDCBackend backend;

    @Spy
    private OIDCBackendConfig config = new TestBackendConfig();

    @SuppressWarnings("unused")
    private MockablePasswordGrantAuthentication authenticationService;

    private int userId;

    private int contextId;

    private String username;

    private String loginmapping;

    @SuppressWarnings("unused")
    private String password;

    private OIDCAuthenticationFailedHandler authenticationFailedHandler;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        authenticationFailedHandler = Mockito.spy(new OIDCAuthenticationFailedHandler());

        userId = ThreadLocalRandom.current().nextInt(1);
        contextId = ThreadLocalRandom.current().nextInt(1);
        username = "testuser";
        loginmapping = "example.com";
        password = "Secret123!";

        when(I(user.getId())).thenReturn(I(userId));
        when(user.getLoginInfo()).thenReturn(username);

        when(I(context.getContextId())).thenReturn(I(contextId));
        when(context.getLoginInfo()).thenReturn(new String[] { loginmapping });

        when(userService.getUser(userId, context)).thenReturn(user);
        when(contextService.getContext(contextId)).thenReturn(context);

        Services.services.set(mockedServices);
        when(mockedServices.getService(UserService.class)).thenReturn(userService);
        when(mockedServices.getService(ContextService.class)).thenReturn(contextService);

        when(mockedServices.getService(SessiondService.class)).thenReturn(sessiondService);

        when(backend.getBackendConfig()).thenReturn(config);
        when(backendRegistry.getAllRegisteredBackends()).thenReturn(Collections.singletonList(backend));

        authenticationService = Mockito.spy(new MockablePasswordGrantAuthentication(backendRegistry));
    }

    @Test
    public void testIgnoreNonOAuthSessions() throws Exception {
        Session session = Mockito.mock(Session.class);
        MailConfig mailConfig = Mockito.mock(MailConfig.class);
        for (AuthType type : AuthType.values()) {
            if (type != AuthType.OAUTH && type != AuthType.OAUTHBEARER) {
                Mockito.reset(mailConfig);
                when(mailConfig.getAuthType()).thenReturn(type);

                AuthenticationFailureHandlerResult result = authenticationFailedHandler.handleAuthenticationFailed(Mockito.mock(OXException.class), Service.MAIL, mailConfig, session);
                assertEquals(AuthenticationFailureHandlerResult.Type.CONTINUE, result.getType());
                verify(backend, Mockito.times(0)).isTokenExpired(session);
                verify(backend, Mockito.times(0)).updateOauthTokens(session);
                verify(sessiondService, Mockito.times(0)).removeSession(session.getSessionID());
                verify(mailConfig, Mockito.times(0)).setPassword(ArgumentMatchers.anyString());
            }
        }
    }

    @Test
    public void testIgnoreNonOIDCSessions() throws Exception {
        Session session = Mockito.mock(Session.class);
        when(session.getParameter(OIDCTools.IDTOKEN)).thenReturn(null);
        when(B(session.containsParameter(OIDCTools.IDTOKEN))).thenReturn(B(false));
        MailConfig mailConfig = Mockito.mock(MailConfig.class);
        when(mailConfig.getAuthType()).thenReturn(AuthType.OAUTHBEARER);

        AuthenticationFailureHandlerResult result = authenticationFailedHandler.handleAuthenticationFailed(Mockito.mock(OXException.class), Service.MAIL, mailConfig, session);
        assertEquals(AuthenticationFailureHandlerResult.Type.CONTINUE, result.getType());

        verify(backend, Mockito.times(0)).isTokenExpired(session);
        verify(backend, Mockito.times(0)).updateOauthTokens(session);
        verify(sessiondService, Mockito.times(0)).removeSession(session.getSessionID());
        verify(mailConfig, Mockito.times(0)).setPassword(ArgumentMatchers.anyString());
    }

}
