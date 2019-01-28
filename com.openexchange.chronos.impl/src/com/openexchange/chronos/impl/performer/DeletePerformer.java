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
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.initRecurrenceRule;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.splitExceptionDates;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.RecurrenceRange;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link DeletePerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DeletePerformer extends AbstractUpdatePerformer {

    /**
     * Initializes a new {@link DeletePerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    public DeletePerformer(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Performs the deletion of an event.
     *
     * @param objectId The identifier of the event to delete
     * @param recurrenceId The recurrence identifier of the occurrence to delete, or <code>null</code> if no specific occurrence is targeted
     * @param clientTimestamp The client timestamp to catch concurrent modifications
     * @return The result
     */
    public InternalCalendarResult perform(String objectId, RecurrenceId recurrenceId, long clientTimestamp) throws OXException {
        /*
         * load plain original event data
         */
        Event originalEvent = loadEventData(objectId);
        /*
         * check current session user's permissions
         */
        Check.eventIsInFolder(originalEvent, folder);
        requireUpToDateTimestamp(originalEvent, clientTimestamp);
        if (null == recurrenceId) {
            deleteEvent(originalEvent);
        } else {
            deleteRecurrence(originalEvent, recurrenceId);
        }
        return resultTracker.getResult();
    }

    /**
     * Deletes a single event.
     *
     * @param originalEvent The original event to delete
     * @return The result
     */
    private void deleteEvent(Event originalEvent) throws OXException {
        if (deleteRemovesEvent(originalEvent)) {
            /*
             * deletion of not group-scheduled event / by organizer / last user attendee
             */
            requireDeletePermissions(originalEvent);
            if (isSeriesException(originalEvent)) {
                deleteException(originalEvent);
            } else {
                delete(originalEvent);
            }
            return;
        }
        Attendee userAttendee = find(originalEvent.getAttendees(), calendarUserId);
        if (null != userAttendee) {
            /*
             * deletion as one of the attendees
             */
            requireDeletePermissions(originalEvent, userAttendee);
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
        throw CalendarExceptionCodes.NO_DELETE_PERMISSION.create(folder.getId());
    }

    /**
     * Deletes a specific recurrence of a recurring event.
     *
     * @param originalEvent The original exception event, or the targeted series master event
     * @return The result
     */
    private void deleteRecurrence(Event originalEvent, RecurrenceId recurrenceId) throws OXException {
        if (deleteRemovesEvent(originalEvent)) {
            /*
             * deletion of not group-scheduled event / by organizer / last user attendee
             */
            requireDeletePermissions(originalEvent);
            if (isSeriesMaster(originalEvent)) {
                recurrenceId = Check.recurrenceIdExists(session.getRecurrenceService(), originalEvent, recurrenceId);
                if (null != recurrenceId.getRange()) {
                    /*
                     * delete "this and future" recurrences; adjust recurrence rule to have a fixed UNTIL one second or day prior the targeted occurrence
                     */
                    Check.recurrenceRangeMatches(recurrenceId, RecurrenceRange.THISANDFUTURE);
                    Event eventUpdate = EventMapper.getInstance().copy(originalEvent, null, EventField.ID, EventField.SERIES_ID, EventField.START_DATE, EventField.END_DATE);
                    RecurrenceRule rule = initRecurrenceRule(originalEvent.getRecurrenceRule());
                    DateTime until = recurrenceId.getValue().addDuration(recurrenceId.getValue().isAllDay() ? new Duration(-1, 1, 0) : new Duration(-1, 0, 1));
                    rule.setUntil(until);
                    eventUpdate.setRecurrenceRule(rule.toString());
                    /*
                     * remove any change- and delete exceptions after the occurrence
                     */
                    Entry<SortedSet<RecurrenceId>, SortedSet<RecurrenceId>> splittedDeleteExceptionDates = splitExceptionDates(originalEvent.getDeleteExceptionDates(), until);
                    if (false == splittedDeleteExceptionDates.getValue().isEmpty()) {
                        eventUpdate.setDeleteExceptionDates(splittedDeleteExceptionDates.getKey());
                    }
                    Entry<SortedSet<RecurrenceId>, SortedSet<RecurrenceId>> splittedChangeExceptionDates = splitExceptionDates(originalEvent.getChangeExceptionDates(), until);
                    if (false == splittedChangeExceptionDates.getValue().isEmpty()) {
                        for (Event changeException : loadExceptionData(originalEvent, splittedChangeExceptionDates.getValue())) {
                            delete(changeException);
                        }
                        eventUpdate.setChangeExceptionDates(splittedChangeExceptionDates.getKey());
                    }
                    /*
                     * update series master in storage & track results
                     */
                    eventUpdate.setSequence(originalEvent.getSequence() + 1);
                    Consistency.setModified(session, timestamp, eventUpdate, session.getUserId());
                    storage.getEventStorage().updateEvent(eventUpdate);
                    Event updatedEvent = loadEventData(originalEvent.getId());
                    updateAlarmTrigger(originalEvent, updatedEvent);
                    resultTracker.trackUpdate(originalEvent, updatedEvent);
                } else if (contains(originalEvent.getChangeExceptionDates(), recurrenceId)) {
                    /*
                     * deletion of existing change exception
                     */
                    // deleteException(loadExceptionData(originalEvent.getId(), recurrenceID));
                    // TODO: not supported in old stack (attempt fails with APP-0011), so throwing exception as expected by test for now
                    // com.openexchange.ajax.appointment.recurrence.TestsForCreatingChangeExceptions.testShouldFailIfTryingToCreateADeleteExceptionOnTopOfAChangeException())
                    throw CalendarExceptionCodes.INVALID_RECURRENCE_ID.create(
                        new Exception("Deletion of existing change exception not supported"), recurrenceId, new DefaultRecurrenceData(originalEvent));
                } else {
                    /*
                     * creation of new delete exception
                     */
                    addDeleteExceptionDate(originalEvent, recurrenceId);
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
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(originalEvent.getId(), String.valueOf(recurrenceId));
        }
        Attendee userAttendee = find(originalEvent.getAttendees(), calendarUserId);
        if (null != userAttendee) {
            /*
             * deletion as attendee
             */
            requireDeletePermissions(originalEvent, userAttendee);
            if (isSeriesMaster(originalEvent)) {
                recurrenceId = Check.recurrenceIdExists(session.getRecurrenceService(), originalEvent, recurrenceId);
                if (contains(originalEvent.getChangeExceptionDates(), recurrenceId)) {
                    /*
                     * deletion of existing change exception
                     */
                    // deleteException(loadExceptionData(originalEvent.getId(), recurrenceID), userAttendee);
                    // TODO: not supported in old stack (attempt fails with APP-0011), so throwing exception as expected by test for now
                    // com.openexchange.ajax.appointment.recurrence.TestsForCreatingChangeExceptions.testShouldFailIfTryingToCreateADeleteExceptionOnTopOfAChangeException())
                    throw CalendarExceptionCodes.INVALID_RECURRENCE_ID.create(
                        new Exception("Deletion of existing change exception not supported"), recurrenceId, originalEvent.getRecurrenceRule());
                } else if (null != recurrenceId.getRange()) {
                    throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), originalEvent.getRecurrenceRule());
                } else {
                    /*
                     * creation of new delete exception
                     */
                    deleteFromRecurrence(originalEvent, recurrenceId, userAttendee);
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
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(originalEvent.getId(), String.valueOf(recurrenceId));
        }
        /*
         * no delete permissions, otherwise
         */
        throw CalendarExceptionCodes.NO_DELETE_PERMISSION.create(folder.getId());
    }

    /**
     * Adds a specific recurrence identifier to the series master's delete exception array, i.e. creates a new delete exception. A
     * previously existing entry for the recurrence identifier in the master's change exception date array is removed implicitly. In case
     * there are no occurrences remaining at all after the deletion, the whole series event is deleted.
     *
     * @param originalMasterEvent The original series master event
     * @param recurrenceId The recurrence identifier of the occurrence to add
     */
    private void addDeleteExceptionDate(Event originalMasterEvent, RecurrenceId recurrenceId) throws OXException {
        /*
         * build new set of delete exception dates
         */
        SortedSet<RecurrenceId> deleteExceptionDates = new TreeSet<RecurrenceId>();
        if (null != originalMasterEvent.getDeleteExceptionDates()) {
            deleteExceptionDates.addAll(originalMasterEvent.getDeleteExceptionDates());
        }
        if (false == deleteExceptionDates.add(recurrenceId)) {
            /*
             * delete exception data already exists, ignore
             */
            LOG.warn("Delete exeception data for {} already exists, ignoring.", recurrenceId);
        }
        /*
         * check if there are any further occurrences left
         */
        if (false == hasFurtherOccurrences(originalMasterEvent, deleteExceptionDates)) {
            /*
             * delete series master
             */
            delete(originalMasterEvent);
        } else {
            /*
             * re-build exception date lists based on existing series master to guarantee consistency
             */
            SortedSet<RecurrenceId> changeExceptionDates = loadChangeExceptionDates(originalMasterEvent.getSeriesId());
            for (RecurrenceId changeExceptionDate : changeExceptionDates) {
                if (deleteExceptionDates.remove(changeExceptionDate)) {
                    LOG.warn("Skipping {} in delete exception date collection due to existing change exception event.", changeExceptionDate);
                }
            }
            /*
             * update series master accordingly
             */
            resultTracker.rememberOriginalEvent(originalMasterEvent);
            Event eventUpdate = new Event();
            eventUpdate.setId(originalMasterEvent.getId());
            eventUpdate.setDeleteExceptionDates(deleteExceptionDates);
            if (false == changeExceptionDates.equals(originalMasterEvent.getChangeExceptionDates())) {
                eventUpdate.setChangeExceptionDates(changeExceptionDates);
            }
            eventUpdate.setSequence(originalMasterEvent.getSequence() + 1);
            Consistency.setModified(session, timestamp, eventUpdate, calendarUserId);
            storage.getEventStorage().updateEvent(eventUpdate);
            Event updatedMasterEvent = loadEventData(originalMasterEvent.getId());
            updateAlarmTrigger(originalMasterEvent, updatedMasterEvent);
            /*
             * track update of master in result
             */
            resultTracker.trackUpdate(originalMasterEvent, updatedMasterEvent);
        }
    }

    private void updateAlarmTrigger(Event originalMasterEvent, Event updatedMasterEvent) throws OXException {
        Map<Integer, List<Alarm>> alarms = storage.getAlarmStorage().loadAlarms(updatedMasterEvent);
        storage.getAlarmTriggerStorage().deleteTriggers(originalMasterEvent.getId());
        storage.getAlarmTriggerStorage().insertTriggers(updatedMasterEvent, alarms);
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
        String seriesId = originalExceptionEvent.getSeriesId();
        RecurrenceId recurrenceId = originalExceptionEvent.getRecurrenceId();
        delete(originalExceptionEvent);
        /*
         * update the series master accordingly
         */
        addDeleteExceptionDate(loadEventData(seriesId), recurrenceId);
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
        String seriesId = originalExceptionEvent.getSeriesId();
        delete(originalExceptionEvent, originalAttendee);
        /*
         * 'touch' the series master accordingly & track result
         */
        Event originalMasterEvent = loadEventData(seriesId);
        touch(seriesId);
        resultTracker.trackUpdate(originalMasterEvent, loadEventData(originalMasterEvent.getId()));
    }

}
