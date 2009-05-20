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
import com.openexchange.groupware.contexts.Context;

/**
 * Interface class for accessing the database system.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Database {

    private static DatabaseService databaseService;

    /**
     * Prevent instantiation.
     */
    private Database() {
        super();
    }

    public static void setDatabaseService(DatabaseService databaseService) {
        Database.databaseService = databaseService;
    }

    public static int resolvePool(int contextId, boolean write) throws DBPoolingException {
        return databaseService.getWritablePool(contextId);
    }

    public static String getSchema(int contextId) throws DBPoolingException {
        return databaseService.getSchemaName(contextId);
    }

    /**
     * Returns a connection to the config database.
     * @param write <code>true</code> if you need a writable connection.
     * @return a connection to the config database.
     * @throws DBPoolingException if no connection can be obtained.
     */
    public static Connection get(boolean write) throws DBPoolingException {
        return write ? databaseService.getWritable() : databaseService.getReadOnly();
    }

    /**
     * Returns a connection to the database of the specified context.
     * @param ctx Context.
     * @param write <code>true</code> if you need a writable connection.
     * @return a connection to the database of the specified context.
     * @throws DBPoolingException if no connection can be obtained.
     */
    public static Connection get(Context ctx, boolean write) throws DBPoolingException {
        return write ? databaseService.getWritable(ctx) : databaseService.getReadOnly(ctx);
    }

    /**
     * Returns a connection to the database of the context with the specified identifier.
     * @param contextId identifier of the context.
     * @param write <code>true</code> if you need a writable connection.
     * @return a connection to the database of the context.
     * @throws DBPoolingException if no connection can be obtained.
     */
    public static Connection get(int contextId, boolean write) throws DBPoolingException {
        return write ? databaseService.getWritable(contextId) : databaseService.getReadOnly(contextId);
    }

    /**
     * This method must only be used for update tasks to get a connection to the database of the context with the specified identifier.
     * @param contextId identifier of the context.
     * @param write <code>true</code> if you need a writable connection.
     * @return a connection to the database of the context.
     * @throws DBPoolingException if no connection can be obtained.
     */
    public static Connection getNoTimeout(int contextId, boolean write) throws DBPoolingException {
        return databaseService.getForUpdateTask(contextId);
    }

    /**
     * This method is for moving contexts only.
     * @param poolId identifier of the database pool.
     * @param schema schema name.
     * @return a connection to the database from the given pool directed to the given schema.
     * @throws DBPoolingException if no connection can be obtained.
     */
    public static Connection get(int poolId, String schema) throws DBPoolingException {
        return databaseService.get(poolId, schema);
    }

    /**
     * Returns a connection to the config database to the pool.
     * @param write <code>true</code> if you obtained a writable connection.
     * @param con Connection to return.
     */
    public static void back(boolean write, Connection con) {
        if (write) {
            databaseService.backWritable(con);
        } else {
            databaseService.backReadOnly(con);
        }
    }

    /**
     * Returns a connection to the database of the specified context to the pool.
     * @param ctx Context.
     * @param write <code>true</code> if you obtained a writable connection.
     * @param con Connection to return.
     */
    public static void back(Context ctx, boolean write, Connection con) {
        if (write) {
            databaseService.backWritable(ctx, con);
        } else {
            databaseService.backReadOnly(ctx, con);
        }
    }

    /**
     * Returns a connection to the database of the context with the specified identifier to the pool.
     * @param contextId identifier of the context.
     * @param write <code>true</code> if you obtained a writable connection.
     * @param con Connection to return.
     */
    public static void back(final int contextId, final boolean write, final Connection con) {
        if (write) {
            databaseService.backWritable(contextId, con);
        } else {
            databaseService.backReadOnly(contextId, con);
        }
    }

    /**
     * This method must only be used by database update tasks that return a connection to the database of the context with the specified
     * identifier to the pool.
     * @param contextId identifier of the context.
     * @param write <code>true</code> if you obtained a writable connection.
     * @param con Connection to return.
     */
    public static void backNoTimeout(int contextId, boolean write, Connection con) {
        databaseService.backForUpdateTask(contextId, con);
    }

    /**
     * This method is for moving contexts only.
     * @param poolId identifier of the pool the connection should be returned to.
     * @param con connection to return.
     */
    public static void back(int poolId, Connection con) {
        databaseService.back(poolId, con);
    }

    /**
     * Resets the database pooling information for a context. This is especially the assignments to database servers.
     * @param contextId unique identifier of the context.
     * @throws DBPoolingException if resolving the server identifier fails.
     */
    public static void reset(int contextId) throws DBPoolingException {
        databaseService.invalidate(contextId);
    }

    public static int[] getContextsInSameSchema(int contextId) throws DBPoolingException {
        return databaseService.getContextsInSameSchema(contextId);
    }
}
