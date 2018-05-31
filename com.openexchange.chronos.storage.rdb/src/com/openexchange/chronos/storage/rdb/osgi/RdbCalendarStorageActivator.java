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

package com.openexchange.chronos.storage.rdb.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.caching.CacheService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.chronos.storage.rdb.groupware.CalendarAlarmRepairShiftedTriggersTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarEventAddRDateColumnTask;
import com.openexchange.chronos.storage.rdb.groupware.ChronosCreateTableService;
import com.openexchange.chronos.storage.rdb.groupware.ChronosCreateTableTask;
import com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask;
import com.openexchange.chronos.storage.rdb.migration.ChronosStoragePurgeLegacyDataTask;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DatabaseServiceDBProvider;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.quota.QuotaService;

/**
 * {@link RdbCalendarStorageActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class RdbCalendarStorageActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(RdbCalendarStorageActivator.class);

    /**
     * Initializes a new {@link RdbCalendarStorageActivator}.
     */
    public RdbCalendarStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, ContextService.class, RecurrenceService.class, ConfigurationService.class, QuotaService.class};
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { CalendarUtilities.class, CacheService.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("starting bundle {}", context.getBundle());
            Services.set(this);
            DatabaseServiceDBProvider defaultDbProvider = new DatabaseServiceDBProvider(getService(DatabaseService.class));
            com.openexchange.chronos.storage.rdb.RdbCalendarStorageFactory storageFactory = new com.openexchange.chronos.storage.rdb.RdbCalendarStorageFactory(this, defaultDbProvider);
            /*
             * register services for infrastructure
             */
            registerService(CreateTableService.class, new ChronosCreateTableService());
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new ChronosCreateTableTask(), new CalendarEventAddRDateColumnTask(), new ChronosStorageMigrationTask(this)));
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new CalendarAlarmRepairShiftedTriggersTask(this)));
            if (getService(ConfigurationService.class).getBoolProperty("com.openexchange.calendar.migration.purgeLegacyData", false)) {
                registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new ChronosStoragePurgeLegacyDataTask()));
            }
            /*
             * register storage factory services
             */
            registerService(CalendarStorageFactory.class, storageFactory);
            // Availability disabled until further notice
            //registerService(CalendarAvailabilityStorageFactory.class, new com.openexchange.chronos.storage.rdb.RdbCalendarAvailabilityStorageFactory());
        } catch (Exception e) {
            LOG.error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle {}", context.getBundle());
        Services.set(null);
        super.stopBundle();
    }

}
