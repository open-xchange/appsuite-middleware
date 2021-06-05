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

package com.openexchange.ajax.session;

import static com.openexchange.java.Autoboxing.I;
import static javax.servlet.http.HttpServletResponse.SC_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.session.actions.EmptyHttpAuthRequest;
import com.openexchange.ajax.session.actions.HttpAuthRequest;
import com.openexchange.ajax.session.actions.HttpAuthResponse;

/**
 * no autologin with httpauth
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug34928Test extends AbstractAJAXSession {

    private AJAXClient client;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = new AJAXClient(new AJAXSession(), true);
        client.getSession().getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
    }

    @Test
    public void testAutoHttpAuthLogin() throws Exception {
        /*
         * perform initial HTTP Auth login
         */
        String firstSessionID = firstHttpAuthLogin();
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

    @Test
    public void testAutoHttpLoginWithWrongSecretCookie() throws Exception {
        /*
         * perform initial HTTP Auth login
         */
        String firstSessionID = firstHttpAuthLogin();
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

    @Test
    public void testAutoHttpLoginWithWrongSessionCookie() throws Exception {
        /*
         * perform initial HTTP Auth login
         */
        String firstSessionID = firstHttpAuthLogin();
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

    private String firstHttpAuthLogin() throws Exception {
        HttpAuthResponse httpAuthResponse = client.execute(new HttpAuthRequest(testUser.getLogin(), testUser.getPassword()));
        String sessionID = extractSessionID(httpAuthResponse);
        client.getSession().setId(sessionID);
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
        return location.substring(sessionStart + 8);
    }
}
