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
 *    trademarks of the OX Software GmbH. group of companies.
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

import static com.openexchange.java.Autoboxing.i;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.AttendeeAndAlarm;
import com.openexchange.testing.httpclient.models.CalendarResult;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.Trigger.RelatedEnum;

/**
 * {@link AlarmPropagationTests}
 *
 * @author <a href="mailto:firstname.lastname@open-xchange.com">Firstname Lastname</a>
 * @since v7.10.2
 */
public class AlarmPropagationTests extends AbstractAlarmTest {

    private static final String PT15M = "-PT15M";
    private static final String PT30M = "-PT30M";
    private EventData series;
    private String exceptionRecurrenceId;
    private String exceptionId;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        EventData eventData = EventFactory.createSeriesEvent(i(defaultUserApi.getCalUser()), AlarmPropagationTests.class.getSimpleName(), 3, folderId);
        series = eventManager.createEvent(eventData, true);
        TimeZone timeZone = TimeZone.getTimeZone(series.getStartDate().getTzid());
        Date from = CalendarUtils.truncateTime(new Date(), timeZone);
        Date until = CalendarUtils.add(from, Calendar.DATE, 4, timeZone);
        List<EventData> events = eventManager.getAllEvents(from, until, true, folderId);
        List<EventData> eventsByUid = getEventsByUid(events, series.getUid());
        assertEquals(3, eventsByUid.size());
        Date parseDateTime = DateTimeUtil.parseDateTime(eventData.getStartDate());
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(parseDateTime);
        exceptionRecurrenceId = eventsByUid.get(1).getRecurrenceId();
        CalendarResult response = eventManager.shiftEvent(series.getId(), exceptionRecurrenceId, eventData, cal, TimeUnit.HOURS, 1, null);
        assertFalse("Exception not updated", response.getCreated().isEmpty());
        assertEquals(1, response.getCreated().size());
        exceptionId = response.getCreated().get(0).getId();
    }

    @Test
    public void testAddAlarmToEmptyAlarm() throws ApiException, ChronosApiException {
        addSingleAlarm();
    }

    /**
     * Add a single alarm to the event series
     *
     * @return The exception event
     * @throws ApiException
     * @throws ChronosApiException
     */
    private EventData addSingleAlarm() throws ApiException, ChronosApiException {
        eventManager.updateAttendee(series.getId(), null, folderId, getAttendeeAndAlarm(PT15M), false);
        EventData event = eventManager.getEvent(folderId, exceptionId, false);
        assertFalse("Exception should contain new alarm", event.getAlarms().isEmpty());
        assertEquals(1, event.getAlarms().size());
        assertEquals(PT15M, event.getAlarms().get(0).getTrigger().getDuration());
        return event;
    }

    @Test
    public void testAddSecondAlarm() throws ApiException, ChronosApiException {
        EventData exception = addSingleAlarm();
        AttendeeAndAlarm data = getAttendeeAndAlarm();
        data.addAlarmsItem(exception.getAlarms().get(0));
        data.addAlarmsItem(AlarmFactory.createAlarm(PT30M, RelatedEnum.START));
        eventManager.getEvent(folderId, series.getId());
        eventManager.updateAttendee(series.getId(), null, folderId, data, false);
        EventData event = eventManager.getEvent(folderId, exceptionId, false);
        assertFalse("Exception should contain new alarm", event.getAlarms().isEmpty());
        assertEquals(2, event.getAlarms().size());
        String duration1 = event.getAlarms().get(0).getTrigger().getDuration();
        String duration2 = event.getAlarms().get(1).getTrigger().getDuration();
        assertTrue(PT15M.equals(duration1) && PT30M.equals(duration2) || PT15M.equals(duration2) && PT30M.equals(duration1));
    }

    @Test
    public void testRemoveAlarm() throws ApiException, ChronosApiException {
        addSingleAlarm();
        AttendeeAndAlarm data = getAttendeeAndAlarm();
        eventManager.getEvent(folderId, series.getId());
        eventManager.updateAttendee(series.getId(), null, folderId, data, false);
        EventData event = eventManager.getEvent(folderId, exceptionId, false);
        assertTrue("Exception should contain new alarm", event.getAlarms().isEmpty());
    }

    @Test
    public void testUnequalAlarms() throws ApiException, ChronosApiException {
        // add alarm to exception
        AttendeeAndAlarm data = getAttendeeAndAlarm(PT30M);
        eventManager.updateAttendee(exceptionId, null, folderId, data, false);

        EventData event = eventManager.getEvent(folderId, exceptionId, false);
        assertFalse("Exception should contain new alarm", event.getAlarms().isEmpty());
        assertEquals(1, event.getAlarms().size());
        assertEquals(PT30M, event.getAlarms().get(0).getTrigger().getDuration());

        // add other alarm to event series 
        eventManager.updateAttendee(series.getId(), null, folderId, getAttendeeAndAlarm(PT15M), false);

        event = eventManager.getEvent(folderId, exceptionId, false);
        assertFalse("Exception should still contain old alarm", event.getAlarms().isEmpty());
        assertEquals(1, event.getAlarms().size());
        assertEquals(PT30M, event.getAlarms().get(0).getTrigger().getDuration());
    }
    
    @Test
    public void testIgnoreUnrelativeMasterAlarms() throws ApiException, ChronosApiException {
        addSingleAlarm();
        EventData master = eventManager.getEvent(folderId, series.getId());
        eventManager.snoozeAlarm(series.getId(), i(master.getAlarms().get(0).getId()), 5000, folderId);
        
        master = eventManager.getEvent(folderId, series.getId());
        assertEquals(2, master.getAlarms().size());
        
        AttendeeAndAlarm data = getAttendeeAndAlarm();
        data.addAlarmsItem(master.getAlarms().get(0));
        data.addAlarmsItem(master.getAlarms().get(1));
        data.addAlarmsItem(AlarmFactory.createAlarm(PT30M, RelatedEnum.START));
        eventManager.updateAttendee(series.getId(), null, folderId, data, false);
        
        EventData event = eventManager.getEvent(folderId, exceptionId, false);
        assertFalse("Exception should contain new alarm", event.getAlarms().isEmpty());
        assertEquals(2, event.getAlarms().size());
        String duration1 = event.getAlarms().get(0).getTrigger().getDuration();
        String duration2 = event.getAlarms().get(1).getTrigger().getDuration();
        assertTrue(PT15M.equals(duration1) && PT30M.equals(duration2) || PT15M.equals(duration2) && PT30M.equals(duration1));
    }
    
    @Test
    public void testIgnoreUnrelativeExceptionAlarms() throws ApiException, ChronosApiException {
        addSingleAlarm();
        EventData exception = eventManager.getEvent(folderId, exceptionId);
        eventManager.snoozeAlarm(exceptionId, i(exception.getAlarms().get(0).getId()), 5000, folderId);
        
        exception = eventManager.getEvent(folderId, exceptionId);
        assertEquals(2, exception.getAlarms().size());
        
        EventData master = eventManager.getEvent(folderId, series.getId());
        assertEquals(1, master.getAlarms().size());
        
        AttendeeAndAlarm data = getAttendeeAndAlarm();
        data.addAlarmsItem(master.getAlarms().get(0));
        data.addAlarmsItem(AlarmFactory.createAlarm(PT30M, RelatedEnum.START));
        eventManager.updateAttendee(series.getId(), null, folderId, data, false);
        
        EventData event = eventManager.getEvent(folderId, exceptionId, false);
        assertFalse("Exception should contain new alarm", event.getAlarms().isEmpty());
        assertEquals(3, event.getAlarms().size());
        boolean [] found = new boolean[3];
        
        for(Alarm alarm: event.getAlarms()) {
            String duration = alarm.getTrigger().getDuration();
            if (duration != null) {
                if (duration.equals(PT15M)) {
                    found[0]=true;
                    continue;
                }
                if (duration.equals(PT30M)) {
                    found[1]=true;
                    continue;
                }
            } else {
                if (alarm.getTrigger().getDateTime() != null) {
                    found[2]=true;
                    continue;
                }
            }
        }
        
        assertTrue(found[0] && found[1] && found[2]);
    }
    
    /**
     * Creates an {@link AttendeeAndAlarm} object without alarms
     *
     * @return An {@link AttendeeAndAlarm} object only containing the attendee
     */
    private AttendeeAndAlarm getAttendeeAndAlarm() {
        return getAttendeeAndAlarm(null);
    }

    /**
     * 
     * Creates an {@link AttendeeAndAlarm} object with a single alarm with the given duration
     *
     * @param duration The duration to use for the alarm or null for no alarms
     * @return The {@link AttendeeAndAlarm} object
     */
    private AttendeeAndAlarm getAttendeeAndAlarm(String duration) {
        AttendeeAndAlarm data = new AttendeeAndAlarm();
        Attendee attendee = series.getAttendees().get(0);
        attendee.setMember(null);
        data.setAttendee(attendee);
        if (duration != null) {
            data.addAlarmsItem(AlarmFactory.createAlarm(duration, RelatedEnum.START));
        }
        return data;
    }

}
