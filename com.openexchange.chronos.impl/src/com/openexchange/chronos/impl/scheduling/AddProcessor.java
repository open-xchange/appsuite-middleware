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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.impl.scheduling;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.impl.performer.AbstractUpdatePerformer;
import com.openexchange.chronos.scheduling.IncomingSchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.scheduling.SchedulingSource;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link AddProcessor} - Processes the method {@link SchedulingMethod#ADD}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v8.0.0
 */
public class AddProcessor extends AbstractUpdatePerformer {

    /** The source the scheduling was triggered by */
    private final SchedulingSource source;
    /** Flag to add attendee instead of throwing an error. For internal code re-usage. */
    private final boolean addUserAttendee;

    /**
     * Initializes a new {@link RequestProcessor}.
     * 
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @param source The source from which the scheduling has been triggered
     * @throws OXException If initialization fails
     */
    public AddProcessor(CalendarStorage storage, CalendarSession session, CalendarFolder folder, SchedulingSource source) throws OXException {
        this(storage, session, folder, source, false);
    }

    /**
     * Initializes a new {@link RequestProcessor}.
     * 
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @param source The source from which the scheduling has been triggered
     * @param addUserAttendee <code>true</code> to indicate that the calendar user shall be added to the list of attendees if she is missing.
     *            Set to <code>false</code> to throw an appropriated exception, which is default for the normal process of the {@link SchedulingMethod#ADD}.
     * @throws OXException If initialization fails
     */
    protected AddProcessor(CalendarStorage storage, CalendarSession session, CalendarFolder folder, SchedulingSource source, boolean addUserAttendee) throws OXException {
        super(storage, session, folder);
        this.source = source;
        this.addUserAttendee = addUserAttendee;
    }

    /**
     * Creates new change exceptions
     *
     * @param message The {@link IncomingSchedulingMessage}
     * @return An {@link InternalCalendarResult} containing the changes that has been performed
     * @throws OXException In case data is invalid, outdated or permissions are missing
     */
    public InternalCalendarResult process(IncomingSchedulingMessage message) throws OXException {
        return process(message, message.getResource().getEvents());
    }

    /**
     * Creates new change exceptions
     *
     * @param message The {@link IncomingSchedulingMessage}
     * @param changeExceptions The change exceptions to process
     * @return An {@link InternalCalendarResult} containing the changes that has been performed
     * @throws OXException In case data is invalid, outdated or permissions are missing
     */
    public InternalCalendarResult process(IncomingSchedulingMessage message, List<Event> changeExceptions) throws OXException {
        if (null == changeExceptions || changeExceptions.isEmpty()) {
            return resultTracker.getResult();
        }

        Event firstEvent = changeExceptions.get(0);
        EventID eventID = Utils.resolveEventId(session, storage, firstEvent.getUid(), null, message.getTargetUser());
        Event originalMasterEvent = loadEventData(eventID.getObjectID());
        if (false == CalendarUtils.isSeriesMaster(originalMasterEvent)) {
            /*
             * It is recommended to send a REFRESH, see https://tools.ietf.org/html/rfc5546#section-3.2.4
             * However we don't support this, so throw error
             */
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Can't add ocurrences to a non series event");
        }
        List<Event> originalChangeExceptions = loadExceptionData(originalMasterEvent);
        for (Event exception : changeExceptions) {
            if (null != find(originalChangeExceptions, exception.getRecurrenceId())) {
                LOG.debug("Unable to add existing recurrence");
                continue;
            }
            /*
             * Create new exception for existing series
             */
            createNewChangeException(message, originalMasterEvent, exception);
        }

        return resultTracker.getResult();
    }

    /*
     * ============================== HELPERS ==============================
     */

    /**
     * Finds a specific event identified by its object-identifier and an optional recurrence identifier in a collection. The lookup is
     * performed based on {@link RecurrenceId#matches(RecurrenceId)}.
     *
     * @param events The events to search in
     * @param recurrenceID The recurrence identifier of the event to search
     * @return The event, or <code>null</code> if not found
     * @see RecurrenceId#matches(RecurrenceId)
     */
    public static Event find(Collection<Event> events, RecurrenceId recurrenceID) {
        if (null == events) {
            return null;
        }
        for (Event event : events) {
            if (null != recurrenceID && recurrenceID.matches(event.getRecurrenceId())) {
                return event;
            }
        }
        return null;
    }

    /**
     * Creates a new change exception for the given series and adds the given attendee to the attendee list
     *
     * @param originalSeriesMaster The series master
     * @param recurrenceId The {@link RecurrenceId} to create the exception on
     * @param attendee The attendee to add
     * @return The created change exception
     * @throws OXException
     */
    private Event createNewChangeException(IncomingSchedulingMessage message, Event originalSeriesMaster, Event transmittedExceptionEvent) throws OXException {
        /*
         * Prepare new change exceptions
         */
        Map<Integer, List<Alarm>> seriesMasterAlarms = storage.getAlarmStorage().loadAlarms(originalSeriesMaster);
        Event newExceptionEvent = prepareException(originalSeriesMaster, transmittedExceptionEvent.getRecurrenceId());
        Check.quotaNotExceeded(storage, session);
        newExceptionEvent = prepareChangeException(message, newExceptionEvent, transmittedExceptionEvent);
        prepareAttendees(newExceptionEvent, message.getChangedPartStat());

        /*
         * Create exception
         */
        interceptorRegistry.triggerInterceptorsOnBeforeCreate(newExceptionEvent);
        storage.getEventStorage().insertEvent(newExceptionEvent);

        storage.getAttendeeStorage().insertAttendees(newExceptionEvent.getId(), originalSeriesMaster.getAttendees());
        storage.getAttachmentStorage().insertAttachments(session.getSession(), folder.getId(), newExceptionEvent.getId(), originalSeriesMaster.getAttachments());
        storage.getConferenceStorage().insertConferences(newExceptionEvent.getId(), prepareConferences(originalSeriesMaster.getConferences()));
        insertAlarms(newExceptionEvent, prepareExceptionAlarms(seriesMasterAlarms), true);
        newExceptionEvent = loadEventData(newExceptionEvent.getId());

        newExceptionEvent = loadEventData(newExceptionEvent.getId());
        resultTracker.trackCreation(newExceptionEvent, originalSeriesMaster);
        /*
         * Add change exception date to series master & track results
         */
        resultTracker.rememberOriginalEvent(originalSeriesMaster);
        addChangeExceptionDate(originalSeriesMaster, newExceptionEvent.getRecurrenceId(), false);
        Event updatedMasterEvent = loadEventData(originalSeriesMaster.getId());
        resultTracker.trackUpdate(originalSeriesMaster, updatedMasterEvent);
        /*
         * Reset alarm triggers for series master event and new change exception
         */
        storage.getAlarmTriggerStorage().deleteTriggers(updatedMasterEvent.getId());
        storage.getAlarmTriggerStorage().insertTriggers(updatedMasterEvent, seriesMasterAlarms);
        storage.getAlarmTriggerStorage().deleteTriggers(newExceptionEvent.getId());
        storage.getAlarmTriggerStorage().insertTriggers(newExceptionEvent, storage.getAlarmStorage().loadAlarms(newExceptionEvent));
        return newExceptionEvent;
    }

    /**
     * Prepares a new change exception before it is inserted into the storage
     *
     * @param newExceptionEvent The new change exception to insert
     * @param transmittedExceptionEvent The transmitted change exception by the organizer
     * @param message The message to get optional new attachments from
     * @return The prepared exception
     * @throws OXException
     */
    private Event prepareChangeException(IncomingSchedulingMessage message, Event newExceptionEvent, Event transmittedExceptionEvent) throws OXException {
        /*
         * Filter for attachments belonging to one special occurrence and add their binary representation
         */
        newExceptionEvent.setAttachments(SchedulingUtils.filterAttachments(newExceptionEvent.getAttachments(), transmittedExceptionEvent.getAttachments(), message));
        /*
         * Take over fields from transmitted event as-is
         */
        return EventMapper.getInstance().copy(transmittedExceptionEvent, newExceptionEvent, //@formatter:off
            new EventField[] { 
                    EventField.ATTENDEES, EventField.CONFERENCES, EventField.DESCRIPTION, EventField.END_DATE, 
                    EventField.EXTENDED_PROPERTIES, EventField.GEO, EventField.LOCATION, EventField.RELATED_TO, 
                    EventField.SEQUENCE, EventField.START_DATE, EventField.SUMMARY, EventField.URL
                });//@formatter:on
    }

    /**
     * Prepares the attendee list including the attendee of the acting user
     *
     * @param eventData The event
     * @param partStat The {@link ParticipationStatus} to set for the acting user
     * @throws OXException In case copying fails
     */
    private void prepareAttendees(Event event, ParticipationStatus partStat) throws OXException {
        List<Attendee> attendees = AttendeeMapper.getInstance().copy(event.getAttendees(), (AttendeeField[]) null);
        Attendee attendee = CalendarUtils.find(attendees, calendarUserId);
        if (null == attendee) {
            if (addUserAttendee) {
                /*
                 * The scheduling objects seems to have been forwarded to us. We are a "party-crasher"
                 * TODO We might can prepare a "delegation" here, too. See https://tools.ietf.org/html/rfc5546#section-3.2.2.3
                 */
                attendee = session.getEntityResolver().prepareUserAttendee(calendarUserId);
                attendees.add(attendee);
            }
            /*
             * Change exception without the current user
             */
            throw CalendarExceptionCodes.ATTENDEE_NOT_FOUND.create(calendarUser, event);
        }
        /*
         * Use transmitted participant status or (re-)set status to let the user decide
         */
        if (SchedulingSource.API.equals(source)) {
            attendee.setPartStat(partStat);
        } else {
            attendee.setPartStat(ParticipationStatus.NEEDS_ACTION);
        }
        event.setAttendees(attendees);
    }

}
