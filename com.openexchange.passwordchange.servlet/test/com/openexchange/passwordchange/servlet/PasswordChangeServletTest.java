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

package com.openexchange.passwordchange.servlet;

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
import org.mockito.Matchers;
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
import com.openexchange.groupware.ldap.User;
import com.openexchange.passwordchange.PasswordChangeEvent;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
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
        PowerMockito.when(AJAXServlet.getBody((HttpServletRequest) Matchers.any())).thenReturn(requestBody);

        PowerMockito.mockStatic(SessionUtility.class);
        PowerMockito.when(SessionUtility.getSessionObject((ServletRequest) Matchers.any(), Matchers.anyBoolean())).thenReturn(session);

        PowerMockito.mockStatic(Tools.class);
        Map<String, List<String>> map = Collections.emptyMap();
        PowerMockito.when(Tools.copyHeaders((HttpServletRequest) Matchers.any())).thenReturn(map);

        PowerMockito.when(serviceLookup.getService(PasswordChangeService.class)).thenReturn(passwordChangeService);
        PowerMockito.when(serviceLookup.getService(ContextService.class)).thenReturn(contextService);
        PowerMockito.when(serviceLookup.getService(UserService.class)).thenReturn(userService);
        PowerMockito.when(userService.getUser(Matchers.anyInt(), (Context) Matchers.any())).thenReturn(user);
        PowerMockito.when(user.isGuest()).thenReturn(Boolean.FALSE);
    }

    @Test
    public void testActionPutUpdate_everythingFine_callPasswordChangeServiceOnce() throws JSONException, IOException, OXException {
        PasswordChangeServlet servlet = new PasswordChangeServlet(serviceLookup);
        servlet.actionPutUpdate(request, response);

        Mockito.verify(passwordChangeService, Mockito.times(1)).perform((PasswordChangeEvent) Matchers.any());
    }

    @Test
    public void testActionPutUpdate_oldPasswordMissing_doNotUpdate() throws JSONException, IOException, OXException {
        PowerMockito.when(AJAXServlet.getBody((HttpServletRequest) Matchers.any())).thenReturn("{\"new_password\":\"secret1\",\"new_password2\":\"secret1\"}");

        PasswordChangeServlet servlet = new PasswordChangeServlet(serviceLookup);
        servlet.actionPutUpdate(request, response);

        Mockito.verify(passwordChangeService, Mockito.never()).perform((PasswordChangeEvent) Matchers.any());
    }

    @Test
    public void testActionPutUpdate_newPassword1Missing_doNotUpdate() throws JSONException, IOException, OXException {
        PowerMockito.when(AJAXServlet.getBody((HttpServletRequest) Matchers.any())).thenReturn("{\"old_password\":\"secret\",\"new_password2\":\"secret1\"}");

        PasswordChangeServlet servlet = new PasswordChangeServlet(serviceLookup);
        servlet.actionPutUpdate(request, response);

        Mockito.verify(passwordChangeService, Mockito.never()).perform((PasswordChangeEvent) Matchers.any());
    }

    @Test
    public void testActionPutUpdate_newPassword2Missing_doNotUpdate() throws JSONException, IOException, OXException {
        PowerMockito.when(AJAXServlet.getBody((HttpServletRequest) Matchers.any())).thenReturn("{\"old_password\":\"secret\",\"new_password1\":\"secret1\"}");

        PasswordChangeServlet servlet = new PasswordChangeServlet(serviceLookup);
        servlet.actionPutUpdate(request, response);

        Mockito.verify(passwordChangeService, Mockito.never()).perform((PasswordChangeEvent) Matchers.any());
    }

    @Test
    public void testActionPutUpdate_contextServiceAbsent_doNotUpdate() throws JSONException, IOException, OXException {
        PowerMockito.when(serviceLookup.getService(ContextService.class)).thenReturn(null);

        PasswordChangeServlet servlet = new PasswordChangeServlet(serviceLookup);
        servlet.actionPutUpdate(request, response);

        Mockito.verify(passwordChangeService, Mockito.never()).perform((PasswordChangeEvent) Matchers.any());
    }

    @Test
    public void testActionPutUpdate_passwordChangeServiceAbsent_doNotUpdate() throws JSONException, IOException, OXException {
        PowerMockito.when(serviceLookup.getService(PasswordChangeService.class)).thenReturn(null);

        PasswordChangeServlet servlet = new PasswordChangeServlet(serviceLookup);
        servlet.actionPutUpdate(request, response);

        Mockito.verify(passwordChangeService, Mockito.never()).perform((PasswordChangeEvent) Matchers.any());
    }


}
