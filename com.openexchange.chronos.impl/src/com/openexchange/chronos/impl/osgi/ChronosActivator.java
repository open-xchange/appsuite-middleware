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

package com.openexchange.chronos.impl.osgi;

import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.impl.CalendarServiceImpl;
import com.openexchange.chronos.impl.FreeBusyServiceImpl;
import com.openexchange.chronos.impl.groupware.CalendarDeleteListener;
import com.openexchange.chronos.impl.groupware.CalendarDowngradeListener;
import com.openexchange.chronos.impl.osgi.event.EventAdminServiceTracker;
import com.openexchange.chronos.impl.session.DefaultCalendarUtilities;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.service.CalendarAvailabilityService;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.FreeBusyService;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.context.ContextService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.database.DatabaseService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.downgrade.DowngradeRegistry;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.quota.QuotaService;
import com.openexchange.resource.ResourceService;
import com.openexchange.threadpool.ThreadPoolService;
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

    //@formatter:off
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ConfigViewFactory.class, CalendarStorageFactory.class, /*CalendarAvailabilityStorageFactory.class,*/
            FolderService.class, ContextService.class, UserService.class, GroupService.class, ResourceService.class, DatabaseService.class, RecurrenceService.class,
            ThreadPoolService.class, QuotaService.class, LeanConfigurationService.class, AdministrativeCalendarAccountService.class, ConversionService.class };
    }
    //@formatter:on

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { FreeBusyService.class, ContactCollectorService.class, ObjectUseCountService.class, CalendarAvailabilityService.class };
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
            /*
             * register services
             */

            CalendarService calendarService = new CalendarServiceImpl(calendarHandlers);
            DefaultCalendarUtilities calendarUtilities = new DefaultCalendarUtilities(this);
            CalendarStorageFactory factory = getServiceSafe(CalendarStorageFactory.class);

            registerService(CalendarService.class, calendarService);
            registerService(FreeBusyService.class, new FreeBusyServiceImpl());
            registerService(CalendarUtilities.class, calendarUtilities);
            // Availability disabled until further notice
            //registerService(CalendarAvailabilityService.class, new CalendarAvailabilityServiceImpl());
            registerService(DeleteListener.class, new CalendarDeleteListener(factory, calendarService, calendarHandlers));
            DowngradeRegistry.getInstance().registerDowngradeListener(new CalendarDowngradeListener(factory, calendarService, calendarHandlers));
            /*
             * register calendar handler to propagate OSGi events
             */
            track(EventAdmin.class, new EventAdminServiceTracker(context));
            openTrackers();
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
}
