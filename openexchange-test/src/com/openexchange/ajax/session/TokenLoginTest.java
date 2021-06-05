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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.stream.Stream;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.session.actions.LogoutRequest;
import com.openexchange.ajax.session.actions.TokenLoginJSONRequest;
import com.openexchange.ajax.session.actions.TokenLoginJSONResponse;
import com.openexchange.ajax.session.actions.TokenLoginRequest;
import com.openexchange.ajax.session.actions.TokenLoginResponse;
import com.openexchange.ajax.session.actions.TokensRequest;
import com.openexchange.ajax.session.actions.TokensResponse;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * Tests the action tokenLogin of the login servlet.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class TokenLoginTest extends AbstractAJAXSession {

    @Test
    public void testTokenLogin() throws Exception {
        final AJAXSession session = new AJAXSession();
        session.getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            TokenLoginResponse response = myClient.execute(new TokenLoginRequest(testUser.getLogin(), testUser.getPassword()));
            assertNotNull("Path of redirect response is not found.", response.getPath());
            assertNotNull("Server side token not found as fragment.", response.getServerToken());
            // assertNotNull("Login string was not found as fragment.", response.getLogin());
            assertNotSame("", I(-1), I(response.getUserId()));
            assertNotNull("Language string was not found as fragment.", response.getLanguage());
            // Activate session with tokens.
            TokensResponse response2 = myClient.execute(new TokensRequest(response.getHttpSessionId(), response.getClientToken(), response.getServerToken()));
            session.setId(response2.getSessionId());
            // Test if session really works
            int userId = myClient.execute(new GetRequest(Tree.Identifier)).getInteger();
            assertTrue("Users identifier is somehow wrong. Check if session is correctly activated.", userId > 2);
        } finally {
            myClient.logout();
        }
    }

    @Test
    public void testTokenLoginSetShardCookie() throws Exception {
        final AJAXSession session = new AJAXSession();
        session.getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            TokenLoginResponse response = myClient.execute(new TokenLoginRequest(testUser.getLogin(), testUser.getPassword()));
            TokensResponse response2 = myClient.execute(new TokensRequest(response.getHttpSessionId(), response.getClientToken(), response.getServerToken()));
            assertTrue("Shard cookie is not set", isShardCookieSet(session));
            session.setId(response2.getSessionId());
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

    @Test
    public void testSessionExpire() throws Exception {
        final AJAXSession session = new AJAXSession();
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            TokenLoginResponse response = myClient.execute(new TokenLoginRequest(testUser.getLogin(), testUser.getPassword()));
            assertNotNull("Path of redirect response is not found.", response.getPath());
            assertNotNull("Server side token not found as fragment.", response.getServerToken());
            // assertNotNull("Login string was not found as fragment.", response.getLogin());
            assertNotSame("", I(-1), I(response.getUserId()));
            assertNotNull("Language string was not found as fragment.", response.getLanguage());
            Thread.sleep(60000);
            // Tokened session should be timed out.
            TokensResponse response2 = myClient.execute(new TokensRequest(response.getHttpSessionId(), response.getClientToken(), response.getServerToken(), false));
            assertTrue("Tokened session should be expired.", response2.hasError());
            // Check for correct exception
            OXException expected = OXExceptionFactory.getInstance().create(SessionExceptionCodes.NO_SESSION_FOR_SERVER_TOKEN, "a", "b");
            OXException actual = response2.getException();
            assertTrue("Wrong exception", actual.similarTo(expected));
        } finally {
            myClient.logout();
        }
    }

    @Test
    public void testTokenLogin_passwordInRequest_returnError() throws Exception {
        final AJAXSession session = new AJAXSession();
        session.getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            TokenLoginJSONResponse response = myClient.execute(new TokenLoginJSONRequest(testUser.getLogin(), testUser.getPassword(), false, true));

            assertNotNull(response);
            assertTrue("Error expected.", response.hasError());
            assertEquals("Wrong error.", AjaxExceptionCodes.NOT_ALLOWED_URI_PARAM.getNumber(), response.getException().getCode());
            assertTrue("Wrong param", response.getErrorMessage().contains("password"));
        } finally {
            myClient.logout();
        }
    }

    @Test
    public void testTokenLoginWithJSON_returnJsonResponse() throws Exception {
        final AJAXSession session = new AJAXSession();
        session.getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            TokenLoginJSONResponse response = myClient.execute(new TokenLoginJSONRequest(testUser.getLogin(), testUser.getPassword(), true));
            JSONObject json = response.getResponse().getJSON();
            assertNotNull(json);
            assertNotNull(json.get("jsessionid"));
            assertNull(response.getData());
            assertNull(response.getErrorMessage());
            assertNull(response.getConflicts());
            assertNull(response.getException());
        } finally {
            myClient.logout();
        }
    }

    @Test
    public void testTokenLoginWithJSONResponse_passwordInRequest_returnError() throws Exception {
        final AJAXSession session = new AJAXSession();
        session.getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            TokenLoginJSONResponse response = myClient.execute(new TokenLoginJSONRequest(testUser.getLogin(), testUser.getPassword(), true, true));

            JSONObject json = ResponseWriter.getJSON(response.getResponse());
            assertNotNull(json);
            assertTrue("Error expected.", response.hasError());
            assertEquals("Wrong error.", AjaxExceptionCodes.NOT_ALLOWED_URI_PARAM.getNumber(), response.getException().getCode());
            assertTrue("Wrong param", response.getErrorMessage().contains("password"));
        } finally {
            myClient.logout();
        }
    }

    @Test
    public void testSessionCookieWithStaySignedIn() throws Exception {
        final AJAXSession session = new AJAXSession();
        session.getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            TokenLoginResponse response = myClient.execute(new TokenLoginRequest(testUser.getLogin(), testUser.getPassword(), true));
            TokensResponse response2 = myClient.execute(new TokensRequest(response.getHttpSessionId(), response.getClientToken(), response.getServerToken()));

            Stream<Cookie> cookies = session.getHttpClient().getCookieStore().getCookies().stream();
            Cookie sessionCookie = cookies.filter(c -> c.getName().startsWith(LoginServlet.SESSION_PREFIX)).findFirst().orElse(null);
            assertNotNull("Session cookie is missing in tokens response", sessionCookie);
            assertNotNull("Session cookie has no expiry date set", sessionCookie.getExpiryDate());
            session.setId(response2.getSessionId());
        } finally {
            myClient.logout();
        }
    }

    @Test
    public void testSessionCookieWithoutStaySignedIn() throws Exception {
        final AJAXSession session = new AJAXSession();
        session.getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            TokenLoginResponse response = myClient.execute(new TokenLoginRequest(testUser.getLogin(), testUser.getPassword()));
            TokensResponse response2 = myClient.execute(new TokensRequest(response.getHttpSessionId(), response.getClientToken(), response.getServerToken()));

            Stream<Cookie> cookies = session.getHttpClient().getCookieStore().getCookies().stream();
            Cookie sessionCookie = cookies.filter(c -> c.getName().startsWith(LoginServlet.SESSION_PREFIX)).findFirst().orElse(null);
            assertNotNull("Session cookie is missing in tokens response", sessionCookie);
            assertNull("Session cookie has expiry date set but should not", sessionCookie.getExpiryDate());
            session.setId(response2.getSessionId());
        } finally {
            myClient.logout();
        }
    }

}
