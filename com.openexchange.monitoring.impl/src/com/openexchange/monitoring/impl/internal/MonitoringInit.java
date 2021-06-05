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

package com.openexchange.monitoring.impl.internal;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;
import com.openexchange.server.Initialization;
import com.openexchange.server.ServiceLookup;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class MonitoringInit implements Initialization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MonitoringInit.class);

    /**
     * @return the singleton instance.
     */
    public static MonitoringInit newInstance(ServiceLookup services) {
        return new MonitoringInit(services);
    }

    // ----------------------------------------------------------------------------------------------------------

    private final ServiceLookup services;
    private ObjectName objectName; // only accessed synchronized
    private volatile boolean started;

    /**
     * Prevent instantiation.
     */
    private MonitoringInit(ServiceLookup services) {
        super();
        this.services = services;
    }

    private ObjectName getObjectName() throws MalformedObjectNameException, NullPointerException {
        if (null == objectName) {
            objectName = new ObjectName("com.openexchange.monitoring", "name", "GeneralMonitor");
        }
        return objectName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void start() throws OXException {
        if (started) {
            LOG.error("{} already started", MonitoringInit.class.getName());
            return;
        }

        /*
         * Create Beans and register them
         */
        ManagementService managementAgent = services.getService(ManagementService.class);
        GeneralMonitor generalMonitorBean = new GeneralMonitor(services);
        try {
            managementAgent.registerMBean(getObjectName(), generalMonitorBean);
        } catch (MalformedObjectNameException exc) {
            LOG.error("", exc);
        } catch (NullPointerException exc) {
            LOG.error("", exc);
        } catch (Exception exc) {
            LOG.error("", exc);
        }
        LOG.info("JMX Monitor applied");

        started = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void stop() throws OXException {
        if (!started) {
            LOG.error("{} has not been started", MonitoringInit.class.getName());
            return;
        }

        ManagementService managementAgent = services.getService(ManagementService.class);
        if (managementAgent != null) {
            try {
                managementAgent.unregisterMBean(getObjectName());
            } catch (MalformedObjectNameException exc) {
                LOG.error("", exc);
            } catch (NullPointerException exc) {
                LOG.error("", exc);
            } catch (Exception exc) {
                LOG.error("", exc);
            }
            LOG.info("JMX Monitor removed");
        }
        started = false;
    }

    /**
     * @return <code>true</code> if monitoring has been started; otherwise <code>false</code>
     */
    public boolean isStarted() {
        return started;
    }
}
