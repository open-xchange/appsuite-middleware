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
            LOG.error("Cannot reload " + GlobalDbInit.CONFIGFILE + ". GlobalDatabaseServiceImpl not available.");
            return;
        }
        if (DB_MIGRATION_EXECUTOR_REF.get() == null) {
            LOG.error("Cannot reload " + GlobalDbInit.CONFIGFILE + ". DBMigrationExecutorService not available.");
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
