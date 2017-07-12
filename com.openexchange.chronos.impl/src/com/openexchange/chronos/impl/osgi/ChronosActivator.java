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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.impl.osgi;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.CalendarAvailability;
import com.openexchange.chronos.CalendarFreeSlot;
import com.openexchange.chronos.impl.CalendarServiceImpl;
import com.openexchange.chronos.impl.FreeBusyServiceImpl;
import com.openexchange.chronos.impl.availability.CalendarAvailabilityServiceImpl;
import com.openexchange.chronos.impl.session.DefaultCalendarSession;
import com.openexchange.chronos.impl.session.DefaultCalendarUtilities;
import com.openexchange.chronos.service.CalendarAvailabilityService;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.FreeBusyService;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.CalendarAvailabilityStorageFactory;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.chronos.storage.LegacyCalendarStorageFactory;
import com.openexchange.chronos.storage.ReplayingCalendarStorageFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.group.GroupService;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.resource.ResourceService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;

/**
 * {@link ChronosActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ChronosActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(ChronosActivator.class);

    /**
     * Initializes a new {@link ChronosActivator}.
     */
    public ChronosActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ConfigViewFactory.class, CalendarStorageFactory.class, CalendarAvailabilityStorageFactory.class, FolderService.class, ContextService.class, UserService.class, GroupService.class, ResourceService.class, DatabaseService.class, RecurrenceService.class, ThreadPoolService.class, LegacyCalendarStorageFactory.class, ReplayingCalendarStorageFactory.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { FreeBusyService.class, ContactCollectorService.class, ObjectUseCountService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("starting bundle {}", context.getBundle());
            Services.setServiceLookup(this);
            /*
             * track calendar handlers
             */
            ServiceSet<CalendarHandler> calendarHandlers = new ServiceSet<CalendarHandler>();
            track(CalendarHandler.class, calendarHandlers);
            openTrackers();
            /*
             * register services
             */
            CalendarService calendarService = new CalendarServiceImpl(calendarHandlers);
            registerService(CalendarService.class, calendarService);
            registerService(FreeBusyService.class, new FreeBusyServiceImpl());
            registerService(CalendarUtilities.class, new DefaultCalendarUtilities(this));
            CalendarAvailabilityServiceImpl service = new CalendarAvailabilityServiceImpl();
            registerService(CalendarAvailabilityService.class, service);
            testCAService(calendarService, service);
        } catch (Exception e) {
            LOG.error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle {}", context.getBundle());
        super.stopBundle();
    }

    private void testCAService(CalendarService cs, CalendarAvailabilityService cas) throws OXException {
        CalendarAvailability ca = new CalendarAvailability();
        CalendarFreeSlot freeSlot = new CalendarFreeSlot();
        freeSlot.setCreated(new Date());
        freeSlot.setStartTime(new Date());
        freeSlot.setUid(UUID.randomUUID().toString());
        freeSlot.setEndTime(new Date(System.currentTimeMillis() + 3600));
        freeSlot.setCreationTimestamp(new Date());
        ca.setCalendarFreeSlots(Collections.singletonList(freeSlot));
        ca.setStartTime(new Date(System.currentTimeMillis() - 14400));
        ca.setEndTime(new Date(System.currentTimeMillis() + 14400));
        ca.setCreated(new Date(System.currentTimeMillis()));
        ca.setCreationTimestamp(System.currentTimeMillis());
        ca.setUid(UUID.randomUUID().toString());
        
        
        CalendarSession calendarSession = new DefaultCalendarSession(ServerSessionAdapter.valueOf(3, 31145), cs);
        cas.setAvailability(calendarSession, Collections.singletonList(ca));
    }

}
