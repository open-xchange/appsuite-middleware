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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import com.openexchange.config.ConfigurationService;
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

    private static final String[] PROPERTIES = new String[] { "complete yaml file" };
    private final GlobalDatabaseServiceImpl globalDatabaseServiceImpl;

    private final DBMigrationExecutorService migrationService;

    /**
     * Initializes a new {@link GenericReloadable}.
     *
     * @param globalDatabaseService
     * @param migrationService
     */
    public GlobalDbConfigsReloadable(GlobalDatabaseServiceImpl globalDatabaseServiceImpl, DBMigrationExecutorService migrationService) {
        super();
        this.globalDatabaseServiceImpl = globalDatabaseServiceImpl;
        this.migrationService = migrationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            Map<String, GlobalDbConfig> loadGlobalDbConfigs = this.globalDatabaseServiceImpl.loadGlobalDbConfigs(configService);

            List<DBMigrationState> scheduleMigrations = this.globalDatabaseServiceImpl.scheduleMigrations(migrationService, loadGlobalDbConfigs);
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
            this.globalDatabaseServiceImpl.setGlobalDbConfigs(loadGlobalDbConfigs);
        } catch (OXException e) {
            LOG.error("Unable to reload global database configuration from globaldb.yml. Please review your changes and reload again!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String[]> getConfigFileNames() {
        Map<String, String[]> map = new HashMap<String, String[]>(1);
        map.put(GlobalDbInit.CONFIGFILE, PROPERTIES);
        return map;
    }
}
