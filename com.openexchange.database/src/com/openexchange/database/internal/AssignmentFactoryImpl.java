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

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.openexchange.database.Assignment;
import com.openexchange.database.AssignmentFactory;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;

/**
 * {@link AssignmentFactoryImpl}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.1
 */
public class AssignmentFactoryImpl implements AssignmentFactory {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AssignmentFactoryImpl.class);

    private final AtomicReference<Assignments> assignmentsRef;

    private final DatabaseServiceImpl databaseService;

    /**
     * Initializes a new {@link AssignmentFactoryImpl}.
     *
     * @param databaseService The database service to use
     */
    public AssignmentFactoryImpl(DatabaseServiceImpl databaseService) {
        super();
        this.databaseService = databaseService;
        assignmentsRef = new AtomicReference<Assignments>(new Assignments(null));
    }

    /**
     * Initializes the db assignments
     *
     * @throws OXException
     */
    @Override
    public void reload() throws OXException {
        List<Assignment> pools = readPools();
        if (pools.isEmpty()) {
            LOG.info("Cannot find any database assignment. Services that make use of the AssignmentFactory won't work!");
        }
        assignmentsRef.set(new Assignments(pools));
    }

    private List<Assignment> readPools() throws OXException {
        Connection readOnly = this.databaseService.getReadOnly();

        int readPoolId = 0;
        int writePoolId = 0;

        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = readOnly.prepareStatement("SELECT dc.write_db_pool_id, dc.read_db_pool_id, dp.name FROM db_cluster dc INNER JOIN db_pool dp ON dc.write_db_pool_id = dp.db_pool_id");
            result = stmt.executeQuery();
            if (false == result.next()) {
                return Collections.emptyList();
            }

            List<Assignment> lAssignments = new LinkedList<>();
            int serverId = Server.getServerId();
            do {
                writePoolId = result.getInt(1);
                readPoolId = result.getInt(2);
                String databaseName = result.getString(3);
                if (readPoolId == 0) {
                    readPoolId = writePoolId;
                }

                List<SchemaAndContext> list = get(writePoolId, serverId, readOnly);
                if (null == list) {
                    // This is an assignment w/o a context_server2db_pool affiliation.
                    // Expect the database name to be the actual schema name
                    AssignmentImpl assignmentImpl = new AssignmentImpl(0, serverId, readPoolId, writePoolId, databaseName);
                    lAssignments.add(assignmentImpl);
                    LOG.debug("Found assignment and added to pool: {}", assignmentImpl);
                } else {
                    // An assignment with one or more context_server2db_pool affiliations.
                    for (SchemaAndContext schemaAndContext : list) {
                        AssignmentImpl assignmentImpl = new AssignmentImpl(schemaAndContext.contextId, serverId, readPoolId, writePoolId, schemaAndContext.schemaName);
                        lAssignments.add(assignmentImpl);
                        LOG.debug("Found assignment and added to pool: {}", assignmentImpl);
                    }
                }
            } while (result.next());
            return lAssignments;
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(result, stmt);
            databaseService.backReadOnly(readOnly);
        }
    }

    /**
     * Gets the list of schemas (accompanied by the identifier of any context in that schema).
     *
     * @param poolId The pool identifier
     * @param serverId The server identifier
     * @param con The connection to use
     * @return The list of schemas or <code>null</code>
     * @throws OXException If schemas cannot be listed
     */
    private List<SchemaAndContext> get(int poolId, int serverId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT db_schema, MIN(cid) FROM context_server2db_pool WHERE server_id=1 AND write_db_pool_id=1996 GROUP BY db_schema");
            stmt.setInt(1, serverId);
            stmt.setInt(2, poolId);
            result = stmt.executeQuery();

            if (false == result.next()) {
                // Not context related DBs like: global, guard and shard
                return null;
            }

            List<SchemaAndContext> list = new LinkedList<>();
            do {
                list.add(new SchemaAndContext(result.getString(1), result.getInt(2)));
            } while (result.next());
            return list;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Assignment get(int contextId) {
        try {
            return databaseService.getAssignment(contextId);
        } catch (OXException e) {
            LOG.warn("Found no assignment for context {}", contextId, e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Assignment get(String schemaName) {
        if (null == schemaName) {
            // Garbage in, garbage out...
            return null;
        }

        Assignments assignments = assignmentsRef.get();
        List<Assignment> candidates = assignments.getForSchema(schemaName);
        if (null == candidates) {
            LOG.warn("Found no assignment for schema name {}", schemaName);
            return null;
        }
        if (candidates.size() > 1) {
            LOG.warn("Found duplicate assignments for schema name {}", schemaName, new Throwable("Found duplicate assignments"));
        }
        return candidates.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Assignment get(int poolId, boolean write) {
        Assignments assignments = assignmentsRef.get();
        List<Assignment> candidates = assignments.getForPoolId(poolId, write);
        if (null == candidates) {
            LOG.warn("Found no assignment for {} pool {}.", write ? "read-write" : "read-only", poolId);
            return null;
        }
        if (candidates.size() > 1) {
            LOG.warn("Found duplicate assignments for {} pool {}.", write ? "read-write" : "read-only", poolId, new Throwable("Found duplicate assignments"));
        }
        return candidates.get(0);
    }

    // -------------------------------------------------------------------------------------------------------

    private static final class SchemaAndContext {

        final int contextId;
        final String schemaName;

        SchemaAndContext(String schemaName, int contextId) {
            super();
            this.schemaName = schemaName;
            this.contextId = contextId;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(32);
            builder.append('{');
            if (schemaName != null) {
                builder.append("schemaName=").append(schemaName).append(", ");
            }
            builder.append("contextId=").append(contextId);
            builder.append('}');
            return builder.toString();
        }
    }

    private static final class Assignments {

        private final Map<String, List<Assignment>> assignmentsPerSchema;
        private final Map<Integer, List<Assignment>> assignmentsPerWritePool;
        private final Map<Integer, List<Assignment>> assignmentsPerReadPool;

        Assignments(List<Assignment> assignments) {
            super();
            int size;
            if (null == assignments || (size = assignments.size()) <= 0) {
                assignmentsPerSchema    = Collections.emptyMap();
                assignmentsPerWritePool = Collections.emptyMap();
                assignmentsPerReadPool  = Collections.emptyMap();
            } else {
                Map<String, List<Assignment>> assignmentsPerSchema = new HashMap<>(size);
                Map<Integer, List<Assignment>> assignmentsPerWritePool = new HashMap<>(size);
                Map<Integer, List<Assignment>> assignmentsPerReadPool = new HashMap<>(size);
                for (Assignment assignment : assignments) {
                    List<Assignment> list = assignmentsPerSchema.get(assignment.getSchema());
                    if (null == list) {
                        list = new LinkedList<>();
                        assignmentsPerSchema.put(assignment.getSchema(), list);
                    }
                    list.add(assignment);

                    Integer key = Integer.valueOf(assignment.getWritePoolId());
                    list = assignmentsPerWritePool.get(key);
                    if (null == list) {
                        list = new LinkedList<>();
                        assignmentsPerWritePool.put(key, list);
                    }
                    list.add(assignment);

                    key = Integer.valueOf(assignment.getReadPoolId());
                    list = assignmentsPerReadPool.get(key);
                    if (null == list) {
                        list = new LinkedList<>();
                        assignmentsPerReadPool.put(key, list);
                    }
                    list.add(assignment);
                }

                // Yield appropriate immutable collections for thread-safe read-only access
                this.assignmentsPerSchema       = asImmutableMap(assignmentsPerSchema);
                this.assignmentsPerWritePool    = asImmutableMap(assignmentsPerWritePool);
                this.assignmentsPerReadPool     = asImmutableMap(assignmentsPerReadPool);
            }
        }

        private <K, V> Map<K, List<V>> asImmutableMap(Map<K, List<V>> source) {
            Builder<K, List<V>> builder = ImmutableMap.<K, List<V>> builder();
            for (Map.Entry<K, List<V>> entry : source.entrySet()) {
                builder.put(entry.getKey(), asImmutableList(entry.getValue()));
            }
            return builder.build();
        }

        private <V> List<V> asImmutableList(List<V> source) {
            return ImmutableList.<V> builder().addAll(source).build();
        }

        List<Assignment> getForSchema(String schema) {
            return assignmentsPerSchema.get(schema);
        }

        List<Assignment> getForPoolId(int poolId, boolean write) {
            return write ? assignmentsPerWritePool.get(Integer.valueOf(poolId)) : assignmentsPerReadPool.get(Integer.valueOf(poolId));
        }
    }

}
