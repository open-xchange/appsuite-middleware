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

import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.AttendeeMapper;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.EventMapper;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PublicType;

/**
 * {@link UpdateAttendeeOperation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdateAttendeeOperation extends AbstractOperation {

    /**
     * Prepares an update attendee operation.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @return The prepared update attendee operation
     */
    public static UpdateAttendeeOperation prepare(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        return new UpdateAttendeeOperation(storage, session, folder);
    }

    /**
     * Initializes a new {@link UpdateAttendeeOperation}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    private UpdateAttendeeOperation(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Performs the attendee update in an event.
     *
     * @param objectID The identifier of the event to update the attendee in
     * @param recurrenceID The recurrence identifier of the occurrence to update, or <code>null</code> if no specific occurrence is targeted
     * @param attendee The attendee data to update
     * @param clientTimestamp The client timestamp to catch concurrent modifications, or <code>null</code> to skip checks
     * @return The result
     */
    public CalendarResultImpl perform(int objectID, RecurrenceId recurrenceID, Attendee attendee, Long clientTimestamp) throws OXException {
        /*
         * load original event data & attendee
         */
        Event originalEvent = loadEventData(objectID);
        Attendee originalAttendee = Check.attendeeExists(originalEvent, attendee);
        /*
         * check current session user's permissions
         */
        Check.eventIsInFolder(originalEvent, folder);
        if (session.getUser().getId() == originalEvent.getCreatedBy()) {
            requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, WRITE_OWN_OBJECTS, NO_PERMISSIONS);
        } else {
            requireCalendarPermission(folder, READ_FOLDER, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, NO_PERMISSIONS);
        }
        if (null != clientTimestamp) {
            requireUpToDateTimestamp(originalEvent, clientTimestamp.longValue());
        }
        if (0 < originalAttendee.getEntity() && calendarUser.getId() != originalAttendee.getEntity() && session.getUser().getId() != originalAttendee.getEntity()) {
            // TODO: even allowed for proxy user? calendarUser.getId() != originalAttendee.getEntity()
            throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(I(i(folder)));
        }
        if (null == recurrenceID) {
            updateAttendee(originalEvent, originalAttendee, attendee);
        } else {
            updateAttendee(originalEvent, originalAttendee, attendee, recurrenceID);
        }
        return result;
    }

    /**
     * Updates an attendee in an event.
     *
     * @param originalEvent The original event
     * @param originalAttendee The original attendee
     * @param attendee The updated attendee
     */
    private void updateAttendee(Event originalEvent, Attendee originalAttendee, Attendee attendee) throws OXException {
        /*
         * prepare update
         */
        Attendee attendeeUpdate = prepareAttendeeUpdate(originalEvent, originalAttendee, attendee);
        if (null == attendeeUpdate) {
            //TODO or throw?
            result.addUpdate(new UpdateResultImpl(originalEvent, i(folder), originalEvent));
            return;
        }
        int updatedFolderID;
        if (attendeeUpdate.containsFolderID()) {
            /*
             * store tombstone references in case of a move operation for the attendee
             */
            updatedFolderID = attendeeUpdate.getFolderID();
            storage.getEventStorage().insertTombstoneEvent(EventMapper.getInstance().getTombstone(originalEvent, timestamp, calendarUser.getId()));
            storage.getAttendeeStorage().insertTombstoneAttendee(originalEvent.getId(), AttendeeMapper.getInstance().getTombstone(originalAttendee));
        } else {
            updatedFolderID = i(folder);
        }
        /*
         * update attendee & 'touch' the corresponding event
         */
        storage.getAttendeeStorage().updateAttendee(originalEvent.getId(), attendeeUpdate);
        touch(originalEvent.getId());
        result.addUpdate(new UpdateResultImpl(originalEvent, updatedFolderID, loadEventData(originalEvent.getId())));
        if (isSeriesException(originalEvent)) {
            /*
             * also 'touch' the series master in case of an exception update
             */
            Event originalMasterEvent = loadEventData(originalEvent.getSeriesId());
            touch(originalEvent.getSeriesId());
            result.addUpdate(new UpdateResultImpl(originalMasterEvent, updatedFolderID, loadEventData(originalEvent.getSeriesId())));
        }
    }

    private void updateAttendee(Event originalEvent, Attendee originalAttendee, Attendee attendee, RecurrenceId recurrenceID) throws OXException {
        if (isSeriesMaster(originalEvent)) {
            if (null != originalEvent.getChangeExceptionDates() && originalEvent.getChangeExceptionDates().contains(new Date(recurrenceID.getValue()))) {
                /*
                 * update for existing change exception
                 */
                Event originalExceptionEvent = loadExceptionData(originalEvent.getId(), recurrenceID);
                Attendee originalExceptionAttendee = Check.attendeeExists(originalExceptionEvent, attendee);
                updateAttendee(originalExceptionEvent, originalExceptionAttendee, attendee);
            } else {
                /*
                 * update for new change exception, prepare & insert the exception
                 */
                Event exceptionEvent = prepareException(originalEvent, Check.recurrenceIdExists(originalEvent, recurrenceID));
                storage.getEventStorage().insertEvent(exceptionEvent);
                /*
                 * take over all original attendees & alarms
                 */
                List<Attendee> excpetionAttendees = new ArrayList<Attendee>(originalEvent.getAttendees());
                storage.getAttendeeStorage().insertAttendees(exceptionEvent.getId(), excpetionAttendees);
                /*
                 * take over all original alarms
                 */
                for (Entry<Integer, List<Alarm>> entry : storage.getAlarmStorage().loadAlarms(originalEvent.getId()).entrySet()) {
                    storage.getAlarmStorage().insertAlarms(exceptionEvent.getId(), entry.getKey().intValue(), entry.getValue());
                }
                /*
                 * perform the attendee update
                 */
                Attendee attendeeUpdate = prepareAttendeeUpdate(exceptionEvent, originalAttendee, attendee);
                if (null != attendeeUpdate) {
                    storage.getAttendeeStorage().updateAttendee(exceptionEvent.getId(), attendeeUpdate);
                }
                addChangeExceptionDate(originalEvent, recurrenceID);
                result.addCreation(new CreateResultImpl(loadEventData(exceptionEvent.getId())));
            }
        } else if (isSeriesException(originalEvent)) {
            /*
             * update for existing change exception
             */
            updateAttendee(originalEvent, originalAttendee, attendee);
        } else {
            /*
             * unsupported, otherwise
             */
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(I(originalEvent.getId()), String.valueOf(recurrenceID));
        }
    }

    /**
     * Prepares an attendee update based on the differences of the original and updated attendee, implicitly checking for allowed
     * attendee changes.
     *
     * @param originalEvent The original event
     * @param originalAttendee The original attendee
     * @param updatedAttendee The updated attendee
     * @return A 'delta' attendee representing the changes, or <code>null</code> if no changes need to be stored
     */
    private Attendee prepareAttendeeUpdate(Event originalEvent, Attendee originalAttendee, Attendee updatedAttendee) throws OXException {
        /*
         * determine & check modified fields
         */
        Attendee attendeeUpdate = AttendeeMapper.getInstance().getDifferences(originalAttendee, updatedAttendee);
        AttendeeField[] updatedFields = AttendeeMapper.getInstance().getAssignedFields(attendeeUpdate);
        if (0 == updatedFields.length) {
            // TODO or throw?
            return null;
        }
        for (AttendeeField field : updatedFields) {
            switch (field) {
                case FOLDER_ID:
                    checkFolderUpdate(originalEvent, originalAttendee, attendeeUpdate.getFolderID());
                    break;
                case CU_TYPE:
                case ENTITY:
                case MEMBER:
                case URI:
                    throw CalendarExceptionCodes.FORBIDDEN_ATTENDEE_CHANGE.create(I(originalEvent.getId()), originalAttendee, field);
                default:
                    break;
            }
        }
        /*
         * take over identifying properties from original
         */
        AttendeeMapper.getInstance().copy(originalAttendee, attendeeUpdate, AttendeeField.ENTITY, AttendeeField.MEMBER, AttendeeField.CU_TYPE, AttendeeField.URI);
        if (session.getUser().getId() != calendarUser.getId() && false == attendeeUpdate.containsSentBy()) {
            attendeeUpdate.setSentBy(session.getEntityResolver().applyEntityData(new CalendarUser(), session.getUser().getId()));
        }
        return attendeeUpdate;
    }

    private void checkFolderUpdate(Event originalEvent, Attendee originalAttendee, int updatedFolderID) throws OXException {
        if (originalAttendee.getFolderID() != i(folder)) {
            throw CalendarExceptionCodes.FORBIDDEN_ATTENDEE_CHANGE.create(I(originalEvent.getId()), originalAttendee, AttendeeField.FOLDER_ID);
        }
        if (isSeriesMaster(originalEvent)) {
            throw CalendarExceptionCodes.MOVE_SERIES_NOT_SUPPORTED.create(I(originalEvent.getId()), I(i(folder)), I(updatedFolderID));
        }
        if (isSeriesException(originalEvent)) {
            throw CalendarExceptionCodes.MOVE_OCCURRENCE_NOT_SUPPORTED.create(I(originalEvent.getId()), I(i(folder)), I(updatedFolderID));
        }
        if (PublicType.getInstance().equals(folder.getType())) {
            throw CalendarExceptionCodes.FORBIDDEN_ATTENDEE_CHANGE.create(I(originalEvent.getId()), originalAttendee, AttendeeField.FOLDER_ID);
        }
        UserizedFolder targetFolder = getFolder(updatedFolderID);
        if (folder.getCreatedBy() != targetFolder.getCreatedBy()) {
            throw CalendarExceptionCodes.FORBIDDEN_ATTENDEE_CHANGE.create(I(originalEvent.getId()), originalAttendee, AttendeeField.FOLDER_ID);
        }
        requireCalendarPermission(targetFolder, CREATE_OBJECTS_IN_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        if (session.getUser().getId() == originalEvent.getCreatedBy()) {
            requireCalendarPermission(folder, READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, DELETE_OWN_OBJECTS);
        } else {
            requireCalendarPermission(folder, READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, DELETE_ALL_OBJECTS);
        }
    }
}
