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

package com.openexchange.threadpool.osgi;

import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.management.ManagementService;
import com.openexchange.session.SessionThreadCounter;
import com.openexchange.sessionCount.SessionThreadCountMBean;
import com.openexchange.sessionCount.SessionThreadCountMBeanImpl;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link ManagementServiceTrackerCustomizer2} - The {@link ServiceTrackerCustomizer customizer} for {@link ManagementService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ManagementServiceTrackerCustomizer2 implements ServiceTrackerCustomizer<ManagementService, ManagementService> {

    private final BundleContext context;

    private final SessionThreadCounter counter;
    private final ServiceTracker<SessiondService, SessiondService> sessiondServiceTracker;

    private ObjectName objectName;

    /**
     * Initializes a new {@link ManagementServiceTrackerCustomizer}.
     *
     * @param context The bundle context
     * @param counter The service
     */
    public ManagementServiceTrackerCustomizer2(final BundleContext context, final SessionThreadCounter counter, final ServiceTracker<SessiondService, SessiondService> sessiondServiceTracker) {
        super();
        this.context = context;
        this.counter = counter;
        this.sessiondServiceTracker = sessiondServiceTracker;
    }

    @Override
    public ManagementService addingService(final ServiceReference<ManagementService> reference) {
        final ManagementService service = context.getService(reference);
        /*
         * Register MBean for thread pool
         */
        registerCacheMBean(service);
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<ManagementService> reference, final ManagementService service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<ManagementService> reference, final ManagementService service) {
        if (null != service && ManagementService.class.isInstance(service)) {
            try {
                /*
                 * Unregister MBean for thread pool
                 */
                unregisterCacheMBean(service);
            } finally {
                context.ungetService(reference);
            }
        }
    }

    void registerCacheMBean(final ManagementService management) {
        if (objectName == null) {
            final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ManagementServiceTrackerCustomizer2.class);
            try {
                objectName = getObjectName(SessionThreadCountMBeanImpl.class.getName(), SessionThreadCountMBean.SESSION_THREAD_COUNT_DOMAIN);
                management.registerMBean(objectName, new SessionThreadCountMBeanImpl(counter, sessiondServiceTracker));
            } catch (final MalformedObjectNameException e) {
                LOG.error("", e);
            } catch (final NotCompliantMBeanException e) {
                LOG.error("", e);
            } catch (final Exception e) {
                LOG.error("", e);
            }
        }
    }

    void unregisterCacheMBean(final ManagementService management) {
        if (objectName != null) {
            try {
                management.unregisterMBean(objectName);
            } catch (final Exception e) {
                org.slf4j.LoggerFactory.getLogger(ManagementServiceTrackerCustomizer2.class).error("", e);
            } finally {
                objectName = null;
            }
        }
    }

    /**
     * Creates an appropriate instance of {@link ObjectName} from specified class name and domain name.
     *
     * @param className The class name to use as object name
     * @param domain The domain name
     * @return An appropriate instance of {@link ObjectName}
     * @throws MalformedObjectNameException If instantiation of {@link ObjectName} fails
     */
    private static ObjectName getObjectName(final String className, final String domain) throws MalformedObjectNameException {
        final int pos = className.lastIndexOf('.');
        return new ObjectName(domain, "name", pos == -1 ? className : className.substring(pos + 1));
    }

}
