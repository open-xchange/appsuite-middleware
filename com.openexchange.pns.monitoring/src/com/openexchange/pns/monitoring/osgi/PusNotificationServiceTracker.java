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

package com.openexchange.pns.monitoring.osgi;

import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.management.ManagementService;
import com.openexchange.management.Managements;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.pns.monitoring.PushNotificationMBean;
import com.openexchange.pns.monitoring.impl.PushNotificationMBeanImpl;
import com.openexchange.timer.TimerService;

/**
 * {@link PusNotificationServiceTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class PusNotificationServiceTracker implements ServiceTrackerCustomizer<PushNotificationService, PushNotificationService> {

    private final BundleContext xContext;
    private final ManagementService managementService;
    private final TimerService timerService;
    private PushNotificationMBeanImpl mbean;

    /**
     * Initializes a new {@link PusNotificationServiceTracker}.
     */
    public PusNotificationServiceTracker(ManagementService managementService, TimerService timerService, BundleContext xContext) {
        super();
        this.xContext = xContext;
        this.managementService = managementService;
        this.timerService = timerService;
    }

    @Override
    public synchronized PushNotificationService addingService(ServiceReference<PushNotificationService> reference) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PusNotificationServiceTracker.class);

        PushNotificationService pushNotificationService = xContext.getService(reference);
        boolean error = true;
        try {
            ObjectName objectName = Managements.getObjectName(PushNotificationMBean.class.getName(), PushNotificationMBean.DOMAIN);
            PushNotificationMBeanImpl mbean = new PushNotificationMBeanImpl(pushNotificationService, timerService);
            this.mbean = mbean;
            managementService.registerMBean(objectName, mbean);
            error = false;
            logger.info("Registered MBean {}", PushNotificationMBean.class.getName());
            return pushNotificationService;
        } catch (Exception e) {
            logger.warn("Could not register MBean {}", PushNotificationMBean.class.getName(), e);
        } finally {
            if (error) {
                xContext.ungetService(reference);
            }
        }

        return null;
    }

    @Override
    public synchronized void modifiedService(ServiceReference<PushNotificationService> reference, PushNotificationService service) {
        // Ignore
    }

    @Override
    public synchronized void removedService(ServiceReference<PushNotificationService> reference, PushNotificationService service) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PusNotificationServiceTracker.class);

        try {
            managementService.unregisterMBean(Managements.getObjectName(PushNotificationMBean.class.getName(), PushNotificationMBean.DOMAIN));
            logger.info("Unregistered MBean {}", PushNotificationMBean.class.getName());
        } catch (Exception e) {
            logger.warn("Could not un-register MBean {}", PushNotificationMBean.class.getName(), e);
        }

        PushNotificationMBeanImpl mbean = this.mbean;
        if (null != mbean) {
            this.mbean = null;
            mbean.stop();
        }

        xContext.ungetService(reference);
    }

}
