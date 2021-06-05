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

package com.openexchange.passwordchange.servlet;

import static com.openexchange.java.Autoboxing.B;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;
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
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.passwordchange.PasswordChangeEvent;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link PasswordChangeServletTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.6.2
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ SessionUtility.class, AJAXServlet.class, Tools.class })
public class PasswordChangeServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServerSession session;

    @Mock
    private ServiceLookup serviceLookup;

    @Mock
    private PasswordChangeService passwordChangeService;

    @Mock
    private ContextService contextService;

    @Mock
    private UserService userService;

    @Mock
    private User user;

    private final String requestBody = "{\"old_password\":\"secret\",\"new_password\":\"secret1\",\"new_password2\":\"secret1\"}";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(AJAXServlet.class);
        PowerMockito.when(AJAXServlet.getBody((HttpServletRequest) ArgumentMatchers.any())).thenReturn(requestBody);

        PowerMockito.mockStatic(SessionUtility.class);
        PowerMockito.when(SessionUtility.getSessionObject((ServletRequest) ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(session);

        PowerMockito.mockStatic(Tools.class);
        Map<String, List<String>> map = Collections.emptyMap();
        PowerMockito.when(Tools.copyHeaders((HttpServletRequest) ArgumentMatchers.any())).thenReturn(map);

        PowerMockito.when(serviceLookup.getService(PasswordChangeService.class)).thenReturn(passwordChangeService);
        PowerMockito.when(serviceLookup.getService(ContextService.class)).thenReturn(contextService);
        PowerMockito.when(serviceLookup.getService(UserService.class)).thenReturn(userService);
        PowerMockito.when(userService.getUser(ArgumentMatchers.anyInt(), (Context) ArgumentMatchers.any())).thenReturn(user);
        PowerMockito.when(B(user.isGuest())).thenReturn(Boolean.FALSE);
    }

     @Test
     public void testActionPutUpdate_everythingFine_callPasswordChangeServiceOnce() throws JSONException, IOException, OXException {
        PasswordChangeServlet servlet = new PasswordChangeServlet(serviceLookup);
        servlet.actionPutUpdate(request, response);

        Mockito.verify(passwordChangeService, Mockito.times(1)).perform((PasswordChangeEvent) ArgumentMatchers.any());
    }

     @Test
     public void testActionPutUpdate_oldPasswordMissing_doNotUpdate() throws JSONException, IOException, OXException {
        PowerMockito.when(AJAXServlet.getBody((HttpServletRequest) ArgumentMatchers.any())).thenReturn("{\"new_password\":\"secret1\",\"new_password2\":\"secret1\"}");

        PasswordChangeServlet servlet = new PasswordChangeServlet(serviceLookup);
        servlet.actionPutUpdate(request, response);

        Mockito.verify(passwordChangeService, Mockito.never()).perform((PasswordChangeEvent) ArgumentMatchers.any());
    }

     @Test
     public void testActionPutUpdate_newPassword1Missing_doNotUpdate() throws JSONException, IOException, OXException {
        PowerMockito.when(AJAXServlet.getBody((HttpServletRequest) ArgumentMatchers.any())).thenReturn("{\"old_password\":\"secret\",\"new_password2\":\"secret1\"}");

        PasswordChangeServlet servlet = new PasswordChangeServlet(serviceLookup);
        servlet.actionPutUpdate(request, response);

        Mockito.verify(passwordChangeService, Mockito.never()).perform((PasswordChangeEvent) ArgumentMatchers.any());
    }

     @Test
     public void testActionPutUpdate_newPassword2Missing_doNotUpdate() throws JSONException, IOException, OXException {
        PowerMockito.when(AJAXServlet.getBody((HttpServletRequest) ArgumentMatchers.any())).thenReturn("{\"old_password\":\"secret\",\"new_password1\":\"secret1\"}");

        PasswordChangeServlet servlet = new PasswordChangeServlet(serviceLookup);
        servlet.actionPutUpdate(request, response);

        Mockito.verify(passwordChangeService, Mockito.never()).perform((PasswordChangeEvent) ArgumentMatchers.any());
    }

     @Test
     public void testActionPutUpdate_contextServiceAbsent_doNotUpdate() throws JSONException, IOException, OXException {
        PowerMockito.when(serviceLookup.getService(ContextService.class)).thenReturn(null);

        PasswordChangeServlet servlet = new PasswordChangeServlet(serviceLookup);
        servlet.actionPutUpdate(request, response);

        Mockito.verify(passwordChangeService, Mockito.never()).perform((PasswordChangeEvent) ArgumentMatchers.any());
    }

     @Test
     public void testActionPutUpdate_passwordChangeServiceAbsent_doNotUpdate() throws JSONException, IOException, OXException {
        PowerMockito.when(serviceLookup.getService(PasswordChangeService.class)).thenReturn(null);

        PasswordChangeServlet servlet = new PasswordChangeServlet(serviceLookup);
        servlet.actionPutUpdate(request, response);

        Mockito.verify(passwordChangeService, Mockito.never()).perform((PasswordChangeEvent) ArgumentMatchers.any());
    }


}
