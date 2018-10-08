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

import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import static com.openexchange.chronos.common.CalendarUtils.isLastUserAttendee;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_SUPPRESS_ITIP;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarParameters;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarEventNotificationService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link CalendarDeleteListener} - Cleans up calendar data on deletion of types
 * <li>{@link DeleteEvent#TYPE_USER}</li>
 * <li>{@link DeleteEvent#TYPE_GROUP}</li>
 * <li>{@link DeleteEvent#TYPE_RESOURCE} </li>
 *
 * Type {@link DeleteEvent#TYPE_CONTEXT} is handled by com.openexchange.admin.storage.mysqlStorage.OXContextMySQLStorage#deleteTablesData.
 * Type {@link DeleteEvent#TYPE_RESOURCE_GROUP} will thrown an appropriated error.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public final class CalendarDeleteListener implements DeleteListener {

    private final CalendarUtilities calendarUtilities;
    private final CalendarEventNotificationService notificationService;

    /**
     * Initializes a new {@link CalendarDeleteListener}.
     *
     * @param calendarUtilities A reference to the calendar utilities
     * @param notificationService The {@link CalendarEventNotificationService}
     */
    public CalendarDeleteListener(CalendarUtilities calendarUtilities, CalendarEventNotificationService notificationService) {
        super();
        this.calendarUtilities = calendarUtilities;
        this.notificationService = notificationService;
    }

    @Override
    public void deletePerformed(DeleteEvent deleteEvent, Connection readCon, Connection writeCon) throws OXException {
        switch (deleteEvent.getType()) {
            case DeleteEvent.TYPE_USER:
                if (DeleteEvent.SUBTYPE_ANONYMOUS_GUEST != deleteEvent.getSubType() && DeleteEvent.SUBTYPE_INVITED_GUEST != deleteEvent.getSubType()) {
                    purgeUserData(new SimpleDBProvider(readCon, writeCon), deleteEvent.getContext(), deleteEvent.getId(), deleteEvent.getDestinationUserID(), deleteEvent.getSession());
                }
                break;
            case DeleteEvent.TYPE_GROUP:
            case DeleteEvent.TYPE_RESOURCE:
                deleteAttendee(new SimpleDBProvider(readCon, writeCon), deleteEvent.getContext(), deleteEvent.getId(), deleteEvent.getSession());
                break;
            case DeleteEvent.TYPE_RESOURCE_GROUP:
                throw DeleteFailedExceptionCodes.ERROR.create("Deletion of type RESOURCE_GROUP is not supported!");
            case DeleteEvent.TYPE_CONTEXT:
                /*
                 * DeleteEvent.TYPE_CONTEXT is handled by com.openexchange.admin.storage.mysqlStorage.OXContextMySQLStorage.deleteTablesData(String, Integer, Connection, boolean)
                 * All tables containing a 'cid' will be cleaned up by this. So there is no need to delete anything here.
                 */
                break;
            default:
                throw DeleteFailedExceptionCodes.UNKNOWN_TYPE.create(Integer.valueOf(deleteEvent.getType()));
        }
    }

    /**
     * Purges the user data
     *
     * @param dbProvider The {@link DBProvider}
     * @param context The {@link Context}
     * @param userId The user identifier
     * @param destinationUserId The identifier of the destination user specified in {@link DeleteEvent#getDestinationUserID()}
     * @param adminSession The context admins session
     * @throws OXException In case service is unavailable or SQL error
     */
    private void purgeUserData(DBProvider dbProvider, Context context, int userId, Integer destinationUserId, Session adminSession) throws OXException {
        EntityResolver entityResolver = calendarUtilities.getEntityResolver(context.getContextId());
        CalendarStorage storage = Services.getService(CalendarStorageFactory.class).create(context, Utils.ACCOUNT_ID, entityResolver, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        StorageUpdater updater = new StorageUpdater(storage, entityResolver, notificationService, userId, null != destinationUserId && 0 < i(destinationUserId) ? i(destinationUserId) : context.getMailadmin());
        /*
         * Get all events the user attends & distinguish between those that can be deleted completely, and those that need to be updated
         */
        List<Event> eventsToDelete = new LinkedList<>();
        List<Event> eventsToUpdate = new LinkedList<>();
        for (Event event : updater.searchEvents()) {
            if (isLastUserAttendee(event.getAttendees(), userId)) {
                // The attendee is the only one left, delete event
                eventsToDelete.add(event);
            } else {
                // remove attendee from group scheduled with other internal users
                eventsToUpdate.add(event);
            }
        }
        /*
         * Remove user references in events where the user is attendee, delete where he is the last internal user
         */
        updater.removeUserReferences(eventsToUpdate);
        updater.deleteEvent(eventsToDelete, ServerSessionAdapter.valueOf(userId, context.getContextId()));
        /*
         * Update event fields where the user might still be referenced
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.OR)
            .addSearchTerm(getSearchTerm(EventField.CREATED_BY, SingleOperation.EQUALS, Integer.valueOf(userId)))
            .addSearchTerm(CalendarUtils.getSearchTerm(EventField.MODIFIED_BY, SingleOperation.EQUALS, Integer.valueOf(userId)))
            .addSearchTerm(CalendarUtils.getSearchTerm(EventField.ORGANIZER, SingleOperation.EQUALS, '*' + ResourceId.forUser(context.getContextId(), userId) + '*'))
        ;
        updater.replaceAttendeeIn(updater.searchEvents(searchTerm));
        try {
            // Legacy storage doesen't know a calendar user, so make an independent call
            updater.replaceAttendeeIn(updater.searchEvents(CalendarUtils.getSearchTerm(EventField.CALENDAR_USER, SingleOperation.EQUALS, Integer.valueOf(userId))));
        } catch (IllegalArgumentException e) {
            if(false == e.getMessage().equals("No mapping available for: CALENDAR_USER")) {
                throw e;
            }
        }
        /*
         * Delete account
         */
        updater.deleteAccount();

        // Trigger calendar events
        updater.notifyCalendarHandlers(adminSession, new DefaultCalendarParameters().set(PARAMETER_SUPPRESS_ITIP, Boolean.TRUE));
    }

    /**
     * Removes the given attendee from every event it attends, set the modification date in the event accordingly
     * and finally deletes the attendee.
     *
     * @param dbProvider The {@link DBProvider}
     * @param context The {@link Context}
     * @param attendeeId The identifier of the attendee. Should be either a {@link CalendarUserType#GROUP} or {@link CalendarUserType#RESOURCE}
     * @param adminSession The context admins session
     * @throws OXException In case service is unavailable or SQL error
     */
    private void deleteAttendee(DBProvider dbProvider, Context context, int attendeeId, Session adminSession) throws OXException {
        EntityResolver entityResolver = calendarUtilities.getEntityResolver(context.getContextId());
        CalendarStorage storage = Services.getService(CalendarStorageFactory.class).create(context, Utils.ACCOUNT_ID, entityResolver, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        StorageUpdater updater = new StorageUpdater(storage, entityResolver, notificationService, attendeeId, context.getMailadmin());
        updater.removeAttendeeFrom(updater.searchEvents());
        updater.notifyCalendarHandlers(adminSession, new DefaultCalendarParameters().set(PARAMETER_SUPPRESS_ITIP, Boolean.TRUE));
    }

}
