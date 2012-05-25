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

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.database.Assignment;
import com.openexchange.database.ConfigDatabaseService;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.log.ForceLog;
import com.openexchange.log.LogProperties;
import com.openexchange.pooling.PoolingException;

/**
 * Interface class for accessing the database system.
 * TODO test threads.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class DatabaseServiceImpl implements DatabaseService {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DatabaseServiceImpl.class));

    private final boolean forceWriteOnly;

    private final Pools pools;

    private final ConfigDatabaseService configDatabaseService;

    private final ContextDatabaseAssignmentService assignmentService;

    /**
     * Default constructor.
     */
    public DatabaseServiceImpl(final boolean forceWriteOnly, final Pools pools, final ConfigDatabaseService configDatabaseService, final ContextDatabaseAssignmentService assignmentService) {
        super();
        this.forceWriteOnly = forceWriteOnly;
        this.pools = pools;
        this.configDatabaseService = configDatabaseService;
        this.assignmentService = assignmentService;
    }

    private Connection get(final int contextId, final boolean write, final boolean noTimeout) throws OXException {
        final AssignmentImpl assign = assignmentService.getAssignment(contextId);
        LogProperties.putLogProperty("com.openexchange.database.schema", ForceLog.valueOf(assign.getSchema()));
        return ReplicationMonitor.checkActualAndFallback(pools, assign, noTimeout, write || forceWriteOnly);
    }

    private void back(final Connection con) {
        if (null == con) {
            LogProperties.putLogProperty("com.openexchange.database.schema", null);
            final OXException e = DBPoolingExceptionCodes.NULL_CONNECTION.create();
            LOG.error(e.getMessage(), e);
            return;
        }
        try {
            con.close();
        } catch (final SQLException e) {
            final OXException e1 = DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            LOG.error(e1.getMessage(), e1);
        } finally {
            LogProperties.putLogProperty("com.openexchange.database.schema", null);
        }
    }

    @Override
    public void invalidate(final int contextId) throws OXException {
        assignmentService.removeAssignments(contextId);
    }

    // Delegate config database service methods.

    @Override
    public Connection getReadOnly() throws OXException {
        return configDatabaseService.getReadOnly();
    }

    @Override
    public Connection getWritable() throws OXException {
        return configDatabaseService.getWritable();
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
    public int[] listContexts(final int poolId) throws OXException {
        return configDatabaseService.listContexts(poolId);
    }

    @Override
    public int getServerId() throws OXException {
        return configDatabaseService.getServerId();
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
                LOG.error(e1.getMessage(), e1);
            }
            throw DBPoolingExceptionCodes.SCHEMA_FAILED.create(e);
        }
        return con;
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
    public void backForUpdateTask(final int contextId, final Connection con) {
        back(con);
    }

    @Override
    public void back(final int poolId, final Connection con) {
        try {
            pools.getPool(poolId).back(con);
        } catch (final PoolingException e) {
            final OXException e2 = DBPoolingExceptionCodes.RETURN_FAILED.create(e, I(poolId));
            LOG.error(e2.getMessage(), e2);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void backNoTimeoout(final int poolId, final Connection con) {
        try {
            pools.getPool(poolId).backWithoutTimeout(con);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public int getWritablePool(final int contextId) throws OXException {
        final Assignment assign = assignmentService.getAssignment(contextId);
        return assign.getWritePoolId();
    }

    @Override
    public String getSchemaName(final int contextId) throws OXException {
        return assignmentService.getAssignment(contextId).getSchema();
    }

    @Override
    public int[] getContextsInSameSchema(final int contextId) throws OXException {
        final Assignment assign = assignmentService.getAssignment(contextId);
        final ConfigDBStorage configDBStorage = new ConfigDBStorage(configDatabaseService);
        return configDBStorage.getContextsFromSchema(assign.getSchema(), assign.getWritePoolId());
    }

    @Override
    public void writeAssignment(Connection con, Assignment assignment) throws OXException {
        assignmentService.writeAssignment(con, assignment);
    }
}
