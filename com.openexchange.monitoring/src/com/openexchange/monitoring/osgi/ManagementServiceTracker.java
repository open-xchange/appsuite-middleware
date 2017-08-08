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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.monitoring.osgi;

import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.management.ManagementService;
import com.openexchange.management.Managements;
import com.openexchange.monitoring.sockets.SocketTraceMBean;
import com.openexchange.monitoring.sockets.TracingSocketMonitor;
import com.openexchange.monitoring.sockets.internal.SocketTraceMBeanImpl;

/**
 * {@link ManagementServiceTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ManagementServiceTracker implements ServiceTrackerCustomizer<ManagementService, ManagementService> {

    private final BundleContext bundleContext;
    private final TracingSocketMonitor monitor;

    /**
     * Initializes a new {@link ManagementServiceTracker}.
     */
    public ManagementServiceTracker(TracingSocketMonitor monitor, BundleContext bundleContext) {
        super();
        this.monitor = monitor;
        this.bundleContext = bundleContext;
    }

    @Override
    public ManagementService addingService(ServiceReference<ManagementService> reference) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(ManagementServiceTracker.class);

        ManagementService managementService = bundleContext.getService(reference);
        boolean error = true;
        try {
            ObjectName objectName = Managements.getObjectName(SocketTraceMBean.class.getName(), SocketTraceMBean.DOMAIN);
            managementService.registerMBean(objectName, new SocketTraceMBeanImpl(monitor));
            error = false;
            logger.info("Registered MBean {}", SocketTraceMBean.class.getName());
            return managementService;
        } catch (Exception e) {
            logger.warn("Could not register MBean {}", SocketTraceMBean.class.getName(), e);
        } finally {
            if (error) {
                bundleContext.ungetService(reference);
            }
        }

        return null;
    }

    @Override
    public void modifiedService(ServiceReference<ManagementService> reference, ManagementService service) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<ManagementService> reference, ManagementService service) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(ManagementServiceTracker.class);

        try {
            service.unregisterMBean(Managements.getObjectName(SocketTraceMBean.class.getName(), SocketTraceMBean.DOMAIN));
            logger.info("Unregistered MBean {}", SocketTraceMBean.class.getName());
        } catch (Exception e) {
            logger.warn("Could not un-register MBean {}", SocketTraceMBean.class.getName(), e);
        }

        bundleContext.ungetService(reference);
    }

}
