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

import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.initCalendar;
import static com.openexchange.chronos.common.CalendarUtils.isFloating;
import static com.openexchange.chronos.common.CalendarUtils.isInRange;
import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.truncateTime;
import static com.openexchange.chronos.impl.Utils.asList;
import static com.openexchange.chronos.impl.Utils.getFields;
import static com.openexchange.chronos.impl.Utils.getTimeZone;
import static com.openexchange.chronos.impl.Utils.isIgnoreConflicts;
import static com.openexchange.chronos.impl.Utils.loadAdditionalEventData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.impl.EventConflictImpl;
import com.openexchange.chronos.impl.EventMapper;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.UserizedEvent;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.java.Autoboxing;
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
    private final Date today;

    private Map<Integer, Permission> folderPermissions;

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
        this.today = truncateTime(new Date(), getTimeZone(session));
    }

    /**
     * Checks for conflicts.
     *
     * @param event The event being inserted/updated
     * @param attendees The event's list of attendees
     * @return The conflicts, or an empty list if there are none
     */
    public List<EventConflict> checkConflicts(Event event, List<Attendee> attendees) throws OXException {
        /*
         * check which attendees need to be checked
         */
        List<Attendee> attendeesToCheck = getAttendeesToCheck(event, attendees);
        if (attendeesToCheck.isEmpty()) {
            return Collections.emptyList();
        }
        /*
         * get conflicts for series or regular event
         */
        return isSeriesMaster(event) ? getSeriesConflicts(event, attendeesToCheck) : getEventConflicts(event, attendeesToCheck);
    }

    /**
     * Checks for conflicts for a single, non recurring event (or a single exception event of a series).
     *
     * @param event The event being inserted/updated
     * @param attendeesToCheck The attendees to check
     * @return The conflicts, or an empty list if there are none
     */
    private List<EventConflict> getEventConflicts(Event event, List<Attendee> attendeesToCheck) throws OXException {
        /*
         * derive checked period (+/- one day to cover floating events in different timezone)
         */
        Date until = new Date(event.getEndDate().getTime() + 86400000);
        if (today.after(until)) {
            return Collections.emptyList();
        }
        Date from = new Date(event.getStartDate().getTime() - 86400000);
        /*
         * search for potentially conflicting events in period
         */
        List<Event> eventsInPeriod = getOverlappingEvents(from, until, attendeesToCheck);
        if (eventsInPeriod.isEmpty()) {
            return Collections.emptyList();
        }
        /*
         * check against each event in period
         */
        TimeZone eventTimeZone = isFloating(event) || null == event.getTimeZone() ? getTimeZone(session) : TimeZone.getTimeZone(event.getTimeZone());
        List<EventConflict> conflicts = new ArrayList<EventConflict>();
        for (Event eventInPeriod : eventsInPeriod) {
            if (eventInPeriod.getId() == event.getId()) {
                continue;
            }
            /*
             * determine intersecting attendees
             */
            List<Attendee> conflictingAttendees = getConflictingAttendees(attendeesToCheck, eventInPeriod);
            if (null == conflictingAttendees || 0 == conflictingAttendees.size()) {
                continue;
            }
            boolean hardConflict = isHardConflict(eventInPeriod, conflictingAttendees);
            if (isSeriesMaster(eventInPeriod)) {
                /*
                 * expand & check all occurrences of event series in period
                 */
                long duration = eventInPeriod.getEndDate().getTime() - eventInPeriod.getStartDate().getTime();
                Iterator<RecurrenceId> iterator = Services.getService(RecurrenceService.class)
                    .getRecurrenceIterator(eventInPeriod, initCalendar(TimeZones.UTC, from), initCalendar(TimeZones.UTC, until), false);
                while (iterator.hasNext()) {
                    RecurrenceId recurrenceId = iterator.next();
                    if (event.getStartDate().getTime() < recurrenceId.getValue() + duration && event.getEndDate().getTime() > recurrenceId.getValue()) {
                        /*
                         * add conflict for occurrence
                         */
                        conflicts.add(getSeriesConflict(eventInPeriod, recurrenceId, conflictingAttendees, hardConflict));
                    }
                }
            } else {
                if (isInRange(eventInPeriod, event, eventTimeZone)) {
                    //                if (event.getStartDate().getTime() < eventInPeriod.getEndDate().getTime() && event.getEndDate().getTime() > eventInPeriod.getStartDate().getTime()) {
                    /*
                     * add conflict
                     */
                    conflicts.add(getEventConflict(eventInPeriod, conflictingAttendees, hardConflict));
                }
            }
        }
        return conflicts;
    }

    /**
     * Checks for conflicts for a recurring event, considering every occurrence of the series.
     *
     * @param masterEvent The series master event being inserted/updated
     * @param attendeesToCheck The attendees to check
     * @return The conflicts, or an empty list if there are none
     */
    private List<EventConflict> getSeriesConflicts(Event masterEvent, List<Attendee> attendeesToCheck) throws OXException {
        /*
         * resolve occurrences for event series & derive checked period
         */
        List<RecurrenceId> eventRecurrenceIds = asList(Services.getService(RecurrenceService.class)
            .getRecurrenceIterator(masterEvent, initCalendar(TimeZones.UTC, today), null, false));
        if (0 == eventRecurrenceIds.size()) {
            return Collections.emptyList();
        }
        long masterEventDuration = masterEvent.getEndDate().getTime() - masterEvent.getStartDate().getTime();
        Date until = new Date(eventRecurrenceIds.get(eventRecurrenceIds.size() - 1).getValue() + masterEventDuration);
        if (today.after(until)) {
            return Collections.emptyList();
        }
        Date from = new Date(eventRecurrenceIds.get(0).getValue());
        /*
         * search for potentially conflicting events in period
         */
        List<Event> eventsInPeriod = getOverlappingEvents(from, until, attendeesToCheck);
        if (eventsInPeriod.isEmpty()) {
            return Collections.emptyList();
        }
        /*
         * check against each event in period
         */
        List<EventConflict> conflicts = new ArrayList<EventConflict>();
        for (Event eventInPeriod : eventsInPeriod) {
            if (eventInPeriod.getId() == masterEvent.getId()) {
                continue;
            }
            /*
             * determine intersecting attendees
             */
            List<Attendee> conflictingAttendees = getConflictingAttendees(attendeesToCheck, eventInPeriod);
            if (null == conflictingAttendees || 0 == conflictingAttendees.size()) {
                continue;
            }
            boolean hardConflict = isHardConflict(eventInPeriod, conflictingAttendees);
            if (isSeriesMaster(eventInPeriod)) {
                /*
                 * expand & check all occurrences of event series in period
                 */
                long duration = eventInPeriod.getEndDate().getTime() - eventInPeriod.getStartDate().getTime();
                Iterator<RecurrenceId> iterator = Services.getService(RecurrenceService.class)
                    .getRecurrenceIterator(eventInPeriod, initCalendar(TimeZones.UTC, from), initCalendar(TimeZones.UTC, until), false);
                while (iterator.hasNext()) {
                    RecurrenceId recurrenceId = iterator.next();
                    for (RecurrenceId eventRecurrenceId : eventRecurrenceIds) {
                        if (eventRecurrenceId.getValue() >= recurrenceId.getValue() + duration) {
                            /*
                             * further occurrences are also "after" the checked event occurrence
                             */
                            break;
                        } else if (eventRecurrenceId.getValue() + masterEventDuration > recurrenceId.getValue()) {
                            /*
                             * add conflict for occurrence
                             */
                            conflicts.add(getSeriesConflict(eventInPeriod, recurrenceId, conflictingAttendees, hardConflict));
                        }
                    }
                }
            } else {
                for (RecurrenceId eventRecurrenceId : eventRecurrenceIds) {
                    if (eventRecurrenceId.getValue() >= eventInPeriod.getEndDate().getTime()) {
                        /*
                         * further occurrences are also "after" the checked event
                         */
                        break;
                    } else if (eventRecurrenceId.getValue() + masterEventDuration > eventInPeriod.getStartDate().getTime()) {
                        /*
                         * add conflict
                         */
                        conflicts.add(getEventConflict(eventInPeriod, conflictingAttendees, hardConflict));
                    }
                }
            }
        }
        return conflicts;
    }

    /**
     * Creates an event conflict for a single event.
     *
     * @param event The conflicting event
     * @param conflictingAttendees The conflicting attendees to apply
     * @param hardConflict <code>true</code> to mark as <i>hard</i> conflict, <code>false</code>, otherwise
     * @return The event conflict
     */
    private EventConflict getEventConflict(Event event, List<Attendee> conflictingAttendees, boolean hardConflict) throws OXException {
        Event eventData = new Event();
        EventMapper.getInstance().copy(event, eventData, EventField.START_DATE, EventField.END_DATE, EventField.TRANSP, EventField.ALL_DAY, EventField.ID, EventField.CREATED_BY, EventField.SUMMARY, EventField.LOCATION);
        UserizedEvent userizedEvent = new UserizedEvent(session.getSession(), eventData);
        return new EventConflictImpl(userizedEvent, conflictingAttendees, hardConflict);
    }

    /**
     * Creates an event conflict for a specific occurrence of an event series.
     *
     * @param seriesMaster The series master event of the conflicting occurrence
     * @param recurrenceId The recurrence identifier of the conflicting occurrence
     * @param conflictingAttendees The conflicting attendees to apply
     * @param hardConflict <code>true</code> to mark as <i>hard</i> conflict, <code>false</code>, otherwise
     * @return The event conflict
     */
    private EventConflict getSeriesConflict(Event seriesMaster, RecurrenceId recurrenceId, List<Attendee> conflictingAttendees, boolean hardConflict) throws OXException {
        Event eventData = new Event();
        EventMapper.getInstance().copy(seriesMaster, eventData, EventField.TRANSP, EventField.ALL_DAY, EventField.ID, EventField.CREATED_BY, EventField.SUMMARY, EventField.LOCATION);
        eventData.setStartDate(new Date(recurrenceId.getValue()));
        eventData.setEndDate(new Date(recurrenceId.getValue() + (seriesMaster.getEndDate().getTime() - seriesMaster.getStartDate().getTime())));
        eventData.setRecurrenceId(recurrenceId);
        UserizedEvent userizedEvent = new UserizedEvent(session.getSession(), eventData);
        return new EventConflictImpl(userizedEvent, conflictingAttendees, hardConflict);
    }

    /**
     * Gets a list of potentially conflicting events within a specific period where at least one of the checked attendees participate in.
     *
     * @param from The start date of the period
     * @param until The end date of the period
     * @param attendeesToCheck The attendees to check
     * @return The overlapping events of the attendees, or an empty list if there are none
     */
    private List<Event> getOverlappingEvents(Date from, Date until, List<Attendee> attendeesToCheck) throws OXException {
        EventField[] fields = getFields(new EventField[] { EventField.TRANSP, EventField.SUMMARY });
        List<Event> eventsInPeriod = storage.getEventStorage().searchOverlappingEvents(from, until, attendeesToCheck, false, null, fields);
        if (0 == eventsInPeriod.size()) {
            return Collections.emptyList();
        }
        return loadAdditionalEventData(storage, eventsInPeriod, new EventField[] { EventField.ATTENDEES });
    }

    /**
     * Gets those attendees of a conflicting event that are actually part of the current conflict check, and do not have a participation
     * status of {@link ParticipationStatus#DECLINED}.
     *
     * @param checkedAttendees The attendees where conflicts are checked for
     * @param conflictingEvent The conflicting event
     * @return The conflicting attendees, i.e. those checked attendees that also attend the conflicting event
     */
    private List<Attendee> getConflictingAttendees(List<Attendee> checkedAttendees, Event conflictingEvent) throws OXException {
        List<Attendee> conflictingAttendees = new ArrayList<Attendee>();
        List<Attendee> allAttendees = conflictingEvent.containsAttendees() ? conflictingEvent.getAttendees() : storage.getAttendeeStorage().loadAttendees(conflictingEvent.getId());
        for (Attendee checkedAttendee : checkedAttendees) {
            Attendee matchingAttendee = find(allAttendees, checkedAttendee);
            if (null != matchingAttendee && false == ParticipationStatus.DECLINED.equals(matchingAttendee.getPartStat())) {
                conflictingAttendees.add(matchingAttendee);
            }
        }
        return 0 < conflictingAttendees.size() ? conflictingAttendees : null;
    }

    private boolean detailsVisible(Event conflictingEvent) throws OXException {
        int userID = session.getUser().getId();
        /*
         * details available if user is creator or attendee
         */
        if (conflictingEvent.getCreatedBy() == userID || CalendarUtils.isAttendee(conflictingEvent, userID)) {
            return true;
        }
        /*
         * no details for non-public events
         */
        if (false == Classification.PUBLIC.equals(conflictingEvent.getClassification())) {
            return false;
        }
        /*
         * details available based on folder permissions
         */
        if (0 < conflictingEvent.getPublicFolderId()) {
            Permission permission = getFolderPermissions().get(Autoboxing.I(conflictingEvent.getPublicFolderId()));
            return null != permission && Permission.READ_ALL_OBJECTS <= permission.getReadPermission();
        } else {
            for (Attendee attendee : conflictingEvent.getAttendees()) {
                if (CalendarUserType.INDIVIDUAL.equals(attendee.getCuType()) && 0 < attendee.getEntity()) {
                    Permission permission = getFolderPermissions().get(Autoboxing.I(attendee.getFolderID()));
                    if (null != permission && Permission.READ_ALL_OBJECTS <= permission.getReadPermission()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private Map<Integer, Permission> getFolderPermissions() throws OXException {
        if (null == folderPermissions) {
            List<UserizedFolder> folders = Utils.getVisibleFolders(session);
            folderPermissions = new HashMap<Integer, Permission>(folders.size());
            for (UserizedFolder folder : folders) {
                folderPermissions.put(Integer.valueOf(folder.getID()), folder.getOwnPermission());
            }
        }
        return folderPermissions;
    }

    /**
     * Determines which attendees should be included in the conflict check during inserting/updating a certain event.
     * <ul>
     * <li>events marked as {@link Transp#TRANSPARENT} are never checked</li>
     * <li><i>hard</i>-conflicting attendees are always checked, while other internal attendees are included based on
     * {@link CalendarParameters#PARAMETER_IGNORE_CONFLICTS}.</li>
     * </ul>
     *
     * @param event The event being inserted/updated
     * @param attendees The event's list of attendees
     * @return <code>true</code> if the event is in the past, <code>false</code>, otherwise
     */
    private List<Attendee> getAttendeesToCheck(Event event, List<Attendee> attendees) throws OXException {
        if (event.containsTransp() && TimeTransparency.TRANSPARENT.equals(event.getTransp())) {
            return Collections.emptyList();
        }
        boolean includeUserAttendees = false == isIgnoreConflicts(session);
        List<Attendee> checkedAttendees = new ArrayList<Attendee>();
        for (Attendee attendee : attendees) {
            if (isInternal(attendee)) {
                switch (attendee.getCuType()) {
                    case RESOURCE:
                    case ROOM:
                        checkedAttendees.add(attendee);
                        break;
                    case INDIVIDUAL:
                        if (includeUserAttendees) {
                            checkedAttendees.add(attendee);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return checkedAttendees;
    }

    private static boolean isHardConflict(Event conflictingEvent, List<Attendee> conflictingAttendees) {
        for (Attendee attendee : conflictingAttendees) {
            if (CalendarUserType.RESOURCE.equals(attendee.getCuType()) || CalendarUserType.ROOM.equals(attendee.getCuType())) {
                return true;
            }
        }
        return false;
    }

}
