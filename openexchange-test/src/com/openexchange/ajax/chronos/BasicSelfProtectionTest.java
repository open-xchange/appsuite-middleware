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

import static com.openexchange.java.Autoboxing.L;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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

/**
 *
 * {@link BasicSelfProtectionTest} tests self protection mechanisms of the chronos stack
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class BasicSelfProtectionTest extends AbstractChronosTest {

    @SuppressWarnings("hiding")
    private String folderId;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderId = createAndRememberNewFolder(defaultUserApi, getDefaultFolder(), defaultUserApi.getCalUser().intValue());
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
        EventData toCreate = EventFactory.createSeriesEvent(getCalendaruser(), "testTooManyEvents", 1002, folderId);

        // Try create with expand 'true'
        ChronosCalendarResultResponse createEvent = defaultUserApi.getChronosApi().createEvent(folderId, toCreate, FALSE, null, FALSE, null, fromStr, untilStr, TRUE, null);
        Assert.assertNotNull("Response doesn't contain an error", createEvent.getError());
        Assert.assertEquals(excpectedErrorCode, createEvent.getCode());

        // Create normally
        EventData expectedEventData = eventManager.createEvent(toCreate, true);
        EventData actualEventData = eventManager.getEvent(folderId, expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);
        long timestamp = eventManager.getLastTimeStamp();

        // Query all event occurrences
        EventsResponse eventsResponse = defaultUserApi.getChronosApi().getAllEvents(fromStr, untilStr, folderId, null, null, null, TRUE, TRUE, FALSE);
        Assert.assertNotNull("Response doesn't contain an error", eventsResponse.getError());
        Assert.assertEquals(excpectedErrorCode, eventsResponse.getCode());

        // Update event with expand 'true'
        EventData eventData = actualEventData;
        eventData.setDescription("Changed description");
        ChronosCalendarResultResponse updateResponse = defaultUserApi.getChronosApi().updateEvent(folderId, eventData.getId(), L(eventManager.getLastTimeStamp()), getUpdateBody(eventData), null, null, FALSE, null, FALSE, null, null, fromStr, untilStr, TRUE, null);
        Assert.assertNotNull("Response doesn't contain an error", updateResponse.getError());
        Assert.assertEquals(excpectedErrorCode, updateResponse.getCode());

        // Do a successful update
        eventManager.updateEvent(eventData);

        // Query updates with expand 'true'
        ChronosUpdatesResponse updatesResponse = defaultUserApi.getChronosApi().getUpdates(folderId, L(timestamp), fromStr, untilStr, null, null, null, TRUE, FALSE);
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
        EventData toCreate = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "testTooManyAttendees", folderId);

        ArrayList<Attendee> attendees = new ArrayList<>(1010);
        attendees.addAll(toCreate.getAttendees());
        toCreate.setAttendees(attendees);

        for(int x=0; x<1005; x++){
            String mail = UUID.randomUUID() + "@toomanyattendees.test";
            toCreate.addAttendeesItem(AttendeeFactory.createIndividual(mail));
        }

        // Try create with expand 'true'
        ChronosCalendarResultResponse createEvent = defaultUserApi.getChronosApi().createEvent(folderId, toCreate, FALSE, null, FALSE, null, null, null, FALSE, null);
        Assert.assertNotNull("Response doesn't contain an error", createEvent.getError());
        Assert.assertEquals(excpectedErrorCode, createEvent.getCode());


        // Create normal and try to update
        toCreate = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "testTooManyAttendees", folderId);
        EventData expectedEventData = eventManager.createEvent(toCreate);
        EventData actualEventData = eventManager.getEvent(folderId, expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);

        for(int x=0; x<1005; x++){
            String mail = UUID.randomUUID() + "@toomanyattendees.test";
            actualEventData.addAttendeesItem(AttendeeFactory.createIndividual(mail));
        }

        ChronosCalendarResultResponse updateResponse = defaultUserApi.getChronosApi().updateEvent(folderId, actualEventData.getId(), L(eventManager.getLastTimeStamp()), getUpdateBody(actualEventData), null, null, FALSE, null, FALSE, null, null, null, null, FALSE, null);
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
        EventData toCreate = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "testTooManyAttendees", folderId);

        ArrayList<Alarm> alarms = new ArrayList<>(110);
        toCreate.setAlarms(alarms);

        for(int x=0; x<105; x++){
            toCreate.addAlarmsItem(AlarmFactory.createDisplayAlarm("-PT"+x+"M"));
        }

        // Try create with expand 'true'
        ChronosCalendarResultResponse createEvent = defaultUserApi.getChronosApi().createEvent(folderId, toCreate, FALSE, null, FALSE, null, null, null, FALSE, null);
        Assert.assertNotNull("Response doesn't contain an error", createEvent.getError());
        Assert.assertEquals(excpectedErrorCode, createEvent.getCode());


        // Create normal and try to update
        toCreate = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "testTooManyAttendees", folderId);
        EventData expectedEventData = eventManager.createEvent(toCreate);
        EventData actualEventData = eventManager.getEvent(folderId, expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);

        for(int x=0; x<105; x++){
            actualEventData.addAlarmsItem(AlarmFactory.createDisplayAlarm("-PT"+x+"M"));
        }

        ChronosCalendarResultResponse updateResponse = defaultUserApi.getChronosApi().updateEvent(folderId, actualEventData.getId(), L(eventManager.getLastTimeStamp()), getUpdateBody(actualEventData), null, null, FALSE, null, FALSE, null, null, null, null, FALSE, null);
        Assert.assertNotNull("Response doesn't contain an error", updateResponse.getError());
        Assert.assertEquals(excpectedErrorCode, updateResponse.getCode());
    }
}
