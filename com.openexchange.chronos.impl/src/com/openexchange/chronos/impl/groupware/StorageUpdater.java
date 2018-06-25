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

package com.openexchange.chronos.impl.groupware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.session.Session;

/**
 * {@link StorageUpdater} - Update events by removing or replacing an attendee or by deleting the event.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
class StorageUpdater {

    private static final EventField[] SEARCH_FIELDS = { EventField.ID, EventField.SERIES_ID, EventField.RECURRENCE_ID, EventField.FOLDER_ID, EventField.CREATED_BY, EventField.MODIFIED_BY,
        EventField.CALENDAR_USER, EventField.ORGANIZER, EventField.ATTENDEES };

    private final CalendarStorage storage;
    private final SimpleResultTracker tracker;
    private final int attendeeId;
    private final CalendarUser replacement;
    private final Date date;
    private final EntityResolver entityResolver;

    /**
     * Initializes a new {@link StorageUpdater}.
     *
     * @param storage The underlying calendar storage
     * @param entityResolver The entity resolver to use
     * @param dbProvider The database provider to use
     * @param attendeeId The identifier of the attendee
     * @param destinationUserId The identifier of the destination user
     */
    public StorageUpdater(CalendarStorage storage, EntityResolver entityResolver, int attendeeId, int destinationUserId) throws OXException {
        super();
        this.entityResolver = entityResolver;
        this.attendeeId = attendeeId;
        this.replacement = entityResolver.prepareUserAttendee(destinationUserId);
        this.tracker = new SimpleResultTracker();
        this.date = new Date();
        this.storage = storage;
    }

    /**
     * Removes the attendee from the event and updates it
     *
     * @param event The event to remove the attendee from
     * @throws OXException Various
     */
    void removeAttendeeFrom(Event event) throws OXException {
        List<Attendee> updatedAttendees = new ArrayList<Attendee>(event.getAttendees());
        Attendee attendee = CalendarUtils.find(updatedAttendees, attendeeId);
        if (null != attendee) {
            Event eventUpdate = new Event();
            eventUpdate.setId(event.getId());
            updatedAttendees.remove(attendee);
            eventUpdate.setAttendees(updatedAttendees);
            Consistency.setModified(date, eventUpdate, replacement);
            storage.getAlarmStorage().deleteAlarms(event.getId(), attendeeId);
            storage.getAlarmTriggerStorage().deleteTriggers(Collections.singletonList(event.getId()), attendeeId);
            storage.getAttendeeStorage().deleteAttendees(event.getId(), Collections.singletonList(attendee));
            storage.getEventStorage().updateEvent(eventUpdate);
            Event updatedEvent = EventMapper.getInstance().copy(event, null, (EventField[]) null);
            updatedEvent = EventMapper.getInstance().copy(eventUpdate, updatedEvent, (EventField[]) null);
            tracker.addUpdate(event, updatedEvent);
        }
    }

    /**
     * Removes the attendee from the events and update them
     *
     * @param events The events to remove the attendee from
     * @throws OXException Various
     */
    void removeAttendeeFrom(List<Event> events) throws OXException {
        for (final Event event : events) {
            removeAttendeeFrom(event);
        }
    }

    /**
     * Delete an event for the attendee
     *
     * @param event The event to delete
     * @param session The {@link Session}. Is used to remove the attachments for an event.
     * @throws OXException Various
     */
    void deleteEvent(Event event, Session session) throws OXException {
        storage.getAlarmStorage().deleteAlarms(event.getId());
        storage.getAlarmTriggerStorage().deleteTriggers(event.getId());
        storage.getAttachmentStorage().deleteAttachments(session, CalendarUtils.getFolderView(event, attendeeId), event.getId());
        storage.getAttendeeStorage().deleteAttendees(event.getId());
        storage.getEventStorage().deleteEvent(event.getId());
        tracker.addDelete(event, date.getTime());
    }

    /**
     * Delete all given events for the attendee
     *
     * @param events The events to delete
     * @param session The {@link Session}. Is used to remove the attachments for an event.
     * @throws OXException Various
     */
    void deleteEvent(List<Event> events, Session session) throws OXException {
        if (null == events || 0 == events.size()) {
            return;
        }
        List<String> eventIds = Arrays.asList(CalendarUtils.getObjectIDs(events));
        for (Event event : events) {
            storage.getAttachmentStorage().deleteAttachments(session, CalendarUtils.getFolderView(event, attendeeId), event.getId());
        }
        storage.getAlarmTriggerStorage().deleteTriggers(eventIds);
        storage.getAlarmStorage().deleteAlarms(eventIds);
        storage.getAttendeeStorage().deleteAttendees(eventIds);
        storage.getEventStorage().deleteEvents(eventIds);
        for (Event event : events) {
            tracker.addDelete(event, date.getTime());
        }
    }

    /**
     * Check event fields where the attendee could be referenced in and replaces the attendee
     *
     * @param event The {@link Event} to update
     * @throws OXException Various
     */
    void replaceAttendeeIn(Event event) throws OXException {
        Event eventUpdate = new Event();
        boolean updated = false;
        if (CalendarUtils.matches(event.getCreatedBy(), attendeeId)) {
            eventUpdate.setCreatedBy(replacement);
            updated = true;
        }
        if (CalendarUtils.matches(event.getModifiedBy(), attendeeId)) {
            eventUpdate.setModifiedBy(replacement);
            updated = true;
        }
        if (CalendarUtils.matches(event.getCalendarUser(), attendeeId)) {
            eventUpdate.setCalendarUser(replacement);
            updated = true;
        }
        if (null != event.getOrganizer()) {
            if (CalendarUtils.matches(event.getOrganizer(), attendeeId)) {
                eventUpdate.setOrganizer(entityResolver.applyEntityData(new Organizer(), replacement.getEntity()));
                updated = true;
            } else if (CalendarUtils.matches(event.getOrganizer().getSentBy(), attendeeId)) {
                Organizer organizer = new Organizer(event.getOrganizer());
                organizer.setSentBy(replacement);
                eventUpdate.setOrganizer(organizer);
                updated = true;
            }
        }
        if (updated) {
            eventUpdate.setId(event.getId());
            Consistency.setModified(date, eventUpdate, replacement);
            storage.getEventStorage().updateEvent(eventUpdate);
            Event updatedEvent = EventMapper.getInstance().copy(event, null, (EventField[]) null);
            updatedEvent = EventMapper.getInstance().copy(eventUpdate, updatedEvent, (EventField[]) null);
            tracker.addUpdate(event, updatedEvent);
        }
    }

    /**
     * Check event fields where the attendee could be referenced in and replaces the attendee
     *
     * @param events The {@link Event}s to update
     * @throws OXException Various
     */
    void replaceAttendeeIn(List<Event> events) throws OXException {
        for (final Event event : events) {
            replaceAttendeeIn(event);
        }
    }

    /**
     * Removes any references to the internal user from multiple events. This includes removing the user from the list of attendees,
     * removing his alarms and -triggers, as well as replacing him in the created-by-, modified-by-, organizer- and calendar-user-
     * properties.
     *
     * @param events The events to remove the user from
     */
    public void removeUserReferences(List<Event> events) throws OXException {
        if (null == events || events.isEmpty()) {
            return;
        }
        /*
         * delete any alarms and alarm triggers
         */
        storage.getAlarmTriggerStorage().deleteTriggers(attendeeId);
        storage.getAlarmStorage().deleteAlarms(attendeeId);
        /*
         * remove user references from each event
         */
        for (Event event : events) {
            removeUserReferences(event);
        }
    }

    private void removeUserReferences(Event event) throws OXException {
        Event eventUpdate = new Event();
        boolean updated = false;
        /*
         * remove user in event metadata
         */
        if (CalendarUtils.matches(event.getCreatedBy(), attendeeId)) {
            eventUpdate.setCreatedBy(replacement);
            updated = true;
        }
        if (CalendarUtils.matches(event.getModifiedBy(), attendeeId)) {
            eventUpdate.setModifiedBy(replacement);
            updated = true;
        }
        if (CalendarUtils.matches(event.getCalendarUser(), attendeeId)) {
            eventUpdate.setCalendarUser(replacement);
            updated = true;
        }
        if (CalendarUtils.matches(event.getOrganizer(), attendeeId)) {
            eventUpdate.setOrganizer(entityResolver.applyEntityData(new Organizer(), replacement.getEntity()));
            updated = true;
        }
        /*
         * remove user from attendees
         */
        Attendee attendee = CalendarUtils.find(event.getAttendees(), attendeeId);
        if (null != attendee) {
            storage.getAttendeeStorage().deleteAttendees(event.getId(), Collections.singletonList(attendee));
            List<Attendee> updatedAttendees = new ArrayList<Attendee>(event.getAttendees());
            updatedAttendees.remove(attendee);
            eventUpdate.setAttendees(updatedAttendees);
            updated = true;
        }
        /*
         * update event data in storage & track update result
         */
        if (updated) {
            eventUpdate.setId(event.getId());
            Consistency.setModified(date, eventUpdate, replacement);
            storage.getEventStorage().updateEvent(eventUpdate);
            Event updatedEvent = EventMapper.getInstance().copy(event, null, (EventField[]) null);
            updatedEvent = EventMapper.getInstance().copy(eventUpdate, updatedEvent, (EventField[]) null);
            tracker.addUpdate(event, updatedEvent);
        }
    }

    /**
     * Searches for all events in which the attendee participates in
     *
     * @return A {@link List} of {@link Event}s
     * @throws OXException If events can't be loaded
     */
    List<Event> searchEvents() throws OXException {
        return searchEvents(CalendarUtils.getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, Integer.valueOf(attendeeId)));
    }

    /**
     * Searches for events with the given {@link SearchTerm}
     *
     * @param searchTerm The {@link SearchTerm}
     * @return A {@link List} of {@link Event}s
     * @throws OXException If events can't be loaded
     */
    List<Event> searchEvents(SearchTerm<?> searchTerm) throws OXException {
        List<Event> events = storage.getEventStorage().searchEvents(searchTerm, null, SEARCH_FIELDS);
        return storage.getUtilities().loadAdditionalEventData(attendeeId, events, new EventField[] { EventField.ATTENDEES });
    }

    /**
     * Notifies the {@link CalendarHandler}s
     *
     * @param session The admin session
     * @param calendarHandlers The handlers to notify
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     */
    void notifyCalendarHandlers(Session session, Set<CalendarHandler> handlers, CalendarParameters parameters) throws OXException {
        tracker.notifyCalenderHandlers(session, entityResolver, handlers, parameters);
    }

    /**
     * Delete the default account for the attendee
     *
     * @throws OXException In case account can't be deleted
     */
    void deleteAccount() throws OXException {
        try {
            storage.getAccountStorage().deleteAccount(attendeeId, CalendarAccount.DEFAULT_ACCOUNT.getAccountId(), CalendarUtils.DISTANT_FUTURE);
        } catch (OXException e) {
            if ("CAL-4044".equals(e.getErrorCode())) {
                // "Account not found [account %1$d]"; ignore
                return;
            }
            throw e;
        }
    }

}
