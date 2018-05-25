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

import static com.openexchange.chronos.common.AlarmUtils.filterRelativeTriggers;
import static com.openexchange.chronos.common.CalendarUtils.contains;
import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
import static com.openexchange.folderstorage.Permission.CREATE_OBJECTS_IN_FOLDER;
import static com.openexchange.folderstorage.Permission.DELETE_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.DELETE_OWN_OBJECTS;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.i;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;

/**
 * {@link UpdateAttendeePerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdateAttendeePerformer extends AbstractUpdatePerformer {

    /**
     * Initializes a new {@link UpdateAttendeePerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    public UpdateAttendeePerformer(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Performs the attendee update in an event.
     *
     * @param eventId The identifier of the event to update the attendee in
     * @param recurrenceId The recurrence identifier of the occurrence to update, or <code>null</code> if no specific occurrence is targeted
     * @param attendee The attendee data to update
     * @param alarms The alarms to update, or <code>null</code> to not change alarms, or an empty array to delete any existing alarms
     * @param clientTimestamp The client timestamp to catch concurrent modifications, or <code>null</code> to skip checks
     * @return The result
     */
    public InternalCalendarResult perform(String eventId, RecurrenceId recurrenceId, Attendee attendee, List<Alarm> alarms, Long clientTimestamp) throws OXException {
        /*
         * load original event data & check permissions
         */
        Event originalEvent = loadEventData(eventId);
        Check.eventIsVisible(folder, originalEvent);
        Check.eventIsInFolder(originalEvent, folder);
        requireWritePermissions(originalEvent, attendee);
        if (null != clientTimestamp) {
            requireUpToDateTimestamp(originalEvent, clientTimestamp.longValue());
        }
        /*
         * check targeted attendee
         */
        Attendee resolvedAttendee = session.getEntityResolver().prepare(AttendeeMapper.getInstance().copy(attendee, null, (AttendeeField[]) null));
        Attendee originalAttendee = Check.attendeeExists(originalEvent, resolvedAttendee);
        if (0 < originalAttendee.getEntity() && calendarUserId != originalAttendee.getEntity() && session.getUserId() != originalAttendee.getEntity()) {
            // TODO: even allowed for proxy user? calendarUserId != originalAttendee.getEntity()
            throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(folder.getId());
        }
        if (needsConflictCheck(originalEvent, originalAttendee, attendee)) {
            Check.noConflicts(storage, session, originalEvent, Collections.singletonList(resolvedAttendee));
        }
        /*
         * perform update
         */
        if (null == recurrenceId) {
            updateAttendee(originalEvent, originalAttendee, attendee);
        } else {
            updateAttendee(originalEvent, originalAttendee, attendee, recurrenceId);
        }
        /*
         * also update alarms as needed
         */
        if (null != alarms) {
            if (false == isInternal(originalAttendee)) {
                throw CalendarExceptionCodes.FORBIDDEN_ATTENDEE_CHANGE.create(eventId, originalAttendee, EventField.ALARMS);
            }
            new UpdateAlarmsPerformer(this).perform(eventId, recurrenceId, alarms.isEmpty() ? null : alarms, L(timestamp.getTime()));
        }
        return resultTracker.getResult();
    }

    /**
     * Gets a value indicating whether conflict checks should take place along with the update or not.
     *
     * @param originalEvent The original event
     * @param originalAttendee The original {@link Attendee}
     * @param updatedAttendee The updated {@link Attendee}
     * @return <code>true</code> if conflict checks should take place, <code>false</code>, otherwise
     */
    private boolean needsConflictCheck(Event originalEvent, Attendee originalAttendee, Attendee updatedAttendee) {
        ParticipationStatus originalPartStat = null != originalAttendee.getPartStat() ? originalAttendee.getPartStat() : ParticipationStatus.NEEDS_ACTION;
        ParticipationStatus updatedPartStat = null != updatedAttendee.getPartStat() ? updatedAttendee.getPartStat() : ParticipationStatus.NEEDS_ACTION;
        if (originalPartStat.equals(ParticipationStatus.ACCEPTED)) {
            return false;
        }
        if (updatedPartStat.equals(ParticipationStatus.ACCEPTED)) {
            if (originalPartStat.equals(ParticipationStatus.TENTATIVE)) {
                return false;
            }
            return true;
        }
        if (updatedPartStat.equals(ParticipationStatus.TENTATIVE) && (!originalPartStat.equals(ParticipationStatus.TENTATIVE))) {
            return true;
        }
        return false;
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
        resultTracker.rememberOriginalEvent(originalEvent);
        Attendee attendeeUpdate = prepareAttendeeUpdate(originalEvent, originalAttendee, attendee);
        if (attendeeUpdate.containsFolderID()) {
            /*
             * store tombstone references in case of a move operation for the attendee
             */
            storage.getEventStorage().insertEventTombstone(storage.getUtilities().getTombstone(originalEvent, timestamp, calendarUser));
            storage.getAttendeeStorage().insertAttendeeTombstone(originalEvent.getId(), storage.getUtilities().getTombstone(originalAttendee));
        }
        /*
         * update attendee & 'touch' the corresponding event
         */
        storage.getAttendeeStorage().updateAttendee(originalEvent.getId(), attendeeUpdate);
        touch(originalEvent.getId());
        Event updatedEvent = loadEventData(originalEvent.getId());
        resultTracker.trackUpdate(originalEvent, updatedEvent);
        if (isSeriesException(originalEvent)) {
            /*
             * also 'touch' the series master in case of an exception update
             */
            Event originalMasterEvent = loadEventData(originalEvent.getSeriesId());
            resultTracker.rememberOriginalEvent(originalMasterEvent);
            touch(originalEvent.getSeriesId());
            Event updatedMasterEvent = loadEventData(originalEvent.getSeriesId());
            resultTracker.trackUpdate(originalMasterEvent, updatedMasterEvent);
        }
    }

    private void updateAttendee(Event originalEvent, Attendee originalAttendee, Attendee attendee, RecurrenceId recurrenceId) throws OXException {
        if (isSeriesMaster(originalEvent)) {
            Event originalSeriesMaster = originalEvent;
            recurrenceId = Check.recurrenceIdExists(session.getRecurrenceService(), originalEvent, recurrenceId);
            if (contains(originalEvent.getChangeExceptionDates(), recurrenceId)) {
                /*
                 * update for existing change exception
                 */
                Event originalExceptionEvent = loadExceptionData(originalEvent.getId(), recurrenceId);
                Attendee originalExceptionAttendee = Check.attendeeExists(originalExceptionEvent, attendee);
                updateAttendee(originalExceptionEvent, originalExceptionAttendee, attendee);
            } else {
                /*
                 * update for new change exception; prepare & insert a plain exception first, based on the original data from the master
                 */
                Map<Integer, List<Alarm>> seriesMasterAlarms = storage.getAlarmStorage().loadAlarms(originalSeriesMaster);
                Event newExceptionEvent = prepareException(originalSeriesMaster, recurrenceId);
                Check.quotaNotExceeded(storage, session);
                storage.getEventStorage().insertEvent(newExceptionEvent);
                storage.getAttendeeStorage().insertAttendees(newExceptionEvent.getId(), originalSeriesMaster.getAttendees());
                storage.getAttachmentStorage().insertAttachments(session.getSession(), folder.getId(), newExceptionEvent.getId(), originalSeriesMaster.getAttachments());
                for (Entry<Integer, List<Alarm>> entry : seriesMasterAlarms.entrySet()) {
                    insertAlarms(newExceptionEvent, i(entry.getKey()), filterRelativeTriggers(entry.getValue()), true);
                }
                newExceptionEvent = loadEventData(newExceptionEvent.getId());
                resultTracker.trackCreation(newExceptionEvent, originalSeriesMaster);
                /*
                 * perform the attendee update & track results
                 */
                resultTracker.rememberOriginalEvent(newExceptionEvent);
                Attendee attendeeUpdate = prepareAttendeeUpdate(newExceptionEvent, originalAttendee, attendee);
                if (null != attendeeUpdate) {
                    storage.getAttendeeStorage().updateAttendee(newExceptionEvent.getId(), attendeeUpdate);
                }
                Event updatedExceptionEvent = loadEventData(newExceptionEvent.getId());
                resultTracker.trackUpdate(newExceptionEvent, updatedExceptionEvent);
                /*
                 * add change exception date to series master & track results
                 */
                resultTracker.rememberOriginalEvent(originalSeriesMaster);
                addChangeExceptionDate(originalSeriesMaster, recurrenceId);
                Event updatedMasterEvent = loadEventData(originalSeriesMaster.getId());
                resultTracker.trackUpdate(originalSeriesMaster, updatedMasterEvent);
                /*
                 * reset alarm triggers for series master event and new change exception
                 */
                storage.getAlarmTriggerStorage().deleteTriggers(updatedMasterEvent.getId());
                storage.getAlarmTriggerStorage().insertTriggers(updatedMasterEvent, seriesMasterAlarms);
                storage.getAlarmTriggerStorage().deleteTriggers(updatedExceptionEvent.getId());
                storage.getAlarmTriggerStorage().insertTriggers(updatedExceptionEvent, storage.getAlarmStorage().loadAlarms(updatedExceptionEvent));
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
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(originalEvent.getId(), String.valueOf(recurrenceId));
        }
    }

    /**
     * Prepares an attendee update based on the differences of the original and updated attendee, implicitly checking for allowed
     * attendee changes.
     *
     * @param originalEvent The original event
     * @param originalAttendee The original attendee
     * @param updatedAttendee The updated attendee
     * @return A 'delta' attendee representing the changes
     */
    private Attendee prepareAttendeeUpdate(Event originalEvent, Attendee originalAttendee, Attendee updatedAttendee) throws OXException {
        /*
         * determine & check modified fields
         */
        Attendee attendeeUpdate = AttendeeMapper.getInstance().getDifferences(originalAttendee, updatedAttendee);
        AttendeeField[] updatedFields = AttendeeMapper.getInstance().getAssignedFields(attendeeUpdate);
        for (AttendeeField field : updatedFields) {
            switch (field) {
                case FOLDER_ID:
                    checkFolderUpdate(originalEvent, originalAttendee, attendeeUpdate.getFolderId());
                    break;
                case CU_TYPE:
                case ENTITY:
                case MEMBER:
                case URI:
                    throw CalendarExceptionCodes.FORBIDDEN_ATTENDEE_CHANGE.create(originalEvent.getId(), originalAttendee, field);
                default:
                    break;
            }
        }
        /*
         * take over identifying properties from original
         */
        AttendeeMapper.getInstance().copy(originalAttendee, attendeeUpdate, AttendeeField.ENTITY, AttendeeField.MEMBER, AttendeeField.CU_TYPE, AttendeeField.URI);
        if (session.getUserId() != calendarUserId && false == attendeeUpdate.containsSentBy()) {
            attendeeUpdate.setSentBy(session.getEntityResolver().applyEntityData(new CalendarUser(), session.getUserId()));
        }
        return attendeeUpdate;
    }

    private void checkFolderUpdate(Event originalEvent, Attendee originalAttendee, String updatedFolderID) throws OXException {
        if (false == originalAttendee.getFolderId().equals(folder.getId())) {
            throw CalendarExceptionCodes.FORBIDDEN_ATTENDEE_CHANGE.create(originalEvent.getId(), originalAttendee, AttendeeField.FOLDER_ID);
        }
        if (isSeriesMaster(originalEvent)) {
            throw CalendarExceptionCodes.MOVE_SERIES_NOT_SUPPORTED.create(originalEvent.getId(), folder.getId(), updatedFolderID);
        }
        if (isSeriesException(originalEvent)) {
            throw CalendarExceptionCodes.MOVE_OCCURRENCE_NOT_SUPPORTED.create(originalEvent.getId(), folder.getId(), updatedFolderID);
        }
        if (PublicType.getInstance().equals(folder.getType())) {
            throw CalendarExceptionCodes.FORBIDDEN_ATTENDEE_CHANGE.create(originalEvent.getId(), originalAttendee, AttendeeField.FOLDER_ID);
        }
        CalendarFolder targetFolder = Utils.getFolder(session, updatedFolderID);
        if (folder.getCreatedBy() != targetFolder.getCreatedBy()) {
            throw CalendarExceptionCodes.FORBIDDEN_ATTENDEE_CHANGE.create(originalEvent.getId(), originalAttendee, AttendeeField.FOLDER_ID);
        }
        requireCalendarPermission(targetFolder, CREATE_OBJECTS_IN_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        if (matches(originalEvent.getCreatedBy(), session.getUserId())) {
            requireCalendarPermission(folder, READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, DELETE_OWN_OBJECTS);
        } else {
            requireCalendarPermission(folder, READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, DELETE_ALL_OBJECTS);
        }
    }
}
