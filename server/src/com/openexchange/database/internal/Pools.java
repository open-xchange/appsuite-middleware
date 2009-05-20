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

package com.openexchange.database.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.management.ManagementException;
import com.openexchange.management.ManagementService;
import com.openexchange.pooling.ReentrantLockPool.Config;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DBPoolingException.Code;
import com.openexchange.database.internal.Configuration.Property;

/**
 * This class stores all connection pools. It also removes pools that are empty.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Pools implements Runnable {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Pools.class);

    public static final int CONFIGDB_READ_ID = -1;

    public static final int CONFIGDB_WRITE_ID = -2;

    private final TimerService timerService;

    private final Lock poolsLock = new ReentrantLock(true);

    private ConnectionPool configDBRead;

    private ConnectionPool configDBWrite;

    private ConnectionDataStorage connectionDataStorage;

    private final Map<Integer, ConnectionPool> oxPools = new HashMap<Integer, ConnectionPool>();

    private long cleanerInterval = 10000;

    private ManagementService managementService;

    /**
     * Default constructor.
     */
    public Pools(TimerService timerService) {
        super();
        this.timerService = timerService;
    }

    /**
     * @return an array with all connection pools.
     */
    public ConnectionPool[] getPools() {
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

    public ConnectionPool getPool(final int poolId) throws DBPoolingException {
        if (null == configDBRead) {
            throw new DBPoolingException(Code.NOT_INITIALIZED, Pools.class.getName());
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
                    final ConnectionData data = connectionDataStorage.loadPoolData(poolId);
                    try {
                        Class.forName(data.driverClass);
                    } catch (final ClassNotFoundException e) {
                        throw new DBPoolingException(Code.NO_DRIVER, e);
                    }
                    retval = createPool(poolId, data.url, data.props, getConfig(data));
                    oxPools.put(I(poolId), retval);
                }
            } finally {
                poolsLock.unlock();
            }
        }
        return retval;
    }

    private ConnectionPool createPool(int poolId, String url, Properties props, Config config) {
        ConnectionPool retval = new ConnectionPool(url, props, config);
        retval.registerCleaner(timerService, cleanerInterval);
        registerMBean(createMBeanName(poolId), retval);
        return retval;
    }

    private void destroyPool(int poolId, ConnectionPool pool) {
        unregisterMBean(createMBeanName(poolId));
        pool.destroy();
    }

    private ScheduledTimerTask cleaner;

    private void startCleaner() throws DBPoolingException {
        if (null != cleaner) {
            throw new DBPoolingException(Code.ALREADY_INITIALIZED, "PoolsCleaner");
        }
        cleaner = timerService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    Thread thread = Thread.currentThread();
                    String origName = thread.getName();
                    thread.setName("PoolsCleaner");
                    Pools.this.run();
                    thread.setName(origName);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }, cleanerInterval, cleanerInterval);
    }

    private void stopCleaner() {
        if (null == cleaner) {
            // TODO throw DBPoolingException
            throw new IllegalStateException("Pools cleaner is already stopped.");
        }
        if (!cleaner.cancel()) {
            LOG.error("Can not stop pools cleaner.");
        }
        cleaner = null;
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        LOG.trace("Starting cleaner run.");
        poolsLock.lock();
        try {
            final Iterator<Map.Entry<Integer, ConnectionPool>> iter = oxPools.entrySet().iterator();
            while (iter.hasNext()) {
                final Map.Entry<Integer, ConnectionPool> entry = iter.next();
                final ConnectionPool pool = entry.getValue();
                if (pool.isEmpty()) {
                    iter.remove();
                    destroyPool(entry.getKey().intValue(), pool);
                }
            }
        } finally {
            poolsLock.unlock();
        }
        LOG.trace("Cleaner run ending.");
    }

    /**
     * @param poolId identifier of the pool.
     * @return the name of the mbean for the pool.
     */
    private static String createMBeanName(final int poolId) {
        switch (poolId) {
        case CONFIGDB_READ_ID:
            return "ConfigDB Read";
        case CONFIGDB_WRITE_ID:
            return "ConfigDB Write";
        default:
            return "DB Pool " + poolId;
        }
    }

    /**
     * Removes a pool from monitoring.
     * @param name Name of the pool to remove.
     */
    private void unregisterMBean(final String name) {
        try {
            if (null != managementService) {
                final ObjectName objName = new ObjectName(ConnectionPoolMBean.DOMAIN, "name", name);
                managementService.unregisterMBean(objName);
            }
        } catch (final MalformedObjectNameException e) {
            LOG.error(e.getMessage(), e);
        } catch (final NullPointerException e) {
            LOG.error(e.getMessage(), e);
        } catch (final ManagementException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Registers a connection pool as monitored bean.
     * @param name of the pool.
     * @param pool the pool to monitor.
     */
    private void registerMBean(final String name, final ConnectionPool pool) {
        try {
            if (null != managementService) {
                final ObjectName objName = new ObjectName(ConnectionPoolMBean.DOMAIN, "name", name);
                managementService.registerMBean(objName, pool);
            }
        } catch (final MalformedObjectNameException e) {
            LOG.error(e.getMessage(), e);
        } catch (final NullPointerException e) {
            LOG.error(e.getMessage(), e);
        } catch (final ManagementException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void registerMBeans() {
        if (null == configDBRead) {
            // Service appeared before Pools are started.
            return;
        }
        registerMBean(createMBeanName(CONFIGDB_READ_ID), configDBRead);
        if (configDBWrite != configDBRead) {
            registerMBean(createMBeanName(CONFIGDB_WRITE_ID), configDBWrite);
        }
        poolsLock.lock();
        try {
            for (final Map.Entry<Integer, ConnectionPool> entry : oxPools.entrySet()) {
                registerMBean(createMBeanName(entry.getKey().intValue()), entry.getValue());
            }
        } finally {
            poolsLock.unlock();
        }
    }

    public void unregisterMBeans() {
        if (configDBWrite != configDBRead) {
            unregisterMBean(createMBeanName(CONFIGDB_WRITE_ID));
        }
        unregisterMBean(createMBeanName(CONFIGDB_READ_ID));
        poolsLock.lock();
        try {
            for (final Map.Entry<Integer, ConnectionPool> entry : oxPools.entrySet()) {
                unregisterMBean(createMBeanName(entry.getKey().intValue()));
            }
        } finally {
            poolsLock.unlock();
        }
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
        registerMBeans();
    }

    public void removeManagementService() {
        unregisterMBeans();
        managementService = null;
    }

    /**
     * Initializes the default pool configuration and starts read and write
     * pools for ConfigDB.
     * @throws DBPoolingException if starting fails.
     */
    public void start(Configuration configuration) throws DBPoolingException {
        if (null != configDBRead) {
            throw new DBPoolingException(Code.ALREADY_INITIALIZED, Pools.class.getName());
        }
        initPoolConfig(configuration);
        cleanerInterval = configuration.getLong(Property.CLEANER_INTERVAL, cleanerInterval);
        startCleaner();
        configDBRead = createPool(CONFIGDB_READ_ID, configuration.getReadUrl(), configuration.getReadProps(), config);
        if (configuration.isWriteDefined()) {
            configDBWrite = createPool(CONFIGDB_WRITE_ID, configuration.getWriteUrl(), configuration.getWriteProps(), config);
        } else {
            configDBWrite = configDBRead;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void stop() {
        poolsLock.lock();
        try {
            for (final Map.Entry<Integer, ConnectionPool> entry : oxPools.entrySet()) {
                destroyPool(entry.getKey().intValue(), entry.getValue());
            }
            oxPools.clear();
        } finally {
            poolsLock.unlock();
        }
        if (configDBWrite != configDBRead) {
            destroyPool(CONFIGDB_WRITE_ID, configDBWrite);
        }
        configDBWrite = null;
        destroyPool(CONFIGDB_READ_ID, configDBRead);
        configDBRead = null;
        stopCleaner();
        config = null;
    }

    public void setConnectionDataStorage(ConnectionDataStorage connectionDataStorage) {
        this.connectionDataStorage = connectionDataStorage;
    }

    /**
     * Pooling configuration.
     */
    private ConnectionPool.Config config;

    /**
     * Reads the pooling configuration from the configdb.properties file.
     */
    private void initPoolConfig(Configuration configuration) {
        config = ConnectionPool.DEFAULT_CONFIG;
        config.minIdle = configuration.getInt(Property.MIN_IDLE, config.minIdle);
        config.maxIdle = configuration.getInt(Property.MAX_IDLE, config.maxIdle);
        config.maxIdleTime = configuration.getLong(Property.MAX_IDLE_TIME, config.maxIdleTime);
        config.maxActive = configuration.getInt(Property.MAX_ACTIVE, config.maxActive);
        config.maxWait = configuration.getLong(Property.MAX_WAIT, config.maxWait);
        config.maxLifeTime = configuration.getLong(Property.MAX_LIFE_TIME, config.maxLifeTime);
        config.exhaustedAction = ConnectionPool.ExhaustedActions.valueOf(configuration.getProperty(
            Property.EXHAUSTED_ACTION,
            config.exhaustedAction.name()));
        config.testOnActivate = configuration.getBoolean(Property.TEST_ON_ACTIVATE, config.testOnActivate);
        config.testOnDeactivate = configuration.getBoolean(Property.TEST_ON_DEACTIVATE, config.testOnDeactivate);
        config.testOnIdle = configuration.getBoolean(Property.TEST_ON_IDLE, config.testOnIdle);
        config.testThreads = configuration.getBoolean(Property.TEST_THREADS, config.testThreads);
        config.forceWriteOnly = configuration.getBoolean(Property.WRITE_ONLY, false);
        LOG.info(config.toString());
    }

    /**
     * Customizes configured pooling configuration with settings from database.
     * @param data settings from the database.
     * @return pooling configuration.
     */
    private ConnectionPool.Config getConfig(
        final ConnectionData data) {
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
