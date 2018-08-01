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
import com.openexchange.database.GeneralDatabaseConnectionListener;
import com.openexchange.database.internal.reloadable.ConnectionReloader;
import com.openexchange.database.internal.reloadable.GlobalDbConfigsReloadable;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.exception.OXException;
import com.openexchange.lock.LockService;
import com.openexchange.osgi.ServiceListing;

/**
 * Contains the code to startup the complete database connection pooling and replication monitor.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Initialization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Initialization.class);

    private static final Initialization SINGLETON = new Initialization();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static final Initialization getInstance() {
        return SINGLETON;
    }

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

    private static final AtomicReference<LockService> LOCKSERV_REF = new AtomicReference<LockService>();

    /**
     * Sets the lock service.
     *
     * @param configurationService The lock service
     */
    public static void setLockService(final LockService configurationService) {
        LOCKSERV_REF.set(configurationService);
    }

    /**
     * Gets the lock service.
     *
     * @return The lock service or <code>null</code> if absent
     */
    public static LockService getLockService() {
        return LOCKSERV_REF.get();
    }

    // -------------------------------------------------------------------------------------------------------------- //

    private final Timer timer = new Timer();
    private final Management management = new Management(timer);

    private Configuration configuration = null;
    private CacheService cacheService;
    private ReplicationMonitor monitor;
    private Pools pools;
    private ConfigDatabaseServiceImpl configDatabaseService;
    private DatabaseServiceImpl databaseService;
    private GlobalDatabaseServiceImpl globalDatabaseService;

    private Initialization() {
        super();
    }

    public synchronized boolean isStarted() {
        return null != databaseService;
    }

    /**
     * Initializes the database service.
     *
     * @param configurationService A reference to the configuration service
     * @param configViewFactory The config view factory
     * @param migrationService The database migration service, or <code>null</code> if not available
     * @param connectionListeners The connection listeners
     * @param configuration The {@link Configuration}
     * @param reloader {@link ConnectionReloader} to reload connection pools 
     * @return The database service
     * @throws OXException Various
     */
    public synchronized DatabaseServiceImpl start(ConfigurationService configurationService, ConfigViewFactory configViewFactory, DBMigrationExecutorService migrationService, ServiceListing<GeneralDatabaseConnectionListener> connectionListeners, Configuration configuration, ConnectionReloaderImpl reloader) throws OXException {
        this.configuration = configuration;
        if (null != databaseService) {
            throw DBPoolingExceptionCodes.ALREADY_INITIALIZED.create(Initialization.class.getName());
        }
        // Set timer interval
        timer.configure(configuration);
        // Setting up database connection pools.
        pools = new Pools(management, timer);
        // Setting up the replication monitor
        monitor = new ReplicationMonitor(configuration.getBoolean(REPLICATION_MONITOR, true), configuration.getBoolean(CHECK_WRITE_CONS, false), connectionListeners);
        management.addOverview(new Overview(pools, monitor));
        // Add life cycle for configuration database
        final ConfigDatabaseLifeCycle configDBLifeCycle = new ConfigDatabaseLifeCycle(configuration, management, timer);
        pools.addLifeCycle(configDBLifeCycle);
        reloader.setConfigurationListener(configDBLifeCycle);
        // Configuration database connection pool service.
        configDatabaseService = new ConfigDatabaseServiceImpl(new ConfigDatabaseAssignmentImpl(), pools, monitor, LockMech.lockMechFor(configuration.getProperty(Configuration.Property.LOCK_MECH, LockMech.ROW_LOCK.getId())));
        if (null != cacheService) {
            configDatabaseService.setCacheService(cacheService);
        }
        // Context pool life cycle.
        ContextDatabaseLifeCycle contextLifeCycle = new ContextDatabaseLifeCycle(configuration, management, timer, configDatabaseService);
        pools.addLifeCycle(contextLifeCycle);
        reloader.setConfigurationListener(contextLifeCycle);
        reloader.setConfigurationListener(pools);
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

    public synchronized void stop() {
        databaseService = null;
        configDatabaseService.removeCacheService();
        configDatabaseService = null;
        globalDatabaseService = null;
        pools.stop(timer);
        pools = null;
        configuration.clear();
    }

    public synchronized void setCacheService(final CacheService service) {
        this.cacheService = service;
        if (null != configDatabaseService) {
            configDatabaseService.setCacheService(service);
        }
    }

    public synchronized void removeCacheService() {
        this.cacheService = null;
        if (null != configDatabaseService) {
            configDatabaseService.removeCacheService();
        }
    }

    public synchronized Management getManagement() {
        return management;
    }

    public synchronized Timer getTimer() {
        return timer;
    }

}
