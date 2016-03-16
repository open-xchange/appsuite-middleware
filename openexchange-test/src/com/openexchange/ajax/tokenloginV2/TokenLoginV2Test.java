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

package com.openexchange.ajax.tokenloginV2;

import static com.openexchange.ajax.session.LoginTools.generateAuthId;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LoginRequest.TokenLoginParameters;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.ajax.session.actions.TokenLoginV2Request;
import com.openexchange.ajax.session.actions.TokenLoginV2Response;
import com.openexchange.configuration.AJAXConfig;
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

    public TokenLoginV2Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testAcquire() throws Exception {
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = getClient().execute(request);
        String token = response.getToken();
        assertNotNull("Missing token.", token);
        assertFalse("Invalid token.", token.equals(""));

        assertEquals("Different token.", token, getClient().execute(request).getToken());
    }

    public void testLoginWithPassword() throws Exception {
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = getClient().execute(request);
        String token = response.getToken();

        LoginRequest login = new LoginRequest(new TokenLoginParameters(token, SECRET_1, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"));
        AJAXClient client = new AJAXClient();
        LoginResponse loginResponse = client.execute(login);
        assertEquals("Wrong password.", AJAXConfig.getProperty(User.User1.getPassword()), loginResponse.getPassword());

        Thread.sleep(500);
        login = new LoginRequest(new TokenLoginParameters(token, SECRET_1, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"), false);
        client = new AJAXClient();
        loginResponse = client.execute(login);

        assertTrue("Error expected, but got: " + loginResponse.getResponse().toString(), loginResponse.hasError());

        int expectedErrorCode = TokenLoginExceptionCodes.NO_SUCH_TOKEN.getNumber();
        int actualErrorCode = loginResponse.getException().getCode();
        assertEquals("Wrong error. Expected \"" + expectedErrorCode + "\", but was \""+ actualErrorCode+"\".", expectedErrorCode, actualErrorCode);
    }

    public void testLoginWithoutPassword() throws Exception {
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = getClient().execute(request);
        String token = response.getToken();

        LoginRequest login = new LoginRequest(new TokenLoginParameters(token, SECRET_2, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"));
        AJAXClient client = new AJAXClient();
        LoginResponse loginResponse = client.execute(login);
        assertNull("No password expected.", loginResponse.getPassword());

        login = new LoginRequest(new TokenLoginParameters(token, SECRET_2, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"), false);
        client = new AJAXClient();
        loginResponse = client.execute(login);
        assertTrue("Error expected", loginResponse.hasError());
        assertEquals("Wrong error.", TokenLoginExceptionCodes.NO_SUCH_TOKEN.getNumber(), loginResponse.getException().getCode());
    }

    public void testBadSecret() throws Exception {
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = getClient().execute(request);
        String token = response.getToken();

        LoginRequest login = new LoginRequest(new TokenLoginParameters(token, "blubb", generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"), false);
        AJAXClient client = new AJAXClient();
        LoginResponse loginResponse = client.execute(login);

        assertTrue("Error expected.", loginResponse.hasError());
        assertEquals("Wrong error.", TokenLoginExceptionCodes.TOKEN_REDEEM_DENIED.getNumber(), loginResponse.getException().getCode());
    }

    public void testInvalidate() throws Exception {
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = getClient().execute(request);
        String token = response.getToken();

        getClient().logout();

        LoginRequest login = new LoginRequest(new TokenLoginParameters(token, SECRET_1, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"), false);
        AJAXClient client = new AJAXClient();
        LoginResponse loginResponse = client.execute(login);

        assertTrue("Error expected.", loginResponse.hasError());
        assertTrue("Wrong error.", loginResponse.getException().getCode() == TokenLoginExceptionCodes.NO_SUCH_TOKEN.getNumber() || loginResponse.getException().getCode() == TokenLoginExceptionCodes.NO_SUCH_SESSION_FOR_TOKEN.getNumber());
    }

    public void testBadToken() throws Exception {
        LoginRequest login = new LoginRequest(new TokenLoginParameters("phantasyToken", SECRET_1, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0"), false);
        AJAXClient client = new AJAXClient();
        LoginResponse loginResponse = client.execute(login);

        assertTrue("Error expected.", loginResponse.hasError());
    }

    public void testRedirect() throws Exception {
        final String REDIRECT = client.getHostname() + "/tokenRedirectTest";
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = client.execute(request);
        String token = response.getToken();
        assertNotNull("Missing token.", token);
        assertFalse("Invalid token.", token.equals(""));
        AJAXClient client2 = new AJAXClient();
        TokenLoginV2Request login = new TokenLoginV2Request(token, SECRET_1, generateAuthId(), TokenLoginV2Test.class.getName(), "7.8.0", REDIRECT);
        TokenLoginV2Response loginResponse = client2.execute(login);
        assertTrue("Tokenlogin failed.", loginResponse.isLoginSuccessful());
        assertTrue("Redirect urls does not match.", loginResponse.getRedirectUrl().startsWith(REDIRECT));
    }

    public void testLoginWithPasswordInURI_doNotAccept() throws Exception {
        AcquireTokenRequest request = new AcquireTokenRequest();
        AcquireTokenResponse response = getClient().execute(request);
        String token = response.getToken();

        LoginRequest login = new LoginRequest(token, SECRET_1, generateAuthId(), TokenLoginV2Test.class.getName(), "7.4.0", false, true);
        LoginResponse loginResponse = client.execute(login);

        assertTrue("Error expected.", loginResponse.hasError());
        assertEquals("Wrong error.", AjaxExceptionCodes.NOT_ALLOWED_URI_PARAM.getNumber(), loginResponse.getException().getCode());
        assertTrue("Wrong param", loginResponse.getErrorMessage().contains("password"));
    }

}
