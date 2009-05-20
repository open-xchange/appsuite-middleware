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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.database.ConfigDatabaseService;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.DBPoolingException.Code;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.pooling.PoolingException;

/**
 * Interface class for accessing the database system.
 * TODO test threads.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class DatabaseServiceImpl implements DatabaseService {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(DatabaseServiceImpl.class);

    private static boolean forceWriteOnly;

    private static Pools pools;

    private static AssignmentStorage assignmentStorage;

    private static ConfigDatabaseService configDatabaseService;

    /**
     * Default constructor.
     */
    public DatabaseServiceImpl() {
        super();
    }

    public static int resolvePool(int contextId, boolean write) throws DBPoolingException {
        Assignment assign = assignmentStorage.getAssignment(contextId);
        return write || forceWriteOnly ? assign.getWritePoolId() : assign.getReadPoolId();
    }

    public static String getSchema(int contextId) throws DBPoolingException {
        return assignmentStorage.getAssignment(contextId).getSchema();
    }

    /**
     * Returns a connection to the database of the specified context.
     * @param ctx Context.
     * @param write <code>true</code> if you need a writable connection.
     * @return a connection to the database of the specified context.
     * @throws DBPoolingException if no connection can be obtained.
     */
    public static Connection get(Context ctx, boolean write) throws DBPoolingException {
        return get(ctx.getContextId(), write);
    }

    /**
     * Returns a connection to the database of the context with the specified identifier.
     * @param contextId identifier of the context.
     * @param write <code>true</code> if you need a writable connection.
     * @return a connection to the database of the context.
     * @throws DBPoolingException if no connection can be obtained.
     */
    public static Connection get(int contextId, boolean write) throws DBPoolingException {
        return get(contextId, write, false);
    }

    /**
     * This method must only be used for update tasks to get a connection to the database of the context with the specified identifier.
     * @param contextId identifier of the context.
     * @param write <code>true</code> if you need a writable connection.
     * @return a connection to the database of the context.
     * @throws DBPoolingException if no connection can be obtained.
     */
    public static Connection getNoTimeout(int contextId, boolean write) throws DBPoolingException {
        return get(contextId, write, true);
    }

    private static Connection get(int contextId, boolean write, boolean noTimeout) throws DBPoolingException {
        final Assignment assign = assignmentStorage.getAssignment(contextId);
        final int poolId;
        if (write || forceWriteOnly) {
            poolId = assign.getWritePoolId();
        } else {
            poolId = assign.getReadPoolId();
        }
        return get(poolId, assign.getSchema(), noTimeout);
    }

    private static Connection get(int poolId, String schema, boolean noTimeout) throws DBPoolingException {
        final Connection con;
        try {
            final ConnectionPool pool = pools.getPool(poolId);
            if (noTimeout) {
                con = pool.getWithoutTimeout();
            } else {
                con = pool.get();
            }
        } catch (final PoolingException e) {
            throw new DBPoolingException(Code.NO_CONNECTION, e, I(poolId));
        }
        try {
            final String oldSchema = con.getCatalog();
            if (!oldSchema.equals(schema)) {
                con.setCatalog(schema);
            }
        } catch (final SQLException e) {
            try {
                pools.getPool(poolId).back(con);
            } catch (final PoolingException e1) {
                LOG.error(e1.getMessage(), e1);
            }
            throw new DBPoolingException(Code.SCHEMA_FAILED, e);
        }
        return con;
    }

    /**
     * Returns a connection to the database of the specified context to the pool.
     * @param ctx Context.
     * @param write <code>true</code> if you obtained a writable connection.
     * @param con Connection to return.
     */
    public static void back(Context ctx, boolean write, Connection con) {
        back(ctx.getContextId(), write, con);
    }

    /**
     * Returns a connection to the database of the context with the specified identifier to the pool.
     * @param contextId identifier of the context.
     * @param write <code>true</code> if you obtained a writable connection.
     * @param con Connection to return.
     */
    public static void back(int contextId, boolean write, Connection con) {
        back(contextId, write, con, false);
    }

    /**
     * This method must only be used by database update tasks that return a connection to the database of the context with the specified
     * identifier to the pool.
     * @param contextId identifier of the context.
     * @param write <code>true</code> if you obtained a writable connection.
     * @param con Connection to return.
     */
    public static void backNoTimeout(int contextId, boolean write, Connection con) {
        back(contextId, write, con, true);
    }

    private static void back(int contextId, boolean write, Connection con, boolean noTimeout) {
        final int poolId;
        try {
            poolId = resolvePool(contextId, write);
        } catch (final DBPoolingException e) {
            LOG.error(e.getMessage(), e);
            return;
        }
        back(poolId, con, noTimeout);
    }

    private static void back(int poolId, Connection con, boolean noTimeout) {
        try {
            final ConnectionPool pool = pools.getPool(poolId);
            if (noTimeout) {
                pool.backWithoutTimeout(con);
            } else {
                pool.back(con);
            }
        } catch (final PoolingException e) {
            final DBPoolingException exc = new DBPoolingException(Code.RETURN_FAILED, e, I(poolId));
            LOG.error(exc.getMessage(), exc);
        } catch (final DBPoolingException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void invalidate(int contextId) throws DBPoolingException {
        assignmentStorage.removeAssignments(contextId);
    }

    /**
     * Sets a new check time for all database connetion pools. If the check time
     * is exhausted since the last use of the connection a select statement is
     * sent to the database to verify that the connection still works.
     * @param checkTime new check time.
     */
    public static void setCheckTime(long checkTime) {
        for (final ConnectionPool pool : pools.getPools()) {
            pool.setCheckTime(checkTime);
        }
    }

    public static int getNumConnections(Context ctx, boolean write) {
        return getNumConnections(ctx.getContextId(), write);
    }

    public static int getNumConnections(int contextId, boolean write) {
        int retval = -1;
        try {
            final int poolId = resolvePool(contextId, write);
            final ConnectionPool pool = pools.getPool(poolId);
            retval = pool.getNumActive() + pool.getNumIdle();
        } catch (DBPoolingException e) {
            LOG.error(e.getMessage(), e);
        }
        return retval;
    }

    /**
     * @return the total number of database connections.
     */
    public static int getNumConnections() {
        int connections = 0;
        for (final ConnectionPool pool : pools.getPools()) {
            connections += pool.getPoolSize();
        }
        return connections;
    }

    public static void setPools(Pools pools) {
        DatabaseServiceImpl.pools = pools;
    }

    public static void setAssignmentStorage(AssignmentStorage assignementStorage) {
        DatabaseServiceImpl.assignmentStorage = assignementStorage;
    }

    public static void setForceWrite(final boolean forceWriteOnly) {
        DatabaseServiceImpl.forceWriteOnly = forceWriteOnly;
    }

    static void setConfigDatabaseService(ConfigDatabaseService configDatabaseService) {
        DatabaseServiceImpl.configDatabaseService = configDatabaseService;
    }

    // Delegate config database service methods.

    public Connection getReadOnly() throws DBPoolingException {
        return configDatabaseService.getReadOnly();
    }

    public Connection getWritable() throws DBPoolingException {
        return configDatabaseService.getWritable();
    }

    public void backReadOnly(Connection con) {
        configDatabaseService.backReadOnly(con);
    }

    public void backWritable(Connection con) {
        configDatabaseService.backWritable(con);
    }

    // Implemented database service methods.

    public Connection getReadOnly(Context ctx) throws DBPoolingException {
        return get(ctx.getContextId(), false);
    }

    public Connection getReadOnly(int contextId) throws DBPoolingException {
        return get(contextId, false);
    }

    public Connection getWritable(Context ctx) throws DBPoolingException {
        return get(ctx.getContextId(), true);
    }

    public Connection getWritable(int contextId) throws DBPoolingException {
        return get(contextId, true);
    }

    public Connection getForUpdateTask(int contextId) throws DBPoolingException {
        return getNoTimeout(contextId, true);
    }

    public Connection get(int poolId, String schema) throws DBPoolingException {
        return get(poolId, schema, false);
    }

    public void backReadOnly(Context ctx, Connection con) {
        back(ctx.getContextId(), false, con);
    }

    public void backReadOnly(int contextId, Connection con) {
        back(contextId, false, con);
    }

    public void backWritable(Context ctx, Connection con) {
        back(ctx.getContextId(), true, con);
    }

    public void backWritable(int contextId, Connection con) {
        back(contextId, true, con);
    }

    public void backForUpdateTask(int contextId, Connection con) {
        backNoTimeout(contextId, true, con);
    }

    public void back(int poolId, Connection con) {
        back(poolId, con, false);
    }

    public int getWritablePool(int contextId) throws DBPoolingException {
        return resolvePool(contextId, true);
    }

    public String getSchemaName(int contextId) throws DBPoolingException {
        return getSchema(contextId);
    }

    public int[] getContextsInSameSchema(int contextId) throws DBPoolingException {
        ConfigDBStorage configDBStorage = new ConfigDBStorage(this);
        return configDBStorage.getContextsFromSchema(getSchema(contextId), resolvePool(contextId, true));
    }
}
