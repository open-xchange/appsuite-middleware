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

package com.openexchange.ajax.chronos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.testing.httpclient.models.CalendarAccountId;
import com.openexchange.testing.httpclient.models.CalendarAccountResponse;
import com.openexchange.testing.httpclient.models.CalendarAccountsResponse;

/**
 * {@link CalendarAccountTest}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class CalendarAccountTest extends AbstractChronosTest {

    private final String TEST_PROVIDER_ID = "testProvider";

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testCreateAndLoadCalendarAccount() throws Exception {
        CalendarAccountResponse response = calendarAccountManager.createCalendarAccount(TEST_PROVIDER_ID, calendarAccountManager.createCalendarAccountTestConfiguration(false));

        response = calendarAccountManager.loadCalendarAccount(calendarAccountManager.createCalendarAccountId(response.getData().getId(), response.getData().getTimestamp()));
        assertEquals("The providerId of the calendar account is invalid!", TEST_PROVIDER_ID, response.getData().getProvider());

        //      Test for default Account
        response = calendarAccountManager.loadCalendarAccount(calendarAccountManager.createCalendarAccountId(calendarAccountManager.DEFAULT_ACCOUNT_ID, null));
        assertEquals("The id of the default calendar account is invalid!", calendarAccountManager.DEFAULT_ACCOUNT_ID, response.getData().getId());
    }

    @Test
    public void testLoadAllCalendarAccountsForOneProvider() throws Exception {
        calendarAccountManager.createCalendarAccount(TEST_PROVIDER_ID, calendarAccountManager.createCalendarAccountTestConfiguration(false));
        calendarAccountManager.createCalendarAccount(TEST_PROVIDER_ID, calendarAccountManager.createCalendarAccountTestConfiguration(false));

        //TODO: create loadAllAccount method in manager
        CalendarAccountsResponse resp = defaultUserApi.getChronosApi().getAllAccounts(defaultUserApi.getSession(), TEST_PROVIDER_ID);
        assertEquals("Invalid data size! Data should contain two entries!", 2, resp.getData().size());

        //Test for default account
        resp = defaultUserApi.getChronosApi().getAllAccounts(defaultUserApi.getSession(), calendarAccountManager.DEFAULT_ACCOUNT_PROVIDER_ID);
        assertNull(resp.getError(), resp.getError());
        assertNotNull(resp.getData());
        assertEquals("Invalid data size! Data should only contain default account!", 1, resp.getData().size());
        assertEquals("The id of the default calendar account is invalid!", calendarAccountManager.DEFAULT_ACCOUNT_ID, resp.getData().get(0).getId());
    }

    @Test
    public void testUpdateCalendarAccount() throws Exception {
        CalendarAccountResponse response = calendarAccountManager.createCalendarAccount(TEST_PROVIDER_ID, calendarAccountManager.createCalendarAccountTestConfiguration(false));

        response = calendarAccountManager.updateCalendarAccount(calendarAccountManager.createCalendarAccountId(response.getData().getId(), response.getData().getTimestamp()), calendarAccountManager.createCalendarAccountTestConfiguration(true));
        assertNull(response.getError(), response.getError());

        response = calendarAccountManager.loadCalendarAccount(calendarAccountManager.createCalendarAccountId(response.getData().getId(), response.getData().getTimestamp()));

        //Test for default account
        response = calendarAccountManager.updateCalendarAccount(calendarAccountManager.createCalendarAccountId(calendarAccountManager.DEFAULT_ACCOUNT_ID, System.currentTimeMillis()), calendarAccountManager.createCalendarAccountTestConfiguration(true));
        assertNotNull(response.getError(), response.getError());
    }

    @Test
    public void testDeleteCalendarAccount() throws Exception {
        CalendarAccountResponse response = calendarAccountManager.createCalendarAccount(TEST_PROVIDER_ID, calendarAccountManager.createCalendarAccountTestConfiguration(false));

        String id = response.getData().getId();

        response = calendarAccountManager.loadCalendarAccount(calendarAccountManager.createCalendarAccountId(response.getData().getId(), response.getData().getTimestamp()));

        assertEquals(id, response.getData().getId());
        assertEquals(TEST_PROVIDER_ID, response.getData().getProvider());

        List<CalendarAccountId> idsToDelete = new ArrayList<>(2);
        idsToDelete.add(calendarAccountManager.createCalendarAccountId(response.getData().getId(), response.getData().getTimestamp()));

        response = calendarAccountManager.createCalendarAccount(TEST_PROVIDER_ID, calendarAccountManager.createCalendarAccountTestConfiguration(false));

        id = response.getData().getId();

        response = calendarAccountManager.loadCalendarAccount(calendarAccountManager.createCalendarAccountId(response.getData().getId(), response.getData().getTimestamp()));
        assertEquals(id, response.getData().getId());
        assertEquals(TEST_PROVIDER_ID, response.getData().getProvider());
        idsToDelete.add(calendarAccountManager.createCalendarAccountId(response.getData().getId(), response.getData().getTimestamp()));

        calendarAccountManager.deleteCalendarAccount(idsToDelete);

        //Try test to delete default account
        idsToDelete.clear();
        idsToDelete.add(calendarAccountManager.createCalendarAccountId(calendarAccountManager.DEFAULT_ACCOUNT_ID, System.currentTimeMillis()));
        calendarAccountManager.deleteCalendarAccount(idsToDelete);
    }

}
