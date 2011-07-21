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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajp13.osgi;

import static com.openexchange.ajp13.AJPv13ServiceRegistry.getServiceRegistry;
import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13Server;
import com.openexchange.ajp13.monitoring.AJPv13Monitors;
import com.openexchange.ajp13.najp.threadpool.AJPv13SynchronousQueueProvider;
import com.openexchange.ajp13.servlet.http.osgi.HttpServiceImpl;
import com.openexchange.ajp13.xajp.XAJPv13Server;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.management.ManagementService;
import com.openexchange.server.Initialization;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link AJPv13Activator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13Activator extends DeferredActivator {

    /**
     * The logger.
     */
    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(AJPv13Activator.class));

    private static final int AJP_MODE_STABLE = 1;

    private static final int AJP_MODE_THREAD_POOL = 2;

    private static final int AJP_MODE_NIO = 3;

    private List<Initialization> inits;

    private int mode;

    private List<ServiceTracker> trackers;

    private List<ServiceRegistration> registrations;

    /**
     * Initializes a new {@link AJPv13Activator}.
     */
    public AJPv13Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, TimerService.class, ThreadPoolService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        final org.apache.commons.logging.Log logger = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(AJPv13Activator.class));
        if (logger.isInfoEnabled()) {
            logger.info("Re-available service: " + clazz.getName());
        }
        final Object service = getService(clazz);
        getServiceRegistry().addService(clazz, service);
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        final org.apache.commons.logging.Log logger = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(AJPv13Activator.class));
        if (logger.isWarnEnabled()) {
            logger.warn("Absent service: " + clazz.getName());
        }
        getServiceRegistry().removeService(clazz);
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
                for (final Class<?> classe : classes) {
                    final Object service = getService(classe);
                    if (null != service) {
                        registry.addService(classe, service);
                    }
                }
            }
            inits = new ArrayList<Initialization>(2);
            /*
             * Set starter dependent on mode
             */
            mode = AJP_MODE_THREAD_POOL;
            switch (mode) {
            case AJP_MODE_STABLE:
                inits.add(new AJPStableStarter());
                break;
            case AJP_MODE_THREAD_POOL:
                inits.add(new NAJPStarter());
                break;
            case AJP_MODE_NIO:
                inits.add(new XAJPStarter());
                break;
            default:
                throw new IllegalArgumentException("Unknown AJP mode: " + mode);
            }
            inits.add(com.openexchange.ajp13.servlet.http.HttpManagersInit.getInstance());
            /*
             * Start
             */
            for (final Initialization initialization : inits) {
                initialization.start();
            }
            if (LOG.isInfoEnabled()) {
                final String prefix = ((AJP_MODE_STABLE == mode) ? "Stable AJP server " : ((AJP_MODE_NIO == mode) ? "NIO AJP server " : "New AJP server "));
                LOG.info(new StringBuilder(32).append(prefix).append("successfully started.").toString());
            }
            /*
             * Start trackers
             */
            trackers = new ArrayList<ServiceTracker>(1);
            trackers.add(new ServiceTracker(context, ManagementService.class.getName(), new ManagementServiceTracker(context)));
            for (final ServiceTracker tracker : trackers) {
                tracker.open();
            }
            /*
             * Register services
             */
            registrations = new ArrayList<ServiceRegistration>(1);
            final HttpServiceImpl http = new HttpServiceImpl();
            registrations.add(context.registerService(HttpService.class.getName(), http, null));
            http.registerServlet("/servlet/TestServlet", new com.openexchange.ajp13.TestServlet(), null, null);

            /*-
             * Alternative approach for HttpService:
             * 
             * http://www.eclipse.org/equinox/server/
             * http://www.eclipse.org/equinox/server/http_in_equinox.php
             * 
             * http://docs.codehaus.org/display/JETTY/OSGi+Tips
             */

        } catch (final Exception e) {
            org.apache.commons.logging.LogFactory.getLog(AJPv13Activator.class).error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            if (registrations != null) {
                while (!registrations.isEmpty()) {
                    registrations.remove(0).unregister();
                }
                registrations = null;
            }
            if (trackers != null) {
                while (!trackers.isEmpty()) {
                    trackers.remove(0).close();
                }
                trackers = null;
            }
            if (inits != null) {
                while (!inits.isEmpty()) {
                    inits.remove(0).stop();
                }
                inits = null;
            }
            if (LOG.isInfoEnabled()) {
                final String prefix = ((AJP_MODE_STABLE == mode) ? "Stable AJP server " : ((AJP_MODE_NIO == mode) ? "NIO AJP server " : "New AJP server "));
                LOG.info(new StringBuilder(32).append(prefix).append("successfully stopped.").toString());
            }
            /*
             * Clear service registry
             */
            getServiceRegistry().clearRegistry();
        } catch (final Exception e) {
            org.apache.commons.logging.LogFactory.getLog(AJPv13Activator.class).error(e.getMessage(), e);
            throw e;
        }
    }

    private static final class AJPStableStarter implements Initialization {

        public AJPStableStarter() {
            super();
        }

        public void start() throws AbstractOXException {
            AJPv13Server.setInstance(new com.openexchange.ajp13.stable.AJPv13ServerImpl());
            AJPv13Config.getInstance().start();
            AJPv13Monitors.setListenerMonitor(com.openexchange.ajp13.stable.AJPv13ServerImpl.getListenerMonitor());
            AJPv13Server.startAJPServer();
        }

        public void stop() throws AbstractOXException {
            AJPv13Server.stopAJPServer();
            AJPv13Monitors.releaseListenerMonitor();
            AJPv13Config.getInstance().stop();
            AJPv13Server.releaseInstrance();
        }
    }

    private static final class NAJPStarter implements Initialization {

        public NAJPStarter() {
            super();
        }

        public void start() throws AbstractOXException {
            /*
             * Proper synchronous queue
             */
            String property = System.getProperty("java.specification.version");
            if (null == property) {
                property = System.getProperty("java.runtime.version");
                if (null == property) {
                    // JRE not detectable, use fallback
                    AJPv13SynchronousQueueProvider.initInstance(false);
                } else {
                    // "java.runtime.version=1.6.0_0-b14" OR "java.runtime.version=1.5.0_18-b02"
                    AJPv13SynchronousQueueProvider.initInstance(!property.startsWith("1.5"));
                }
            } else {
                // "java.specification.version=1.5" OR "java.specification.version=1.6"
                AJPv13SynchronousQueueProvider.initInstance("1.5".compareTo(property) < 0);
            }
            AJPv13Server.setInstance(new com.openexchange.ajp13.najp.AJPv13ServerImpl());
            AJPv13Config.getInstance().start();
            AJPv13Server.startAJPServer();
        }

        public void stop() throws AbstractOXException {
            com.openexchange.ajp13.najp.AJPv13ServerImpl.stopAJPServer();
            AJPv13Config.getInstance().stop();
            AJPv13Server.releaseInstrance();
            AJPv13SynchronousQueueProvider.releaseInstance();
        }
    }

    private static final class XAJPStarter implements Initialization {

        public XAJPStarter() {
            super();
        }

        public void start() throws AbstractOXException {
            AJPv13Config.getInstance().start();
            XAJPv13Server.getInstance().start();
        }

        public void stop() throws AbstractOXException {
            AJPv13Config.getInstance().stop();
            XAJPv13Server.getInstance().close();
            XAJPv13Server.releaseInstance();
        }

    }

}
