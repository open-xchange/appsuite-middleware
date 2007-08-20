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

package com.openexchange.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.pooling.PoolingException;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.DBPoolingException.Code;

/**
 * Interface class for accessing the database system.
 * TODO test threads.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Database {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Database.class);

    /**
     * Prevent instantiation
     */
    private Database() {
        super();
    }

    public static int resolvePool(final int contextId, final boolean write)
        throws DBPoolingException {
        final Assignment assign = AssignmentStorage.getAssignment(contextId);
        final int poolId;
        if (write) {
            poolId = assign.writePoolId;
        } else {
            poolId = assign.readPoolId;
        }
        return poolId;
    }

    public static String getSchema(final int contextId)
        throws DBPoolingException {
        final Assignment assign = AssignmentStorage.getAssignment(contextId);
        return assign.schema;
    }

    /**
     * Returns a connection to the config database.
     * @param write <code>true</code> if you need a writable connection.
     * @return a connection to the config database.
     * @throws DBPoolingException if no connection can be obtained.
     */
    public static Connection get(final boolean write)
        throws DBPoolingException {
        final Assignment assign = AssignmentStorage.getConfigDBAssignment();
        final int poolId;
        if (write) {
            poolId = assign.writePoolId;
        } else {
            poolId = assign.readPoolId;
        }
        try {
            return Pools.getPool(poolId).get();
        } catch (PoolingException e) {
            throw new DBPoolingException(Code.NO_CONFIG_DB, e);
        }
    }

    /**
     * Returns a connection to the database of the specified context.
     * @param ctx Context.
     * @param write <code>true</code> if you need a writable connection.
     * @return a connection to the database of the specified context.
     * @throws DBPoolingException if no connection can be obtained.
     */
    public static Connection get(final Context ctx, final boolean write)
        throws DBPoolingException {
        return get(ctx.getContextId(), write);
    }

    /**
     * Returns a connection to the database of the context with the specified
     * identifier.
     * @param contextId identifier of the context.
     * @param write <code>true</code> if you need a writable connection.
     * @return a connection to the database of the context.
     * @throws DBPoolingException if no connection can be obtained.
     */
    public static Connection get(final int contextId, final boolean write)
        throws DBPoolingException {
        final Assignment assign = AssignmentStorage.getAssignment(contextId);
        final int poolId;
        if (write) {
            poolId = assign.writePoolId;
        } else {
            poolId = assign.readPoolId;
        }
        return get(poolId, assign.schema);
    }

    /**
     * Fetches a connection from the given pool and sets the database schema.
     * @param poolId unique identifier of the pool
     * @param schema schema name.
     * @return a fetched connection that is directed to the schema.
     * @throws DBPoolingException if fetching a connection fails.
     */
    public static Connection get(final int poolId, final String schema)
        throws DBPoolingException {
        final Connection con;
        try {
            con = Pools.getPool(poolId).get();
        } catch (PoolingException e) {
            throw new DBPoolingException(Code.NO_CONNECTION, e, Integer.valueOf(
                poolId));
        }
        try {
            final String oldSchema = con.getCatalog();
            if (!oldSchema.equals(schema)) {
                con.setCatalog(schema);
            }
        } catch (SQLException e) {
            try {
                Pools.getPool(poolId).back(con);
            } catch (PoolingException e1) {
                LOG.error(e1.getMessage(), e1);
            }
            throw new DBPoolingException(Code.SCHEMA_FAILED, e);
        }
        return con;
    }

    /**
     * Returns a connection to the config database to the pool.
     * @param write <code>true</code> if you obtained a writable connection.
     * @param con Connection to return.
     */
    public static void back(final boolean write, final Connection con) {
        // TODO remove null check to produce more error messages
        final Assignment assign = AssignmentStorage.getConfigDBAssignment();
        final int poolId;
        if (write) {
            poolId = assign.writePoolId;
        } else {
            poolId = assign.readPoolId;
        }
        back(poolId, con);
    }

    /**
     * Returns a connection to the database of the specified context to the
     * pool.
     * @param ctx Context.
     * @param write <code>true</code> if you obtained a writable connection.
     * @param con Connection to return.
     */
    public static void back(final Context ctx, final boolean write,
        final Connection con) {
        back(ctx.getContextId(), write, con);
    }

    /**
     * Returns a connection to the database of the context with the specified
     * identifier to the pool.
     * @param contextId identifier of the context.
     * @param write <code>true</code> if you obtained a writable connection.
     * @param con Connection to return.
     */
    public static void back(final int contextId, final boolean write,
        final Connection con) {
        final int poolId;
        try {
            poolId = resolvePool(contextId, write);
        } catch (DBPoolingException e) {
            LOG.error(e.getMessage(), e);
            return;
        }
        back(poolId, con);
    }

    /**
     * Returns the given connection to a pool.
     * @param poolId unique identifier of the pool
     * @param con connection to return.
     */
    public static void back(final int poolId, final Connection con) {
        try {
            Pools.getPool(poolId).back(con);
        } catch (PoolingException e) {
            final DBPoolingException exc = new DBPoolingException(
                Code.RETURN_FAILED, e, Integer.valueOf(poolId));
            LOG.error(exc.getMessage(), exc);
        } catch (DBPoolingException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Resets the database pooling information for a context. This is
     * especially the assignments to database servers.
     * @param contextId unique identifier of the context.
     * @throws DBPoolingException if resolving the server identifier fails.
     */
    public static void reset(final int contextId) throws DBPoolingException {
        AssignmentStorage.removeAssignments(contextId);
    }

    /**
     * Sets a new check time for all database connetion pools. If the check time
     * is exhausted since the last use of the connection a select statement is
     * sent to the database to verify that the connection still works.
     * @param checkTime new check time.
     */
    public static void setCheckTime(final long checkTime) {
        for (ConnectionPool pool : Pools.getPools()) {
            pool.setCheckTime(checkTime);
        }
    }

    public static int getNumConnections(final Context ctx,
        final boolean write) {
        return getNumConnections(ctx.getContextId(), write);
    }

    public static int getNumConnections(final int contextId,
        final boolean write) {
        int retval = -1;
        try {
            final int poolId = resolvePool(contextId, write);
            final ConnectionPool pool = Pools.getPool(poolId);
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
        for (ConnectionPool pool : Pools.getPools()) {
            connections += pool.getPoolSize();
        }
        return connections;
    }
}
