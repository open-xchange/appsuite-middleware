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
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
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
import com.openexchange.exception.OXException;

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
    private static final String CONTEXTS_IN_DATABASE = "SELECT cid FROM context_server2db_pool WHERE read_db_pool_id=? OR write_db_pool_id=?";
    private static final String NOTFILLED = "SELECT db_schema,COUNT(db_schema) AS count FROM context_server2db_pool WHERE write_db_pool_id=? GROUP BY db_schema HAVING count<? ORDER BY count ASC";

    private final ConfigDatabaseService configDatabaseService;

    private static final String CACHE_NAME = "OXDBPoolCache";

    private volatile CacheService cacheService;

    private volatile Cache cache;

    /**
     * Lock for the cache.
     */
    private final Lock cacheLock = new ReentrantLock(true);

    /**
     * Default constructor.
     */
    public ContextDatabaseAssignmentImpl(final ConfigDatabaseService configDatabaseService) {
        super();
        this.configDatabaseService = configDatabaseService;
    }

    @Override
    public AssignmentImpl getAssignment(final int contextId) throws OXException {
        CacheService myCacheService = this.cacheService;
        Cache myCache = this.cache;

        // Check cache references
        if (null == myCache || null == myCacheService) {
            // No cache available
            return loadAssignment(contextId);
        }

        // Use that cache
        CacheKey key = myCacheService.newCacheKey(contextId, Server.getServerId());
        Object object = myCache.get(key);
        if (object instanceof AssignmentImpl) {
            return (AssignmentImpl) object;
        }

        // Need to load - synchronously!
        cacheLock.lock();
        try {
            AssignmentImpl retval = (AssignmentImpl) myCache.get(key);
            if (null == retval) {
                retval = loadAssignment(contextId);
                try {
                    myCache.putSafe(key, retval);
                } catch (OXException e) {
                    LOG.error("Cannot put database assignment into cache.", e);
                }
            }
            return retval;
        } finally {
            cacheLock.unlock();
        }
    }

    private static AssignmentImpl loadAssignment(Connection con, int contextId) throws OXException {
        final AssignmentImpl retval;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT);
            stmt.setInt(1, Server.getServerId());
            stmt.setInt(2, contextId);
            result = stmt.executeQuery();
            if (result.next()) {
                int pos = 1;
                retval = new AssignmentImpl(contextId, Server.getServerId(), result.getInt(pos++), result.getInt(pos++),
                        result.getString(pos++));
            } else {
                retval = null;
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return retval;
    }

    private AssignmentImpl loadAssignment(final int contextId) throws OXException {
        final AssignmentImpl retval;
        final Connection con = configDatabaseService.getReadOnly();
        try {
            retval = loadAssignment(con, contextId);
        } finally {
            configDatabaseService.backReadOnly(con);
        }
        if (null == retval) {
            throw DBPoolingExceptionCodes.RESOLVE_FAILED.create(I(contextId), I(Server.getServerId()));
        }
        return retval;
    }

    private static void writeAssignmentDB(Connection con, Assignment assign, boolean update) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(update ? UPDATE : INSERT);
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
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void writeAssignment(Connection con, Assignment assign) throws OXException {
        boolean update = assign instanceof AssignmentInsertData ? false : null != loadAssignment(con, assign.getContextId());
        Cache myCache = this.cache;
        if (null != myCache) {
            final CacheKey key = myCache.newCacheKey(assign.getContextId(), assign.getServerId());
            cacheLock.lock();
            try {
                if (update) {
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
        writeAssignmentDB(con, assign, update);
    }

    private static void deleteAssignmentDB(Connection con, int contextId) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(DELETE);
            stmt.setInt(1, contextId);
            stmt.setInt(2, Server.getServerId());
            stmt.executeUpdate();
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

    private static int[] listContexts(Connection con, int poolId) throws OXException {
        final List<Integer> tmp = new LinkedList<Integer>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(CONTEXTS_IN_DATABASE);
            stmt.setInt(1, poolId);
            stmt.setInt(2, poolId);
            result = stmt.executeQuery();
            while (result.next()) {
                tmp.add(I(result.getInt(1)));
            }
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        final int[] retval = new int[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            retval[i] = tmp.get(i).intValue();
        }
        return retval;
    }

    @Override
    public int[] getContextsInDatabase(int poolId) throws OXException {
        final Connection con = configDatabaseService.getReadOnly();
        try {
            return listContexts(con, poolId);
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
            List<String> retval = new LinkedList<String>();
            while (result.next()) {
                String schema = result.getString(1);
                int count = result.getInt(2);
                LOG.debug("schema {} is filled with {} contexts.", schema, I(count));
                retval.add(schema);
            }
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
            Map<String, Integer> retval = new LinkedHashMap<String, Integer>(32, 0.9F);
            while (result.next()) {
                String schema = result.getString(1);
                int count = result.getInt(2);
                LOG.debug("schema {} is filled with {} contexts.", schema, I(count));
                retval.put(schema, I(count));
            }
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
            stmt = con.prepareStatement("SELECT COUNT(*) FROM context_server2db_pool WHERE write_db_pool_id=? FOR UPDATE");
            stmt.setInt(1, writePoolId);
            stmt.execute();
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
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
