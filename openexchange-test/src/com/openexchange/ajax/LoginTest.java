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

package com.openexchange.ajax;

import java.io.IOException;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.meterware.httpunit.WebConversation;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.session.LoginTools;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.ajax.session.actions.LogoutRequest;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.exception.OXException;

/**
 * This class contains the login test. It also contains static methods to made logins from other places.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginTest extends AbstractAJAXTest {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginTest.class);

    /**
     * URL of the login AJAX servlet.
     */
    private static final String LOGIN_URL = "/ajax/login";

    /**
     * Default constructor.
     * 
     * @param name Name of the test.
     */
    public LoginTest(final String name) {
        super(name);
    }

    /**
     * This method mades a logout.
     * 
     * @param conversation WebConversation.
     * @param hostname hostname of the server running the server.
     * @param sessionId Session identifier of the user.
     * @throws IOException if the communication with the server fails.
     * @throws JSONException
     * @throws OXException
     * @throws SAXException if a SAX error occurs.
     */
    public static void logout(final WebConversation conversation, final String hostname, final String sessionId) throws IOException, OXException, JSONException {

        LOG.trace("Logging out.");
        LogoutRequest request = new LogoutRequest();
        AJAXClient client = new AJAXClient(new AJAXSession(conversation, hostname, sessionId), false);
        client.setHostname(hostname);
        client.setProtocol(AJAXConfig.getProperty(Property.PROTOCOL));
        client.execute(request);
    }

    /**
     * This method mades a login and returns the sessionId if the login is successful.
     * 
     * @param conversation WebConversation.
     * @param hostname hostname of the server running the server.
     * @param login Login of the user.
     * @param password Password of the user.
     * @return the session identifier if the login is successful.
     * @throws JSONException if parsing of serialized json fails.
     * @throws SAXException if a SAX error occurs.
     * @throws IOException if the communication with the server fails.
     * @throws OXException
     * @deprecated use new AJAXClient request and response framework.
     */
    @Deprecated
    public static String getSessionId(final WebConversation conversation, final String hostname, final String login, final String password) throws IOException, JSONException, OXException {
        LoginRequest request = new LoginRequest(
            login,
            password,
            LoginTools.generateAuthId(),
            AJAXClient.class.getName(),
            AJAXClient.VERSION);
        // an empty string is passed to put temporary AJAXSession not in mustLogout mode. Logout is done with above logout() method.
        AJAXClient client = new AJAXClient(new AJAXSession(conversation, hostname, "no logout"), false);
        client.setHostname(hostname);
        client.setProtocol(AJAXConfig.getProperty(Property.PROTOCOL));
        LoginResponse response = client.execute(request);
        return response.getSessionId();
    }
}
