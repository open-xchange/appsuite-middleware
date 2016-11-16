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

import static com.openexchange.chronos.common.CalendarUtils.contains;
import static com.openexchange.chronos.common.CalendarUtils.filter;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.isOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Utils.getFields;
import static com.openexchange.chronos.impl.Utils.i;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.DataAwareRecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.impl.EventMapper;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SortOptions;
import com.openexchange.chronos.service.UserizedEvent;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;

/**
 * {@link FreeBusyPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class FreeBusyPerformer extends AbstractQueryPerformer {

    /** The event fields returned in free/busy queries by default */
    private static final EventField[] FREEBUSY_FIELDS = {
        EventField.CREATED_BY, EventField.ID, EventField.SERIES_ID, EventField.PUBLIC_FOLDER_ID, EventField.COLOR, EventField.CLASSIFICATION,
        EventField.ALL_DAY, EventField.SUMMARY, EventField.START_DATE, EventField.START_TIMEZONE, EventField.END_DATE, EventField.END_TIMEZONE,
        EventField.CATEGORIES, EventField.TRANSP, EventField.LOCATION, EventField.RECURRENCE_ID, EventField.RECURRENCE_RULE
    };

    /** The restricted event fields returned in free/busy queries if the user has no access to the event */
    private static final EventField[] RESTRICTED_FREEBUSY_FIELDS = {
        EventField.CREATED_BY, EventField.ID, EventField.SERIES_ID, EventField.CLASSIFICATION, EventField.ALL_DAY,
        EventField.START_DATE, EventField.START_TIMEZONE, EventField.END_DATE, EventField.END_TIMEZONE,
        EventField.TRANSP, EventField.RECURRENCE_ID, EventField.RECURRENCE_RULE
    };

    /**
     * Initializes a new {@link FreeBusyPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     */
    public FreeBusyPerformer(CalendarSession session, CalendarStorage storage) throws OXException {
        super(session, storage);
    }

    /**
     * Performs the free/busy operation.
     *
     * @param attendees The attendees to query free/busy information for
     * @param from The start date of the period to consider
     * @param until The end date of the period to consider
     * @return The free/busy result
     */
    public Map<Attendee, List<UserizedEvent>> perform(List<Attendee> attendees, Date from, Date until) throws OXException {
        /*
         * prepare & filter internal attendees for lookup
         */
        attendees = session.getEntityResolver().prepare(attendees);
        attendees = filter(attendees, Boolean.TRUE, CalendarUserType.INDIVIDUAL, CalendarUserType.RESOURCE);
        if (0 == attendees.size()) {
            return Collections.emptyMap();
        }
        /*
         * search (potentially) overlapping events for the attendees
         */
        EventField[] fields = getFields(FREEBUSY_FIELDS, EventField.DELETE_EXCEPTION_DATES, EventField.CHANGE_EXCEPTION_DATES, EventField.RECURRENCE_ID, EventField.START_TIMEZONE, EventField.END_TIMEZONE);
        List<Event> events = storage.getEventStorage().searchOverlappingEvents(from, until, attendees, true, new SortOptions(session), fields);
        readAdditionalEventData(events, new EventField[] { EventField.ATTENDEES });
        List<UserizedFolder> visibleFolders = Utils.getVisibleFolders(session);
        /*
         * step through events & build free/busy per requested attendee
         */
        Map<Attendee, List<UserizedEvent>> eventsPerAttendee = new HashMap<Attendee, List<UserizedEvent>>(attendees.size());
        for (Event event : events) {
            for (Attendee attendee : attendees) {
                Attendee eventAttendee = find(event.getAttendees(), attendee);
                if (null == eventAttendee || ParticipationStatus.DECLINED.equals(eventAttendee.getPartStat())) {
                    continue;
                }
                int folderID = 0 < event.getPublicFolderId() ? event.getPublicFolderId() : eventAttendee.getFolderID();
                if (isSeriesMaster(event)) {
                    Iterator<Event> iterator = resolveOccurrences(event, from, until);
                    DefaultRecurrenceData recurrenceData = new DefaultRecurrenceData(event);
                    while (iterator.hasNext()) {
                        Event occurrence = iterator.next();
                        occurrence.setRecurrenceId(new DataAwareRecurrenceId(recurrenceData, occurrence.getRecurrenceId().getValue()));
                        UserizedEvent resultingEvent = getResultingEvent(occurrence, folderID, visibleFolders);
                        com.openexchange.tools.arrays.Collections.put(eventsPerAttendee, attendee, resultingEvent);
                    }
                } else {
                    UserizedEvent resultingEvent = getResultingEvent(event, folderID, visibleFolders);
                    com.openexchange.tools.arrays.Collections.put(eventsPerAttendee, attendee, resultingEvent);
                }
            }
        }
        return eventsPerAttendee;
    }

    /**
     * Gets a resulting userized event for the free/busy result based on the supplied event data. Only a subset of properties is copied
     * over, and a folder identifier is applied optionally, depending on the user's access permissions for the actual event data.
     *
     * @param event The event data to get the result for
     * @param folderId The folder identifier representing the actual attendee's view on the event
     * @param visibleFolders A collection of calendar folder the current session user has access to
     * @return The resulting event representing the free/busy slot
     */
    private UserizedEvent getResultingEvent(Event event, int folderID, Collection<UserizedFolder> visibleFolders) throws OXException {
        int userID = session.getUser().getId();
        /*
         * never anonymize if user is attendee, organizer or creator
         */
        if (event.getCreatedBy() == userID || contains(event.getAttendees(), userID) || isOrganizer(event, userID)) {
            return getResultingEvent(event, folderID, FREEBUSY_FIELDS);
        }
        /*
         * always anonymize if event is classified private/confidential (and user is not attendee, organizer or creator)
         */
        if (null != event.getClassification() && false == Classification.PUBLIC.equals(event.getClassification())) {
            return getResultingEvent(event, -1, RESTRICTED_FREEBUSY_FIELDS);
        }
        /*
         * don't anonymize if event appears in a folder visible to the user
         */
        if (null != visibleFolders && 0 < visibleFolders.size()) {
            for (UserizedFolder folder : visibleFolders) {
                int readPermission = folder.getOwnPermission().getReadPermission();
                if (Permission.READ_ALL_OBJECTS <= readPermission || Permission.READ_OWN_OBJECTS == readPermission && event.getCreatedBy() == userID) {
                    if (0 < event.getPublicFolderId()) {
                        if (event.getPublicFolderId() == i(folder)) {
                            return getResultingEvent(event, folderID, FREEBUSY_FIELDS);
                        }
                    } else {
                        for (Attendee eventAttendee : filter(event.getAttendees(), Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
                            if (eventAttendee.getFolderID() == i(folder)) {
                                return getResultingEvent(event, folderID, FREEBUSY_FIELDS);
                            }
                        }
                    }
                }
            }
        }
        /*
         * anonymize resulting event, otherwise
         */
        return getResultingEvent(event, -1, RESTRICTED_FREEBUSY_FIELDS);
    }

    /**
     * Gets a resulting userized event for the free/busy result based on the supplied event data. Only a subset of properties is copied
     * over, and a folder identifier is applied optionally.
     *
     * @param event The event data to get the result for
     * @param folderId The folder identifier to take over, or a value <code><= 0</code> if unknown
     * @param copiedFields The event fields to take over for the result
     * @return The resulting event representing the free/busy slot
     */
    private UserizedEvent getResultingEvent(Event event, int folderId, EventField[] copiedFields) throws OXException {
        Event resultingEvent = new Event();
        EventMapper.getInstance().copy(event, resultingEvent, copiedFields);
        if (0 < folderId) {
            return new UserizedEvent(session.getSession(), resultingEvent, folderId, null);
        } else {
            return new UserizedEvent(session.getSession(), resultingEvent);
        }
    }

}
