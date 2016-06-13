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
import java.util.Map;
import org.osgi.framework.FrameworkUtil;
import com.openexchange.caching.CacheService;
import com.openexchange.database.Assignment;
import com.openexchange.database.ConfigDatabaseService;
import com.openexchange.database.DBPoolingExceptionCodes;
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

    ConfigDatabaseServiceImpl(ConfigDatabaseAssignmentService assignmentService, Pools pools, ReplicationMonitor monitor) {
        super();
        this.assignmentService = assignmentService;
        contextAssignment = new ContextDatabaseAssignmentImpl(this);
        this.pools = pools;
        this.monitor = monitor;
    }

    /**
     * Schedules pending migrations for the config database.
     *
     * @param migrationService The database migration service
     * @return The scheduled migration
     */
    public DBMigrationState scheduleMigrations(DBMigrationExecutorService migrationService) throws OXException {
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
                ConfigDatabaseServiceImpl.back(connection);
            }
        };
        /*
         * register utility MBean and schedule migration
         */
        String schema = null != assignment.getSchema() ? assignment.getSchema() : "configdb";
        DBMigration migration = new DBMigration(connectionProvider, CONFIGDB_CHANGE_LOG, localResourceAccessor, schema);
        migrationService.registerMBean(migration);
        return migrationService.scheduleDBMigration(migration);
    }

    private Connection get(final boolean write) throws OXException {
        return get(write, false);
    }

    private Connection get(final boolean write, final boolean noTimeout) throws OXException {
        final AssignmentImpl assign = assignmentService.getConfigDBAssignment();
        return monitor.checkFallback(pools, assign, noTimeout, write);
        // TODO Enable the following if the configuration database gets a table replicationMonitor.
        // return ReplicationMonitor.checkActualAndFallback(pools, assign, false, write);
    }

    private static void back(final Connection con) {
        back(con, false);
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
        back(con);
    }

    @Override
    public void backWritable(final Connection con) {
        back(con);
    }

    @Override
    public void backForUpdateTask(Connection con) {
        back(con);
    }

    @Override
    public void backForUpdateTaskAfterReading(Connection con) {
        back(con, true);
    }

    @Override
    public int[] listContexts(final int poolId) throws OXException {
        return contextAssignment.getContextsInDatabase(poolId);
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
        final Assignment assign = contextAssignment.getAssignment(contextId);
        return assign.getWritePoolId();
    }

    @Override
    public String getSchemaName(int contextId) throws OXException {
        return contextAssignment.getAssignment(contextId).getSchema();
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
}
