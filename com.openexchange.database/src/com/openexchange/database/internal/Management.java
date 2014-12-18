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
                } catch (final MalformedObjectNameException e) {
                    LOG.error("", e);
                } catch (final NullPointerException e) {
                    LOG.error("", e);
                } catch (final OXException e) {
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
        } catch (final OXException e) {
            LOG.error("", e);
        } catch (final MalformedObjectNameException e) {
            LOG.error("", e);
        } catch (final NullPointerException e) {
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
                } catch (final MalformedObjectNameException e) {
                    LOG.error("", e);
                } catch (final NullPointerException e) {
                    LOG.error("", e);
                } catch (final OXException e) {
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
        } catch (final MalformedObjectNameException e) {
            LOG.error("", e);
        } catch (final OXException e) {
            LOG.error("", e);
        } catch (final NullPointerException e) {
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
