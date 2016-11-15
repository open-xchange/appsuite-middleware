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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

import static com.openexchange.chronos.common.CalendarUtils.filter;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.isInRange;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.truncateTime;
import static com.openexchange.chronos.impl.Utils.getTimeZone;
import static com.openexchange.chronos.impl.Utils.isIgnoreConflicts;
import static com.openexchange.chronos.impl.Utils.isInPast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.Period;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.compat.Recurrence;
import com.openexchange.chronos.impl.EventConflictImpl;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.UserizedEvent;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;

/**
 * {@link ConflictChecker}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ConflictChecker {

    private final CalendarSession session;
    private final CalendarStorage storage;
    private final Date now;

    /**
     * Initializes a new {@link ConflictChecker}.
     *
     * @param session The calendar session
     * @param storage The calendar storage
     */
    public ConflictChecker(CalendarSession session, CalendarStorage storage) {
        super();
        this.session = session;
        this.storage = storage;
        this.now = new Date();
    }

    public List<EventConflict> checkConflicts(Event event, List<Attendee> attendees) throws OXException {
        if (isInPast(event, truncateTime(now, TimeZones.UTC), getTimeZone(session))) {
            return Collections.emptyList();
        }
        List<Attendee> checkedAttendees = new ArrayList<Attendee>();
        checkedAttendees.addAll(filter(attendees, Boolean.TRUE, CalendarUserType.RESOURCE));
        if (false == isIgnoreConflicts(session) && (false == event.containsTransp() || false == TimeTransparency.TRANSPARENT.equals(event.getTransp()))) {
            checkedAttendees.addAll(filter(attendees, Boolean.TRUE, CalendarUserType.INDIVIDUAL));
        }
        if (0 == checkedAttendees.size()) {
            return Collections.emptyList();
        }
        TimeZone eventTimeZone = CalendarUtils.isFloating(event) ? getTimeZone(session) : TimeZone.getTimeZone(event.getTimeZone());
        Date from;
        Date until;
        if (isSeriesMaster(event)) {
            Period period = Recurrence.getImplicitSeriesPeriod(new DefaultRecurrenceData(event), new Period(event));
            from = period.getStartDate();
            until = period.getEndDate();
        } else {
            //TODO: more clever expansion of queried range; need to ensure that floating events are matched by query
            from = new Date(event.getStartDate().getTime() - 86400000L);
            until = new Date(event.getEndDate().getTime() + 86400000L);
        }
        if (until.before(truncateTime(now, TimeZones.UTC))) {
            // TODO: com.openexchange.ajax.appointment.recurrence.TestsToCreateMinimalAppointmentSeries fails, otherwise
            return Collections.emptyList();
        }
        /*
         * get potentially conflicting events from storage & resolve occurrences
         */
        List<Event> conflictingEvents = storage.getEventStorage().searchConflictingEvents(from, until, checkedAttendees, null, null);
        List<Event> checkedEvents = new ArrayList<Event>(conflictingEvents.size());
        for (Event conflictingEvent : conflictingEvents) {
            if (event.getId() == conflictingEvent.getId()) {
                continue;
            }
            conflictingEvent.setAttendees(storage.getAttendeeStorage().loadAttendees(conflictingEvent.getId()));
            if (isSeriesMaster(conflictingEvent)) {
                //TODO
                //                Calendar startCalendar = CalendarUtils.initCalendar(eventTimeZone, from);
                //                Calendar endCalendar = CalendarUtils.initCalendar(eventTimeZone, until);
                //                Iterator<Event> occurrencesIterator = Services.getService(RecurrenceService.class).calculateInstancesRespectExceptions(conflictingEvent, startCalendar, endCalendar, null, null);
                //                while (occurrencesIterator.hasNext()) {
                //                    checkedEvents.add(occurrencesIterator.next());
                //                }
            } else {
                checkedEvents.add(conflictingEvent);
            }
        }
        /*
         * check against created/updated event
         */
        if (isSeriesMaster(event)) {
            List<EventConflict> conflicts = new ArrayList<EventConflict>();
            //TODO
            //            Calendar startCalendar = CalendarUtils.initCalendar(eventTimeZone, from);
            //            Calendar endCalendar = CalendarUtils.initCalendar(eventTimeZone, until);
            //            Iterator<Event> occurrencesIterator = Services.getService(RecurrenceService.class).calculateInstancesRespectExceptions(event, startCalendar, endCalendar, null, null);
            //            while (occurrencesIterator.hasNext()) {
            //                conflicts.addAll(getConflicts(occurrencesIterator.next(), checkedAttendees, conflictingEvents));
            //            }
            return conflicts;
        }
        return getConflicts(event, checkedAttendees, conflictingEvents);
    }

    private List<EventConflict> getConflicts(Event event, List<Attendee> attendees, List<Event> conflictingEvents) throws OXException {

        TimeZone timeZone = getTimeZone(session);
        List<EventConflict> conflicts = new ArrayList<EventConflict>();

        Period period = new Period(event);
        for (Event conflictingEvent : conflictingEvents) {
            if (event.getId() == conflictingEvent.getId()) {
                continue;
            }
            if (isInRange(conflictingEvent, period.getStartDate(), period.getEndDate(), timeZone)) {
                List<Attendee> conflictingAttendees = new ArrayList<Attendee>();
                List<Attendee> allAttendees = conflictingEvent.containsAttendees() ? conflictingEvent.getAttendees() : storage.getAttendeeStorage().loadAttendees(conflictingEvent.getId());
                for (Attendee checkedAttendee : attendees) {
                    Attendee matchingAttendee = find(allAttendees, checkedAttendee);
                    if (null != matchingAttendee && false == ParticipationStatus.DECLINED.equals(matchingAttendee.getPartStat())) {
                        conflictingAttendees.add(matchingAttendee);
                    }
                }
                if (0 < conflictingAttendees.size()) {
                    conflictingEvent.setAttendees(allAttendees);
                    int folderID = conflictingEvent.getPublicFolderId();
                    Attendee userAttendee = find(allAttendees, session.getUser().getId());
                    if (null != userAttendee && 0 < userAttendee.getFolderID()) {
                        folderID = userAttendee.getFolderID();
                    }
                    //TODO: check further possible parent folder candidates, anonymize if not visible
                    UserizedEvent userizedEvent = new UserizedEvent(session.getSession(), conflictingEvent, folderID, null);
                    conflicts.add(new EventConflictImpl(userizedEvent, conflictingAttendees, isHardConflict(conflictingEvent, conflictingAttendees)));
                }
            }
        }
        return conflicts;
    }

    private static boolean isHardConflict(Event conflictingEvent, List<Attendee> conflictingAttendees) {
        for (Attendee attendee : conflictingAttendees) {
            if (CalendarUserType.RESOURCE.equals(attendee.getCuType())) {
                return true;
            }
        }
        return false;
    }

}
