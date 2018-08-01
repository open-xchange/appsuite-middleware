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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.ConfigDatabaseService;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.pooling.ExhaustedActions;

/**
 * Handles the life cycle of database connection pools for contexts databases.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ContextDatabaseLifeCycle extends AbstractConfigurationListener implements PoolLifeCycle {

    private final static Logger LOGGER = LoggerFactory.getLogger(ContextDatabaseLifeCycle.class);

    private static final String SELECT = "SELECT url,driver,login,password,hardlimit,max,initial FROM db_pool WHERE db_pool_id=?";

    private final ConfigDatabaseService configDatabaseService;

    private final Map<Integer, ConnectionPool> pools = new HashMap<Integer, ConnectionPool>();

    private final Map<Integer, ReentrantLock> locks = new HashMap<Integer, ReentrantLock>();

    private final ReadWriteLock configLock = new ReentrantReadWriteLock(true);

    private Configuration configuration;

    public ContextDatabaseLifeCycle(final Configuration configuration, final Management management, final Timer timer, final ConfigDatabaseService configDatabaseService) {
        super(management, timer);
        this.configDatabaseService = configDatabaseService;
        this.configuration = configuration;
    }

    @Override
    public ConnectionPool create(final int poolId) throws OXException {
        return create(poolId, false);
    }

    private ConnectionPool create(final int poolId, boolean cleanUp) throws OXException {
        final ConnectionData data = loadPoolData(poolId);
        try {
            Class.forName(data.driverClass);
        } catch (final ClassNotFoundException e) {
            throw DBPoolingExceptionCodes.NO_DRIVER.create(e, data.driverClass);
        }
        if (false == cleanUp) {
            // New connection pool
            locks.put(I(poolId), new ReentrantLock(true));
        }// Else; Update case, lock was already created

        final ConnectionPool retval = new ConnectionPool(data.url, data.props, getConfig(data));
        setPool(poolId, cleanUp, locks.get(I(poolId)), retval, (ConnectionPool pool) -> {
            /*
             * Don't destroy pools. Currently the ConnectionPools are still cached in 'Pools'
             * For the transition phase it is okay, that those pools will still be used.
             * Cache will be cleared afterwards and new pools will be used instead. GCC should
             * remove the old unused objects afterwards
             */
            pools.put(I(poolId), pool);
        });

        return retval;
    }

    @Override
    public boolean destroy(final int poolId) {
        ReentrantLock lock = locks.get(I(poolId));
        if (null == lock) {
            return false;
        }
        lock.lock();
        try {
            final ConnectionPool toDestroy = pools.remove(I(poolId));
            if (null == toDestroy) {
                return false;
            }
            management.removePool(poolId);
            timer.removeTask(toDestroy.getCleanerTask());
            toDestroy.destroy();
            return true;
        } finally {
            lock.unlock();
            locks.remove(I(poolId));
        }
    }

    private ConnectionPool.Config getConfig(final ConnectionData data) {
        configLock.readLock().lock();
        try {
            final ConnectionPool.Config retval = configuration.getPoolConfig().clone();
            retval.maxActive = data.max;
            if (data.block) {
                retval.exhaustedAction = ExhaustedActions.BLOCK;
            } else {
                retval.exhaustedAction = ExhaustedActions.GROW;
            }
            return retval;
        } finally {
            configLock.readLock().unlock();
        }
    }

    private void removeParameters(ConnectionData retval) {
        int paramStart = retval.url.indexOf('?');
        if (paramStart != -1) {
            retval.url = retval.url.substring(0, paramStart);
        }
    }

    ConnectionData loadPoolData(final int poolId) throws OXException {
        ConnectionData retval = null;
        final Connection con = configDatabaseService.getReadOnly();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT);
            stmt.setInt(1, poolId);
            result = stmt.executeQuery();
            if (result.next()) {
                retval = new ConnectionData();
                Properties defaults = new Properties();
                retval.props = defaults;
                int pos = 1;
                retval.url = result.getString(pos++);
                retval.driverClass = result.getString(pos++);
                defaults.put("user", result.getString(pos++));
                defaults.put("password", result.getString(pos++));
                retval.block = result.getBoolean(pos++);
                retval.max = result.getInt(pos++);
                retval.min = result.getInt(pos++);
            } else {
                throw DBPoolingExceptionCodes.NO_DBPOOL.create(I(poolId));
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            configDatabaseService.backReadOnly(con);
        }

        removeParameters(retval);
        configLock.readLock().lock();
        try {
            retval.props.putAll(configuration.getJdbcProps());
        } finally {
            configLock.readLock().unlock();
        }
        return retval;
    }

    @Override
    public void notify(Configuration configuration) {
        Set<Integer> ids;
        configLock.writeLock().lock();
        try {
            // No new pools can be added, so we get all current
            this.configuration = configuration;
            ids = new HashSet<>(pools.keySet());
        } finally {
            configLock.writeLock().unlock();
        }

        /*
         * New pools will be initialized with new configuration.
         * We now need to replace all old pools.
         */
        for (Iterator<Integer> iterator = ids.iterator(); iterator.hasNext();) {
            Integer poolId = iterator.next();
            try {
                create(poolId.intValue(), true);
            } catch (OXException e) {
                LOGGER.error("unable to replace pool with id " + poolId.toString(), e);
            }
        }
    }

    @Override
    public int getPriority() {
        /*
         * Due a higher priority then the caching instance we can work in the background
         * and replace all pools. Meanwhile the cache will deliver perfectly usable
         * Connection pools to accessing threads.
         */
        return 25;
    }
}
