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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.appPassword;

import static com.openexchange.java.Autoboxing.I;
import static java.lang.Boolean.FALSE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.exception.OXException;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.AppPasswordApplication;
import com.openexchange.testing.httpclient.models.AppPasswordRegistrationResponseData;
import com.openexchange.testing.httpclient.models.ContactsResponse;
import com.openexchange.testing.httpclient.models.EventsResponse;
import com.openexchange.testing.httpclient.models.InfoItemsResponse;
import com.openexchange.testing.httpclient.models.MailsResponse;
import com.openexchange.testing.httpclient.modules.ChronosApi;
import com.openexchange.testing.httpclient.modules.ContactsApi;
import com.openexchange.testing.httpclient.modules.InfostoreApi;
import com.openexchange.testing.httpclient.modules.LoginApi;
import com.openexchange.testing.httpclient.modules.MailApi;

/**
 * {@link PermissionTest}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class PermissionTest extends AbstractAppPasswordTest {

    private LoginApi loginApi;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        loginApi = new LoginApi(getApiClient());
    }

    @Override
    public void tearDown() throws Exception {
        try {
            removeAll();
        } finally {
            super.tearDown();
        }
    }

    /**
     * Simple test to see if we can get a list of emails
     * tryMail
     *
     * @param client
     * @return false if denied
     * @throws ApiException
     */
    private boolean tryMail(ApiClient client) throws ApiException {
        try {
            MailApi mailApi = new MailApi(client);
            String inbox = getClient().getValues().getInboxFolder();
            MailsResponse allMails = mailApi.getAllMails(getSessionId(), inbox, "600", null, FALSE, FALSE, null, null, I(0), I(5), I(5), null);
            return allMails.getError() == null;
        } catch (OXException | IOException | JSONException e) {
            throw new ApiException(e.getMessage());
        }
    }

    private boolean tryDrive(ApiClient client) throws ApiException {
        try {
            InfostoreApi driveApi = new InfostoreApi(client);
            int folder = getClient().getValues().getPrivateInfostoreFolder();
            InfoItemsResponse resp = driveApi.getAllInfoItems(getSessionId(), Integer.toString(folder), "1,2,3", "702", "asc", -9, 201, 10, false);
            return resp.getError() == null;
        } catch (OXException | IOException | JSONException e) {
            throw new ApiException(e.getMessage());
        }
    }

    private boolean tryContacts(ApiClient client) throws ApiException {
        try {
            ContactsApi contactApi = new ContactsApi(client);
            int folder = getClient().getValues().getPrivateContactFolder();
            ContactsResponse resp = contactApi.getAllContacts(getSessionId(), Integer.toString(folder), "1,20,101,607", "607", "asc", false, null);
            return resp.getError() == null;
        } catch (OXException | IOException | JSONException e) {
            throw new ApiException(e.getMessage());
        }
    }

    private boolean tryCalendar(ApiClient client) throws ApiException {
        ChronosApi calApi = new ChronosApi(client);
        EventsResponse resp = calApi.getAllEvents(getSessionId(),
            DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime() - 10000).getValue(),
            DateTimeUtil.getZuluDateTime(new Date(System.currentTimeMillis()).getTime()).getValue(),
            null, null, null, null, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
        return resp.getError() == null;
    }

    private void testApis(String type, ApiClient client) throws ApiException {
        switch (type.trim()) {
            case "mail":
                assertTrue(tryMail(client));
                assertTrue(tryContacts(client));
                assertFalse(tryDrive(client));
                assertFalse(tryCalendar(client));
                break;
            case "drive":
                assertTrue(tryDrive(client));
                assertFalse(tryMail(client));
                assertFalse(tryContacts(client));
                assertFalse(tryCalendar(client));
                break;
            case "contacts":
                assertTrue(tryContacts(client));
                assertFalse(tryMail(client));
                assertFalse(tryDrive(client));
                assertFalse(tryCalendar(client));
                break;
            case "calendar":
                assertTrue(tryCalendar(client));
                assertFalse(tryMail(client));
                assertFalse(tryContacts(client));
                assertFalse(tryDrive(client));
                break;
        }
    }

    @Test
    public void testPermissions() throws ApiException {

        List<AppPasswordApplication> apps = getApps(this.getSessionId());
        assertThat(apps.size(), greaterThan(1));
        String origLogin = testUser.getLogin();
        String origPassword = testUser.getPassword();
        Map<String, AppPasswordRegistrationResponseData> logins = new HashMap<String, AppPasswordRegistrationResponseData>();
        for (AppPasswordApplication app : apps) {
            AppPasswordRegistrationResponseData loginData = addPassword(app.getName());
            logins.put(app.getName(), loginData);
        }

        // Logout, and try logging in with new app Spec password
        getApiClient().logout();

        for (Map.Entry<String, AppPasswordRegistrationResponseData> entry : logins.entrySet()) {
            AppPasswordRegistrationResponseData loginData = entry.getValue();
            String type = entry.getKey();
            // Login with app password
            String login = loginData.getLogin();
            String username = login.contains("@") ? login.substring(0, login.indexOf("@")) : login;
            String domain = login.contains("@") ? login.substring(login.indexOf("@") + 1) : testUser.getContext();
            TestUser test = new TestUser(username, domain, loginData.getPassword());
            try {
                apiClient = generateApiClient("test-client", test);
                // Test it
                testApis(type, apiClient);
                // Logout
                apiClient.logout();
            } catch (OXException e) {
                throw new ApiException(e.getMessage());
            }

        }
        getApiClient().login(origLogin, origPassword);
        resetApiClient();

    }

}
