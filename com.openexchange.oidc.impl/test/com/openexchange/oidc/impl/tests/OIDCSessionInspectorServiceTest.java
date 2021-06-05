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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.impl.OIDCSessionInspectorService;
import com.openexchange.oidc.osgi.OIDCBackendRegistry;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.spi.AbstractOIDCBackend;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.session.oauth.SessionOAuthTokenService;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link OIDCSessionInspectorServiceTest} Testclass for {@link OIDCSessionInspectorService}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({OIDCSessionInspectorService.class, AbstractOIDCBackend.class, Services.class})
public class OIDCSessionInspectorServiceTest {

    @Mock
    private OIDCBackendRegistry mockedOidcBackends;

    @Mock
    private BundleContext mockedBundleContext;

    @Mock
    private HttpServletRequest mockedRequest;

    @Mock
    private HttpServletResponse mockedResponse;

    @Mock
    private Session mockedSession;

    @Mock
    private AbstractOIDCBackend mockedBackend;

    @Mock
    private OIDCBackendConfig mockedBackendConfig;

    private OIDCSessionInspectorService inspector;

    @Mock
    private List<OIDCBackend> mockedBackendList;

    @Mock
    private SessiondService mockedSessiondService;

    @Mock
    private SessionOAuthTokenService tokenService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(SessiondService.class)).thenReturn(mockedSessiondService);
        Mockito.when(this.mockedBackend.getBackendConfig()).thenReturn(this.mockedBackendConfig);
        Mockito.when(this.mockedOidcBackends.getAllRegisteredBackends()).thenReturn(this.mockedBackendList);
        Mockito.when(mockedSession.getParameter(OIDCTools.IDTOKEN)).thenReturn(new Object());
        Mockito.when(mockedSession.getParameter(Session.PARAM_LOCK)).thenReturn(new ReentrantLock());
        this.inspector = PowerMockito.spy(new OIDCSessionInspectorService(this.mockedOidcBackends, this.tokenService));
    }

    @Test
    public void onSessionHit_NoBackendPathInSessionTest() throws Exception {
        Mockito.when(this.mockedSession.getParameter(OIDCTools.BACKEND_PATH)).thenReturn(null);
        Reply result = this.inspector.onSessionHit(this.mockedSession, this.mockedRequest, this.mockedResponse);
        assertTrue("Wrong reply", result == Reply.NEUTRAL);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onSessionHit_NoBackendTrackedTest() {
        Mockito.when(this.mockedOidcBackends.getAllRegisteredBackends()).thenReturn(Collections.<OIDCBackend>emptyList());
        SessionInspectorService emptyInspector = new OIDCSessionInspectorService(this.mockedOidcBackends, this.tokenService);
        Mockito.when(this.mockedSession.getParameter(OIDCTools.BACKEND_PATH)).thenReturn("backendPath");
        Mockito.when(this.mockedBundleContext.getService(ArgumentMatchers.any(ServiceReference.class))).thenReturn("wrongPath");
        try {
            emptyInspector.onSessionHit(this.mockedSession, this.mockedRequest, this.mockedResponse);
        } catch (OXException e) {
            assertTrue("Wrong error message thrown.", e.getExceptionCode() == OIDCExceptionCode.UNABLE_TO_FIND_BACKEND_FOR_SESSION);
            return;
        }
        fail("No error was thrown, but expected");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void onSessionHit_IgnoreUnmanagedSession() throws Exception {
        PowerMockito.doReturn(mockedBackend).when(this.inspector, PowerMockito.method(OIDCSessionInspectorService.class, "loadBackendForSession", Session.class)).withArguments(ArgumentMatchers.any(Session.class));
        Mockito.when(mockedSession.getParameter(OIDCTools.IDTOKEN)).thenReturn(null);
        Mockito.when(B(mockedSession.containsParameter(OIDCTools.IDTOKEN))).thenReturn(B(false));

        Reply result = this.inspector.onSessionHit(this.mockedSession, this.mockedRequest, this.mockedResponse);

        Mockito.verify(mockedBackend, Mockito.times(0)).isTokenExpired(mockedSession);
        Mockito.verify(mockedBackend, Mockito.times(0)).updateOauthTokens(mockedSession);
        Mockito.verify(mockedBackend, Mockito.times(0)).logoutCurrentUser(mockedSession, mockedRequest, mockedResponse);
        assertTrue("Wrong reply", result == Reply.NEUTRAL);
    }
}
