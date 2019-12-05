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

    private MockablePasswordGrantAuthentication authenticationService;

    private int userId;

    private int contextId;

    private String username;

    private String loginmapping;

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

        when(user.getId()).thenReturn(userId);
        when(user.getLoginInfo()).thenReturn(username);

        when(context.getContextId()).thenReturn(contextId);
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
        when(session.containsParameter(OIDCTools.IDTOKEN)).thenReturn(false);
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
