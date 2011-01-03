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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.pooling.PoolingException;

/**
 * Interface class for accessing the database system.
 * TODO test threads.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class DatabaseServiceImpl implements DatabaseService {

    private static final Log LOG = LogFactory.getLog(DatabaseServiceImpl.class);

    private final boolean forceWriteOnly;

    private final Pools pools;

    private final ConfigDatabaseService configDatabaseService;

    private final ContextDatabaseAssignmentService assignmentService;

    /**
     * Default constructor.
     */
    public DatabaseServiceImpl(boolean forceWriteOnly, Pools pools, ConfigDatabaseService configDatabaseService, ContextDatabaseAssignmentService assignmentService) {
        super();
        this.forceWriteOnly = forceWriteOnly;
        this.pools = pools;
        this.configDatabaseService = configDatabaseService;
        this.assignmentService = assignmentService;
    }

    private Connection get(int contextId, boolean write, boolean noTimeout) throws DBPoolingException {
        final Assignment assign = assignmentService.getAssignment(contextId);
        return ReplicationMonitor.checkActualAndFallback(pools, assign, noTimeout, write || forceWriteOnly);
    }

    private void back(Connection con) {
        try {
            con.close();
        } catch (SQLException e) {
            DBPoolingException e1 = DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            LOG.error(e1.getMessage(), e1);
        }
    }

    public void invalidate(int contextId) throws DBPoolingException {
        assignmentService.removeAssignments(contextId);
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

    public int[] listContexts(int poolId) throws DBPoolingException {
        return configDatabaseService.listContexts(poolId);
    }

    // Implemented database service methods.

    public Connection getReadOnly(Context ctx) throws DBPoolingException {
        return get(ctx.getContextId(), false, false);
    }

    public Connection getReadOnly(int contextId) throws DBPoolingException {
        return get(contextId, false, false);
    }

    public Connection getWritable(Context ctx) throws DBPoolingException {
        return get(ctx.getContextId(), true, false);
    }

    public Connection getWritable(int contextId) throws DBPoolingException {
        return get(contextId, true, false);
    }

    public Connection getForUpdateTask(int contextId) throws DBPoolingException {
        return get(contextId, true, true);
    }

    public Connection get(int poolId, String schema) throws DBPoolingException {
        final Connection con;
        try {
            con = pools.getPool(poolId).get();
        } catch (PoolingException e) {
            throw DBPoolingExceptionCodes.NO_CONNECTION.create(e, I(poolId));
        }
        try {
            String oldSchema = con.getCatalog();
            if (!oldSchema.equals(schema)) {
                con.setCatalog(schema);
            }
        } catch (SQLException e) {
            try {
                pools.getPool(poolId).back(con);
            } catch (PoolingException e1) {
                LOG.error(e1.getMessage(), e1);
            }
            throw DBPoolingExceptionCodes.SCHEMA_FAILED.create(e);
        }
        return con;
    }

    public void backReadOnly(Context ctx, Connection con) {
        back(con);
    }

    public void backReadOnly(int contextId, Connection con) {
        back(con);
    }

    public void backWritable(Context ctx, Connection con) {
        back(con);
    }

    public void backWritable(int contextId, Connection con) {
        back(con);
    }

    public void backForUpdateTask(int contextId, Connection con) {
        back(con);
    }

    public void back(int poolId, Connection con) {
        try {
            pools.getPool(poolId).back(con);
        } catch (PoolingException e) {
            final DBPoolingException e2 = DBPoolingExceptionCodes.RETURN_FAILED.create(e, I(poolId));
            LOG.error(e2.getMessage(), e2);
        } catch (DBPoolingException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public int getWritablePool(int contextId) throws DBPoolingException {
        Assignment assign = assignmentService.getAssignment(contextId);
        return assign.getWritePoolId();
    }

    public String getSchemaName(int contextId) throws DBPoolingException {
        return assignmentService.getAssignment(contextId).getSchema();
    }

    public int[] getContextsInSameSchema(int contextId) throws DBPoolingException {
        Assignment assign = assignmentService.getAssignment(contextId);
        ConfigDBStorage configDBStorage = new ConfigDBStorage(configDatabaseService);
        return configDBStorage.getContextsFromSchema(assign.getSchema(), assign.getWritePoolId());
    }
}
