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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * Contains the code to startup the complete database connection pooling and replication monitor.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Initialization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Initialization.class);
    private static final Initialization SINGLETON = new Initialization();

    private final Management management = new Management();
    private final Timer timer = new Timer();
    private final Configuration configuration = new Configuration();

    private CacheService cacheService;
    private ReplicationMonitor monitor;
    private Pools pools;
    private ContextDatabaseAssignmentImpl contextAssignment;
    private ConfigDatabaseServiceImpl configDatabaseService;
    private DatabaseServiceImpl databaseService;

    private Initialization() {
        super();
    }

    public static final Initialization getInstance() {
        return SINGLETON;
    }

    public boolean isStarted() {
        return null != databaseService;
    }

    public DatabaseService start(final ConfigurationService configurationService) throws OXException {
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
        // Context database assignments.
        contextAssignment = new ContextDatabaseAssignmentImpl(configDatabaseService);
        if (null != cacheService) {
            contextAssignment.setCacheService(cacheService);
        }
        // Context pool life cycle.
        final ContextDatabaseLifeCycle contextDBLifeCycle = new ContextDatabaseLifeCycle(
            configuration,
            management,
            timer,
            configDatabaseService);
        pools.addLifeCycle(contextDBLifeCycle);
        Server.setConfigDatabaseService(configDatabaseService);
        Server.start(configurationService);
        try {
            LOG.info("Resolved server name \"" + Server.getServerName() + "\" to identifier " + Server.getServerId());
        } catch (OXException e) {
            LOG.warn("Resolving server name to an identifier failed. This is normal until a server has been registered.", e);
        }
        databaseService = new DatabaseServiceImpl(pools, configDatabaseService, contextAssignment, monitor);
        return databaseService;
    }

    public void stop() {
        databaseService = null;
        contextAssignment.removeCacheService();
        contextAssignment = null;
        configDatabaseService = null;
        pools.stop(timer);
        pools = null;
        configuration.clear();
    }

    public void setCacheService(final CacheService service) {
        this.cacheService = service;
        if (null != contextAssignment) {
            contextAssignment.setCacheService(service);
        }
    }

    public void removeCacheService() {
        this.cacheService = null;
        if (null != contextAssignment) {
            contextAssignment.removeCacheService();
        }
    }

    public Management getManagement() {
        return management;
    }

    public Timer getTimer() {
        return timer;
    }
}
