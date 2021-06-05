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

package com.openexchange.ajax.chronos;

import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;
import com.openexchange.testing.httpclient.models.QuotasResponse;
import com.openexchange.testing.httpclient.modules.QuotaApi;

/**
 * {@link ChronosQuotaTest} - For new CalendarQuotaProvider
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class ChronosQuotaTest extends AbstractChronosTest {

    private static final String MODULE = "calendar";
    private static final HashMap<String, String> CONFIG = new HashMap<>(1);

    static {
        CONFIG.put("com.openexchange.quota.calendar", "0");
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUpConfiguration();
    }

    /**
     * Checks if the creation of an Event fails if the there is no quota available.
     *
     * @throws Exception In case of mismatching test results
     */
    @Test
    public void testExeededQuota() throws Exception {
        QuotaApi api = new QuotaApi(getApiClient());
        QuotasResponse response = api.getQuotaInformation(MODULE, "0", defaultFolderId);
        Object data = response.getData();

        // Check for the right type
        assertThat("Can't assign response data to the known output", data, instanceOf(Map.class));

        // Suppress since we already checked ..
        @SuppressWarnings("unchecked") Map<String, Object> info = (Map<String, Object>) data;

        // Check quota info
        assertThat("Account identifier does not match", info.get("account_id"), is("0"));
        assertThat("Account name does not match", info.get("account_name"), is("Internal Calendar"));
        assertThat("Account quota should be null and therefore not be part of the response.", info.get("countquota"), is(nullValue()));
        /*
         * Can't check something like
         * assertThat("Account use does not match", info.get("countuse"), is(new Integer(0)));
         * --> Other test might create event that get not deleted..
         */

        // Try creating a new event
        // Can't use EventManager, we need to check the exception here
        ChronosCalendarResultResponse resultResponse = defaultUserApi.getChronosApi().createEvent(getDefaultFolder(getApiClient()), createSingleEvent("SingleEventQuotaTest"), Boolean.TRUE, null, Boolean.FALSE, null, null, null, Boolean.FALSE, null);

        // Check that creation failed
        assertThat("No response!", resultResponse, is(not(nullValue())));
        assertThat("Event could be saved out of quota limit", resultResponse.getError(), is(not(nullValue())));
        String expectedCode = "QUOTA-0003";
        assertThat("Code does not match. Expected '" + expectedCode + "'.", resultResponse.getCode(), is(expectedCode));
    }

    private EventData createSingleEvent(String summary, DateTimeData startDate, DateTimeData endDate) {
        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        Attendee attendee = new Attendee();
        attendee.entity(I(testUser.getUserId()));
        attendee.cuType(CuTypeEnum.INDIVIDUAL);
        singleEvent.setAttendees(Collections.singletonList(attendee));
        singleEvent.setStartDate(startDate);
        singleEvent.setEndDate(endDate);
        singleEvent.setTransp(TranspEnum.OPAQUE);
        singleEvent.setSummary(summary);
        return singleEvent;
    }

    private EventData createSingleEvent(String summary) {
        return createSingleEvent(summary, DateTimeUtil.getDateTime(System.currentTimeMillis()), DateTimeUtil.getDateTime(System.currentTimeMillis() + 5000));
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return CONFIG;
    }

    @Override
    protected String getScope() {
        return "user";
    }
}
