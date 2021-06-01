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

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.slf4j.Logger;
import com.openexchange.database.Assignment;
import com.openexchange.database.AssignmentFactory;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link AssignmentFactoryImpl}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.1
 */
@SingletonService
public class AssignmentFactoryImpl implements AssignmentFactory {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AssignmentFactoryImpl.class);
    }

    private final DatabaseServiceImpl databaseService;
    private final ConcurrentMap<String, Future<List<Assignment>>> assignmentsPerSchema;

    /**
     * Initializes a new {@link AssignmentFactoryImpl}.
     *
     * @param databaseService The database service to use
     */
    public AssignmentFactoryImpl(DatabaseServiceImpl databaseService) {
        super();
        this.databaseService = databaseService;
        assignmentsPerSchema = new ConcurrentHashMap<>(16, 0.9F, 1);
    }

    /**
     * Initializes the db assignments
     *
     * @throws OXException
     */
    @Override
    public void reload() throws OXException {
        assignmentsPerSchema.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Assignment get(int contextId) {
        try {
            return databaseService.getAssignment(contextId);
        } catch (OXException e) {
            LoggerHolder.LOG.warn("Found no assignment for context {}", I(contextId), e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Assignment get(final String schemaName) {
        if (null == schemaName) {
            // Garbage in, garbage out...
            return null;
        }

        Future<List<Assignment>> f = assignmentsPerSchema.get(schemaName);
        if (null == f) {
            final DatabaseService databaseService = this.databaseService;
            FutureTask<List<Assignment>> ft = new FutureTask<>(new Callable<List<Assignment>>() {

                @Override
                public List<Assignment> call() throws Exception {
                    List<Assignment> lAssignments = new LinkedList<>();
                    int serverId = Server.getServerId();

                    PreparedStatement stmt = null;
                    ResultSet result = null;
                    Connection readOnly = databaseService.getReadOnly();
                    try {
                        stmt = readOnly.prepareStatement("SELECT dc.write_db_pool_id, dc.read_db_pool_id, dp.name FROM db_cluster dc INNER JOIN db_pool dp ON dc.write_db_pool_id = dp.db_pool_id WHERE dp.name=?");
                        stmt.setString(1, schemaName);
                        result = stmt.executeQuery();
                        if (result.next()) {
                            // This is an assignment w/o a context_server2db_pool affiliation.
                            // Expect the database name to be the actual schema name
                            do {
                                int writePoolId = result.getInt(1);
                                int readPoolId = result.getInt(2);
                                String databaseName = result.getString(3);
                                if (readPoolId == 0) {
                                    readPoolId = writePoolId;
                                }
                                AssignmentImpl assignmentImpl = new AssignmentImpl(0, serverId, readPoolId, writePoolId, databaseName);
                                lAssignments.add(assignmentImpl);
                            } while (result.next());

                            // TODO: Worth looking up 'context_server2db_pool' table, too ? If yes, remove return statement below:
                            return lAssignments;
                        }
                        Databases.closeSQLStuff(result, stmt);
                        result = null;
                        stmt = null;

                        stmt = readOnly.prepareStatement("SELECT db_schema, MIN(cid), MIN(write_db_pool_id), MIN(read_db_pool_id) FROM context_server2db_pool WHERE server_id=? AND db_schema=?");
                        stmt.setInt(1, serverId);
                        stmt.setString(2, schemaName);
                        result = stmt.executeQuery();
                        while (result.next()) {
                            String queriedSchema = result.getString(1);
                            if (null != queriedSchema) {
                                // An assignment with a context_server2db_pool affiliation.
                                int contextId = result.getInt(2);
                                int writePoolId = result.getInt(3);
                                int readPoolId = result.getInt(4);
                                if (readPoolId == 0) {
                                    readPoolId = writePoolId;
                                }
                                AssignmentImpl assignmentImpl = new AssignmentImpl(contextId, serverId, readPoolId, writePoolId, schemaName);
                                lAssignments.add(assignmentImpl);
                            }
                        }
                        Databases.closeSQLStuff(result, stmt);
                        result = null;
                        stmt = null;

                        return lAssignments;
                    } catch (SQLException e) {
                        throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                    } finally {
                        Databases.closeSQLStuff(result, stmt);
                        databaseService.backReadOnly(readOnly);
                    }
                }
            });
            f = assignmentsPerSchema.putIfAbsent(schemaName, ft);
            if (null == f) {
                f = ft;
                ft.run();
            }
        }

        try {
            List<Assignment> assignments = f.get();
            if (null == assignments || assignments.isEmpty()) {
                LoggerHolder.LOG.warn("Found no assignment for schema name {}", schemaName);
                return null;
            }
            if (assignments.size() > 1) {
                LoggerHolder.LOG.warn("Found duplicate assignments for schema name {}", schemaName, new Throwable("Found duplicate assignments"));
            }
            return assignments.get(0);
        } catch (InterruptedException e) {
            // Cannot occur
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            LoggerHolder.LOG.warn("Found no assignment for schema name {}", schemaName, t);
            return null;
        }
    }

}
