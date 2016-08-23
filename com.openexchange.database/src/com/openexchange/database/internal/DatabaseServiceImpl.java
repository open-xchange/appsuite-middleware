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

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import com.openexchange.database.Assignment;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.internal.wrapping.JDBC4ConnectionReturner;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.pooling.PoolingException;

/**
 * Interface class for accessing the database system.
 * TODO test threads.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class DatabaseServiceImpl implements DatabaseService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DatabaseServiceImpl.class);

    private final Pools pools;
    private final ConfigDatabaseServiceImpl configDatabaseService;
    private final GlobalDatabaseServiceImpl globalDatabaseService;
    private final ReplicationMonitor monitor;

    public DatabaseServiceImpl(Pools pools, ConfigDatabaseServiceImpl configDatabaseService, GlobalDatabaseServiceImpl globalDatabaseService, ReplicationMonitor monitor) {
        super();
        this.pools = pools;
        this.configDatabaseService = configDatabaseService;
        this.globalDatabaseService = globalDatabaseService;
        this.monitor = monitor;
    }

    private Connection get(final int contextId, final boolean write, final boolean noTimeout) throws OXException {
        final AssignmentImpl assign = configDatabaseService.getAssignment(contextId);
        return get(assign, write, noTimeout);
    }

    private Connection get(final AssignmentImpl assign, final boolean write, final boolean noTimeout) throws OXException {
        return monitor.checkActualAndFallback(pools, assign, noTimeout, write);
    }

    private static void back(final Connection con) {
        if (null == con) {
            final OXException e = DBPoolingExceptionCodes.NULL_CONNECTION.create();
            LOG.error("", e);
            return;
        }
        try {
            con.close();
        } catch (final SQLException e) {
            final OXException e1 = DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            LOG.error("", e1);
        }
    }

    private static void backFromReading(Connection con) {
        if (null == con) {
            final OXException e = DBPoolingExceptionCodes.NULL_CONNECTION.create();
            LOG.error("", e);
            return;
        }
        try {
            if (con instanceof JDBC4ConnectionReturner) {
                // Not the nice way to tell the replication monitor not to increment the counter.
                ((JDBC4ConnectionReturner) con).setUsedAsRead(true);
            }
            con.close();
        } catch (final SQLException e) {
            final OXException e1 = DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            LOG.error("", e1);
        }
    }

    // Delegate config database service methods.

    /**
     * Gets the assignment for specified context identifier
     *
     * @param contextId The context identifier
     * @return The associated assignment
     * @throws OXException If such an assignment cannot be returned
     */
    public AssignmentImpl getAssignment(int contextId) throws OXException {
        return configDatabaseService.getAssignment(contextId);
    }

    @Override
    public Connection getReadOnly() throws OXException {
        return configDatabaseService.getReadOnly();
    }

    @Override
    public Connection getWritable() throws OXException {
        return configDatabaseService.getWritable();
    }

    @Override
    public Connection getForUpdateTask() throws OXException {
        return configDatabaseService.getForUpdateTask();
    }

    @Override
    public void backReadOnly(final Connection con) {
        configDatabaseService.backReadOnly(con);
    }

    @Override
    public void backWritable(final Connection con) {
        configDatabaseService.backWritable(con);
    }

    @Override
    public void backWritableAfterReading(Connection con) {
        configDatabaseService.backWritableAfterReading(con);
    }

    @Override
    public void backForUpdateTask(Connection con) {
        configDatabaseService.backForUpdateTask(con);
    }

    @Override
    public void backForUpdateTaskAfterReading(Connection con) {
        configDatabaseService.backForUpdateTaskAfterReading(con);
    }

    @Override
    public int[] listContexts(final int poolId) throws OXException {
        return configDatabaseService.listContexts(poolId);
    }

    @Override
    public int getServerId() throws OXException {
        return configDatabaseService.getServerId();
    }

    @Override
    public String getServerName() throws OXException {
        return configDatabaseService.getServerName();
    }

    @Override
    public int getWritablePool(int contextId) throws OXException {
        return configDatabaseService.getWritablePool(contextId);
    }

    @Override
    public String getSchemaName(int contextId) throws OXException {
        return configDatabaseService.getSchemaName(contextId);
    }

    @Override
    public int[] getContextsInSameSchema(int contextId) throws OXException {
        return configDatabaseService.getContextsInSameSchema(contextId);
    }

    @Override
    public int[] getContextsInSameSchema(Connection con, int contextId) throws OXException {
        return configDatabaseService.getContextsInSameSchema(con, contextId);
    }

    @Override
    public int[] getContextsInSchema(Connection con, int poolId, String schema) throws OXException {
        return configDatabaseService.getContextsInSchema(con, poolId, schema);
    }

    @Override
    public String[] getUnfilledSchemas(Connection con, int poolId, int maxContexts) throws OXException {
        return configDatabaseService.getUnfilledSchemas(con, poolId, maxContexts);
    }

    @Override
    public Map<String, Integer> getContextCountPerSchema(Connection con, int poolId, int maxContexts) throws OXException {
        return configDatabaseService.getContextCountPerSchema(con, poolId, maxContexts);
    }

    @Override
    public void invalidate(int... contextIds) {
        configDatabaseService.invalidate(contextIds);
    }

    @Override
    public void writeAssignment(Connection con, Assignment assignment) throws OXException {
        configDatabaseService.writeAssignment(con, assignment);
    }

    @Override
    public void deleteAssignment(Connection con, int contextId) throws OXException {
        configDatabaseService.deleteAssignment(con, contextId);
    }

    @Override
    public void lock(Connection con, int writePoolId) throws OXException {
        configDatabaseService.lock(con, writePoolId);
    }

    // Delegate global database service methods.

    @Override
    public boolean isGlobalDatabaseAvailable(String group) throws OXException {
        return globalDatabaseService.isGlobalDatabaseAvailable(group);
    }

    @Override
    public boolean isGlobalDatabaseAvailable(int contextId) throws OXException {
        return globalDatabaseService.isGlobalDatabaseAvailable(contextId);
    }

    @Override
    public Connection getReadOnlyForGlobal(String group) throws OXException {
        return globalDatabaseService.getReadOnlyForGlobal(group);
    }

    @Override
    public Connection getReadOnlyForGlobal(int contextId) throws OXException {
        return globalDatabaseService.getReadOnlyForGlobal(contextId);
    }

    @Override
    public void backReadOnlyForGlobal(String group, Connection connection) {
        globalDatabaseService.backReadOnlyForGlobal(group, connection);
    }

    @Override
    public void backReadOnlyForGlobal(int contextId, Connection connection) {
        globalDatabaseService.backReadOnlyForGlobal(contextId, connection);
    }

    @Override
    public Connection getWritableForGlobal(String group) throws OXException {
        return globalDatabaseService.getWritableForGlobal(group);
    }

    @Override
    public Connection getWritableForGlobal(int contextId) throws OXException {
        return globalDatabaseService.getWritableForGlobal(contextId);
    }

    @Override
    public void backWritableForGlobal(String group, Connection connection) {
        globalDatabaseService.backWritableForGlobal(group, connection);
    }

    @Override
    public void backWritableForGlobal(int contextId, Connection connection) {
        globalDatabaseService.backWritableForGlobal(contextId, connection);
    }

    // Implemented database service methods.

    @Override
    public Connection getReadOnly(final Context ctx) throws OXException {
        return get(ctx.getContextId(), false, false);
    }

    @Override
    public Connection getReadOnly(final int contextId) throws OXException {
        return get(contextId, false, false);
    }

    @Override
    public Connection getWritable(final Context ctx) throws OXException {
        return get(ctx.getContextId(), true, false);
    }

    @Override
    public Connection getWritable(final int contextId) throws OXException {
        return get(contextId, true, false);
    }

    @Override
    public Connection getForUpdateTask(final int contextId) throws OXException {
        return get(contextId, true, true);
    }

    @Override
    public Connection get(final int poolId, final String schema) throws OXException {
        final Connection con;
        try {
            con = pools.getPool(poolId).get();
        } catch (final PoolingException e) {
            throw DBPoolingExceptionCodes.NO_CONNECTION.create(e, I(poolId));
        }
        try {
            if (null != schema && !con.getCatalog().equals(schema)) {
                con.setCatalog(schema);
            }
        } catch (final SQLException e) {
            try {
                pools.getPool(poolId).back(con);
            } catch (final PoolingException e1) {
                Databases.close(con);
                LOG.error(e1.getMessage(), e1);
            }
            throw DBPoolingExceptionCodes.SCHEMA_FAILED.create(e);
        }
        return con;
    }

    @Override
    public Connection getNoTimeout(final int poolId, final String schema) throws OXException {
        final Connection con;
        try {
            con = pools.getPool(poolId).getWithoutTimeout();
        } catch (final PoolingException e) {
            throw DBPoolingExceptionCodes.NO_CONNECTION.create(e, I(poolId));
        }
        try {
            if (null != schema && !con.getCatalog().equals(schema)) {
                con.setCatalog(schema);
            }
        } catch (final SQLException e) {
            try {
                pools.getPool(poolId).back(con);
            } catch (final PoolingException e1) {
                Databases.close(con);
                LOG.error(e1.getMessage(), e1);
            }
            throw DBPoolingExceptionCodes.SCHEMA_FAILED.create(e);
        }
        return con;
    }

    @Override
    public Connection getReadOnlyMonitored(int readPoolId, int writePoolId, String schema, int partitionId) throws OXException {
        return getMonitoredConnection(readPoolId, writePoolId, schema, partitionId, false, false);
    }

    @Override
    public Connection getWritableMonitored(int readPoolId, int writePoolId, String schema, int partitionId) throws OXException {
        return getMonitoredConnection(readPoolId, writePoolId, schema, partitionId, true, false);
    }

    @Override
    public Connection getWritableMonitoredForUpdateTask(int readPoolId, int writePoolId, String schema, int partitionId) throws OXException {
        return getMonitoredConnection(readPoolId, writePoolId, schema, partitionId, true, true);
    }

    public Connection getMonitoredConnection(int readPoolId, int writePoolId, String schema, int partitionId, boolean write, boolean noTimeout) throws OXException {
        AssignmentImpl assignment = new AssignmentImpl(partitionId, Server.getServerId(), readPoolId, writePoolId, schema);
        return get(assignment, write, noTimeout);
    }

    @Override
    public void backReadOnly(final Context ctx, final Connection con) {
        back(con);
    }

    @Override
    public void backReadOnly(final int contextId, final Connection con) {
        back(con);
    }

    @Override
    public void backWritable(final Context ctx, final Connection con) {
        back(con);
    }

    @Override
    public void backWritable(final int contextId, final Connection con) {
        back(con);
    }

    @Override
    public void backWritableAfterReading(Context ctx, Connection con) {
        backFromReading(con);
    }

    @Override
    public void backWritableAfterReading(int contextId, Connection con) {
        backFromReading(con);
    }

    @Override
    public void backForUpdateTask(final int contextId, final Connection con) {
        back(con);
    }

    @Override
    public void backForUpdateTaskAfterReading(final int contextId, final Connection con) {
        backFromReading(con);
    }

    @Override
    public void back(final int poolId, final Connection con) {
        try {
            pools.getPool(poolId).back(con);
        } catch (final PoolingException e) {
            Databases.close(con);
            final OXException e2 = DBPoolingExceptionCodes.RETURN_FAILED.create(e, con.toString());
            LOG.error("", e2);
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    @Override
    public void backNoTimeoout(final int poolId, final Connection con) {
        try {
            pools.getPool(poolId).backWithoutTimeout(con);
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    @Override
    public void backReadOnlyMonitored(int readPoolId, int writePoolId, String schema, int partitionId, Connection con) {
        back(con);
    }

    @Override
    public void backWritableMonitored(int readPoolId, int writePoolId, String schema, int partitionId, Connection con) {
        back(con);
    }

    @Override
    public void backWritableMonitoredForUpdateTask(int readPoolId, int writePoolId, String schema, int partitionId, Connection con) {
        back(con);
    }

    @Override
    public void initMonitoringTables(int writePoolId, String schema) throws OXException {
        Connection con = get(writePoolId, schema);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;
            CreateReplicationTable createReplicationTable = new CreateReplicationTable();
            createReplicationTable.perform(con);
            con.commit();
            rollback = false;
        } catch (SQLException x) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(x, x.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            if (con != null) {
                Databases.autocommit(con);
                back(writePoolId, con);
            }
        }
    }

    @Override
    public void initPartitions(int writePoolId, String schema, int... partitions) throws OXException {
        if (null == partitions || partitions.length <= 0) {
            return;
        }
        Connection con = get(writePoolId, schema);
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;
            stmt = con.prepareStatement("INSERT INTO replicationMonitor (cid, transaction) VALUES (?, ?)");
            stmt.setInt(2, 0);
            for (int partition : partitions) {
                stmt.setInt(1, partition);
                stmt.addBatch();
            }
            stmt.executeBatch();
            con.commit();
            rollback = false;
        } catch (SQLException x) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(x, x.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(stmt);
            if (con != null) {
                Databases.autocommit(con);
                back(writePoolId, con);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getReadOnly(Assignment assignment, boolean noTimeout) throws OXException {
        AssignmentImpl assignmentImpl = new AssignmentImpl(assignment);
        return get(assignmentImpl, false, noTimeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getWritable(Assignment assignment, boolean noTimeout) throws OXException {
        AssignmentImpl assignmentImpl = new AssignmentImpl(assignment);
        return get(assignmentImpl, true, noTimeout);
    }
}
