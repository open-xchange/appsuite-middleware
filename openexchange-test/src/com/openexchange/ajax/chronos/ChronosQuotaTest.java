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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
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
import com.openexchange.testing.httpclient.models.LoginResponse;
import com.openexchange.testing.httpclient.models.QuotasResponse;
import com.openexchange.testing.httpclient.modules.QuotaApi;
import edu.emory.mathcs.backport.java.util.Collections;

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
        QuotasResponse response = api.getQuotaInformation(getApiClient().getSession(), MODULE, "0");
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
        LoginResponse login = defaultUserApi.login(testUser.getLogin(), testUser.getPassword(), getApiClient());

        // Can't use EventManager, we need to check the exception here
        ChronosCalendarResultResponse resultResponse = defaultUserApi.getChronosApi().createEvent(login.getSession(), getDefaultFolder(login.getSession(), getApiClient()), createSingleEvent("SingleEventQuotaTest"), Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null, null, Boolean.FALSE);

        // Check that creation failed
        assertThat("No response!", resultResponse, is(not(nullValue())));
        assertThat("Event could be saved out of quota limit", resultResponse.getError(), is(not(nullValue())));
        String expectedCode = "QUOTA-0003";
        assertThat("Code does not match. Expected '" + expectedCode + "'.", resultResponse.getCode(), is(expectedCode));
    }

    @SuppressWarnings("unchecked")
    private EventData createSingleEvent(String summary, DateTimeData startDate, DateTimeData endDate) {
        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        Attendee attendee = new Attendee();
        attendee.entity(getApiClient().getUserId());
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
