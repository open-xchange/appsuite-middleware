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

package com.openexchange.chronos.impl.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.impl.CalendarEventNotificationServiceImpl;
import com.openexchange.chronos.impl.CalendarServiceImpl;
import com.openexchange.chronos.impl.FreeBusyServiceImpl;
import com.openexchange.chronos.impl.groupware.CalendarDeleteListener;
import com.openexchange.chronos.impl.groupware.CalendarDowngradeListener;
import com.openexchange.chronos.impl.osgi.event.EventAdminServiceTracker;
import com.openexchange.chronos.impl.rmi.ChronosRMIServiceImpl;
import com.openexchange.chronos.impl.session.DefaultCalendarUtilities;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.rmi.ChronosRMIService;
import com.openexchange.chronos.scheduling.SchedulingBroker;
import com.openexchange.chronos.scheduling.changes.DescriptionService;
import com.openexchange.chronos.scheduling.changes.SchedulingChangeService;
import com.openexchange.chronos.service.AdministrativeFreeBusyService;
import com.openexchange.chronos.service.CalendarAvailabilityService;
import com.openexchange.chronos.service.CalendarEventNotificationService;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarInterceptor;
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
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.principalusecount.PrincipalUseCountService;
import com.openexchange.quota.QuotaService;
import com.openexchange.regional.RegionalSettingsService;
import com.openexchange.resource.ResourceService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.oxfolder.property.FolderSubscriptionHelper;
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
        return new Class<?>[] { FreeBusyService.class, ContactCollectorService.class, ObjectUseCountService.class, CalendarAvailabilityService.class,
            PrincipalUseCountService.class, CalendarEventNotificationService.class, SchedulingBroker.class, SchedulingChangeService.class, DescriptionService.class,
            RegionalSettingsService.class, FolderSubscriptionHelper.class, MimeTypeMap.class };
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
            CalendarEventNotificationService notificationService = new CalendarEventNotificationServiceImpl(calendarHandlers);
            registerService(CalendarEventNotificationService.class, notificationService);
            /*
             * track calendar interceptors
             */
            ServiceSet<CalendarInterceptor> calendarInterceptors = new ServiceSet<CalendarInterceptor>();
            track(CalendarInterceptor.class, calendarInterceptors);
            /*
             * register services
             */
            DefaultCalendarUtilities calendarUtilities = new DefaultCalendarUtilities(this);
            {
                Dictionary<String, Object> props = new Hashtable<String, Object>(2);
                props.put("RMIName", ChronosRMIService.RMI_NAME);
                registerService(Remote.class, new ChronosRMIServiceImpl(calendarUtilities), props);
            }
            CalendarServiceImpl calendarService = new CalendarServiceImpl(this, calendarInterceptors);
            registerService(CalendarService.class, calendarService);
            addService(CalendarService.class, calendarService);
            registerService(FreeBusyService.class, new FreeBusyServiceImpl());
            registerService(AdministrativeFreeBusyService.class, new FreeBusyServiceImpl());
            registerService(CalendarUtilities.class, calendarUtilities);
            addService(CalendarUtilities.class, calendarUtilities);

            // Availability disabled until further notice
            //registerService(CalendarAvailabilityService.class, new CalendarAvailabilityServiceImpl());
            registerService(DeleteListener.class, new CalendarDeleteListener(this, calendarUtilities, notificationService));
            DowngradeRegistry.getInstance().registerDowngradeListener(new CalendarDowngradeListener(this, calendarUtilities, notificationService));
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
