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

package com.openexchange.chronos.storage.rdb.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.caching.CacheService;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.AdministrativeAlarmTriggerStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.chronos.storage.rdb.AdministrativeRdbAlarmTriggerStorage;
import com.openexchange.chronos.storage.rdb.CalendarExternalAccountProvider;
import com.openexchange.chronos.storage.rdb.groupware.CalendarAddConferenceTablesTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarAlarmAddTimestampColumnTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarAlarmTriggerCorrectFolderTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarAlarmTriggerRemoveOrphanedTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarAttendeeAddHiddenColumnTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarAttendeeAddTimestampColumnTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarEventAddAttendeePrivilegesColumnTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarEventAddRDateColumnTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarEventAddSeriesIndexTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarEventAdjustRecurrenceColumnTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarEventCorrectFilenamesTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarEventCorrectOrganizerSentByTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarEventCorrectRangesTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarEventCorrectStaleOrganizerValuesTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarEventRemoveStaleFolderReferencesTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarEventUnfoldOrganizerValuesTask;
import com.openexchange.chronos.storage.rdb.groupware.CalendarStorageInterceptor;
import com.openexchange.chronos.storage.rdb.groupware.ChronosCreateTableService;
import com.openexchange.chronos.storage.rdb.groupware.ChronosCreateTableTask;
import com.openexchange.chronos.storage.rdb.groupware.RemoveOrphanedCalendarAlarmsTask;
import com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask;
import com.openexchange.chronos.storage.rdb.migration.ChronosStoragePurgeLegacyDataTask;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DatabaseServiceDBProvider;
import com.openexchange.external.account.ExternalAccountProvider;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.quota.QuotaService;
import com.openexchange.user.interceptor.UserServiceInterceptor;
import com.openexchange.userconf.UserPermissionService;

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
        return new Class<?>[] { DatabaseService.class, ContextService.class, RecurrenceService.class, ConfigurationService.class, QuotaService.class, CalendarProviderRegistry.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { CalendarUtilities.class, CacheService.class, UserPermissionService.class };
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
            // @formatter:off
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(
                new ChronosCreateTableTask(),
                new CalendarEventAddRDateColumnTask(),
                new CalendarEventAddSeriesIndexTask(),
                new ChronosStorageMigrationTask(this),
                new CalendarEventCorrectFilenamesTask(),
                new CalendarEventCorrectRangesTask(),
                new CalendarAttendeeAddHiddenColumnTask(),
                new CalendarAlarmAddTimestampColumnTask(),
                new CalendarAlarmTriggerCorrectFolderTask(),
                new CalendarEventAddAttendeePrivilegesColumnTask(),
                new RemoveOrphanedCalendarAlarmsTask(),
                new CalendarAlarmTriggerRemoveOrphanedTask(),
                new CalendarEventRemoveStaleFolderReferencesTask(),
                new CalendarEventCorrectOrganizerSentByTask(),
                new CalendarEventAdjustRecurrenceColumnTask(),
                new CalendarAttendeeAddTimestampColumnTask(),
                new CalendarAddConferenceTablesTask(),
                new CalendarEventUnfoldOrganizerValuesTask(),
                new CalendarEventCorrectStaleOrganizerValuesTask()
            ));
            // @formatter:on
            if (getService(ConfigurationService.class).getBoolProperty("com.openexchange.calendar.migration.purgeLegacyData", true)) {
                registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new ChronosStoragePurgeLegacyDataTask()));
            }
            registerService(UserServiceInterceptor.class, new CalendarStorageInterceptor(this, defaultDbProvider));
            /*
             * register storage factory services
             */
            registerService(CalendarStorageFactory.class, storageFactory);
            registerService(AdministrativeAlarmTriggerStorage.class, new AdministrativeRdbAlarmTriggerStorage());
            registerService(ExternalAccountProvider.class, new CalendarExternalAccountProvider(this));
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
