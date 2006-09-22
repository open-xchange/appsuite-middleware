/*
 * OPEN-XCHANGE - "the communication and information enviroment"
 *
 * All intellectual property rights in the Software are protected by
 * international copyright laws.
 *
 * OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all other
 * brand and product names are or may be trademarks of, and are used to identify
 * products or services of, their respective owners.
 *
 * Please make sure that third-party modules and libraries are used according to
 * their respective licenses.
 *
 * Any modifications to this package must retain all copyright notices of the
 * original copyright holder(s) for the original code used.
 *
 * After any such modifications, the original code will still remain copyrighted
 * by the copyright holder(s) or original author(s).
 *
 * Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 * mail:                    info@netline-is.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.openexchange.ajax;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * This class contains the login test. It also contains static methods to made
 * logins from other places.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginTest extends AbstractAJAXTest {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(LoginTest.class);

    /**
     * URL of the login AJAX servlet.
     */
    private static final String LOGIN_URL = "/ajax/login";

    /**
     * Default constructor.
     * @param name Name of the test.
     */
    public LoginTest(final String name) {
        super(name);
    }

    /**
     * Tests the login.
     * @throws Throwable if an error occurs.
     */
    public void testLogin() throws Throwable {
        final String sessionId = getSessionId(getWebConversation(),
            getHostName(), getLogin(), getPassword());
        assertNotNull("Got no sessionId", sessionId);
        assertTrue("Length of session identifier is zero.",
            sessionId.length() > 0);
    }

    /**
     * This method mades a login and returns the complete login object.
     * @param conversation WebConversation.
     * @param hostname hostname of the server running the server.
     * @param login Login of the user.
     * @param password Password of the user.
     * @return the session identifier if the login is successful.
     * @throws JSONException if parsing of serialized json fails.
     * @throws SAXException if a SAX error occurs.
     * @throws IOException if the communication with the server fails.
     */
    public static JSONObject login(final WebConversation conversation,
        final String hostname, final String login, final String password)
        throws IOException, SAXException, JSONException {
        LOG.trace("Logging in.");
        final WebRequest req = new GetMethodWebRequest(PROTOCOL
            + hostname + LOGIN_URL);
        req.setParameter("action", "login");
        req.setParameter("name", login);
        req.setParameter("password", password);
        req.setHeaderField("Content-Type", "");
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        final JSONObject json = new JSONObject(body);
        assertTrue("Session ID is missing: " + body, json.has(
            Login.PARAMETER_SESSION));
        assertTrue("Random is missing: " + body, json.has(Login._random));
        return json;
    }

    /**
     * This method mades a logout.
     * @param conversation WebConversation.
     * @param hostname hostname of the server running the server.
     * @param sessionId Session identifier of the user.
     * @throws IOException if the communication with the server fails.
     * @throws SAXException if a SAX error occurs.
     */
    public static void logout(final WebConversation conversation,
        final String hostname, final String sessionId)
        throws IOException, SAXException {
        LOG.trace("Logging out.");
        final WebRequest req = new GetMethodWebRequest(PROTOCOL
            + hostname + LOGIN_URL);
        req.setParameter(AJAXServlet.PARAMETER_ACTION, "logout");
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setHeaderField("Context-Type", "");
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
    }

    /**
     * This method mades a login and returns the sessionId if the login is
     * successful.
     * @param conversation WebConversation.
     * @param hostname hostname of the server running the server.
     * @param login Login of the user.
     * @param password Password of the user.
     * @return the session identifier if the login is successful.
     * @throws JSONException if parsing of serialized json fails.
     * @throws SAXException if a SAX error occurs.
     * @throws IOException if the communication with the server fails.
     */
    public static String getSessionId(final WebConversation conversation,
        final String hostname, final String login, final String password)
        throws IOException, SAXException, JSONException {
        final JSONObject jslogin = login(conversation, hostname, login,
            password);
        final String sessionId = jslogin.getString("session");
        assertNotNull("Can't get sessionId", sessionId);
        assertTrue("Can't get sessionId", sessionId.length() > 0);
        return sessionId;
    }

    /**
     * This method mades a login and returns the sessionId if the login is
     * successful.
     * @param conversation WebConversation.
     * @param hostname hostname of the server running the server.
     * @param login Login of the user.
     * @param password Password of the user.
     * @return the session identifier if the login is successful.
     * @throws JSONException if parsing of serialized json fails.
     * @throws SAXException if a SAX error occurs.
     * @throws IOException if the communication with the server fails.
     */
    public static String[] getSessionIdWithUserId(
        final WebConversation conversation, final String hostname,
        final String login, final String password) throws IOException,
        SAXException, JSONException {
        final JSONObject jslogin = login(conversation, hostname, login,
            password);
        final String sessionId = jslogin.getString("session");
        assertNotNull("Can't get sessionId.", sessionId);
        assertTrue("Can't get sessionId.", sessionId.length() > 0);
        final String userId = jslogin.getString("id");
        assertNotNull("Can't get user identifier.", userId);
        assertTrue("Can't get user identifier.", userId.length() > 0);
        return new String[] { sessionId, userId };
    }
}
