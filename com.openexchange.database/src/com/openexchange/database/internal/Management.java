/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.database.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;

/**
 * Responsible for registering dynamically connection pools in the JMX interface.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Management {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Management.class);

    private final Map<Integer, ConnectionPool> pools = new ConcurrentHashMap<Integer, ConnectionPool>();
    private final Timer timer;

    private ManagementService managementService;
    private Overview overview;

    Management(Timer timer) {
        super();
        this.timer = timer;
    }

    public void removeManagementService() {
        unregisterMBeans();
        this.managementService = null;
    }

    public void setManagementService(final ManagementService service) {
        this.managementService = service;
        registerMBeans();
    }

    /**
     * Registers a connection pool as monitored bean.
     * @param name of the pool.
     * @param pool the pool to monitor.
     */
    private void registerPool(final String name, final ConnectionPool pool) {
        final ManagementService service = managementService;
        if (null == service) {
            return;
        }
        timer.addOnceTask(new Runnable() {
            @Override
            public void run() {
                try {
                    final ObjectName objName = new ObjectName(ConnectionPoolMBean.DOMAIN, "name", name);
                    service.registerMBean(objName, pool);
                } catch (MalformedObjectNameException e) {
                    LOG.error("", e);
                } catch (NullPointerException e) {
                    LOG.error("", e);
                } catch (OXException e) {
                    LOG.error("", e);
                }
            }
        });
    }

    public void registerMBeans() {
        for (final Map.Entry<Integer, ConnectionPool> entry : pools.entrySet()) {
            registerPool(createMBeanName(entry.getKey().intValue()), entry.getValue());
        }
        registerOverview();
    }

    private void registerOverview() {
        try {
            if (null != overview) {
                managementService.registerMBean(new ObjectName(ConnectionPoolMBean.DOMAIN, "name", "Overview"), overview);
            }
        } catch (OXException e) {
            LOG.error("", e);
        } catch (MalformedObjectNameException e) {
            LOG.error("", e);
        } catch (NullPointerException e) {
            LOG.error("", e);
        }
    }

    /**
     * Removes a pool from monitoring.
     * @param name Name of the pool to remove.
     */
    private void unregisterMBean(final String name) {
        final ManagementService service = managementService;
        if (null == service) {
            return;
        }
        timer.addOnceTask(new Runnable() {
            @Override
            public void run() {
                try {
                    final ObjectName objName = new ObjectName(ConnectionPoolMBean.DOMAIN, "name", name);
                    service.unregisterMBean(objName);
                } catch (MalformedObjectNameException e) {
                    LOG.error("", e);
                } catch (NullPointerException e) {
                    LOG.error("", e);
                } catch (OXException e) {
                    LOG.error("", e);
                }
            }
        });
    }

    public void unregisterMBeans() {
        for (final Map.Entry<Integer, ConnectionPool> entry : pools.entrySet()) {
            unregisterMBean(Management.createMBeanName(entry.getKey().intValue()));
        }
        try {
            managementService.unregisterMBean(new ObjectName(ConnectionPoolMBean.DOMAIN, "name", "Overview"));
        } catch (MalformedObjectNameException e) {
            LOG.error("", e);
        } catch (OXException e) {
            LOG.error("", e);
        } catch (NullPointerException e) {
            LOG.error("", e);
        }
    }

    /**
     * @param poolId identifier of the pool.
     * @return the name of the mbean for the pool.
     */
    private static String createMBeanName(final int poolId) {
        switch (poolId) {
        case Constants.CONFIGDB_READ_ID:
            return "ConfigDB Read";
        case Constants.CONFIGDB_WRITE_ID:
            return "ConfigDB Write";
        default:
            return "DB Pool " + poolId;
        }
    }

    public void addPool(final int poolId, final ConnectionPool pool) {
        pools.put(I(poolId), pool);
        if (null != managementService) {
            registerPool(createMBeanName(poolId), pool);
        }
    }

    public void removePool(final int poolId) {
        pools.remove(I(poolId));
        if (null != managementService) {
            unregisterMBean(createMBeanName(poolId));
        }
    }

    public void addOverview(final Overview newOverview) {
        this.overview = newOverview;
        if (null != managementService) {
            registerOverview();
        }
    }
}
