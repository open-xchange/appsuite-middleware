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
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.openexchange.ajax.chronos.factory.AccountConfigurationFactory;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;

/**
 * {@link BasicSchedJoulesProviderTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@Ignore //FIXME class only made compileable ... 
public class BasicSchedJoulesProviderTest extends AbstractChronosTest {

    private static final int ROOT_PAGE = 115673;
    private static final int NON_EXISTING_CALENDAR = 31145;
    private static final int CALENDAR_ONE = 90734;
    private static final int CALENDAR_TWO = 24428282;
    private static final int CALENDAR_THREE = 24428313;
    private static final String PROVIDER_ID = "schedjoules";

    private String folderName = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderName = "testfolder_" + System.nanoTime();
    }

    /**
     * Tests the initial creation of a calendar account with
     * a SchedJoules subscription to a non existing calendar
     * 
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @Test
    public void testCreateAccountWithNonExistingSchedJoulesCalendar() throws ApiException, JSONException, JsonParseException, JsonMappingException, IOException {
        try {
            AccountConfiguration ac = AccountConfigurationFactory.createSubscriptionConfiguration(NON_EXISTING_CALENDAR);
            calendarAccountManager.createCalendarAccount(PROVIDER_ID, folderName, ac.getConfiguration(), true);
            fail("No exception was thrown");
        } catch (ChronosApiException e) {
            assertNotNull(e);
            assertEquals("SCHEDJOULES-0007", e.getErrorCode());
        }
    }

    /**
     * Tests the initial creation of a calendar account with
     * a SchedJoules subscription to an invalid calendar, i.e. a 'page' item_class
     * 
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @Test
    public void testCreateAccountWithInvalidSchedJoulesCalendar() throws ApiException, JSONException, JsonParseException, JsonMappingException, IOException {
        try {
            AccountConfiguration ac = AccountConfigurationFactory.createSubscriptionConfiguration(ROOT_PAGE);
            calendarAccountManager.createCalendarAccount(PROVIDER_ID, folderName, ac.getConfiguration(), true);
            fail("No exception was thrown");
        } catch (ChronosApiException e) {
            assertNotNull(e);
            assertEquals("SCHEDJOULES-0001", e.getErrorCode());
        }
    }

    /**
     * Tests the initial creation of a calendar account with
     * a SchedJoules subscription to a random calendar.
     * 
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @Test
    public void testCreateAccountWithSchedJoulesSubscription() throws ApiException, JSONException, ChronosApiException, JsonParseException, JsonMappingException, IOException {
        AccountConfiguration ac = AccountConfigurationFactory.createSubscriptionConfiguration(CALENDAR_ONE);
        FolderUpdateResponse accountData = calendarAccountManager.createCalendarAccount(PROVIDER_ID, folderName, ac.getConfiguration(), false);
        assertAccountConfiguration(accountData.getData(), 1);
    }

    /**
     * Tests the update of a SchedJoules calendar account by
     * adding one additional subscription
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     * @throws ChronosApiException 
     */
    @Test
    public void testUpdateAccountAddOneSubscription() throws ApiException, JSONException, JsonParseException, JsonMappingException, IOException, ChronosApiException {
        AccountConfiguration ac = AccountConfigurationFactory.createSubscriptionConfiguration(CALENDAR_ONE);
        FolderUpdateResponse calendarAccount = calendarAccountManager.createCalendarAccount(PROVIDER_ID, folderName, ac.getConfiguration(), false);

        //TODO load account and verify
        
        assertAccountConfiguration(calendarAccount.getData(), 1);
        ac.addFolderConfiguration(CALENDAR_TWO);

        // Add the second subscription
        FolderUpdateResponse updatedAccountData = calendarAccountManager.updateCalendarAccount(calendarAccount.getData(), folderName, System.nanoTime(), ac.getConfiguration());
        //TODO load account and verify
        assertAccountConfiguration(updatedAccountData.getData(), 2);
    }

    /**
     * Tests the update of a SchedJoules calendar account by
     * removing one existing subscription
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     * @throws ChronosApiException 
     */
    @Test
    public void testUpdateAccountRemoveOneSubscription() throws JSONException, ApiException, JsonParseException, JsonMappingException, IOException, ChronosApiException {
        AccountConfiguration ac = AccountConfigurationFactory.createSubscriptionConfiguration(CALENDAR_ONE);
        FolderUpdateResponse calendarAccount = calendarAccountManager.createCalendarAccount(PROVIDER_ID, folderName, ac.getConfiguration(), false);

        //TODO load account and verify
//        assertAccountConfiguration(accountData.getConfiguration(), 1);
        ac.addFolderConfiguration(CALENDAR_TWO);

        // Add the second subscription
        FolderUpdateResponse updatedAccountData = calendarAccountManager.updateCalendarAccount(calendarAccount.getData(), folderName, System.nanoTime(), ac.getConfiguration());
        //TODO load account and verify
        assertAccountConfiguration(updatedAccountData.getData(), 2);

        ac.removeFolderConfiguration(CALENDAR_ONE);

        // Remove the first subscription
        updatedAccountData = calendarAccountManager.updateCalendarAccount(updatedAccountData.getData(), folderName, System.nanoTime(), ac.getConfiguration());
        //TODO load account and verify
        assertAccountConfiguration(updatedAccountData, 1);
    }

    /**
     * Tests the update of a SchedJoules calendar account by
     * adding one additional subscription and deleting an existing one
     * @throws IOException 
     * @throws ChronosApiException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    @Test
    public void testUpdateAccountAddOneRemoveOneSubscription() throws JSONException, ApiException, JsonParseException, JsonMappingException, ChronosApiException, IOException {
        AccountConfiguration ac = AccountConfigurationFactory.createSubscriptionConfiguration(CALENDAR_ONE);
        ac.addFolderConfiguration(CALENDAR_TWO);
        FolderUpdateResponse calendarAccount = calendarAccountManager.createCalendarAccount(PROVIDER_ID, folderName, ac.getConfiguration(), false);

        //TODO load account and verify

        assertAccountConfiguration(calendarAccount.getData(), 2);
        ac.removeFolderConfiguration(CALENDAR_TWO);
        ac.addFolderConfiguration(CALENDAR_THREE);

        // Update
        FolderUpdateResponse updatedAccountData = calendarAccountManager.updateCalendarAccount(calendarAccount.getData(), folderName, System.nanoTime(), ac.getConfiguration());
        //TODO load account and verify

        assertAccountConfiguration(updatedAccountData.getData(), 2);
    }

    /**
     * Tests the update of a SchedJoules calendar account by
     * renaming an existing subscription
     * 
     * @throws JSONException
     * @throws ApiException
     * @throws IOException 
     * @throws ChronosApiException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    @Test
    public void testUpdateAccountRenameSubscription() throws JSONException, ApiException, JsonParseException, JsonMappingException, ChronosApiException, IOException {
        AccountConfiguration ac = AccountConfigurationFactory.createSubscriptionConfiguration(CALENDAR_ONE);
        FolderUpdateResponse calendarAccount = calendarAccountManager.createCalendarAccount(PROVIDER_ID, folderName, ac.getConfiguration(), false);

        //TODO load account and verify
        assertAccountConfiguration(calendarAccount.getData(), 1);
        ac.renameFolder(CALENDAR_ONE, "testUpdateAccountRenameSubscription");

        FolderUpdateResponse updatedAccountData = calendarAccountManager.updateCalendarAccount(calendarAccount.getData(), folderName, System.nanoTime(), ac.getConfiguration());
        //TODO load account and verify
        JSONObject config = assertAccountConfiguration(updatedAccountData.getData(), 1);
        JSONArray folders = config.getJSONArray("folders");
        JSONObject folder = folders.getJSONObject(0);
        assertEquals("The subscription's name was not changed", "testUpdateAccountRenameSubscription", folder.getString("name"));
    }

    /**
     * Tests the deletion of a SchedJoules calendar account with subscriptions
     * @throws IOException 
     * @throws ChronosApiException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    @Test
    public void testDeleteAccountWithSubscriptions() throws JSONException, ApiException, JsonParseException, JsonMappingException, ChronosApiException, IOException {
        AccountConfiguration ac = AccountConfigurationFactory.createSubscriptionConfiguration(CALENDAR_ONE);
        ac.addFolderConfiguration(CALENDAR_TWO);
        FolderUpdateResponse calendarAccount = calendarAccountManager.createCalendarAccount(PROVIDER_ID, folderName, ac.getConfiguration(), false);
        //TODO load account and verify
        assertAccountConfiguration(calendarAccount.getData(), 2);

        calendarAccountManager.deleteCalendarAccount(Collections.singletonList(calendarAccount.getData()));
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
}
