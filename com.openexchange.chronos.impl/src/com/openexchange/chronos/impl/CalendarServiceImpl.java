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

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.impl.CalendarUtils.containsAttendee;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarService;
import com.openexchange.chronos.CalendarStorage;
import com.openexchange.chronos.CalendarStorageFactory;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventID;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CalendarService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarServiceImpl implements CalendarService {

    public UserizedEvent createEvent(ServerSession session, int folderID, Event event, List<Alarm> alarms) throws OXException {
        /*
         * get & check requested folder
         */
        UserizedFolder folder = getFolder(session, folderID);
        if (false == CalendarContentType.class.isInstance(folder.getContentType())) {
            throw new OXException();
        }
        if (Permission.CREATE_OBJECTS_IN_FOLDER > folder.getOwnPermission().getFolderPermission()) {
            throw new OXException();
        }
        /*
         * insert event data
         */
        CalendarStorage storage = getCalendarStorage(session);
        int objectID = storage.insertEvent(event);
        /*
         * insert alarms
         */
        if (null != alarms && 0 < alarms.size()) {
            int targetAttendee = getTargetAttendee(session, folder, event.getAttendees());
            if (0 < targetAttendee) {
                storage.insertAlarms(targetAttendee, objectID, alarms);
            }
        }
        /*
         * return reloaded event
         */
        return getEvent(session, folder, objectID);
    }

    public UserizedEvent updateEvent(ServerSession session, int folderID, Event event, List<Alarm> alarms) throws OXException {
        return updateEvent(session, getFolder(session, folderID), event, alarms);
    }

    private UserizedEvent updateEvent(ServerSession session, UserizedFolder folder, Event event, List<Alarm> alarms) throws OXException {

        /*
         * return reloaded event
         */
        return getEvent(session, folder, event.getId());
    }

    @Override
    public List<UserizedEvent> getEvents(ServerSession session, List<EventID> eventIDs) throws OXException {
        List<UserizedEvent> events = new ArrayList<UserizedEvent>(eventIDs.size());
        for (EventID eventID : eventIDs) {
            events.add(getEvent(session, eventID.getFolderID(), eventID.getObjectID()));
        }
        return events;
    }

    @Override
    public List<UserizedEvent> getEvents(ServerSession session, int folderID, Date from, Date until) throws OXException {
        return getEvents(session, getFolder(session, folderID), from, until);
    }

    @Override
    public UserizedEvent getEvent(ServerSession session, int folderID, int objectID) throws OXException {
        return getEvent(session, getFolder(session, folderID), objectID);
    }

    private UserizedEvent getEvent(ServerSession session, UserizedFolder folder, int objectID) throws OXException {
        /*
         * check requested folder
         */
        requireCalendarContentType(folder);
        requireFolderPermission(folder, Permission.READ_FOLDER);
        requireReadPermission(folder, Permission.READ_OWN_OBJECTS);
        /*
         * load event data from storage
         */
        CalendarStorage storage = getCalendarStorage(session);
        Event event = storage.loadEvent(objectID);
        if (Permission.READ_ALL_OBJECTS > folder.getOwnPermission().getReadPermission() && session.getUserId() != event.getCreatedBy()) {
            throw new OXException();
        }
        return userize(session, storage, folder, event);
    }

    private List<UserizedEvent> getEvents(ServerSession session, UserizedFolder folder, Date from, Date until) throws OXException {
        /*
         * check requested folder
         */
        requireCalendarContentType(folder);
        requireFolderPermission(folder, Permission.READ_FOLDER);
        requireReadPermission(folder, Permission.READ_OWN_OBJECTS);
        /*
         * load event data from storage & return userized events
         */
        boolean onlyOwn = Permission.READ_ALL_OBJECTS > folder.getOwnPermission().getReadPermission();
        CalendarStorage storage = getCalendarStorage(session);
        List<Event> events = storage.loadEvents(session.getUserId(), Integer.parseInt(folder.getID()), from, until, onlyOwn);
        return userize(session, storage, folder, events);
    }

    private static List<UserizedEvent> userize(ServerSession session, CalendarStorage storage, UserizedFolder folder, List<Event> events) throws OXException {
        List<UserizedEvent> userizedEvents = new ArrayList<UserizedEvent>(events.size());
        for (Event event : events) {
            userizedEvents.add(userize(session, storage, folder, event));
        }
        return userizedEvents;
    }

    private static UserizedEvent userize(ServerSession session, CalendarStorage storage, UserizedFolder folder, Event event) throws OXException {
        /*
         * load alarms
         * - of folder owner for shared/personal folder
         * - of session user for public folder, if user is attendee
         * - otherwise not (not attending)
         */
        int targetAttendee = getTargetAttendee(session, folder, event.getAttendees());
        List<Alarm> alarms = 0 < targetAttendee ? storage.loadAlarms(targetAttendee, event.getId()) : null;
        /*
         * build userized event & return
         */
        UserizedEvent userizedEvent = new UserizedEvent(event);
        userizedEvent.setFolderId(Integer.valueOf(folder.getID()));
        userizedEvent.setAlarms(alarms);
        return userizedEvent;
    }

    private static int getTargetAttendee(ServerSession session, UserizedFolder folder, List<Attendee> attendees) {
        if (PrivateType.getInstance().equals(folder.getType()) && containsAttendee(attendees, session.getUserId())) {
            return session.getUserId();
        } else if (SharedType.getInstance().equals(folder.getType()) && containsAttendee(attendees, folder.getCreatedBy())) {
            return folder.getCreatedBy();
        } else if (PublicType.getInstance().equals(folder.getType()) && containsAttendee(attendees, session.getUserId())) {
            return session.getUserId();
        }
        return -1;
    }

    private static UserizedFolder getFolder(ServerSession session, int folderID) throws OXException {
        return Services.getService(FolderService.class).getFolder(FolderStorage.REAL_TREE_ID, String.valueOf(folderID), session, null);
    }

    private static CalendarStorage getCalendarStorage(ServerSession session) throws OXException {
        return Services.getService(CalendarStorageFactory.class).create(session);
    }

    private static void requireCalendarContentType(UserizedFolder folder) throws OXException {
        if (false == CalendarContentType.class.isInstance(folder.getContentType())) {
            throw new OXException();
        }
    }

    private static void requireFolderPermission(UserizedFolder folder, int requiredPermission) throws OXException {
        if (folder.getOwnPermission().getFolderPermission() < requiredPermission) {
            throw new OXException();
        }
    }

    private static void requireReadPermission(UserizedFolder folder, int requiredPermission) throws OXException {
        if (folder.getOwnPermission().getReadPermission() < requiredPermission) {
            throw new OXException();
        }
    }

}
