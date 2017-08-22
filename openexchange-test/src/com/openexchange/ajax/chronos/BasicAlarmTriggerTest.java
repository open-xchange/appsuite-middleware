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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.AlarmTrigger;
import com.openexchange.testing.httpclient.models.AlarmTriggerData;
import com.openexchange.testing.httpclient.models.AlarmTriggerResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.CalendarResult;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.EventResponse;
import com.openexchange.testing.httpclient.models.Trigger;
import com.openexchange.testing.httpclient.models.Trigger.RelatedEnum;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 *
 * {@link BasicAlarmTriggerTest} tests alarm triggers.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class BasicAlarmTriggerTest extends AbstractChronosTest {

    private String folderId;

    private EventData createSingleEventWithSingleAlarm(String summary) {
        // Default to tomorrow at the same time with duration of "-PT15M"
       return createSingleEventWithSingleAlarm(summary, System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1), "-PT15M");
    }

    @SuppressWarnings("unchecked")
    private EventData createSingleEventWithSingleAlarm(String summary, Long startDate, String duration) {
        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        Attendee attendee = new Attendee();
        attendee.entity(calUser);
        attendee.cuType(CuTypeEnum.INDIVIDUAL);
        attendee.setUri("mailto:" + this.testUser.getLogin());
        singleEvent.setAttendees(Collections.singletonList(attendee));
        singleEvent.setStartDate(getDateTime(startDate));
        singleEvent.setEndDate(getDateTime(startDate + TimeUnit.HOURS.toMillis(1)));
        singleEvent.setTransp(TranspEnum.OPAQUE);
        singleEvent.setAllDay(false);

        Alarm alarm = new Alarm();
        alarm.setAction("display");
        Trigger trigger = new Trigger();
        trigger.setRelated(RelatedEnum.START);
        trigger.setDuration(duration);
        alarm.setTrigger(trigger);
        alarm.setDescription("This is the display message!");
        singleEvent.setAlarms(Collections.singletonList(alarm));
        singleEvent.setSummary(summary);
        return singleEvent;
    }

    @SuppressWarnings("unchecked")
    private EventData createSeriesEventWithSingleAlarm(String summary, Long startDate, String duration) {
        EventData seriesEvent = new EventData();
        seriesEvent.setPropertyClass("PUBLIC");
        Attendee attendee = new Attendee();
        attendee.entity(calUser);
        attendee.cuType(CuTypeEnum.INDIVIDUAL);
        attendee.setUri("mailto:" + this.testUser.getLogin());
        seriesEvent.setAttendees(Collections.singletonList(attendee));
        seriesEvent.setStartDate(getDateTime(startDate));
        seriesEvent.setEndDate(getDateTime(startDate + TimeUnit.HOURS.toMillis(1)));
        seriesEvent.setTransp(TranspEnum.OPAQUE);
        seriesEvent.setRrule("FREQ=DAILY;COUNT=4");
        seriesEvent.setAllDay(false);

        Alarm alarm = new Alarm();
        alarm.setAction("display");
        Trigger trigger = new Trigger();
        trigger.setRelated(RelatedEnum.START);
        trigger.setDuration(duration);
        alarm.setTrigger(trigger);
        alarm.setDescription("This is the display message!");
        seriesEvent.setAlarms(Collections.singletonList(alarm));
        seriesEvent.setSummary(summary);
        return seriesEvent;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderId = getDefaultFolder();
    }

    @Test
    public void testCreateSingleAlarmTrigger() throws Exception {
        // Create an event with alarm
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSingleEventWithSingleAlarm("testSingleAlarmTrigger"), false, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolderId(folderId);
        rememberEventId(eventId);
        EventResponse eventResponse = api.getEvent(session, event.getId(), folderId, null, null);
        assertNull(eventResponse.getError(), createEvent.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);
        assertNotNull(eventResponse.getData().getAlarms());
        assertEquals(1, eventResponse.getData().getAlarms().size());

        // Test alarm/until action with different time-slots
        // 1. Get alarms within the next hour
        AlarmTriggerResponse triggerResponse = api.getAlarmTrigger(session, getZuluDateTime(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)).getValue(), null, null);
        assertNull(triggerResponse.getErrorDesc(), triggerResponse.getError());
        assertNotNull(triggerResponse.getData());
        AlarmTriggerData triggers = triggerResponse.getData();
        assertTrue(triggers.size()==0); // No triggers

        // 2. Get alarms within the next two days
        triggerResponse = api.getAlarmTrigger(session, getZuluDateTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2)).getValue(), null, null);
        assertNull(triggerResponse.getErrorDesc(), triggerResponse.getError());
        assertNotNull(triggerResponse.getData());
        triggers = triggerResponse.getData();
        assertTrue(triggers.size()==1); // The created alarm
        assertTrue(triggers.get(0).getEventId().equals(eventId.getId()));
        // 3. Get only mail alarms within the next two days
        triggerResponse = api.getAlarmTrigger(session, getZuluDateTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2)).getValue(), null, "MAIL");
        assertNull(triggerResponse.getErrorDesc(), triggerResponse.getError());
        assertNotNull(triggerResponse.getData());
        triggers = triggerResponse.getData();
        assertTrue(triggers.size()==0); // No triggers

    }

    @Test
    public void testSingleEventAlarmTriggerTime() throws Exception {
        // Create an event tomorrow 12 o clock
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date startTime = cal.getTime();

        // Create an event with alarm
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSingleEventWithSingleAlarm("testSingleAlarmTriggerTime", startTime.getTime(), "-PT15M"), false, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolderId(folderId);
        rememberEventId(eventId);
        EventResponse eventResponse = api.getEvent(session, event.getId(), folderId, null, null);
        assertNull(eventResponse.getError(), createEvent.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);
        assertNotNull(eventResponse.getData().getAlarms());
        assertEquals(1, eventResponse.getData().getAlarms().size());

        // Check if next trigger is at correct time
        AlarmTriggerResponse triggerResponse = api.getAlarmTrigger(session, getZuluDateTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2)).getValue(), null, null);
        assertNull(triggerResponse.getErrorDesc(), triggerResponse.getError());
        assertNotNull(triggerResponse.getData());
        AlarmTriggerData triggers = triggerResponse.getData();
        assertTrue(triggers.size()==1); // The created alarm
        AlarmTrigger alarmTrigger = triggers.get(0);
        assertTrue(alarmTrigger.getEventId().equals(eventId.getId()));
        cal.add(Calendar.MINUTE, -15);
        Date parsedTime = ZULU_FORMATER.parse(alarmTrigger.getTime());
        assertEquals(cal.getTime().getTime(), parsedTime.getTime());
    }

    @Test
    public void testSingleEventAlarmTriggerTimeAfterUpdate() throws Exception {
        // Create an event tomorrow 12 o clock
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date startTime = cal.getTime();

        // Create an event with alarm
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSingleEventWithSingleAlarm("testSingleAlarmTriggerWithUpdate", startTime.getTime(), "-PT15M"), false, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolderId(folderId);
        rememberEventId(eventId);
        EventResponse eventResponse = api.getEvent(session, event.getId(), folderId, null, null);
        assertNull(eventResponse.getError(), createEvent.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);
        assertNotNull(eventResponse.getData().getAlarms());
        assertEquals(1, eventResponse.getData().getAlarms().size());

        // Check if next trigger is at correct time
        AlarmTriggerResponse triggerResponse = api.getAlarmTrigger(session, getZuluDateTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2)).getValue(), null, null);
        assertNull(triggerResponse.getErrorDesc(), triggerResponse.getError());
        assertNotNull(triggerResponse.getData());
        AlarmTriggerData triggers = triggerResponse.getData();
        assertTrue(triggers.size()==1); // The created alarm
        AlarmTrigger alarmTrigger = triggers.get(0);
        assertTrue(alarmTrigger.getEventId().equals(eventId.getId()));
        Calendar trigger1 = (Calendar) cal.clone();
        trigger1.add(Calendar.MINUTE, -15);
        Date parsedTime = ZULU_FORMATER.parse(alarmTrigger.getTime());
        assertEquals(trigger1.getTime().getTime(), parsedTime.getTime());

        // Shift the start time by one hour
        cal.add(Calendar.HOUR, 1);
        event.setStartDate(getDateTime(cal.getTimeInMillis()));
        event.setEndDate(getDateTime(cal.getTimeInMillis()+ TimeUnit.HOURS.toMillis(1)));
        ChronosCalendarResultResponse updateEvent = api.updateEvent(session, folderId, event.getId(), event, eventResponse.getTimestamp(), null, false, false);
        assertNull(updateEvent.getErrorDesc(), updateEvent.getError());
        assertNotNull(updateEvent.getData());

        // Check if trigger time changed accordingly
        triggerResponse = api.getAlarmTrigger(session, getZuluDateTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2)).getValue(), null, null);
        assertNull(triggerResponse.getErrorDesc(), triggerResponse.getError());
        assertNotNull(triggerResponse.getData());
        triggers = triggerResponse.getData();
        assertTrue(triggers.size()==1); // The updated alarm
        alarmTrigger = triggers.get(0);
        assertTrue(alarmTrigger.getEventId().equals(eventId.getId()));
        Calendar trigger2 = (Calendar) cal.clone();
        trigger2.add(Calendar.MINUTE, -15);
        parsedTime = ZULU_FORMATER.parse(alarmTrigger.getTime());
        assertEquals(trigger2.getTime().getTime(), parsedTime.getTime());

    }


    @Test
    public void testEventSeriesAlarmTriggerTime() throws Exception {
        // Create an event yesterday 12 o clock
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date startTime = cal.getTime();

        // Create an event with alarm
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSeriesEventWithSingleAlarm("testSeriesAlarmTriggerTime", startTime.getTime(), "-PT15M"), false, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolderId(folderId);
        rememberEventId(eventId);
        EventResponse eventResponse = api.getEvent(session, event.getId(), folderId, null, null);
        assertNull(eventResponse.getError(), createEvent.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);
        assertNotNull(eventResponse.getData().getAlarms());
        assertEquals(1, eventResponse.getData().getAlarms().size());

        // Check if next trigger is at correct time
        long currentTime = System.currentTimeMillis();
        AlarmTriggerResponse triggerResponse = api.getAlarmTrigger(session, getZuluDateTime(currentTime + TimeUnit.DAYS.toMillis(2)).getValue(), getZuluDateTime(currentTime).getValue(), null);
        assertNull(triggerResponse.getErrorDesc(), triggerResponse.getError());
        assertNotNull(triggerResponse.getData());
        AlarmTriggerData triggers = triggerResponse.getData();
        assertTrue(triggers.size()==1); // The created alarm
        AlarmTrigger alarmTrigger = triggers.get(0);
        assertTrue(alarmTrigger.getEventId().equals(eventId.getId()));
        if(currentTime < (startTime.getTime() - TimeUnit.MINUTES.toMillis(15) + TimeUnit.DAYS.toMillis(1))){
            // The next trigger is today
            cal.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            // The next trigger is tomorrow
            cal.add(Calendar.DAY_OF_MONTH, 2);
        }
        cal.add(Calendar.MINUTE, -15);
        Date parsedTime = ZULU_FORMATER.parse(alarmTrigger.getTime());
        assertEquals(cal.getTime().getTime(), parsedTime.getTime());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEventSeriesAlarmTriggerTimeRoundtripForSingleUser() throws Exception {
        /*
         *  1. Create an event series and test if the time is correct
         */

        // Create an event yesterday 12 o clock
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date startTime = cal.getTime();

        // Create an event with alarm
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSeriesEventWithSingleAlarm("testSeriesAlarmTriggerTimeRoundtrip", startTime.getTime(), "-PT15M"), false, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolderId(folderId);
        rememberEventId(eventId);
        EventResponse eventResponse = api.getEvent(session, event.getId(), folderId, null, null);
        assertNull(eventResponse.getError(), createEvent.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);
        assertNotNull(eventResponse.getData().getAlarms());
        assertEquals(1, eventResponse.getData().getAlarms().size());

        // Check if next trigger is at correct time
        long currentTime = System.currentTimeMillis();
        AlarmTriggerResponse triggerResponse = api.getAlarmTrigger(session, getZuluDateTime(currentTime + TimeUnit.DAYS.toMillis(2)).getValue(), getZuluDateTime(currentTime).getValue(), null);
        assertNull(triggerResponse.getErrorDesc(), triggerResponse.getError());
        assertNotNull(triggerResponse.getData());
        AlarmTriggerData triggers = triggerResponse.getData();
        assertEquals(1, triggers.size()); // The created alarm
        AlarmTrigger alarmTrigger = triggers.get(0);
        assertTrue(alarmTrigger.getEventId().equals(eventId.getId()));
        Calendar alarmTriggerTime = Calendar.getInstance();
        alarmTriggerTime.setTimeInMillis(cal.getTimeInMillis());
        if(currentTime < (startTime.getTime() - TimeUnit.MINUTES.toMillis(15) + TimeUnit.DAYS.toMillis(1))){
            // The next trigger is today
            alarmTriggerTime.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            // The next trigger is tomorrow
            alarmTriggerTime.add(Calendar.DAY_OF_MONTH, 2);
        }
        alarmTriggerTime.add(Calendar.MINUTE, -15);
        Date parsedTime = ZULU_FORMATER.parse(alarmTrigger.getTime());
        assertEquals(alarmTriggerTime.getTime().getTime(), parsedTime.getTime());

        /*
         *  2. create an exception for the event recurrence of the next trigger by shifting the start time by one hour
         */

        DateTimeData exceptionStartDate = addTimeToDateTimeData(event.getStartDate(), TimeUnit.DAYS.toMillis(1)+TimeUnit.HOURS.toMillis(1));
        DateTimeData exceptionEndDate = addTimeToDateTimeData(event.getEndDate(), TimeUnit.DAYS.toMillis(1)+TimeUnit.HOURS.toMillis(1));
        event.setStartDate(exceptionStartDate);
        event.setEndDate(exceptionEndDate);
        ChronosCalendarResultResponse updateEventResponse = api.updateEvent(session, folderId, event.getId(), event, eventResponse.getTimestamp(), alarmTrigger.getRecurrence(), false, false);
        assertNull(updateEventResponse.getErrorDesc(), updateEventResponse.getError());
        assertNotNull(updateEventResponse.getData());
        CalendarResult updateResult = updateEventResponse.getData();
        assertNotNull(updateResult.getCreated());
        assertEquals(1, updateResult.getCreated().size());
        EventData exceptionEvent = updateResult.getCreated().get(0);

        // Check if trigger times are correct
        triggerResponse = api.getAlarmTrigger(session, getZuluDateTime(currentTime + TimeUnit.DAYS.toMillis(2)).getValue(), getZuluDateTime(currentTime).getValue(), null);
        assertNull(triggerResponse.getErrorDesc(), triggerResponse.getError());
        assertNotNull(triggerResponse.getData());
        triggers = triggerResponse.getData();
        assertEquals(2, triggers.size()); // The alarm of the series and the alarm for the exception

        // Check the exception
        Calendar exceptionTriggerTime = Calendar.getInstance();
        exceptionTriggerTime.setTimeInMillis(alarmTriggerTime.getTimeInMillis());
        exceptionTriggerTime.add(Calendar.HOUR, 1); // Old alarm time shifted by one hour

        AlarmTrigger exceptionTrigger = triggers.get(0);
        assertTrue(exceptionTrigger.getEventId().equals(exceptionEvent.getId()));
        parsedTime = ZULU_FORMATER.parse(exceptionTrigger.getTime());
        assertEquals(exceptionTriggerTime.getTimeInMillis(), parsedTime.getTime());

        // Check the normal alarm
        Calendar newAlarmTriggerTime = Calendar.getInstance();
        newAlarmTriggerTime.setTimeInMillis(alarmTriggerTime.getTimeInMillis());
        newAlarmTriggerTime.add(Calendar.DAY_OF_MONTH, 1); // Old alarm time shifted by one day (next recurrence)

        AlarmTrigger newAlarmTrigger = triggers.get(1);
        assertTrue(newAlarmTrigger.getEventId().equals(event.getId()));
        parsedTime = ZULU_FORMATER.parse(newAlarmTrigger.getTime());
        assertEquals(newAlarmTriggerTime.getTimeInMillis(), parsedTime.getTime());


        /*
         * 3. Delete the exception
         */

        EventId toDelete = new EventId();
        toDelete.setFolderId(folderId);
        toDelete.setId(exceptionEvent.getId());
        List<EventId> singletonList = Collections.singletonList(toDelete);
        ChronosCalendarResultResponse deleteResponse = api.deleteEvent(session, updateEventResponse.getTimestamp(), singletonList);
        assertNull(deleteResponse.getErrorDesc(), deleteResponse.getError());
        assertNotNull(deleteResponse.getData());

        // Check the normal alarm
        triggerResponse = api.getAlarmTrigger(session, getZuluDateTime(currentTime + TimeUnit.DAYS.toMillis(2)).getValue(), getZuluDateTime(currentTime).getValue(), null);
        assertNull(triggerResponse.getErrorDesc(), triggerResponse.getError());
        assertNotNull(triggerResponse.getData());
        triggers = triggerResponse.getData();
        assertEquals(1, triggers.size()); // Only the alarm of the series

        AlarmTrigger seriesTrigger = triggers.get(0);
        assertTrue(seriesTrigger.getEventId().equals(event.getId()));
        parsedTime = ZULU_FORMATER.parse(seriesTrigger.getTime());
        assertEquals(newAlarmTriggerTime.getTimeInMillis(), parsedTime.getTime());

        /*
         * 4. Delete series too
         */
        toDelete = new EventId();
        toDelete.setFolderId(folderId);
        toDelete.setId(event.getId());
        singletonList = Collections.singletonList(toDelete);
        deleteResponse = api.deleteEvent(session, deleteResponse.getTimestamp(), singletonList);
        assertNull(deleteResponse.getErrorDesc(), deleteResponse.getError());
        assertNotNull(deleteResponse.getData());

        // Check the normal alarm
        triggerResponse = api.getAlarmTrigger(session, getZuluDateTime(currentTime + TimeUnit.DAYS.toMillis(2)).getValue(), getZuluDateTime(currentTime).getValue(), null);
        assertNull(triggerResponse.getErrorDesc(), triggerResponse.getError());
        assertNotNull(triggerResponse.getData());
        triggers = triggerResponse.getData();
        assertEquals(0, triggers.size()); // No upcoming triggers
    }

}
