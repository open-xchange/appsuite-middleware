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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.configuration.ConfigDB;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ConfigDB.Property;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.ServerTimer;
import com.openexchange.server.DBPoolingException.Code;

/**
 * This class stores all connection pools. It also removes pools that are empty.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Pools implements Runnable {

    private static final String ERR_CANT_UNREGISTER_POOL_MBEAN = "Cannot unregister pool mbean.";

	/**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Pools.class);

    public static final int CONFIGDB_READ_ID = -1;

    public static final int CONFIGDB_WRITE_ID = -2;

    private static ConnectionPool CONFIGDB_READ;

    private static ConnectionPool CONFIGDB_WRITE;

    private static long cleanerInterval = 10000;

    private static final Map<Integer, ConnectionPool> POOLS =
        new HashMap<Integer, ConnectionPool>();

    private static final Lock lock = new ReentrantLock(true);

    /**
     * Prevent instantiation
     */
    private Pools() {
        super();
    }

    /**
     * @return an array with all connection pools.
     */
    static ConnectionPool[] getPools() {
        final List<ConnectionPool> pools = new ArrayList<ConnectionPool>();
        pools.add(CONFIGDB_READ);
        if (CONFIGDB_WRITE != CONFIGDB_READ) {
            pools.add(CONFIGDB_WRITE);
        }
        lock.lock();
        try {
            pools.addAll(POOLS.values());
        } finally {
            lock.unlock();
        }
        return pools.toArray(new ConnectionPool[pools.size()]);
    }

    static ConnectionPool getPool(final int poolId) throws DBPoolingException {
        if (null == CONFIGDB_READ) {
            throw new DBPoolingException(Code.NOT_INITIALIZED,
                Pools.class.getName());
        }
        ConnectionPool retval;
        switch (poolId) {
        case CONFIGDB_READ_ID:
            retval = CONFIGDB_READ;
            break;
        case CONFIGDB_WRITE_ID:
            retval = CONFIGDB_WRITE;
            break;
        default:
            lock.lock();
            try {
                retval = POOLS.get(Integer.valueOf(poolId));
                if (null == retval) {
                    final ConnectionDataStorage.ConnectionData data =
                        ConnectionDataStorage.loadPoolData(poolId);
                    try {
                        Class.forName(data.driverClass);
                    } catch (ClassNotFoundException e) {
                        throw new DBPoolingException(Code.NO_DRIVER, e);
                    }
                    retval = new ConnectionPool(data.url, data.props,
                        getConfig(data));
                    retval.registerCleaner(ServerTimer.getTimer(),
                        cleanerInterval);
                    registerMBean(createMBeanName(poolId), retval);
                    POOLS.put(Integer.valueOf(poolId), retval);
                }
            } finally {
                lock.unlock();
            }
        }
        return retval;
    }

    private static TimerTask cleaner = new TimerTask() {
        @Override
        public void run() {
            try {
                final Thread thread = new Thread(new Pools());
                thread.setName("PoolsCleaner");
                thread.start();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    };

    /**
     * {@inheritDoc}
     */
    public void run() {
        if (LOG.isTraceEnabled()) {
			LOG.trace("Starting cleaner run.");
		}
        lock.lock();
        try {
            final int size = POOLS.size();
            final Iterator<Map.Entry<Integer, ConnectionPool>> iter =
                POOLS.entrySet().iterator();
            for (int i = 0; i < size; i++) {
                final Map.Entry<Integer, ConnectionPool> entry = iter.next();
                final ConnectionPool pool = entry.getValue();
                if (pool.isEmpty()) {
                    unregisterMBean(createMBeanName(entry.getKey().intValue()));
                    pool.destroy();
                    iter.remove();
                }
            }
        } finally {
            lock.unlock();
        }
        if (LOG.isTraceEnabled()) {
			LOG.trace("Cleaner run ending.");
		}
    }

    /**
     * @param poolId identifier of the pool.
     * @return the name of the mbean for the pool.
     */
    private static String createMBeanName(final int poolId) {
        return "DB Pool " + poolId;
    }

    /**
     * Removes a pool from monitoring.
     * @param name Name of the pool to remove.
     */
    private void unregisterMBean(final String name) {
        // TODO finding correct mbean server
        try {
            final ObjectName objName = new ObjectName(
                ConnectionPoolMBean.DOMAIN, "name", name);
            final List servers = MBeanServerFactory.findMBeanServer(null);
            if (servers.size() > 0) {
                final MBeanServer server = (MBeanServer) servers.get(0);
                server.unregisterMBean(objName);
            }
        } catch (MalformedObjectNameException e) {
            LOG.error(ERR_CANT_UNREGISTER_POOL_MBEAN, e);
        } catch (NullPointerException e) {
            LOG.error(ERR_CANT_UNREGISTER_POOL_MBEAN, e);
        } catch (InstanceNotFoundException e) {
            LOG.error(ERR_CANT_UNREGISTER_POOL_MBEAN, e);
        } catch (MBeanRegistrationException e) {
            LOG.error(ERR_CANT_UNREGISTER_POOL_MBEAN, e);
        }
    }

    /**
     * Registers a connection pool as monitored bean.
     * @param name of the pool.
     * @param pool the pool to monitor.
     */
    private static void registerMBean(final String name,
        final ConnectionPool pool) {
        // TODO this doesn't work because I don't know where i'm running (admin
        // or groupware)
        try {
            final ObjectName objName = new ObjectName(
                ConnectionPoolMBean.DOMAIN, "name", name);
            final List servers = MBeanServerFactory.findMBeanServer(null);
            if (servers.size() > 0) {
                final MBeanServer server = (MBeanServer) servers.get(0);
                server.registerMBean(pool, objName);
            }
        } catch (Exception e) {
            LOG.error("Cannot register pool mbean.", e);
        }
    }

    public static void init() throws DBPoolingException {
        if (null != CONFIGDB_READ) {
            return;
        }
        try {
            ConfigDB.init();
        } catch (ConfigurationException e) {
            throw new DBPoolingException(e);
        }
        cleanerInterval = ConfigDB.getLong(Property.CLEANER_INTERVAL,
            cleanerInterval);
        ServerTimer.getTimer().scheduleAtFixedRate(cleaner, cleanerInterval,
            cleanerInterval);
        CONFIGDB_READ = new ConnectionPool(ConfigDB.getReadUrl(),
            ConfigDB.getReadProps(), getConfig());
        CONFIGDB_READ.registerCleaner(ServerTimer.getTimer(), cleanerInterval);
        registerMBean("ConfigDB Read", CONFIGDB_READ);
        if (ConfigDB.isWriteDefined()) {
            CONFIGDB_WRITE = new ConnectionPool(ConfigDB.getWriteUrl(),
                ConfigDB.getWriteProps(), getConfig());
            CONFIGDB_WRITE.registerCleaner(ServerTimer.getTimer(),
                cleanerInterval);
            registerMBean("ConfigDB Write", CONFIGDB_WRITE);
        } else {
            CONFIGDB_WRITE = CONFIGDB_READ;
        }
    }

    /**
     * Pooling configuration.
     */
    private static ConnectionPool.Config config;

    /**
     * Reads the pooling configuration from the configdb.properties file.
     * @return pooling information.
     */
    private static ConnectionPool.Config getConfig() {
        if (null == config) {
            config = ConnectionPool.DEFAULT_CONFIG;
            config.minIdle = ConfigDB.getInt(Property.MIN_IDLE, config.minIdle);
            config.maxIdle = ConfigDB.getInt(Property.MAX_IDLE, config.maxIdle);
            config.maxIdleTime = ConfigDB.getLong(Property.MAX_IDLE_TIME,
                config.maxIdleTime);
            config.maxActive = ConfigDB.getInt(Property.MAX_ACTIVE,
                config.maxActive);
            config.maxWait = ConfigDB.getLong(Property.MAX_WAIT,
                config.maxWait);
            config.maxLifeTime = ConfigDB.getLong(Property.MAX_LIFE_TIME,
                config.maxLifeTime);
            config.exhaustedAction = ConnectionPool.ExhaustedActions.valueOf(
                ConfigDB.getProperty(Property.EXHAUSTED_ACTION,
                    config.exhaustedAction.name()));
            config.testOnActivate = ConfigDB.getBoolean(
                Property.TEST_ON_ACTIVATE, config.testOnActivate);
            config.testOnDeactivate = ConfigDB.getBoolean(
                Property.TEST_ON_DEACTIVATE, config.testOnDeactivate);
            config.testOnIdle = ConfigDB.getBoolean(
                Property.TEST_ON_IDLE, config.testOnIdle);
            config.testThreads = ConfigDB.getBoolean(
                Property.TEST_THREADS, config.testThreads);
            if (LOG.isInfoEnabled()) {
	            final StringBuilder sb = new StringBuilder();
	            sb.append("Database pooling options:\n");
	            sb.append("\tMinimum idle connections: ");
	            sb.append(config.minIdle);
	            sb.append("\n\tMaximum idle connections: ");
	            sb.append(config.maxIdle);
	            sb.append("\n\tMaximum idle time: ");
	            sb.append(config.maxIdleTime);
	            sb.append("ms\n\tMaximum active connections: ");
	            sb.append(config.maxActive);
	            sb.append("\n\tMaximum wait time for a connection: ");
	            sb.append(config.maxWait);
	            sb.append("ms\n\tMaximum life time of a connection: ");
	            sb.append(config.maxLifeTime);
	            sb.append("ms\n\tAction if connections exhausted: ");
	            sb.append(config.exhaustedAction.toString());
	            sb.append("\n\tTest connections on activate  : ");
	            sb.append(config.testOnActivate);
	            sb.append("\n\tTest connections on deactivate: ");
	            sb.append(config.testOnDeactivate);
	            sb.append("\n\tTest idle connections         : ");
	            sb.append(config.testOnIdle);
	            sb.append("\n\tTest threads for bad connection usage (SLOW): ");
	            sb.append(config.testThreads);
	            LOG.info(sb.toString());
            }
        }
        return config;
    }

    /**
     * Customizes configured pooling configuration with settings from database.
     * @param data settings from the database.
     * @return pooling configuration.
     */
    private static ConnectionPool.Config getConfig(
        final ConnectionDataStorage.ConnectionData data) {
        final ConnectionPool.Config retval = getConfig();
        retval.maxActive = data.max;
        retval.minIdle = data.min;
        if (data.block) {
            retval.exhaustedAction = ConnectionPool.ExhaustedActions.BLOCK;
        } else {
            retval.exhaustedAction = ConnectionPool.ExhaustedActions.GROW;
        }
        return retval;
    }
}
