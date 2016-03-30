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

package com.openexchange.databaseold;

import java.sql.Connection;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceExceptionCode;

/**
 * Interface class for accessing the database system.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Database {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Database.class);

    private static volatile DatabaseService databaseService;

    /**
     * Prevent instantiation.
     */
    private Database() {
        super();
    }

    public static void setDatabaseService(final DatabaseService databaseService) {
        Database.databaseService = databaseService;
    }

    /**
     * Gets the database service.
     *
     * @return The database service
     * @throws OXException If service is unavailable at the moment
     */
    public static DatabaseService getDatabaseService() throws OXException {
        final DatabaseService databaseService = Database.databaseService;
        if (null == databaseService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        return databaseService;
    }

    public static int resolvePool(final int contextId, final boolean write) throws OXException {
        return getDatabaseService().getWritablePool(contextId);
    }

    public static String getSchema(final int contextId) throws OXException {
        return getDatabaseService().getSchemaName(contextId);
    }

    /**
     * Returns a connection to the config database.
     * @param write <code>true</code> if you need a writable connection.
     * @return a connection to the config database.
     * @throws OXException if no connection can be obtained.
     */
    public static Connection get(final boolean write) throws OXException {
        return write ? getDatabaseService().getWritable() : getDatabaseService().getReadOnly();
    }

    /**
     * Returns a connection to the database of the specified context.
     * @param ctx Context.
     * @param write <code>true</code> if you need a writable connection.
     * @return a connection to the database of the specified context.
     * @throws OXException if no connection can be obtained.
     */
    public static Connection get(final Context ctx, final boolean write) throws OXException {
        return write ? getDatabaseService().getWritable(ctx) : getDatabaseService().getReadOnly(ctx);
    }

    /**
     * Returns a connection to the database of the context with the specified identifier.
     * @param contextId identifier of the context.
     * @param write <code>true</code> if you need a writable connection.
     * @return a connection to the database of the context.
     * @throws OXException if no connection can be obtained.
     */
    public static Connection get(final int contextId, final boolean write) throws OXException {
        return write ? getDatabaseService().getWritable(contextId) : getDatabaseService().getReadOnly(contextId);
    }

    /**
     * This method must only be used for update tasks to get a connection to the database of the context with the specified identifier.
     * @param contextId identifier of the context.
     * @param write <code>true</code> if you need a writable connection.
     * @return a connection to the database of the context.
     * @throws OXException if no connection can be obtained.
     */
    public static Connection getNoTimeout(final int contextId, final boolean write) throws OXException {
        return getDatabaseService().getForUpdateTask(contextId);
    }

    /**
     * This method is for moving contexts only.
     * @param poolId identifier of the database pool.
     * @param schema schema name.
     * @return a connection to the database from the given pool directed to the given schema.
     * @throws OXException if no connection can be obtained.
     */
    public static Connection get(final int poolId, final String schema) throws OXException {
        return getDatabaseService().get(poolId, schema);
    }

    /**
     * Returns a connection to the config database to the pool.
     * @param write <code>true</code> if you obtained a writable connection.
     * @param con Connection to return.
     */
    public static void back(final boolean write, final Connection con) {
        try {
            if (write) {
                getDatabaseService().backWritable(con);
            } else {
                getDatabaseService().backReadOnly(con);
            }
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    /**
     * Returns a connection to the database of the specified context to the pool.
     * @param ctx Context.
     * @param write <code>true</code> if you obtained a writable connection.
     * @param con Connection to return.
     */
    public static void back(final Context ctx, final boolean write, final Connection con) {
        try {
            if (write) {
                getDatabaseService().backWritable(ctx, con);
            } else {
                getDatabaseService().backReadOnly(ctx, con);
            }
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    public static void backAfterReading(Context ctx, Connection con) {
        try {
            getDatabaseService().backWritableAfterReading(ctx, con);
        } catch (OXException e) {
            LOG.error("", e);
        }
    }

    /**
     * Returns a connection to the database of the context with the specified identifier to the pool.
     * @param contextId identifier of the context.
     * @param write <code>true</code> if you obtained a writable connection.
     * @param con Connection to return.
     */
    public static void back(final int contextId, final boolean write, final Connection con) {
        try {
            if (write) {
                getDatabaseService().backWritable(contextId, con);
            } else {
                getDatabaseService().backReadOnly(contextId, con);
            }
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    public static void backAfterReading(int contextId, Connection con) {
        try {
            getDatabaseService().backWritableAfterReading(contextId, con);
        } catch (OXException e) {
            LOG.error("", e);
        }
    }

    /**
     * This method must only be used by database update tasks that return a connection to the database of the context with the specified
     * identifier to the pool.
     * @param contextId identifier of the context.
     * @param write <code>true</code> if you obtained a writable connection.
     * @param con Connection to return.
     */
    public static void backNoTimeout(final int contextId, final boolean write, final Connection con) {
        try {
            if (write) {
                getDatabaseService().backForUpdateTask(contextId, con);
            } else {
                getDatabaseService().backForUpdateTaskAfterReading(contextId, con);
            }
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    /**
     * This method is for moving contexts only.
     * @param poolId identifier of the pool the connection should be returned to.
     * @param con connection to return.
     */
    public static void back(final int poolId, final Connection con) {
        try {
            getDatabaseService().back(poolId, con);
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    /**
     * This method is only for administrative access to contexts.
     * @param poolId identifier of the database pool.
     * @param schema schema name.
     * @return a connection to the database without a time-out from the given pool directed to the given schema.
     * @throws OXException if no connection can be obtained.
     */
    public static Connection getNoTimeout(final int poolId, final String schema) throws OXException {
        return getDatabaseService().getNoTimeout(poolId, schema);
    }

    /**
     * This method is only for administrative access to contexts.
     * @param poolId identifier of the pool the connection should be returned to.
     * @param con connection to return.
     */
    public static void backNoTimeoout(final int poolId, final Connection con) {
        try {
            getDatabaseService().backNoTimeoout(poolId, con);
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    /**
     * Resets the database pooling information for a context. This is especially the assignments to database servers.
     * @param contextId unique identifier of the context.
     * @throws OXException if resolving the server identifier fails.
     */
    public static void reset(final int contextId) throws OXException {
        getDatabaseService().invalidate(contextId);
    }

    public static int[] getContextsInSameSchema(final int contextId) throws OXException {
        return getDatabaseService().getContextsInSameSchema(contextId);
    }

    public static int getServerId() throws OXException {
        return getDatabaseService().getServerId();
    }

    public static String getServerName() throws OXException {
        return getDatabaseService().getServerName();
    }
}
