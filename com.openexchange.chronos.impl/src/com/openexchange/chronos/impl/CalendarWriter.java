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

import static com.openexchange.chronos.impl.CalendarUtils.find;
import static com.openexchange.chronos.impl.CalendarUtils.getUserIDs;
import static com.openexchange.chronos.impl.CalendarUtils.i;
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
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventID;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;

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

    public UserizedEvent updateEvent(EventID eventID, UserizedEvent event, long clientTimestamp) throws OXException {
        return updateEvent(getFolder(eventID.getFolderID()), eventID.getObjectID(), event, clientTimestamp);
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
        Event originalEvent = storage.getEventStorage().loadEvent(objectID, null);
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
            storage.getEventStorage().insertTombstoneEvent(tombstoneEvent);
            storage.getAlarmStorage().deleteAlarms(objectID);
            storage.getEventStorage().deleteEvent(objectID);

        } else if (isAttendee(originalEvent, calendarUser.getId())) {
            /*
             * deletion as attendee
             */
            if (1 == originalEvent.getAttendees().size()) {
                Event tombstoneEvent = getTombstone(originalEvent);
                Consistency.setModifiedNow(tombstoneEvent, calendarUser.getId());
                storage.getEventStorage().insertTombstoneEvent(tombstoneEvent);
                storage.getAlarmStorage().deleteAlarms(objectID);
                storage.getEventStorage().deleteEvent(objectID);
            } else {
                Attendee attendee = CalendarUtils.find(originalEvent.getAttendees(), calendarUser.getId());
                Event tombstoneEvent = getTombstone(originalEvent);
                Consistency.setModifiedNow(tombstoneEvent, calendarUser.getId());
                tombstoneEvent.setAttendees(Collections.singletonList(getTombstone(attendee)));
                storage.getEventStorage().insertTombstoneEvent(tombstoneEvent);
                storage.getAlarmStorage().deleteAlarms(objectID, session.getUser().getId());
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

    private UserizedEvent insertEvent(UserizedFolder folder, UserizedEvent userizedEvent) throws OXException {
        requireCalendarContentType(folder);
        requireFolderPermission(folder, Permission.CREATE_OBJECTS_IN_FOLDER);
        requireWritePermission(folder, Permission.WRITE_OWN_OBJECTS);
        Event event = userizedEvent.getEvent();
        User calendarUser = getCalendarUser(folder);
        Date now = new Date();
        Consistency.setCreated(now, event, calendarUser.getId());
        Consistency.setModified(now, event, session.getUser().getId());
        if (null == event.getOrganizer()) {
            Consistency.setOrganizer(event, calendarUser, session.getUser());
        }
        Consistency.setTimeZone(event, calendarUser);
        Consistency.adjustAllDayDates(event);
        event.setSequence(0);
        if (Strings.isNotEmpty(event.getUid())) {
            if (0 < resolveUid(event.getUid())) {
                throw OXException.general("Duplicate uid"); //TODO
            }
        } else {
            event.setUid(UUID.randomUUID().toString());
        }
        event.setPublicFolderId(PublicType.getInstance().equals(folder.getType()) ? i(folder) : 0);
        /*
         * assign new object identifier
         */
        int objectID = storage.nextObjectID();
        event.setId(objectID);
        if (event.containsRecurrenceRule() && null != event.getRecurrenceRule()) {
            event.setSeriesId(objectID);
        }
        /*
         * insert event, attendees & alarms of user
         */
        storage.getEventStorage().insertEvent(event);
        storage.getAttendeeStorage().insertAttendees(objectID, new AttendeeHelper(session, folder, null, event.getAttendees()).getAttendeesToInsert());
        if (userizedEvent.containsAlarms() && null != userizedEvent.getAlarms() && 0 < userizedEvent.getAlarms().size()) {
            storage.getAlarmStorage().insertAlarms(objectID, calendarUser.getId(), userizedEvent.getAlarms());
        }
        return readEvent(folder, objectID);
    }

    private UserizedEvent updateEvent(UserizedFolder folder, int objectID, UserizedEvent userizedEvent, long clientTimestamp) throws OXException {
        requireCalendarContentType(folder);
        requireWritePermission(folder, Permission.WRITE_OWN_OBJECTS);
        Event event = userizedEvent.getEvent();
        Event originalEvent = storage.getEventStorage().loadEvent(objectID, null);
        requireUpToDateTimestamp(originalEvent, clientTimestamp);
        if (session.getUser().getId() != originalEvent.getCreatedBy()) {
            requireWritePermission(folder, Permission.WRITE_ALL_OBJECTS);
        }

        User calendarUser = getCalendarUser(folder);
        Date now = new Date();

        if (userizedEvent.containsFolderId() && 0 < userizedEvent.getFolderId() && i(folder) != userizedEvent.getFolderId()) {
            /*
             * move ...
             */
            if (session.getUser().getId() == originalEvent.getCreatedBy()) {
                requireDeletePermission(folder, Permission.DELETE_OWN_OBJECTS);
            } else {
                requireDeletePermission(folder, Permission.DELETE_ALL_OBJECTS);
            }
            UserizedFolder targetFolder = getFolder(userizedEvent.getFolderId());
            User targetCalendarUser = getCalendarUser(targetFolder);
            requireCalendarContentType(targetFolder);
            requireFolderPermission(targetFolder, Permission.CREATE_OBJECTS_IN_FOLDER);

            if (PublicType.getInstance().equals(folder.getType()) && false == PublicType.getInstance().equals(targetFolder.getType()) || 
                PublicType.getInstance().equals(targetFolder.getType()) && false == PublicType.getInstance().equals(folder.getType())) {
                throw OXException.general("unsupported move");
            }
            if (PublicType.getInstance().equals(folder.getType()) && PublicType.getInstance().equals(targetFolder.getType())) {
                /*
                 * move from one public folder to another, update event's folder
                 */
                Event eventUpdate = new Event();
                eventUpdate.setId(objectID);
                eventUpdate.setPublicFolderId(i(targetFolder));
                Consistency.setModified(now, eventUpdate, calendarUser.getId());
                storage.getEventStorage().updateEvent(eventUpdate);
            } else if (calendarUser.getId() == targetCalendarUser.getId()) {
                /*
                 * move from one personal folder to another, update attendee's folder
                 */
                List<Attendee> originalAttendees = storage.getAttendeeStorage().loadAttendees(objectID);
                Attendee attendee = find(originalAttendees, calendarUser.getId());
                if (null == attendee) {
                    throw OXException.notFound(calendarUser.toString());
                }
                attendee.setFolderID(i(targetFolder));
                storage.getAttendeeStorage().updateAttendee(objectID, attendee);
            } else {
                /*
                 * move from one personal folder to another user's personal folder, add corresponding default attendee
                 */
                List<Attendee> originalAttendees = storage.getAttendeeStorage().loadAttendees(objectID);
                //                Attendee attendee = find(originalAttendees, calendarUser.getId());
                //                if (null == attendee) {
                //                    throw OXException.notFound(calendarUser.toString());
                //                }
//                storage.getAttendeeStorage().insertTombstoneAttendees(objectID, Collections.singletonList(getTombstone(attendee)));
//                storage.getAttendeeStorage().deleteAttendees(objectID, Collections.singletonList(attendee));
                Attendee newAttendee = find(originalAttendees, targetCalendarUser.getId());
                if (null != newAttendee) {
                    newAttendee.setFolderID(i(targetFolder));
                    storage.getAttendeeStorage().updateAttendee(objectID, newAttendee);
                } else {
                    newAttendee = new AttendeeHelper(session, targetFolder, null, null).getAttendeesToInsert().get(0);
                    storage.getAttendeeStorage().insertAttendees(objectID, Collections.singletonList(newAttendee));
                }
            }
            /*
             * take over new parent folder
             */
            folder = targetFolder;
        }
        if (isOrganizer(originalEvent, calendarUser.getId())) {
            /*
             * no organizer or update by (or on behalf of) organizer
             */
            if (event.containsAttendees()) {
                /*
                 * process any attendee updates
                 */
                List<Attendee> originalAttendees = storage.getAttendeeStorage().loadAttendees(objectID);
                AttendeeHelper attendeeHelper = new AttendeeHelper(session, folder, originalAttendees, event.getAttendees());
                List<Attendee> attendeesToDelete = attendeeHelper.getAttendeesToDelete();
                if (null != attendeesToDelete && 0 < attendeesToDelete.size()) {
                    /*
                     * insert tombstone entries
                     */
                    Event tombstoneEvent = getTombstone(originalEvent);
                    Consistency.setModified(now, tombstoneEvent, calendarUser.getId());
                    storage.getEventStorage().insertTombstoneEvent(tombstoneEvent);
                    storage.getAttendeeStorage().insertTombstoneAttendees(objectID, attendeesToDelete);
                    /*
                     * remove attendee data
                     */
                    storage.getAttendeeStorage().deleteAttendees(objectID, attendeesToDelete);
                    storage.getAlarmStorage().deleteAlarms(objectID, getUserIDs(attendeesToDelete));
                }
                List<Attendee> attendeesToUpdate = attendeeHelper.getAttendeesToUpdate();
                if (null != attendeesToUpdate && 0 < attendeesToUpdate.size()) {
                    storage.getAttendeeStorage().updateAttendees(objectID, attendeesToUpdate);
                }
                List<Attendee> attendeesToInsert = attendeeHelper.getAttendeesToInsert();
                if (null != attendeesToInsert && 0 < attendeesToInsert.size()) {
                    storage.getAttendeeStorage().insertAttendees(objectID, attendeesToInsert);
                }
            }
            /*
             * update event data
             */
            Event eventUpdate = EventMapper.getInstance().getDifferences(originalEvent, event);
            for (EventField field : EventMapper.getInstance().getAssignedFields(eventUpdate)) {
                switch (field) {
                    case ALL_DAY:
                        /*
                         * adjust start- and enddate, too, if required
                         */
                        if (false == eventUpdate.containsStartDate()) {
                            eventUpdate.setStartDate(originalEvent.getStartDate());
                        }
                        if (false == eventUpdate.containsEndDate()) {
                            eventUpdate.setEndDate(originalEvent.getEndDate());
                        }
                        Consistency.adjustAllDayDates(eventUpdate);
                        break;
                    case UID:
                    case CREATED:
                    case CREATED_BY:
                    case SEQUENCE:
                    case SERIES_ID:
                        throw OXException.general("not allowed change");
                    default:
                        break;
                }
            }
            eventUpdate.setId(objectID);
            Consistency.setModified(now, eventUpdate, calendarUser.getId());
            eventUpdate.setSequence(originalEvent.getSequence() + 1);
            storage.getEventStorage().updateEvent(eventUpdate);
            /*
             * update alarms for calendar user
             */
            if (userizedEvent.containsAlarms()) {
                List<Alarm> alarms = userizedEvent.getAlarms();
                if (null == alarms) {
                    storage.getAlarmStorage().deleteAlarms(objectID, calendarUser.getId());
                } else {
                    storage.getAlarmStorage().updateAlarms(objectID, calendarUser.getId(), alarms);
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
        return readEvent(folder, objectID);
    }

    private UserizedEvent updateAttendee(UserizedFolder folder, int objectID, Attendee attendee) throws OXException {
        requireCalendarContentType(folder);
        requireWritePermission(folder, Permission.WRITE_OWN_OBJECTS);
        Event originalEvent = storage.getEventStorage().loadEvent(objectID, null);
        if (null == originalEvent) {
            throw OXException.notFound(String.valueOf(objectID));//TODO
        }
        if (session.getUser().getId() != originalEvent.getCreatedBy()) {
            requireWritePermission(folder, Permission.WRITE_ALL_OBJECTS);
        }
        Attendee originalAttendee = find(storage.getAttendeeStorage().loadAttendees(objectID), attendee);
        if (null == originalAttendee) {
            throw OXException.notFound(attendee.toString());//TODO
        }
        User calendarUser = getCalendarUser(folder);
        if (0 < originalAttendee.getEntity() && calendarUser.getId() != originalAttendee.getEntity() && session.getUser().getId() != originalAttendee.getEntity()) {
            // TODO: allowed for proxy user? calendarUser.getId() != originalAttendee.getEntity()
            throw OXException.general("can't confirm for someone else");
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
            storage.getAttendeeStorage().updateAttendee(objectID, originalAttendee);
            Event eventUpdate = new Event();
            eventUpdate.setId(objectID);
            Consistency.setModifiedNow(eventUpdate, session.getUser().getId());
            storage.getEventStorage().updateEvent(eventUpdate);
        }
        return readEvent(folder, originalEvent.getId());
    }

}
