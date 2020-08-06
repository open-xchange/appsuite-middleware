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
import static com.openexchange.chronos.common.CalendarUtils.getUpdatedResource;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.EventOccurrence;
import com.openexchange.chronos.common.mapping.AttendeeEventUpdate;
import com.openexchange.chronos.common.mapping.DefaultEventUpdate;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventUpdate;
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
     */
    private void deleteEvent(Event originalEvent) throws OXException {
        if (deleteRemovesEvent(originalEvent)) {
            /*
             * deletion of not group-scheduled event / by organizer / last user attendee
             */
            requireDeletePermissions(originalEvent);
            if (isSeriesException(originalEvent)) {
                Event originalSeriesMaster = loadEventData(originalEvent.getSeriesId());
                List<Event> deletedEvents = deleteException(originalSeriesMaster, originalEvent);
                schedulingHelper.trackDeletion(new DefaultCalendarObjectResource(deletedEvents), originalSeriesMaster, null);
            } else {
                List<Event> deletedEvents = delete(originalEvent);
                schedulingHelper.trackDeletion(new DefaultCalendarObjectResource(deletedEvents));
            }
        } else {
            /*
             * deletion as one of the attendees
             */
            Attendee userAttendee = find(originalEvent.getAttendees(), calendarUserId);
            if (null == userAttendee) {
                throw CalendarExceptionCodes.NO_DELETE_PERMISSION.create(folder.getId());
            }
            requireDeletePermissions(originalEvent, userAttendee);
            if (isSeriesException(originalEvent)) {
                Event originalSeriesMaster = loadEventData(originalEvent.getSeriesId());
                List<EventUpdate> attendeeEventUpdates = deleteException(originalSeriesMaster, originalEvent, userAttendee);
                schedulingHelper.trackReply(userAttendee, getUpdatedResource(attendeeEventUpdates), originalSeriesMaster, attendeeEventUpdates);
            } else {
                List<EventUpdate> attendeeEventUpdates = delete(originalEvent, userAttendee);
                schedulingHelper.trackReply(userAttendee, getUpdatedResource(attendeeEventUpdates), attendeeEventUpdates);
            }
        }
    }

    /**
     * Deletes a specific recurrence of a recurring event.
     *
     * @param originalEvent The original exception event, or the targeted series master event
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
                     * delete "this and future" recurrences
                     */
                    Event updatedEvent = deleteFutureRecurrences(originalEvent, recurrenceId, true);
                    CalendarObjectResource updatedResource = new DefaultCalendarObjectResource(updatedEvent, loadExceptionData(updatedEvent, updatedEvent.getChangeExceptionDates()));
                    schedulingHelper.trackUpdate(updatedResource, new DefaultEventUpdate(originalEvent, updatedEvent));
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
                     * create new delete exception in master & prepare cancel or reply scheduling message representing the delete operation
                     */
                    Event virtualException = new EventOccurrence(originalEvent, recurrenceId);
                    addDeleteExceptionDate(originalEvent, recurrenceId);
                    schedulingHelper.trackDeletion(new DefaultCalendarObjectResource(virtualException), originalEvent, null);
                }
            } else if (isSeriesException(originalEvent)) {
                /*
                 * delete existing change exception & prepare cancel or reply scheduling message representing the delete operation
                 */
                Event originalSeriesMaster = loadEventData(originalEvent.getSeriesId());
                List<Event> deletedEvents = deleteException(originalSeriesMaster, originalEvent);
                schedulingHelper.trackDeletion(new DefaultCalendarObjectResource(deletedEvents), originalSeriesMaster, null);
            } else {
                /*
                 * unsupported, otherwise
                 */
                throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(originalEvent.getId(), String.valueOf(recurrenceId));
            }
        } else {
            /*
             * deletion as one of the attendees
             */
            Attendee userAttendee = find(originalEvent.getAttendees(), calendarUserId);
            if (null == userAttendee) {
                throw CalendarExceptionCodes.NO_DELETE_PERMISSION.create(folder.getId());
            }
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
                    List<EventUpdate> attendeeEventUpdates = deleteFromRecurrence(originalEvent, recurrenceId, userAttendee);
                    schedulingHelper.trackReply(userAttendee, getUpdatedResource(attendeeEventUpdates), originalEvent, attendeeEventUpdates);
                }
            } else if (isSeriesException(originalEvent)) {
                /*
                 * deletion of existing change exception
                 */
                Event originalSeriesMaster = loadEventData(originalEvent.getSeriesId());
                List<EventUpdate> attendeeEventUpdates = deleteException(originalSeriesMaster, originalEvent, userAttendee);
                schedulingHelper.trackReply(userAttendee, getUpdatedResource(attendeeEventUpdates), originalSeriesMaster, attendeeEventUpdates);
            } else {
                /*
                 * unsupported, otherwise
                 */
                throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(originalEvent.getId(), String.valueOf(recurrenceId));
            }
        }
    }

    /**
     * Deletes a specific internal user attendee from an existing change exception. Besides the removal of the attendee via
     * {@link #delete(Event, Attendee)}, this also includes 'touching' the master event's last-modification timestamp.
     *
     * @param originalSeriesMaster The original series master event
     * @param originalExceptionEvent The original exception event
     * @param originalAttendee The original attendee to delete
     * @return A list containing the performed event update as {@link AttendeeEventUpdate}
     */
    private List<EventUpdate> deleteException(Event originalSeriesMaster, Event originalExceptionEvent, Attendee originalAttendee) throws OXException {
        /*
         * delete the attendee in the exception
         */
        List<EventUpdate> attendeeEventUpdates = delete(originalExceptionEvent, originalAttendee);
        /*
         * 'touch' the series master accordingly & track result
         */
        touch(originalSeriesMaster.getId());
        resultTracker.trackUpdate(originalSeriesMaster, loadEventData(originalSeriesMaster.getId()));
        return attendeeEventUpdates;
    }

}
