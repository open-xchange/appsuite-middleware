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
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.AttendeeAndAlarm;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.CheckEventConflictResponse;
import com.openexchange.testing.httpclient.models.CheckEventResponse;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.ChronosFreeBusyEventsData;
import com.openexchange.testing.httpclient.models.ChronosFreeBusyEventsResponse;
import com.openexchange.testing.httpclient.models.ChronosFreeBusyHasResponse;
import com.openexchange.testing.httpclient.models.ChronosFreeBusyResponse;
import com.openexchange.testing.httpclient.models.ChronosFreeBusyResponseData;
import com.openexchange.testing.httpclient.models.ChronosUpdatesResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;
import com.openexchange.testing.httpclient.modules.ChronosApi;
import com.openexchange.testing.httpclient.modules.ChronosFreebusyApi;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.EventsResponse;
import com.openexchange.testing.httpclient.models.FreeBusyTime;
import com.openexchange.testing.httpclient.models.LoginResponse;
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
    private ChronosFreebusyApi freeBusyApi;

    @SuppressWarnings("unchecked")
    private EventData createSingleEvent(String summary, long startDate, long endDate, List<Attendee> attendees) {
        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        if(attendees==null){
            Attendee attendee = new Attendee();
            attendee.entity(calUser);
            attendee.cuType(CuTypeEnum.INDIVIDUAL);
            attendee.setUri("mailto:" + this.testUser.getLogin());
            singleEvent.setAttendees(Collections.singletonList(attendee));
        } else {
            singleEvent.setAttendees(attendees);
        }
        singleEvent.setStartDate(getDateTime(startDate));
        singleEvent.setEndDate(getDateTime(endDate));
        singleEvent.setTransp(TranspEnum.OPAQUE);
        singleEvent.setAllDay(false);
        singleEvent.setSummary(summary);
        return singleEvent;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderId = getDefaultFolder();
        freeBusyApi = new ChronosFreebusyApi(getClient());
    }

    @Test
    public void testHasEvents() throws Exception {

        long day1 = 1000 * (System.currentTimeMillis()/ 1000);
        long day3 = day1 + TimeUnit.DAYS.toMillis(2);
        long day5 = day3 + TimeUnit.DAYS.toMillis(2);
        long nextWeek = day1 + TimeUnit.DAYS.toMillis(7);
        createEvent("dayOne", day1, day1+TimeUnit.HOURS.toMillis(1));
        createEvent("dayThree", day3, day3+TimeUnit.HOURS.toMillis(1));
        createEvent("dayFive", day5, day5+TimeUnit.HOURS.toMillis(1));

        ChronosFreeBusyHasResponse freebusyHas = freeBusyApi.freebusyHas(session, getZuluDateTime(day1).getValue(), getZuluDateTime(nextWeek).getValue());
        assertEquals(freebusyHas.getErrorDesc(), null, freebusyHas.getError());
        assertNotNull(freebusyHas.getData());
        List<Boolean> data = freebusyHas.getData();
        assertEquals(7, data.size());
        assertTrue(data.get(0));
        assertTrue(data.get(2));
        assertTrue(data.get(4));
    }

    @Test
    public void testFreeBusyEvents() throws Exception {

        long day1 = 1000 * (System.currentTimeMillis()/ 1000);
        long nextWeek = day1 + TimeUnit.DAYS.toMillis(7);
        EventData event = createEvent("dayOne", day1, day1+TimeUnit.HOURS.toMillis(1));

        ChronosFreeBusyEventsResponse freeBusyEvents = freeBusyApi.freebusyEvents(session, getZuluDateTime(day1).getValue(), getZuluDateTime(nextWeek).getValue(), Integer.toString(calUser));
        assertEquals(freeBusyEvents.getErrorDesc(), null, freeBusyEvents.getError());
        assertNotNull(freeBusyEvents.getData());
        List<ChronosFreeBusyEventsData> data = freeBusyEvents.getData();
        assertEquals(1, data.size());
        List<EventData> events = data.get(0).getEvents();
        assertEquals(1, events.size());
        assertEquals(event.getId(), events.get(0).getId());
    }

    @Test
    public void testFreeBusyTime() throws Exception {

        long day1 = 1000 * (System.currentTimeMillis()/ 1000);
        long day3 = day1 + TimeUnit.DAYS.toMillis(2);
        long day5 = day3 + TimeUnit.DAYS.toMillis(2);
        long nextWeek = day1 + TimeUnit.DAYS.toMillis(7);
        createEvent("dayOne", day1, day1+TimeUnit.HOURS.toMillis(1));
        createEvent("dayThree", day3, day3+TimeUnit.HOURS.toMillis(1));
        createEvent("dayFive", day5, day5+TimeUnit.HOURS.toMillis(1));

        ChronosFreeBusyResponse freeBusy = freeBusyApi.freebusy(session, getZuluDateTime(day1).getValue(), getZuluDateTime(nextWeek).getValue(), Integer.toString(calUser));
        assertEquals(freeBusy.getErrorDesc(), null, freeBusy.getError());
        assertNotNull(freeBusy.getData());
        List<ChronosFreeBusyResponseData> data = freeBusy.getData();
        //Expect only one event for the given attendee
        assertEquals(1, data.size());
        List<FreeBusyTime> freeBusyTimes = data.get(0).getFreeBusyTime();
        // Expect 3 free busy times. One each for every event
        assertEquals(3, freeBusyTimes.size());
        assertEquals(day1, freeBusyTimes.get(0).getStartTime().longValue());
        assertEquals(day1+TimeUnit.HOURS.toMillis(1), freeBusyTimes.get(0).getEndTime().longValue());
        assertEquals("BUSY", freeBusyTimes.get(0).getFbType());

        assertEquals(day3, freeBusyTimes.get(1).getStartTime().longValue());
        assertEquals(day3+TimeUnit.HOURS.toMillis(1), freeBusyTimes.get(1).getEndTime().longValue());
        assertEquals("BUSY", freeBusyTimes.get(1).getFbType());

        assertEquals(day5, freeBusyTimes.get(2).getStartTime().longValue());
        assertEquals(day5+TimeUnit.HOURS.toMillis(1), freeBusyTimes.get(2).getEndTime().longValue());
        assertEquals("BUSY", freeBusyTimes.get(2).getFbType());
    }

    @Test
    public void testFreeBusyTimeWithOverlappingEventsWithDifferentStati() throws Exception {
    	// Define starting dates
        long first = 1000 * (System.currentTimeMillis()/ 1000);
        long second = first + TimeUnit.MINUTES.toMillis(30);
        long third = second + TimeUnit.MINUTES.toMillis(30);
        long nextWeek = first + TimeUnit.DAYS.toMillis(7);

        // Define two users
        IdWrappingTestUser[] users = new IdWrappingTestUser[2];
        users[0] = new IdWrappingTestUser(testUser);
        users[0].setUserId(calUser);
        users[1] = new IdWrappingTestUser(testUser2);
        ApiClient client = generateClient(testUser2);
        LoginResponse login = login(testUser2, client);
        String secondSession = login.getSession();
        users[1].setUserId(login.getUserId());

        ChronosApi secondUserChronosApi = new ChronosApi(client);
        String secondUserFolder = getDefaultFolder(secondSession, client);
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
        ChronosCalendarResultResponse updateAttendee = secondUserChronosApi.updateAttendee(secondSession, secondUserFolder, newEventId, getLastTimestamp(), body, null, false, true);
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
        ChronosCalendarResultResponse updateAttendee2 = secondUserChronosApi.updateAttendee(secondSession, secondUserFolder, newEventId, getLastTimestamp(), body, null, false, true);
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
        ChronosCalendarResultResponse updateAttendee3 = secondUserChronosApi.updateAttendee(secondSession, secondUserFolder, newEventId, getLastTimestamp(), body, null, false, true);
        assertNull(updateAttendee3.getErrorDesc(), updateAttendee3.getError());
        assertNotNull(updateAttendee3.getData());

        ChronosFreebusyApi secondUserFreeBusyApi = new ChronosFreebusyApi(client);
        ChronosFreeBusyResponse freeBusy = secondUserFreeBusyApi.freebusy(secondSession, getZuluDateTime(first - TimeUnit.HOURS.toMillis(5)).getValue(), getZuluDateTime(nextWeek).getValue(), Integer.toString(users[1].getUserId()));
        logoutClient(client);
        assertEquals(freeBusy.getErrorDesc(), null, freeBusy.getError());
        assertNotNull(freeBusy.getData());
        List<ChronosFreeBusyResponseData> data = freeBusy.getData();
        //Expect only one event for the given attendee
        assertEquals(1, data.size());
        List<FreeBusyTime> freeBusyTimes = data.get(0).getFreeBusyTime();
        // Expect 1 free busy times.
        assertEquals(1, freeBusyTimes.size());
        assertEquals(first, freeBusyTimes.get(0).getStartTime().longValue());
        assertEquals(first + TimeUnit.MINUTES.toMillis(90), freeBusyTimes.get(0).getEndTime().longValue());
        assertEquals("BUSY", freeBusyTimes.get(0).getFbType());
    }

    @Test
    public void testCheckEvent() throws Exception {

        long day1 = 1000 * (System.currentTimeMillis()/ 1000);
        IdWrappingTestUser[] users = new IdWrappingTestUser[2];
        users[0] = new IdWrappingTestUser(testUser);
        users[0].setUserId(calUser);
        users[1] = new IdWrappingTestUser(testUser2);
        ApiClient client = generateClient(testUser2);
        users[1].setUserId(login(testUser2, client).getUserId());
        logoutClient(client);

        // Check with one event for both attendees
        EventData event = createEvent("conflicting event", day1, day1+TimeUnit.HOURS.toMillis(1), users);

        StringBuilder attendees = new StringBuilder();
        for(IdWrappingTestUser user: users){
           attendees.append(user.getUserId()).append(",");
        }

        event.setId(null);
        CheckEventResponse freebusyHas = freeBusyApi.checkEvent(session, folderId, attendees.toString(), event);
        assertNull(freebusyHas.getErrorDesc(), freebusyHas.getError());
        assertNotNull(freebusyHas.getData());
        CheckEventConflictResponse data = freebusyHas.getData();
        assertEquals(1, data.getConflicts().size());

        // Check again with one additional event for user 1
        {
            IdWrappingTestUser[] users2 = new IdWrappingTestUser[1];
            users2[0] = new IdWrappingTestUser(testUser);
            users2[0].setUserId(calUser);
            EventData event2 = createEvent("conflicting event2", day1, day1+TimeUnit.HOURS.toMillis(1), users2);

            StringBuilder attendees2 = new StringBuilder();
            for(IdWrappingTestUser user: users2){
               attendees2.append(user.getUserId()).append(",");
            }

            // Now the check should return 2 conflicts
            event2.setId(null);
            CheckEventResponse freebusyHas2 = freeBusyApi.checkEvent(session, folderId, attendees2.toString(), event2);
            assertEquals(null, freebusyHas2.getError());
            assertNotNull(freebusyHas2.getData());
            CheckEventConflictResponse data2 = freebusyHas2.getData();
            assertEquals(2, data2.getConflicts().size());

            // Check again for attendee 2. There should still be only one conflict.
            freebusyHas2 = freeBusyApi.checkEvent(session, folderId, Integer.toString(users[1].getUserId()), event2);
            assertEquals(null, freebusyHas2.getError());
            assertNotNull(freebusyHas2.getData());
            data2 = freebusyHas2.getData();
            assertEquals(1, data2.getConflicts().size());
        }
    }

    private EventData createEvent(String summary, long start, long end, IdWrappingTestUser... users) throws ApiException{
        List<Attendee> attendees = null;
        if(users!=null && users.length>0){
            attendees = new ArrayList<>(users.length);
            for(IdWrappingTestUser user: users){
                Attendee att = new Attendee();
                att.setCuType(CuTypeEnum.INDIVIDUAL);
                if(user.containsId()){
                    att.setEntity(user.getUserId());
                }
                attendees.add(att);
            }
        }
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSingleEvent(summary, start, end, attendees), true, false);
        assertNull(createEvent.getErrorDesc(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolderId(folderId);
        rememberEventId(eventId);
        setLastTimestamp(createEvent.getTimestamp());
        return event;
    }


}
