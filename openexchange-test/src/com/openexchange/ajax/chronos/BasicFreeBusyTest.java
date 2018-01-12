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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.AttendeeAndAlarm;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.ChronosFreeBusyResponse;
import com.openexchange.testing.httpclient.models.ChronosFreeBusyResponseData;
import com.openexchange.testing.httpclient.models.ChronosUpdatesResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.EventsResponse;
import com.openexchange.testing.httpclient.models.FreeBusyBody;
import com.openexchange.testing.httpclient.models.FreeBusyTime;
import com.openexchange.testing.httpclient.modules.ChronosApi;
import edu.emory.mathcs.backport.java.util.Collections;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

/**
 *
 * {@link BasicFreeBusyTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class BasicFreeBusyTest extends AbstractChronosTest {

    private String folderId;
    private UserApi user2;

    @SuppressWarnings("unchecked")
    private EventData createSingleEvent(String summary, long startDate, long endDate, List<Attendee> attendees) {
        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        if (attendees == null) {
            Attendee attendee = new Attendee();
            attendee.entity(defaultUserApi.getCalUser());
            attendee.cuType(CuTypeEnum.INDIVIDUAL);
            attendee.setUri("mailto:" + this.testUser.getLogin());
            singleEvent.setAttendees(Collections.singletonList(attendee));
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
        folderId = createAndRememberNewFolder(defaultUserApi, defaultUserApi.getSession(), getDefaultFolder(), defaultUserApi.getCalUser());

        // prepare second user
        user2 = new UserApi(generateClient(testUser2), generateEnhancedClient(testUser2), testUser2);
        rememberClient(user2.getClient());
        rememberClient(user2.getEnhancedApiClient());
    }


    @Test
    public void testFreeBusyTime() throws Exception {
        Date now = new Date();
        int offset = TimeZone.getDefault().getOffset(now.getTime());
        long day1 = 1000 * (now.getTime() / 1000);
        long day3 = day1 + TimeUnit.DAYS.toMillis(2);
        long day5 = day3 + TimeUnit.DAYS.toMillis(2);
        long nextWeek = day1 + TimeUnit.DAYS.toMillis(7);
        createEvent("dayOne", day1, day1 + TimeUnit.HOURS.toMillis(1));
        createEvent("dayThree", day3, day3 + TimeUnit.HOURS.toMillis(1));
        createEvent("dayFive", day5, day5 + TimeUnit.HOURS.toMillis(1));

        ChronosFreeBusyResponse freeBusy = chronosApi.freebusy(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(day1 - offset).getValue(), DateTimeUtil.getZuluDateTime(nextWeek).getValue(), createAttendeesBody(defaultUserApi.getCalUser()), null);

        assertEquals(freeBusy.getError(), null, freeBusy.getErrorDesc());
        assertNotNull(freeBusy.getData());
        List<ChronosFreeBusyResponseData> data = freeBusy.getData();
        //Expect only one event for the given attendee
        assertEquals(1, data.size());
        List<FreeBusyTime> freeBusyTimes = data.get(0).getFreeBusyTime();

        // Adapt to timezone
        day1 -= offset;
        day3 -= offset;
        day5 -= offset;

        // Expect 3 free busy times. One each for every event
        assertEquals(3, freeBusyTimes.size());
        assertEquals(day1, freeBusyTimes.get(0).getStartTime().longValue());
        assertEquals(day1 + TimeUnit.HOURS.toMillis(1), freeBusyTimes.get(0).getEndTime().longValue());
        assertEquals("BUSY", freeBusyTimes.get(0).getFbType());

        assertEquals(day3, freeBusyTimes.get(1).getStartTime().longValue());
        assertEquals(day3 + TimeUnit.HOURS.toMillis(1), freeBusyTimes.get(1).getEndTime().longValue());
        assertEquals("BUSY", freeBusyTimes.get(1).getFbType());

        assertEquals(day5, freeBusyTimes.get(2).getStartTime().longValue());
        assertEquals(day5 + TimeUnit.HOURS.toMillis(1), freeBusyTimes.get(2).getEndTime().longValue());
        assertEquals("BUSY", freeBusyTimes.get(2).getFbType());
    }

    @Test
    public void testFreeBusyTimeWithOverlappingEventsWithDifferentStati() throws Exception {
        Date now = new Date();
        int offset = TimeZone.getDefault().getOffset(now.getTime());
        // Define starting dates
        long first = 1000 * (now.getTime() / 1000);
        long second = first + TimeUnit.MINUTES.toMillis(30);
        long third = second + TimeUnit.MINUTES.toMillis(30);
        long nextWeek = first + TimeUnit.DAYS.toMillis(7);

        // Define two users
        IdWrappingTestUser[] users = new IdWrappingTestUser[2];
        users[0] = new IdWrappingTestUser(testUser);
        users[0].setUserId(defaultUserApi.getCalUser());
        users[1] = new IdWrappingTestUser(testUser2);

        String secondSession = user2.getSession();
        users[1].setUserId(user2.getCalUser());

        ChronosApi secondUserChronosApi = user2.getChronosApi();
        String secondUserFolder = getDefaultFolder(secondSession, user2.getClient());
        // Do a request to get a valid timestamp
        EventsResponse allEvents = secondUserChronosApi.getAllEvents(secondSession, "20170101T000000Z", "20180101T000000Z", secondUserFolder, null, null, null, false, true);
        assertNull(allEvents.getErrorDesc(), allEvents.getError());
        Long timestamp = allEvents.getTimestamp();

        // Create three overlapping events
        createEvent("first", first, first + TimeUnit.HOURS.toMillis(1), users);
        ChronosUpdatesResponse updates = secondUserChronosApi.getUpdates(secondSession, secondUserFolder, timestamp, null, null, null, null, null, false, true);
        assertNull(updates.getErrorDesc(), updates.getError());
        assertNotNull(updates.getData());
        assertEquals(1, updates.getData().getNewAndModified().size());
        String newEventId = updates.getData().getNewAndModified().get(0).getId();
        AttendeeAndAlarm body = new AttendeeAndAlarm();
        Attendee att = new Attendee();
        att.setEntity(users[1].getUserId());
        att.setCuType(CuTypeEnum.INDIVIDUAL);
        att.setPartStat("ACCEPTED");
        body.setAttendee(att);
        // FIXME: Use the EventManager instead
        ChronosCalendarResultResponse updateAttendee = secondUserChronosApi.updateAttendee(secondSession, secondUserFolder, newEventId, getLastTimestamp(), body, null, false, true, null, null, false);
        assertNull(updateAttendee.getErrorDesc(), updateAttendee.getError());
        assertNotNull(updateAttendee.getData());

        createEvent("second", second, second + TimeUnit.HOURS.toMillis(1), users);
        updates = secondUserChronosApi.getUpdates(secondSession, secondUserFolder, updateAttendee.getTimestamp(), null, null, null, null, null, false, true);
        assertNull(updates.getErrorDesc(), updates.getError());
        assertNotNull(updates.getData());
        assertEquals(1, updates.getData().getNewAndModified().size());
        newEventId = updates.getData().getNewAndModified().get(0).getId();
        body = new AttendeeAndAlarm();
        att = new Attendee();
        att.setEntity(users[1].getUserId());
        att.setCuType(CuTypeEnum.INDIVIDUAL);
        att.setPartStat("TENTATIVE");
        body.setAttendee(att);
        // FIXME: Use the EventManager instead
        ChronosCalendarResultResponse updateAttendee2 = secondUserChronosApi.updateAttendee(secondSession, secondUserFolder, newEventId, getLastTimestamp(), body, null, false, true, null, null, false);
        assertNull(updateAttendee2.getErrorDesc(), updateAttendee2.getError());
        assertNotNull(updateAttendee2.getData());

        createEvent("third", third, third + TimeUnit.HOURS.toMillis(1), users);
        updates = secondUserChronosApi.getUpdates(secondSession, secondUserFolder, updateAttendee2.getTimestamp(), null, null, null, null, null, false, true);
        assertNull(updates.getErrorDesc(), updates.getError());
        assertNotNull(updates.getData());
        assertEquals(1, updates.getData().getNewAndModified().size());
        newEventId = updates.getData().getNewAndModified().get(0).getId();
        body = new AttendeeAndAlarm();
        att = new Attendee();
        att.setEntity(users[1].getUserId());
        att.setCuType(CuTypeEnum.INDIVIDUAL);
        att.setPartStat("DECLINED");
        body.setAttendee(att);
        // FIXME: Use the EventManager instead
        ChronosCalendarResultResponse updateAttendee3 = secondUserChronosApi.updateAttendee(secondSession, secondUserFolder, newEventId, getLastTimestamp(), body, null, false, true, null, null, false);
        assertNull(updateAttendee3.getErrorDesc(), updateAttendee3.getError());
        assertNotNull(updateAttendee3.getData());

        ChronosApi secondChronosApi = new ChronosApi(user2.getClient());
        ChronosFreeBusyResponse freeBusy = secondChronosApi.freebusy(secondSession, DateTimeUtil.getZuluDateTime(first - TimeUnit.HOURS.toMillis(5) - offset).getValue(), DateTimeUtil.getZuluDateTime(nextWeek).getValue(), createAttendeesBody(users[1].getUserId()), null);
        assertEquals(freeBusy.getErrorDesc(), null, freeBusy.getError());
        assertNotNull(freeBusy.getData());
        List<ChronosFreeBusyResponseData> data = freeBusy.getData();
        //Expect only one event for the given attendee
        assertEquals(1, data.size());
        List<FreeBusyTime> freeBusyTimes = data.get(0).getFreeBusyTime();
        // Expect 1 free busy times.
        assertEquals(1, freeBusyTimes.size());
        assertEquals(first - offset, freeBusyTimes.get(0).getStartTime().longValue());
        assertEquals(first - offset + TimeUnit.MINUTES.toMillis(90), freeBusyTimes.get(0).getEndTime().longValue());
        assertEquals("BUSY", freeBusyTimes.get(0).getFbType());
    }

    @Test
    public void testMaskId() throws Exception {
        Date now = new Date();
        int offset = TimeZone.getDefault().getOffset(now.getTime());
        long day1 = 1000 * (now.getTime() / 1000);
        long day3 = day1 + TimeUnit.DAYS.toMillis(2);
        long day5 = day3 + TimeUnit.DAYS.toMillis(2);
        long nextWeek = day1 + TimeUnit.DAYS.toMillis(7);
        createEvent("dayOne", day1, day1 + TimeUnit.HOURS.toMillis(1));
        EventData createEvent = createEvent("dayThree", day3, day3 + TimeUnit.HOURS.toMillis(1));
        createEvent("dayFive", day5, day5 + TimeUnit.HOURS.toMillis(1));

        ChronosFreeBusyResponse freeBusy = chronosApi.freebusy(defaultUserApi.getSession(), DateTimeUtil.getZuluDateTime(day1 - offset).getValue(), DateTimeUtil.getZuluDateTime(nextWeek).getValue(), createAttendeesBody(defaultUserApi.getCalUser()), createEvent.getId());

        assertEquals(freeBusy.getError(), null, freeBusy.getErrorDesc());
        assertNotNull(freeBusy.getData());
        List<ChronosFreeBusyResponseData> data = freeBusy.getData();
        //Expect only one event for the given attendee
        assertEquals(1, data.size());
        List<FreeBusyTime> freeBusyTimes = data.get(0).getFreeBusyTime();

        // Adapt to timezone
        day1 -= offset;
        day3 -= offset;
        day5 -= offset;

        // Expect only 2 free busy times.
        assertEquals(2, freeBusyTimes.size());
        assertEquals(day1, freeBusyTimes.get(0).getStartTime().longValue());
        assertEquals(day1 + TimeUnit.HOURS.toMillis(1), freeBusyTimes.get(0).getEndTime().longValue());
        assertEquals("BUSY", freeBusyTimes.get(0).getFbType());

        assertEquals(day5, freeBusyTimes.get(1).getStartTime().longValue());
        assertEquals(day5 + TimeUnit.HOURS.toMillis(1), freeBusyTimes.get(1).getEndTime().longValue());
        assertEquals("BUSY", freeBusyTimes.get(1).getFbType());
    }

    private EventData createEvent(String summary, long start, long end, IdWrappingTestUser... users) throws ApiException {
        List<Attendee> attendees = null;
        if (users != null && users.length > 0) {
            attendees = new ArrayList<>(users.length);
            for (IdWrappingTestUser user : users) {
                Attendee att = new Attendee();
                att.setCuType(CuTypeEnum.INDIVIDUAL);
                if (user.containsId()) {
                    att.setEntity(user.getUserId());
                }
                attendees.add(att);
            }
        }
        // FIXME: Use the EventManager instead
        ChronosCalendarResultResponse createEvent = defaultUserApi.getChronosApi().createEvent(defaultUserApi.getSession(), folderId, createSingleEvent(summary, start, end, attendees), true, false, null, null, false);
        assertNull(createEvent.getErrorDesc(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolder(folderId);
        rememberEventId(defaultUserApi, eventId);
        setLastTimestamp(createEvent.getTimestamp());
        return event;
    }

    private FreeBusyBody createAttendeesBody(int... attendees) {
        FreeBusyBody b = new FreeBusyBody();
        for (Integer i : attendees) {
            Attendee attendee = new Attendee();
            attendee.setCuType(CuTypeEnum.INDIVIDUAL);
            attendee.setEntity(i);
            b.addAttendeesItem(attendee);
        }
        return b;
    }

}
