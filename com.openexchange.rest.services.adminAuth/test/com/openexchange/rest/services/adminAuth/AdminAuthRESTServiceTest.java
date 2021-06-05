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

package com.openexchange.rest.services.adminAuth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.auth.Authenticator;
import com.openexchange.auth.Credentials;
import com.openexchange.exception.OXException;
import com.openexchange.server.MockingServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AdminAuthRESTServiceTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AdminAuthRESTServiceTest {

    private MockingServiceLookup services;
    private Authenticator authenticator;
    private AdminAuthRESTService service;
    private final int ctx = 314;

    @Before
    public void setup() throws OXException {
        services = new MockingServiceLookup();
        service = new AdminAuthRESTService(services);
        authenticator = services.mock(Authenticator.class);

        initialize();
    }

    private void initialize() throws OXException {
        Credentials falseCreds = new Credentials("iAmNotTheMaster", "tr0ll");
        AdminAuthRESTService spyService = spy(new AdminAuthRESTService(services));
        doReturn(falseCreds).when(spyService).createCredentials("iAmNotTheMaster", "tr0ll");

        doThrow(new OXException(9999, "Authentication Failed")).when(authenticator).doAuthentication(falseCreds);
        doThrow(new OXException(9999, "Authentication Failed")).when(authenticator).doAuthentication(falseCreds, ctx);
    }

    @Test
    public void testMissingBody() {
        JSONObject body = new JSONObject();
        try {
            service.doAdminAuth(body);
            fail("Should have thrown an exception");
        } catch (OXException e) {
            assertEquals(e.getExceptionCode(), AjaxExceptionCodes.MISSING_REQUEST_BODY);
            assertEquals(e.getCode(), AjaxExceptionCodes.MISSING_REQUEST_BODY.getNumber());
        }
    }

    @Test
    public void testMissingFields() throws JSONException {
        JSONObject body = new JSONObject();
        body.put("login", "foobar");
        try {
            service.doAdminAuth(body);
            fail("Should have thrown an exception");
        } catch (OXException e) {
            assertEquals(e.getExceptionCode(), AjaxExceptionCodes.MISSING_FIELD);
            assertEquals(e.getCode(), AjaxExceptionCodes.MISSING_FIELD.getNumber());
        }

        body = new JSONObject();
        body.put("password", "foobar");
        try {
            service.doAdminAuth(body);
            fail("Should have thrown an exception");
        } catch (OXException e) {
            assertEquals(e.getExceptionCode(), AjaxExceptionCodes.MISSING_FIELD);
            assertEquals(e.getCode(), AjaxExceptionCodes.MISSING_FIELD.getNumber());
        }
    }

    @Test
    public void testMasterAdminAuthentication() throws JSONException, OXException {
        JSONObject body = new JSONObject();
        body.put("login", "masterOfDisaster");
        body.put("password", "super53cr37");
        JSONObject response = service.doAdminAuth(body);
        assertEquals("Unexpected length for response object", 1, response.length());
        assertTrue("Authentication should have succeeeded", response.getBoolean("result"));
    }

    @Test
    public void testContextAdminAuthentication() throws JSONException, OXException {
        JSONObject body = new JSONObject();
        body.put("login", "masterOfDisaster");
        body.put("password", "super53cr37");
        body.put("context", ctx);
        JSONObject response = service.doAdminAuth(body);
        assertEquals("Unexpected length for response object", 1, response.length());
        assertTrue("Authentication should have succeeeded", response.getBoolean("result"));
    }

    @Test
    public void testFailMasterAdminAuthentication() throws JSONException, OXException {
        JSONObject body = new JSONObject();
        body.put("login", "iAmNotTheMaster");
        body.put("password", "tr0ll");
        JSONObject response = service.doAdminAuth(body);
        assertEquals("Unexpected length for response object", 1, response.length());
        assertFalse("Authentication should have failed", response.getBoolean("result"));
    }

    @Test
    public void testFailContextAdminAuthentication() throws OXException, JSONException {
        JSONObject body = new JSONObject();
        body.put("login", "iAmNotTheMaster");
        body.put("password", "tr0ll");
        body.put("context", ctx);
        JSONObject response = service.doAdminAuth(body);
        assertEquals("Unexpected length for response object", 1, response.length());
        assertFalse("Authentication should have failed", response.getBoolean("result"));
    }
}
