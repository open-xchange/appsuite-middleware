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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.Trigger.RelatedEnum;

/**
 * {@link RestrictedAttendeePermissionsTest}
 *
 * Tests the attendee scheduling resource restrictions as described in the RFC
 * @see <a href="https://tools.ietf.org/html/rfc6638#section-3.1">RFC 6638, section 3.1</a>
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class RestrictedAttendeePermissionsTest extends AbstractAttendeeTest {

    @Test
    public void testAttendeeAlarmsChanges() throws Exception {
        // attendee should be able to adjust his alarms
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(getCalendaruser(), "testAddSingleAlarm", folderId), true);
        String expectedEventId = expectedEventData.getId();

        expectedEventData.setAttendees(addAdditionalAttendee(expectedEventData));
        eventManager.updateEvent(expectedEventData, false, false);

        EventData eventToUpdate = eventManager2.getEvent(null, expectedEventId);

        assertEquals(2, eventToUpdate.getAttendees().size());
        eventManager2.setLastTimeStamp(eventManager.getLastTimeStamp());
        List<Alarm> body = new ArrayList<>();
        body.add(AlarmFactory.createAlarm("-PT20M", RelatedEnum.START));
        body.add(AlarmFactory.createAlarm("-PT30M", RelatedEnum.START));
        eventToUpdate = updateAlarms(expectedEventId, eventManager2.getLastTimeStamp(), body, null);
        assertNotNull(eventToUpdate.getAlarms());
        assertEquals(2, eventToUpdate.getAlarms().size());
    }

    @Test
    public void testAttendeeRemoveFromEvent() throws Exception {
        // attendee should be able to remove himself from an event
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(getCalendaruser(), "testAttendeeRemoveSingle", folderId), true);
        String expectedEventId = expectedEventData.getId();

        expectedEventData.setAttendees(addAdditionalAttendee(expectedEventData));
        EventData updatedEvent = eventManager.updateEvent(expectedEventData, false, false);

        List<Attendee> attendeeList = updatedEvent.getAttendees();
        assertEquals(2, attendeeList.size());
        String eventId = updatedEvent.getId();
        assertEquals(expectedEventId, eventId);

        EventId event = new EventId();
        event.setFolder(folderId2);
        event.setId(expectedEventId);

        eventManager2.deleteEvent(event);
        try {
            eventManager2.getEvent(folderId2, eventId, true);
            Assert.fail("Expected an exception.");
        } catch (@SuppressWarnings("unused") ChronosApiException e) {
            // expected
        }

        expectedEventData = eventManager.getEvent(folderId, expectedEventId);
        assertNotNull(expectedEventData);
        assertEquals("Wrong attendee size.", 2, expectedEventData.getAttendees().size());
        boolean found = false;
        for (Attendee att : expectedEventData.getAttendees()) {
            if (att.getEntity() == user2.getCalUser()) {
                assertEquals("declined", att.getPartStat().toLowerCase());
                found = true;
            }
        }

        assertTrue("Second user not found in the attendee list.", found);
    }

    @Test
    public void testAttendeeParticipationStatus() throws Exception {
        // attendee should be able to change his participation status
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(getCalendaruser(), "testParticipationSingle", folderId), true);
        String expectedEventId = expectedEventData.getId();

        expectedEventData.setAttendees(addAdditionalAttendee(expectedEventData));
        EventData updatedEvent = eventManager.updateEvent(expectedEventData, false, false);

        EventData eventToUpdate = eventManager2.getEvent(null, expectedEventId);

        assertEquals(2, eventToUpdate.getAttendees().size());
        eventManager2.setLastTimeStamp(eventManager.getLastTimeStamp());
        eventManager2.updateAttendee(expectedEventId, createAttendeeAndAlarm(eventToUpdate, user2.getCalUser().intValue()), false);

        updatedEvent = eventManager.getEvent(folderId, expectedEventId);

        assertNotNull(updatedEvent.getAttendees());
        assertEquals(2, updatedEvent.getAttendees().size());
        int accepted = updatedEvent.getAttendees().get(0).getEntity() == defaultUserApi.getCalUser() ? 0 : 1;
        assertEquals("ACCEPTED", updatedEvent.getAttendees().get(accepted).getPartStat());
        assertEquals("TENTATIVE", updatedEvent.getAttendees().get(accepted == 0 ? 1 : 0).getPartStat());
    }

    @Test
    public void testAttendeePermissionRestrictions() throws Exception {
        // attendee should not be able to do anything else
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(getCalendaruser(), "testPermissionSingle", folderId), true);
        String expectedEventId = expectedEventData.getId();
        expectedEventData.setAttendees(addAdditionalAttendee(expectedEventData));
        EventData updatedEvent = eventManager.updateEvent(expectedEventData);
        EventData event2 = eventManager2.getEvent(null, expectedEventId);
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(System.currentTimeMillis());
        event2.setStartDate(DateTimeUtil.getDateTime(start));
        try {
            updatedEvent = eventManager2.updateEvent(event2, true, false);
            fail("No exception was thrown");
        } catch (ChronosApiException e) {
            assertNotNull(e);
            assertEquals("CAL-4038", e.getErrorCode());
        }
        eventManager2.setLastTimeStamp(eventManager.getLastTimeStamp());
        try {
            eventManager2.updateAttendee(expectedEventId, createAttendeeAndAlarm(updatedEvent, getCalendaruser()), true);
        } catch (ChronosApiException e) {
            assertNotNull(e);
            assertEquals("CAL-4038", e.getErrorCode());
        }
    }

}
