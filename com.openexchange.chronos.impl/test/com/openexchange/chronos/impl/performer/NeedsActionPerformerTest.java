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

import static com.openexchange.time.TimeTools.D;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.UUID;
import org.dmfs.rfc5545.DateTime;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.exception.OXException;
import com.openexchange.time.TimeTools;

/**
 * {@link NeedsActionPerformerTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.4
 */
public class NeedsActionPerformerTest {

    private NeedsActionPerformer performer;
    private String timeZone;

    @Before
    public void setUp() {
        performer = new NeedsActionPerformer(null, null) {

            @Override
            protected Event prepareException(Event originalMasterEvent, RecurrenceId recurrenceId, String objectId, Date timestamp) throws OXException {
                return originalMasterEvent;
            }
        };
        timeZone = TimeZone.getAvailableIDs()[0];
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
        eventsByUID.put(UUID.randomUUID().toString(), createSeriesWithMaster());

        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(eventsByUID);

        assertEquals(2, reduceEventsNeedingAction.size());
    }

    @Test
    public void testReduceEventsNeedingAction_singleEventAndSeriesWithoutExceptionButNoMasterAvailable_returnAll() throws OXException {
        Map<String, List<Event>> eventsByUID = new HashMap<>();
        eventsByUID.put(UUID.randomUUID().toString(), Collections.singletonList(createSingleEvent()));
        eventsByUID.put(UUID.randomUUID().toString(), createSeriesWithoutMaster());

        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(eventsByUID);

        assertEquals(5, reduceEventsNeedingAction.size());
    }

    @Test
    public void testReduceEventsNeedingAction_singleEventAndSeriesWithExceptionBasedOnDifferentParticipantStatus_returnTwoEvents() throws OXException {
        Map<String, List<Event>> eventsByUID = new HashMap<>();
        eventsByUID.put(UUID.randomUUID().toString(), Collections.singletonList(createSingleEvent()));
        List<Event> createEventSeries = createSeriesWithMaster();
        Event master = createEventSeries.get(0);
        master.getAttendees().get(0).setPartStat(ParticipationStatus.DECLINED);
        eventsByUID.put(master.getUid(), createEventSeries);

        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(eventsByUID);

        assertEquals(2, reduceEventsNeedingAction.size());
    }

    @Test
    public void testReduceEventsNeedingAction_singleEventAndSeriesWithExceptionBasedOnDifferentParticipantStatusButNoMasterAvailable_returnAll() throws OXException {
        Map<String, List<Event>> eventsByUID = new HashMap<>();
        eventsByUID.put(UUID.randomUUID().toString(), Collections.singletonList(createSingleEvent()));
        List<Event> createEventSeries = createSeriesWithoutMaster();
        Event event = createEventSeries.get(2);
        event.getAttendees().get(0).setPartStat(ParticipationStatus.DECLINED);
        eventsByUID.put(event.getUid(), createEventSeries);

        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(eventsByUID);

        assertEquals(5, reduceEventsNeedingAction.size());
    }

    @Test
    public void testReduceEventsNeedingAction_singleEventAndSeriesWithExceptionBasedOnSummaryChange_returnTwoEvents() throws OXException {
        Map<String, List<Event>> eventsByUID = new HashMap<>();
        eventsByUID.put(UUID.randomUUID().toString(), Collections.singletonList(createSingleEvent()));
        List<Event> createEventSeries = createSeriesWithMaster();

        TimeZone utc = TimeZone.getTimeZone("UTC");
        TimeZone tz = TimeZone.getTimeZone(timeZone);

        Event master = createEventSeries.get(0);
        Event change = getInstance(master, D("03.10.2008 00:00:00", utc), D("03.10.2008 14:35:00", tz), D("03.10.2008 16:35:00", tz));
        change.setSummary("Summary changed");
        createEventSeries.add(change);
        eventsByUID.put(change.getUid(), createEventSeries);

        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(eventsByUID);

        assertEquals(3, reduceEventsNeedingAction.size());
    }

    @Test
    public void testReduceEventsNeedingAction_singleEventAndSeriesWithExceptionBasedOnTimeChange_returnTwoEvents() throws OXException {
        Map<String, List<Event>> eventsByUID = new HashMap<>();
        eventsByUID.put(UUID.randomUUID().toString(), Collections.singletonList(createSingleEvent()));
        List<Event> createEventSeries = createSeriesWithMaster();

        TimeZone utc = TimeZone.getTimeZone("UTC");
        TimeZone tz = TimeZone.getTimeZone(timeZone);

        Event master = createEventSeries.get(0);
        Event change = getInstance(master, D("03.10.2008 00:00:00", utc), D("03.10.2008 14:35:00", tz), D("03.10.2008 16:35:00", tz));
        change.setStartDate(DT("03.10.2008 14:35:00", tz, false));
        change.setEndDate(DT("03.10.2008 16:35:00", tz, false));
        createEventSeries.add(change);
        eventsByUID.put(change.getUid(), createEventSeries);

        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(eventsByUID);

        assertEquals(3, reduceEventsNeedingAction.size());
    }

    @Test
    public void testReduceEventsNeedingAction_singleEventAndSeriesWithExceptionBasedOnSummaryChangeButNoMasterAvailable_returnAll() throws OXException {
        Map<String, List<Event>> eventsByUID = new HashMap<>();
        eventsByUID.put(UUID.randomUUID().toString(), Collections.singletonList(createSingleEvent()));
        List<Event> createEventSeries = createSeriesWithoutMaster();
        Event event = createEventSeries.get(2);
        event.setSummary("Summary changed");
        eventsByUID.put(event.getUid(), createEventSeries);

        List<Event> reduceEventsNeedingAction = performer.reduceEventsNeedingAction(eventsByUID);

        assertEquals(5, reduceEventsNeedingAction.size());
    }

    private List<Event> createSeriesWithMaster() {
        List<Event> series = new ArrayList<Event>();
        series.add(createSeriesMaster());
        return series;
    }

    private Event createSeriesMaster() {
        String uid = UUID.randomUUID().toString();

        Event master = new Event();
        String seriesId = UUID.randomUUID().toString();
        master.setId(seriesId);
        master.setSeriesId(seriesId);
        master.setUid(uid);
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        master.setAttendees(createAttendees());

        TimeZone utc = TimeZone.getTimeZone("UTC");
        setStartAndEndDates(master, "01.10.2008 00:00:00", "01.10.2008 00:00:00", true, utc);

        return master;
    }

    private List<Event> createSeriesWithoutMaster() {
        List<Event> series = new ArrayList<Event>();
        String uid = UUID.randomUUID().toString();
        series.add(createSingleEvent(uid));
        series.add(createSingleEvent(uid));
        series.add(createSingleEvent(uid));
        series.add(createSingleEvent(uid));

        return series;
    }

    protected static void setStartAndEndDates(Event event, String start, String end, boolean allDay, TimeZone timeZone) {
        event.setStartDate(DT(start, timeZone, allDay));
        event.setEndDate(DT(end, timeZone, allDay));
    }

    protected static Event getInstance(Event master, Date recurrenceId, Date start, Date end) {
        Event instance = clone(master);
        instance.removeId();
        instance.removeRecurrenceRule();
        instance.removeDeleteExceptionDates();
        instance.setRecurrenceId(new DefaultRecurrenceId(DT(recurrenceId, master.getStartDate().getTimeZone(), master.getStartDate().isAllDay())));
        instance.setStartDate(DT(start, instance.getStartDate().getTimeZone(), instance.getStartDate().isAllDay()));
        instance.setEndDate(DT(end, instance.getEndDate().getTimeZone(), instance.getEndDate().isAllDay()));
        return instance;
    }

    protected static Event clone(Event event) {
        Event clone = new Event();
        if (event.containsAttachments()) {
            clone.setAttachments(cloneList(event.getAttachments()));
        }
        if (event.containsAttendees()) {
            clone.setAttendees(cloneList(event.getAttendees()));
        }
        if (event.containsAlarms()) {
            clone.setAlarms(cloneList(event.getAlarms()));
        }
        if (event.containsCategories()) {
            clone.setCategories(cloneList(event.getCategories()));
        }
        if (event.containsClassification()) {
            clone.setClassification(event.getClassification());
        }
        if (event.containsColor()) {
            clone.setColor(event.getColor());
        }
        if (event.containsCreated()) {
            clone.setCreated(event.getCreated());
        }
        if (event.containsCreatedBy()) {
            clone.setCreatedBy(event.getCreatedBy());
        }
        if (event.containsDeleteExceptionDates()) {
            clone.setDeleteExceptionDates(cloneSet(event.getDeleteExceptionDates()));
        }
        if (event.containsDescription()) {
            clone.setDescription(event.getDescription());
        }
        if (event.containsEndDate()) {
            clone.setEndDate(event.getEndDate());
        }
        if (event.containsFilename()) {
            clone.setFilename(event.getFilename());
        }
        if (event.containsFolderId()) {
            clone.setFolderId(event.getFolderId());
        }
        if (event.containsId()) {
            clone.setId(event.getId());
        }
        if (event.containsLastModified()) {
            clone.setLastModified(event.getLastModified());
        }
        if (event.containsLocation()) {
            clone.setLocation(event.getLocation());
        }
        if (event.containsModifiedBy()) {
            clone.setModifiedBy(event.getModifiedBy());
        }
        if (event.containsOrganizer()) {
            clone.setOrganizer(event.getOrganizer());
        }
        if (event.containsRecurrenceId()) {
            clone.setRecurrenceId(event.getRecurrenceId());
        }
        if (event.containsRecurrenceRule()) {
            clone.setRecurrenceRule(event.getRecurrenceRule());
        }
        if (event.containsSequence()) {
            clone.setSequence(event.getSequence());
        }
        if (event.containsSeriesId()) {
            clone.setSeriesId(event.getSeriesId());
        }
        if (event.containsStartDate()) {
            clone.setStartDate(event.getStartDate());
        }
        if (event.containsStatus()) {
            clone.setStatus(event.getStatus());
        }
        if (event.containsSummary()) {
            clone.setSummary(event.getSummary());
        }
        if (event.containsTransp()) {
            clone.setTransp(event.getTransp());
        }
        if (event.containsUid()) {
            clone.setUid(event.getUid());
        }
        return clone;
    }

    private static <T> List<T> cloneList(List<T> list) {
        if (null == list) {
            return null;
        }
        List<T> retval = new ArrayList<T>();
        retval.addAll(list);
        return retval;
    }

    private static <T> SortedSet<T> cloneSet(SortedSet<T> list) {
        if (null == list) {
            return null;
        }
        SortedSet<T> retval = new TreeSet<T>();
        retval.addAll(list);
        return retval;
    }

    protected static DateTime DT(String value, TimeZone timeZone, boolean allDay) {
        return DT(TimeTools.D(value, timeZone), timeZone, allDay);
    }

    protected static DateTime DT(Date date, TimeZone timeZone, boolean allDay) {
        if (allDay) {
            return new DateTime(date.getTime()).toAllDay();
        }
        return new DateTime(timeZone, date.getTime());
    }

    private Event createSingleEvent() {
        return createSingleEvent(UUID.randomUUID().toString());
    }

    private Event createSingleEvent(String uid) {
        Event event = new Event();
        event.setSummary("Event summary");
        event.setUid(uid);
        event.setId(UUID.randomUUID().toString());
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
