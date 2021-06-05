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
import com.openexchange.test.common.test.pool.TestUser;
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
import com.openexchange.testing.httpclient.modules.MailApi;

/**
 * {@link PermissionTest}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class PermissionTest extends AbstractAppPasswordTest {

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
            MailsResponse allMails = mailApi.getAllMails(inbox, "600", null, FALSE, FALSE, null, null, I(0), I(5), I(5), null);
            return allMails.getError() == null;
        } catch (OXException | IOException | JSONException e) {
            throw new ApiException(e.getMessage());
        }
    }

    private boolean tryDrive(ApiClient client) throws ApiException {
        try {
            InfostoreApi driveApi = new InfostoreApi(client);
            int folder = getClient().getValues().getPrivateInfostoreFolder();
            InfoItemsResponse resp = driveApi.getAllInfoItems(Integer.toString(folder), "1,2,3", "702", "asc", I(-9), I(201), I(10), Boolean.FALSE);
            return resp.getError() == null;
        } catch (OXException | IOException | JSONException e) {
            throw new ApiException(e.getMessage());
        }
    }

    private boolean tryContacts(ApiClient client) throws ApiException {
        try {
            ContactsApi contactApi = new ContactsApi(client);
            int folder = getClient().getValues().getPrivateContactFolder();
            ContactsResponse resp = contactApi.getAllContacts(Integer.toString(folder), "1,20,101,607", "607", "asc", null);
            return resp.getError() == null;
        } catch (OXException | IOException | JSONException e) {
            throw new ApiException(e.getMessage());
        }
    }

    private boolean tryCalendar(ApiClient client) throws ApiException {
        ChronosApi calApi = new ChronosApi(client);
        EventsResponse resp = calApi.getAllEvents(
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

        List<AppPasswordApplication> apps = getApps();
        assertThat(I(apps.size()), greaterThan(I(1)));
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
            ApiClient apiClient = test.generateApiClient();
            // Test it
            testApis(type, apiClient);
            // Logout
            apiClient.logout();

        }
        getApiClient().login(origLogin, origPassword);
        resetApiClient();

    }

}
