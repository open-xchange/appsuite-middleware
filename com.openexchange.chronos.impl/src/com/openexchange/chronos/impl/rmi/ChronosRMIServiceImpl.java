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

package com.openexchange.chronos.impl.rmi;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.impl.session.DefaultCalendarUtilities;
import com.openexchange.chronos.rmi.ChronosRMIService;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.user.UserService;

/**
 * {@link ChronosRMIServiceImpl}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class ChronosRMIServiceImpl implements ChronosRMIService {

    Logger LOG = LoggerFactory.getLogger(ChronosRMIServiceImpl.class);

    private DefaultCalendarUtilities calendarUtilities;

    public ChronosRMIServiceImpl(DefaultCalendarUtilities calendarUtilities) {
        super();
        this.calendarUtilities = calendarUtilities;
    }

    @Override
    public void setEventOrganizer(int contextId, int eventId, int userId) throws RemoteException {
        
        Connection readCon = null;
        Connection writeCon = null;
        DatabaseService databaseService = null;
        boolean backAfterRead = true;
        try {
            databaseService = Services.getService(DatabaseService.class, true);
            readCon = databaseService.getReadOnly(contextId);
            writeCon = databaseService.getWritable(contextId);
            SimpleDBProvider dbProvider = new SimpleDBProvider(readCon, writeCon);

            ContextService contextService = Services.getService(ContextService.class, true);
            Context context = contextService.getContext(contextId);

            CalendarStorage storage = getStorage(contextId, dbProvider, context);
            EntityResolver entityResolver = calendarUtilities.getEntityResolver(contextId);
            Event event = loadEvent(eventId, storage);
            if (isNoop(event, userId)) {
                return;
            }
            backAfterRead = false;
            checkPreConditions(event, userId, context);

            if (CalendarUtils.isSeriesMaster(event)) {
                handleMaster(event.getId(), userId, storage, entityResolver);
            } else if (CalendarUtils.isSeriesException(event)) {
                handleMaster(event.getSeriesId(), userId, storage, entityResolver);
            } else {
                handleSingle(event, userId, storage, entityResolver);
            }
        } catch (RemoteException re) {
            throw re;
        } catch (Exception e) {
            LOG.error("", e);
            String message = e.getMessage();
            throw new RemoteException(message, e);
        } finally {
            if (databaseService != null) {
                if(readCon != null) {
                    databaseService.backReadOnly(contextId, readCon);
                }
                if(writeCon != null) {
                    if(backAfterRead) {
                        databaseService.backWritableAfterReading(contextId, writeCon);
                    } else {
                        databaseService.backWritable(contextId, writeCon);
                    }
                }
            }
        }
    }

    /**
     * Handles the series master and all of it's exceptions.
     *
     * @param seriesId The master id
     * @param userId The user who should become organizer
     * @param storage The calendar storage
     * @param entityResolver The entity resolver
     * @throws OXException if an error occurs
     * @throws RemoteException if an error occurs
     */
    private void handleMaster(String seriesId, int userId, CalendarStorage storage, EntityResolver entityResolver) throws OXException {
        List<Event> exceptions = storage.getEventStorage().loadExceptions(seriesId, null);
        for (Event exception : exceptions) {
            exception.setAttendees(storage.getAttendeeStorage().loadAttendees(exception.getId()));
            handleSingle(exception, userId, storage, entityResolver);
        }

        Event master = storage.getEventStorage().loadEvent(seriesId, null);
        if (master != null) {
            master.setAttendees(storage.getAttendeeStorage().loadAttendees(master.getId()));
            handleSingle(master, userId, storage, entityResolver);
        }
    }

    /**
     * Handles a single event (either a series master, an exception or just a plain single event).
     *
     * @param event The event to handle
     * @param userId The user who should become organizer
     * @param storage The calendar storage
     * @param entityResolver The entity resolver
     * @throws OXException if an error occurs
     * @throws RemoteException if an error occurs
     */
    private void handleSingle(Event event, int userId, CalendarStorage storage, EntityResolver entityResolver) throws OXException {
        Attendee newOrganizerAttendee = modifyEventObject(event, userId, entityResolver);
        storage.getEventStorage().updateEvent(event);
        if (newOrganizerAttendee != null) {
            storage.getAttendeeStorage().insertAttendees(event.getId(), Collections.singletonList(newOrganizerAttendee));
        }
    }

    /**
     * Modifies the given event by adding the new Organizer (also as attendee if missing).
     * 
     * @param event The event to modify
     * @param newOrganizer The user who should become organizer
     * @param entityResolver The entity resolver
     * @return An attendee if adding is necessary
     * @throws OXException if an error occurs
     */
    private Attendee modifyEventObject(Event event, int newOrganizer, EntityResolver entityResolver) throws OXException {
        Attendee newOrganizerAttendee = null;
        if (!CalendarUtils.isOrganizer(event, newOrganizer)) {
            event.setOrganizer(entityResolver.applyEntityData(new Organizer(), newOrganizer));
            LOG.info("Changed organizer for event {} to {}.", event.getId(), event.getOrganizer().toString());
        }
        if (event.getAttendees().stream().noneMatch(a -> a.getEntity() == newOrganizer)) {
            Attendee organizer = entityResolver.applyEntityData(new Attendee(), newOrganizer);
            event.getAttendees().add(organizer);
            newOrganizerAttendee = organizer;
            LOG.info("Added organizer {} to attendees for event {}.", organizer.toString(), event.getId());
        }
        event.setTimestamp(System.currentTimeMillis());
        return newOrganizerAttendee;
    }

    /**
     * Checks if any changes need to be performed or if this is a no-op.
     *
     * @param event The event to check
     * @param newOrganizer The potential new organizer
     * @return true if this is a no-op, false otherwise
     */
    private boolean isNoop(Event event, int newOrganizer) {
        if (!CalendarUtils.isOrganizer(event, newOrganizer)) {
            return false;
        }
        return !event.getAttendees().stream().noneMatch(a -> a.getEntity() == newOrganizer);
    }

    private void checkPreConditions(Event event, int newOrganizer, Context context) throws RemoteException, OXException {
        if (CalendarUtils.hasExternalOrganizer(event)) {
            throw new RemoteException("Current organizer '" + event.getOrganizer().toString() + "' is external.");
        }

        try {
            Services.getService(UserService.class).getUser(newOrganizer, context);
        } catch (OXException e) {
            if (UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                throw new RemoteException("Invalid user: " + newOrganizer, e);
            }
            throw e;
        }
    }

    private Event loadEvent(int eventId, CalendarStorage storage) throws OXException {
        Event event = storage.getEventStorage().loadEvent(Integer.toString(eventId), null);
        event.setAttendees(storage.getAttendeeStorage().loadAttendees(event.getId()));
        return event;
    }

    private CalendarStorage getStorage(int contextId, DBProvider dbProvider, Context context) throws OXException {
        EntityResolver entityResolver = calendarUtilities.getEntityResolver(contextId);
        return Services.getService(CalendarStorageFactory.class).create(context, Utils.ACCOUNT_ID, entityResolver, dbProvider, DBTransactionPolicy.NORMAL_TRANSACTIONS);
    }

}
