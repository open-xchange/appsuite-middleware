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

package org.apache.felix.eventadmin.monitoring;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.apache.felix.eventadmin.EventAdminMBean;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;


/**
 * {@link EventAdminMonitoringActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class EventAdminMonitoringActivator implements BundleActivator {

    /** The logger constant */
    static final Logger LOG = LoggerFactory.getLogger(EventAdminMonitoringActivator.class);

    private ServiceTracker<Object, Object> tracker;

    /**
     * Initializes a new {@link EventAdminMonitoringActivator}.
     */
    public EventAdminMonitoringActivator() {
        super();
    }

    @Override
    public synchronized void start(final BundleContext context) throws Exception {
        tracker = new ServiceTracker<Object, Object>(
            context,
            FrameworkUtil.createFilter(
                "(|(objectClass=org.apache.felix.eventadmin.EventAdminMBean)" +
                "(objectClass=com.openexchange.management.ManagementService))"),
            new ServiceTrackerCustomizer<Object, Object>() {
                private ManagementService managementService;
                private EventAdminMBean mbean;
                @Override
                public synchronized Object addingService(ServiceReference<Object> reference) {
                    Object service = context.getService(reference);
                    if (service instanceof ManagementService) {
                        managementService = (ManagementService) service;
                    } else if (service instanceof EventAdminMBean) {
                        mbean = (EventAdminMBean) service;
                    }

                    if (managementService == null || mbean == null) {
                        return service;
                    }

                    try {
                        managementService.registerMBean(createObjectName(), mbean);
                    } catch (MalformedObjectNameException e) {
                        LOG.error("Could not register EventAdminMBean", e);
                    } catch (OXException e) {
                        LOG.error("Could not register EventAdminMBean", e);
                    }

                    return service;
                }

                @Override
                public synchronized void modifiedService(ServiceReference<Object> reference, Object service) {
                    // Nothing
                }

                @Override
                public synchronized void removedService(ServiceReference<Object> reference, Object service) {
                    if (service instanceof ManagementService) {
                        managementService = null;
                        if (mbean != null) {
                            unregisterMBean((ManagementService) service);
                        }
                    } else if (service instanceof EventAdminMBean) {
                        mbean = null;
                        if (managementService != null) {
                            unregisterMBean(managementService);
                        }
                    }
                }

                private void unregisterMBean(ManagementService service) {
                    try {
                        service.unregisterMBean(createObjectName());
                    } catch (MalformedObjectNameException e) {
                        LOG.error("Could not unregister EventAdminMBean", e);
                    } catch (OXException e) {
                        LOG.error("Could not unregister EventAdminMBean", e);
                    }
                }

                private ObjectName createObjectName() throws MalformedObjectNameException {
                    return new ObjectName("org.apache.felix.eventadmin.monitoring", "type", "EventAdminMBean");
                }
            }
        );
        tracker.open();
    }

    @Override
    public synchronized void stop(final BundleContext context) throws Exception {
        ServiceTracker<Object, Object> tracker = this.tracker;
        if (tracker != null) {
            this.tracker = null;
            tracker.close();
        }
    }

}
