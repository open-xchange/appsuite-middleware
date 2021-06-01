/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.impl.groupware;

import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import static com.openexchange.chronos.common.CalendarUtils.isLastUserAttendee;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_SCHEDULING;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.SchedulingControl;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarParameters;
import com.openexchange.chronos.common.EntityUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
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
import com.openexchange.java.Autoboxing;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.server.ServiceLookup;
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
	private final ServiceLookup services;

    /**
     * Initializes a new {@link CalendarDeleteListener}.
     * 
     * @param services The service lookup 
     * @param calendarUtilities A reference to the calendar utilities
     * @param notificationService The {@link CalendarEventNotificationService}
     */
    public CalendarDeleteListener(ServiceLookup services, CalendarUtilities calendarUtilities, CalendarEventNotificationService notificationService) {
        super();
        this.services = services;
        this.calendarUtilities = calendarUtilities;
        this.notificationService = notificationService;
    }

    @Override
    public void deletePerformed(DeleteEvent deleteEvent, Connection readCon, Connection writeCon) throws OXException {
        switch (deleteEvent.getType()) {
            case DeleteEvent.TYPE_USER:
                try {
                    if (DeleteEvent.SUBTYPE_ANONYMOUS_GUEST != deleteEvent.getSubType()) {
                        purgeUserData(new SimpleDBProvider(readCon, writeCon), deleteEvent.getContext(), deleteEvent.getId(), deleteEvent.getDestinationUserID(), deleteEvent.getSession());
                    }
                    break;
                } catch (Exception e) {
                    org.slf4j.LoggerFactory.getLogger(CalendarDeleteListener.class).error(
                        "Unexpected error handling delete event for entity {} in context {}: {}", I(deleteEvent.getId()), I(deleteEvent.getContext().getContextId()), e.getMessage(), e);
                    throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());  
                }
            case DeleteEvent.TYPE_GROUP:
            case DeleteEvent.TYPE_RESOURCE:
                try {
                    deleteAttendee(new SimpleDBProvider(readCon, writeCon), deleteEvent.getContext(), deleteEvent.getId(), deleteEvent.getSession());
                    break;
                } catch (Exception e) {
                    org.slf4j.LoggerFactory.getLogger(CalendarDeleteListener.class).error(
                        "Unexpected error handling delete event for entity {} in context {}: {}", I(deleteEvent.getId()), I(deleteEvent.getContext().getContextId()), e.getMessage(), e);
                    throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());  
                }
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
        StorageUpdater updater = new StorageUpdater(services, storage, entityResolver, notificationService, userId, null != destinationUserId && 0 < i(destinationUserId) ? i(destinationUserId) : context.getMailadmin());
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
        updater.removeUserReferences(eventsToUpdate, context);
        updater.deleteEvent(eventsToDelete, ServerSessionAdapter.valueOf(userId, context.getContextId()));
        /*
         * Update event fields where the user might still be referenced
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.OR)
            .addSearchTerm(getSearchTerm(EventField.CREATED_BY, SingleOperation.EQUALS, Integer.valueOf(userId)))
            .addSearchTerm(CalendarUtils.getSearchTerm(EventField.MODIFIED_BY, SingleOperation.EQUALS, Integer.valueOf(userId)))
            .addSearchTerm(CalendarUtils.getSearchTerm(EventField.ORGANIZER, SingleOperation.EQUALS, '*' + ResourceId.forUser(context.getContextId(), userId) + '*'))
        ;
        updater.replaceAttendeeIn(updater.searchEvents(searchTerm), context);
        try {
            // Legacy storage doesen't know a calendar user, so make an independent call
            updater.replaceAttendeeIn(updater.searchEvents(CalendarUtils.getSearchTerm(EventField.CALENDAR_USER, SingleOperation.EQUALS, Integer.valueOf(userId))), context);
        } catch (IllegalArgumentException e) {
            if (false == e.getMessage().equals("No mapping available for: CALENDAR_USER")) {
                throw e;
            }
        }
        /*
         * Replace attendee tombstones with externals
         */
        replaceTombstones(dbProvider, context, userId);
        /*
         * Delete account
         */
        updater.deleteAccount();

        // Trigger calendar events
        updater.notifyCalendarHandlers(adminSession, new DefaultCalendarParameters().set(PARAMETER_SCHEDULING, SchedulingControl.NONE));
    }

    private static final String SELECT_TOMBSTONES = "SELECT account, event, uri FROM calendar_attendee_tombstone WHERE cid = ? AND entity = ?;";

    private static final String REPLACE_TOMBSTONES = "UPDATE calendar_attendee_tombstone SET folder = NULL, entity = ? WHERE cid = ? AND account = ? AND event = ? AND entity = ?;";

    /**
     * Replaces the tombstone entries for the user with externals.
     *
     * @param dbProvider
     * @param context
     * @param userId
     * @throws OXException
     */
    private void replaceTombstones(DBProvider dbProvider, Context context, int userId) throws OXException {
        Connection con = dbProvider.getWriteConnection(context);
        boolean onlyRead = true;
        try {
            List<Triple<Integer, Integer, String>> triples = new ArrayList<>();
            try (PreparedStatement stmt = con.prepareStatement(SELECT_TOMBSTONES)) {
                stmt.setInt(1, context.getContextId());
                stmt.setInt(2, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        triples.add(new ImmutableTriple<>(Autoboxing.I(rs.getInt("account")), Autoboxing.I(rs.getInt("event")), rs.getString("uri")));
                    }
                }
            } catch (SQLException e) {
                throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
            }

            Random r = new Random();
            try (PreparedStatement stmt = con.prepareStatement(REPLACE_TOMBSTONES)) {
                Attendee attendee = new Attendee();
                attendee.setCuType(CalendarUserType.INDIVIDUAL);
                attendee.setEntity(userId);
                attendee.setEntity(-1);
                for (Triple<Integer, Integer, String> a : triples) {
                    attendee.setUri(a.getRight());
                    stmt.setInt(1, EntityUtils.determineEntity(attendee, new HashSet<>(), r.nextInt()));
                    stmt.setInt(2, context.getContextId());
                    stmt.setInt(3, Autoboxing.i(a.getLeft()));
                    stmt.setInt(4, Autoboxing.i(a.getMiddle()));
                    stmt.setInt(5, userId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                onlyRead = false;
            } catch (SQLException e) {
                throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
            }
        } finally {
            if (onlyRead) {
                dbProvider.releaseWriteConnectionAfterReading(context, con);
            } else {
                dbProvider.releaseWriteConnection(context, con);
            }
        }
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
        StorageUpdater updater = new StorageUpdater(services, storage, entityResolver, notificationService, attendeeId, context.getMailadmin());
        updater.removeAttendeeFrom(updater.searchEvents());
        updater.notifyCalendarHandlers(adminSession, new DefaultCalendarParameters().set(PARAMETER_SCHEDULING, SchedulingControl.NONE));
    }

}
