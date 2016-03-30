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

import static com.openexchange.java.Autoboxing.I;
import static javax.servlet.http.HttpServletResponse.SC_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.session.actions.EmptyHttpAuthRequest;
import com.openexchange.ajax.session.actions.HttpAuthRequest;
import com.openexchange.ajax.session.actions.HttpAuthResponse;
import com.openexchange.ajax.session.actions.StoreRequest;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.java.Strings;

/**
 * no autologin with httpauth
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug34928Test extends AbstractAJAXSession {

    private AJAXClient client;
    private String login;
    private String password;

    /**
     * Initializes a new {@link Bug34928Test}.
     *
     * @param name The test name
     */
    public Bug34928Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        AJAXConfig.init();
        login = AJAXConfig.getProperty(Property.LOGIN) + "@" + AJAXConfig.getProperty(Property.CONTEXTNAME);
        password = AJAXConfig.getProperty(Property.PASSWORD);
        client = new AJAXClient(new AJAXSession(), true);
        client.getSession().getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
    }

    @Override
    protected void tearDown() throws Exception {
        if (null != client && false == Strings.isEmpty(client.getSession().getId())) {
            client.logout();
        }
        super.tearDown();
    }

    public void testAutoHttpAuthLogin() throws Exception {
        /*
         * perform initial HTTP Auth login & store session cookie
         */
        String firstSessionID = firstHttpAuthLogin(true);
        /*
         * perform second HTTP Auth login (without providing authorization headers)
         */
        client.getSession().setId(null);
        HttpAuthResponse httpAuthResponse = client.execute(new EmptyHttpAuthRequest(false, false, false));
        assertThat("Second authentication with cookies failed. Session " + firstSessionID, I(httpAuthResponse.getStatusCode()), equalTo(I(SC_FOUND)));
        String secondSessionID = extractSessionID(httpAuthResponse);
        assertNotNull("No session ID", secondSessionID);
        assertEquals("Different session IDs", firstSessionID, secondSessionID);
        /*
         * re-enable first session for logout in tearDown
         */
        client.getSession().setId(firstSessionID);
    }

    public void testAutoHttpLoginWithWrongSecretCookie() throws Exception {
        /*
         * perform initial HTTP Auth login & store session cookie
         */
        String firstSessionID = firstHttpAuthLogin(true);
        /*
         * perform second HTTP Auth login with wrong secret cookie
         */
        client.getSession().setId(null);
        BasicClientCookie cookie = findCookie(LoginServlet.SECRET_PREFIX);
        String correctSecret = cookie.getValue();
        cookie.setValue("wrongsecret");
        HttpAuthResponse httpAuthResponse = client.execute(new EmptyHttpAuthRequest(false, false, false));
        assertEquals("Wrong response code", HttpServletResponse.SC_UNAUTHORIZED, httpAuthResponse.getStatusCode());
        /*
         * re-enable first session for logout in tearDown
         */
        client.getSession().setId(firstSessionID);
        cookie.setValue(correctSecret);
    }

    public void testAutoHttpLoginWithWrongSessionCookie() throws Exception {
        /*
         * perform initial HTTP Auth login & store session cookie
         */
        String firstSessionID = firstHttpAuthLogin(true);
        /*
         * perform second HTTP Auth login with wrong secret cookie
         */
        client.getSession().setId(null);
        BasicClientCookie cookie = findCookie(LoginServlet.SESSION_PREFIX);
        String correctSession = cookie.getValue();
        cookie.setValue("wrongsecret");
        HttpAuthResponse httpAuthResponse = client.execute(new EmptyHttpAuthRequest(false, false, false));
        assertEquals("Wrong response code", HttpServletResponse.SC_UNAUTHORIZED, httpAuthResponse.getStatusCode());
        /*
         * re-enable first session for logout in tearDown
         */
        client.getSession().setId(firstSessionID);
        cookie.setValue(correctSession);
    }

    public void testAutoHttpLoginWithoutStore() throws Exception {
        /*
         * perform initial HTTP Auth login, don't store session cookie
         */
        String firstSessionID = firstHttpAuthLogin(false);
        /*
         * perform second HTTP Auth login
         */
        client.getSession().setId(null);
        HttpAuthResponse httpAuthResponse = client.execute(new EmptyHttpAuthRequest(false, false, false));
        assertEquals("Wrong response code", HttpServletResponse.SC_UNAUTHORIZED, httpAuthResponse.getStatusCode());
        /*
         * re-enable first session for logout in tearDown
         */
        client.getSession().setId(firstSessionID);
    }

    private String firstHttpAuthLogin(boolean store) throws Exception {
        HttpAuthResponse httpAuthResponse = client.execute(new HttpAuthRequest(login, password));
        String sessionID = extractSessionID(httpAuthResponse);
        client.getSession().setId(sessionID);
        if (store) {
            client.execute(new StoreRequest(sessionID));
        }
        return sessionID;
    }

    private BasicClientCookie findCookie(String prefix) {
        List<Cookie> cookies = client.getSession().getHttpClient().getCookieStore().getCookies();
        for (int i = 0; i < cookies.size(); i++) {
            if (cookies.get(i).getName().startsWith(prefix)) {
                return (BasicClientCookie) cookies.get(i);
            }
        }
        fail("No cookie with prefix \"" + prefix + "\" found");
        return null;
    }

    private static String extractSessionID(HttpAuthResponse httpAuthResponse) {
        String location = httpAuthResponse.getLocation();
        assertNotNull("Location is missing in response", location);
        int sessionStart = location.indexOf("session=");
        assertTrue("No session ID", 0 <= sessionStart);
        return location.substring(sessionStart + 8, location.indexOf('&', sessionStart + 8));
    }
}
