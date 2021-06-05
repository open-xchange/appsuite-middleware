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
import static com.openexchange.java.Autoboxing.l;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.test.common.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.ChronosFreeBusyResponse;
import com.openexchange.testing.httpclient.models.ChronosFreeBusyResponseData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.FreeBusyBody;
import com.openexchange.testing.httpclient.models.FreeBusyTime;

/**
 *
 * {@link BasicFreeBusyTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class BasicFreeBusyTest extends AbstractChronosTest {

    @SuppressWarnings("hiding")
    private String folderId;

    private EventData createSingleEvent(String summary, long startDate, long endDate, List<Attendee> attendees) {
        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        if (attendees == null) {
            singleEvent.setAttendees(Collections.singletonList(AttendeeFactory.createIndividual(I(testUser.getUserId()))));
        } else {
            singleEvent.setAttendees(attendees);
        }
        singleEvent.setStartDate(DateTimeUtil.getDateTime(startDate));
        singleEvent.setEndDate(DateTimeUtil.getDateTime(endDate));
        singleEvent.setTransp(TranspEnum.OPAQUE);
        singleEvent.setSummary(summary);
        return singleEvent;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderId = createAndRememberNewFolder(defaultUserApi, getDefaultFolder(), getCalendaruser());
    }

    @Test
    public void testMaskId() throws Exception {
        Date now = new Date();
        long day1 = 1000 * (now.getTime() / 1000);
        long day3 = day1 + TimeUnit.DAYS.toMillis(2);
        long day5 = day3 + TimeUnit.DAYS.toMillis(2);
        long nextWeek = day1 + TimeUnit.DAYS.toMillis(7);
        createEvent("dayOne", day1, day1 + TimeUnit.HOURS.toMillis(1));
        EventData createEvent = createEvent("dayThree", day3, day3 + TimeUnit.HOURS.toMillis(1));
        createEvent("dayFive", day5, day5 + TimeUnit.HOURS.toMillis(1));

        ChronosFreeBusyResponse freeBusy = chronosApi.freebusy(DateTimeUtil.getZuluDateTime(day1).getValue(), DateTimeUtil.getZuluDateTime(nextWeek).getValue(), createAttendeesBody(getCalendaruser()), createEvent.getId(), Boolean.TRUE);

        assertEquals(freeBusy.getError(), null, freeBusy.getErrorDesc());
        assertNotNull(freeBusy.getData());
        List<ChronosFreeBusyResponseData> data = freeBusy.getData();
        //Expect one result for the given attendee
        assertEquals(1, data.size());
        List<FreeBusyTime> freeBusyTimes = data.get(0).getFreeBusyTime();

        // Expect free busy times events on day one and five, but not for day 3
        FreeBusyTime time1 = null;
        FreeBusyTime time3 = null;
        FreeBusyTime time5 = null;
        for (FreeBusyTime freeBusyTime : freeBusyTimes) {
            if (day1 == l(freeBusyTime.getStartTime())) {
                time1 = freeBusyTime;
            } else if (day3 == l(freeBusyTime.getStartTime())) {
                time3 = freeBusyTime;
            } else if (day5 == l(freeBusyTime.getStartTime())) {
                time5 = freeBusyTime;
            }
        }

        assertNotNull("No free/busy time for event on " + day1, time1);
        assertEquals(day1 + TimeUnit.HOURS.toMillis(1), time1.getEndTime().longValue());
        assertEquals("BUSY", time1.getFbType());

        assertNotNull("No free/busy time for event on " + day5, time5);
        assertEquals(day5 + TimeUnit.HOURS.toMillis(1), time5.getEndTime().longValue());
        assertEquals("BUSY", time5.getFbType());

        assertNull("Unexpected free/busy time for masked event on " + day3, time3);
    }

    private EventData createEvent(String summary, long start, long end, TestUser... users) throws ApiException {
        List<Attendee> attendees = null;
        if (users != null && users.length > 0) {
            attendees = new ArrayList<>(users.length);
            for (TestUser user : users) {
                Attendee att = new Attendee();
                att.setCuType(CuTypeEnum.INDIVIDUAL);
                att.setEntity(I(user.getUserId()));
                attendees.add(att);
            }
        }
        EventData event = createSingleEvent(summary, start, end, attendees);
        event.setFolder(folderId);
        event = eventManager.createEvent(event, true);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolder(folderId);
        rememberEventId(eventId);
        return event;
    }

    private FreeBusyBody createAttendeesBody(int... attendees) {
        FreeBusyBody b = new FreeBusyBody();
        for (int i : attendees) {
            Attendee attendee = new Attendee();
            attendee.setCuType(CuTypeEnum.INDIVIDUAL);
            attendee.setEntity(Integer.valueOf(i));
            b.addAttendeesItem(attendee);
        }
        return b;
    }

}
