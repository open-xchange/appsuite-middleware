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

import static com.openexchange.chronos.common.CalendarUtils.contains;
import static com.openexchange.chronos.common.CalendarUtils.filter;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.getCalendarUser;
import static com.openexchange.chronos.impl.Utils.i;
import static com.openexchange.folderstorage.Permission.CREATE_OBJECTS_IN_FOLDER;
import static com.openexchange.folderstorage.Permission.DELETE_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.DELETE_OWN_OBJECTS;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import static com.openexchange.folderstorage.Permission.WRITE_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.WRITE_OWN_OBJECTS;
import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.AttendeeHelper;
import com.openexchange.chronos.impl.AttendeeMapper;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.EventMapper;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.groupware.ldap.User;

/**
 * {@link MoveOperation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class MoveOperation extends AbstractOperation {

    /**
     * Prepares a move operation.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @return The prepared move operation
     */
    public static MoveOperation prepare(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        return new MoveOperation(storage, session, folder);
    }

    /**
     * Initializes a new {@link MoveOperation}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    private MoveOperation(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Performs the move of an event to another folder. No further updates are processed.
     *
     * @param objectID The identifier of the event to move
     * @param targetFolder The target folder to move the event to
     * @param clientTimestamp The client timestamp to catch concurrent modifications
     * @return The result
     */
    public CalendarResultImpl perform(int objectID, UserizedFolder targetFolder, long clientTimestamp) throws OXException {
        return perform(loadEventData(objectID), targetFolder, clientTimestamp);
    }

    /**
     * Performs the move of an event to another folder. No further updates are processed.
     *
     * @param originalEvent The original event to move
     * @param targetFolder The target folder to move the event to
     * @param clientTimestamp The client timestamp to catch concurrent modifications
     * @return The result
     */
    public CalendarResultImpl perform(Event originalEvent, UserizedFolder targetFolder, long clientTimestamp) throws OXException {
        /*
         * check current session user's permissions
         */
        Check.eventIsInFolder(originalEvent, folder);
        requireCalendarPermission(targetFolder, CREATE_OBJECTS_IN_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        if (session.getUser().getId() == originalEvent.getCreatedBy()) {
            requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, WRITE_OWN_OBJECTS, DELETE_OWN_OBJECTS);
        } else {
            requireCalendarPermission(folder, READ_FOLDER, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, DELETE_ALL_OBJECTS);
        }
        /*
         * check if move is supported
         */
        if (isSeriesMaster(originalEvent)) {
            throw CalendarExceptionCodes.MOVE_SERIES_NOT_SUPPORTED.create(I(originalEvent.getId()), I(i(folder)), I(i(targetFolder)));
        }
        if (isSeriesException(originalEvent)) {
            throw CalendarExceptionCodes.MOVE_OCCURRENCE_NOT_SUPPORTED.create(I(originalEvent.getId()), I(i(folder)), I(i(targetFolder)));
        }
        /*
         * perform move operation based on parent folder types
         */
        if (PublicType.getInstance().equals(folder.getType()) && PublicType.getInstance().equals(targetFolder.getType())) {
            moveBetweenPublicFolders(originalEvent, targetFolder);
        } else if (false == PublicType.getInstance().equals(folder.getType()) && false == PublicType.getInstance().equals(targetFolder.getType())) {
            moveBetweenPersonalFolders(originalEvent, targetFolder);
        } else if (PublicType.getInstance().equals(folder.getType()) && false == PublicType.getInstance().equals(targetFolder.getType())) {
            moveFromPublicToPersonalFolder(originalEvent, targetFolder);
        } else if (false == PublicType.getInstance().equals(folder.getType()) && PublicType.getInstance().equals(targetFolder.getType())) {
            moveFromPersonalToPublicFolder(originalEvent, targetFolder);
        } else {
            throw new UnsupportedOperationException("Move not implemented from " + folder.getType() + " to " + targetFolder.getType());
        }
        /*
         * track update & return result
         */
        result.addUpdate(new UpdateResultImpl(originalEvent, i(targetFolder), loadEventData(originalEvent.getId())));
        return result;
    }

    private void moveFromPersonalToPublicFolder(Event originalEvent, UserizedFolder targetFolder) throws OXException {
        /*
         * move from personal to public folder, take over common public folder identifier for user attendees
         */
        for (Attendee originalAttendee : filter(originalEvent.getAttendees(), Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
            updateAttendeeFolderId(originalEvent.getId(), originalAttendee, AttendeeHelper.ATTENDEE_PUBLIC_FOLDER_ID);
        }
        /*
         * take over new public folder id & touch event
         */
        updatePublicFolderId(originalEvent, i(targetFolder));
    }

    private void moveFromPublicToPersonalFolder(Event originalEvent, UserizedFolder targetFolder) throws OXException {
        /*
         * move from public to personal folder, take over default personal folders for user attendees
         */
        User targetCalendarUser = getCalendarUser(targetFolder);
        for (Attendee originalAttendee : filter(originalEvent.getAttendees(), Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
            int folderId = targetCalendarUser.getId() == originalAttendee.getEntity() ? i(targetFolder) : getDefaultCalendarID(originalAttendee.getEntity());
            updateAttendeeFolderId(originalEvent.getId(), originalAttendee, folderId);
        }
        /*
         * ensure to add default calendar user if not already present
         */
        if (false == contains(originalEvent.getAttendees(), targetCalendarUser.getId())) {
            Attendee defaultAttendee = new AttendeeHelper(session, targetFolder, null, null).getDefaultAttendee(targetFolder, null);
            storage.getAttendeeStorage().insertAttendees(originalEvent.getId(), Collections.singletonList(defaultAttendee));
        }
        /*
         * remove previous public folder id from event & touch event
         */
        updatePublicFolderId(originalEvent, 0);
    }

    private void moveBetweenPublicFolders(Event originalEvent, UserizedFolder targetFolder) throws OXException {
        /*
         * move from one public folder to another, update event's folder
         */
        updatePublicFolderId(originalEvent, i(targetFolder));
    }

    private void moveBetweenPersonalFolders(Event originalEvent, UserizedFolder targetFolder) throws OXException {
        /*
         * move between personal calendar folders
         */
        User targetCalendarUser = getCalendarUser(targetFolder);
        if (calendarUser.getId() == targetCalendarUser.getId()) {
            /*
             * move from one personal folder to another of the same user, update attendee's folder
             */
            Attendee originalAttendee = find(originalEvent.getAttendees(), calendarUser.getId());
            if (null == originalAttendee) {
                throw CalendarExceptionCodes.ATTENDEE_NOT_FOUND.create(String.valueOf(calendarUser.getId()), originalEvent.getId());
            }
            updateAttendeeFolderId(originalEvent.getId(), originalAttendee, i(targetFolder));
        } else if (calendarUser.getId() == session.getUser().getId()) {
            /*
             * move from user's own calendar to another one ("reassign"), remove the original default user and
             * ensure that the target calendar user becomes attendee
             */
            Attendee defaultAttendee = find(originalEvent.getAttendees(), targetCalendarUser.getId());
            if (null == defaultAttendee) {
                defaultAttendee = new AttendeeHelper(session, targetFolder, null, null).getDefaultAttendee(targetFolder, null);
                storage.getAttendeeStorage().insertAttendees(originalEvent.getId(), Collections.singletonList(defaultAttendee));
            }
            for (Attendee originalAttendee : filter(originalEvent.getAttendees(), Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
                if (calendarUser.getId() == originalAttendee.getEntity()) {
                    storage.getAttendeeStorage().insertTombstoneAttendee(originalEvent.getId(), AttendeeMapper.getInstance().getTombstone(originalAttendee));
                    storage.getAttendeeStorage().deleteAttendees(originalEvent.getId(), Collections.singletonList(originalAttendee));
                } else {
                    int folderId = targetCalendarUser.getId() == originalAttendee.getEntity() ? i(targetFolder) : getDefaultCalendarID(originalAttendee.getEntity());
                    updateAttendeeFolderId(originalEvent.getId(), originalAttendee, folderId);
                }
            }
        } else {
            /*
             * move from one personal folder to another user's personal folder, take over target folder for new default attendee
             * and reset personal calendar folders of further user attendees
             */
            for (Attendee originalAttendee : filter(originalEvent.getAttendees(), Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
                int folderId = targetCalendarUser.getId() == originalAttendee.getEntity() ? i(targetFolder) : getDefaultCalendarID(originalAttendee.getEntity());
                updateAttendeeFolderId(originalEvent.getId(), originalAttendee, folderId);
            }
            /*
             * ensure to add default calendar user of target folder if not already present
             */
            if (false == contains(originalEvent.getAttendees(), targetCalendarUser.getId())) {
                Attendee defaultAttendee = new AttendeeHelper(session, targetFolder, null, null).getDefaultAttendee(targetFolder, null);
                storage.getAttendeeStorage().insertAttendees(originalEvent.getId(), Collections.singletonList(defaultAttendee));
            }
        }
        /*
         * insert corresponding tombstone & also 'touch' parent event
         */
        storage.getEventStorage().insertTombstoneEvent(EventMapper.getInstance().getTombstone(originalEvent, timestamp, calendarUser.getId()));
        touch(originalEvent.getId());
    }

    private void updateAttendeeFolderId(int eventID, Attendee originalAttendee, int folderId) throws OXException {
        Attendee attendeeUpdate = new Attendee();
        AttendeeMapper.getInstance().copy(originalAttendee, attendeeUpdate, AttendeeField.ENTITY, AttendeeField.MEMBER, AttendeeField.CU_TYPE, AttendeeField.URI);
        attendeeUpdate.setFolderID(folderId);
        if (attendeeUpdate.getFolderID() != originalAttendee.getFolderID()) {
            storage.getAttendeeStorage().insertTombstoneAttendee(eventID, AttendeeMapper.getInstance().getTombstone(originalAttendee));
            storage.getAttendeeStorage().updateAttendee(eventID, attendeeUpdate);
        }
    }

    private void updatePublicFolderId(Event originalEvent, int folderId) throws OXException {
        if (folderId != originalEvent.getPublicFolderId()) {
            Event eventUpdate = new Event();
            eventUpdate.setId(originalEvent.getId());
            eventUpdate.setPublicFolderId(folderId);
            Consistency.setModified(timestamp, eventUpdate, calendarUser.getId());
            storage.getEventStorage().insertTombstoneEvent(EventMapper.getInstance().getTombstone(originalEvent, timestamp, calendarUser.getId()));
            storage.getEventStorage().updateEvent(eventUpdate);
        }
    }

}
