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

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.util.AssertUtil;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.ChronosUpdatesResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventsResponse;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

/**
 *
 * {@link BasicSelfProtectionTest} tests self protection mechanisms of the chronos stack
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class BasicSelfProtectionTest extends AbstractChronosTest {

    private String folderId;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderId = getDefaultFolder();
    }

    /**
     * Tests the creation of a series event
     */
    @Test
    public void testTooManyEvents() throws Exception {
        // Create event with more than 1000 occurrences
        String excpectedErrorCode = CalendarExceptionCodes.TOO_MANY_EVENT_RESULTS.create().getErrorCode();
        long now = System.currentTimeMillis();
        Date from = new Date(now - TimeUnit.DAYS.toMillis(1));
        Date until = new Date(now + TimeUnit.DAYS.toMillis(1012));
        String fromStr = DateTimeUtil.getZuluDateTime(from.getTime()).getValue();
        String untilStr = DateTimeUtil.getZuluDateTime(until.getTime()).getValue();
        EventData toCreate = EventFactory.createSeriesEvent(defaultUserApi.getCalUser(), "testTooManyEvents", 1002, folderId);

        // Try create with expand 'true'
        ChronosCalendarResultResponse createEvent = defaultUserApi.getChronosApi().createEvent(defaultUserApi.getSession(), folderId, toCreate, false, false, false, fromStr, untilStr, true);
        Assert.assertNotNull("Response doesn't contain an error", createEvent.getError());
        Assert.assertEquals(excpectedErrorCode, createEvent.getCode());

        // Create normally
        EventData expectedEventData = eventManager.createEvent(toCreate);
        EventData actualEventData = eventManager.getEvent(folderId, expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);
        long timestamp = eventManager.getLastTimeStamp();

        // Query all event occurrences
        EventsResponse eventsResponse = defaultUserApi.getChronosApi().getAllEvents(defaultUserApi.getSession(), fromStr, untilStr, folderId, null, null, null, true, true, false);
        Assert.assertNotNull("Response doesn't contain an error", eventsResponse.getError());
        Assert.assertEquals(excpectedErrorCode, eventsResponse.getCode());

        // Update event with expand 'true'
        EventData eventData = actualEventData;
        eventData.setDescription("Changed description");
        ChronosCalendarResultResponse updateResponse = defaultUserApi.getChronosApi().updateEvent(defaultUserApi.getSession(), folderId, eventData.getId(), eventData, eventManager.getLastTimeStamp(), null, null, false, false, false, fromStr, untilStr, true);
        Assert.assertNotNull("Response doesn't contain an error", updateResponse.getError());
        Assert.assertEquals(excpectedErrorCode, updateResponse.getCode());

        // Do a successful update
        eventManager.updateEvent(eventData);

        // Query updates with expand 'true'
        ChronosUpdatesResponse updatesResponse = defaultUserApi.getChronosApi().getUpdates(defaultUserApi.getSession(), folderId, timestamp, fromStr, untilStr, null, null, null, true, false);
        Assert.assertNotNull("Response doesn't contain an error", updatesResponse.getError());
        Assert.assertEquals(excpectedErrorCode, updatesResponse.getCode());
    }

    /**
     * Tests the creation of a series event
     */
    @Test
    public void testTooManyAttendees() throws Exception {

        String excpectedErrorCode = CalendarExceptionCodes.TOO_MANY_ATTENDEES.create().getErrorCode();

        // Create single event with over 1000 attendees
        EventData toCreate = EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), "testTooManyAttendees");

        ArrayList<Attendee> attendees = new ArrayList<>(1010);
        attendees.addAll(toCreate.getAttendees());
        toCreate.setAttendees(attendees);

        for(int x=0; x<1005; x++){
            String mail = UUID.randomUUID() + "@toomanyattendees.test";
            toCreate.addAttendeesItem(AttendeeFactory.createIndividual(mail));
        }

        // Try create with expand 'true'
        ChronosCalendarResultResponse createEvent = defaultUserApi.getChronosApi().createEvent(defaultUserApi.getSession(), folderId, toCreate, false, false, false, null, null, false);
        Assert.assertNotNull("Response doesn't contain an error", createEvent.getError());
        Assert.assertEquals(excpectedErrorCode, createEvent.getCode());


        // Create normal and try to update
        toCreate = EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), "testTooManyAttendees", folderId);
        EventData expectedEventData = eventManager.createEvent(toCreate);
        EventData actualEventData = eventManager.getEvent(folderId, expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);

        for(int x=0; x<1005; x++){
            String mail = UUID.randomUUID() + "@toomanyattendees.test";
            actualEventData.addAttendeesItem(AttendeeFactory.createIndividual(mail));
        }

        ChronosCalendarResultResponse updateResponse = defaultUserApi.getChronosApi().updateEvent(defaultUserApi.getSession(), folderId, actualEventData.getId(), actualEventData, eventManager.getLastTimeStamp(), null, null, false, false, false, null, null, false);
        Assert.assertNotNull("Response doesn't contain an error", updateResponse.getError());
        Assert.assertEquals(excpectedErrorCode, updateResponse.getCode());
    }

    /**
     * Tests the creation of a series event
     */
    @Test
    public void testTooManyAlarms() throws Exception {

        String excpectedErrorCode = CalendarExceptionCodes.TOO_MANY_ALARMS.create().getErrorCode();

        // Create single event with over 100 alarms
        EventData toCreate = EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), "testTooManyAttendees");

        ArrayList<Alarm> alarms = new ArrayList<>(110);
        toCreate.setAlarms(alarms);

        for(int x=0; x<105; x++){
            toCreate.addAlarmsItem(AlarmFactory.createDisplayAlarm("-PT"+x+"M"));
        }

        // Try create with expand 'true'
        ChronosCalendarResultResponse createEvent = defaultUserApi.getChronosApi().createEvent(defaultUserApi.getSession(), folderId, toCreate, false, false, false, null, null, false);
        Assert.assertNotNull("Response doesn't contain an error", createEvent.getError());
        Assert.assertEquals(excpectedErrorCode, createEvent.getCode());


        // Create normal and try to update
        toCreate = EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), "testTooManyAttendees");
        EventData expectedEventData = eventManager.createEvent(toCreate);
        EventData actualEventData = eventManager.getEvent(folderId, expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);

        for(int x=0; x<105; x++){
            actualEventData.addAlarmsItem(AlarmFactory.createDisplayAlarm("-PT"+x+"M"));
        }

        ChronosCalendarResultResponse updateResponse = defaultUserApi.getChronosApi().updateEvent(defaultUserApi.getSession(), folderId, actualEventData.getId(), actualEventData, eventManager.getLastTimeStamp(), null, null, false, false, false, null, null, false);
        Assert.assertNotNull("Response doesn't contain an error", updateResponse.getError());
        Assert.assertEquals(excpectedErrorCode, updateResponse.getCode());
    }
}
