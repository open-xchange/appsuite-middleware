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
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Utils.anonymizeIfNeeded;
import static com.openexchange.chronos.impl.Utils.getFields;
import static com.openexchange.chronos.impl.Utils.getVisibleFolders;
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
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.impl.EventMapper;
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
        List<Event> eventsInPeriod = storage.getEventStorage().searchOverlappingEvents(from, until, attendees, true, new SortOptions(session), fields);
        if (0 == eventsInPeriod.size()) {
            return Collections.emptyMap();
        }
        readAdditionalEventData(eventsInPeriod, new EventField[] { EventField.ATTENDEES });
        List<UserizedFolder> visibleFolders = getVisibleFolders(session);
        /*
         * step through events & build free/busy per requested attendee
         */
        Map<Attendee, List<UserizedEvent>> eventsPerAttendee = new HashMap<Attendee, List<UserizedEvent>>(attendees.size());
        for (Event eventInPeriod : eventsInPeriod) {
            for (Attendee attendee : attendees) {
                Attendee eventAttendee = find(eventInPeriod.getAttendees(), attendee);
                if (null == eventAttendee || ParticipationStatus.DECLINED.equals(eventAttendee.getPartStat())) {
                    continue;
                }
                // TODO: com.openexchange.ajax.appointment.FreeBusyTest.testResourceParticipantStatusFree() still expects 0 for resource attendee
                //                int folderID = chooseFolderID(eventInPeriod, visibleFolders);
                int folderID = CalendarUserType.INDIVIDUAL.equals(eventAttendee.getCuType()) ? chooseFolderID(eventInPeriod, visibleFolders) : -1;
                if (isSeriesMaster(eventInPeriod)) {
                    Iterator<RecurrenceId> iterator = getRecurrenceIterator(eventInPeriod, from, until);
                    while (iterator.hasNext()) {
                        com.openexchange.tools.arrays.Collections.put(eventsPerAttendee, attendee, getResultingOccurrence(eventInPeriod, iterator.next(), folderID));
                    }
                } else {
                    com.openexchange.tools.arrays.Collections.put(eventsPerAttendee, attendee, getResultingEvent(eventInPeriod, folderID));
                }
            }
        }
        return eventsPerAttendee;
    }

    /**
     * Gets a resulting userized event occurrence for the free/busy result based on the supplied data of the master event. Only a subset
     * of properties is copied over, and a folder identifier is applied optionally, depending on the user's access permissions for the
     * actual event data.
     *
     * @param masterEvent The event data to get the result for
     * @param recurrenceId The recurrence identifier of the occurrence
     * @param folderID The folder identifier representing the user's view on the event, or <code>-1</code> if not accessible in any folder
     * @return The resulting event occurrence representing the free/busy slot
     */
    private UserizedEvent getResultingOccurrence(Event masterEvent, RecurrenceId recurrenceId, int folderID) throws OXException {
        UserizedEvent userizedEvent = getResultingEvent(masterEvent, folderID);
        userizedEvent.getEvent().setRecurrenceRule(null);
        userizedEvent.getEvent().removeSeriesId();
        userizedEvent.getEvent().removeClassification();
        userizedEvent.getEvent().setRecurrenceId(recurrenceId);
        userizedEvent.getEvent().setStartDate(new Date(recurrenceId.getValue()));
        userizedEvent.getEvent().setEndDate(new Date(recurrenceId.getValue() + (masterEvent.getStartDate().getTime() - masterEvent.getEndDate().getTime())));
        return userizedEvent;
    }

    /**
     * Gets a resulting userized event for the free/busy result based on the supplied event data. Only a subset of properties is copied
     * over, and a folder identifier is applied optionally, depending on the user's access permissions for the actual event data.
     *
     * @param event The event data to get the result for
     * @param folderID The folder identifier representing the user's view on the event, or <code>-1</code> if not accessible in any folder
     * @return The resulting event representing the free/busy slot
     */
    private UserizedEvent getResultingEvent(Event event, int folderID) throws OXException {
        Event resultingEvent = new Event();
        if (0 < folderID) {
            EventMapper.getInstance().copy(event, resultingEvent, FREEBUSY_FIELDS);
            return anonymizeIfNeeded(new UserizedEvent(session.getSession(), resultingEvent, folderID, null));
        } else {
            EventMapper.getInstance().copy(event, resultingEvent, RESTRICTED_FREEBUSY_FIELDS);
            return new UserizedEvent(session.getSession(), resultingEvent);
        }
    }

    /**
     * Chooses the most appropriate parent folder identifier to render an event in for the current session's user. This is
     * <ul>
     * <li>the common parent folder identifier for an event in a public folder, in case the user has appropriate folder permissions</li>
     * <li><code>-1</code> for an event in a public folder, in case the user has no appropriate folder permissions</li>
     * <li>the user attendee's personal folder identifier for an event in a non-public folder, in case the user is attendee of the event</li>
     * <li>another attendee's personal folder identifier for an event in a non-public folder, in case the user does not attend on his own, but has appropriate folder permissions for this attendee's folder</li>
     * <li><code>-1</code> for an event in a non-public folder, in case the user has no appropriate folder permissions for any of the attendees</li>
     * </ul>
     *
     * @param event The event to choose the folder identifier for
     * @param visibleFolders A collection of calendar folder the current session user has access to
     * @return The chosen folder identifier, or <code>-1</code> if there is none
     */
    private int chooseFolderID(Event event, Collection<UserizedFolder> visibleFolders) throws OXException {
        /*
         * check common folder permissions for events in public folders
         */
        if (0 < event.getPublicFolderId()) {
            UserizedFolder folder = findFolder(visibleFolders, event.getPublicFolderId());
            if (null != folder) {
                int readPermission = folder.getOwnPermission().getReadPermission();
                if (Permission.READ_ALL_OBJECTS <= readPermission || Permission.READ_OWN_OBJECTS == readPermission && event.getCreatedBy() == session.getUser().getId()) {
                    return event.getPublicFolderId();
                }
            }
            return -1;
        }
        /*
         * prefer user's personal folder if user is attendee
         */
        Attendee ownAttendee = find(event.getAttendees(), session.getUser().getId());
        if (null != ownAttendee) {
            return ownAttendee.getFolderID();
        }
        /*
         * choose the most appropriate attendee folder, otherwise
         */
        UserizedFolder chosenFolder = null;
        for (Attendee attendee : event.getAttendees()) {
            UserizedFolder folder = findFolder(visibleFolders, attendee.getFolderID());
            if (null != folder) {
                int readPermission = folder.getOwnPermission().getReadPermission();
                if (Permission.READ_ALL_OBJECTS <= readPermission || Permission.READ_OWN_OBJECTS == readPermission && event.getCreatedBy() == session.getUser().getId()) {
                    chosenFolder = chooseFolder(chosenFolder, folder);
                }
            }
        }
        return null == chosenFolder ? -1 : i(chosenFolder);
    }

    /**
     * Chooses a folder from two candidates based on the <i>highest</i> own permissions.
     *
     * @param folder1 The first candidate, or <code>null</code> to always choose the second candidate
     * @param folder2 The second candidate, or <code>null</code> to always choose the first candidate
     * @return The chosen folder, or <code>null</code> in case both candidates were <code>null</code>
     */
    private static UserizedFolder chooseFolder(UserizedFolder folder1, UserizedFolder folder2) {
        if (null == folder1) {
            return folder2;
        }
        if (null == folder2) {
            return folder1;
        }
        Permission permission1 = folder1.getOwnPermission();
        Permission permission2 = folder2.getOwnPermission();
        if (permission1.getReadPermission() > permission2.getReadPermission()) {
            return folder1;
        }
        if (permission1.getReadPermission() < permission2.getReadPermission()) {
            return folder2;
        }
        if (permission1.getWritePermission() > permission2.getWritePermission()) {
            return folder1;
        }
        if (permission1.getWritePermission() < permission2.getWritePermission()) {
            return folder2;
        }
        if (permission1.getDeletePermission() > permission2.getDeletePermission()) {
            return folder1;
        }
        if (permission1.getDeletePermission() < permission2.getDeletePermission()) {
            return folder2;
        }
        if (permission1.getFolderPermission() > permission2.getFolderPermission()) {
            return folder1;
        }
        if (permission1.getFolderPermission() < permission2.getFolderPermission()) {
            return folder2;
        }
        return permission1.isAdmin() ? folder1 : permission2.isAdmin() ? folder2 : folder1;
    }

    /**
     * Searches a userized folder in a collection of folders by its numerical identifier.
     *
     * @param folders The folders to search
     * @param id The identifier of the folder to lookup
     * @return The matching folder, or <code>null</code> if not found
     */
    private static UserizedFolder findFolder(Collection<UserizedFolder> folders, int id) {
        if (null != folders) {
            String folderID = String.valueOf(id);
            for (UserizedFolder folder : folders) {
                if (folderID.equals(folder.getID())) {
                    return folder;
                }
            }
        }
        return null;
    }

}
