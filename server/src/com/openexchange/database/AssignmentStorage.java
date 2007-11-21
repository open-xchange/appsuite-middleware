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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.database;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import com.openexchange.cache.CacheKey;
import com.openexchange.cache.impl.Configuration;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.configuration.SystemConfig.Property;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.DBPoolingException.Code;

/**
 * Reads assignments from the database, maybe stores them in a cache for faster
 * access and contains the static assignment for the config DB.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class AssignmentStorage {

    private static final AssignmentStorage singleton = new AssignmentStorage();

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(AssignmentStorage.class);

    private static final String SELECT = "SELECT "
        + "read_db_pool_id,write_db_pool_id,db_schema "
        + "FROM context_server2db_pool "
        + "WHERE server_id=? AND cid=?";

    private static final String CACHE_NAME = "OXDBPoolCache";

    private Assignment configDB;

    private JCS cache;

    /**
     * Lock for the cache.
     */
    private final Lock cacheLock = new ReentrantLock(true);

    /**
     * Prevent instantiation
     */
    private AssignmentStorage() {
        super();
    }

    /**
     * Gets a database assignment for a context. If the cache is enabled this
     * method looks into the cache for the assignment and loads it from the
     * database if cache is disabled or the cache doesn't contain the entry.
     * @param contextId unique identifier of the context.
     * @return the assignment.
     * @throws DBPoolingException if getting the assignment fails.
     */
    Assignment getAssignment(final int contextId)
        throws DBPoolingException {
        Assignment retval;
        if (null == cache) {
            retval = loadAssignment(contextId);
        } else {
            final CacheKey key = new CacheKey(contextId, Integer.valueOf(Server.getServerId()));
            cacheLock.lock();
            try {
                retval = (Assignment) cache.get(key);
                if (null == retval) {
                    retval = loadAssignment(contextId);
                    try {
                        cache.putSafe(key, retval);
                    } catch (CacheException e) {
                        LOG.error("Cannot put database assignment into cache.",
                            e);
                    }
                }
            } finally {
                cacheLock.unlock();
            }
        }
        return retval;
    }

    private Assignment loadAssignment(final int contextId)
        throws DBPoolingException {
        Assignment retval = null;
        final Connection con = Database.get(false);
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT);
            stmt.setInt(1, Server.getServerId());
            stmt.setInt(2, contextId);
            result = stmt.executeQuery();
            if (result.next()) {
                int pos = 1;
                retval = new Assignment(
                    contextId,
                    Server.getServerId(),
                    result.getInt(pos++),
                    result.getInt(pos++),
                    result.getString(pos++));
            } else {
                throw new DBPoolingException(Code.RESOLVE_FAILED, Integer.valueOf(contextId),
                		Integer.valueOf(Server.getServerId()));
            }
        } catch (SQLException e) {
            throw new DBPoolingException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            Database.back(false, con);
        }
        return retval;
    }

    public List<Integer> listContexts(final int poolid) throws DBPoolingException {
        final List<Integer> retval = new ArrayList<Integer>();
        final Connection con = Database.get(false);
        // TODO optimize this bad query
        final String getcid = "SELECT cid FROM context_server2db_pool "
            + "WHERE read_db_pool_id=? OR write_db_pool_id=?";
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(getcid);
            stmt.setInt(1, poolid);
            stmt.setInt(2, poolid);
            result = stmt.executeQuery();
            while (result.next()) {
                retval.add(Integer.valueOf(result.getInt(1)));
            }
        } catch (SQLException e) {
            throw new DBPoolingException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            Database.back(false, con);
        }
        return retval;
    }

    /**
     * Invalidates an assignment for a context in the cache.
     * @param contextId unique identifier of the context.
     * @throws DBPoolingException if getting the server identifier fails.
     */
    public void removeAssignments(final int contextId) throws DBPoolingException {
        if (null != cache) {
            try {
                cache.remove(new CacheKey(contextId, Integer.valueOf(Server.getServerId())));
            } catch (CacheException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    Assignment getConfigDBAssignment() {
        return configDB;
    }

    public static AssignmentStorage getInstance() {
        return singleton;
    }

    public void start() throws DBPoolingException {
        if (null != configDB) {
            LOG.error("Duplicate AssignmentStorage initialization.");
            return;
        }
        configDB = new Assignment(0, 0, Pools.CONFIGDB_READ_ID,
            Pools.CONFIGDB_WRITE_ID, null);
        if (Boolean.parseBoolean(SystemConfig.getProperty(Property.CACHE))) {
            try {
                Configuration.load();
                cache = JCS.getInstance(CACHE_NAME);
            } catch (CacheException e) {
                throw new DBPoolingException(Code.NOT_INITIALIZED, e,
                    CACHE_NAME);
            } catch (ConfigurationException e) {
                throw new DBPoolingException(Code.NOT_INITIALIZED, e,
                    CACHE_NAME);
            }
        }
        
    }

    public void stop() {
        if (null != cache) {
            try {
                cache.clear();
            } catch (CacheException e) {
                LOG.error("Problem clearing the cache.", e);
            }
            cache = null;
        }
        configDB = null;
    }
}
