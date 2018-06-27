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

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testAttendeeAlarmsChanges() throws Exception {
        // attendee should be able to adjust his alarms
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), "testAddSingleAlarm", folderId));
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
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), "testAttendeeRemoveSingle", folderId));
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

        expectedEventData = eventManager.getEvent(folderId, expectedEventId);
        assertNotNull(expectedEventData);
        assertEquals("Wrong attendee size.", 1, expectedEventData.getAttendees().size());
    }

    @Test
    public void testAttendeeParticipationStatus() throws Exception {
        // attendee should be able to change his participation status
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), "testParticipationSingle", folderId));
        String expectedEventId = expectedEventData.getId();

        expectedEventData.setAttendees(addAdditionalAttendee(expectedEventData));
        EventData updatedEvent = eventManager.updateEvent(expectedEventData, false, false);

        EventData eventToUpdate = eventManager2.getEvent(null, expectedEventId);

        assertEquals(2, eventToUpdate.getAttendees().size());
        eventManager2.setLastTimeStamp(eventManager.getLastTimeStamp());
        eventManager2.updateAttendee(expectedEventId, createAttendeeAndAlarm(eventToUpdate, user2.getCalUser()), false);

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
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), "testPermissionSingle", folderId));
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
            eventManager2.updateAttendee(expectedEventId, createAttendeeAndAlarm(updatedEvent, defaultUserApi.getCalUser()), true);
        } catch (ChronosApiException e) {
            assertNotNull(e);
            assertEquals("CAL-4038", e.getErrorCode());
        }
    }

}
