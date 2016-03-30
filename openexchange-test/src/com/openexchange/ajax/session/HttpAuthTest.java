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

import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.session.actions.HttpAuthRequest;
import com.openexchange.ajax.session.actions.HttpAuthResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;

/**
 * Tests the HTTP authorization header on the login servlet.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class HttpAuthTest extends TestCase {

    private String protocol;
    private String hostname;
    private String login;
    private String password;

    public HttpAuthTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        AJAXConfig.init();
        protocol = AJAXConfig.getProperty(Property.PROTOCOL);
        hostname = AJAXConfig.getProperty(Property.HOSTNAME);
        login = AJAXConfig.getProperty(Property.LOGIN) + "@" + AJAXConfig.getProperty(Property.CONTEXTNAME);
        password = AJAXConfig.getProperty(Property.PASSWORD);
    }

    @Override
    protected void tearDown() throws Exception {
        login = null;
        password = null;
        super.tearDown();
    }

    public void testAuthorizationRequired() throws Throwable {
        HttpClient client2 = AJAXSession.newHttpClient();
        HttpUriRequest request = new HttpGet(protocol + "://" + hostname + HttpAuthRequest.HTTP_AUTH_URL);
        HttpResponse response = client2.execute(request);
        assertEquals("HTTP auth URL does not respond with a required authorization.", HttpServletResponse.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
    }

    public void testRedirect() throws Throwable {
        final AJAXSession session = new AJAXSession();
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            session.getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
            // Create session.
            HttpAuthResponse response = myClient.execute(new HttpAuthRequest(login, password));
            String location = response.getLocation();
            assertNotNull("Location is missing in response.", location);
            int sessionStart = location.indexOf("session=");
            String sessionId = location.substring(sessionStart + 8, location.indexOf('&', sessionStart + 8));
            session.setId(sessionId);
        } finally {
            myClient.logout();
        }
    }
}
