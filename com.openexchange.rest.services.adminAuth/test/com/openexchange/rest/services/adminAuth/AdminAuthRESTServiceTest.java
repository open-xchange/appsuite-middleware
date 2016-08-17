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
import com.openexchange.rest.services.adminAuth.AdminAuthRESTService;
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
    public void setup() throws OXException, JSONException {
        services = new MockingServiceLookup();
        service = new AdminAuthRESTService(services);
        authenticator = services.mock(Authenticator.class);

        initialize();
    }

    private void initialize() throws OXException, JSONException {
        Credentials falseCreds = new Credentials("iAmNotTheMaster", "tr0ll");
        AdminAuthRESTService spyService = spy(new AdminAuthRESTService(services));
        doReturn(falseCreds).when(spyService).createCredentials("iAmNotTheMaster", "tr0ll");

        doThrow(new OXException(9999, "Authentication Failed")).when(authenticator).doAuthentication(falseCreds);
        doThrow(new OXException(9999, "Authentication Failed")).when(authenticator).doAuthentication(falseCreds, ctx);
    }

    @Test
    public void testMissingBody() throws JSONException {
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
