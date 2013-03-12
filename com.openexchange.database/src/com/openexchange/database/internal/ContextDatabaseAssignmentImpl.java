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
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.database.Assignment;
import com.openexchange.database.ConfigDatabaseService;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * Reads assignments from the database, maybe stores them in a cache for faster access.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ContextDatabaseAssignmentImpl implements ContextDatabaseAssignmentService {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContextDatabaseAssignmentImpl.class));

    private static final String SELECT = "SELECT read_db_pool_id,write_db_pool_id,db_schema FROM context_server2db_pool WHERE server_id=? AND cid=?";
    private static final String INSERT = "INSERT INTO context_server2db_pool (server_id,cid,read_db_pool_id,write_db_pool_id,db_schema) VALUES (?,?,?,?,?)";

    private final ConfigDatabaseService configDatabaseService;

    private static final String CACHE_NAME = "OXDBPoolCache";

    private CacheService cacheService;

    private Cache cache;

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
        AssignmentImpl retval;
        if (null == cache) {
            retval = loadAssignment(contextId);
        } else {
            final CacheKey key = cacheService.newCacheKey(contextId, Server.getServerId());
            cacheLock.lock();
            try {
                retval = (AssignmentImpl) cache.get(key);
                if (null == retval) {
                    retval = loadAssignment(contextId);
                    try {
                        cache.putSafe(key, retval);
                    } catch (final OXException e) {
                        LOG.error("Cannot put database assignment into cache.", e);
                    }
                }
            } finally {
                cacheLock.unlock();
            }
        }
        return retval;
    }

    private AssignmentImpl loadAssignment(final int contextId) throws OXException {
        AssignmentImpl retval = null;
        final Connection con = configDatabaseService.getReadOnly();
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
                throw DBPoolingExceptionCodes.RESOLVE_FAILED.create(I(contextId), I(Server.getServerId()));
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            configDatabaseService.backReadOnly(con);
        }
        return retval;
    }

    private void writeAssignmentDB(Connection con, Assignment assign) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT);
            int pos = 1;
            stmt.setInt(pos++, assign.getServerId());
            stmt.setInt(pos++, assign.getContextId());
            stmt.setInt(pos++, assign.getReadPoolId());
            stmt.setInt(pos++, assign.getWritePoolId());
            stmt.setString(pos++, assign.getSchema());
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
        if (null != cache) {
            final CacheKey key = cacheService.newCacheKey(assign.getContextId(), assign.getServerId());
            cacheLock.lock();
            try {
                try {
                    cache.putSafe(key, new AssignmentImpl(assign));
                } catch (OXException e) {
                    LOG.error("Cannot put database assignment into cache.", e);
                }
            } finally {
                cacheLock.unlock();
            }
        }
        writeAssignmentDB(con, assign);
    }

    @Override
    public void removeAssignments(final int contextId) {
        if (null != cache) {
            try {
                cache.remove(cache.newCacheKey(contextId, Server.getServerId()));
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    void setCacheService(final CacheService service) {
        this.cacheService = service;
        try {
            this.cache = service.getCache(CACHE_NAME);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void removeCacheService() {
        this.cacheService = null;
        if (null != cache) {
            try {
                cache.clear();
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
            cache = null;
        }
    }
}
