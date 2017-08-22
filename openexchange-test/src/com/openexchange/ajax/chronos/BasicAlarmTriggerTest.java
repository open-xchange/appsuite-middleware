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
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.AlarmTrigger;
import com.openexchange.testing.httpclient.models.AlarmTriggerData;
import com.openexchange.testing.httpclient.models.AlarmTriggerResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.CalendarResult;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
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

    /**
     * Checks if a response doesn't contain any errors
     *
     * @param error The error element of the response
     * @param errorDesc The error description element of the response
     * @param data The data element of the response
     * @return The data
     */
    private <T> T checkResponse(String error, String errorDesc, T data) {
        assertNull(errorDesc, error);
        assertNotNull(data);
        return data;
    }

    /**
     * Simple wrapper to return alarm triggers
     *
     * @param from The lower limit
     * @param unit The unit for the upper limit calculation
     * @param value The amount for the upper limit calculation
     * @param actions The alarm actions to retrieve or null to retrieve the basic ones
     * @return The {@link AlarmTriggerResponse}
     * @throws ApiException
     */
    private AlarmTriggerResponse getAlarmTrigger(long from, TimeUnit unit, int value, String actions) throws ApiException {
        return api.getAlarmTrigger(session, getZuluDateTime(from + unit.toMillis(value)).getValue(), getZuluDateTime(from).getValue(), actions);
    }

    /**
     * Shifts a given event by the given amount
     *
     * @param eventId The event id
     * @param recurrence The recurrence id or null
     * @param event The event data to change
     * @param startTime The start time of the event
     * @param unit The unit of the shift
     * @param value The shifting amount
     * @param timestamp The timestamp of the last request
     * @return The {@link CalendarResult}
     * @throws ApiException
     */
    private CalendarResult shiftEvent(String eventId, String recurrence, EventData event, long startTime, TimeUnit unit, int value, long timestamp) throws ApiException {
        long newStartTime = startTime + unit.toMillis(value);
        event.setStartDate(getDateTime(newStartTime));
        event.setEndDate(getDateTime(newStartTime + TimeUnit.HOURS.toMillis(1)));
        ChronosCalendarResultResponse updateEvent = api.updateEvent(session, folderId, eventId, event, timestamp, recurrence, false, false);
        this.setLastTimestamp(updateEvent.getTimestamp());
        return checkResponse(updateEvent.getError(), updateEvent.getErrorDesc(), updateEvent.getData());
    }

    /**
     * Checks if a given event exists and if it contains the giben amount of alarm objects
     *
     * @param event The event to check
     * @param alarmSize The amount of alarm object
     * @throws ApiException
     */
    private void getAndCheckEvent(EventData event, int alarmSize) throws ApiException{
        EventResponse eventResponse = api.getEvent(session, event.getId(), folderId, null, null);
        EventData getEvent = checkResponse(eventResponse.getError(), eventResponse.getErrorDesc(), eventResponse.getData());
        EventUtil.compare(event, getEvent, true);
        assertNotNull(getEvent.getAlarms());
        assertEquals(alarmSize, getEvent.getAlarms().size());
        this.setLastTimestamp(eventResponse.getTimestamp());
    }

    /**
     * Retrieves alarm triggers from the given time until the given time (calculated by unit and value) and checks if the response contains the correct amount of alarm trigger objects.
     * Its also possible to filter for specific actions.
     *
     * @param from The lower limit
     * @param unit The unit to add to the lower limit
     * @param value The amount of the unit to add
     * @param actions The actions to retrieve
     * @param expected The expected amount of alarm objects
     * @return The {@link AlarmTriggerData}
     * @throws ApiException
     */
    private AlarmTriggerData getAndCheckAlarmTrigger(long from, TimeUnit unit, int value, String actions, int expected) throws ApiException {
        AlarmTriggerResponse triggerResponse = getAlarmTrigger(from, unit, value, actions);
        AlarmTriggerData triggers = checkResponse(triggerResponse.getError(), triggerResponse.getErrorDesc(), triggerResponse.getData());
        assertTrue(triggers.size() == expected);
        return triggers;
    }

    /**
     * Retrieves alarm triggers from the given time until two days and checks if the response contains the correct amount of alarm trigger objects
     *
     * @param from The lower limit of the request
     * @param expected The amount of expected alarm trigger objects
     * @return The {@link AlarmTriggerData}
     * @throws ApiException
     */
    private AlarmTriggerData getAndCheckAlarmTrigger(long from, int expected) throws ApiException {
        AlarmTriggerResponse triggerResponse = getAlarmTrigger(from, TimeUnit.DAYS, 2, null);
        AlarmTriggerData triggers = checkResponse(triggerResponse.getError(), triggerResponse.getErrorDesc(), triggerResponse.getData());
        assertTrue(triggers.size() == expected);
        return triggers;
    }

    /**
     * Returns an {@link Calendar} object with time set to today 12 o clock and timezone set to 'utc'
     * @return The calendar
     */
    private Calendar getUTCCalendar(){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Creates a single event with the given start time and name.
     * Also handles all necessary tests and remembers the event for later deletion.
     *
     * @param name The name of the event
     * @param startTime The startTime of the event
     * @return The created event
     * @throws ApiException
     */
    private EventData createSingleEvent(String name, long startTime) throws ApiException{
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSingleEventWithSingleAlarm(name, startTime, "-PT15M"), false, false);
        return handleCreation(createEvent);
    }

    /**
     * Creates a event series with the given start time and name.
     * Also handles all necessary tests and remembers the event for later deletion.
     *
     * @param name The name of the event
     * @param startTime The startTime of the event
     * @return The created event
     * @throws ApiException
     */
    private EventData createSeriesEvent(String name, long startTime) throws ApiException{
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSeriesEventWithSingleAlarm(name, startTime, "-PT15M"), false, false);
        return handleCreation(createEvent);
    }

    /**
     * Handles the result response of an event creation
     *
     * @param createEvent The result
     * @return The created event
     */
    private EventData handleCreation(ChronosCalendarResultResponse createEvent){
        CalendarResult result = checkResponse(createEvent.getError(), createEvent.getErrorDesc(), createEvent.getData());
        EventData event = result.getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolderId(folderId);
        rememberEventId(eventId);
        this.setLastTimestamp(createEvent.getTimestamp());
        return event;
    }

    /**
     * Checks if the trigger is related to the given event and if the trigger time is correct
     *
     * @param trigger The trigger
     * @param eventId The event id
     * @param expectedTime The expected trigger time
     * @throws ParseException
     */
    private void checkAlarmTime(AlarmTrigger trigger, String eventId, long expectedTime) throws ParseException{
        assertTrue(trigger.getEventId().equals(eventId));
        Date parsedTime = ZULU_FORMATER.parse(trigger.getTime());
        assertEquals(expectedTime, parsedTime.getTime());
    }


    @Test
    public void testCreateSingleAlarmTrigger() throws Exception {
        // Create an event with alarm
        long currentTime = System.currentTimeMillis();

        EventData event = createSingleEvent("testSingleAlarmTrigger", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        getAndCheckEvent(event, 1);

        // Test alarm/until action with different time-slots
        // 1. Get alarms within the next hour
        getAndCheckAlarmTrigger(currentTime, TimeUnit.HOURS, 1, null, 0); // No triggers

        // 2. Get alarms within the next two days
        AlarmTriggerData triggers = getAndCheckAlarmTrigger(currentTime, 1); // No triggers
        assertTrue(triggers.get(0).getEventId().equals(event.getId()));
        // 3. Get only mail alarms within the next two days
        getAndCheckAlarmTrigger(currentTime, TimeUnit.DAYS, 2, "MAIL", 0); // No triggers

    }

    @Test
    public void testSingleEventAlarmTriggerTime() throws Exception {
        // Create an event tomorrow 12 o clock

        Calendar cal = getUTCCalendar();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date startTime = cal.getTime();

        long currentTime = System.currentTimeMillis();

        // Create an event with alarm
        EventData event = createSingleEvent("testSingleAlarmTriggerTime", startTime.getTime());

        getAndCheckEvent(event, 1);

        // Check if next trigger is at correct time
        AlarmTriggerData triggers = getAndCheckAlarmTrigger(currentTime, 1); // No triggers
        cal.add(Calendar.MINUTE, -15);
        checkAlarmTime(triggers.get(0), event.getId(), cal.getTimeInMillis());
    }

    @Test
    public void testSingleEventAlarmTriggerTimeAfterUpdate() throws Exception {
        // Create an event tomorrow 12 o clock
        Calendar cal = getUTCCalendar();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date startTime = cal.getTime();

        long currentTime = System.currentTimeMillis();

        // Create an event with alarm
        EventData event = createSingleEvent("testSingleAlarmTriggerWithUpdate", startTime.getTime());
        getAndCheckEvent(event, 1);

        // Check if next trigger is at correct time
        AlarmTriggerData triggers = getAndCheckAlarmTrigger(currentTime, 1); // No triggers
        Calendar trigger1 = (Calendar) cal.clone();
        trigger1.add(Calendar.MINUTE, -15);
        checkAlarmTime(triggers.get(0), event.getId(), trigger1.getTimeInMillis());

        // Shift the start time by one hour
        shiftEvent(event.getId(), null, event, cal.getTimeInMillis(), TimeUnit.HOURS, 1, getLastTimestamp());

        // Check if trigger time changed accordingly
        triggers = getAndCheckAlarmTrigger(currentTime, 1); // No triggers
        Calendar trigger2 = (Calendar) cal.clone();
        trigger2.add(Calendar.HOUR, 1);
        trigger2.add(Calendar.MINUTE, -15);
        checkAlarmTime(triggers.get(0), event.getId(), trigger2.getTimeInMillis());

    }

    @Test
    public void testEventSeriesAlarmTriggerTime() throws Exception {
        // Create an event yesterday 12 o clock
        Calendar cal = getUTCCalendar();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date startTime = cal.getTime();

        // Create an event with alarm
        EventData event = createSeriesEvent("testSeriesAlarmTriggerTime", startTime.getTime());
        getAndCheckEvent(event, 1);

        // Check if next trigger is at correct time
        long currentTime = System.currentTimeMillis();
        AlarmTriggerData triggers = getAndCheckAlarmTrigger(currentTime, 1); // No triggers
        AlarmTrigger alarmTrigger = triggers.get(0);
        if (currentTime < (startTime.getTime() - TimeUnit.MINUTES.toMillis(15) + TimeUnit.DAYS.toMillis(1))) {
            // The next trigger is today
            cal.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            // The next trigger is tomorrow
            cal.add(Calendar.DAY_OF_MONTH, 2);
        }
        cal.add(Calendar.MINUTE, -15);
        checkAlarmTime(alarmTrigger, event.getId(), cal.getTimeInMillis());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEventSeriesAlarmTriggerTimeRoundtripForSingleUser() throws Exception {

        /*
         * 1. Create an event series and test if the time is correct
         */

        // Create an event yesterday 12 o clock
        Calendar cal = getUTCCalendar();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date startTime = cal.getTime();

        // Create an event with alarm
        EventData event = createSeriesEvent("testSeriesAlarmTriggerTimeRoundtrip", startTime.getTime());
        getAndCheckEvent(event, 1);

        // Check if next trigger is at correct time
        long currentTime = System.currentTimeMillis();
        AlarmTriggerData triggers = getAndCheckAlarmTrigger(currentTime, 1); // The created alarm
        AlarmTrigger alarmTrigger = triggers.get(0);
        assertTrue(alarmTrigger.getEventId().equals(event.getId()));
        Calendar eventTime = Calendar.getInstance();
        eventTime.setTimeInMillis(cal.getTimeInMillis());
        if (currentTime < (startTime.getTime() - TimeUnit.MINUTES.toMillis(15) + TimeUnit.DAYS.toMillis(1))) {
            // The next trigger is today
            eventTime.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            // The next trigger is tomorrow
            eventTime.add(Calendar.DAY_OF_MONTH, 2);
        }
        Calendar alarmTriggerTime = Calendar.getInstance();
        alarmTriggerTime.setTime(eventTime.getTime());
        alarmTriggerTime.add(Calendar.MINUTE, -15);
        checkAlarmTime(triggers.get(0), event.getId(), alarmTriggerTime.getTimeInMillis());

        /*
         * 2. create an exception for the event recurrence of the next trigger by shifting the start time by one hour
         */
        CalendarResult updateResult = shiftEvent(event.getId(), alarmTrigger.getRecurrence(), event, eventTime.getTimeInMillis(), TimeUnit.HOURS, 1, getLastTimestamp());
        assertNotNull(updateResult.getCreated());
        assertEquals(1, updateResult.getCreated().size());
        EventData exceptionEvent = updateResult.getCreated().get(0);

        // Check if trigger times are correct
        triggers = getAndCheckAlarmTrigger(currentTime, 2); // The alarm of the series and the alarm for the exception

        // Check the exception
        Calendar exceptionTriggerTime = Calendar.getInstance();
        exceptionTriggerTime.setTimeInMillis(alarmTriggerTime.getTimeInMillis());
        exceptionTriggerTime.add(Calendar.HOUR, 1); // Old alarm time shifted by one hour
        checkAlarmTime(triggers.get(0), exceptionEvent.getId(), exceptionTriggerTime.getTimeInMillis());

        // Check the normal alarm
        Calendar newAlarmTriggerTime = Calendar.getInstance();
        newAlarmTriggerTime.setTimeInMillis(alarmTriggerTime.getTimeInMillis());
        newAlarmTriggerTime.add(Calendar.DAY_OF_MONTH, 1); // Old alarm time shifted by one day (next recurrence)
        checkAlarmTime(triggers.get(1), event.getId(), newAlarmTriggerTime.getTimeInMillis());

        /*
         * 3. Delete the exception
         */
        EventId toDelete = new EventId();
        toDelete.setFolderId(folderId);
        toDelete.setId(exceptionEvent.getId());
        List<EventId> singletonList = Collections.singletonList(toDelete);
        ChronosCalendarResultResponse deleteResponse = api.deleteEvent(session, getLastTimestamp(), singletonList);
        checkResponse(deleteResponse.getError(), deleteResponse.getErrorDesc(), deleteResponse.getData());

        // Check the normal alarm
        triggers = getAndCheckAlarmTrigger(currentTime, 1); // Only the alarm of the series
        checkAlarmTime(triggers.get(0), event.getId(), newAlarmTriggerTime.getTimeInMillis());

        /*
         * 4. Delete series too
         */
        toDelete = new EventId();
        toDelete.setFolderId(folderId);
        toDelete.setId(event.getId());
        singletonList = Collections.singletonList(toDelete);
        deleteResponse = api.deleteEvent(session, deleteResponse.getTimestamp(), singletonList);
        checkResponse(deleteResponse.getError(), deleteResponse.getErrorDesc(), deleteResponse.getData());

        // Check the normal alarm
        getAndCheckAlarmTrigger(currentTime, 0); // No upcoming triggers
    }

}
