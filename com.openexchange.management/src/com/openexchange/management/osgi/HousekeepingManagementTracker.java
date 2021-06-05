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

package com.openexchange.management.osgi;

import javax.management.ObjectName;
import javax.management.StandardMBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.management.ManagementService;
import com.openexchange.management.Managements;

/**
 * {@link HousekeepingManagementTracker} - Helper class for tracking the {@link ManagementService}
 * and registering an MBean.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class HousekeepingManagementTracker implements ServiceTrackerCustomizer<ManagementService, ManagementService> {

    private static final Logger LOG = LoggerFactory.getLogger(HousekeepingManagementTracker.class);

    private final BundleContext context;
    private final String mbeanName;
    private final String domainName;
    private final StandardMBean mbean;

    /**
     * Initialises a new {@link HousekeepingManagementTracker}.
     *
     * @param context The bundle context
     * @param mbeaName The name of the MBean
     * @param domainName The name of the domain
     * @param mbean The MBean instance
     */
    public HousekeepingManagementTracker(BundleContext context, String mbeanName, String domainName, StandardMBean mbean) {
        super();
        this.context = context;
        this.mbeanName = mbeanName;
        this.domainName = domainName;
        this.mbean = mbean;
    }

    @Override
    public ManagementService addingService(ServiceReference<ManagementService> reference) {
        ManagementService managementService = context.getService(reference);
        boolean error = true;
        try {
            ObjectName objectName = Managements.getObjectName(mbeanName, domainName);
            managementService.registerMBean(objectName, mbean);
            error = false;
            LOG.info("Registered MBean {}", mbeanName);
            return managementService;
        } catch (Exception e) {
            LOG.warn("Could not register MBean {}", mbeanName, e);
        } finally {
            if (error) {
                context.ungetService(reference);
            }
        }
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<ManagementService> reference, ManagementService service) {
        // nothing to modify
    }

    @Override
    public void removedService(ServiceReference<ManagementService> reference, ManagementService managementService) {
        if (null == managementService) {
            return;
        }
        try {
            managementService.unregisterMBean(Managements.getObjectName(mbeanName, domainName));
            LOG.info("Unregistered MBean {}", mbeanName);
        } catch (Exception e) {
            LOG.warn("Could not un-register MBean {}", domainName, e);
        }
        context.ungetService(reference);
    }

}
