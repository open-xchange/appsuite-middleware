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

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.configuration.ConfigDB;
import com.openexchange.configuration.ConfigDB.Property;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.ServerTimer;
import com.openexchange.server.DBPoolingException.Code;

/**
 * This class stores all connection pools. It also removes pools that are empty.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Pools implements Runnable {

    /**
     * Singleton.
     */
    private static final Pools SINGLETON = new Pools();

    private static final String ERR_UNREGISTER = "Cannot unregister pool mbean.";

    private static final String ERR_REGISTER = "Cannot register pool mbean.";

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Pools.class);

    public static final int CONFIGDB_READ_ID = -1;

    public static final int CONFIGDB_WRITE_ID = -2;

    private ConnectionPool configDBRead;

    private ConnectionPool configDBWrite;

    private long cleanerInterval = 10000;

    private final Map<Integer, ConnectionPool> oxPools =
        new HashMap<Integer, ConnectionPool>();

    private final Lock poolsLock = new ReentrantLock(true);

    /**
     * Prevent instantiation.
     */
    private Pools() {
        super();
    }

    /**
     * @return an array with all connection pools.
     */
    ConnectionPool[] getPools() {
        final List<ConnectionPool> pools = new ArrayList<ConnectionPool>();
        pools.add(configDBRead);
        if (configDBWrite != configDBRead) {
            pools.add(configDBWrite);
        }
        poolsLock.lock();
        try {
            pools.addAll(oxPools.values());
        } finally {
            poolsLock.unlock();
        }
        return pools.toArray(new ConnectionPool[pools.size()]);
    }

    ConnectionPool getPool(final int poolId) throws DBPoolingException {
        if (null == configDBRead) {
            throw new DBPoolingException(Code.NOT_INITIALIZED,
                Pools.class.getName());
        }
        ConnectionPool retval;
        switch (poolId) {
        case CONFIGDB_READ_ID:
            retval = configDBRead;
            break;
        case CONFIGDB_WRITE_ID:
            retval = configDBWrite;
            break;
        default:
            poolsLock.lock();
            try {
                retval = oxPools.get(Integer.valueOf(poolId));
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
                    oxPools.put(Integer.valueOf(poolId), retval);
                }
            } finally {
                poolsLock.unlock();
            }
        }
        return retval;
    }

    private TimerTask cleaner;

    private void startCleaner() {
        if (null != cleaner) {
            throw new IllegalStateException("");
        }
        cleaner =  new TimerTask() {
            @Override
            public void run() {
                try {
                    final Thread thread = new Thread(Pools.this);
                    thread.setName("PoolsCleaner");
                    thread.start();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        };
        ServerTimer.getTimer().scheduleAtFixedRate(cleaner, cleanerInterval,
            cleanerInterval);
    }

    private void stopCleaner() {
        if (null == cleaner) {
            throw new IllegalStateException("");
        }
        cleaner.cancel();
        cleaner = null;
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Starting cleaner run.");
        }
        poolsLock.lock();
        try {
            final Iterator<Map.Entry<Integer, ConnectionPool>> iter =
                oxPools.entrySet().iterator();
            while (iter.hasNext()) {
                final Map.Entry<Integer, ConnectionPool> entry = iter.next();
                final ConnectionPool pool = entry.getValue();
                if (pool.isEmpty()) {
                    unregisterMBean(createMBeanName(entry.getKey().intValue()));
                    pool.destroy();
                    iter.remove();
                }
            }
        } finally {
            poolsLock.unlock();
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
    private static void unregisterMBean(final String name) {
        // TODO finding correct mbean server
        try {
            final ObjectName objName = new ObjectName(
                ConnectionPoolMBean.DOMAIN, "name", name);
            final List<MBeanServer> servers = MBeanServerFactory
                .findMBeanServer(null);
            if (servers.size() > 0) {
                final MBeanServer server = (MBeanServer) servers.get(0);
                server.unregisterMBean(objName);
            }
        } catch (MalformedObjectNameException e) {
            LOG.error(ERR_UNREGISTER, e);
        } catch (NullPointerException e) {
            LOG.error(ERR_UNREGISTER, e);
        } catch (InstanceNotFoundException e) {
            LOG.error(ERR_UNREGISTER, e);
        } catch (MBeanRegistrationException e) {
            LOG.error(ERR_UNREGISTER, e);
        }
    }

    /**
     * Registers a connection pool as monitored bean.
     * @param name of the pool.
     * @param pool the pool to monitor.
     */
    private static void registerMBean(final String name,
        final ConnectionPool pool) {
        // TODO finding correct mbean server
        try {
            final ObjectName objName = new ObjectName(
                ConnectionPoolMBean.DOMAIN, "name", name);
            final List<MBeanServer> servers = MBeanServerFactory
                .findMBeanServer(null);
            if (servers.size() > 0) {
                final MBeanServer server = (MBeanServer) servers.get(0);
                server.registerMBean(pool, objName);
            }
        } catch (MalformedObjectNameException e) {
            LOG.error(ERR_REGISTER, e);
        } catch (NullPointerException e) {
            LOG.error(ERR_REGISTER, e);
        } catch (InstanceAlreadyExistsException e) {
            LOG.error(ERR_REGISTER, e);
        } catch (MBeanRegistrationException e) {
            LOG.error(ERR_REGISTER, e);
        } catch (NotCompliantMBeanException e) {
            LOG.error(ERR_REGISTER, e);
        }
    }

    /**
     * @return the singleton instance.
     */
    public static Pools getInstance() {
        return SINGLETON;
    }

    /**
     * Initializes the default pool configuration and starts read and write
     * pools for ConfigDB.
     * @throws DBPoolingException if starting fails.
     */
    public void start() throws DBPoolingException {
        if (null != configDBRead) {
            LOG.error("Duplicate startup of Pools.");
            return;
        }
        initPoolConfig();
        final ConfigDB configDB = ConfigDB.getInstance();
        cleanerInterval = configDB.getLong(Property.CLEANER_INTERVAL,
            cleanerInterval);
        startCleaner();
        // TODO write createPool method.
        configDBRead = new ConnectionPool(configDB.getReadUrl(),
            configDB.getReadProps(), config);
        configDBRead.registerCleaner(ServerTimer.getTimer(), cleanerInterval);
        registerMBean("ConfigDB Read", configDBRead);
        if (configDB.isWriteDefined()) {
            configDBWrite = new ConnectionPool(configDB.getWriteUrl(),
                configDB.getWriteProps(), config);
            configDBWrite.registerCleaner(ServerTimer.getTimer(),
                cleanerInterval);
            registerMBean("ConfigDB Write", configDBWrite);
        } else {
            configDBWrite = configDBRead;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        // TODO write destroyPool method.
        if (ConfigDB.getInstance().isWriteDefined()) {
            unregisterMBean("ConfigDB Write");
            configDBWrite.getCleanerTask().cancel();
            configDBWrite.destroy();
        }
        configDBWrite = null;
        unregisterMBean("ConfigDB Read");
        configDBRead.getCleanerTask().cancel();
        configDBRead.destroy();
        configDBRead = null;
        stopCleaner();
        config = null;
    }

    /**
     * Pooling configuration.
     */
    private ConnectionPool.Config config;

    /**
     * Reads the pooling configuration from the configdb.properties file.
     */
    private void initPoolConfig() {
        config = ConnectionPool.DEFAULT_CONFIG;
        final ConfigDB configDB = ConfigDB.getInstance();
        config.minIdle = configDB.getInt(Property.MIN_IDLE, config.minIdle);
        config.maxIdle = configDB.getInt(Property.MAX_IDLE, config.maxIdle);
        config.maxIdleTime = configDB.getLong(Property.MAX_IDLE_TIME,
            config.maxIdleTime);
        config.maxActive = configDB.getInt(Property.MAX_ACTIVE,
            config.maxActive);
        config.maxWait = configDB.getLong(Property.MAX_WAIT,
            config.maxWait);
        config.maxLifeTime = configDB.getLong(Property.MAX_LIFE_TIME,
            config.maxLifeTime);
        config.exhaustedAction = ConnectionPool.ExhaustedActions.valueOf(
            configDB.getProperty(Property.EXHAUSTED_ACTION, config
                .exhaustedAction.name()));
        config.testOnActivate = configDB.getBoolean(Property.TEST_ON_ACTIVATE,
            config.testOnActivate);
        config.testOnDeactivate = configDB.getBoolean(
            Property.TEST_ON_DEACTIVATE, config.testOnDeactivate);
        config.testOnIdle = configDB.getBoolean(Property.TEST_ON_IDLE,
            config.testOnIdle);
        config.testThreads = configDB.getBoolean(Property.TEST_THREADS,
            config.testThreads);
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

    /**
     * Customizes configured pooling configuration with settings from database.
     * @param data settings from the database.
     * @return pooling configuration.
     */
    private ConnectionPool.Config getConfig(
        final ConnectionDataStorage.ConnectionData data) {
        final ConnectionPool.Config retval = config.clone();
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
