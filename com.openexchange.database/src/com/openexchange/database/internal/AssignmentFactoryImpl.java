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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.database.Assignment;
import com.openexchange.database.AssignmentFactory;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
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

    private static final String GET_POOL_MAPPING = "SELECT dc.write_db_pool_id, dc.read_db_pool_id, dp.name FROM db_cluster dc INNER JOIN db_pool dp ON dc.write_db_pool_id = dp.db_pool_id;";

    private static final String CONTEXTS_IN_DATABASE = "SELECT cid FROM context_server2db_pool WHERE read_db_pool_id=? OR write_db_pool_id=?";

    private final List<Assignment> assignments = new CopyOnWriteArrayList<>();

    private final DatabaseService databaseService;

    public AssignmentFactoryImpl(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    /**
     * Initializes the db assignments
     * 
     * @throws OXException
     */
    @Override
    public void reload() throws OXException {
        List<Assignment> readPools = readPools();
        if (readPools.size() == 0) {
            LOG.info("Cannot find any database assignment. Services that make use of the AssignmentFactory won't work!");
        }
        assignments.clear();
        assignments.addAll(readPools);
    }

    private List<Assignment> readPools() throws OXException {
        List<Assignment> lAssignments = new ArrayList<>();
        Connection readOnly = this.databaseService.getReadOnly();

        int readPoolId = 0;
        int writePoolId = 0;

        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = readOnly.prepareStatement(GET_POOL_MAPPING);
            result = stmt.executeQuery();

            int serverId = Server.getServerId();

            while (result.next()) {
                writePoolId = result.getInt("write_db_pool_id");
                readPoolId = result.getInt("read_db_pool_id");
                String schema = result.getString("name");
                if (readPoolId == 0) {
                    readPoolId = writePoolId;
                }
                int context = get(readOnly, writePoolId);
                AssignmentImpl assignmentImpl = new AssignmentImpl(context, serverId, readPoolId, writePoolId, schema);
                lAssignments.add(assignmentImpl);
                LOG.debug("Found assignment and added to pool: {}", assignmentImpl.toString());
            }
            stmt.close();
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(result, stmt);
            databaseService.backReadOnly(readOnly);
        }
        return lAssignments;
    }

    private int get(Connection con, int poolId) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(CONTEXTS_IN_DATABASE);
            stmt.setInt(1, poolId);
            stmt.setInt(2, poolId);
            result = stmt.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            }
            return 0; // non context related dbs like: global, guard and shard
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
        for (Assignment assignment : assignments) {
            if (assignment.getContextId() == contextId) {
                return assignment;
            }
        }
        LOG.warn("Found no assignment for context id {}", contextId);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Assignment get(String schemaName) {
        for (Assignment assignment : assignments) {
            if (assignment.getSchema().equalsIgnoreCase(schemaName)) {
                LOG.debug("Found the following assignment for schema name {}: {}", schemaName, assignment.toString());
                return assignment;
            }
        }
        LOG.warn("Found no assignment for schema name {}", schemaName);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Assignment get(int poolId, boolean write) {
        for (Assignment assignment : assignments) {
            if (write) {
                if (assignment.getWritePoolId() == poolId) {
                    LOG.debug("Found the following assignment for 'write' identified by poolId {}: {}", poolId, assignment.toString());
                    return assignment;
                }
            } else {
                if (assignment.getReadPoolId() == poolId) {
                    LOG.debug("Found the following assignment for identified by poolId {}: {}", poolId, assignment.toString());
                    return assignment;
                }
            }
        }
        LOG.warn("Found no assignment for pool id {} in pool write {}.", poolId, Boolean.toString(write));
        return null;
    }
}
