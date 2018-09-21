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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.management;

import javax.management.ObjectName;
import javax.management.StandardMBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HousekeepingManagementTracker} - Helper class for tracking the {@link ManagementService}
 * and registering an MBean
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
     * @param mbean The mbean instance
     */
    public HousekeepingManagementTracker(BundleContext context, String mbeanName, String domainName, StandardMBean mbean) {
        super();
        this.context = context;
        this.mbeanName = mbeanName;
        this.domainName = domainName;
        this.mbean = mbean;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public ManagementService addingService(ServiceReference<ManagementService> reference) {
        ManagementService managementService = context.getService(reference);
        try {
            ObjectName objectName = Managements.getObjectName(mbeanName, domainName);
            managementService.registerMBean(objectName, mbean);
            LOG.info("Registered MBean {}", mbeanName);
            return managementService;
        } catch (Exception e) {
            LOG.warn("Could not register MBean {}", mbeanName, e);
        }
        context.ungetService(reference);
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void modifiedService(ServiceReference<ManagementService> reference, ManagementService service) {
        // nothing to modify
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
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
