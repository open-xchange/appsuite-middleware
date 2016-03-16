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
import org.apache.http.client.params.ClientPNames;
import org.json.JSONObject;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.session.actions.TokenLoginJSONRequest;
import com.openexchange.ajax.session.actions.TokenLoginJSONResponse;
import com.openexchange.ajax.session.actions.TokenLoginRequest;
import com.openexchange.ajax.session.actions.TokenLoginResponse;
import com.openexchange.ajax.session.actions.TokensRequest;
import com.openexchange.ajax.session.actions.TokensResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
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

    private String login;

    private String password;

    public TokenLoginTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        AJAXConfig.init();
        login = AJAXConfig.getProperty(Property.LOGIN) + "@" + AJAXConfig.getProperty(Property.CONTEXTNAME);
        password = AJAXConfig.getProperty(Property.PASSWORD);
    }

    @Override
    protected void tearDown() throws Exception {
        login = null;
        password = null;
        super.tearDown();
    }

    public void testTokenLogin() throws Exception {
        final AJAXSession session = new AJAXSession();
        session.getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            TokenLoginResponse response = myClient.execute(new TokenLoginRequest(login, password));
            assertNotNull("Path of redirect response is not found.", response.getPath());
            assertNotNull("Server side token not found as fragment.", response.getServerToken());
            assertNotNull("Login string was not found as fragment.", response.getLogin());
            assertNotSame("", I(-1), I(response.getUserId()));
            assertNotNull("Language string was not found as fragment.", response.getLanguage());
            // Activate session with tokens.
            TokensResponse response2 = myClient.execute(new TokensRequest(
                response.getHttpSessionId(),
                response.getClientToken(),
                response.getServerToken()));
            session.setId(response2.getSessionId());
            // Test if session really works
            int userId = myClient.execute(new GetRequest(Tree.Identifier)).getInteger();
            assertTrue("Users identifier is somehow wrong. Check if session is correctly activated.", userId > 2);
        } finally {
            myClient.logout();
        }
    }

    public void testSessionExpire() throws Exception {
        final AJAXSession session = new AJAXSession();
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            TokenLoginResponse response = myClient.execute(new TokenLoginRequest(login, password));
            assertNotNull("Path of redirect response is not found.", response.getPath());
            assertNotNull("Server side token not found as fragment.", response.getServerToken());
            assertNotNull("Login string was not found as fragment.", response.getLogin());
            assertNotSame("", I(-1), I(response.getUserId()));
            assertNotNull("Language string was not found as fragment.", response.getLanguage());
            Thread.sleep(60000);
            // Tokened session should be timed out.
            TokensResponse response2 = myClient.execute(new TokensRequest(
                response.getHttpSessionId(),
                response.getClientToken(),
                response.getServerToken(),
                false));
            assertTrue("Tokened session should be expired.", response2.hasError());
            // Check for correct exception
            OXException expected = OXExceptionFactory.getInstance().create(SessionExceptionCodes.NO_SESSION_FOR_SERVER_TOKEN, "a", "b");
            OXException actual = response2.getException();
            assertTrue("Wrong exception", actual.similarTo(expected));
        } finally {
            myClient.logout();
        }
    }

    public void testTokenLogin_passwordInRequest_returnError() throws Exception {
        final AJAXSession session = new AJAXSession();
        session.getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            TokenLoginJSONResponse response = myClient.execute(new TokenLoginJSONRequest(login, password, false, true));

            assertNotNull(response);
            assertTrue("Error expected.", response.hasError());
            assertEquals("Wrong error.", AjaxExceptionCodes.NOT_ALLOWED_URI_PARAM.getNumber(), response.getException().getCode());
            assertTrue("Wrong param", response.getErrorMessage().contains("password"));
        } finally {
            myClient.logout();
        }
    }


    public void testTokenLoginWithJSON_returnJsonResponse() throws Exception {
        final AJAXSession session = new AJAXSession();
        session.getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            TokenLoginJSONResponse response = myClient.execute(new TokenLoginJSONRequest(login, password, true));
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

    public void testTokenLoginWithJSONResponse_passwordInRequest_returnError() throws Exception {
        final AJAXSession session = new AJAXSession();
        session.getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            TokenLoginJSONResponse response = myClient.execute(new TokenLoginJSONRequest(login, password, true, true));

            JSONObject json = response.getResponse().getJSON();
            assertNotNull(json);
            assertTrue("Error expected.", response.hasError());
            assertEquals("Wrong error.", AjaxExceptionCodes.NOT_ALLOWED_URI_PARAM.getNumber(), response.getException().getCode());
            assertTrue("Wrong param", response.getErrorMessage().contains("password"));
        } finally {
            myClient.logout();
        }
    }
}
