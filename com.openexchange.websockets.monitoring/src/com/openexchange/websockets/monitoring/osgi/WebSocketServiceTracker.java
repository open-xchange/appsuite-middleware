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

package com.openexchange.websockets.monitoring.osgi;

import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.management.ManagementService;
import com.openexchange.management.Managements;
import com.openexchange.websockets.WebSocketService;
import com.openexchange.websockets.monitoring.WebSocketMBean;
import com.openexchange.websockets.monitoring.impl.WebSocketMBeanImpl;

/**
 * {@link WebSocketServiceTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketServiceTracker implements ServiceTrackerCustomizer<WebSocketService, WebSocketService> {

    private final BundleContext xContext;
    private final ManagementService managementService;

    /**
     * Initializes a new {@link WebSocketServiceTracker}.
     */
    public WebSocketServiceTracker(ManagementService managementService, BundleContext xContext) {
        super();
        this.xContext = xContext;
        this.managementService = managementService;
    }

    @Override
    public WebSocketService addingService(ServiceReference<WebSocketService> reference) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(WebSocketServiceTracker.class);

        WebSocketService webSocketService = xContext.getService(reference);
        boolean error = true;
        try {
            ObjectName objectName = Managements.getObjectName(WebSocketMBean.class.getName(), WebSocketMBean.DOMAIN);
            managementService.registerMBean(objectName, new WebSocketMBeanImpl(webSocketService));
            error = false;
            logger.info("Registered MBean {}", WebSocketMBean.class.getName());
            return webSocketService;
        } catch (Exception e) {
            logger.warn("Could not register MBean {}", WebSocketMBean.class.getName(), e);
        } finally {
            if (error) {
                xContext.ungetService(reference);
            }
        }

        return null;
    }

    @Override
    public void modifiedService(ServiceReference<WebSocketService> reference, WebSocketService service) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<WebSocketService> reference, WebSocketService service) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(WebSocketMonitoringActivator.class);

        try {
            managementService.unregisterMBean(Managements.getObjectName(WebSocketMBean.class.getName(), WebSocketMBean.DOMAIN));
            logger.info("Unregistered MBean {}", WebSocketMBean.class.getName());
        } catch (Exception e) {
            logger.warn("Could not un-register MBean {}", WebSocketMBean.class.getName(), e);
        }

        xContext.ungetService(reference);
    }

}
