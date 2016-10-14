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

package com.openexchange.chronos.operation;

import static com.openexchange.chronos.common.CalendarUtils.filter;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.isInRange;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.truncateTime;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.i;
import static com.openexchange.chronos.impl.Utils.isIgnoreConflicts;
import static com.openexchange.folderstorage.Permission.CREATE_OBJECTS_IN_FOLDER;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.WRITE_OWN_OBJECTS;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.Period;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.compat.Recurrence;
import com.openexchange.chronos.impl.AttendeeHelper;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.EventMapper;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.UserizedEvent;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;

/**
 * {@link CreateOperation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CreateOperation extends AbstractOperation {

    /**
     * Prepares a create operation.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @return The prepared create operation
     */
    public static CreateOperation prepare(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        return new CreateOperation(storage, session, folder);
    }

    /**
     * Initializes a new {@link CreateOperation}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    private CreateOperation(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Performs the creation of an event.
     *
     * @param event The event to create
     * @param alarms The alarms to insert for the current calendar user
     * @return The result
     */
    public CalendarResultImpl perform(Event event, List<Alarm> alarms) throws OXException {
        /*
         * check current session user's permissions
         */
        requireCalendarPermission(folder, CREATE_OBJECTS_IN_FOLDER, NO_PERMISSIONS, WRITE_OWN_OBJECTS, NO_PERMISSIONS);
        /*
         * prepare event & attendee data for insert
         */
        Event newEvent = prepareEvent(event);
        List<Attendee> newAttendees = prepareAttendees(event.getAttendees());
        /*
         * check for conflicts
         */
        List<EventConflict> conflicts = checkConflicts(newEvent, newAttendees);
        if (null != conflicts && 0 < conflicts.size()) {
            for (EventConflict eventConflict : conflicts) {
                result.addConflict(eventConflict);
            }
            return result;
        }
        /*
         * insert event, attendees & alarms of user
         */
        storage.getEventStorage().insertEvent(newEvent);
        storage.getAttendeeStorage().insertAttendees(newEvent.getId(), newAttendees);
        if (null != alarms && 0 < alarms.size()) {
            storage.getAlarmStorage().insertAlarms(newEvent.getId(), calendarUser.getId(), alarms);
        }
        result.addCreation(new CreateResultImpl(loadEventData(newEvent.getId())));
        return result;
    }

    private List<Attendee> prepareAttendees(List<Attendee> attendeeData) throws OXException {
        return new AttendeeHelper(session, folder, null, attendeeData).getAttendeesToInsert();
    }

    private Event prepareEvent(Event eventData) throws OXException {
        Event event = new Event();
        /*
         * identifiers
         */
        event.setId(storage.nextObjectID());
        event.setPublicFolderId(PublicType.getInstance().equals(folder.getType()) ? i(folder) : 0);
        event.setSequence(0);
        if (false == eventData.containsUid() || Strings.isEmpty(eventData.getUid())) {
            event.setUid(UUID.randomUUID().toString());
        } else {
            event.setUid(Check.uidIsUnique(storage, eventData));
        }
        /*
         * creation/modification metadata, organizer
         */
        Consistency.setCreated(timestamp, event, calendarUser.getId());
        Consistency.setModified(timestamp, event, session.getUser().getId());
        if (eventData.containsOrganizer() && null != eventData.getOrganizer()) {
            //TODO: check/overwrite client-supplied organizer?
            event.setOrganizer(eventData.getOrganizer());
        } else {
            Consistency.setOrganizer(event, calendarUser, session.getUser());
        }
        /*
         * date/time related properties
         */
        Check.startAndEndDate(eventData);
        EventMapper.getInstance().copy(eventData, event, EventField.START_DATE, EventField.END_DATE, EventField.START_TIMEZONE, EventField.END_TIMEZONE);
        event.setAllDay(eventData.containsAllDay() ? eventData.getAllDay() : false);
        Consistency.adjustAllDayDates(event);
        Consistency.setTimeZone(event, calendarUser);
        /*
         * classification, status, transparency
         */
        if (eventData.containsClassification() && null != eventData.getClassification()) {
            event.setClassification(Check.classificationIsValid(eventData.getClassification(), folder));
        } else {
            event.setClassification(Classification.PUBLIC);
        }
        event.setStatus(eventData.containsStatus() && null != eventData.getStatus() ? eventData.getStatus() : EventStatus.CONFIRMED);
        event.setTransp(eventData.containsTransp() && null != eventData.getTransp() ? eventData.getTransp() : TimeTransparency.OPAQUE);
        /*
         * recurrence related fields
         */
        if (eventData.containsRecurrenceRule() && null != eventData.getRecurrenceRule()) {
            event.setRecurrenceRule(Check.recurrenceRuleIsValid(eventData));
            event.setSeriesId(event.getId());
            if (eventData.containsDeleteExceptionDates()) {
                event.setDeleteExceptionDates(Check.recurrenceIdsExist(eventData, eventData.getDeleteExceptionDates()));
            }
        }
        /*
         * copy over further (unchecked) event fields
         */
        EventMapper.getInstance().copy(eventData, event, EventField.SUMMARY, EventField.LOCATION, EventField.DESCRIPTION, EventField.CATEGORIES, EventField.COLOR);
        return event;
    }

    //TODO
    private List<EventConflict> checkConflicts(Event event, List<Attendee> attendees) throws OXException {
        if (isSeriesMaster(event)) {
            //TODO: check for "finished" sequence
        } else {
            Date today = truncateTime(new Date(), TimeZones.UTC);
            if (false == isInRange(event, today, null, TimeZones.UTC)) {
                return Collections.emptyList();
            }
        }
        List<Attendee> checkedAttendees = new ArrayList<Attendee>();
        checkedAttendees.addAll(filter(attendees, Boolean.TRUE, CalendarUserType.RESOURCE));
        if (false == isIgnoreConflicts(session) && (false == event.containsTransp() || false == TimeTransparency.TRANSPARENT.equals(event.getTransp()))) {
            checkedAttendees.addAll(filter(attendees, Boolean.TRUE, CalendarUserType.INDIVIDUAL));
        }
        if (0 == checkedAttendees.size()) {
            return Collections.emptyList();
        }
        Date from;
        Date until;
        if (isSeriesMaster(event)) {
            DefaultRecurrenceData recurrenceData = new DefaultRecurrenceData(event);
            Period period = Recurrence.getImplicitSeriesPeriod(recurrenceData, new Period(event));
            from = period.getStartDate();
            until = period.getEndDate();
        } else {
            //TODO: more clever expansion of queried range; need to ensure that floating events are matched by query
            from = new Date(event.getStartDate().getTime() - 86400000L);
            until = new Date(event.getEndDate().getTime() + 86400000L);
        }
        List<Event> conflictingEvents = storage.getEventStorage().searchConflictingEvents(from, until, checkedAttendees, null, null);
        return getConflicts(event, checkedAttendees, conflictingEvents);
    }

    private List<EventConflict> getConflicts(Event event, List<Attendee> attendees, List<Event> conflictingEvents) throws OXException {
        TimeZone timeZone = Utils.getTimeZone(session);
        List<EventConflict> conflicts = new ArrayList<EventConflict>();
        Period period = new Period(event); // TODO: resolve occurrences
        for (Event conflictingEvent : conflictingEvents) {
            if (event.getId() == conflictingEvent.getId() || TimeTransparency.TRANSPARENT.equals(event.getTransp())) {
                continue;
            }
            if (CalendarUtils.isInRange(conflictingEvent, period.getStartDate(), period.getEndDate(), timeZone)) {
                List<Attendee> conflictingAttendees = new ArrayList<Attendee>();
                List<Attendee> allAttendees = storage.getAttendeeStorage().loadAttendees(conflictingEvent.getId());
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
