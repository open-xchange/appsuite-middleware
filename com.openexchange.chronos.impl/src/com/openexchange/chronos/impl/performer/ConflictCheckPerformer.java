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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.add;
import static com.openexchange.chronos.common.CalendarUtils.calculateEnd;
import static com.openexchange.chronos.common.CalendarUtils.calculateStart;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.common.CalendarUtils.isAttendee;
import static com.openexchange.chronos.common.CalendarUtils.isFloating;
import static com.openexchange.chronos.common.CalendarUtils.isGroupScheduled;
import static com.openexchange.chronos.common.CalendarUtils.isInRange;
import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.chronos.common.CalendarUtils.isOpaqueTransparency;
import static com.openexchange.chronos.common.CalendarUtils.isOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.isPublicClassification;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.common.CalendarUtils.truncateTime;
import static com.openexchange.chronos.impl.Utils.isCheckConflicts;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.EventConflictImpl;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.java.Reference;

/**
 * {@link ConflictCheckPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ConflictCheckPerformer extends AbstractFreeBusyPerformer {

    private final int maxConflictsPerRecurrence;
    private final int maxConflicts;
    private final int maxAttendeesPerConflict;

    private final Date today;

    private Map<String, Permission> folderPermissions;

    /**
     * Initializes a new {@link ConflictCheckPerformer}.
     *
     * @param session The calendar session
     * @param storage The calendar storage
     */
    public ConflictCheckPerformer(CalendarSession session, CalendarStorage storage) throws OXException {
        super(session, storage);
        this.today = truncateTime(new Date(), Utils.getTimeZone(session));
        maxConflicts = session.getConfig().getMaxConflicts();
        maxAttendeesPerConflict = session.getConfig().getMaxAttendeesPerConflict();
        maxConflictsPerRecurrence = session.getConfig().getMaxConflictsPerRecurrence();
    }

    /**
     * Performs the conflict check.
     *
     * @param event The event being inserted/updated
     * @param attendees The event's list of attendees, or <code>null</code> in case of a not group-scheduled event
     * @return The conflicts, or an empty list if there are none
     */
    public List<EventConflict> perform(Event event, List<Attendee> attendees) throws OXException {
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
        List<EventConflict> conflicts;
        if (isSeriesMaster(event) || null == event.getId() && null != event.getRecurrenceRule()) {
            conflicts = getSeriesConflicts(event, attendeesToCheck);
        } else {
            conflicts = getEventConflicts(event, attendeesToCheck);
        }
        return sortAndTrim(conflicts);
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
        TimeZone eventTimeZone = isFloating(event) || null == event.getStartDate().getTimeZone() ? Utils.getTimeZone(session) : event.getStartDate().getTimeZone();
        Date from = add(new Date(event.getStartDate().getTimestamp()), Calendar.DATE, -1, eventTimeZone);
        Date until = add(new Date(event.getEndDate().getTimestamp()), Calendar.DATE, 1, eventTimeZone);
        if (today.after(until)) {
            return Collections.emptyList();
        }
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
            /*
             * skip checks with event itself or any other event from same series
             */
            if (eventInPeriod.getId().equals(event.getId()) || null != event.getSeriesId() && event.getSeriesId().equals(eventInPeriod.getSeriesId())) {
                continue;
            }
            /*
             * determine intersecting attendees
             */
            Reference<Boolean> hardConflict = new Reference<Boolean>(Boolean.FALSE);
            List<Attendee> conflictingAttendees = getConflictingAttendees(attendeesToCheck, eventInPeriod, hardConflict);
            if (null == conflictingAttendees || 0 == conflictingAttendees.size()) {
                continue;
            }
            if (Boolean.FALSE.equals(hardConflict.getValue()) && false == considerForFreeBusy(eventInPeriod)) {
                continue; // exclude 'soft' conflicts for events classified as 'private' (but keep 'confidential' ones)
            }
            if (isSeriesMaster(eventInPeriod)) {
                /*
                 * expand & check all (non overridden) instances of event series in period
                 */
                Iterator<RecurrenceId> iterator = session.getRecurrenceService().iterateRecurrenceIds(new DefaultRecurrenceData(eventInPeriod), from, until);
                while (iterator.hasNext()) {
                    RecurrenceId recurrenceId = iterator.next();
                    DateTime occurrenceEnd = calculateEnd(eventInPeriod, recurrenceId);
                    if (event.getStartDate().before(occurrenceEnd)) {
                        DateTime occurrenceStart = calculateStart(eventInPeriod, recurrenceId);
                        if (event.getEndDate().after(occurrenceStart)) {
                            /*
                             * add conflict for occurrence
                             */
                            conflicts.add(getSeriesConflict(eventInPeriod, recurrenceId, conflictingAttendees, hardConflict.getValue()));
                        }
                    }
                }
            } else {
                if (isInRange(eventInPeriod, event, eventTimeZone)) {
                    /*
                     * add conflict
                     */
                    conflicts.add(getEventConflict(eventInPeriod, conflictingAttendees, hardConflict.getValue()));
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
        Iterator<RecurrenceId> recurrenceIterator = session.getRecurrenceService().iterateRecurrenceIds(new DefaultRecurrenceData(masterEvent), today, null);
        List<RecurrenceId> eventRecurrenceIds = new ArrayList<RecurrenceId>();
        while (recurrenceIterator.hasNext()) {
            eventRecurrenceIds.add(recurrenceIterator.next());
            try {
                getSelfProtection().checkEventCollection(eventRecurrenceIds);
            } catch (OXException e) {
                break;
            }
        }

        if (0 == eventRecurrenceIds.size()) {
            return Collections.emptyList();
        }
        long masterEventDuration = masterEvent.getEndDate().getTimestamp() - masterEvent.getStartDate().getTimestamp();
        Date until = new Date(eventRecurrenceIds.get(eventRecurrenceIds.size() - 1).getValue().getTimestamp() + masterEventDuration);
        if (today.after(until)) {
            return Collections.emptyList();
        }
        Date from = new Date(eventRecurrenceIds.get(0).getValue().getTimestamp());
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
            /*
             * skip checks with event itself or any other event from same series
             */
            if (eventInPeriod.getId().equals(masterEvent.getId()) || null != eventInPeriod.getSeriesId() && eventInPeriod.getSeriesId().equals(masterEvent.getSeriesId())) {
                continue;
            }
            /*
             * determine intersecting attendees
             */
            Reference<Boolean> hardConflict = new Reference<Boolean>(Boolean.FALSE);
            List<Attendee> conflictingAttendees = getConflictingAttendees(attendeesToCheck, eventInPeriod, hardConflict);
            if (null == conflictingAttendees || 0 == conflictingAttendees.size()) {
                continue;
            }
            if (Boolean.FALSE.equals(hardConflict.getValue()) && false == considerForFreeBusy(eventInPeriod)) {
                continue; // exclude 'soft' conflicts for events classified as 'private' (but keep 'confidential' ones)
            }
            if (isSeriesMaster(eventInPeriod)) {
                /*
                 * expand & check all (non overridden) instances of event series in period
                 */
                int count = 0;
                long duration = eventInPeriod.getEndDate().getTimestamp() - eventInPeriod.getStartDate().getTimestamp();
                Iterator<RecurrenceId> iterator = session.getRecurrenceService().iterateRecurrenceIds(new DefaultRecurrenceData(eventInPeriod), from, until);
                while (iterator.hasNext() && count < maxConflictsPerRecurrence) {
                    RecurrenceId recurrenceId = iterator.next();
                    for (RecurrenceId eventRecurrenceId : eventRecurrenceIds) {
                        if (eventRecurrenceId.getValue().getTimestamp() >= recurrenceId.getValue().getTimestamp() + duration) {
                            /*
                             * further occurrences are also "after" the checked event occurrence
                             */
                            break;
                        } else if (eventRecurrenceId.getValue().getTimestamp() + masterEventDuration > recurrenceId.getValue().getTimestamp()) {
                            /*
                             * add conflict for occurrence
                             */
                            conflicts.add(getSeriesConflict(eventInPeriod, recurrenceId, conflictingAttendees, hardConflict.getValue()));
                            count++;
                        }
                    }
                }
            } else {
                for (RecurrenceId eventRecurrenceId : eventRecurrenceIds) {
                    if (eventRecurrenceId.getValue().getTimestamp() >= eventInPeriod.getEndDate().getTimestamp()) {
                        /*
                         * further occurrences are also "after" the checked event
                         */
                        break;
                    } else if (eventRecurrenceId.getValue().getTimestamp() + masterEventDuration > eventInPeriod.getStartDate().getTimestamp()) {
                        /*
                         * add conflict
                         */
                        conflicts.add(getEventConflict(eventInPeriod, conflictingAttendees, hardConflict.getValue()));
                    }
                }
            }
            getSelfProtection().checkEventCollection(conflicts);
        }
        return conflicts;
    }

    /**
     * Creates an event conflict for a single event.
     *
     * @param event The conflicting event
     * @param conflictingAttendees The conflicting attendees to apply
     * @param hardConflict {@link Boolean#TRUE} to mark as <i>hard</i> conflict, {@link Boolean#FALSE} or <code>null</code>, otherwise
     * @return The event conflict
     */
    private EventConflict getEventConflict(Event event, List<Attendee> conflictingAttendees, Boolean hardConflict) throws OXException {
        Event eventData = EventMapper.getInstance().copy(event, null, EventField.ID, EventField.SERIES_ID, EventField.RECURRENCE_ID,
            EventField.START_DATE, EventField.END_DATE, EventField.TRANSP, EventField.CREATED_BY);
        if (detailsVisible(event)) {
            eventData = EventMapper.getInstance().copy(event, eventData, EventField.SUMMARY, EventField.LOCATION);
            eventData.setFolderId(chooseFolderID(event));
        }
        return new EventConflictImpl(eventData, conflictingAttendees, null != hardConflict ? hardConflict.booleanValue() : false);
    }

    /**
     * Creates an event conflict for a specific occurrence of an event series.
     *
     * @param seriesMaster The series master event of the conflicting occurrence
     * @param recurrenceId The recurrence identifier of the conflicting occurrence
     * @param conflictingAttendees The conflicting attendees to apply
     * @param hardConflict {@link Boolean#TRUE} to mark as <i>hard</i> conflict, {@link Boolean#FALSE} or <code>null</code>, otherwise
     * @return The event conflict
     */
    private EventConflict getSeriesConflict(Event seriesMaster, RecurrenceId recurrenceId, List<Attendee> conflictingAttendees, Boolean hardConflict) throws OXException {
        Event eventData = new Event();
        eventData.setStartDate(calculateStart(seriesMaster, recurrenceId));
        eventData.setEndDate(calculateEnd(seriesMaster, recurrenceId));
        eventData.setId(seriesMaster.getId());
        eventData.setRecurrenceId(recurrenceId);
        eventData.setCreatedBy(seriesMaster.getCreatedBy());
        eventData.setTransp(seriesMaster.getTransp());
        if (detailsVisible(seriesMaster)) {
            eventData.setSummary(seriesMaster.getSummary());
            eventData.setLocation(seriesMaster.getLocation());
            eventData.setFolderId(chooseFolderID(seriesMaster));
        }
        return new EventConflictImpl(eventData, conflictingAttendees, null != hardConflict ? hardConflict.booleanValue() : false);
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
        EventField[] fields = getFields(new EventField[] { EventField.TRANSP, EventField.SUMMARY, EventField.LOCATION, EventField.ORGANIZER });
        List<Event> eventsInPeriod = storage.getEventStorage().searchOverlappingEvents(attendeesToCheck, false, new SearchOptions().setRange(from, until), fields);
        if (0 == eventsInPeriod.size()) {
            return Collections.emptyList();
        }
        return readAttendeeData(eventsInPeriod, Boolean.TRUE);
    }

    /**
     * Gets those attendees of a conflicting event that are actually part of the current conflict check, and do not have a participation
     * status of {@link ParticipationStatus#DECLINED}.
     *
     * @param checkedAttendees The attendees where conflicts are checked for
     * @param conflictingEvent The conflicting event
     * @param hardConflict A reference that gets set to {@link Boolean#TRUE} if the conflicting attendees will indicate a <i>hard</i> conflict
     * @return The conflicting attendees, i.e. those checked attendees that also attend the conflicting event
     */
    private List<Attendee> getConflictingAttendees(List<Attendee> checkedAttendees, Event conflictingEvent, Reference<Boolean> hardConflict) throws OXException {
        List<Attendee> conflictingAttendees = new ArrayList<Attendee>();
        List<Attendee> allAttendees = conflictingEvent.containsAttendees() ? conflictingEvent.getAttendees() : storage.getAttendeeStorage().loadAttendees(conflictingEvent.getId());
        for (Attendee checkedAttendee : checkedAttendees) {
            if (isHardConflict(checkedAttendee)) {
                Attendee matchingAttendee = find(allAttendees, checkedAttendee);
                if (null != matchingAttendee && false == ParticipationStatus.DECLINED.equals(matchingAttendee.getPartStat())) {
                    hardConflict.setValue(Boolean.TRUE);
                    conflictingAttendees.add(0, matchingAttendee);
                }
            } else if (maxAttendeesPerConflict > conflictingAttendees.size()) {
                if (isGroupScheduled(conflictingEvent)) {
                    Attendee matchingAttendee = find(allAttendees, checkedAttendee);
                    if (null != matchingAttendee && false == ParticipationStatus.DECLINED.equals(matchingAttendee.getPartStat())) {
                        conflictingAttendees.add(matchingAttendee);
                    }
                } else if (matches(conflictingEvent.getCalendarUser(), checkedAttendee.getEntity())) {
                    conflictingAttendees.add(session.getEntityResolver().prepareUserAttendee(checkedAttendee.getEntity()));
                }
            }
        }
        if (maxAttendeesPerConflict < conflictingAttendees.size()) {
            return conflictingAttendees.subList(0, maxAttendeesPerConflict);
        }
        return 0 < conflictingAttendees.size() ? conflictingAttendees : null;
    }

    /**
     * Gets a value indicating whether detailed event data is available for the current user based on the user's access rights.
     *
     * @param conflictingEvent The conflicting event to decide whether details are visible or not
     * @return <code>true</code> if details are available, <code>false</code>, otherwise
     */
    private boolean detailsVisible(Event conflictingEvent) throws OXException {
        int userID = session.getUserId();
        /*
         * details available if user is creator or attendee
         */
        if (matches(conflictingEvent.getCalendarUser(), userID) || matches(conflictingEvent.getCreatedBy(), userID) || isAttendee(conflictingEvent, userID) || isOrganizer(conflictingEvent, userID)) {
            return true;
        }
        /*
         * no details for non-public events
         */
        if (false == isPublicClassification(conflictingEvent)) {
            return false;
        }
        /*
         * details available based on folder permissions
         */
        if (null != conflictingEvent.getFolderId()) {
            Permission permission = getFolderPermissions().get(conflictingEvent.getFolderId());
            return null != permission && Permission.READ_ALL_OBJECTS <= permission.getReadPermission();
        } else if (isGroupScheduled(conflictingEvent)) {
            for (Attendee attendee : conflictingEvent.getAttendees()) {
                if (CalendarUserType.INDIVIDUAL.equals(attendee.getCuType()) && 0 < attendee.getEntity()) {
                    Permission permission = getFolderPermissions().get(attendee.getFolderId());
                    if (null != permission && Permission.READ_ALL_OBJECTS <= permission.getReadPermission()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Map<String, Permission> getFolderPermissions() throws OXException {
        if (null == folderPermissions) {
            List<CalendarFolder> folders = getVisibleFolders();
            folderPermissions = new HashMap<String, Permission>(folders.size());
            for (CalendarFolder folder : folders) {
                folderPermissions.put(folder.getId(), folder.getOwnPermission());
            }
        }
        return folderPermissions;
    }

    /**
     * Determines which attendees should be included in the conflict check during inserting/updating a certain event.
     * <ul>
     * <li>events marked as {@link Transp#TRANSPARENT} are never checked</li>
     * <li><i>hard</i>-conflicting attendees are always checked, while other internal attendees are included based on
     * {@link CalendarParameters#PARAMETER_CHECK_CONFLICTS}.</li>
     * </ul>
     *
     * @param event The event being inserted/updated
     * @param attendees The event's list of attendees, or <code>null</code> in case of a not group-scheduled event
     * @return <code>true</code> if the event is in the past, <code>false</code>, otherwise
     */
    private List<Attendee> getAttendeesToCheck(Event event, List<Attendee> attendees) throws OXException {
        if (false == isOpaqueTransparency(event)) {
            return Collections.emptyList();
        }
        boolean includeUserAttendees = isCheckConflicts(session);
        List<Attendee> checkedAttendees = new ArrayList<Attendee>();
        if (null == attendees || attendees.isEmpty()) {
            /*
             * assume simple, not group-scheduled event
             */
            if (false == includeUserAttendees || null == event.getCalendarUser()) {
                return Collections.emptyList();
            }
            return Collections.singletonList(session.getEntityResolver().prepareUserAttendee(event.getCalendarUser().getEntity()));
        }
        for (Attendee attendee : attendees) {
            if (isInternal(attendee) && (includeUserAttendees || CalendarUserType.RESOURCE.equals(attendee.getCuType()) || CalendarUserType.ROOM.equals(attendee.getCuType()))) {
                checkedAttendees.add(attendee);
            }
        }
        return checkedAttendees;
    }

    private List<EventConflict> sortAndTrim(List<EventConflict> conflicts) {
        if (null != conflicts && 1 < conflicts.size()) {
            Collections.sort(conflicts, HARD_CONFLICTS_FIRST_COMPARATOR);
            if (maxConflicts < conflicts.size()) {
                conflicts = conflicts.subList(0, maxConflicts);
            }
        }
        return conflicts;
    }

    /**
     * Gets a value indicating whether a conflicting attendee would indicate a <i>hard</i> conflict or not.
     *
     * @param conflictingAttendee The attendee to check
     * @return <code>true</code> if the conflicting attendee would indicate a <i>hard</i> conflict, <code>false</code>, otherwise
     */
    private static boolean isHardConflict(Attendee conflictingAttendee) {
        return CalendarUserType.RESOURCE.equals(conflictingAttendee.getCuType()) || CalendarUserType.ROOM.equals(conflictingAttendee.getCuType());
    }

    /**
     * A comparator for event conflicts that orders <i>hard</i> conflicts first, otherwise compares the conflicting event's start dates.
     */
    private static final Comparator<EventConflict> HARD_CONFLICTS_FIRST_COMPARATOR = new Comparator<EventConflict>() {

        @Override
        public int compare(EventConflict conflict1, EventConflict conflict2) {
            if (conflict1.isHardConflict() && false == conflict2.isHardConflict()) {
                return -1;
            }
            if (false == conflict1.isHardConflict() && conflict2.isHardConflict()) {
                return 1;
            }
            return Long.compare(conflict1.getConflictingEvent().getStartDate().getTimestamp(), conflict2.getConflictingEvent().getStartDate().getTimestamp());
        }
    };

}
