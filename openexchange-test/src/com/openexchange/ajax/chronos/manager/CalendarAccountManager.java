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

package com.openexchange.ajax.chronos.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.chronos.UserApi;
import com.openexchange.ajax.chronos.factory.AccountFactory;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CalendarAccountData;
import com.openexchange.testing.httpclient.models.CalendarAccountId;
import com.openexchange.testing.httpclient.models.CalendarAccountProviderData;
import com.openexchange.testing.httpclient.models.CalendarAccountProvidersResponse;
import com.openexchange.testing.httpclient.models.CalendarAccountResponse;
import com.openexchange.testing.httpclient.models.CalendarAccountsResponse;
import com.openexchange.testing.httpclient.models.CommonResponse;

/**
 * {@link CalendarAccountManager}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class CalendarAccountManager extends AbstractManager {

    public final String DEFAULT_ACCOUNT_ID = "0";

    public final String DEFAULT_ACCOUNT_PROVIDER_ID = "chronos";

    private final UserApi userApi;

    private List<CalendarAccountId> calAccIds;

    /**
     * Initializes a new {@link CalendarAccountManager}.
     */
    public CalendarAccountManager(UserApi userApi) {
        super();
        this.userApi = userApi;
        calAccIds = new ArrayList<>();
    }

    public void cleanUp() {
        try {
            for (CalendarAccountId id : calAccIds) {
                userApi.getChronosApi().deleteAccount(userApi.getSession(), id.getId(), System.currentTimeMillis());
            }
        } catch (ApiException e) {
            System.err.println("Could not clean up the calendar accounts for user " + userApi.getCalUser() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public CalendarAccountResponse createCalendarAccount(String providerId, String configuration) throws ApiException {
        CalendarAccountResponse response = userApi.getChronosApi().createAccount(userApi.getSession(), providerId, configuration);
        assertNull("Calendar account could not be created due an error.", response.getError());
        assertNotNull(response.getData());
        rememberCalendarAccountId(AccountFactory.createCalendarAccountId(response.getData().getId(), response.getData().getTimestamp()));
        return response;
    }

    /**
     * Creates a calendar account with the specified configuration for the specified provider
     * 
     * @param providerId The calendar provider identifier
     * @param configuration The configuration
     * @param expectedException flag to indicate that an exception is expected
     * @return The created {@link CalendarAccountData}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public CalendarAccountData createCalendarAccount(String providerId, String configuration, boolean expectedException) throws ApiException, ChronosApiException {
        CalendarAccountResponse response = userApi.getChronosApi().createAccount(userApi.getSession(), providerId, configuration);
        if (expectedException) {
            assertNotNull("An error was expected", response.getError());
            throw new ChronosApiException(response.getCode(), response.getError());
        }
        return handleCreation(response);
    }

    public void deleteCalendarAccount(List<CalendarAccountId> idsToDelete) throws ApiException {
        for (CalendarAccountId id : idsToDelete) {
            CommonResponse resp = userApi.getChronosApi().deleteAccount(userApi.getSession(), id.getId(), System.currentTimeMillis());
            assertNull(resp.getError(), resp.getError());
        }
        for (CalendarAccountId calendarAccountId : idsToDelete) {
            forgetCalendarAccountId(calendarAccountId);
        }
    }

    public CalendarAccountResponse loadCalendarAccount(CalendarAccountId calAccId) throws ApiException {
        CalendarAccountResponse response = userApi.getChronosApi().getAccount(userApi.getSession(), calAccId.getId());
        assertNull(response.getError(), response.getError());
        assertNotNull(response.getData());
        assertEquals("The id of the calendar account is invalid!", calAccId.getId(), response.getData().getId());
        return response;
    }

    public CalendarAccountsResponse loadAllCalendarAccounts(String providerId) throws ApiException {
        CalendarAccountsResponse response = userApi.getChronosApi().getAllAccounts(userApi.getSession(), providerId);
        assertNull(response.getError(), response.getError());
        assertNotNull(response.getData());
        return response;
    }

    /**
     * Helper method for {@link #updateCalendarAccount(CalendarAccountId, String)}
     * 
     * @param accountId The account identifier
     * @param timestamp The latest known timestamp
     * @param configuration The optional configuration
     * @return The updated {@link CalendarAccountData}
     * @throws ApiException if an API error is occurred
     */
    public CalendarAccountData updateCalendarAccount(String accountId, long timestamp, String configuration) throws ApiException {
        CalendarAccountResponse response = userApi.getChronosApi().updateAccount(userApi.getSession(), accountId, timestamp, configuration);
        return checkResponse(response.getError(), response.getErrorDesc(), response.getData());
    }

    public CalendarAccountResponse updateCalendarAccount(CalendarAccountId calAccId, String configuration) throws ApiException {
        forgetCalendarAccountId(calAccId);
        CalendarAccountResponse response = userApi.getChronosApi().updateAccount(userApi.getSession(), calAccId.getId(), System.currentTimeMillis(), configuration);
        if (null != response.getData()) {
            rememberCalendarAccountId(AccountFactory.createCalendarAccountId(response.getData().getId(), null));
        }
        return response;
    }

    /**
     * Returns a {@link List} with all available calendar providers
     * 
     * @return A {@link List} with all available calendar providers
     * @throws ApiException if an API error is occurred
     */
    public List<CalendarAccountProviderData> listAvailableProviders() throws ApiException {
        CalendarAccountProvidersResponse providersResponse = userApi.getChronosApi().providers(userApi.getSession());
        return checkResponse(providersResponse.getError(), providersResponse.getErrorDesc(), providersResponse.getData());
    }

    private void rememberCalendarAccountId(CalendarAccountId calAccId) throws ApiException {
        CalendarAccountResponse response = userApi.getChronosApi().getAccount(userApi.getSession(), calAccId.getId());
        if (calAccIds == null) {
            calAccIds = new ArrayList<>();
        }
        calAccIds.add(AccountFactory.createCalendarAccountId(response.getData().getId(), response.getData().getTimestamp()));
    }

    private void forgetCalendarAccountId(CalendarAccountId calAccId) throws ApiException {
        CalendarAccountResponse response = userApi.getChronosApi().getAccount(userApi.getSession(), calAccId.getId());
        if (calAccIds == null) {
            calAccIds = new ArrayList<>();
        }
        if (null != response.getData()) {
            calAccIds.remove(AccountFactory.createCalendarAccountId(response.getData().getId(), response.getData().getTimestamp()));
        }
    }

    public String createCalendarAccountTestConfiguration(boolean updateConfig) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("enabled", "true");
        obj.put("color", updateConfig ? "blue" : "red");
        return obj.toString();
    }

    /**
     * Handles the creation response and remembers the account id
     * 
     * @param response The {@link CalendarAccountResponse}
     * @return The {@link CalendarAccountData}
     * @throws ApiException if an API error is occurred
     */
    private CalendarAccountData handleCreation(CalendarAccountResponse response) throws ApiException {
        CalendarAccountData data = checkResponse(response.getError(), response.getErrorDesc(), response.getData());
        rememberCalendarAccountId(AccountFactory.createCalendarAccountId(data.getId(), data.getTimestamp()));
        return data;
    }
}
