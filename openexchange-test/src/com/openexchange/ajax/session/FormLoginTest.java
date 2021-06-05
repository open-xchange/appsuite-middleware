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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.apache.http.cookie.Cookie;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.session.actions.FormLoginRequest;
import com.openexchange.ajax.session.actions.FormLoginResponse;
import com.openexchange.ajax.session.actions.LogoutRequest;
import com.openexchange.test.common.test.pool.TestUser;

/**
 * Tests the action formLogin of the login servlet.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class FormLoginTest extends AbstractAJAXSession {

    private String login;

    private String password;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        TestUser testUser3 = testContext.acquireUser();
        login = testUser3.getLogin();
        password = testUser3.getPassword();
    }

    @Test
    public void testFormLogin() throws Exception {
        final AJAXSession session = new AJAXSession();
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            FormLoginResponse response = myClient.execute(new FormLoginRequest(login, password));
            assertNotNull("Path of redirect response is not found.", response.getPath());
            assertNotNull("Session identifier not found as fragment.", response.getSessionId());
            // assertNotNull("Login string was not found as fragment.", response.getLogin());
            assertNotSame("", I(-1), I(response.getUserId()));
            assertNotNull("Language string was not found as fragment.", response.getLanguage());
            session.setId(response.getSessionId());
        } finally {
            myClient.logout();
        }
    }

    @Test
    public void testFormLoginSetShardCookie() throws Exception {
        final AJAXSession session = new AJAXSession();
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            FormLoginResponse response = myClient.execute(new FormLoginRequest(login, password));
            assertTrue("Shard cookie is not set", isShardCookieSet(session));
            session.setId(response.getSessionId());
        } finally {
            myClient.execute(new LogoutRequest(true));
            assertFalse("Shard cookie is still set", isShardCookieSet(session));
            myClient.logout();
        }
    }

    private boolean isShardCookieSet(final AJAXSession session) {
        List<Cookie> cookies = session.getHttpClient().getCookieStore().getCookies();
        boolean isShardCookieSet = false;
        for (Cookie cookie : cookies) {
            isShardCookieSet = cookie.getName().equals(LoginServlet.SHARD_COOKIE_NAME);
        }
        return isShardCookieSet;
    }

    /**
     * This test is disabled because it tests a hidden feature. Normally the formLogin request requires an authId. This test verifies that
     * the formLogin request works without authId. To disable the required authId put
     * <code>com.openexchange.login.formLoginWithoutAuthId=true</code> into the login.properties configuration file.
     */
    public void dontTestFormLoginWithoutAuthId() throws Exception {
        final AJAXSession session = new AJAXSession();
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            FormLoginResponse response = myClient.execute(new FormLoginRequest(login, password, null));
            assertNotNull("Path of redirect response is not found.", response.getPath());
            assertNotNull("Session identifier not found as fragment.", response.getSessionId());
            //assertNotNull("Login string was not found as fragment.", response.getLogin());
            assertNotSame("", I(-1), I(response.getUserId()));
            assertNotNull("Language string was not found as fragment.", response.getLanguage());
            session.setId(response.getSessionId());
        } finally {
            myClient.logout();
        }
    }
}
