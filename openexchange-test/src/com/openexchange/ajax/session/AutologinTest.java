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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpState;
import org.junit.Test;
import com.openexchange.ajax.LoginServlet;

/**
 * {@link AutologinTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AutologinTest extends AbstractLoginTest {

    public AutologinTest() {
        super();
    }

    @Test
    public void testGeeIForgotMySessionIDCanYouGiveItBack() throws Exception {
        as(USER1);
        inModule("login");
        call("store");

        String sessionID = currentClient.getSessionID();

        forgetSession();

        raw("autologin");

        for (String key : Arrays.asList("session", "random")) {
            assertTrue("Missing key: " + key, rawResponse.has(key));
        }

        assertEquals(sessionID, rawResponse.getString("session"));

        inModule("quota");
        call("filestore", "session", sessionID); // Send some request.

        assertNoError();
    }

    @Test
    public void testRetrieveSessionIDForCertainClient() throws Exception {
        createClient();
        inModule("login");

        String[] credentials = credentials(USER1);

        raw("login", "name", credentials[0], "password", credentials[1], "client", "testclient1");
        String sessionID = rawResponse.getString("session");

        call("store", "session", sessionID);

        forgetSession();
        raw("autologin", "client", "testclient1");

        assertEquals(sessionID, rawResponse.get("session"));

    }

    // Error Cases
    @Test
    public void testUnknownSession() throws Exception {
        as(USER1);
        inModule("login");
        call("store");

        HttpState state = currentClient.getClient().getState();

        Cookie[] cookies = state.getCookies();
        boolean replaced = false;
        for (Cookie cookie : cookies) {
            if (cookie.getName().startsWith(LoginServlet.SESSION_PREFIX)) {
                cookie.setValue("1234567");
                state.addCookie(cookie);
                replaced = true;
                break;
            }
        }
        assertTrue("Could not find session cookie", replaced);

        forgetSession();

        call("autologin");
        assertError();
        assertNoOXCookies();
    }

    @Test
    public void testSessionAndSecretMismatch() throws Exception {
        as(USER1);
        String sessionID = currentClient.getSessionID();

        as(USER2);
        inModule("login");
        call("store");

        HttpState state = currentClient.getClient().getState();

        Cookie[] cookies = state.getCookies();
        boolean replaced = false;
        for (Cookie cookie : cookies) {
            if (cookie.getName().startsWith(LoginServlet.SESSION_PREFIX)) {
                cookie.setValue(sessionID); // The session of user 1
                state.addCookie(cookie);
                replaced = true;
                break;
            }
        }
        assertTrue("Could not find session cookie", replaced);

        forgetSession();
        call("autologin");

        assertError();
        assertNoOXCookies();
    }

    private void forgetSession() {
        currentClient.setSessionID(null);
    }

}
