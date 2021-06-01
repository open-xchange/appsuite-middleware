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
import static org.junit.Assert.assertNotNull;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractTestEnvironment;
import com.openexchange.ajax.session.actions.HttpAuthRequest;
import com.openexchange.ajax.session.actions.HttpAuthResponse;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.configuration.AJAXConfig.Property;
import com.openexchange.test.common.test.pool.TestContext;
import com.openexchange.test.common.test.pool.TestContextPool;
import com.openexchange.test.common.test.pool.TestUser;

/**
 * Tests the HTTP authorization header on the login servlet.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class HttpAuthTest extends AbstractTestEnvironment {

    private String protocol;
    private String hostname;
    private String login;
    private String password;
    private TestContext testContext;

    @Before
    public void setUp() throws Exception {
        testContext = TestContextPool.acquireContext(this.getClass().getCanonicalName());

        protocol = AJAXConfig.getProperty(Property.PROTOCOL);
        hostname = AJAXConfig.getProperty(Property.HOSTNAME);
        TestUser testUser = testContext.acquireUser();
        login = testUser.getLogin();
        password = testUser.getPassword();
    }

    @After
    public void tearDown() throws OXException {
        TestContextPool.backContext(testContext);
    }

    @Test
    public void testAuthorizationRequired() throws Throwable {
        HttpUriRequest request = new HttpGet(protocol + "://" + hostname + HttpAuthRequest.HTTP_AUTH_URL);
        HttpResponse response = AJAXSession.newHttpClient().execute(request);
        assertEquals("HTTP auth URL does not respond with a required authorization.", HttpServletResponse.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
    }

    @Test
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
            String sessionId = location.substring(sessionStart + 8);
            session.setId(sessionId);
        } finally {
            myClient.logout();
        }
    }
}
