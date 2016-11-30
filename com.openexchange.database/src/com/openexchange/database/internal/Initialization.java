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

package com.openexchange.database.internal;

import static com.openexchange.database.internal.Configuration.Property.CHECK_WRITE_CONS;
import static com.openexchange.database.internal.Configuration.Property.REPLICATION_MONITOR;
import static com.openexchange.java.Autoboxing.I;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.internal.reloadable.GlobalDbConfigsReloadable;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.exception.OXException;

/**
 * Contains the code to startup the complete database connection pooling and replication monitor.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Initialization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Initialization.class);
    private static final Initialization SINGLETON = new Initialization();

    private static final AtomicReference<ConfigurationService> CONF_REF = new AtomicReference<ConfigurationService>();

    /**
     * Sets the configuration service.
     *
     * @param configurationService The configuration service
     */
    public static void setConfigurationService(final ConfigurationService configurationService) {
        CONF_REF.set(configurationService);
    }

    /**
     * Gets the configuration service.
     *
     * @return The configuration service or <code>null</code> if absent
     */
    public static ConfigurationService getConfigurationService() {
        return CONF_REF.get();
    }

    // -------------------------------------------------------------------------------------------------------------- //

    private final Timer timer = new Timer();
    private final Management management = new Management(timer);
    private final Configuration configuration = new Configuration();

    private CacheService cacheService;
    private ReplicationMonitor monitor;
    private Pools pools;
    private ConfigDatabaseServiceImpl configDatabaseService;
    private DatabaseServiceImpl databaseService;
    private GlobalDatabaseServiceImpl globalDatabaseService;

    private Initialization() {
        super();
    }

    public static final Initialization getInstance() {
        return SINGLETON;
    }

    public boolean isStarted() {
        return null != databaseService;
    }

    /**
     * Initializes the database service.
     *
     * @param configurationService A reference to the configuration service
     * @param configViewFactory The config view factory
     * @param migrationService The database migration service, or <code>null</code> if not available
     * @return The database service
     */
    public DatabaseServiceImpl start(ConfigurationService configurationService, ConfigViewFactory configViewFactory, DBMigrationExecutorService migrationService) throws OXException {
        if (null != databaseService) {
            throw DBPoolingExceptionCodes.ALREADY_INITIALIZED.create(Initialization.class.getName());
        }
        // Parse configuration
        configuration.readConfiguration(configurationService);
        // Set timer interval
        timer.configure(configuration);
        // Setting up database connection pools.
        pools = new Pools(timer);
        // Setting up the replication monitor
        monitor = new ReplicationMonitor(configuration.getBoolean(REPLICATION_MONITOR, true), configuration.getBoolean(CHECK_WRITE_CONS, false));
        management.addOverview(new Overview(pools, monitor));
        // Add life cycle for configuration database
        final ConfigDatabaseLifeCycle configDBLifeCycle = new ConfigDatabaseLifeCycle(configuration, management, timer);
        pools.addLifeCycle(configDBLifeCycle);
        // Configuration database connection pool service.
        configDatabaseService = new ConfigDatabaseServiceImpl(new ConfigDatabaseAssignmentImpl(), pools, monitor);
        if (null != cacheService) {
            configDatabaseService.setCacheService(cacheService);
        }
        // Context pool life cycle.
        pools.addLifeCycle(new ContextDatabaseLifeCycle(configuration, management, timer, configDatabaseService));
        Server.setConfigDatabaseService(configDatabaseService);
        Server.start(configurationService);
        try {
            LOG.info("Resolved server name \"{}\" to identifier {}", Server.getServerName(), I(Server.getServerId()));
        } catch (OXException e) {
            LOG.warn("Resolving server name to an identifier failed. This is normal until a server has been registered.", e);
        }
        // Global database service
        globalDatabaseService = new GlobalDatabaseServiceImpl(pools, monitor, configurationService, configDatabaseService, configViewFactory);
        GlobalDbConfigsReloadable.setGlobalDatabaseServiceRef(globalDatabaseService);
        GlobalDbConfigsReloadable.setGlobalDatabaseServiceRef(migrationService);
        // Schedule pending migrations
        if (null != migrationService) {
            configDatabaseService.scheduleMigrations(migrationService);
            globalDatabaseService.scheduleMigrations(migrationService);
        }
        databaseService = new DatabaseServiceImpl(pools, configDatabaseService, globalDatabaseService, monitor);
        return databaseService;
    }

    public void stop() {
        databaseService = null;
        configDatabaseService.removeCacheService();
        configDatabaseService = null;
        globalDatabaseService = null;
        pools.stop(timer);
        pools = null;
        configuration.clear();
    }

    public void setCacheService(final CacheService service) {
        this.cacheService = service;
        if (null != configDatabaseService) {
            configDatabaseService.setCacheService(service);
        }
    }

    public void removeCacheService() {
        this.cacheService = null;
        if (null != configDatabaseService) {
            configDatabaseService.removeCacheService();
        }
    }

    public Management getManagement() {
        return management;
    }

    public Timer getTimer() {
        return timer;
    }

}
