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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Collections;
import java.util.List;
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
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.inspector.SessionInspectorService;

/**
 * {@link OIDCSessionInspectorServiceTest} Testclass for {@link OIDCSessionInspectorService}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(OIDCSessionInspectorService.class)
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
    private OIDCBackend mockedBackend;
    
    @Mock
    private OIDCBackendConfig mockedBackendConfig;

    private OIDCSessionInspectorService inspector;

    @Mock
    private List<OIDCBackend> mockedBackendList;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(this.mockedBackend.getBackendConfig()).thenReturn(this.mockedBackendConfig);
        Mockito.when(this.mockedOidcBackends.getAllRegisteredBackends()).thenReturn(this.mockedBackendList);
        this.inspector = PowerMockito.spy(new OIDCSessionInspectorService(this.mockedOidcBackends));
    }

    @Test
    public void onSessionHit_NoBackendPathInSessionTest() throws Exception {
        Mockito.when(this.mockedSession.getParameter(OIDCTools.BACKEND_PATH)).thenReturn(null);
        Reply result = this.inspector.onSessionHit(this.mockedSession, this.mockedRequest, this.mockedResponse);
        assertTrue("Wrong reply", result == Reply.NEUTRAL);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onSessionHit_NoBackendTrackedTest() throws Exception {
        Mockito.when(this.mockedOidcBackends.getAllRegisteredBackends()).thenReturn(Collections.<OIDCBackend>emptyList());
        SessionInspectorService emptyInspector = new OIDCSessionInspectorService(this.mockedOidcBackends);
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

    @Test
    public void onSessionHit_TokenNotExpiredTest() throws Exception {
      PowerMockito.doReturn(mockedBackend).when(this.inspector, PowerMockito.method(OIDCSessionInspectorService.class, "loadBackendForSession", Session.class)).withArguments(ArgumentMatchers.any(Session.class));
      Mockito.when(mockedBackend.isTokenExpired(mockedSession)).thenReturn(false);
      
      Reply result = this.inspector.onSessionHit(this.mockedSession, this.mockedRequest, this.mockedResponse);
      
      Mockito.verify(mockedBackend, Mockito.times(0)).updateOauthTokens(mockedSession);
      assertTrue("Wrong reply", result == Reply.NEUTRAL);
    }

    @Test
    public void onSessionHit_UpdateTokensFailTest() throws Exception {
        PowerMockito.doReturn(mockedBackend).when(this.inspector, PowerMockito.method(OIDCSessionInspectorService.class, "loadBackendForSession", Session.class)).withArguments(ArgumentMatchers.any(Session.class));
        Mockito.when(mockedBackend.isTokenExpired(mockedSession)).thenReturn(true);
        Mockito.when(mockedBackend.updateOauthTokens(mockedSession)).thenReturn(false);
        
        Reply result = this.inspector.onSessionHit(this.mockedSession, this.mockedRequest, this.mockedResponse);
        
        Mockito.verify(mockedBackend, Mockito.times(1)).logoutCurrentUser(mockedSession, mockedRequest, mockedResponse);
        assertTrue("Wrong reply", result == Reply.NEUTRAL);
    }

    @Test
    public void onSessionHit_UpdateTokensSuccessTest() throws Exception {
        PowerMockito.doReturn(mockedBackend).when(this.inspector, PowerMockito.method(OIDCSessionInspectorService.class, "loadBackendForSession", Session.class)).withArguments(ArgumentMatchers.any(Session.class));
        Mockito.when(mockedBackend.isTokenExpired(mockedSession)).thenReturn(true);
        Mockito.when(mockedBackend.updateOauthTokens(mockedSession)).thenReturn(true);
        
        Reply result = this.inspector.onSessionHit(this.mockedSession, this.mockedRequest, this.mockedResponse);
        
        Mockito.verify(mockedBackend, Mockito.times(0)).logoutCurrentUser(mockedSession, mockedRequest, mockedResponse);
        assertTrue("Wrong reply", result == Reply.NEUTRAL);
    }
}
