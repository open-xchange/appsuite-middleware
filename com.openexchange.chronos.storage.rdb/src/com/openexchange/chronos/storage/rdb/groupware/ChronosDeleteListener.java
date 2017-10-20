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

package com.openexchange.chronos.storage.rdb.groupware;

import java.sql.Connection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.storage.AttendeeStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.chronos.storage.EventStorage;
import com.openexchange.chronos.storage.rdb.osgi.Services;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;

/**
 * {@link ChronosDeleteListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public final class ChronosDeleteListener implements DeleteListener {

    private CalendarStorageFactory factory;

    /**
     * Initializes a new {@link ChronosDeleteListener}.
     * 
     * @param factory The {@link CalendarStorageFactory}
     */
    public ChronosDeleteListener(CalendarStorageFactory factory) {
        super();
        this.factory = factory;
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER == event.getType()) {
            if (DeleteEvent.SUBTYPE_ANONYMOUS_GUEST != event.getSubType() && DeleteEvent.SUBTYPE_INVITED_GUEST != event.getSubType()) {
                /*
                 * remove all calendar data of deleted user
                 */
                purgeUserData(readCon, writeCon, event.getContext(), event.getId(), event.getSession());
            }
        } else if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            /*
             * remove all calendar data of deleted context
             */
            purgeContextData(readCon, writeCon, event.getContext(), event.getSession());
        }
    }

    /**
     * Purges the user data
     * 
     * @param readCon The readable {@link Connection}
     * @param writeCon The writable {@link Connection}
     * @param context The {@link Context}
     * @param userId The user identifier
     * @param session {@link Session} The users session
     * @throws OXException In case service is unavailable or SQL error
     */
    private void purgeUserData(Connection readCon, Connection writeCon, Context context, int userId, Session session) throws OXException {
        Integer userID = Integer.valueOf(userId);
        SimpleDBProvider dbProvider = new SimpleDBProvider(readCon, writeCon);
        int accountId = CalendarAccount.DEFAULT_ACCOUNT.getAccountId();
        CalendarStorage storage = factory.create(context, accountId, null, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);

        // Delete alarms for the user
        storage.getAlarmStorage().deleteAlarms(userId);
        storage.getAlarmTriggerStorage().deleteTriggers(userId);

        /*
         * Delete and update events.
         */
        EventStorage eventStorage = storage.getEventStorage();
        EventField[] fields = new EventField[] { EventField.ID };
        List<Event> events = null;

        // Admin CalendarUser as replacement
        CalendarUser admin = getCalendarUser(context, context.getMailadmin());

        // Update events where the user is the calendar user
        SingleSearchTerm user = new SingleSearchTerm(SingleOperation.EQUALS).addOperand(new ColumnFieldOperand<Enum<?>>(EventField.CALENDAR_USER)).addOperand(new ConstantOperand<Integer>(userID));
        events = eventStorage.searchEvents(user, null, fields);
        for (Event event : events) {
            event.setCalendarUser(admin);
            event.setLastModified(new Date());
            eventStorage.updateEvent(event);
        }

        // Update events of the user which he created
        SingleSearchTerm createdBy = new SingleSearchTerm(SingleOperation.EQUALS).addOperand(new ColumnFieldOperand<Enum<?>>(EventField.CREATED_BY)).addOperand(new ConstantOperand<Integer>(userID));
        events = eventStorage.searchEvents(createdBy, null, fields);
        for (Event event : events) {
            event.setCreatedBy(admin);
            event.setLastModified(new Date());
            eventStorage.updateEvent(event);
        }
        // Update events of the user where he is the modifier
        SingleSearchTerm modifiedBy = new SingleSearchTerm(SingleOperation.EQUALS).addOperand(new ColumnFieldOperand<Enum<?>>(EventField.MODIFIED_BY)).addOperand(new ConstantOperand<Integer>(userID));
        events = eventStorage.searchEvents(modifiedBy, null, fields);
        for (Event event : events) {
            event.setModifiedBy(admin);
            event.setLastModified(new Date());
            eventStorage.updateEvent(event);
        }
        // Update events of the user where he is the organizer
        SingleSearchTerm organizer = new SingleSearchTerm(SingleOperation.EQUALS).addOperand(new ColumnFieldOperand<Enum<?>>(EventField.ORGANIZER)).addOperand(new ConstantOperand<String>(ResourceId.forUser(context.getContextId(), userId)));
        events = eventStorage.searchEvents(organizer, null, fields);
        for (Event event : events) {
            event.setOrganizer(getOrganizer(admin));
            event.setLastModified(new Date());
            eventStorage.updateEvent(event);
        }

        /*
         * Update events of the user where he is attendee.
         * 1) load all remaining event
         * 2) load the attendees for those events
         * 3) find the user(as attendee)
         * 4) (Optional) Delete event if the user is the only attendee
         * 5) remove the user as attendee from the event
         * 6) delete attachments
         */
        fields = new EventField[] { EventField.ID, EventField.FOLDER_ID };
        SingleSearchTerm allEvents = new SingleSearchTerm(SingleOperation.EQUALS).addOperand(new ColumnFieldOperand<Enum<?>>(AttendeeField.ENTITY)).addOperand(new ConstantOperand<Integer>(userID));
        events = eventStorage.searchEvents(allEvents, null, fields);
        if (false == events.isEmpty()) {
            List<String> remainingEventIds = events.stream().map(Event::getId).collect(Collectors.toList());
            AttendeeStorage attendeeStorage = storage.getAttendeeStorage();
            Map<String, List<Attendee>> eventToAttendee = attendeeStorage.loadAttendees(remainingEventIds.toArray(new String[] {}));
            for (Event event : events) {
                List<Attendee> attendees = eventToAttendee.get(event.getId());
                if (attendees.size() == 1) {
                    // Only attendee, delete event
                    eventStorage.deleteEvent(event.getId());
                } else {
                    // Throw error if the user is not attendee in the event
                    attendees = Collections.singletonList(attendees.stream().filter(a -> a.getEntity() == userId).findFirst().orElseThrow(CalendarExceptionCodes.ATTENDEE_NOT_FOUND::create));
                }
                attendeeStorage.deleteAttendees(event.getId(), attendees);

                // We need to get the users view on the attachments, so create a session on behave of the deleted user
                String folderId = attendees.get(0).getFolderId();
                if (false == Strings.isEmpty(folderId)) {
                    storage.getAttachmentStorage().deleteAttachments(ServerSessionAdapter.valueOf(userId, context.getContextId()), folderId, event.getId());
                }
            }
        }

        // Delete account
        storage.getAccountStorage().deleteAccount(userId, accountId);

        // TODO Remove Tombstone
    }

    /**
     * Purges the context data
     * 
     * @param readCon The readable {@link Connection}
     * @param writeCon The writable {@link Connection}
     * @param context The {@link Context}
     * @param session {@link Session} The users session
     * @throws OXException In case service is unavailable or SQL error
     */
    private void purgeContextData(Connection readCon, Connection writeCon, Context context, Session session) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * ====== HELPERS ======
     */

    /**
     * Loads the user from the {@link UserService} and converts it to a {@link CalendarUser}
     * 
     * @param context The {@link Context}
     * @param userId The identifier of the user
     * @return The user as {@link CalendarUser}
     * @throws OXException In case of missing service
     */
    private CalendarUser getCalendarUser(Context context, int userId) throws OXException {
        User user = Services.getService(UserService.class, true).getUser(userId, context);
        CalendarUser calendarUser = new CalendarUser();
        calendarUser.setEntity(user.getId());
        calendarUser.setCn(user.getDisplayName());
        calendarUser.setUri(CalendarUtils.getURI(user.getMail()));
        return calendarUser;
    }

    /**
     * Get the organizer from the given user
     * 
     * @param user The {@link CalendarUser}
     * @return The user as {@link Organizer}
     */
    private Organizer getOrganizer(CalendarUser user) {
        Organizer organizer = new Organizer();
        organizer.setEntity(user.getEntity());
        organizer.setCn(user.getCn());
        organizer.setUri(user.getUri());
        return organizer;
    }
}
