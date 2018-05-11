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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.AlarmTrigger;
import com.openexchange.testing.httpclient.models.AlarmTriggerData;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.AttendeeAndAlarm;
import com.openexchange.testing.httpclient.models.CalendarResult;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.Trigger.RelatedEnum;
import com.openexchange.testing.httpclient.models.UpdatesResult;

/**
 *
 * {@link BasicAlarmTriggerTest} tests alarm triggers.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class BasicAlarmTriggerTest extends AbstractUserTimezoneAlarmTriggerTest {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Test
    public void testCreateSingleAlarmTrigger() throws Exception {
        int currentTriggers = getAlarmTriggers().size();
        // Create an event with alarm
        long currentTime = System.currentTimeMillis();
        DateTimeData startDate = DateTimeUtil.getDateTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        DateTimeData endDate = DateTimeUtil.getDateTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(2));

        EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser(), "testCreateSingleAlarmTrigger", startDate, endDate, AlarmFactory.createDisplayAlarm("-PT15M"), folderId);
        EventData event = eventManager.createEvent(toCreate);
        getAndAssertAlarms(event, 1, folderId);

        // Test alarm/until action with different time-slots
        // 1. Get alarms within the next hour
        getAndCheckAlarmTrigger(currentTime + TimeUnit.HOURS.toMillis(1), null, 0); // No triggers

        // 2. Get alarms within the next two days
        AlarmTriggerData triggers = getAndCheckAlarmTrigger(1 + currentTriggers); // One trigger

        AlarmTrigger alarmTrigger = null;
        for (AlarmTrigger trigger : triggers) {
            if (trigger.getFolder().equalsIgnoreCase(folderId)) {
                alarmTrigger = trigger;
                break;
            }
        }

        assertTrue(alarmTrigger.getEventId().equals(event.getId()));
        // 3. Get only mail alarms within the next two days
        getAndCheckAlarmTrigger(currentTime + TimeUnit.DAYS.toMillis(2), "MAIL", 0); // No triggers

    }

    @Test
    public void testSingleEventAlarmTriggerTime() throws Exception {
        int currentTriggers = getAlarmTriggers().size();

        // Create an event tomorrow 12 o clock
        Calendar cal = DateTimeUtil.getUTCCalendar();
        cal.add(Calendar.DAY_OF_MONTH, 1);

        DateTimeData startDate = DateTimeUtil.getDateTime(cal);
        DateTimeData endDate = DateTimeUtil.incrementDateTimeData(startDate, TimeUnit.HOURS.toMillis(1));

        // Create an event with alarm
        EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser(), "testSingleEventAlarmTriggerTime", startDate, endDate, AlarmFactory.createDisplayAlarm("-PT15M"), folderId);
        EventData event = eventManager.createEvent(toCreate);
        getAndAssertAlarms(event, 1, folderId);

        Calendar today = Calendar.getInstance(UTC);
        today.setTime(new Date());

        // Check if next trigger is at correct time
        AlarmTriggerData triggers = getAndCheckAlarmTrigger(1 + currentTriggers); // No triggers

        AlarmTrigger alarmTrigger = null;
        for (AlarmTrigger trigger : triggers) {
            if (trigger.getFolder().equalsIgnoreCase(folderId)) {
                alarmTrigger = trigger;
                break;
            }
        }

        cal.add(Calendar.MINUTE, -15);
        checkAlarmTime(alarmTrigger, event.getId(), cal.getTimeInMillis());
    }

    @Test
    public void testSingleEventAlarmTriggerTimeAfterUpdate() throws Exception {
        int currentTriggers = getAlarmTriggers().size();

        // Create an event tomorrow 12 o clock
        Calendar cal = DateTimeUtil.getUTCCalendar();
        cal.add(Calendar.DAY_OF_MONTH, 1);

        DateTimeData startDate = DateTimeUtil.getDateTime(cal);
        DateTimeData endDate = DateTimeUtil.incrementDateTimeData(startDate, TimeUnit.HOURS.toMillis(1));

        // Create an event with alarm
        EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser(), "testSingleEventAlarmTriggerTimeAfterUpdate", startDate, endDate, AlarmFactory.createDisplayAlarm("-PT15M"), folderId);
        EventData event = eventManager.createEvent(toCreate);
        getAndAssertAlarms(event, 1, folderId);

        Calendar today = Calendar.getInstance(UTC);
        today.setTime(new Date());

        // Check if next trigger is at correct time
        AlarmTriggerData triggers = getAndCheckAlarmTrigger(1 + currentTriggers);
        Calendar trigger1 = (Calendar) cal.clone();
        trigger1.add(Calendar.MINUTE, -15);

        AlarmTrigger alarmTrigger = null;
        for (AlarmTrigger trigger : triggers) {
            if (trigger.getFolder().equalsIgnoreCase(folderId)) {
                alarmTrigger = trigger;
                break;
            }
        }

        checkAlarmTime(alarmTrigger, event.getId(), trigger1.getTimeInMillis());

        // Shift the start time by one hour
        eventManager.shiftEvent(event.getId(), null, event, cal, TimeUnit.HOURS, 1, eventManager.getLastTimeStamp());

        // Check if trigger time changed accordingly
        triggers = getAndCheckAlarmTrigger(1 + currentTriggers);
        Calendar trigger2 = (Calendar) cal.clone();
        trigger2.add(Calendar.HOUR, 1);
        trigger2.add(Calendar.MINUTE, -15);

        alarmTrigger = null;
        for (AlarmTrigger trigger : triggers) {
            if (trigger.getFolder().equalsIgnoreCase(folderId)) {
                alarmTrigger = trigger;
                break;
            }
        }

        checkAlarmTime(alarmTrigger, event.getId(), trigger2.getTimeInMillis());
    }

    @Test
    public void testEventSeriesAlarmTriggerTime() throws Exception {
        // Create an event yesterday 12 o clock
        Calendar cal = DateTimeUtil.getUTCCalendar();
        cal.add(Calendar.DAY_OF_MONTH, -1);

        DateTimeData startDate = DateTimeUtil.getDateTime(cal);
        DateTimeData endDate = DateTimeUtil.incrementDateTimeData(startDate, TimeUnit.HOURS.toMillis(1));

        // Create an event with alarm
        EventData toCreate = EventFactory.createSeriesEvent(defaultUserApi.getCalUser(), "testSeriesAlarmTriggerTime", startDate, endDate, 4, folderId);
        toCreate.setAlarms(Collections.singletonList(AlarmFactory.createDisplayAlarm("-PT15M")));

        EventData event = eventManager.createEvent(toCreate);
        getAndAssertAlarms(event, 1, folderId);

        // Check if next trigger is at correct time
        long currentTime = System.currentTimeMillis();
        AlarmTriggerData triggers = getAndCheckAlarmTrigger(1); // No triggers
        AlarmTrigger alarmTrigger = triggers.get(0);
        if (currentTime < (cal.getTimeInMillis() - TimeUnit.MINUTES.toMillis(15) + TimeUnit.DAYS.toMillis(1))) {
            // The next trigger is today
            cal.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            // The next trigger is tomorrow
            cal.add(Calendar.DAY_OF_MONTH, 2);
        }
        cal.add(Calendar.MINUTE, -15);
        checkAlarmTime(alarmTrigger, event.getId(), cal.getTimeInMillis());
    }

    @Test
    public void testEventSeriesAlarmTriggerTimeRoundtripForSingleUser() throws Exception {

        /*
         * 1. Create an event series and test if the time is correct
         */

        // Create an event yesterday 12 o clock
        Calendar cal = DateTimeUtil.getUTCCalendar();
        cal.add(Calendar.DAY_OF_MONTH, -1);

        DateTimeData startDate = DateTimeUtil.getDateTime(cal);
        DateTimeData endDate = DateTimeUtil.incrementDateTimeData(startDate, TimeUnit.HOURS.toMillis(1));

        // Create an event with alarm
        EventData toCreate = EventFactory.createSeriesEvent(defaultUserApi.getCalUser(), "testSeriesAlarmTriggerTimeRoundtrip", startDate, endDate, 4, folderId);
        toCreate.setAlarms(Collections.singletonList(AlarmFactory.createDisplayAlarm("-PT15M")));

        EventData event = eventManager.createEvent(toCreate);
        getAndAssertAlarms(event, 1, folderId);

        // Check if next trigger is at correct time
        long currentTime = System.currentTimeMillis();
        AlarmTriggerData triggers = getAndCheckAlarmTrigger(1); // The created alarm
        AlarmTrigger alarmTrigger = triggers.get(0);
        assertTrue(alarmTrigger.getEventId().equals(event.getId()));
        Calendar eventTime = Calendar.getInstance(UTC);
        eventTime.setTimeInMillis(cal.getTimeInMillis());
        if (currentTime < (cal.getTimeInMillis() - TimeUnit.MINUTES.toMillis(15) + TimeUnit.DAYS.toMillis(1))) {
            // The next trigger is today
            eventTime.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            // The next trigger is tomorrow
            eventTime.add(Calendar.DAY_OF_MONTH, 2);
        }
        Calendar alarmTriggerTime = Calendar.getInstance(UTC);
        alarmTriggerTime.setTime(eventTime.getTime());
        alarmTriggerTime.add(Calendar.MINUTE, -15);
        checkAlarmTime(triggers.get(0), event.getId(), alarmTriggerTime.getTimeInMillis());

        /*
         * 2. create an exception for the event recurrence of the next trigger by shifting the start time by one hour
         */
        CalendarResult updateResult = eventManager.shiftEvent(event.getId(), alarmTrigger.getRecurrenceId(), event, eventTime, TimeUnit.HOURS, 1, eventManager.getLastTimeStamp());
        assertNotNull(updateResult.getCreated());
        assertEquals(1, updateResult.getCreated().size());
        EventData exceptionEvent = updateResult.getCreated().get(0);

        // Check if trigger times are correct
        triggers = getAndCheckAlarmTrigger(2); // The alarm of the series and the alarm for the exception

        // Check the exception
        Calendar exceptionTriggerTime = Calendar.getInstance(UTC);
        exceptionTriggerTime.setTimeInMillis(alarmTriggerTime.getTimeInMillis());
        exceptionTriggerTime.add(Calendar.HOUR, 1); // Old alarm time shifted by one hour
        checkAlarmTime(triggers.get(0), exceptionEvent.getId(), exceptionTriggerTime.getTimeInMillis());

        // Check the normal alarm
        Calendar newAlarmTriggerTime = Calendar.getInstance(UTC);
        newAlarmTriggerTime.setTimeInMillis(alarmTriggerTime.getTimeInMillis());
        newAlarmTriggerTime.add(Calendar.DAY_OF_MONTH, 1); // Old alarm time shifted by one day (next recurrence)
        checkAlarmTime(triggers.get(1), event.getId(), newAlarmTriggerTime.getTimeInMillis());

        /*
         * 3. Delete the exception
         */
        EventId toDelete = new EventId();
        toDelete.setFolder(exceptionEvent.getFolder());
        toDelete.setId(exceptionEvent.getId());
        eventManager.deleteEvent(toDelete);

        // Check the normal alarm
        triggers = getAndCheckAlarmTrigger(1); // Only the alarm of the series
        checkAlarmTime(triggers.get(0), event.getId(), newAlarmTriggerTime.getTimeInMillis());

        /*
         * 4. Delete series too
         */
        toDelete = new EventId();
        toDelete.setFolder(exceptionEvent.getFolder());
        toDelete.setId(event.getId());
        eventManager.deleteEvent(toDelete);

        // Check the normal alarm
        getAndCheckAlarmTrigger(0); // No upcoming triggers
    }

    @Test
    public void testEventSeriesAlarmTriggerTimeRoundtripForMultipleUser() throws Exception {
        int currentTriggers = getAlarmTriggers().size();

        /*
         * 1. Create an event series with two attendees and test if the times are correct
         */

        // Create an event yesterday 12 o clock
        Calendar cal = DateTimeUtil.getUTCCalendar();
        cal.add(Calendar.DAY_OF_MONTH, -1);

        DateTimeData startDate = DateTimeUtil.getDateTime(cal);
        DateTimeData endDate = DateTimeUtil.incrementDateTimeData(startDate, TimeUnit.HOURS.toMillis(1));

        // Create an event with alarm and two attendees
        ArrayList<Attendee> atts = new ArrayList<>(2);
        Attendee attendee1 = AttendeeFactory.createIndividual(defaultUserApi.getCalUser());
        atts.add(attendee1);
        Attendee attendee2 = AttendeeFactory.createIndividual(user2.getCalUser());
        atts.add(attendee2);

        // Create an event with alarm
        EventData toCreate = EventFactory.createSeriesEvent(defaultUserApi.getCalUser(), "testSeriesAlarmTriggerTimeRoundtrip", startDate, endDate, 4, folderId);
        toCreate.setAlarms(Collections.singletonList(AlarmFactory.createDisplayAlarm("-PT15M")));
        toCreate.setAttendees(atts);

        Date time = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        EventData event = eventManager.createEvent(toCreate);
        getAndAssertAlarms(event, 1, folderId);

        UpdatesResult updatesResult = eventManager2.getUpdates(time);
        if (updatesResult.getNewAndModified().size() == 0) {
            // The event is maybe not processed yet. Wait 10 seconds and try again
            Thread.sleep(10 * 1000);
            updatesResult = eventManager2.getUpdates(time);
        }
        assertEquals(1, updatesResult.getNewAndModified().size());
        EventData eventU2 = updatesResult.getNewAndModified().get(0);

        // Check if next trigger is at correct time
        long currentTime = System.currentTimeMillis();
        AlarmTriggerData triggers = getAndCheckAlarmTrigger(1 + currentTriggers); // The created alarm
        AlarmTrigger alarmTrigger = null;
        for (AlarmTrigger trigger : triggers) {
            if (trigger.getFolder().equalsIgnoreCase(folderId)) {
                alarmTrigger = trigger;
                break;
            }
        }
        Calendar eventTime = Calendar.getInstance(UTC);
        eventTime.setTimeInMillis(cal.getTimeInMillis());
        if (currentTime < (cal.getTimeInMillis() - TimeUnit.MINUTES.toMillis(15) + TimeUnit.DAYS.toMillis(1))) {
            // The next trigger is today
            eventTime.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            // The next trigger is tomorrow
            eventTime.add(Calendar.DAY_OF_MONTH, 2);
        }
        Calendar alarmTriggerTime = Calendar.getInstance(UTC);
        alarmTriggerTime.setTime(eventTime.getTime());
        alarmTriggerTime.add(Calendar.MINUTE, -15);
        checkAlarmTime(alarmTrigger, event.getId(), alarmTriggerTime.getTimeInMillis());

        // User 2 shouldn't have any triggers
        triggers = eventManager2.getAlarmTrigger(currentTime + TimeUnit.DAYS.toMillis(2));
        assertEquals(0, triggers.size());

        /*
         * 2. Accept the event with user 2
         */
        AttendeeAndAlarm body = new AttendeeAndAlarm();
        attendee2.setPartStat("ACCEPTED");
        attendee2.setMember(null);
        body.attendee(attendee2);

        body.addAlarmsItem(AlarmFactory.createAlarm("-PT20M", RelatedEnum.START));
        eventManager2.updateAttendee(eventU2.getId(), body, false);

        // Now user 2 should have a trigger
        triggers = eventManager2.getAlarmTrigger(currentTime + TimeUnit.DAYS.toMillis(2));
        assertEquals(1, triggers.size());
        checkAlarmTime(triggers.get(0), event.getId(), alarmTriggerTime.getTimeInMillis() - TimeUnit.MINUTES.toMillis(5));

        /*
         * 3. create an exception for the event recurrence of the next trigger by shifting the start time by one hour
         */
        CalendarResult updateResult = eventManager.shiftEvent(event.getId(), alarmTrigger.getRecurrenceId(), event, eventTime, TimeUnit.HOURS, 1, System.currentTimeMillis());
        assertNotNull(updateResult.getCreated());
        assertEquals(1, updateResult.getCreated().size());
        EventData exceptionEvent = updateResult.getCreated().get(0);

        // Check if trigger times are correct
        triggers = getAndCheckAlarmTrigger(2 + currentTriggers); // The alarm of the series and the alarm for the exception

        alarmTrigger = null;
        AlarmTrigger alarmTrigger2 = null;
        for (AlarmTrigger trigger : triggers) {
            if (trigger.getFolder().equalsIgnoreCase(folderId)) {
                if (alarmTrigger == null) {
                    alarmTrigger = trigger;
                } else if (alarmTrigger2 == null) {
                    alarmTrigger2 = trigger;
                    break;
                }
            }
        }

        // Check the exception
        Calendar exceptionTriggerTime = Calendar.getInstance(UTC);
        exceptionTriggerTime.setTimeInMillis(alarmTriggerTime.getTimeInMillis());
        exceptionTriggerTime.add(Calendar.HOUR, 1); // Old alarm time shifted by one hour
        checkAlarmTime(alarmTrigger, exceptionEvent.getId(), exceptionTriggerTime.getTimeInMillis());

        // Check the normal alarm
        Calendar newAlarmTriggerTime = Calendar.getInstance(UTC);
        newAlarmTriggerTime.setTimeInMillis(alarmTriggerTime.getTimeInMillis());
        newAlarmTriggerTime.add(Calendar.DAY_OF_MONTH, 1); // Old alarm time shifted by one day (next recurrence)
        checkAlarmTime(alarmTrigger2, event.getId(), newAlarmTriggerTime.getTimeInMillis());

        // Check user 2 too
        triggers = eventManager2.getAlarmTrigger(currentTime + TimeUnit.DAYS.toMillis(2));
        assertEquals(2, triggers.size());
        checkAlarmTime(triggers.get(0), exceptionEvent.getId(), exceptionTriggerTime.getTimeInMillis() - TimeUnit.MINUTES.toMillis(5));
        checkAlarmTime(triggers.get(1), event.getId(), newAlarmTriggerTime.getTimeInMillis() - TimeUnit.MINUTES.toMillis(5));

        /*
         * 4. Remove user 2 from exception
         */
        List<Attendee> attendees = exceptionEvent.getAttendees();
        Attendee removed;
        if (attendees.get(0).getEntity() == defaultUserApi.getCalUser()) {
            removed = attendees.remove(1);
        } else {
            removed = attendees.remove(0);
        }
        EventData updateEvent = eventManager.updateEvent(exceptionEvent);

        // Check again if trigger times are correct
        triggers = getAndCheckAlarmTrigger(2 + currentTriggers); // The alarm of the series and the alarm for the exception

        alarmTrigger = null;
        alarmTrigger2 = null;
        for (AlarmTrigger trigger : triggers) {
            if (trigger.getFolder().equalsIgnoreCase(folderId)) {
                if (alarmTrigger == null) {
                    alarmTrigger = trigger;
                } else if (alarmTrigger2 == null) {
                    alarmTrigger2 = trigger;
                    break;
                }
            }
        }

        // Check the exception
        checkAlarmTime(alarmTrigger, exceptionEvent.getId(), exceptionTriggerTime.getTimeInMillis());

        // Check the normal alarm
        checkAlarmTime(alarmTrigger2, event.getId(), newAlarmTriggerTime.getTimeInMillis());

        // Check user 2 too
        triggers = eventManager2.getAlarmTrigger(currentTime + TimeUnit.DAYS.toMillis(2));
        assertEquals(1, triggers.size());
        checkAlarmTime(triggers.get(0), event.getId(), newAlarmTriggerTime.getTimeInMillis() - TimeUnit.MINUTES.toMillis(5));

        /*
         * 5. Re-add user 2
         */
        attendees.add(removed);
        exceptionEvent.setLastModified(updateEvent.getLastModified());
        eventManager.updateEvent(exceptionEvent);
        eventManager2.setLastTimeStamp(eventManager.getLastTimeStamp());

        // Check again if trigger times are correct
        triggers = getAndCheckAlarmTrigger(2 + currentTriggers); // The alarm of the series and the alarm for the exception

        alarmTrigger = null;
        alarmTrigger2 = null;
        for (AlarmTrigger trigger : triggers) {
            if (trigger.getFolder().equalsIgnoreCase(folderId)) {
                if (alarmTrigger == null) {
                    alarmTrigger = trigger;
                } else if (alarmTrigger2 == null) {
                    alarmTrigger2 = trigger;
                    break;
                }
            }
        }

        // Check the exception
        checkAlarmTime(alarmTrigger, exceptionEvent.getId(), exceptionTriggerTime.getTimeInMillis());

        // Check the normal alarm
        checkAlarmTime(alarmTrigger2, event.getId(), newAlarmTriggerTime.getTimeInMillis());

        // Check user 2 too
        triggers = eventManager2.getAlarmTrigger(currentTime + TimeUnit.DAYS.toMillis(2));
        assertEquals(1, triggers.size());
        checkAlarmTime(triggers.get(0), event.getId(), newAlarmTriggerTime.getTimeInMillis() - TimeUnit.MINUTES.toMillis(5));

        /*
         * 6. Accept exception with user 2
         */
        body = new AttendeeAndAlarm();
        attendee2.setPartStat("ACCEPTED");
        body.attendee(attendee2);

        body.addAlarmsItem(AlarmFactory.createAlarm("-PT20M", RelatedEnum.START));
        eventManager2.updateAttendee(exceptionEvent.getId(), body, false);

        // Now user 2 should have a trigger again
        triggers = eventManager2.getAlarmTrigger(currentTime + TimeUnit.DAYS.toMillis(2));
        assertEquals(2, triggers.size());
        checkAlarmTime(triggers.get(0), exceptionEvent.getId(), exceptionTriggerTime.getTimeInMillis() - TimeUnit.MINUTES.toMillis(5));
        checkAlarmTime(triggers.get(1), event.getId(), newAlarmTriggerTime.getTimeInMillis() - TimeUnit.MINUTES.toMillis(5));

        /*
         * 7. Delete the exception
         */
        EventId toDelete = new EventId();
        toDelete.setFolder(exceptionEvent.getFolder());
        toDelete.setId(exceptionEvent.getId());
        eventManager.deleteEvent(toDelete);

        // Check the normal alarm
        triggers = getAndCheckAlarmTrigger(1 + currentTriggers); // Only the alarm of the series

        alarmTrigger = null;
        for (AlarmTrigger trigger : triggers) {
            if (trigger.getFolder().equalsIgnoreCase(folderId)) {
                alarmTrigger = trigger;
                break;
            }
        }

        checkAlarmTime(alarmTrigger, event.getId(), newAlarmTriggerTime.getTimeInMillis());

        // check user 2
        triggers = eventManager2.getAlarmTrigger(currentTime + TimeUnit.DAYS.toMillis(2));
        assertEquals(1, triggers.size());
        checkAlarmTime(triggers.get(0), event.getId(), newAlarmTriggerTime.getTimeInMillis() - TimeUnit.MINUTES.toMillis(5));

        /*
         * 8. Delete series too
         */
        toDelete = new EventId();
        toDelete.setFolder(event.getFolder());
        toDelete.setId(event.getId());
        eventManager.deleteEvent(toDelete);

        // Check the normal alarm
        getAndCheckAlarmTrigger(currentTriggers); // No upcoming triggers

        // check user 2
        getAndCheckAlarmTrigger(currentTime + TimeUnit.DAYS.toMillis(2), currentTriggers, user2.getChronosApi(), user2.getSession()); // No upcoming triggers
    }

    @Test
    public void testFloatingEventAlarmTriggerTime() throws Exception {
        try {
            TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
            changeTimezone(timeZone);
            // Create an event tomorrow 12 o clock

            // Set floating date one day after the summer time change
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            Calendar cal = DateTimeUtil.getDaylightSavingDate(timeZone, currentYear + 1);
            cal.add(Calendar.DAY_OF_MONTH, 3);

            long offset = timeZone.getOffset(cal.getTimeInMillis());

            // Create an floating event with an alarm 3 days earlier
            DateTimeData startDate = DateTimeUtil.getDateTime(null, cal.getTimeInMillis());
            DateTimeData endDate = DateTimeUtil.incrementDateTimeData(startDate, TimeUnit.HOURS.toMillis(1));

            EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser(), "testFloatingEventAlarmTriggerTime", startDate, endDate, AlarmFactory.createAlarm("-PT3D", RelatedEnum.START), folderId);
            EventData event = eventManager.createEvent(toCreate);
            getAndAssertAlarms(event, 1, folderId);

            // Check if next trigger is at correct time
            Calendar from = Calendar.getInstance(UTC);
            from.setTimeInMillis(cal.getTimeInMillis());
            from.add(Calendar.DAY_OF_MONTH, -5);

            AlarmTriggerData triggers = getAndCheckAlarmTrigger(from.getTimeInMillis() + TimeUnit.DAYS.toMillis(10), null, 1);
            Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
            instance.setTime(cal.getTime());
            instance.add(Calendar.DAY_OF_MONTH, -3);

            long triggerTime = instance.getTimeInMillis();
            checkAlarmTime(triggers.get(0), event.getId(), triggerTime);

            // change timezone
            changeTimezone(TimeZone.getTimeZone("America/New_York"));

            AlarmTriggerData triggers2 = getAndCheckAlarmTrigger(from.getTimeInMillis() + TimeUnit.DAYS.toMillis(10), null, 1);
            Date parse = DateTimeUtil.parseZuluDateTime(triggers2.get(0).getTime());
            assertNotEquals(triggerTime, parse.getTime());

            int offsetNew = TimeZone.getTimeZone("America/New_York").getOffset(cal.getTimeInMillis());
            int offsetOld = TimeZone.getTimeZone("Europe/Berlin").getOffset(cal.getTimeInMillis());
            offset = offsetOld - offsetNew;
            checkAlarmTime(triggers2.get(0), event.getId(), triggerTime + offset);
        } finally {
            // Restore current Timezone
            changeTimezone(timeZone);
        }
    }

    @Test
    public void testPositiveDurationTrigger() throws ApiException, ParseException, ChronosApiException {
        // Create an event with an alarm with a positive duration
        Calendar cal = DateTimeUtil.getUTCCalendar();
        cal.add(Calendar.DAY_OF_MONTH, 1);

        DateTimeData startDate = DateTimeUtil.getDateTime(cal);
        DateTimeData endDate = DateTimeUtil.incrementDateTimeData(startDate, TimeUnit.HOURS.toMillis(1));

        EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser(), "testPositiveDurationTrigger", startDate, endDate, AlarmFactory.createAlarm("PT10M", RelatedEnum.START), folderId);
        EventData event = eventManager.createEvent(toCreate);
        getAndAssertAlarms(event, 1, folderId);

        // Get alarms within the next two days
        AlarmTriggerData triggers = getAndCheckAlarmTrigger(1); // one trigger
        AlarmTrigger alarmTrigger = triggers.get(0);
        checkAlarmTime(alarmTrigger, event.getId(), cal.getTimeInMillis() + TimeUnit.MINUTES.toMillis(10));
    }

    @Test
    public void testEndDateTrigger() throws ApiException, ParseException, ChronosApiException {
        int currentTriggers = getAlarmTriggers().size();

        // Create an event with an alarm related to the end date
        Calendar cal = DateTimeUtil.getUTCCalendar();
        cal.add(Calendar.DAY_OF_MONTH, 1);

        DateTimeData startDate = DateTimeUtil.getDateTime(cal);
        DateTimeData endDate = DateTimeUtil.incrementDateTimeData(startDate, TimeUnit.HOURS.toMillis(1));

        // Create an event with alarm
        EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(defaultUserApi.getCalUser(), "testPositiveDurationTrigger", startDate, endDate, AlarmFactory.createAlarm("-PT10M", RelatedEnum.END), folderId);
        EventData event = eventManager.createEvent(toCreate);
        getAndAssertAlarms(event, 1, folderId);

        // Get alarms within the next two days
        AlarmTriggerData triggers = getAndCheckAlarmTrigger(1 + currentTriggers); // one trigger
        AlarmTrigger alarmTrigger = null;
        for (AlarmTrigger trigger : triggers) {
            if (trigger.getFolder().equalsIgnoreCase(folderId)) {
                alarmTrigger = trigger;
                break;
            }
        }
        assertNotNull("No trigger found", alarmTrigger);
        checkAlarmTime(alarmTrigger, event.getId(), cal.getTimeInMillis() + TimeUnit.HOURS.toMillis(1) - TimeUnit.MINUTES.toMillis(10));
    }
}
