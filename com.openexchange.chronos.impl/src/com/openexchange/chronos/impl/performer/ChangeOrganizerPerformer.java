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

import static com.openexchange.chronos.EventField.ORGANIZER;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.RecurrenceRange;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.Role;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;

/**
 * {@link ChangeOrganizerPerformer}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
public class ChangeOrganizerPerformer extends AbstractUpdatePerformer {

    /**
     * Initializes a new {@link ChangeOrganizerPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @throws OXException If the calendar user can't be resolved
     */
    public ChangeOrganizerPerformer(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Initializes a new {@link ChangeOrganizerPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @param roles The {@link Role}
     * @throws OXException If the calendar user can't be resolved
     */
    public ChangeOrganizerPerformer(CalendarStorage storage, CalendarSession session, CalendarFolder folder, EnumSet<Role> roles) throws OXException {
        super(storage, session, folder, roles);
    }

    /**
     * Performs the update operation.
     *
     * @param eventId The identifier of the event to update
     * @param recurrenceId The optional id of the recurrence.
     * @param organizer The new organizer to set
     * @param clientTimestamp The client timestamp to catch concurrent modifications
     * @return The update result
     * @throws OXException If data could not be loaded or constraints are not fulfilled
     */
    public InternalCalendarResult perform(String eventId, RecurrenceId recurrenceId, CalendarUser organizer, Long clientTimestamp) throws OXException {
        /*
         * Check if feature is enabled
         */
        if (false == session.getConfig().isOrganizerChangeAllowed()) {
            throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(eventId, ORGANIZER);
        }

        /*
         * Load original event data & check permissions
         */
        Event originalEvent = loadEventData(eventId);
        Check.eventIsVisible(folder, originalEvent);
        Check.eventIsInFolder(originalEvent, folder);
        requireWritePermissions(originalEvent);
        if (null != clientTimestamp) {
            requireUpToDateTimestamp(originalEvent, clientTimestamp.longValue());
        }

        /*
         * Ensure that new organizer is set and internal
         */
        if (null == organizer || false == CalendarUtils.isInternal(organizer, CalendarUserType.INDIVIDUAL)) {
            throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(organizer.getUri(), Integer.valueOf(organizer.getEntity()), CalendarUserType.INDIVIDUAL);
        }
        /*
         * Ensure that event is group scheduled
         */
        if (CalendarUtils.isPseudoGroupScheduled(originalEvent) || false == CalendarUtils.isGroupScheduled(originalEvent)) {
            throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(eventId, ORGANIZER);
        }
        /*
         * Ensure that attendees and current organizer are internal users
         */
        if (containsExternal(originalEvent.getAttendees()) || false == CalendarUtils.isInternal(originalEvent.getOrganizer(), CalendarUserType.INDIVIDUAL)) {
            throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(eventId, ORGANIZER);
        }

        /*
         * Update a single event
         */
        if (false == CalendarUtils.isSeriesMaster(originalEvent)) {
            if (CalendarUtils.isSeriesException(originalEvent)) {
                throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(eventId, ORGANIZER);
            }
            updateEvent(originalEvent, organizer);
            return resultTracker.getResult();
        }

        /*
         * Update a series starting at the master event
         */
        if (null == recurrenceId) {
            if (CalendarUtils.isSeriesException(originalEvent)) {
                throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(eventId, ORGANIZER);
            }
            updateSeries(originalEvent, organizer);
            return resultTracker.getResult();
        }

        /*
         * update "this and future" recurrences; first split the series at this recurrence
         */
        Check.recurrenceRangeMatches(recurrenceId, RecurrenceRange.THISANDFUTURE);
        new SplitPerformer(this).perform(originalEvent.getSeriesId(), recurrenceId.getValue(), null, originalEvent.getTimestamp());

        /*
         * reload the (now splitted) series event & apply the update, taking over a new recurrence rule as needed
         */
        updateSeries(originalEvent, organizer);

        return resultTracker.getResult();
    }

    /**
     * Applies the organizer change to a new {@link Event} so that only relevant fields will be updated
     * 
     * @param organizer The new organizer
     * @param originalEvent The original event
     * @return A delta {@link Event}
     * @throws OXException If resolving fails
     */
    private Event prepareChanges(Event originalEvent, CalendarUser organizer) throws OXException {
        Event updatedEvent = new Event();
        updatedEvent.setId(originalEvent.getId());

        if (originalEvent.containsSeriesId()) {
            updatedEvent.setSeriesId(originalEvent.getSeriesId());
        }

        // Set new organizer
        if (Organizer.class.isAssignableFrom(organizer.getClass())) {
            updatedEvent.setOrganizer((Organizer) organizer);
        } else {
            Organizer prepared = session.getEntityResolver().prepare(new Organizer(), CalendarUserType.INDIVIDUAL);
            prepared.setCn(organizer.getCn());
            prepared.setEMail(prepared.getEMail());
            prepared.setEntity(prepared.getEntity());
            prepared.setSentBy(organizer.getSentBy());
            prepared.setUri(organizer.getUri());
        }

        // Update meta data
        updatedEvent.setSequence(originalEvent.getSequence() + 1);
        updatedEvent.setTimestamp(timestamp.getTime());
        updatedEvent.setModifiedBy(calendarUser);

        return updatedEvent;
    }

    private boolean containsExternal(List<Attendee> attendees) {
        for (Attendee attendee : attendees) {
            if (CalendarUtils.isExternalUser(attendee)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Applies the new organizer to a series master and all its change exceptions.
     * Results will be tracked.
     * 
     * @param organizer The new organizer
     * @param originalEvent The original event
     * @throws OXException If update fails
     */
    private void updateSeries(Event originalEvent, CalendarUser organizer) throws OXException {
        Event updatedEvent = updateEvent(originalEvent, organizer);
        updateExceptions(originalEvent, updatedEvent, organizer);
    }

    /**
     * Update the organizer for a single event.
     * 
     * @param organizer The new organizer
     * @param originalEvent The original event
     * @return The updated {@link Event}
     * @throws OXException If updating fails
     */
    private Event updateEvent(Event originalEvent, CalendarUser organizer) throws OXException {
        Event prepareChanges = prepareChanges(originalEvent, organizer);
        storage.getEventStorage().updateEvent(prepareChanges);
        insertOrganizerAsAttendee(originalEvent, organizer);
        Event updatedEvent = loadEventData(originalEvent.getId());
        resultTracker.trackUpdate(originalEvent, updatedEvent);
        return updatedEvent;
    }

    /**
     * Loads series exceptions and applies the new organizer to them.
     * Results will be tracked.
     * 
     * @param originalEvent The original event
     * @param updatedEvent The updated series master
     * @param organizer The new organizer
     * @throws OXException If updating fails
     */
    private void updateExceptions(Event originalEvent, Event updatedEvent, CalendarUser organizer) throws OXException {
        for (Event e : loadExceptionData(updatedEvent)) {
            storage.getEventStorage().updateEvent(prepareChanges(e, organizer));
            insertOrganizerAsAttendee(e, organizer);
            resultTracker.trackUpdate(e, loadEventData(e.getId()));
        }
    }

    /**
     * Adds the new organizer to the list of attendees if necessary
     * 
     * @param originalEvent The original {@link Event}
     * @param organizer The new organizer as {@link CalendarUser}
     * @throws OXException If updating fails
     */
    private void insertOrganizerAsAttendee(Event originalEvent, CalendarUser organizer) throws OXException {
        // Ensure new organizer is attendee
        if (CalendarUtils.contains(originalEvent.getAttendees(), organizer)) {
            return;
        }

        // Add organizer as attendee
        Attendee attendee = session.getEntityResolver().prepareUserAttendee(organizer.getEntity());
        attendee.setPartStat(ParticipationStatus.NEEDS_ACTION);
        if (false == PublicType.getInstance().equals(folder.getType())) {
            attendee.setFolderId(session.getConfig().getDefaultFolderId(organizer.getEntity()));
        }
        storage.getAttendeeStorage().insertAttendees(originalEvent.getId(), Collections.singletonList(attendee));
    }
}
