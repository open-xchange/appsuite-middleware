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

package com.openexchange.rest.passwordchange.history;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import com.openexchange.admin.rest.passwordchange.history.api.PasswordChangeHistoryREST;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateRequest;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateResponse;
import com.openexchange.java.Charsets;
import com.openexchange.rest.AbstractRestTest;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.restclient.invoker.ApiClient;
import com.openexchange.testing.restclient.models.PasswordChangeHistoryEntry;
import com.openexchange.testing.restclient.modules.AdminApi;

/**
 * {@link PasswordChangeHistoryTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeHistoryTest extends AbstractRestTest {

    protected AdminApi pwdhapi;
    protected int contextID;
    protected int userID;
    protected int limit = 1;
    protected long send;

    protected ApiClient pwdRestClient;

    protected final static String CLIENT_ID = "com.openexchange.ajax.framework.AJAXClient";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Override the rest client with context administrators credentials
        pwdRestClient = new ApiClient();
        pwdRestClient.setBasePath(getBasePath());
        TestUser pwdRestUser = testContext.getAdmin();
        pwdRestClient.setUsername(pwdRestUser.getUser());
        pwdRestClient.setPassword(pwdRestUser.getPassword());
        String authorizationHeaderValue = "Basic " + Base64.encodeBase64String((pwdRestUser.getUser() + ":" + pwdRestUser.getPassword()).getBytes(Charsets.UTF_8));
        pwdRestClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);

        // API to operate on
        pwdhapi = new AdminApi(pwdRestClient);

        // Do a password change
        PasswordChangeUpdateRequest request = new PasswordChangeUpdateRequest(testUser.getPassword(), testUser.getPassword(), true);
        send = System.currentTimeMillis();
        PasswordChangeUpdateResponse response = getAjaxClient().execute(request);
        assertFalse("Errors in response!", response.hasError());
        assertFalse("Warnings in response!", response.hasWarnings());

        // Get context and user ID
        contextID = getAjaxClient().getValues().getContextId();
        userID = getAjaxClient().getValues().getUserId();
    }
    
    @Override
    protected Application configure() {
        return new ResourceConfig(PasswordChangeHistoryREST.class);
    }

    @Test
    public void testLimit() throws Exception {
        List<PasswordChangeHistoryEntry> retval = pwdhapi.passwdChanges(I(contextID), I(userID), I(limit), null);
        assertEquals("More than one element! Limitation did not work..", 1, retval.size());
    }
    
    @Test
    public void testTime() throws Exception {
        List<PasswordChangeHistoryEntry> retval = pwdhapi.passwdChanges(I(contextID), I(userID), I(0), "date");
        for (PasswordChangeHistoryEntry entry : retval) {
            if ((send - entry.getDate().longValue()) < 1000) {
                // Check other criteria. This may fail if a password change made by another test was within the last second
                assertEquals("Was not changed by this test!", CLIENT_ID, entry.getClientId());
                assertTrue("IP must be set.", null != entry.getClientAddress());
                return;
            }
        }
        fail("Did not find any timestamp near the transmitting timestamp");
    }
}
