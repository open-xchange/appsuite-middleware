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
 * {@link Activator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Activator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private ServiceTracker<Object, Object> tracker;

    @Override
    public void start(final BundleContext context) throws Exception {
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
                public synchronized void modifiedService(ServiceReference<Object> reference, Object service) {}

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
            }
        );
        tracker.open();
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        if (tracker != null) {
            tracker.close();
            tracker = null;
        }
    }

    private ObjectName createObjectName() throws MalformedObjectNameException {
        return new ObjectName("org.apache.felix.eventadmin.monitoring", "type", "EventAdminMBean");
    }

}
