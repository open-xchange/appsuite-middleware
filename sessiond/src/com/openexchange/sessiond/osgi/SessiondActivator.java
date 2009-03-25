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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.sessiond.osgi;

import static com.openexchange.sessiond.services.SessiondServiceRegistry.getServiceRegistry;
import java.util.ArrayList;
import java.util.List;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.management.ManagementException;
import com.openexchange.management.ManagementService;
import com.openexchange.server.ServiceException;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;
import com.openexchange.sessiond.SessiondMBean;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.cache.SessionCache;
import com.openexchange.sessiond.cache.SessionCacheConfiguration;
import com.openexchange.sessiond.impl.SessionControl;
import com.openexchange.sessiond.impl.SessionHandler;
import com.openexchange.sessiond.impl.SessionImpl;
import com.openexchange.sessiond.impl.SessiondInit;
import com.openexchange.sessiond.impl.SessiondMBeanImpl;
import com.openexchange.sessiond.impl.SessiondServiceImpl;
import com.openexchange.timer.Timer;

/**
 * {@link SessiondActivator} - Activator for sessiond bundle.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessiondActivator extends DeferredActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(SessiondActivator.class);

    private ServiceRegistration sessiondServiceRegistration;

    private ObjectName objectName;

    private final List<ServiceTracker> trackers;

    /**
     * Initializes a new {@link SessiondActivator}
     */
    public SessiondActivator() {
        super();
        trackers = new ArrayList<ServiceTracker>(2);
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, CacheService.class, EventAdmin.class, Timer.class };
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        /*
         * Don't stop the sessiond
         */
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }
        if (CacheService.class.equals(clazz)) {
            try {
                SessionCacheConfiguration.getInstance().stop();
            } catch (final AbstractOXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        getServiceRegistry().addService(clazz, getService(clazz));
        if (CacheService.class.equals(clazz)) {
            try {
                SessionCacheConfiguration.getInstance().start();
            } catch (final AbstractOXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            {
                final ServiceRegistry registry = getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (int i = 0; i < classes.length; i++) {
                    final Object service = getService(classes[i]);
                    if (null != service) {
                        registry.addService(classes[i], service);
                    }
                }
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("starting bundle: com.openexchange.sessiond");
            }
            SessiondInit.getInstance().start();
            sessiondServiceRegistration = context.registerService(SessiondService.class.getName(), new SessiondServiceImpl(), null);
            trackers.add(new ServiceTracker(context, ManagementService.class.getName(), new ServiceTrackerCustomizer() {

                public Object addingService(final ServiceReference reference) {
                    final ManagementService management = (ManagementService) context.getService(reference);
                    registerSessiondMBean(management);
                    return management;
                }

                public void modifiedService(final ServiceReference reference, final Object service) {
                    // Nothing to do.
                }

                public void removedService(final ServiceReference reference, final Object service) {
                    final ManagementService management = (ManagementService) service;
                    unregisterSessiondMBean(management);
                    context.ungetService(reference);
                }
            }));
            for (final ServiceTracker tracker : trackers) {
                tracker.open();
            }
        } catch (final Exception e) {
            LOG.error("SessiondActivator: start: ", e);
            // Try to stop what already has been started.
            SessiondInit.getInstance().stop();
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("stopping bundle: com.openexchange.sessiond");
        }
        try {
            if (null != sessiondServiceRegistration) {
                sessiondServiceRegistration.unregister();
                sessiondServiceRegistration = null;
            }
            for (final ServiceTracker tracker : trackers) {
                tracker.close();
            }
            trackers.clear();
            /*
             * Put remaining sessions into cache for remote distribution
             */
            final List<SessionControl> sessions = SessionHandler.getSessions();
            try {
                for (final SessionControl sessionControl : sessions) {
                    if (null != sessionControl) {
                        SessionCache.getInstance().putCachedSession(((SessionImpl) (sessionControl.getSession())).createCachedSession());
                    }
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info("stopping bundle:\n" + "Remaining active sessions were put into session cache for remote distribution\n");
                }
            } catch (final ServiceException e) {
                LOG.warn(
                    "Missing caching service." + " Remaining active sessions could not be put into session cache for remote distribution",
                    e);
            }
            /*
             * Stop sessiond
             */
            SessiondInit.getInstance().stop();
            /*
             * Clear service registry
             */
            getServiceRegistry().clearRegistry();
        } catch (final Exception e) {
            LOG.error("SessiondActivator: stop: ", e);
            throw e;
        }
    }

    private void registerSessiondMBean(final ManagementService management) {
        if (objectName == null) {
            try {
                objectName = getObjectName(SessiondMBeanImpl.class.getName(), SessiondMBean.SESSIOND_DOMAIN);
                management.registerMBean(objectName, new SessiondMBeanImpl());
            } catch (final MalformedObjectNameException e) {
                LOG.error(e.getMessage(), e);
            } catch (final NotCompliantMBeanException e) {
                LOG.error(e.getMessage(), e);
            } catch (final ManagementException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void unregisterSessiondMBean(final ManagementService management) {
        if (objectName != null) {
            try {
                management.unregisterMBean(objectName);
            } catch (final ManagementException e) {
                LOG.error(e.getMessage(), e);
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
