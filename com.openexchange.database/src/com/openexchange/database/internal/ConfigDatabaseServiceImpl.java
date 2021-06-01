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

package com.openexchange.database.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import org.osgi.framework.FrameworkUtil;
import com.openexchange.caching.CacheService;
import com.openexchange.database.Assignment;
import com.openexchange.database.ConfigDatabaseService;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.SchemaInfo;
import com.openexchange.database.internal.wrapping.JDBC4ConnectionReturner;
import com.openexchange.database.migration.DBMigration;
import com.openexchange.database.migration.DBMigrationConnectionProvider;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.database.migration.DBMigrationState;
import com.openexchange.database.migration.resource.accessor.BundleResourceAccessor;
import com.openexchange.exception.OXException;

/**
 * Implements the database service to the config database.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ConfigDatabaseServiceImpl implements ConfigDatabaseService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigDatabaseServiceImpl.class);
    private static final String CONFIGDB_CHANGE_LOG = "/liquibase/configdbChangeLog.xml";

    // ------------------------------------------------------------------------------------------------ //

    private final Pools pools;
    private final ConfigDatabaseAssignmentService assignmentService;
    private final ContextDatabaseAssignmentImpl contextAssignment;
    private final ReplicationMonitor monitor;

    ConfigDatabaseServiceImpl(ConfigDatabaseAssignmentService assignmentService, Pools pools, ReplicationMonitor monitor, LockMech lockMech) {
        super();
        this.assignmentService = assignmentService;
        contextAssignment = new ContextDatabaseAssignmentImpl(this, lockMech);
        this.pools = pools;
        this.monitor = monitor;
    }

    /**
     * Schedules pending migrations for the config database.
     *
     * @param migrationService The database migration service
     * @return The scheduled migration
     */
    public DBMigrationState scheduleMigrations(DBMigrationExecutorService migrationService) {
        /*
         * use appropriate connection provider fro config database & a local resource accessor for the changeset file
         */
        BundleResourceAccessor localResourceAccessor = new BundleResourceAccessor(FrameworkUtil.getBundle(ConfigDatabaseServiceImpl.class));
        AssignmentImpl assignment = assignmentService.getConfigDBAssignment();
        DBMigrationConnectionProvider connectionProvider = new DBMigrationConnectionProvider() {

            @Override
            public Connection get() throws OXException {
                return ConfigDatabaseServiceImpl.this.getForUpdateTask();
            }

            @Override
            public void back(Connection connection) {
                ConfigDatabaseServiceImpl.back(connection, false);
            }

            @Override
            public void backAfterReading(Connection connection) {
                ConfigDatabaseServiceImpl.back(connection, true);
            }
        };
        /*
         * register utility MBean and schedule migration
         */
        String schema = null != assignment.getSchema() ? assignment.getSchema() : "configdb";
        DBMigration migration = new DBMigration(connectionProvider, CONFIGDB_CHANGE_LOG, localResourceAccessor, schema);
        migrationService.register(migration);
        return migrationService.scheduleDBMigration(migration);
    }

    private Connection get(final boolean write) throws OXException {
        return get(write, false);
    }

    private Connection get(final boolean write, final boolean noTimeout) throws OXException {
        final AssignmentImpl assign = assignmentService.getConfigDBAssignment();
        return monitor.checkActualAndFallback(pools, assign, noTimeout, write);
    }

    static void back(Connection con, boolean usedAsRead) {
        if (null == con) {
            final OXException e = DBPoolingExceptionCodes.NULL_CONNECTION.create();
            LOG.error("", e);
            return;
        }
        try {
            if (usedAsRead && (con instanceof JDBC4ConnectionReturner)) {
                // Not the nice way to tell the replication monitor not to increment the counter.
                ((JDBC4ConnectionReturner) con).setUsedAsRead(true);
            }
            con.close();
        } catch (SQLException e) {
            OXException e1 = DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            LOG.error("", e1);
        }
    }

    void setCacheService(CacheService service) {
        contextAssignment.setCacheService(service);
    }

    void removeCacheService() {
        contextAssignment.removeCacheService();
    }

    AssignmentImpl getAssignment(int contextId) throws OXException {
        return contextAssignment.getAssignment(contextId);
    }

    @Override
    public Connection getReadOnly() throws OXException {
        return get(false);
    }

    @Override
    public Connection getWritable() throws OXException {
        return get(true);
    }

    @Override
    public Connection getForUpdateTask() throws OXException {
        return get(true, true);
    }

    @Override
    public void backReadOnly(final Connection con) {
        back(con, true);
    }

    @Override
    public void backWritable(final Connection con) {
        back(con, false);
    }

    @Override
    public void backWritableAfterReading(final Connection con) {
        back(con, true);
    }

    @Override
    public void backForUpdateTask(Connection con) {
        back(con, false);
    }

    @Override
    public void backForUpdateTaskAfterReading(Connection con) {
        back(con, true);
    }

    @Override
    public int[] listContexts(int poolId, int offset, int length) throws OXException {
        return contextAssignment.getContextsInDatabase(poolId, offset, length);
    }

    @Override
    public int getServerId() throws OXException {
        return Server.getServerId();
    }

    @Override
    public String getServerName() throws OXException {
        return Server.getServerName();
    }

    @Override
    public int getWritablePool(int contextId) throws OXException {
        return contextAssignment.getAssignment(contextId).getWritePoolId();
    }

    @Override
    public String getSchemaName(int contextId) throws OXException {
        return contextAssignment.getAssignment(contextId).getSchema();
    }

    @Override
    public SchemaInfo getSchemaInfo(int contextId) throws OXException {
        Assignment assign = contextAssignment.getAssignment(contextId);
        return SchemaInfo.valueOf(assign.getWritePoolId(), assign.getSchema());
    }

    @Override
    public int[] getContextsInSameSchema(int contextId) throws OXException {
        final Assignment assign = contextAssignment.getAssignment(contextId);
        final Connection con = getReadOnly();
        try {
            return contextAssignment.getContextsFromSchema(con, assign.getWritePoolId(), assign.getSchema());
        } finally {
            backReadOnly(con);
        }
    }

    @Override
    public int[] getContextsInSameSchema(Connection con, int contextId) throws OXException {
        final Assignment assign = contextAssignment.getAssignment(contextId);
        return contextAssignment.getContextsFromSchema(con, assign.getWritePoolId(), assign.getSchema());
    }

    @Override
    public int[] getContextsInSchema(Connection con, int poolId, String schema) throws OXException {
        return contextAssignment.getContextsFromSchema(con, poolId, schema);
    }

    @Override
    public String[] getUnfilledSchemas(Connection con, int poolId, int maxContexts) throws OXException {
        return contextAssignment.getUnfilledSchemas(con, poolId, maxContexts);
    }

    @Override
    public Map<String, Integer> getContextCountPerSchema(Connection con, int poolId, int maxContexts) throws OXException {
        return contextAssignment.getContextCountPerSchema(con, poolId, maxContexts);
    }

    @Override
    public void invalidate(final int... contextIds) {
        contextAssignment.invalidateAssignment(contextIds);
    }

    @Override
    public void writeAssignment(Connection con, Assignment assignment) throws OXException {
        contextAssignment.writeAssignment(con, assignment);
    }

    @Override
    public void deleteAssignment(Connection con, int contextId) throws OXException {
        contextAssignment.deleteAssignment(con, contextId);
    }

    @Override
    public void lock(Connection con, int writePoolId) throws OXException {
        contextAssignment.lock(con, writePoolId);
    }

    @Override
    public Map<String, Integer> getAllSchemata(Connection con) throws OXException {
        return contextAssignment.getAllSchemata(con);
    }
}
