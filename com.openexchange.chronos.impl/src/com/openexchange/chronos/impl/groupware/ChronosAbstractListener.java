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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
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
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ChronosAbstractListener}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ChronosAbstractListener {

    private final CalendarStorageFactory      factory;
    private final CalendarUtilities           calendarUtilities;
    private final ServiceSet<CalendarHandler> calendarHandlers;

    private EntityResolver      entityResolver;
    private CalendarStorage     storage;
    private SimpleResultTracker tracker;

    /**
     * Initializes a new {@link ChronosDeleteListener}.
     * 
     * @param factory The {@link CalendarStorageFactory}
     * @param calendarUtilities The {@link CalendarUtilities}
     * @param calendarHandlers The {@link CalendarHandler}s to notify
     */
    public ChronosAbstractListener(CalendarStorageFactory factory, CalendarUtilities calendarUtilities, ServiceSet<CalendarHandler> calendarHandlers) {
        super();
        this.factory = factory;
        this.calendarUtilities = calendarUtilities;
        this.calendarHandlers = calendarHandlers;
    }

    /**
     * Get the calendar storage
     * 
     * @return The {@link CalendarStorage}
     */
    public CalendarStorage getStorage() {
        return storage;
    }

    /**
     * Get the tracker
     * 
     * @return The {@link SimpleResultTracker}
     */
    public SimpleResultTracker getTracker() {
        return tracker;
    }

    /**
     * Gets the entityResolver
     *
     * @return The {@link EntityResolver}
     */
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    /**
     * Copies an event
     * 
     * @param event The event to copy
     * @return The copied event
     * @throws OXException See {@link CalendarUtilities#copyEvent(Event, EventField...)}
     */
    protected Event copyEvent(Event event) throws OXException {
        return calendarUtilities.copyEvent(event, null);
    }

    /**
     * Initialize the {@link EntityResolver}, the {@link CalendarStorage} ant the {@link SimpleResultTracker}
     * 
     * @param context The {@link Context}
     * @param dbProvider The {@link DBProvider}
     * @throws OXException If the {@link EntityResolver} or the {@link CalendarStorage} can't be created
     */
    public void init(Context context, DBProvider dbProvider) throws OXException {
        entityResolver = calendarUtilities.getEntityResolver(context.getContextId());
        storage = factory.create(context, CalendarAccount.DEFAULT_ACCOUNT.getAccountId(), entityResolver, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        tracker = new SimpleResultTracker(calendarHandlers);
    }

    /**
     * Get all events where the given identifier matches an attendee
     * 
     * @param attendeeID The identifier of the attendee
     * @return a {@link List} of {@link Event}s
     * @throws OXException In case data can not be loaded
     */
    protected List<Event> getEvents(Integer attendeeID) throws OXException {
        EventField[] fields = new EventField[] { EventField.ID, EventField.FOLDER_ID, EventField.RECURRENCE_ID, EventField.ATTENDEES };
        List<Event> events = storage.getEventStorage().searchEvents(CalendarUtils.getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, new ConstantOperand<Integer>(attendeeID)), null, fields);
        return getStorage().getUtilities().loadAdditionalEventData(attendeeID.intValue(), events, new EventField[] { EventField.FOLDER_ID, EventField.RECURRENCE_ID, EventField.ATTENDEES });
    }

    /**
     * Removes the given attendee from the event
     * 
     * @param event The event to remove the attendee from
     * @param attendee To remove
     * @param replacement The replacement of the attendee
     * @param date The time of the change
     * @throws OXException Various
     */
    protected void removeAttendeeFromEvent(Event event, Attendee attendee, CalendarUser replacement, Date date) throws OXException {
        Event updatedEvent = copyEvent(event);
        updatedEvent.setModifiedBy(replacement);
        updatedEvent.setLastModified(date);
        updatedEvent.setTimestamp(date.getTime());
        getStorage().getEventStorage().updateEvent(updatedEvent);
        getStorage().getAttendeeStorage().deleteAttendees(event.getId(), Collections.singletonList(attendee));
        getTracker().addUpdate(event, updatedEvent);
    }

    /**
     * Delete an event
     * 
     * @param userId The identifier of the (last) attendee
     * @param serverSession The {@link ServerSession}. Is used to remove the attachments for an event.
     * @param date The {@link Date} to set {@link Event#setLastModified(Date)} and {@link Event#setTimestamp(long)} to
     * @param event The event to delete
     * @param deleteAlarms <code>true</code> to delete alarms and alarm triggers for a single event, <code>false</code> otherwise
     * @throws OXException Various
     */
    protected void deleteEvent(int userId, ServerSession serverSession, Date date, Event event, boolean deleteAlarms) throws OXException {
        if (deleteAlarms) {
            getStorage().getAlarmStorage().deleteAlarms(event.getId());
            getStorage().getAlarmTriggerStorage().deleteTriggers(event.getId());
        }
        getStorage().getAttachmentStorage().deleteAttachments(serverSession, CalendarUtils.getFolderView(event, userId), event.getId());
        getStorage().getAttendeeStorage().deleteAttendees(event.getId());
        getStorage().getEventStorage().deleteEvent(event.getId());
        getTracker().addDelete(event, date.getTime());
    }

    /**
     * Get all events for given attendee ID and either
     * <li> deletes the event if the attendee is the last internal user or</li>
     * <li> removes the attendee from the events attendee list</li>
     * 
     * @param context The context of the user
     * @param attendeeId The user identifier
     * @param replacement The {@link CalendarUser} to replace the user with
     * @param date The {@link Date} to set {@link Event#setLastModified(Date)} and {@link Event#setTimestamp(long)} to
     * @param isDowngrade <code>true</code> on downgrades to perform an extra check, <code>false</code> otherwise
     * @param deleteAlarms <code>true</code> to delete alarms and alarm triggers for a single event, <code>false</code> otherwise
     * @return The amount of touched events as int
     * @throws OXException Various
     */
    protected int manageEvents(Context context, int attendeeId, CalendarUser replacement, Date date, boolean isDowngrade, boolean deleteAlarms) throws OXException {
        List<Event> events = getEvents(Integer.valueOf(attendeeId));
        if (events.isEmpty()) {
            return 0;
        }

        ServerSession serverSession = null;
        for (Event event : events) {
            if (CalendarUtils.isLastUserAttendee(event.getAttendees(), attendeeId) || (isDowngrade && isPrivae())) {
                // The attendee is the only one left, delete event
                if (null == serverSession) {
                    // Get the session once we know that the attendee is an internal user 
                    serverSession = ServerSessionAdapter.valueOf(attendeeId, context.getContextId());
                }
                deleteEvent(attendeeId, serverSession, date, event, deleteAlarms);
            } else {
                removeAttendeeFromEvent(event, CalendarUtils.find(event.getAttendees(), attendeeId), replacement, date);
            }
        }
        return events.size();
    }

    /**
     * Checks if the given attendee created the event. see com.openexchange.calendar.CalendarAdministration.removePrivate(DowngradeEvent)
     * 
     * @return <code>true</code> If the attendee created the event, <code>false</code> otherwise
     */
    private boolean isPrivae() {
        /* TODO On downgrade delete all 'private' events without checking for last attendee */
        return false;
    }
}
