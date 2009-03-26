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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
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
        final WebRequest req = new PostMethodWebRequest(PROTOCOL
            + hostname + LOGIN_URL);
        req.setParameter("action", "login");
        req.setParameter("name", login);
        req.setParameter("password", password);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (final JSONException e) {
            LOG.error("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(json.optString("error"), json.has("error"));
        assertTrue("Session ID is missing: " + body, json.has(
            Login.PARAMETER_SESSION));
        assertTrue("Random is missing: " + body, json.has(Login.PARAM_RANDOM));
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
}
