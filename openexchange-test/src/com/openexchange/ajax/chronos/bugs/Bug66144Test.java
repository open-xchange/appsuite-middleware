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

package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.java.util.TimeZones;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.ChronosFreeBusyResponse;
import com.openexchange.testing.httpclient.models.ChronosFreeBusyResponseData;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.FreeBusyBody;
import com.openexchange.testing.httpclient.models.FreeBusyTime;
import com.openexchange.testing.httpclient.models.UserData;
import com.openexchange.testing.httpclient.modules.UserApi;

/**
 * {@link Bug66144Test}
 *
 * Missing events in freeBusy request
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class Bug66144Test extends AbstractChronosTest {

    @Test
    public void testFreeeBusy() throws Exception {
        /*
         * insert weekly all day event series, starting next monday
         */
        Date startDate = TimeTools.D("next monday at 00:00", TimeZones.UTC);
        Date endDate = CalendarUtils.add(startDate, Calendar.DATE, 1);
        EventData eventData = new EventData();
        eventData.setStartDate(new DateTimeData().value(formatAsDate(startDate, TimeZones.UTC)));
        eventData.setEndDate(new DateTimeData().value(formatAsDate(endDate, TimeZones.UTC)));
        eventData.setSummary("Bug66144Test");
        eventData.setRrule("FREQ=WEEKLY;BYDAY=MO;COUNT=10");
        eventData = eventManager.createEvent(eventData, true);
        /*
         * retrieve free/busy period for working hours on next monday morning
         */
        UserData userData = new UserApi(getApiClient()).getUser(null).getData();
        String from = formatAsUTC(TimeTools.D("next monday at 09:00", TimeZone.getTimeZone(userData.getTimezone())));
        String until = formatAsUTC(TimeTools.D("next monday at 17:00", TimeZone.getTimeZone(userData.getTimezone())));
        Attendee attendee = new Attendee().entity(getApiClient().getUserId()).cuType(CuTypeEnum.INDIVIDUAL);
        FreeBusyBody freeBusyBody = new FreeBusyBody().attendees(Collections.singletonList(attendee));
        ChronosFreeBusyResponse freeBusyResponse = chronosApi.freebusy(from, until, freeBusyBody, null, null);
        assertNull(freeBusyResponse.getError(), freeBusyResponse.getError());
        /*
         * verify the first occurrence of the event series is contained
         */
        assertTrue(null != freeBusyResponse.getData() && 1 == freeBusyResponse.getData().size());
        ChronosFreeBusyResponseData data = freeBusyResponse.getData().get(0);
        assertEquals(attendee.getEntity(), data.getAttendee().getEntity());
        assertNotNull(data.getFreeBusyTime());
        FreeBusyTime matchingFreeBusyTime = null;
        for (FreeBusyTime freeBusyTime : data.getFreeBusyTime()) {
            if (null != freeBusyTime.getEvent() && eventData.getId().equals(freeBusyTime.getEvent().getId())) {
                matchingFreeBusyTime = freeBusyTime;
                break;
            }
        }
        assertNotNull(matchingFreeBusyTime);
    }

    private static String formatAsDate(Date date, TimeZone timeZone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

    private static String formatAsUTC(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZones.UTC);
        return dateFormat.format(date);
    }

}
