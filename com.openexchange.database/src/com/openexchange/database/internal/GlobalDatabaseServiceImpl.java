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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import liquibase.changelog.ChangeSet;
import org.osgi.framework.FrameworkUtil;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.GlobalDatabaseService;
import com.openexchange.database.internal.wrapping.JDBC4ConnectionReturner;
import com.openexchange.database.migration.DBMigration;
import com.openexchange.database.migration.DBMigrationCallback;
import com.openexchange.database.migration.DBMigrationConnectionProvider;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.database.migration.DBMigrationState;
import com.openexchange.database.migration.resource.accessor.BundleResourceAccessor;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link GlobalDatabaseServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GlobalDatabaseServiceImpl implements GlobalDatabaseService {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GlobalDatabaseServiceImpl.class);
    private static final String GLOBALDB_CHANGE_LOG = "/liquibase/globaldbChangeLog.xml";

    final Pools pools;
    final ReplicationMonitor monitor;
    private Map<String, GlobalDbConfig> globalDbConfigs;
    private final ConfigViewFactory configViewFactory;
    private final ConfigDatabaseServiceImpl configDatabaseService;

    /**
     * Initializes a new {@link GlobalDatabaseServiceImpl}.
     *
     * @param pools A reference to the connection pool
     * @param monitor The replication monitor
     * @param configDatabaseService
     * @param configurationService
     * @param globalDbConfigs The known global database configurations
     * @param configViewFactory The config view factory
     * @throws OXException
     */
    public GlobalDatabaseServiceImpl(Pools pools, ReplicationMonitor monitor, ConfigurationService configurationService, ConfigDatabaseServiceImpl configDatabaseService, ConfigViewFactory configViewFactory) throws OXException {
        super();
        this.pools = pools;
        this.monitor = monitor;
        this.configViewFactory = configViewFactory;
        this.configDatabaseService = configDatabaseService;

        Map<String, GlobalDbConfig> loadGlobalDbConfigs = this.loadGlobalDbConfigs(configurationService);
        this.setGlobalDbConfigs(loadGlobalDbConfigs);
    }

    /**
     * Returns the up to date configuration for global databases from the globaldb.yml file. To apply the returned configuration you have to call com.openexchange.database.internal.GlobalDatabaseServiceImpl.setGlobalDbConfigs(Map<String, GlobalDbConfig>)
     *
     * @param configurationService
     * @throws OXException
     */
    public Map<String, GlobalDbConfig> loadGlobalDbConfigs(ConfigurationService configurationService) throws OXException {
        Map<String, GlobalDbConfig> lGlobalDbConfigs = new ConcurrentHashMap<String, GlobalDbConfig>();

        if ((this.globalDbConfigs != null) && (!this.globalDbConfigs.isEmpty())) {
            lGlobalDbConfigs.putAll(this.globalDbConfigs);
        }

        Map<String, GlobalDbConfig> newGlobalDbConfigs = GlobalDbInit.init(configurationService, configDatabaseService, pools, monitor);
        lGlobalDbConfigs.putAll(newGlobalDbConfigs);

        for (String filename : new ArrayList<String>(lGlobalDbConfigs.keySet())) {
            if (!newGlobalDbConfigs.containsKey(filename)) {
                lGlobalDbConfigs.remove(filename);
            }
        }

        return lGlobalDbConfigs;
    }

    /**
     * Sets the globalDbConfigs
     *
     * @param globalDbConfigs The globalDbConfigs to set
     */
    public void setGlobalDbConfigs(Map<String, GlobalDbConfig> globalDbConfigs) {
        this.globalDbConfigs = globalDbConfigs;
    }

    /**
     * Schedules pending migrations for all known global databases.
     *
     * @param migrationService The database migration service
     * @return The scheduled migrations
     */
    public List<DBMigrationState> scheduleMigrations(DBMigrationExecutorService migrationService) throws OXException {
        return this.scheduleMigrations(migrationService, globalDbConfigs);
    }

    /**
     * Schedules pending migrations for all global databases provided within the {@link Map<String, GlobalDbConfig>} parameter.
     *
     * @param migrationService The database migration service
     * @param newGlobalDbConfigs The configuration to schedule migrations for
     *
     * @return The scheduled migrations
     */
    public List<DBMigrationState> scheduleMigrations(DBMigrationExecutorService migrationService, Map<String, GlobalDbConfig> newGlobalDbConfigs) throws OXException {
        if (null == newGlobalDbConfigs || 0 == newGlobalDbConfigs.size()) {
            return Collections.emptyList();
        }
        /*
         * use appropriate connection provider per global database & a local resource accessor for the changeset file
         */
        BundleResourceAccessor localResourceAccessor = new BundleResourceAccessor(FrameworkUtil.getBundle(GlobalDbInit.class));
        Set<GlobalDbConfig> dbConfigs = new HashSet<GlobalDbConfig>(newGlobalDbConfigs.values());
        List<DBMigrationState> migrationStates = new ArrayList<DBMigrationState>(dbConfigs.size());
        for (GlobalDbConfig dbConfig : dbConfigs) {
            /*
             * use a special assignment override that pretends a connection to the config database to prevent accessing a not yet existing
             * replication monitor
             */
            final AssignmentImpl assignment = dbConfig.getAssignment();
            final AssignmentImpl firstAssignment = new AssignmentImpl(assignment);
            DBMigrationConnectionProvider connectionProvider = new DBMigrationConnectionProvider() {

                @Override
                public Connection get() throws OXException {
                    return GlobalDatabaseServiceImpl.this.get(firstAssignment, true, true);
                }

                @Override
                public void back(Connection connection) {
                    GlobalDatabaseServiceImpl.this.back(connection, false);
                }

                @Override
                public void backAfterReading(Connection connection) {
                    GlobalDatabaseServiceImpl.this.back(connection, true);
                }
            };
            /*
             * use a migration callback that fetches & returns a writable connection to trigger the replication monitor once after
             * migrations were executed
             */
            DBMigrationCallback migrationCallback = new DBMigrationCallback() {

                @Override
                public void onMigrationFinished(List<ChangeSet> executed, List<ChangeSet> rolledBack) {
                    if (null != executed && 0 < executed.size() || null != rolledBack && 0 < rolledBack.size()) {
                        Connection connection = null;
                        try {
                            connection = monitor.checkActualAndFallback(pools, assignment, true, true);
                        } catch (OXException e) {
                            LOG.warn("Unexpected error during migration callback", e);
                        } finally {
                            ConnectionState connectionState = new ConnectionState(false);
                            connectionState.setUsedForUpdate(true);
                            monitor.backAndIncrementTransaction(pools, assignment, connection, true, true, connectionState);
                        }
                    }
                }
            };
            /*
             * register utility MBean and schedule migration
             */
            DBMigration migration = new DBMigration(connectionProvider, GLOBALDB_CHANGE_LOG, localResourceAccessor, assignment.getSchema());
            migrationService.registerMBean(migration);
            migrationStates.add(migrationService.scheduleDBMigration(migration, migrationCallback));
        }
        return migrationStates;
    }

    @Override
    public boolean isGlobalDatabaseAvailable(String group) {
        String name = Strings.isEmpty(group) ? GlobalDbConfig.DEFAULT_GROUP : group;
        return globalDbConfigs.containsKey(name);
    }

    @Override
    public boolean isGlobalDatabaseAvailable(int contextId) throws OXException {
        String group = configViewFactory.getView(-1, contextId).opt("com.openexchange.context.group", String.class, null);
        return isGlobalDatabaseAvailable(group);
    }

    @Override
    public Connection getReadOnlyForGlobal(String group) throws OXException {
        return get(getAssignment(group), false, false);
    }

    @Override
    public Connection getReadOnlyForGlobal(int contextId) throws OXException {
        return get(getAssignment(contextId), false, false);
    }

    @Override
    public void backReadOnlyForGlobal(String group, Connection connection) {
        back(connection, true);
    }

    @Override
    public void backReadOnlyForGlobal(int contextId, Connection connection) {
        back(connection, true);
    }

    @Override
    public Connection getWritableForGlobal(String group) throws OXException {
        return get(getAssignment(group), true, false);
    }

    @Override
    public Connection getWritableForGlobal(int contextId) throws OXException {
        return get(getAssignment(contextId), true, false);
    }

    @Override
    public void backWritableForGlobal(String group, Connection connection) {
        back(connection, false);
    }

    @Override
    public void backWritableForGlobal(int contextId, Connection connection) {
        back(connection, false);
    }

    private AssignmentImpl getAssignment(String group) throws OXException {
        String name = Strings.isEmpty(group) ? GlobalDbConfig.DEFAULT_GROUP : group;
        GlobalDbConfig dbConfig = globalDbConfigs.get(name);
        if (null == dbConfig) {
            // TODO: fall back to "default" also in that case?
            throw DBPoolingExceptionCodes.NO_GLOBALDB_CONFIG_FOR_GROUP.create(group);
        }
        return dbConfig.getAssignment();
    }

    private AssignmentImpl getAssignment(int contextId) throws OXException {
        String group = configViewFactory.getView(-1, contextId).opt("com.openexchange.context.group", String.class, null);
        return getAssignment(group);
    }

    Connection get(AssignmentImpl assignment, boolean write, boolean noTimeout) throws OXException {
        return monitor.checkActualAndFallback(pools, assignment, noTimeout, write);
    }

    void back(Connection connection, boolean usedAsRead) {
        if (null == connection) {
            LOG.error("", DBPoolingExceptionCodes.NULL_CONNECTION.create());
            return;
        }
        try {
            if (usedAsRead && (connection instanceof JDBC4ConnectionReturner)) {
                // Not the nice way to tell the replication monitor not to increment the counter.
                ((JDBC4ConnectionReturner) connection).setUsedAsRead(true);
            }
            connection.close();
        } catch (SQLException e) {
            LOG.error("", DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage()));
        }
    }

}
