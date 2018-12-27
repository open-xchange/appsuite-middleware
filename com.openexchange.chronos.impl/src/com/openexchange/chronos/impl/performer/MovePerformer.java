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

import static com.openexchange.chronos.common.CalendarUtils.contains;
import static com.openexchange.chronos.common.CalendarUtils.filter;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.isGroupScheduled;
import static com.openexchange.chronos.common.CalendarUtils.isPseudoGroupScheduled;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.getCalendarUser;
import static com.openexchange.chronos.impl.Utils.prepareOrganizer;
import static com.openexchange.folderstorage.Permission.CREATE_OBJECTS_IN_FOLDER;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.AttendeeHelper;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;

/**
 * {@link MovePerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class MovePerformer extends AbstractUpdatePerformer {

    /**
     * Initializes a new {@link MovePerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    public MovePerformer(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Performs the move of an event to another folder. No further updates are processed.
     *
     * @param objectId The identifier of the event to move
     * @param targetFolder The target folder to move the event to
     * @param clientTimestamp The client timestamp to catch concurrent modifications
     * @return The result
     */
    public InternalCalendarResult perform(String objectId, CalendarFolder targetFolder, long clientTimestamp) throws OXException {
        /*
         * load original event & check current session user's permissions
         */
        Event originalEvent = loadEventData(objectId);
        Check.eventIsInFolder(originalEvent, folder);
        requireWritePermissions(originalEvent);
        requireCalendarPermission(targetFolder, CREATE_OBJECTS_IN_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        /*
         * check if move is supported
         */
        Check.classificationIsValidOnMove(originalEvent.getClassification(), folder, targetFolder);
        if (isSeriesMaster(originalEvent)) {
            throw CalendarExceptionCodes.MOVE_SERIES_NOT_SUPPORTED.create(originalEvent.getId(), folder.getId(), targetFolder.getId());
        }
        if (isSeriesException(originalEvent)) {
            throw CalendarExceptionCodes.MOVE_OCCURRENCE_NOT_SUPPORTED.create(originalEvent.getId(), folder.getId(), targetFolder.getId());
        }
        /*
         * perform move operation based on parent folder types
         */
        if (PublicType.getInstance().equals(folder.getType()) && PublicType.getInstance().equals(targetFolder.getType())) {
            moveBetweenPublicFolders(originalEvent, targetFolder);
        } else if (false == PublicType.getInstance().equals(folder.getType()) && false == PublicType.getInstance().equals(targetFolder.getType())) {
            if (matches(calendarUser, getCalendarUser(session, targetFolder))) {
                moveBetweenPersonalFoldersOfSameUser(originalEvent, targetFolder);
            } else {
                moveBetweenPersonalFoldersOfDifferentUsers(originalEvent, targetFolder);
            }
        } else if (PublicType.getInstance().equals(folder.getType()) && false == PublicType.getInstance().equals(targetFolder.getType())) {
            moveFromPublicToPersonalFolder(originalEvent, targetFolder);
        } else if (false == PublicType.getInstance().equals(folder.getType()) && PublicType.getInstance().equals(targetFolder.getType())) {
            moveFromPersonalToPublicFolder(originalEvent, targetFolder);
        } else {
            throw new UnsupportedOperationException("Move not implemented from " + folder.getType() + " to " + targetFolder.getType());
        }
        /*
         * rewrite any alarm triggers, track & return result
         */
        Event updatedEvent = loadEventData(originalEvent.getId());
        storage.getAlarmTriggerStorage().deleteTriggers(objectId);
        storage.getAlarmTriggerStorage().insertTriggers(updatedEvent, storage.getAlarmStorage().loadAlarms(updatedEvent));
        resultTracker.trackUpdate(originalEvent, updatedEvent);
        return resultTracker.getResult();
    }

    private void moveFromPersonalToPublicFolder(Event originalEvent, CalendarFolder targetFolder) throws OXException {
        /*
         * move from personal to public folder, take over common public folder identifier for user attendees & update any existing alarms
         */
        Map<Integer, List<Alarm>> originalAlarms = storage.getAlarmStorage().loadAlarms(originalEvent);
        for (Attendee originalAttendee : filter(originalEvent.getAttendees(), Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
            updateAttendeeFolderId(originalEvent, originalAttendee, null);
            updateAttendeeAlarms(originalEvent, originalAlarms.get(I(originalAttendee.getEntity())), originalAttendee.getEntity(), targetFolder.getId());
        }
        /*
         * take over new common folder id, touch event & reset calendar user
         */
        updateCommonFolderId(originalEvent, targetFolder.getId());
        updateCalendarUser(originalEvent, null);
    }

    private void moveFromPublicToPersonalFolder(Event originalEvent, CalendarFolder targetFolder) throws OXException {
        /*
         * move from public to personal folder, require same organizer for group-scheduled events
         */
        if (isGroupScheduled(originalEvent)) {
            CalendarUser targetCalendarUser = getCalendarUser(session, targetFolder);
            if (false == matches(targetCalendarUser, originalEvent.getOrganizer())) {
                throw CalendarExceptionCodes.NOT_ORGANIZER.create(
                    targetFolder.getId(), originalEvent.getId(), targetCalendarUser.getUri(), targetCalendarUser.getSentBy());
            }
        }
        /*
         * take over default personal folders for user attendees & update any existing alarms
         */
        Map<Integer, List<Alarm>> originalAlarms = storage.getAlarmStorage().loadAlarms(originalEvent);
        CalendarUser targetCalendarUser = getCalendarUser(session, targetFolder);
        for (Attendee originalAttendee : filter(originalEvent.getAttendees(), Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
            String folderId = matches(targetCalendarUser, originalAttendee) ? targetFolder.getId() : getDefaultCalendarId(originalAttendee.getEntity());
            updateAttendeeFolderId(originalEvent, originalAttendee, folderId);
            updateAttendeeAlarms(originalEvent, originalAlarms.get(I(originalAttendee.getEntity())), originalAttendee.getEntity(), folderId);
        }
        /*
         * ensure to add default calendar user if not already present
         */
        if (false == contains(originalEvent.getAttendees(), targetCalendarUser)) {
            Attendee defaultAttendee = AttendeeHelper.getDefaultAttendee(session, targetFolder, null);
            storage.getAttendeeStorage().insertAttendees(originalEvent.getId(), Collections.singletonList(defaultAttendee));
        }
        /*
         * remove previous common folder id from event, touch event & assign calendar user
         */
        updateCommonFolderId(originalEvent, null);
        updateCalendarUser(originalEvent, targetCalendarUser);
    }

    private void moveBetweenPublicFolders(Event originalEvent, CalendarFolder targetFolder) throws OXException {
        /*
         * move from one public folder to another, update event's common folder & update any existing alarms
         */
        Map<Integer, List<Alarm>> originalAlarms = storage.getAlarmStorage().loadAlarms(originalEvent);
        for (Map.Entry<Integer, List<Alarm>> entry : originalAlarms.entrySet()) {
            updateAttendeeAlarms(originalEvent, entry.getValue(), entry.getKey().intValue(), targetFolder.getId());
        }
        updateCommonFolderId(originalEvent, targetFolder.getId());
    }

    private void moveBetweenPersonalFoldersOfSameUser(Event originalEvent, CalendarFolder targetFolder) throws OXException {
        /*
         * move from one personal folder to another of the same user
         */
        if (isGroupScheduled(originalEvent)) {
            /*
             * update attendee's folder in a group-scheduled event
             */
            Attendee originalAttendee = find(originalEvent.getAttendees(), calendarUserId);
            if (null == originalAttendee) {
                throw CalendarExceptionCodes.ATTENDEE_NOT_FOUND.create(String.valueOf(calendarUserId), originalEvent.getId());
            }
            updateAttendeeFolderId(originalEvent, originalAttendee, targetFolder.getId());
            touch(originalEvent.getId());
        } else {
            /*
             * update event's common folder id, otherwise
             */
            updateCommonFolderId(originalEvent, targetFolder.getId());
        }
        Map<Integer, List<Alarm>> originalAlarms = storage.getAlarmStorage().loadAlarms(originalEvent);
        updateAttendeeAlarms(originalEvent, originalAlarms.get(I(calendarUserId)), calendarUserId, targetFolder.getId());
    }

    private void moveBetweenPersonalFoldersOfDifferentUsers(Event originalEvent, CalendarFolder targetFolder) throws OXException {
        /*
         * move between personal calendar folders
         */
        if (false == isGroupScheduled(originalEvent)) {
            /*
             * update event's common folder id, assign new calendar user & remove alarms of previous calendar user
             */
            updateCommonFolderId(originalEvent, targetFolder.getId());
            updateCalendarUser(originalEvent, getCalendarUser(session, targetFolder));
            storage.getAlarmStorage().deleteAlarms(originalEvent.getId(), calendarUserId);
        } else if (isPseudoGroupScheduled(originalEvent)) {
            /*
             * exchange default attendee & organizer, remove alarms of previous calendar user
             */
            Attendee originalAttendee = find(originalEvent.getAttendees(), calendarUserId);
            if (null == originalAttendee) {
                throw CalendarExceptionCodes.ATTENDEE_NOT_FOUND.create(String.valueOf(calendarUserId), originalEvent.getId());
            }
            storage.getAttendeeStorage().insertAttendeeTombstone(originalEvent.getId(), storage.getUtilities().getTombstone(originalAttendee));
            storage.getEventStorage().insertEventTombstone(storage.getUtilities().getTombstone(originalEvent, timestamp, calendarUser));
            storage.getAttendeeStorage().deleteAttendees(originalEvent.getId(), Collections.singletonList(originalAttendee));
            storage.getAlarmStorage().deleteAlarms(originalEvent.getId(), originalAttendee.getEntity());
            Attendee newDefaultAttendee = AttendeeHelper.getDefaultAttendee(session, targetFolder, null);
            storage.getAttendeeStorage().insertAttendees(originalEvent.getId(), Collections.singletonList(newDefaultAttendee));
            updateOrganizer(originalEvent, targetFolder);
        } else {
            /*
             * not allowed/supported, otherwise
             */
            throw CalendarExceptionCodes.UNSUPPORTED_FOLDER.create(targetFolder.getId(), targetFolder.getType());
        }
    }

    private void updateAttendeeFolderId(Event originalEvent, Attendee originalAttendee, String folderId) throws OXException {
        if ((null == folderId && null != originalAttendee.getFolderId()) || (null != folderId && false == folderId.equals(originalAttendee.getFolderId()))) {
            storage.getEventStorage().insertEventTombstone(storage.getUtilities().getTombstone(originalEvent, timestamp, calendarUser));
            Attendee attendeeUpdate = AttendeeMapper.getInstance().copy(originalAttendee, null, AttendeeField.ENTITY, AttendeeField.MEMBER, AttendeeField.CU_TYPE, AttendeeField.URI);
            attendeeUpdate.setFolderId(folderId);
            storage.getAttendeeStorage().insertAttendeeTombstone(originalEvent.getId(), storage.getUtilities().getTombstone(originalAttendee));
            storage.getAttendeeStorage().updateAttendee(originalEvent.getId(), attendeeUpdate);
        }
    }

    private void updateCommonFolderId(Event originalEvent, String folderId) throws OXException {
        if ((null == folderId && null != originalEvent.getFolderId()) || (null != folderId && false == folderId.equals(originalEvent.getFolderId()))) {
            Event eventUpdate = new Event();
            eventUpdate.setId(originalEvent.getId());
            eventUpdate.setFolderId(folderId);
            Consistency.setModified(session, timestamp, eventUpdate, session.getUserId());
            storage.getEventStorage().insertEventTombstone(storage.getUtilities().getTombstone(originalEvent, timestamp, calendarUser));
            storage.getEventStorage().updateEvent(eventUpdate);
        }
    }

    private void updateCalendarUser(Event originalEvent, CalendarUser newCalendarUser) throws OXException {
        if (false == matches(newCalendarUser, originalEvent.getCalendarUser())) {
            Event eventUpdate = new Event();
            eventUpdate.setId(originalEvent.getId());
            eventUpdate.setCalendarUser(newCalendarUser);
            Consistency.setModified(session, timestamp, eventUpdate, session.getUserId());
            storage.getEventStorage().updateEvent(eventUpdate);
        }
    }

    private void updateOrganizer(Event originalEvent, CalendarFolder targetFolder) throws OXException {
        if (false == matches(getCalendarUser(session, targetFolder), originalEvent.getOrganizer())) {
            Event eventUpdate = new Event();
            eventUpdate.setId(originalEvent.getId());
            Organizer organizer = prepareOrganizer(session, targetFolder, null);
            eventUpdate.setOrganizer(organizer);
            Consistency.setModified(session, timestamp, eventUpdate, session.getUserId());
            storage.getEventStorage().updateEvent(eventUpdate);
        }
    }

    private void updateAttendeeAlarms(Event originalEvent, List<Alarm> originalAlarms, int userId, final String folderId) throws OXException {
        if (null != originalAlarms && 0 < originalAlarms.size()) {
            Event userizedEvent = new DelegatingEvent(originalEvent) {

                @Override
                public String getFolderId() {
                    return folderId;
                }

                @Override
                public boolean containsFolderId() {
                    return true;
                }
            };
            originalAlarms.stream().forEach(a -> a.setLastModified(System.currentTimeMillis()));
            storage.getAlarmStorage().updateAlarms(userizedEvent, userId, originalAlarms);
        }
    }

    /**
     * Gets the identifier of a specific user's default personal calendar folder.
     *
     * @param userId The identifier of the user to retrieve the default calendar identifier for
     * @return The default calendar folder identifier
     */
    private String getDefaultCalendarId(int userId) throws OXException {
        return session.getConfig().getDefaultFolderId(userId);
    }

}
