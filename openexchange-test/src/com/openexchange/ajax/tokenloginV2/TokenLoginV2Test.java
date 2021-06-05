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

package com.openexchange.ajax.tokenloginV2;

import static com.openexchange.ajax.session.LoginTools.generateAuthId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.apache.http.cookie.Cookie;
import org.junit.Test;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LoginRequest.TokenLoginParameters;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.ajax.session.actions.LogoutRequest;
import com.openexchange.ajax.session.actions.TokenLoginV2Request;
import com.openexchange.ajax.session.actions.TokenLoginV2Response;
import com.openexchange.tokenlogin.TokenLoginExceptionCodes;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link TokenLoginV2Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class TokenLoginV2Test extends AbstractAJAXSession {

    private static final String SECRET_1 = "1234";

    private static final String SECRET_2 = "4321";

    @Test
    public void testAcquire() throws Exception {
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = getClient().execute(request);
        String token = response.getToken();
        assertNotNull("Missing token.", token);
        assertFalse("Invalid token.", token.equals(""));

        assertEquals("Different token.", token, getClient().execute(request).getToken());
    }

    @Test
    public void testLoginWithPassword() throws Exception {
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = getClient().execute(request);
        String token = response.getToken();

        LoginRequest login = new LoginRequest(new TokenLoginParameters(token, SECRET_1, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"));
        AJAXClient client = new AJAXClient(new AJAXSession(), true);
        LoginResponse loginResponse = client.execute(login);
        assertEquals("Wrong password.", testUser.getPassword(), loginResponse.getPassword());

        Thread.sleep(500);
        login = new LoginRequest(new TokenLoginParameters(token, SECRET_1, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"), false);
        client = new AJAXClient(new AJAXSession(), true);
        loginResponse = client.execute(login);

        assertTrue("Error expected, but got: " + loginResponse.getResponse().toString(), loginResponse.hasError());

        int expectedErrorCode = TokenLoginExceptionCodes.NO_SUCH_TOKEN.getNumber();
        int actualErrorCode = loginResponse.getException().getCode();
        assertEquals("Wrong error. Expected \"" + expectedErrorCode + "\", but was \"" + actualErrorCode + "\".", expectedErrorCode, actualErrorCode);
    }

    @Test
    public void testLoginShardCookieSet() throws Exception {
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = getClient().execute(request);
        String token = response.getToken();

        LoginRequest login = new LoginRequest(new TokenLoginParameters(token, SECRET_1, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"));
        AJAXClient client = new AJAXClient(new AJAXSession(), true);
        LoginResponse loginResponse = client.execute(login);
        AJAXSession session = client.getSession();
        session.setId(loginResponse.getSessionId());
        assertTrue("Shard cookie is not set", isShardCookieSet(session));
        client.execute(new LogoutRequest(true));
        assertFalse("Shard cookie is still set", isShardCookieSet(client.getSession()));
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
    public void testLoginWithoutPassword() throws Exception {
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = getClient().execute(request);
        String token = response.getToken();

        LoginRequest login = new LoginRequest(new TokenLoginParameters(token, SECRET_2, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"));
        AJAXClient client = new AJAXClient(new AJAXSession(), true);
        LoginResponse loginResponse = client.execute(login);
        assertNull("No password expected.", loginResponse.getPassword());

        login = new LoginRequest(new TokenLoginParameters(token, SECRET_2, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"), false);
        client = new AJAXClient(new AJAXSession(), true);
        loginResponse = client.execute(login);
        assertTrue("Error expected", loginResponse.hasError());
        assertEquals("Wrong error.", TokenLoginExceptionCodes.NO_SUCH_TOKEN.getNumber(), loginResponse.getException().getCode());
    }

    @Test
    public void testBadSecret() throws Exception {
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = getClient().execute(request);
        String token = response.getToken();

        LoginRequest login = new LoginRequest(new TokenLoginParameters(token, "blubb", generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"), false);
        AJAXClient client = new AJAXClient(new AJAXSession(), true);
        LoginResponse loginResponse = client.execute(login);

        assertTrue("Error expected.", loginResponse.hasError());
        assertEquals("Wrong error.", TokenLoginExceptionCodes.TOKEN_REDEEM_DENIED.getNumber(), loginResponse.getException().getCode());
    }

    @Test
    public void testInvalidate() throws Exception {
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = getClient().execute(request);
        String token = response.getToken();

        getClient().logout();

        LoginRequest login = new LoginRequest(new TokenLoginParameters(token, SECRET_1, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"), false);
        AJAXClient client = new AJAXClient(new AJAXSession(), true);
        LoginResponse loginResponse = client.execute(login);

        assertTrue("Error expected.", loginResponse.hasError());
        assertTrue("Wrong error.", loginResponse.getException().getCode() == TokenLoginExceptionCodes.NO_SUCH_TOKEN.getNumber() || loginResponse.getException().getCode() == TokenLoginExceptionCodes.NO_SUCH_SESSION_FOR_TOKEN.getNumber());
    }

    @Test
    public void testBadToken() throws Exception {
        LoginRequest login = new LoginRequest(new TokenLoginParameters("phantasyToken", SECRET_1, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"), false);
        AJAXClient client = new AJAXClient(new AJAXSession(), true);
        LoginResponse loginResponse = client.execute(login);

        assertTrue("Error expected.", loginResponse.hasError());
    }

    @Test
    public void testRedirect() throws Exception {
        final String REDIRECT = getClient().getProtocol() + "://somewhereelse.com/tokenRedirectTest";
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = getClient().execute(request);
        String token = response.getToken();
        assertNotNull("Missing token.", token);
        assertFalse("Invalid token.", token.equals(""));
        AJAXClient client = new AJAXClient(new AJAXSession(), true);
        TokenLoginV2Request login = new TokenLoginV2Request(token, SECRET_1, generateAuthId(), TokenLoginV2Test.class.getName(), "7.8.0", REDIRECT);
        TokenLoginV2Response loginResponse = client.execute(login);
        assertTrue("Tokenlogin failed.", loginResponse.isLoginSuccessful());
        assertTrue("Redirect urls does not match.", loginResponse.getRedirectUrl().startsWith(REDIRECT));
    }

    @Test
    public void testLoginWithPasswordInURI_doNotAccept() throws Exception {
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = getClient().execute(request);
        String token = response.getToken();

        LoginRequest login = new LoginRequest(token, SECRET_1, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0", false, true);
        LoginResponse loginResponse = getClient().execute(login);

        assertTrue("Error expected.", loginResponse.hasError());
        assertEquals("Wrong error.", AjaxExceptionCodes.NOT_ALLOWED_URI_PARAM.getNumber(), loginResponse.getException().getCode());
        assertTrue("Wrong param", loginResponse.getErrorMessage().contains("password"));
    }

}
