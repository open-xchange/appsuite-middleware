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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;

/**
 * {@link RestrictedAttendeePermissionsTest}
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
    public void testAttendeeAlarms() throws Exception {
        // attendee should be able to adjust his alarms
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), testUser.getLogin(), "testAddSingleAlarm"));

        ArrayList<Attendee> atts = new ArrayList<>(2);
        Attendee attendee2 = AttendeeFactory.createIndividual(user2.getCalUser(), testUser2.getLogin());
        attendee2.setPartStat("ACCEPTED");
        atts.add(attendee2);
        expectedEventData.setAttendees(atts);
        EventData updatedEvent = eventManager.updateEvent(expectedEventData);
        assertEquals(1, updatedEvent.getAttendees().size());

        Alarm attendeeAlarm = AlarmFactory.createDisplayAlarm("-PT30M");
        attendeeAlarm.setAttendees(updatedEvent.getAttendees());
        updatedEvent.setAlarms(Collections.singletonList(AlarmFactory.createDisplayAlarm("-PT30M")));

        expectedEventData = eventManager.updateEvent(updatedEvent);
        assertNotNull(expectedEventData.getAlarms());
    }

    @Test
    public void testAttendeeRemoveFromEvent() throws Exception {
        // attendee should be able to remove himself from an event
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), testUser.getLogin(), "testCreateSingle"));
        String expectedEventId = expectedEventData.getId();

        ArrayList<Attendee> atts = new ArrayList<>(2);
        Attendee attendee2 = AttendeeFactory.createIndividual(user2.getCalUser(), testUser2.getLogin());
        attendee2.setPartStat("ACCEPTED");
        atts.add(attendee2);
        expectedEventData.setAttendees(atts);
        EventData updatedEvent = eventManager.updateEvent(expectedEventData);

        List<Attendee> attendeeList = updatedEvent.getAttendees();
        assertEquals(1, attendeeList.size());
        String folderId = attendeeList.get(0).getFolder();
        String eventId = updatedEvent.getId();
        assertEquals(expectedEventId, eventId);

        EventId event = new EventId();
        event.setFolder(folderId);
        event.setId(eventId);
        List<EventId> eventIdList = new ArrayList<>();
        eventIdList.add(event);
        //TODO put in abstract class
        user2.getChronosApi().deleteEvent(user2.getSession(), System.currentTimeMillis(), eventIdList, null, null, false);

        expectedEventData = eventManager.getEvent(expectedEventId);
        assertNotNull(expectedEventData);
        assertNull(expectedEventData.getAttendees());
    }

    @Test
    public void testAttendeeParticipationStatus() throws Exception {
        // attendee should be able to change his participation status
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), testUser.getLogin(), "testCreateSingle"));
        String expectedEventId = expectedEventData.getId();

        ArrayList<Attendee> atts = new ArrayList<>(2);
        Attendee attendee2 = AttendeeFactory.createIndividual(user2.getCalUser(), testUser2.getLogin());
        attendee2.setPartStat("ACCEPTED");
        atts.add(attendee2);
        expectedEventData.setAttendees(atts);
        EventData updatedEvent = eventManager.updateEvent(expectedEventData);

        assertEquals(1, updatedEvent.getAttendees());
        updatedEvent.getAttendees().get(0).setPartStat("TENTATIVE");
        updatedEvent = eventManager2.updateEvent(updatedEvent);

        expectedEventData = eventManager.getEvent(expectedEventId);
        assertNotNull(expectedEventData);
        assertNotNull(expectedEventData.getAttendees());
        assertEquals(1, expectedEventData.getAttendees());
        assertEquals("TENTATIVE", expectedEventData.getAttendees().get(0).getPartStat());
    }

    @Test
    public void testAttendeePermissionRestrictions() throws Exception {
        // attendee should not be able to do everything else
        EventData expectedEventData = eventManager.createEvent(EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), testUser.getLogin(), "testCreateSingle"));

        ArrayList<Attendee> atts = new ArrayList<>(2);
        Attendee attendee2 = AttendeeFactory.createIndividual(user2.getCalUser(), testUser2.getLogin());
        attendee2.setPartStat("ACCEPTED");
        atts.add(attendee2);
        expectedEventData.setAttendees(atts);

        EventData updatedEvent = eventManager.updateEvent(expectedEventData);

        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(System.currentTimeMillis());

        updatedEvent.setStartDate(DateTimeUtil.getDateTime(start));
        updatedEvent = eventManager2.updateEvent(updatedEvent);
        assertTrue(false);
    }

    //TODO define tests for recurring event series
    //TODO define tests for folder owner based use cases

}
