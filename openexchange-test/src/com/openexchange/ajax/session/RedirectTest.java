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

package com.openexchange.ajax.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.ajax.session.actions.RedirectRequest;
import com.openexchange.ajax.session.actions.RedirectResponse;
import com.openexchange.ajax.session.actions.StoreRequest;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ReinitializableConfigProviderService;
import com.openexchange.config.internal.ConfigurationImpl;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.groupware.Init;
import com.openexchange.java.Strings;
import com.openexchange.login.ConfigurationProperty;
import com.openexchange.tools.servlet.http.Tools;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RedirectTest extends AbstractAJAXSession {

    private String login;

    private String password;

    private boolean insecure;

    public RedirectTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        AJAXConfig.init();
        login = AJAXConfig.getProperty(Property.LOGIN) + "@" + AJAXConfig.getProperty(Property.CONTEXTNAME);
        password = AJAXConfig.getProperty(Property.PASSWORD);
        Init.injectProperty();
        ConfigurationService configService = new ConfigurationImpl(Collections.<ReinitializableConfigProviderService> emptyList());
        final String value = configService.getProperty(
            ConfigurationProperty.INSECURE.getPropertyName(),
            ConfigurationProperty.INSECURE.getDefaultValue());
        insecure = Boolean.parseBoolean(value);
    }

    @Override
    protected void tearDown() throws Exception {
        login = null;
        password = null;
        super.tearDown();
    }

    public void testRedirect() throws Throwable {
        final AJAXSession session = new AJAXSession();
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            // Create session.
            LoginResponse lResponse = myClient.execute(new LoginRequest(
                login,
                password,
                LoginTools.generateAuthId(),
                RedirectTest.class.getName(),
                "6.19.0"));

            /*
             * US 52869957 sets com.openexchange.ajax.login.randomToken=false by default. This test only has to run when the response
             * contains a random
             */
            if (Strings.isEmpty(lResponse.getRandom())) {
                return;
            }

            // Activate Autologin
            myClient.execute(new StoreRequest(lResponse.getSessionId(), false));

            List<String> cookieList = new ArrayList<String>();
            for (Cookie cookie : session.getHttpClient().getCookieStore().getCookies()) {
                if (!Tools.JSESSIONID_COOKIE.equals(cookie.getName())) {
                    cookieList.add(cookie.getName());
                }
            }

            // Remove cookies and that stuff.
            session.getHttpClient().getCookieStore().clear();
            session.getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
            // Test redirect
            final String clientIdentifier = insecure ? RedirectTest.class.getName() + '2' : RedirectTest.class.getName();
            RedirectRequest request = new RedirectRequest(lResponse.getJvmRoute(), lResponse.getRandom(), clientIdentifier);
            final RedirectResponse rResponse = myClient.execute(request);
            assertNotNull("Redirect location is missing.", rResponse.getLocation());

            for (Cookie cookie : session.getHttpClient().getCookieStore().getCookies()) {
                String name = cookie.getName();
                if (insecure) {
                    assertFalse("Cookie " + name + " was not removed after redirect.", cookieList.contains(name));
                }
            }
            // To get logout with tearDown() working.
            session.setId(lResponse.getSessionId());
        } finally {
            myClient.logout();
        }
    }
}
