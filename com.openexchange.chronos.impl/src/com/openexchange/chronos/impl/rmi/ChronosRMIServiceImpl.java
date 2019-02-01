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
import com.openexchange.chronos.EventField;
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
        DatabaseService databaseService = Services.getService(DatabaseService.class);
        Connection readCon = null;
        Connection writeCon = null;
        try {
            readCon = databaseService.getReadOnly(contextId);
            writeCon = databaseService.getWritable(contextId);
            SimpleDBProvider dbProvider = new SimpleDBProvider(readCon, writeCon);

            ContextService contextService = Services.getService(ContextService.class);
            Context context = contextService.getContext(contextId);

            CalendarStorage storage = getStorage(contextId, dbProvider, context);
            EntityResolver entityResolver = calendarUtilities.getEntityResolver(contextId);
            Event event = loadEvent(eventId, storage);
            if (noop(event, userId)) {
                return;
            }

            checkPreConditions(event, userId, context);
            
            if (CalendarUtils.isSeriesMaster(event)) {
                handleMaster(event, userId, storage, entityResolver);
            } else if (CalendarUtils.isSeriesException(event)) {
                handleException(event, userId, storage, entityResolver);
            } else {
                handleSingle(event, userId, storage, entityResolver);
            }
        } catch (Exception e) {
            LOG.error("", e);
            String message = e.getMessage();
            throw new RemoteException(message, e);
        } finally {
            databaseService.backReadOnly(contextId, readCon);
            databaseService.backWritable(contextId, writeCon);
        }
    }

    private void handleException(Event exception, int userId, CalendarStorage storage, EntityResolver entityResolver) throws OXException, RemoteException {
        Event master = storage.getEventStorage().loadEvent(exception.getSeriesId(), null);
        master.setAttendees(storage.getAttendeeStorage().loadAttendees(master.getId()));
        handleMaster(master, userId, storage, entityResolver);
    }

    private void handleMaster(Event master, int userId, CalendarStorage storage, EntityResolver entityResolver) throws OXException, RemoteException {
        List<Event> exceptions = storage.getEventStorage().loadExceptions(master.getId(), null);
        for (Event exception : exceptions) {
            exception.setAttendees(storage.getAttendeeStorage().loadAttendees(exception.getId()));
            handleSingle(exception, userId, storage, entityResolver);
        }
        handleSingle(master, userId, storage, entityResolver);
    }

    private void handleSingle(Event event, int userId, CalendarStorage storage, EntityResolver entityResolver) throws RemoteException, OXException {
        Attendee newOrganizerAttendee = modifyEventObject(event, userId, entityResolver);
        save(event, storage, newOrganizerAttendee);
    }

    private void save(Event event, CalendarStorage storage, Attendee newOrganizerAttendee) throws OXException {
        storage.getEventStorage().updateEvent(event);
        if (newOrganizerAttendee != null) {
            storage.getAttendeeStorage().insertAttendees(event.getId(), Collections.singletonList(newOrganizerAttendee));
        }
    }

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

    private boolean noop(Event event, int newOrganizer) {
        if (!CalendarUtils.isOrganizer(event, newOrganizer)) {
            return false;
        }
        if (event.getAttendees().stream().noneMatch(a -> a.getEntity() == newOrganizer)) {
            return false;
        }
        return true;
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
