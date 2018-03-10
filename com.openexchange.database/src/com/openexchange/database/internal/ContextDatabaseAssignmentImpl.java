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
import static com.openexchange.java.Autoboxing.I;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.database.Assignment;
import com.openexchange.database.AssignmentInsertData;
import com.openexchange.database.ConfigDatabaseService;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.lock.AccessControl;
import com.openexchange.lock.LockService;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;

/**
 * Reads assignments from the database, maybe stores them in a cache for faster access.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ContextDatabaseAssignmentImpl implements ContextDatabaseAssignmentService {

    private static final Logger LOG = LoggerFactory.getLogger(ContextDatabaseAssignmentImpl.class);

    private static final String SELECT = "SELECT read_db_pool_id,write_db_pool_id,db_schema FROM context_server2db_pool WHERE server_id=? AND cid=?";
    private static final String INSERT = "INSERT INTO context_server2db_pool (read_db_pool_id,write_db_pool_id,db_schema,server_id,cid) VALUES (?,?,?,?,?)";
    private static final String UPDATE = "UPDATE context_server2db_pool SET read_db_pool_id=?,write_db_pool_id=?,db_schema=? WHERE server_id=? AND cid=?";
    private static final String DELETE = "DELETE FROM context_server2db_pool WHERE cid=? AND server_id=?";
    private static final String CONTEXTS_IN_SCHEMA = "SELECT cid FROM context_server2db_pool WHERE server_id=? AND write_db_pool_id=? AND db_schema=?";
    private static final String NOTFILLED = "SELECT schemaname,count FROM contexts_per_dbschema WHERE db_pool_id=? AND count<? ORDER BY count ASC";

    private static final String CACHE_NAME = "OXDBPoolCache";

    private static class CacheLockHolder {
        // Wrapper class to initialize only when needed
        static final CacheLock fallbackGlobalCacheLock = CacheLock.cacheLockFor(new ReentrantLock(true));
    }

    private final ConfigDatabaseService configDatabaseService;
    private volatile CacheService cacheService;
    private volatile Cache cache;
    private final LockMech lockMech;

    /**
     * Default constructor.
     */
    public ContextDatabaseAssignmentImpl(final ConfigDatabaseService configDatabaseService, LockMech lockMech) {
        super();
        this.configDatabaseService = configDatabaseService;
        this.lockMech = lockMech;
    }

    private CacheLock cacheLockFor(int contextId) throws OXException {
        LockService lockService = Initialization.getLockService();
        if (null == lockService) {
            return CacheLockHolder.fallbackGlobalCacheLock;
        }

        AccessControl accessControl = lockService.getAccessControlFor(new StringBuilder(16).append(contextId).append('-').append("dbassign").toString(), 1, 1, contextId);
        return CacheLock.cacheLockFor(accessControl);
    }

    @Override
    public AssignmentImpl getAssignment(final int contextId) throws OXException {
        return getAssignment(null, contextId, true);
    }

    /**
     * Gets the assignment for specified context.
     *
     * @param con The (optional) connection to use
     * @param contextId The context identifier
     * @param errorOnAbsence <code>true</code> to throw an error in case no such assignment exists; otherwise <code>false</code>
     * @return The assignment or <code>null</code> (if parameter <code>errorOnAbsence</code> has been set to <code>false</code>)
     * @throws OXException If loading the assignment fails or no such assignment exists (if parameter <code>errorOnAbsence</code> has been set to <code>true</code>)
     */
    private AssignmentImpl getAssignment(Connection con, int contextId, boolean errorOnAbsence) throws OXException {
        CacheService myCacheService = this.cacheService;
        Cache myCache = this.cache;

        // Check cache references
        if (null == myCache || null == myCacheService) {
            // No cache available
            return loadAssignment(con, contextId, errorOnAbsence);
        }

        // Use that cache
        CacheKey key = myCacheService.newCacheKey(contextId, Server.getServerId());
        Object object = myCache.get(key);
        if (object instanceof AssignmentImpl) {
            return (AssignmentImpl) object;
        }

        // Need to load - synchronously!
        CacheLock cacheLock = cacheLockFor(contextId);
        cacheLock.lock();
        try {
            object = myCache.get(key);
            if (object instanceof AssignmentImpl) {
                // Loaded meanwhile
                return (AssignmentImpl) object;
            }

            AssignmentImpl loaded = loadAssignment(con, contextId, errorOnAbsence);
            try {
                myCache.putSafe(key, loaded);
            } catch (OXException e) {
                LOG.error("Cannot put database assignment into cache.", e);
            }
            return loaded;
        } finally {
            cacheLock.unlock();
        }
    }

    private AssignmentImpl loadAssignment(Connection con, int contextId) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT);
            stmt.setInt(1, Server.getServerId());
            stmt.setInt(2, contextId);
            result = stmt.executeQuery();

            if (false == result.next()) {
                return null;
            }

            int pos = 1;
            return new AssignmentImpl(contextId, Server.getServerId(), result.getInt(pos++), result.getInt(pos++), result.getString(pos++));
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private AssignmentImpl loadAssignment(Connection conn, int contextId, boolean errorOnAbsence) throws OXException {
        Connection con = conn;
        if (null == con) {
            con = configDatabaseService.getReadOnly();
            try {
                return loadAndCheck(con, contextId, errorOnAbsence);
            } finally {
                configDatabaseService.backReadOnly(con);
            }
        }

        return loadAndCheck(con, contextId, errorOnAbsence);
    }

    private AssignmentImpl loadAndCheck(Connection con, int contextId, boolean errorOnAbsence) throws OXException {
        AssignmentImpl retval = loadAssignment(con, contextId);
        if (errorOnAbsence && null == retval) {
            throw DBPoolingExceptionCodes.RESOLVE_FAILED.create(I(contextId), I(Server.getServerId()));
        }
        return retval;
    }

    private void writeNewAssignmentDB(Connection con, Assignment assign, boolean updateCounters) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT);
            int pos = 1;
            stmt.setInt(pos++, assign.getReadPoolId());
            stmt.setInt(pos++, assign.getWritePoolId());
            stmt.setString(pos++, assign.getSchema());
            stmt.setInt(pos++, assign.getServerId());
            stmt.setInt(pos++, assign.getContextId());
            int count = stmt.executeUpdate();
            if (1 != count) {
                throw DBPoolingExceptionCodes.INSERT_FAILED.create(I(assign.getContextId()), I(assign.getServerId()));
            }
            Databases.closeSQLStuff(stmt);

            if (updateCounters) {
                updateCountTables(con, assign.getWritePoolId(), assign.getSchema(), true);
            }
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void writeExistingAssignmentDB(Connection con, Assignment assign, AssignmentImpl oldAssign, boolean updateCounters) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(UPDATE);
            int pos = 1;
            stmt.setInt(pos++, assign.getReadPoolId());
            stmt.setInt(pos++, assign.getWritePoolId());
            stmt.setString(pos++, assign.getSchema());
            stmt.setInt(pos++, assign.getServerId());
            stmt.setInt(pos++, assign.getContextId());
            int count = stmt.executeUpdate();
            if (1 != count) {
                throw DBPoolingExceptionCodes.INSERT_FAILED.create(I(assign.getContextId()), I(assign.getServerId()));
            }
            Databases.closeSQLStuff(stmt);

            if (updateCounters) {
                int oldPoolId = oldAssign.getWritePoolId();
                int newPoolId = assign.getWritePoolId();

                if (oldPoolId != newPoolId) {
                    updateCountTables(con, oldPoolId, oldAssign.getSchema(), false);
                    updateCountTables(con, newPoolId, assign.getSchema(), true);
                } else {
                    String oldSchema = oldAssign.getSchema();
                    String newSchema = assign.getSchema();
                    if (false == oldSchema.equals(newSchema)) {
                        updateSchemaCountTable(con, oldPoolId, oldSchema, false);
                        updateSchemaCountTable(con, oldPoolId, newSchema, true);
                    }
                }
            }
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void updateCountTables(Connection con, int poolId, String schemaName, boolean increment) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE contexts_per_dbpool SET count=count" + (increment ? '+' : '-') + "1 WHERE db_pool_id=?");
            stmt.setInt(1, poolId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            updateSchemaCountTable(con, poolId, schemaName, increment);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void updateSchemaCountTable(Connection con, int poolId, String schemaName, boolean increment) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE contexts_per_dbschema SET count=count" + (increment ? '+' : '-') + "1 WHERE db_pool_id=? AND schemaname=?");
            stmt.setInt(1, poolId);
            stmt.setString(2, schemaName);
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void writeAssignment(Connection con, Assignment assign) throws OXException {
        // Create or update...
        boolean updateCounters = true;
        AssignmentImpl oldAssign;
        if (assign instanceof AssignmentInsertData) {
            oldAssign = null;
            updateCounters = ((AssignmentInsertData) assign).updateDatabaseCounters();
        } else {
            oldAssign = getAssignment(con, assign.getContextId(), false);
        }

        // Update cache
        Cache myCache = this.cache;
        if (null != myCache) {
            final CacheKey key = myCache.newCacheKey(assign.getContextId(), assign.getServerId());
            CacheLock cacheLock = cacheLockFor(assign.getContextId());
            cacheLock.lock();
            try {
                if (null != oldAssign) {
                    myCache.remove(key);
                }
                AssignmentImpl cacheValue = new AssignmentImpl(assign);
                try {
                    myCache.putSafe(key, cacheValue);
                } catch (OXException e) {
                    // Already present...
                    LOG.debug("Cannot put database assignment into cache.", e);
                    myCache.remove(key);
                    myCache.putSafe(key, cacheValue);
                }
            } finally {
                cacheLock.unlock();
            }
        }

        // Create/update assignment
        if (null == oldAssign) {
            writeNewAssignmentDB(con, assign, updateCounters);
        } else {
            writeExistingAssignmentDB(con, assign, oldAssign, updateCounters);
        }
    }

    private void deleteAssignmentDB(Connection con, int contextId) throws OXException {
        AssignmentImpl assignment = getAssignment(con, contextId, false);
        if (null == assignment) {
            // No such assignment, hence no need for deletion
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(DELETE);
            stmt.setInt(1, contextId);
            stmt.setInt(2, Server.getServerId());
            stmt.executeUpdate();
            closeSQLStuff(stmt);

            updateCountTables(con, assignment.getWritePoolId(), assignment.getSchema(), false);
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void invalidateAssignment(int... contextIds) {
        Cache myCache = this.cache;
        if (null != myCache) {
            try {
                int serverId = Server.getServerId();
                if (contextIds != null && contextIds.length > 0) {
                    List<Serializable> keys = new ArrayList<Serializable>(contextIds.length);
                    for (int contextId : contextIds) {
                        keys.add(myCache.newCacheKey(contextId, serverId));
                    }
                    myCache.remove(keys);
                }
            } catch (final OXException e) {
                LOG.error("Error while removing database assignment from cache.", e);
            }
        }
    }

    @Override
    public void deleteAssignment(Connection con, int contextId) throws OXException {
        deleteAssignmentDB(con, contextId);
        invalidateAssignment(contextId);
    }

    @Override
    public int[] getContextsFromSchema(Connection con, int writePoolId, String schema) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(CONTEXTS_IN_SCHEMA);
            stmt.setInt(1, Server.getServerId());
            stmt.setInt(2, writePoolId);
            stmt.setString(3, schema);
            rs = stmt.executeQuery();
            final TIntList tmp = new TIntLinkedList();
            while (rs.next()) {
                tmp.add(rs.getInt(1));
            }
            return tmp.toArray();
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static int[] listContexts(Connection con, int poolId, int offset, int length) throws OXException {
        boolean withLimit = true;
        if (offset < 0 || length < 0) {
            withLimit = false;
        }
        if (withLimit && length < 0) {
            throw OXException.general("Invalid length: " + length);
        }
        if (withLimit && (offset + length) < 0) {
            throw OXException.general("Invalid offset/length: " + offset + ", " + length);
        }
        if (length == 0) {
            return new int[0];
        }

        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            // Get the identifier of the read-write pool for given database pool identifier
            stmt = con.prepareStatement("SELECT write_db_pool_id FROM db_cluster WHERE read_db_pool_id=? OR write_db_pool_id=?");
            stmt.setInt(1, poolId);
            stmt.setInt(2, poolId);
            result = stmt.executeQuery();
            if (false == result.next()) {
                // No such database known
                return new int[0];
            }

            int writePoolId = result.getInt(1);
            closeSQLStuff(result, stmt);
            result = null;
            stmt = null;

            if (withLimit) {
                stmt = con.prepareStatement("SELECT cid FROM context_server2db_pool WHERE write_db_pool_id=? ORDER BY cid LIMIT " + offset + ", " + length);
            } else {
                stmt = con.prepareStatement("SELECT cid FROM context_server2db_pool WHERE write_db_pool_id=?");
            }
            stmt.setInt(1, writePoolId);
            result = stmt.executeQuery();
            if (false == result.next()) {
                return new int[0];
            }

            TIntList tmp = length > 0 ? new TIntArrayList(length) : new TIntArrayList(2048);
            do {
                tmp.add(result.getInt(1));
            } while (result.next());
            return tmp.toArray();
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    @Override
    public int[] getContextsInDatabase(int poolId, int offset, int length) throws OXException {
        final Connection con = configDatabaseService.getReadOnly();
        try {
            return listContexts(con, poolId, offset, length);
        } finally {
            configDatabaseService.backReadOnly(con);
        }
    }

    @Override
    public String[] getUnfilledSchemas(Connection con, int poolId, int maxContexts) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(NOTFILLED);
            stmt.setInt(1, poolId);
            stmt.setInt(2, maxContexts);
            result = stmt.executeQuery();
            if (false == result.next()) {
                return new String[0];
            }

            List<String> retval = new LinkedList<String>();
            do {
                String schema = result.getString(1);
                int count = result.getInt(2);
                LOG.debug("schema {} is filled with {} contexts.", schema, I(count));
                retval.add(schema);
            } while (result.next());
            return retval.toArray(new String[retval.size()]);
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    @Override
    public Map<String, Integer> getContextCountPerSchema(Connection con, int poolId, int maxContexts) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(NOTFILLED);
            stmt.setInt(1, poolId);
            stmt.setInt(2, maxContexts);
            result = stmt.executeQuery();
            if (false == result.next()) {
                return Collections.emptyMap();
            }

            Map<String, Integer> retval = new LinkedHashMap<String, Integer>(32, 0.9F);
            do {
                String schema = result.getString(1);
                int count = result.getInt(2);
                LOG.debug("schema {} is filled with {} contexts.", schema, I(count));
                retval.put(schema, I(count));
            } while (result.next());
            return retval;
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    @Override
    public void lock(Connection con, int writePoolId) throws OXException {
        PreparedStatement stmt = null;
        try {
            switch (lockMech) {
                case GLOBAL_LOCK: {
                    stmt = con.prepareStatement("UPDATE ctx_per_schema_sem SET id=id+1");
                    stmt.executeUpdate();
                    break;
                }
                case ROW_LOCK: {
                    stmt = con.prepareStatement("SELECT 1 FROM dbpool_lock WHERE db_pool_id=? FOR UPDATE");
                    stmt.setInt(1, writePoolId);
                    stmt.executeQuery();
                    break;
                }
            }
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public Map<String, Integer> getAllSchemata(Connection con) throws OXException {
        // Get all available database clusters (writeDB to readDB associations)
        Map<Integer, Integer> clusters;
        {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement("SELECT write_db_pool_id, read_db_pool_id FROM db_cluster");
                rs = stmt.executeQuery();
                if (false == rs.next()) {
                    return Collections.emptyMap();
                }

                clusters = new LinkedHashMap<>();
                do {
                    int writeDbId = rs.getInt(1);
                    int readDbId = rs.getInt(2);
                    clusters.put(Integer.valueOf(writeDbId), Integer.valueOf(readDbId <= 0 ? writeDbId : readDbId));
                } while (rs.next());
            } catch (SQLException e) {
                throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(rs, stmt);
            }
        }

        // Iterate database clusters
        int serverId = Server.getServerId();
        Map<String, Integer> allSchemas = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> clusterEntry : clusters.entrySet()) {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
                stmt = con.prepareStatement("SELECT db_schema FROM context_server2db_pool WHERE write_db_pool_id=? AND server_id=? GROUP by db_schema");
                stmt.setInt(1, clusterEntry.getKey().intValue());
                stmt.setInt(2, serverId);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    allSchemas.put(rs.getString(1), clusterEntry.getValue());
                }
            } catch (SQLException e) {
                throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(rs, stmt);
            }
        }
        return allSchemas;
    }

    void setCacheService(final CacheService service) {
        this.cacheService = service;
        try {
            this.cache = service.getCache(CACHE_NAME);
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    void removeCacheService() {
        this.cacheService = null;
        Cache myCache = this.cache;
        if (null != myCache) {
            try {
                myCache.clear();
            } catch (final OXException e) {
                LOG.error("", e);
            }
            this.cache = null;
        }
    }
}
