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

import static com.openexchange.chronos.impl.CalendarUtils.contains;
import static com.openexchange.chronos.impl.Check.requireCalendarContentType;
import static com.openexchange.chronos.impl.Check.requireFolderPermission;
import static com.openexchange.chronos.impl.Check.requireReadPermission;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarService;
import com.openexchange.chronos.CalendarStorage;
import com.openexchange.chronos.CalendarStorageFactory;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventID;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
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
public class CalendarReader {

    protected final ServerSession session;
    protected final CalendarStorage storage;

    /**
     * Initializes a new {@link CalendarReader}.
     *
     * @param session The session
     */
    public CalendarReader(ServerSession session) throws OXException {
        this(session, Services.getService(CalendarStorageFactory.class).create(session.getContext()));
    }

    /**
     * Initializes a new {@link CalendarReader}.
     *
     * @param session The session
     * @param storage The storage
     */
    public CalendarReader(ServerSession session, CalendarStorage storage) {
        super();
        this.session = session;
        this.storage = storage;
    }

    public UserizedEvent readEvent(EventID eventID, EventField[] fields) throws OXException {
        return readEvent(eventID.getFolderID(), eventID.getObjectID(), fields);
    }

    public UserizedEvent readEvent(int folderID, int objectID, EventField[] fields) throws OXException {
        return readEvent(getFolder(folderID), objectID, fields);
    }

    public UserizedEvent readEvent(UserizedFolder folder, int objectID, EventField[] fields) throws OXException {
        requireCalendarContentType(folder);
        requireFolderPermission(folder, Permission.READ_FOLDER);
        requireReadPermission(folder, Permission.READ_OWN_OBJECTS);
        Event event = storage.loadEvent(objectID, fields);
        if (session.getUserId() != event.getCreatedBy()) {
            requireReadPermission(folder, Permission.READ_ALL_OBJECTS);
        }
        return userize(event, folder);
    }

    List<UserizedEvent> readEventsInFolder(int folderID, Date from, Date until, Date updatedSince, EventField[] fields) throws OXException {
        return readEventsInFolder(getFolder(folderID), from, until, updatedSince, fields);
    }

    List<UserizedEvent> readEventsInFolder(UserizedFolder folder, Date from, Date until, Date updatedSince, EventField[] fields) throws OXException {
        requireCalendarContentType(folder);
        requireFolderPermission(folder, Permission.READ_FOLDER);
        requireReadPermission(folder, Permission.READ_OWN_OBJECTS);
        int createdBy = Permission.READ_ALL_OBJECTS > folder.getOwnPermission().getReadPermission() ? session.getUserId() : -1;
        List<Event> events = storage.loadEventsInFolder(Integer.parseInt(folder.getID()), from, until, createdBy, updatedSince, fields);
        return userize(events, folder);
    }

    List<UserizedEvent> readDeletedEventsInFolder(int folderID, Date from, Date until, Date deletedSince) throws OXException {
        return readDeletedEventsInFolder(getFolder(folderID), from, until, deletedSince);
    }

    List<UserizedEvent> readDeletedEventsInFolder(UserizedFolder folder, Date from, Date until, Date deletedSince) throws OXException {
        requireCalendarContentType(folder);
        requireFolderPermission(folder, Permission.READ_FOLDER);
        requireReadPermission(folder, Permission.READ_OWN_OBJECTS);
        int createdBy = Permission.READ_ALL_OBJECTS > folder.getOwnPermission().getReadPermission() ? session.getUserId() : -1;
        List<Event> events = storage.loadDeletedEventsInFolder(Integer.parseInt(folder.getID()), from, until, createdBy, deletedSince);
        return userize(events, folder);
    }

    List<UserizedEvent> readEventsOfUser(int userID, Date from, Date until, Date updatedSince, EventField[] fields) throws OXException {
        List<Event> events = storage.loadEventsOfUser(userID, from, until, updatedSince, fields);
        return userize(events, session.getUserId());
    }

    List<UserizedEvent> readDeletedEventsOfUser(int userID, Date from, Date until, Date deletedSince) throws OXException {
        List<Event> events = storage.loadDeletedEventsOfUser(userID, from, until, deletedSince);
        return userize(events, session.getUserId());
    }

    private List<UserizedEvent> userize(List<Event> events, int forUser) throws OXException {
        List<UserizedEvent> userizedEvents = new ArrayList<UserizedEvent>(events.size());
        for (Event event : events) {
            userizedEvents.add(userize(event, forUser));
        }
        return userizedEvents;
    }

    private List<UserizedEvent> userize(List<Event> events, UserizedFolder inFolder) throws OXException {
        List<UserizedEvent> userizedEvents = new ArrayList<UserizedEvent>(events.size());
        for (Event event : events) {
            userizedEvents.add(userize(event, inFolder));
        }
        return userizedEvents;
    }

    private UserizedEvent userize(Event event, UserizedFolder inFolder) throws OXException {
        /*
         * load alarms
         * - of folder owner for shared/personal folder
         * - of session user for public folder, if user is attendee
         * - otherwise not (not attending)
         */
        int targetAttendee = getTargetAttendee(inFolder, event.getAttendees());
        List<Alarm> alarms = 0 < targetAttendee ? storage.loadAlarms(targetAttendee, event.getId()) : null;
        /*
         * build userized event & return
         */
        return new UserizedEvent(session, targetAttendee, Integer.parseInt(inFolder.getID()), event, alarms);
    }

    private UserizedEvent userize(Event event, int forUser) throws OXException {
        int folderId = event.getPublicFolderId();
        List<Alarm> alarms = null;
        Attendee userAttendee = CalendarUtils.find(event.getAttendees(), forUser);
        if (null != userAttendee) {
            alarms = storage.loadAlarms(event.getId(), userAttendee.getEntity());
            if (0 < userAttendee.getFolderID()) {
                folderId = userAttendee.getFolderID();
            }
        }
        return new UserizedEvent(session, forUser, folderId, event, alarms);
    }

    private static int getTargetAttendee(UserizedFolder folder, List<Attendee> attendees) {
        int userId = folder.getSession().getUserId();
        if (PrivateType.getInstance().equals(folder.getType()) && contains(attendees, userId)) {
            return userId;
        } else if (SharedType.getInstance().equals(folder.getType()) && contains(attendees, folder.getCreatedBy())) {
            return folder.getCreatedBy();
        } else if (PublicType.getInstance().equals(folder.getType()) && contains(attendees, userId)) {
            return userId;
        }
        return -1;
    }

    protected UserizedFolder getFolder(int folderID) throws OXException {
        return Services.getService(FolderService.class).getFolder(FolderStorage.REAL_TREE_ID, String.valueOf(folderID), session, null);
    }

}
