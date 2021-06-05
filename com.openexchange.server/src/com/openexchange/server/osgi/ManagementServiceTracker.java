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

package com.openexchange.server.osgi;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.monitoring.MonitorUtility.getObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.BundleServiceTracker;
import com.openexchange.report.internal.ReportingInit;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.ratelimit.monitoring.RateLimiterMBean;
import com.openexchange.tools.servlet.ratelimit.monitoring.RateLimiterMBeanImpl;

/**
 * {@link ManagementServiceTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ManagementServiceTracker extends BundleServiceTracker<ManagementService> {

    private volatile ObjectName rateLimiterObjectName;

    /**
     * Initializes a new {@link ManagementServiceTracker}
     *
     * @param context The bundle context
     */
    public ManagementServiceTracker(final BundleContext context) {
        super(context, ManagementService.class);
    }

    @Override
    protected void addingServiceInternal(final ManagementService managementService) {
        /*
         * Add management service to server's service registry
         */
        ServerServiceRegistry.getInstance().addService(ManagementService.class, managementService);
        try {
            /*
             * Add all mbeans since management service is now available
             */
            managementService.registerMBean(getObjectName(mailInterfaceMonitor.getClass().getName(), true), mailInterfaceMonitor);
            ObjectName rateLimiterObjectName = getObjectName(RateLimiterMBean.NAME, true);
            managementService.registerMBean(rateLimiterObjectName, new RateLimiterMBeanImpl());
            this.rateLimiterObjectName = rateLimiterObjectName;
        } catch (MalformedObjectNameException e) {
            org.slf4j.LoggerFactory.getLogger(ManagementServiceTracker.class).error("", e);
        } catch (NullPointerException e) {
            org.slf4j.LoggerFactory.getLogger(ManagementServiceTracker.class).error("", e);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ManagementServiceTracker.class).error("", e);
        }
        new ReportingInit(managementService).start();
    }

    @Override
    protected void removedServiceInternal(final ManagementService managementService) {
        new ReportingInit(managementService).stop();
        try {
            /*
             * Remove all mbeans since management service now disappears
             */
            managementService.unregisterMBean(getObjectName(mailInterfaceMonitor.getClass().getName(), true));
            ObjectName rateLimiterObjectName = this.rateLimiterObjectName;
            if (null != rateLimiterObjectName) {
                managementService.unregisterMBean(rateLimiterObjectName);
            }
        } catch (MalformedObjectNameException e) {
            org.slf4j.LoggerFactory.getLogger(ManagementServiceTracker.class).error("", e);
        } catch (NullPointerException e) {
            org.slf4j.LoggerFactory.getLogger(ManagementServiceTracker.class).error("", e);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ManagementServiceTracker.class).error("", e);
        } finally {
            this.rateLimiterObjectName = null;
        }
    }

}
