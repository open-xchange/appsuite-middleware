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

package com.openexchange.database.internal.reloadable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.database.internal.GlobalDatabaseServiceImpl;
import com.openexchange.database.internal.GlobalDbConfig;
import com.openexchange.database.internal.GlobalDbInit;
import com.openexchange.database.migration.DBMigrationExceptionCodes;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.database.migration.DBMigrationState;
import com.openexchange.exception.OXException;

/**
 * {@link Reloadable} responsible for reloading the global database yml file
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class GlobalDbConfigsReloadable implements Reloadable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GlobalDbConfigsReloadable.class);

    private static final AtomicReference<GlobalDatabaseServiceImpl> GLOBAL_DB_REF = new AtomicReference<>();

    private static final AtomicReference<DBMigrationExecutorService> DB_MIGRATION_EXECUTOR_REF = new AtomicReference<>();

    /**
     * Initializes a new {@link GenericReloadable}.
     */
    public GlobalDbConfigsReloadable() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        if (GLOBAL_DB_REF.get() == null) {
            LOG.error("Cannot reload {}. GlobalDatabaseServiceImpl not available.", GlobalDbInit.CONFIGFILE);
            return;
        }
        if (DB_MIGRATION_EXECUTOR_REF.get() == null) {
            LOG.error("Cannot reload {}. DBMigrationExecutorService not available.", GlobalDbInit.CONFIGFILE);
            return;
        }

        try {
            Map<String, GlobalDbConfig> loadGlobalDbConfigs = GLOBAL_DB_REF.get().loadGlobalDbConfigs(configService);
            if (loadGlobalDbConfigs.isEmpty()) {
                LOG.info("Global database configuration is empty. No task for an update to schedule but will set empty configuration for further processing.");
                GLOBAL_DB_REF.get().setGlobalDbConfigs(loadGlobalDbConfigs);
                return;
            }

            List<DBMigrationState> scheduleMigrations = GLOBAL_DB_REF.get().scheduleMigrations(DB_MIGRATION_EXECUTOR_REF.get(), loadGlobalDbConfigs);
            for (DBMigrationState state : scheduleMigrations) {
                try {
                    state.awaitCompletion();
                } catch (ExecutionException e) {
                    LOG.error("", e.getCause());
                    throw DBMigrationExceptionCodes.DB_MIGRATION_ERROR.create(e.getCause());
                } catch (InterruptedException e) {
                    LOG.error("", e);
                    Thread.currentThread().interrupt();
                    throw DBMigrationExceptionCodes.DB_MIGRATION_ERROR.create(e);
                }
            }
            GLOBAL_DB_REF.get().setGlobalDbConfigs(loadGlobalDbConfigs);
        } catch (OXException e) {
            LOG.error("Unable to reload global database configuration from globaldb.yml. Please review your changes and reload again!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().configFileNames(GlobalDbInit.CONFIGFILE).build();
    }

    public static void setGlobalDatabaseServiceRef(GlobalDatabaseServiceImpl lGlobalDatabaseServiceRef) {
        GLOBAL_DB_REF.set(lGlobalDatabaseServiceRef);
    }

    public static void setGlobalDatabaseServiceRef(DBMigrationExecutorService lDBMigrationExecutorService) {
        DB_MIGRATION_EXECUTOR_REF.set(lDBMigrationExecutorService);
    }
}
