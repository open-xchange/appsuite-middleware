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
import static com.openexchange.chronos.impl.CalendarUtils.isAttendee;
import static com.openexchange.chronos.impl.CalendarUtils.isOrganizer;
import static com.openexchange.chronos.impl.Check.requireCalendarContentType;
import static com.openexchange.chronos.impl.Check.requireDeletePermission;
import static com.openexchange.chronos.impl.Check.requireFolderPermission;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
import static com.openexchange.chronos.impl.Check.requireWritePermission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarSession;
import com.openexchange.chronos.CalendarStorage;
import com.openexchange.chronos.CalendarStorageFactory;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.group.Group;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.resource.Resource;

/**
 * {@link CalendarWriter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarWriter extends CalendarReader {

    private static final int ATTENDEE_PUBLIC_FOLDER_ID = -2;

    /**
     * Initializes a new {@link CalendarWriter}.
     *
     * @param session The session
     */
    public CalendarWriter(CalendarSession session) throws OXException {
        this(session, Services.getService(CalendarStorageFactory.class).create(session.getContext()));
    }

    /**
     * Initializes a new {@link CalendarWriter}.
     *
     * @param session The session
     * @param storage The storage
     */
    public CalendarWriter(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    public UserizedEvent insertEvent(UserizedEvent event) throws OXException {
        return insertEvent(getFolder(event.getFolderId()), event);
    }

    public UserizedEvent updateEvent(int folderID, UserizedEvent event, long clientTimestamp) throws OXException {
        return updateEvent(getFolder(folderID), event, clientTimestamp);
    }

    public UserizedEvent updateAttendee(int folderID, int objectID, Attendee attendee) throws OXException {
        return updateAttendee(getFolder(folderID), objectID, attendee);
    }

    public void deleteEvent(int folderID, int objectID, long clientTimestamp) throws OXException {
        deleteEvent(getFolder(folderID), objectID, clientTimestamp);
    }

    private static Event getTombstone(Event event) {
        Event tombstoneEvent = new Event();
        tombstoneEvent.setId(event.getId());
        tombstoneEvent.setPublicFolderId(event.getPublicFolderId());
        tombstoneEvent.setUid(event.getUid());
        tombstoneEvent.setCreated(event.getCreated());
        tombstoneEvent.setCreatedBy(event.getCreatedBy());
        tombstoneEvent.setClassification(event.getClassification());
        tombstoneEvent.setAttendees(getTombstone(event.getAttendees()));
        return tombstoneEvent;
    }

    private static List<Attendee> getTombstone(List<Attendee> attendees) {
        if (null == attendees) {
            return null;
        }
        List<Attendee> tombstoneAttendees = new ArrayList<Attendee>(attendees.size());
        for (Attendee attendee : attendees) {
            tombstoneAttendees.add(getTombstone(attendee));
        }
        return tombstoneAttendees;
    }

    private static Attendee getTombstone(Attendee attendee) {
        Attendee tombstoneAttendee = new Attendee();
        tombstoneAttendee.setCuType(attendee.getCuType());
        tombstoneAttendee.setEntity(attendee.getEntity());
        tombstoneAttendee.setFolderID(attendee.getFolderID());
        tombstoneAttendee.setMember(attendee.getMember());
        tombstoneAttendee.setPartStat(attendee.getPartStat());
        tombstoneAttendee.setUri(attendee.getUri());
        return tombstoneAttendee;
    }


    protected void deleteEvent(UserizedFolder folder, int objectID, long clientTimestamp) throws OXException {
        requireCalendarContentType(folder);
        requireDeletePermission(folder, Permission.DELETE_OWN_OBJECTS);
        Event originalEvent = storage.loadEvent(objectID, null);
        if (null == originalEvent) {
            throw OXException.notFound(String.valueOf(objectID));//TODO
        }
        requireUpToDateTimestamp(originalEvent, clientTimestamp);
        if (session.getUser().getId() != originalEvent.getCreatedBy()) {
            requireDeletePermission(folder, Permission.DELETE_ALL_OBJECTS);
        }
        User calendarUser = getCalendarUser(folder);
        if (isOrganizer(originalEvent, calendarUser.getId())) {
            /*
             * deletion by organizer
             */
            Event tombstoneEvent = getTombstone(originalEvent);
            Consistency.setModifiedNow(tombstoneEvent, session.getUser().getId());
            storage.insertTombstoneEvent(tombstoneEvent);
            storage.deleteAlarms(objectID);
            storage.deleteEvent(objectID);

        } else if (isAttendee(originalEvent, calendarUser.getId())) {
            /*
             * deletion as attendee
             */
            if (1 == originalEvent.getAttendees().size()) {
                Event tombstoneEvent = getTombstone(originalEvent);
                Consistency.setModifiedNow(tombstoneEvent, calendarUser.getId());
                storage.insertTombstoneEvent(tombstoneEvent);
                storage.deleteAlarms(objectID);
                storage.deleteEvent(objectID);
            } else {
                Attendee attendee = CalendarUtils.find(originalEvent.getAttendees(), calendarUser.getId());
                Event tombstoneEvent = getTombstone(originalEvent);
                Consistency.setModifiedNow(tombstoneEvent, calendarUser.getId());
                tombstoneEvent.setAttendees(Collections.singletonList(getTombstone(attendee)));
                storage.insertTombstoneEvent(tombstoneEvent);
                storage.deleteAlarms(objectID, session.getUser().getId());
                //TODO: remove attendee & update event
                //                event.getAttendees().remove(attendee);
                //                storage.update
            }
        } else {
            /*
             * deletion as ?
             */

        }
    }

    private List<Attendee> prepareAttendees(UserizedFolder folder, List<Attendee> requestedAttendees) throws OXException {
        List<Attendee> preparedAttendees = new ArrayList<Attendee>();
        /*
         * always add at least folder owner / current user as attendee
         */
        {
            User user = SharedType.getInstance().equals(folder.getType()) ? getUser(folder.getCreatedBy()) : session.getUser();
            Attendee attendee = CalendarUtils.applyProperties(new Attendee(), user);
            attendee.setCuType(CalendarUserType.INDIVIDUAL);
            Attendee requestedAttendee = CalendarUtils.find(requestedAttendees, user.getId());
            if (null != requestedAttendee) {
                if (requestedAttendee.containsPartStat()) {
                    attendee.setPartStat(requestedAttendee.getPartStat());
                }
            }
            attendee.setFolderID(PublicType.getInstance().equals(folder.getType()) ? ATTENDEE_PUBLIC_FOLDER_ID : Integer.parseInt(folder.getID()));
            preparedAttendees.add(attendee);
            if (null == requestedAttendees || 0 == requestedAttendees.size()) {
                return preparedAttendees;
            }
        }
        for (Attendee attendee : requestedAttendees) {
            if (0 < attendee.getEntity()) {
                if (CalendarUserType.GROUP.equals(attendee.getCuType())) {
                    /*
                     * verify existence of group attendee & resolve to members
                     */
                    Group group = getGroup(attendee.getEntity());
                    Attendee groupAttendee = new Attendee();
                    groupAttendee.setEntity(group.getIdentifier());
                    groupAttendee.setCuType(CalendarUserType.GROUP);
                    groupAttendee.setPartStat(ParticipationStatus.ACCEPTED);
                    groupAttendee.setUri(CalendarUtils.getCalAddress(session.getContext().getContextId(), group));
                    groupAttendee.setCn(group.getDisplayName());
                    if (false == contains(preparedAttendees, groupAttendee)) {
                        preparedAttendees.add(groupAttendee);
                    }
                    for (int userID : group.getMember()) {
                        User user = getUser(userID);
                        Attendee memberAttendee = CalendarUtils.applyProperties(new Attendee(), user);
                        if (false == contains(preparedAttendees, memberAttendee)) {
                            memberAttendee.setPartStat(ParticipationStatus.NEEDS_ACTION);
                            memberAttendee.setCuType(CalendarUserType.INDIVIDUAL);
                            memberAttendee.setFolderID(PublicType.getInstance().equals(folder.getType()) ? ATTENDEE_PUBLIC_FOLDER_ID : getDefaultFolderID(user));
                            memberAttendee.setMember(CalendarUtils.getCalAddress(session.getContext().getContextId(), group));
                            preparedAttendees.add(memberAttendee);
                        }
                    }
                } else if (CalendarUserType.RESOURCE.equals(attendee.getCuType()) || CalendarUserType.ROOM.equals(attendee.getCuType())) {
                    /*
                     * verify existence of resource attendee
                     */
                    Resource resource = getResource(attendee.getEntity());
                    Attendee resourceAttendee = new Attendee();
                    resourceAttendee.setEntity(resource.getIdentifier());
                    resourceAttendee.setCuType(attendee.getCuType());
                    resourceAttendee.setPartStat(ParticipationStatus.ACCEPTED);
                    resourceAttendee.setUri(CalendarUtils.getCalAddress(session.getContext().getContextId(), resource));
                    resourceAttendee.setCn(resource.getDisplayName());
                    resourceAttendee.setComment(resource.getDescription());
                    if (false == contains(preparedAttendees, resourceAttendee)) {
                        preparedAttendees.add(resourceAttendee);
                    }
                } else {
                    /*
                     * verify existence of user attendee
                     */
                    User user = getUser(attendee.getEntity());
                    Attendee userAttendee = CalendarUtils.applyProperties(new Attendee(), user);
                    if (false == contains(preparedAttendees, userAttendee)) {
                        userAttendee.setCuType(CalendarUserType.INDIVIDUAL);
                        userAttendee.setPartStat(ParticipationStatus.NEEDS_ACTION);
                        userAttendee.setFolderID(PublicType.getInstance().equals(folder.getType()) ? ATTENDEE_PUBLIC_FOLDER_ID : getDefaultFolderID(user));
                        preparedAttendees.add(userAttendee);
                    }
                }
            } else {
                /*
                 * take over external attendee
                 */
                //TODO checks? resolve to internal? generate synthetic id?
                if (false == contains(preparedAttendees, attendee)) {
                    preparedAttendees.add(attendee);
                }
            }
        }
        return preparedAttendees;
    }

    private UserizedEvent insertEvent(UserizedFolder folder, UserizedEvent userizedEvent) throws OXException {
        requireCalendarContentType(folder);
        requireFolderPermission(folder, Permission.CREATE_OBJECTS_IN_FOLDER);
        requireWritePermission(folder, Permission.WRITE_OWN_OBJECTS);
        Event event = userizedEvent.getEvent();
        User calendarUser = getCalendarUser(folder);
        Date now = new Date();
        event.setCreated(now);
        event.setCreatedBy(calendarUser.getId());
        event.setLastModified(now);
        event.setModifiedBy(session.getUser().getId());
        //        Consistency.setOrganizer(event, calendarUser, getProxyUser(folder));
        Consistency.setTimeZone(event, calendarUser);
        Consistency.adjustAllDayDates(event);
        event.setSequence(0);
        event.setUid(Strings.isEmpty(event.getUid()) ? UUID.randomUUID().toString() : event.getUid());
        event.setPublicFolderId(PublicType.getInstance().equals(folder.getType()) ? Integer.parseInt(folder.getID()) : 0);
        event.setAttendees(prepareAttendees(folder, event.getAttendees()));
        /*
         * assign new object identifier
         */
        int objectID = storage.nextObjectID();
        event.setId(objectID);
        if (event.containsRecurrenceRule() && null != event.getRecurrenceRule()) {
            event.setSeriesId(objectID);
        }
        /*
         * insert event & alarms of user
         */
        storage.insertEvent(event);
        if (userizedEvent.containsAlarms() && null != userizedEvent.getAlarms() && 0 < userizedEvent.getAlarms().size()) {
            storage.insertAlarms(objectID, calendarUser.getId(), userizedEvent.getAlarms());
        }
        return readEvent(folder, objectID);
    }

    private UserizedEvent updateEvent(UserizedFolder folder, UserizedEvent userizedEvent, long clientTimestamp) throws OXException {
        requireCalendarContentType(folder);
        requireWritePermission(folder, Permission.WRITE_OWN_OBJECTS);
        Event event = userizedEvent.getEvent();
        Event originalEvent = storage.loadEvent(event.getId(), null);
        requireUpToDateTimestamp(originalEvent, clientTimestamp);
        if (session.getUser().getId() != originalEvent.getCreatedBy()) {
            requireWritePermission(folder, Permission.WRITE_ALL_OBJECTS);
        }
        if (userizedEvent.containsFolderId() && 0 < userizedEvent.getFolderId() && Integer.parseInt(folder.getID()) != userizedEvent.getFolderId()) {
            /*
             * move ...
             */
            //TODO
        }

        User calendarUser = getCalendarUser(folder);
        if (isOrganizer(originalEvent, calendarUser.getId())) {
            /*
             * no organizer or update by (or on behalf of) organizer
             */
            Consistency.setModifiedNow(event, session.getUser().getId());
            if (event.containsAttendees()) {
                event.setAttendees(prepareAttendees(folder, event.getAttendees()));
            }
            storage.updateEvent(event);
            if (userizedEvent.containsAlarms()) {
                List<Alarm> alarms = userizedEvent.getAlarms();
                if (null == alarms) {
                    storage.deleteAlarms(originalEvent.getId(), calendarUser.getId());
                } else {
                    storage.updateAlarms(originalEvent.getId(), calendarUser.getId(), alarms);
                }
            }
        } else if (isAttendee(originalEvent, calendarUser.getId())) {
            /*
             * update by attendee
             */
            //TODO: allowed attendee changes
            throw new OXException();
        } else if (isAttendee(event, calendarUser.getId())) {
            /*
             * party crasher?
             */

        } else {
            /*
             * update by?
             */
        }
        return readEvent(folder, originalEvent.getId());
    }

    private UserizedEvent updateAttendee(UserizedFolder folder, int objectID, Attendee attendee) throws OXException {
        requireCalendarContentType(folder);
        requireWritePermission(folder, Permission.WRITE_OWN_OBJECTS);
        Event originalEvent = storage.loadEvent(objectID, null);
        if (session.getUser().getId() != originalEvent.getCreatedBy()) {
            requireWritePermission(folder, Permission.WRITE_ALL_OBJECTS);
        }
        Attendee originalAttendee = CalendarUtils.find(originalEvent.getAttendees(), attendee);
        if (null == originalAttendee) {
            throw new OXException();
        }
        User calendarUser = getCalendarUser(folder);
        if (calendarUser.getId() != originalAttendee.getEntity()) {
            // hm?
        } else {
            if (attendee.containsComment()) {
                originalAttendee.setComment(attendee.getComment());
            }
            if (attendee.containsPartStat()) {
                originalAttendee.setPartStat(attendee.getPartStat());
            }
            if (session.getUser().getId() != calendarUser.getId()) {
                originalAttendee.setSentBy(CalendarUtils.getCalAddress(calendarUser));
            }
            Event event = new Event();
            event.setId(objectID);
            event.setAttendees(originalEvent.getAttendees());
            Consistency.setModifiedNow(event, session.getUser().getId());
            storage.updateEvent(event);
        }
        return readEvent(folder, originalEvent.getId());
    }

}
