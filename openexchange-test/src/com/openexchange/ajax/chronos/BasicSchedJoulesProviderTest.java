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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.ajax.chronos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.java.Strings;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CalendarAccountData;
import com.openexchange.testing.httpclient.models.CalendarAccountProviderData;
import com.openexchange.testing.httpclient.models.CalendarAccountResponse;

/**
 * {@link BasicSchedJoulesProviderTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BasicSchedJoulesProviderTest extends AbstractChronosTest {

    /**
     * _90734
     */
    private static final int CALENDAR_ONE = 90734;
    /**
     * _24428282
     */
    private static final int CALENDAR_TWO = 24428282;
    private static final String PROVIDER_ID = "schedjoules";

    /**
     * Tests the presence of the 'schedjoules' provider
     */
    @Test
    public void testSchedJoulesProvider() throws ApiException {
        List<CalendarAccountProviderData> list = calendarAccountManager.listAvailableProviders();
        CalendarAccountProviderData schedjoulesProvider = null;
        for (CalendarAccountProviderData provider : list) {
            if (PROVIDER_ID.equals(provider.getId())) {
                schedjoulesProvider = provider;
                break;
            }
        }

        assertNotNull("The 'SchedJoules' provider is not present", schedjoulesProvider);
    }

    /**
     * Tests the initial creation of a calendar account with
     * a SchedJoules subscription to a non existing calendar
     */
    @Test
    public void testCreateAccountWithNonExistingSchedJoulesCalendar() throws ApiException, JSONException {
        try {
            calendarAccountManager.createCalendarAccount(PROVIDER_ID, createAccountConfiguration(31145, null, -1).toString(), true);
            fail("No exception was thrown");
        } catch (ChronosApiException e) {
            assertNotNull(e);
            assertEquals("SCHEDJOULES-0007", e.getErrorCode());
        }
    }

    /**
     * Tests the initial creation of a calendar account with
     * a SchedJoules subscription to an invalid calendar, i.e. a 'page' item_class
     */
    @Test
    public void testCreateAccountWithInvalidSchedJoulesCalendar() throws ApiException, JSONException {
        try {
            calendarAccountManager.createCalendarAccount(PROVIDER_ID, createAccountConfiguration(115673, null, -1).toString(), true);
            fail("No exception was thrown");
        } catch (ChronosApiException e) {
            assertNotNull(e);
            assertEquals("SCHEDJOULES-0001", e.getErrorCode());
        }
    }

    /**
     * Tests the initial creation of a calendar account with
     * a SchedJoules subscription to a random calendar.
     */
    @Test
    public void testCreateAccountWithSchedJoulesSubscription() throws ApiException, JSONException, ChronosApiException {
        CalendarAccountData accountData = calendarAccountManager.createCalendarAccount(PROVIDER_ID, createAccountConfiguration(CALENDAR_ONE, null, -1).toString(), false);
        assertAccountConfiguration(accountData.getConfiguration(), 1);
    }

    /**
     * Tests the update of a SchedJoules calendar account by
     * adding one additional subscription
     */
    @Test
    public void testUpdateAccountAddOneSubscription() throws ApiException, JSONException {
        JSONObject initialConfiguration = createAccountConfiguration(CALENDAR_ONE, null, -1);
        CalendarAccountResponse calendarAccount = calendarAccountManager.createCalendarAccount(PROVIDER_ID, initialConfiguration.toString());
        CalendarAccountData accountData = calendarAccount.getData();

        JSONObject accountConfig = assertAccountConfiguration(accountData.getConfiguration(), 1);
        accountConfig.getJSONArray("folders").put(createFolder(CALENDAR_TWO, null, -1));

        JSONObject configuration = new JSONObject();
        configuration.put("configuration", accountConfig);

        // Add the second subscription
        CalendarAccountData updatedAccountData = calendarAccountManager.updateCalendarAccount(accountData.getId(), accountData.getTimestamp(), configuration.toString());
        assertAccountConfiguration(updatedAccountData.getConfiguration(), 2);
    }

    /**
     * Tests the update of a SchedJoules calendar account by
     * removing one existing subscription
     */
    @Test
    public void testUpdateAccountRemoveOneSubscription() throws JSONException, ApiException {
        JSONObject initialConfiguration = createAccountConfiguration(CALENDAR_ONE, null, -1);
        CalendarAccountResponse calendarAccount = calendarAccountManager.createCalendarAccount(PROVIDER_ID, initialConfiguration.toString());
        CalendarAccountData accountData = calendarAccount.getData();

        JSONObject accountConfig = assertAccountConfiguration(accountData.getConfiguration(), 1);
        accountConfig.getJSONArray("folders").put(createFolder(CALENDAR_TWO, null, -1));

        JSONObject configuration = new JSONObject();
        configuration.put("configuration", accountConfig);

        // Add the second subscription
        CalendarAccountData updatedAccountData = calendarAccountManager.updateCalendarAccount(accountData.getId(), accountData.getTimestamp(), configuration.toString());
        accountConfig = assertAccountConfiguration(updatedAccountData.getConfiguration(), 2);

        accountConfig.getJSONArray("folders").remove(0);

        configuration = new JSONObject();
        configuration.put("configuration", accountConfig);

        // Remove the first subscription
        updatedAccountData = calendarAccountManager.updateCalendarAccount(accountData.getId(), updatedAccountData.getTimestamp(), configuration.toString());
        assertAccountConfiguration(updatedAccountData.getConfiguration(), 1);
    }

    /**
     * Tests the update of a SchedJoules calendar account by
     * adding one additional subscription and deleting an existing one
     */
    @Test
    public void testUpdateAccountAddOneRemoveOneSubscription() {
        fail("Not implemented yet");
    }

    /**
     * Tests the update of a SchedJoules calendar account by
     * renaming an existing subscription
     */
    @Test
    public void testUpdateAccountRenameSubscription() {
        fail("Not implemented yet");
    }

    /**
     * Tests the deletion of a SchedJoules calendar account with subscriptions
     */
    @Test
    public void testDeleteAccountWithSubscriptions() {
        fail("Not implemented yet");
    }

    ////////////////////////////////// HELPERS ///////////////////////////////////

    /**
     * Asserts the specified configuration.
     * 
     * @param data The configuration data
     * @param expectedAmountOfFolders The amount of expected folders in the configuration
     * @return the asserted configuration
     */
    private JSONObject assertAccountConfiguration(Object data, int expectedAmountOfFolders) throws JSONException {
        assertNotNull("The account data is 'null'", data);
        assertTrue("The account data is of type: " + data.getClass(), data instanceof Map);
        JSONObject config = (JSONObject) JSONCoercion.coerceToJSON(data);

        JSONArray folders = config.getJSONArray("folders");
        assertNotNull("The folders array is 'null'", folders);
        assertFalse("The folders array is empty", folders.isEmpty());
        assertEquals("The amount of expected folders does not match", expectedAmountOfFolders, folders.length());
        for (int index = 0; index < folders.length(); index++) {
            assertFolder(folders.optJSONObject(index));
        }

        return config;
    }

    /**
     * Asserts the specified folder object
     * 
     * @param folder the folder object to assert
     */
    private void assertFolder(JSONObject folder) {
        assertNotNull("The folder's metadata is 'null'", folder);
        assertFalse("No folder metadata present", folder.isEmpty());
        assertTrue("The 'itemId' is missing", folder.hasAndNotNull("itemId"));
        assertTrue("The 'name' is missing", folder.hasAndNotNull("name"));
    }

    /**
     * Creates a calendar configuration object with the specified itemId
     * 
     * @param itemId The item identifier
     * @param locale The optional locale
     * @param refreshInterval The optional refresh interval
     * @return The configuration
     * @throws JSONException if a JSON error is occurred
     */
    private JSONObject createAccountConfiguration(int itemId, String locale, int refreshInterval) throws JSONException {
        JSONArray folders = new JSONArray();
        folders.put(createFolder(itemId, locale, refreshInterval));

        JSONObject foldersJ = new JSONObject();
        foldersJ.put("folders", folders);

        JSONObject config = new JSONObject();
        config.put("configuration", foldersJ);

        return config;
    }

    /**
     * Creates a calendar folder configuration object with the specified itemId, optional locale and
     * optional refresh interval.
     * 
     * @param itemId The item identifier
     * @param locale The optional locale
     * @param refreshInterval The optional refresh interval
     * @return The folder configuration
     * @throws JSONException if a JSON error is occurred
     */
    private JSONObject createFolder(int itemId, String locale, int refreshInterval) throws JSONException {
        JSONObject folder = new JSONObject();
        folder.put("itemId", itemId);
        if (refreshInterval > 0) {
            folder.put("refreshInterval", refreshInterval);
        }
        if (!Strings.isEmpty(locale)) {
            folder.put("locale", locale);
        }
        return folder;
    }
}
