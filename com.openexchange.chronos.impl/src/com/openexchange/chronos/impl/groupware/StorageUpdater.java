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

import java.sql.Connection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.session.Session;

/**
 * {@link StorageUpdater} - Update events by removing or replacing an attendee or by deleting the event.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
class StorageUpdater {

    private final Context             context;
    private final int                 attendeeId;
    private final CalendarUser        replacement;
    private final EntityResolver      entityResolver;
    private final CalendarStorage     storage;
    private final SimpleResultTracker tracker;
    private final CalendarUtilities   calendarUtilities;
    private final DBProvider          dbProvider;

    /**
     * Initializes a new {@link CalendarDeleteListener}.
     * 
     * @param context The {@link Context}
     * @param attendeeId The identifier of the attendee
     * @param destinationUserId The identifier of the destination user. <code>null</code> to use the context admin
     * @param calendarUtilities The {@link CalendarUtilities}
     * @param factory The {@link CalendarStorageFactory}
     * @param dbProvider The {@link DBProvider} holding the connections
     * @param calendarHandlers The {@link CalendarHandler}
     * @throws OXException In case {@link EntityResolver} or {@link CalendarStorage} can't be created
     */
    StorageUpdater(Context context, int attendeeId, Integer destinationUserId, CalendarUtilities calendarUtilities, CalendarStorageFactory factory, DBProvider dbProvider, ServiceSet<CalendarHandler> calendarHandlers) throws OXException {
        super();
        this.context = context;
        this.attendeeId = attendeeId;
        this.entityResolver = calendarUtilities.getEntityResolver(context.getContextId());
        this.replacement = entityResolver.prepareUserAttendee(null == destinationUserId ? context.getMailadmin() : destinationUserId.intValue());
        this.storage = factory.create(context, CalendarAccount.DEFAULT_ACCOUNT.getAccountId(), entityResolver, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        this.tracker = new SimpleResultTracker(calendarHandlers);
        this.calendarUtilities = calendarUtilities;
        this.dbProvider = dbProvider;
    }

    /**
     * Removes the attendee from the event and updates it
     * 
     * @param event The event to remove the attendee from
     * @param date The time of the change
     * @throws OXException Various
     */
    void removeAttendeeFrom(Event event, Date date) throws OXException {
        Event updatedEvent = calendarUtilities.copyEvent(event, null);
        updatedEvent.setModifiedBy(replacement);
        updatedEvent.setLastModified(date);
        updatedEvent.setTimestamp(date.getTime());
        storage.getAlarmStorage().deleteAlarms(event.getId());
        storage.getAlarmTriggerStorage().deleteTriggers(event.getId());
        storage.getAttendeeStorage().deleteAttendees(event.getId(), Collections.singletonList(CalendarUtils.find(event.getAttendees(), attendeeId)));
        storage.getEventStorage().updateEvent(updatedEvent);
        tracker.addUpdate(event, updatedEvent);
    }

    /**
     * Removes the attendee from the events and update them
     * 
     * @param events The events to remove the attendee from
     * @param date The time of the change
     * @throws OXException Various
     */
    void removeAttendeeFrom(List<Event> events, Date date) throws OXException {
        for (Event event : events) {
            removeAttendeeFrom(event, date);
        }
    }

    /**
     * Delete an event for the attendee
     * 
     * @param event The event to delete
     * @param session The {@link Session}. Is used to remove the attachments for an event.
     * @param date The {@link Date} to set {@link Event#setLastModified(Date)} and {@link Event#setTimestamp(long)} to
     * @throws OXException Various
     */
    void deleteEvent(Event event, Session session, Date date) throws OXException {
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
     * @param date The {@link Date} to set {@link Event#setLastModified(Date)} and {@link Event#setTimestamp(long)} to
     * @throws OXException Various
     */
    void deleteEvent(List<Event> events, Session session, Date date) throws OXException {
        for (Event event : events) {
            deleteEvent(event, session, date);
        }
    }

    /**
     * Check event fields where the attendee could be referenced in and replaces the attendee
     * 
     * @param event The {@link Event} to update
     * @param date The {@link Date} to set {@link Event#setLastModified(Date)} and {@link Event#setTimestamp(long)} to
     * @throws OXException Various
     */
    void replaceAttendeeIn(Event event, Date date) throws OXException {
        Event updatedEvent = calendarUtilities.copyEvent(event, null);
        if (CalendarUtils.matches(event.getCreatedBy(), attendeeId)) {
            updatedEvent.setCreatedBy(replacement);
        }
        if (CalendarUtils.matches(event.getModifiedBy(), attendeeId)) {
            updatedEvent.setModifiedBy(replacement);
        }
        if (CalendarUtils.matches(event.getCalendarUser(), attendeeId)) {
            updatedEvent.setCalendarUser(replacement);
        }
        if (CalendarUtils.matches(event.getOrganizer(), attendeeId)) {
            updatedEvent.setOrganizer(entityResolver.applyEntityData(new Organizer(), replacement.getEntity()));
        }
        updatedEvent.setLastModified(date);
        updatedEvent.setTimestamp(date.getTime());
        storage.getEventStorage().updateEvent(updatedEvent);
        tracker.addUpdate(event, updatedEvent);
    }

    /**
     * Check event fields where the attendee could be referenced in and replaces the attendee
     * 
     * @param events The {@link Event}s to update
     * @param date The new {@link Date} to set in the event
     * @throws OXException Various
     */
    void replaceAttendeeIn(List<Event> events, Date date) throws OXException {
        for (Event event : events) {
            replaceAttendeeIn(event, date);
        }
    }

    /**
     * Searches for all events in which the attendee participates in
     * 
     * @return A {@link List} of {@link Event}s
     * @throws OXException If events can't be loaded
     */
    List<Event> searchEvents() throws OXException {
        SingleSearchTerm searchTerm = CalendarUtils.getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, Integer.valueOf(attendeeId));
        List<Event> events = storage.getEventStorage().searchEvents(searchTerm, null, new EventField[] { EventField.ID, EventField.FOLDER_ID, EventField.RECURRENCE_ID });
        return storage.getUtilities().loadAdditionalEventData(attendeeId, events, new EventField[] { EventField.ATTENDEES });
    }

    /**
     * Searches for events with the given {@link SearchTerm}
     * 
     * @param searchTerm The {@link SearchTerm}
     * @param fields Additional data to load
     * @return A {@link List} of {@link Event}s
     * @throws OXException If events can't be loaded
     */
    List<Event> searchEvents(SearchTerm<?> searchTerm, EventField[] fields) throws OXException {
        return storage.getEventStorage().searchEvents(searchTerm, null, fields);
    }

    /**
     * Notifies the {@link CalendarHandler}s
     * 
     * @param session The {@link Session}
     * @throws OXException In case {@link Connection} can not be acquired
     */
    void notifyCalendarHandlers(Session session) throws OXException {
        tracker.notifyCalenderHandlers(dbProvider.getWriteConnection(context), context, session, entityResolver);
    }

    /**
     * Delete the default account for the attendee
     * 
     * @throws OXException In case account can't be deleted
     */
    void deleteAccount() throws OXException {
        storage.getAccountStorage().deleteAccount(attendeeId, CalendarAccount.DEFAULT_ACCOUNT.getAccountId());
    }
}
