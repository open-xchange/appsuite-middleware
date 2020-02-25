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

package com.openexchange.chronos.impl.performer;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.exception.OXException;

/**
 * {@link NeedsActionPerformerTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.4
 */
public class NeedsActionPerformerTest {

    private NeedsActionPerformer performer;

    @Before
    public void setUp() {
        performer = new NeedsActionPerformer(null, null) {

            @Override
            protected Event prepareException(Event originalMasterEvent, RecurrenceId recurrenceId, String objectId, Date timestamp) throws OXException {
                return originalMasterEvent;
            }
        };
    }

    @Test
    public void testReduceEventsNeedingAction_null_returnEmptyList() throws OXException {
        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(null);

        assertEquals(0, reduceEventsNeedingAction.size());
    }

    @Test
    public void testReduceEventsNeedingAction_empty_returnEmptyList() throws OXException {
        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(Collections.emptyMap());

        assertEquals(0, reduceEventsNeedingAction.size());
    }

    @Test
    public void testReduceEventsNeedingAction_twoEntriesWithNullList_returnEmptyList() throws OXException {
        Map<String, List<Event>> eventsByUID = new HashMap<>();
        eventsByUID.put(UUID.randomUUID().toString(), null);
        eventsByUID.put(UUID.randomUUID().toString(), null);

        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(eventsByUID);

        assertEquals(0, reduceEventsNeedingAction.size());
    }

    @Test
    public void testReduceEventsNeedingAction_twoEntriesWithEmptyList_returnEmptyList() throws OXException {
        Map<String, List<Event>> eventsByUID = new HashMap<>();
        eventsByUID.put(UUID.randomUUID().toString(), Collections.emptyList());
        eventsByUID.put(UUID.randomUUID().toString(), Collections.emptyList());

        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(eventsByUID);

        assertEquals(0, reduceEventsNeedingAction.size());
    }

    @Test
    public void testReduceEventsNeedingAction_twoEntriesSingleEventsList_returnTwoSingleEvents() throws OXException {
        Map<String, List<Event>> eventsByUID = new HashMap<>();
        eventsByUID.put(UUID.randomUUID().toString(), Collections.singletonList(createSingleEvent()));
        eventsByUID.put(UUID.randomUUID().toString(), Collections.singletonList(createSingleEvent()));

        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(eventsByUID);

        assertEquals(2, reduceEventsNeedingAction.size());
    }

    @Test
    public void testReduceEventsNeedingAction_singleEventAndSeriesWithoutException_returnTwoEvents() throws OXException {
        Map<String, List<Event>> eventsByUID = new HashMap<>();
        eventsByUID.put(UUID.randomUUID().toString(), Collections.singletonList(createSingleEvent()));
        eventsByUID.put(UUID.randomUUID().toString(), createEventSeries());

        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(eventsByUID);

        assertEquals(2, reduceEventsNeedingAction.size());
    }

    @Test
    public void testReduceEventsNeedingAction_singleEventAndSeriesWithExceptionBasedOnDifferentParticipantStatus_returnTwoEvents() throws OXException {
        Map<String, List<Event>> eventsByUID = new HashMap<>();
        eventsByUID.put(UUID.randomUUID().toString(), Collections.singletonList(createSingleEvent()));
        List<Event> createEventSeries = createEventSeries();
        Event event = createEventSeries.get(2);
        event.getAttendees().get(0).setPartStat(ParticipationStatus.DECLINED);
        eventsByUID.put(event.getUid(), createEventSeries);

        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(eventsByUID);

        assertEquals(2, reduceEventsNeedingAction.size());
    }

    @Test
    public void testReduceEventsNeedingAction_singleEventAndSeriesWithExceptionBasedOnSummaryChange_returnThreeEvents() throws OXException {
        Map<String, List<Event>> eventsByUID = new HashMap<>();
        eventsByUID.put(UUID.randomUUID().toString(), Collections.singletonList(createSingleEvent()));
        List<Event> createEventSeries = createEventSeries();
        Event event = createEventSeries.get(2);
        event.setSummary("Summary changed");
        eventsByUID.put(event.getUid(), createEventSeries);

        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(eventsByUID);

        assertEquals(3, reduceEventsNeedingAction.size());
    }

    private List<Event> createEventSeries() {
        List<Event> series = new ArrayList<Event>();
        String uid = UUID.randomUUID().toString();
        series.add(createSingleEvent(uid));
        series.add(createSingleEvent(uid));
        series.add(createSingleEvent(uid));
        series.add(createSingleEvent(uid));
        series.add(createSingleEvent(uid));
        return series;
    }

    private Event createSingleEvent() {
        return createSingleEvent(UUID.randomUUID().toString());
    }

    private Event createSingleEvent(String uid) {
        Event event = new Event();
        event.setSummary("Event summary");
        event.setUid(uid);
        event.setAttendees(createAttendees());
        return event;
    }

    public static List<com.openexchange.chronos.Attendee> createAttendees() {
        List<Attendee> attendees = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            Attendee attendee = new Attendee();
            attendee.setEntity(i);
            attendee.setCuType(CalendarUserType.INDIVIDUAL);
            attendee.setPartStat(ParticipationStatus.ACCEPTED);
            attendee.setMember(null);
            attendees.add(attendee);
        }
        return attendees;
    }
}
