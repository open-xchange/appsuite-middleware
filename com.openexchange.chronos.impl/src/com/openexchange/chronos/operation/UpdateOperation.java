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

import static com.openexchange.chronos.common.CalendarUtils.getUserIDs;
import static com.openexchange.chronos.common.CalendarUtils.hasExternalOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
import static com.openexchange.chronos.impl.Utils.i;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import static com.openexchange.folderstorage.Permission.WRITE_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.WRITE_OWN_OBJECTS;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.AttendeeHelper;
import com.openexchange.chronos.impl.AttendeeMapper;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.EventMapper;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;

/**
 * {@link UpdateOperation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdateOperation extends AbstractOperation {

    /**
     * Prepares an update operation.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @return The prepared update operation
     */
    public static UpdateOperation prepare(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        return new UpdateOperation(storage, session, folder);
    }

    /**
     * Initializes a new {@link UpdateOperation}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    private UpdateOperation(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        super(storage, session, folder);
    }

    public CalendarResultImpl perform(int objectID, Event updatedEvent, long clientTimestamp) throws OXException {
        /*
         * load original event data
         */
        Event originalEvent = loadEventData(objectID);
        /*
         * check current session user's permissions
         */
        if (hasExternalOrganizer(originalEvent) && originalEvent.getUid().equals(updatedEvent.getUid()) && matches(originalEvent.getOrganizer(), updatedEvent.getOrganizer())) {
            // don't check that the event already exists in the target folder to support later addition of internal users to externally
            // organized events; see https://bugs.open-xchange.com/show_bug.cgi?id=29566#c12 for details
        } else {
            Check.eventIsInFolder(originalEvent, folder);
        }
        if (session.getUser().getId() == originalEvent.getCreatedBy()) {
            requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, WRITE_OWN_OBJECTS, NO_PERMISSIONS);
        } else {
            requireCalendarPermission(folder, READ_FOLDER, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, NO_PERMISSIONS);
        }
        requireUpToDateTimestamp(originalEvent, clientTimestamp);
        /*
         * update event or event occurrence
         */
        if (CalendarUtils.isSeriesMaster(originalEvent) && updatedEvent.containsRecurrenceId() && null != updatedEvent.getRecurrenceId()) {
            updateEvent(originalEvent, updatedEvent, updatedEvent.getRecurrenceId());
        } else {
            updateEvent(originalEvent, updatedEvent);
        }
        return result;
    }

    private void updateEvent(Event originalEvent, Event updatedEvent, RecurrenceId recurrenceID) throws OXException {
        if (isSeriesMaster(originalEvent)) {
            if (null != originalEvent.getDeleteExceptionDates() && originalEvent.getDeleteExceptionDates().contains(new Date(recurrenceID.getValue()))) {
                throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(I(originalEvent.getSeriesId()), recurrenceID);
            }
            if (null != originalEvent.getChangeExceptionDates() && originalEvent.getChangeExceptionDates().contains(new Date(recurrenceID.getValue()))) {
                /*
                 * update for existing change exception
                 */
                Event originalExceptionEvent = loadExceptionData(originalEvent.getId(), recurrenceID);
                updateEvent(originalExceptionEvent, updatedEvent);
            } else {
                /*
                 * update for new change exception, prepare & insert a plain exception first
                 */
                Event newExceptionEvent = prepareException(originalEvent, Check.recurrenceIdExists(originalEvent, recurrenceID));
                storage.getEventStorage().insertEvent(newExceptionEvent);
                /*
                 * take over all original attendees & alarms
                 */
                List<Attendee> excpetionAttendees = new ArrayList<Attendee>(originalEvent.getAttendees());
                storage.getAttendeeStorage().insertAttendees(newExceptionEvent.getId(), excpetionAttendees);
                /*
                 * take over all original alarms
                 */
                for (Entry<Integer, List<Alarm>> entry : storage.getAlarmStorage().loadAlarms(originalEvent.getId()).entrySet()) {
                    storage.getAlarmStorage().insertAlarms(newExceptionEvent.getId(), entry.getKey().intValue(), entry.getValue());
                }
                /*
                 * reload the newly created exception as 'original' & perform the update
                 */
                newExceptionEvent = loadEventData(newExceptionEvent.getId());
                updateEvent(newExceptionEvent, updatedEvent);
                addChangeExceptionDate(originalEvent, recurrenceID);
                result.addCreation(new CreateResultImpl(loadEventData(newExceptionEvent.getId())));
            }
        } else if (isSeriesException(originalEvent)) {
            /*
             * update for existing change exception
             */
            updateEvent(originalEvent, updatedEvent);
        } else {
            /*
             * unsupported, otherwise
             */
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(I(originalEvent.getId()), String.valueOf(recurrenceID));
        }
    }

    private void updateEvent(Event originalEvent, Event updatedEvent) throws OXException {
        /*
         * update event data
         */
        boolean wasUpdated = false;
        Event eventUpdate = prepareEventUpdate(originalEvent, updatedEvent);
        if (null != eventUpdate) {
            /*
             * check for conflicts
             */
            Event newEvent = originalEvent.clone();
            EventMapper.getInstance().copy(eventUpdate, newEvent, EventField.values());
            List<Attendee> newAttendees;
            if (updatedEvent.containsAttendees()) {
                newAttendees = new AttendeeHelper(session, folder, originalEvent.getAttendees(), updatedEvent.getAttendees()).apply(originalEvent.getAttendees());
            } else {
                newAttendees = originalEvent.getAttendees();
            }
            List<EventConflict> conflicts = new ConflictChecker(session, storage).checkConflicts(newEvent, newAttendees);
            if (null != conflicts && 0 < conflicts.size()) {
                for (EventConflict eventConflict : conflicts) {
                    result.addConflict(eventConflict);
                }
                return;
            }
            /*
             * perform update
             */
            storage.getEventStorage().updateEvent(eventUpdate);
            if (isSeriesMaster(originalEvent) && resetChangeExceptions(originalEvent, eventUpdate)) {
                /*
                 * ensure to also delete any change exceptions if required
                 */
                deleteExceptions(originalEvent.getSeriesId(), originalEvent.getChangeExceptionDates());
            }
            wasUpdated = true;
        }
        /*
         * process any attendee updates
         */
        if (updatedEvent.containsAttendees()) {
            updateAttendees(originalEvent, updatedEvent);
            wasUpdated = true;
        }
        if (wasUpdated) {
            result.addUpdate(new UpdateResultImpl(originalEvent, i(folder), loadEventData(originalEvent.getId())));
        }
    }

    private boolean resetChangeExceptions(Event originalEvent, Event eventUpdate) throws OXException {
        if (false == isSeriesMaster(eventUpdate)) {
            return true;
        }
        Set<EventField> recurrenceRelatedFields = new HashSet<EventField>(java.util.Arrays.asList(EventField.RECURRENCE_RULE, EventField.START_DATE, EventField.END_DATE, EventField.START_TIMEZONE, EventField.END_TIMEZONE, EventField.ALL_DAY));
        EventField[] updatedFields = EventMapper.getInstance().getAssignedFields(EventMapper.getInstance().getDifferences(originalEvent, eventUpdate));
        for (EventField updatedField : updatedFields) {
            if (recurrenceRelatedFields.contains(updatedField)) {
                return true;
            }
        }
        return false;
    }

    private void updateAttendees(Event originalEvent, Event updatedEvent) throws OXException {
        List<Attendee> originalAttendees = originalEvent.getAttendees();
        List<Attendee> updatedAttendees = updatedEvent.getAttendees();
        AttendeeHelper attendeeHelper = new AttendeeHelper(session, folder, originalAttendees, updatedAttendees);
        /*
         * perform attendee deletions
         */
        List<Attendee> attendeesToDelete = attendeeHelper.getAttendeesToDelete();
        if (0 < attendeesToDelete.size()) {
            //TODO: any checks prior removal? user a must not delete user b?
            storage.getEventStorage().insertTombstoneEvent(EventMapper.getInstance().getTombstone(originalEvent, timestamp, calendarUser.getId()));
            storage.getAttendeeStorage().insertTombstoneAttendees(originalEvent.getId(), AttendeeMapper.getInstance().getTombstones(attendeesToDelete));
            storage.getAttendeeStorage().deleteAttendees(originalEvent.getId(), attendeesToDelete);
            storage.getAlarmStorage().deleteAlarms(originalEvent.getId(), getUserIDs(attendeesToDelete));
        }
        /*
         * perform attendee updates
         */
        List<Attendee> attendeesToUpdate = attendeeHelper.getAttendeesToUpdate();
        if (0 < attendeesToUpdate.size()) {
            //TODO: any checks prior removal? user a must not update user b?
            storage.getAttendeeStorage().updateAttendees(originalEvent.getId(), attendeesToUpdate);
        }
        /*
         * perform attendee inserts
         */
        if (0 < attendeeHelper.getAttendeesToInsert().size()) {
            //TODO: any checks prior removal? user a must not add user b if not organizer?
            storage.getAttendeeStorage().insertAttendees(originalEvent.getId(), attendeeHelper.getAttendeesToInsert());
        }
    }

    private Event prepareEventUpdate(Event originalEvent, Event updatedEvent) throws OXException {
        /*
         * determine & check modified fields
         */
        Event eventUpdate = EventMapper.getInstance().getDifferences(originalEvent, updatedEvent);
        EventField[] updatedFields = EventMapper.getInstance().getAssignedFields(eventUpdate);
        if (0 == updatedFields.length) {
            // TODO or throw?
            return null;
        }
        for (EventField field : EventMapper.getInstance().getAssignedFields(eventUpdate)) {
            switch (field) {
                case CLASSIFICATION:
                    Check.mandatoryFields(eventUpdate, EventField.CLASSIFICATION);
                    Check.classificationIsValid(eventUpdate.getClassification(), folder);
                    break;
                case ALL_DAY:
                    /*
                     * adjust start- and enddate, too, if required
                     */
                    EventMapper.getInstance().copyIfNotSet(originalEvent, eventUpdate, EventField.START_DATE, EventField.END_DATE);
                    Consistency.adjustAllDayDates(eventUpdate);
                    break;
                case RECURRENCE_RULE:
                    /*
                     * deny update for change exceptions (but ignore if set to 'null')
                     */
                    if (isSeriesException(originalEvent)) {
                        if (null == eventUpdate.getRecurrenceRule()) {
                            eventUpdate.removeRecurrenceRule();
                            break;
                        }
                        throw OXException.general("not allowed change");
                    }
                    if (isSeriesMaster(originalEvent) && null == eventUpdate.getRecurrenceRule()) {
                        /*
                         * series to single event, remove recurrence & ensure all necessary recurrence data is present in passed event update
                         */
                        EventMapper.getInstance().copyIfNotSet(originalEvent, eventUpdate, EventField.SERIES_ID, EventField.START_DATE, EventField.END_DATE, EventField.START_TIMEZONE, EventField.END_TIMEZONE, EventField.ALL_DAY);
                        eventUpdate.setSeriesId(0);
                        eventUpdate.setChangeExceptionDates(null);
                        eventUpdate.setDeleteExceptionDates(null);
                        break;
                    }
                    /*
                     * ensure all necessary recurrence related data is present in passed event update & check rule validity
                     */
                    EventMapper.getInstance().copyIfNotSet(originalEvent, eventUpdate, EventField.SERIES_ID, EventField.START_DATE, EventField.END_DATE, EventField.START_TIMEZONE, EventField.END_TIMEZONE, EventField.ALL_DAY);
                    Check.recurrenceRuleIsValid(eventUpdate);
                    /*
                     * single event to series, assign new recurrence id
                     */
                    if (0 >= originalEvent.getSeriesId()) {
                        eventUpdate.setSeriesId(originalEvent.getId());
                    }
                    break;
                case START_DATE:
                case END_DATE:
                    /*
                     * ensure all necessary recurrence related data is present in passed event update & check rule validity & re-validate start- and end date
                     */
                    EventMapper.getInstance().copyIfNotSet(originalEvent, eventUpdate, EventField.RECURRENCE_RULE, EventField.SERIES_ID, EventField.START_DATE, EventField.END_DATE, EventField.START_TIMEZONE, EventField.END_TIMEZONE, EventField.ALL_DAY);
                    Check.startAndEndDate(eventUpdate);
                    break;
                case RECURRENCE_ID:
                    if (false == isSeriesException(originalEvent) && null == eventUpdate.getRecurrenceId()) {
                        // ignore neutral value
                        break;
                    }
                    throw OXException.general("not allowed change");
                case UID:
                case CREATED:
                case CREATED_BY:
                case SEQUENCE:
                case SERIES_ID:
                case PUBLIC_FOLDER_ID:
                case CHANGE_EXCEPTION_DATES:
                case DELETE_EXCEPTION_DATES:
                    throw OXException.general("not allowed change");
                default:
                    break;
            }
        }
        EventMapper.getInstance().copy(originalEvent, eventUpdate, EventField.ID);
        Consistency.setModified(timestamp, eventUpdate, session.getUser().getId());
        eventUpdate.setSequence(1 + originalEvent.getSequence());
        return eventUpdate;
    }

}
