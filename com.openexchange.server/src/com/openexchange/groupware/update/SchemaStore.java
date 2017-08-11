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

package com.openexchange.groupware.update;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import com.openexchange.caching.CacheService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.internal.SchemaExceptionCodes;
import com.openexchange.groupware.update.internal.SchemaStoreImpl;

/**
 * Abstract class defining the interface for reading the schema version information.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class SchemaStore {

    private static final SchemaStoreImpl SINGLETON = new SchemaStoreImpl();

    protected SchemaStore() {
        super();
    }

    /**
     * Factory method.
     *
     * @return an implementation for this interface.
     */
    public static SchemaStore getInstance() {
        return SINGLETON;
    }

    protected abstract SchemaUpdateState getSchema(int poolId, String schemaName, Connection con) throws OXException;

    /**
     * Marks given schema as locked due to a start of an update process.
     *
     * @param schema the schema
     * @param background <code>false</code> if blocking tasks are executed.
     * @throws OXException
     */
    public abstract void lockSchema(Schema schema, boolean background) throws OXException;

    /**
     * Marks given schema as unlocked to release this schema from an update process.
     *
     * @param schema the schema
     * @param background <code>false</code> if blocking tasks finished.
     * @throws OXException
     */
    public abstract void unlockSchema(Schema schema, boolean background) throws OXException;

    /**
     * Tries to refresh the schema lock (resetting time stamp to current time).
     *
     * @param schema The schema whose lock is supposed to be refreshed
     * @param background <code>false</code> if blocking tasks are in progress; otherwise <code>true</code>.
     * @return <code>true</code> if the lock was successfully refreshed; otherwise <code>false</code>
     * @throws OXException If refresh fails
     */
    public abstract boolean tryRefreshSchemaLock(Schema schema, boolean background) throws OXException;

    public final Schema getSchema(final Context ctx) throws OXException {
        return getSchema(ctx.getContextId());
    }

    public final SchemaUpdateState getSchema(final int contextId) throws OXException {
        // This method is used when doing a normal login. In this case fetching the Connection runs through replication monitor and
        // initializes the transaction counter from the master. This allows redirecting subsequent reads after normal login to the master
        // if the slave is not actual. See bugs 19817 and 27460.
        Connection con = Database.get(contextId, true);
        try {
            return getSchema(Database.resolvePool(contextId, true), Database.getSchema(contextId), con);
        } finally {
            // In fact the transaction counter is initialized when returning the connection to the pool ;-)
            Database.backAfterReading(contextId, con);
        }
    }

    public final SchemaUpdateState getSchema(int poolId, String schemaName) throws OXException {
        // This method is used when creating a context through the administration daemon. In this case we did not write yet the information
        // into the ConfigDB in which database and schema the context is located. Therefore we are not able to initialize the transaction
        // counter for the replication monitor.
        Connection con = Database.get(poolId, schemaName);
        try {
            return getSchema(poolId, schemaName, con);
        } finally {
            Database.back(poolId, con);
        }
    }

    public abstract ExecutedTask[] getExecutedTasks(int poolId, String schemaName) throws OXException;

    public final void addExecutedTask(String taskName, boolean success, int poolId, String schema) throws OXException {
        Connection con = Database.get(poolId, schema);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;

            addExecutedTask(con, taskName, success, poolId, schema);

            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            Database.back(poolId, con);
        }
    }

    /**
     * @param con a writable database connection, but in transaction mode.
     */
    public abstract void addExecutedTask(Connection con, String taskName, boolean success, int poolId, String schema) throws OXException;

    public final void addExecutedTasks(int contextId, Collection<String> taskNames, boolean success, int poolId, String schema) throws OXException {
        final Connection con = Database.get(contextId, true);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;

            addExecutedTasks(con, taskNames, success, poolId, schema);

            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    /**
     * @param con a writable database connection, but in transaction mode.
     */
    public abstract void addExecutedTasks(Connection con, Collection<String> taskNames, boolean success, int poolId, String schema) throws OXException;

    public abstract void setCacheService(CacheService cacheService);

    public abstract void removeCacheService();
}
