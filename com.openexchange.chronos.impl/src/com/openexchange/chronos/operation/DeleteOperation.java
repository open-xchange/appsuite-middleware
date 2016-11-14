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

import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.isOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
import static com.openexchange.chronos.impl.Utils.i;
import static com.openexchange.folderstorage.Permission.DELETE_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.DELETE_OWN_OBJECTS;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.EventMapper;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;

/**
 * {@link DeleteOperation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DeleteOperation extends AbstractUpdateOperation {

    /**
     * Prepares a delete operation.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @return The prepared delete operation
     */
    public static DeleteOperation prepare(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        return new DeleteOperation(storage, session, folder);
    }

    /**
     * Initializes a new {@link DeleteOperation}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    private DeleteOperation(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Performs the deletion of an event.
     *
     * @param objectID The identifier of the event to delete
     * @param recurrenceID The recurrence identifier of the occurrence to delete, or <code>null</code> if no specific occurrence is targeted
     * @param clientTimestamp The client timestamp to catch concurrent modifications
     * @return The result
     */
    public CalendarResultImpl perform(int objectID, RecurrenceId recurrenceID, long clientTimestamp) throws OXException {
        /*
         * load original event data & attendees
         */
        Event originalEvent = loadEventData(objectID);
        /*
         * check current session user's permissions
         */
        Check.eventIsInFolder(originalEvent, folder);
        if (session.getUser().getId() != originalEvent.getCreatedBy()) {
            requireCalendarPermission(folder, READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, DELETE_ALL_OBJECTS);
        } else {
            requireCalendarPermission(folder, READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, DELETE_OWN_OBJECTS);
        }
        requireUpToDateTimestamp(originalEvent, clientTimestamp);
        if (null == recurrenceID) {
            deleteEvent(originalEvent);
        } else {
            deleteRecurrence(originalEvent, recurrenceID);
        }
        return result;
    }

    /**
     * Deletes a single event.
     *
     * @param originalEvent The original event to delete
     * @return The result
     */
    private void deleteEvent(Event originalEvent) throws OXException {
        if (isOrganizer(originalEvent, calendarUser.getId()) || isLastUserAttendee(originalEvent.getAttendees(), calendarUser.getId())) {
            /*
             * deletion by organizer / last user attendee
             */
            if (isSeriesException(originalEvent)) {
                deleteException(originalEvent);
            } else {
                delete(originalEvent);
            }
            return;
        }
        Attendee userAttendee = find(originalEvent.getAttendees(), calendarUser.getId());
        if (null != userAttendee) {
            /*
             * deletion as one of the attendees
             */
            if (isSeriesException(originalEvent)) {
                deleteException(originalEvent, userAttendee);
            } else {
                delete(originalEvent, userAttendee);
            }
            return;
        }
        /*
         * no delete permissions, otherwise
         */
        throw CalendarExceptionCodes.NO_DELETE_PERMISSION.create(I(i(folder)));
    }

    /**
     * Deletes a specific recurrence of a recurring event.
     *
     * @param originalEvent The original exception event, or the targeted series master event
     * @return The result
     */
    private void deleteRecurrence(Event originalEvent, RecurrenceId recurrenceID) throws OXException {
        if (isOrganizer(originalEvent, calendarUser.getId())) {
            /*
             * deletion as organizer
             */
            if (isSeriesMaster(originalEvent)) {
                if (null != originalEvent.getChangeExceptionDates() && originalEvent.getChangeExceptionDates().contains(new Date(recurrenceID.getValue()))) {
                    /*
                     * deletion of existing change exception
                     */
                    // deleteException(loadExceptionData(originalEvent.getId(), recurrenceID));
                    // TODO: not supported in old stack (attempt fails with APP-0011), so throwing exception as expected by test for now
                    // com.openexchange.ajax.appointment.recurrence.TestsForCreatingChangeExceptions.testShouldFailIfTryingToCreateADeleteExceptionOnTopOfAChangeException())
                    throw CalendarExceptionCodes.INVALID_RECURRENCE_ID.create(new Exception("Deletion of existing change exception not supported"), recurrenceID, originalEvent.getRecurrenceRule());
                } else {
                    /*
                     * creation of new delete exception
                     */
                    addDeleteExceptionDate(originalEvent, recurrenceID);
                }
                return;
            } else if (isSeriesException(originalEvent)) {
                /*
                 * deletion of existing change exception
                 */
                deleteException(originalEvent);
                return;
            }
            /*
             * unsupported, otherwise
             */
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(I(originalEvent.getId()), String.valueOf(recurrenceID));
        }
        Attendee userAttendee = find(originalEvent.getAttendees(), calendarUser.getId());
        if (null != userAttendee) {
            /*
             * deletion as attendee
             */
            if (isSeriesMaster(originalEvent)) {
                if (null != originalEvent.getChangeExceptionDates() && originalEvent.getChangeExceptionDates().contains(new Date(recurrenceID.getValue()))) {
                    /*
                     * deletion of existing change exception
                     */
                    // deleteException(loadExceptionData(originalEvent.getId(), recurrenceID), userAttendee);
                    // TODO: not supported in old stack (attempt fails with APP-0011), so throwing exception as expected by test for now
                    // com.openexchange.ajax.appointment.recurrence.TestsForCreatingChangeExceptions.testShouldFailIfTryingToCreateADeleteExceptionOnTopOfAChangeException())
                    throw CalendarExceptionCodes.INVALID_RECURRENCE_ID.create(new Exception("Deletion of existing change exception not supported"), recurrenceID, originalEvent.getRecurrenceRule());
                } else {
                    /*
                     * creation of new delete exception
                     */
                    deleteFromRecurrence(originalEvent, recurrenceID, userAttendee);
                }
                return;
            } else if (isSeriesException(originalEvent)) {
                /*
                 * deletion of existing change exception
                 */
                deleteException(originalEvent, userAttendee);
                return;
            }
            /*
             * unsupported, otherwise
             */
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(I(originalEvent.getId()), String.valueOf(recurrenceID));
        }
        /*
         * no delete permissions, otherwise
         */
        throw CalendarExceptionCodes.NO_DELETE_PERMISSION.create(I(i(folder)));
    }

    /**
     * Deletes a specific internal user attendee from a single event from the storage. This can be used for any kind of event, i.e. a
     * single, non-recurring event, an existing exception of an event series, or an event series. For the latter one, the attendee is deleted from
     * any existing event exceptions as well.
     * <p/>
     * The deletion includes:
     * <ul>
     * <li>insertion of a <i>tombstone</i> record for the original event</li>
     * <li>insertion of <i>tombstone</i> records for the original attendee</li>
     * <li>deletion of any alarms of the attendee associated with the event</li>
     * <li>deletion of the attendee from the event</li>
     * <li>update of the last-modification timestamp of the original event</li>
     * </ul>
     *
     * @param originalEvent The original event to delete
     * @param originalAttendee The original attendee to delete
     */
    private void delete(Event originalEvent, Attendee originalAttendee) throws OXException {
        /*
         * recursively delete any existing event exceptions for this attendee
         */
        int userID = originalAttendee.getEntity();
        if (isSeriesMaster(originalEvent)) {
            deleteExceptions(originalEvent.getSeriesId(), originalEvent.getChangeExceptionDates(), userID);
        }
        /*
         * delete event data from storage for this attendee
         */
        int objectID = originalEvent.getId();
        storage.getEventStorage().insertTombstoneEvent(EventMapper.getInstance().getTombstone(originalEvent, timestamp, calendarUser.getId()));
        storage.getAttendeeStorage().insertTombstoneAttendee(objectID, originalAttendee);
        storage.getAttendeeStorage().deleteAttendees(objectID, Collections.singletonList(originalAttendee));
        storage.getAlarmStorage().deleteAlarms(objectID, userID);
        /*
         * 'touch' event & add track update result
         */
        touch(objectID);
        result.addUpdate(new UpdateResultImpl(originalEvent, i(folder), loadEventData(objectID)));
    }

    /**
     * Deletes a specific internal user attendee from change exception events from the storage.
     * <p/>
     * For each change exception, the data is removed by invoking {@link #delete(Event, Attendee)} for the exception, in case the
     * user is found the exception's attendee list.
     *
     * @param seriesID The series identifier
     * @param exceptionDates The recurrence identifiers of the change exceptions to delete
     * @param userID The identifier of the user attendee to delete
     */
    private void deleteExceptions(int seriesID, List<Date> exceptionDates, int userID) throws OXException {
        if (null != exceptionDates && 0 < exceptionDates.size()) {
            List<RecurrenceId> recurrenceIDs = new ArrayList<RecurrenceId>();
            for (Date exceptionDate : exceptionDates) {
                recurrenceIDs.add(new DefaultRecurrenceId(exceptionDate));
            }
            for (Event originalExceptionEvent : loadExceptionData(seriesID, recurrenceIDs)) {
                Attendee originalUserAttendee = find(originalExceptionEvent.getAttendees(), userID);
                if (null != originalUserAttendee) {
                    delete(originalExceptionEvent, originalUserAttendee);
                }
            }
        }
    }

    /**
     * Adds a specific recurrence identifier to the series master's delete exception array, i.e. creates a new delete exception. A
     * previously existing entry for the recurrence identifier in the master's change exception date array is removed implicitly.
     *
     * @param originalMasterEvent The original series master event
     * @param recurrenceID The recurrence identifier of the occurrence to add
     */
    private void addDeleteExceptionDate(Event originalMasterEvent, RecurrenceId recurrenceID) throws OXException {
        Event eventUpdate = new Event();
        eventUpdate.setId(originalMasterEvent.getId());
        List<Date> deleteExceptionDates = new ArrayList<Date>();
        if (null != originalMasterEvent.getDeleteExceptionDates()) {
            deleteExceptionDates.addAll(originalMasterEvent.getDeleteExceptionDates());
        }
        if (false == deleteExceptionDates.add(new Date(recurrenceID.getValue()))) {
            // TODO throw/log?
        }
        eventUpdate.setDeleteExceptionDates(deleteExceptionDates);
        List<Date> changeExceptionDates = new ArrayList<Date>();
        if (null != originalMasterEvent.getChangeExceptionDates()) {
            changeExceptionDates.addAll(originalMasterEvent.getChangeExceptionDates());
        }
        if (changeExceptionDates.remove(new Date(recurrenceID.getValue()))) {
            eventUpdate.setChangeExceptionDates(changeExceptionDates);
        }
        Consistency.setModified(timestamp, eventUpdate, calendarUser.getId());
        storage.getEventStorage().updateEvent(eventUpdate);
        Event updatedMasterEvent = loadEventData(originalMasterEvent.getId());
        result.addUpdate(new UpdateResultImpl(originalMasterEvent, i(folder), updatedMasterEvent));
    }

    /**
     * Deletes an existing change exception. Besides the removal of the change exception data via {@link #delete(Event)}, this also
     * includes adjusting the master event's change- and delete exception date arrays.
     *
     * @param originalExceptionEvent The original exception event
     */
    private void deleteException(Event originalExceptionEvent) throws OXException {
        /*
         * delete the exception
         */
        int seriesID = originalExceptionEvent.getSeriesId();
        RecurrenceId recurrenceID = originalExceptionEvent.getRecurrenceId();
        delete(originalExceptionEvent);
        /*
         * update the series master accordingly
         */
        addDeleteExceptionDate(loadEventData(seriesID), recurrenceID);
    }

    /**
     * Deletes a specific internal user attendee from an existing change exception. Besides the removal of the attendee via
     * {@link #delete(Event, Attendee)}, this also includes 'touching' the master event's last-modification timestamp.
     *
     * @param originalExceptionEvent The original exception event
     * @param originalAttendee The original attendee to delete
     */
    private void deleteException(Event originalExceptionEvent, Attendee originalAttendee) throws OXException {
        /*
         * delete the attendee in the exception
         */
        int seriesID = originalExceptionEvent.getSeriesId();
        delete(originalExceptionEvent, originalAttendee);
        /*
         * 'touch' the series master accordingly
         */
        Event originalMasterEvent = loadEventData(seriesID);
        touch(seriesID);
        result.addUpdate(new UpdateResultImpl(originalMasterEvent, i(folder), loadEventData(seriesID)));
    }

    /**
     * Deletes a specific internal user attendee from a specific occurrence of a series event that does not yet exist as change exception.
     * This includes the creation of the corresponding change exception, and the removal of the user attendee from this exception's
     * attendee list.
     *
     * @param originalExceptionEvent The original series master event
     * @param recurrenceID The original series master event
     * @param recurrenceID The recurrence identifier of the occurrence to remove the attendee for
     */
    private void deleteFromRecurrence(Event originalMasterEvent, RecurrenceId recurrenceID, Attendee originalAttendee) throws OXException {
        /*
         * create new exception event
         */
        Event exceptionEvent = prepareException(originalMasterEvent, recurrenceID);
        storage.getEventStorage().insertEvent(exceptionEvent);
        /*
         * take over all other original attendees
         */
        List<Attendee> excpetionAttendees = new ArrayList<Attendee>(originalMasterEvent.getAttendees());
        excpetionAttendees.remove(originalAttendee);
        storage.getAttendeeStorage().insertAttendees(exceptionEvent.getId(), excpetionAttendees);
        /*
         * take over all other original alarms
         */
        for (Entry<Integer, List<Alarm>> entry : storage.getAlarmStorage().loadAlarms(originalMasterEvent.getId()).entrySet()) {
            int userID = entry.getKey().intValue();
            if (userID != originalAttendee.getEntity()) {
                storage.getAlarmStorage().insertAlarms(exceptionEvent.getId(), userID, entry.getValue());
            }
        }
        result.addCreation(new CreateResultImpl(loadEventData(exceptionEvent.getId())));
        /*
         * track new change exception date in master
         */
        addChangeExceptionDate(originalMasterEvent, recurrenceID);
    }

    /**
     * Gets a value indicating whether a specific user is the only / the last internal user attendee in an attendee list.
     *
     * @param attendees The attendees to check
     * @param userID The identifier of the user to lookup in the attendee list
     * @return <code>true</code> if there are no other internal user attendees despite the specified one, <code>false</code>, otherwise
     */
    private static boolean isLastUserAttendee(List<Attendee> attendees, int userID) {
        List<Attendee> userAttendees = CalendarUtils.filter(attendees, Boolean.TRUE, CalendarUserType.INDIVIDUAL);
        if (1 == userAttendees.size() && userID == userAttendees.get(0).getEntity()) {
            return true;
        }
        return false;
    }

}
