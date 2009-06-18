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

package com.openexchange.control.osgi;

import static com.openexchange.control.internal.GeneralControl.shutdown;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.control.internal.GeneralControl;
import com.openexchange.management.ManagementException;
import com.openexchange.management.ManagementService;

/**
 * {@link ControlActivator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ControlActivator implements BundleActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ControlActivator.class);

    private ServiceTracker managementServiceTracker;

    private Thread shutdownHookThread;

    /**
     * Initializes a new {@link ControlActivator}
     */
    public ControlActivator() {
        super();
    }

    public void start(final BundleContext context) throws Exception {
        LOG.info("starting bundle: com.openexchange.control");
        try {
            /*
             * Create & open service tracker
             */
            managementServiceTracker = new ServiceTracker(
                context,
                ManagementService.class.getName(),
                new ManagementServiceTrackerCustomizer(context, LOG));
            managementServiceTracker.open();
            /*
             * Add shutdown hook
             */
            shutdownHookThread = new ControlShutdownHookThread(context);
            Runtime.getRuntime().addShutdownHook(shutdownHookThread);

        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    public void stop(final BundleContext context) throws Exception {
        LOG.info("stopping bundle: com.openexchange.control");

        try {
            if (null != shutdownHookThread) {
                if (!shutdownHookThread.isAlive()) {
                    /*
                     * Remove shutdown hook if not running. Otherwise stop() is invoked by the thread itself.
                     */
                    try {
                        if (!Runtime.getRuntime().removeShutdownHook(shutdownHookThread)) {
                            LOG.error("com.openexchange.control shutdown hook could not be deregistered");
                        }
                    } catch (final IllegalStateException e) {
                        /*
                         * Just for safety reason...
                         */
                        LOG.error("Virtual machine is already in the process of shutting down!", e);
                    }
                }
                shutdownHookThread = null;
            }

            if (null != managementServiceTracker) {
                managementServiceTracker.close();
                managementServiceTracker = null;
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    private final class ManagementServiceTrackerCustomizer implements ServiceTrackerCustomizer {

        private final BundleContext bundleContext;

        private final org.apache.commons.logging.Log logger;

        public ManagementServiceTrackerCustomizer(final BundleContext bundleContext, final org.apache.commons.logging.Log logger) {
            super();
            this.bundleContext = bundleContext;
            this.logger = logger;
        }

        public Object addingService(final ServiceReference reference) {
            final Object addedService = bundleContext.getService(reference);
            if (!(addedService instanceof ManagementService)) {
                bundleContext.ungetService(reference);
                return null;
            }
            try {
                ((ManagementService) addedService).registerMBean(
                    new ObjectName("com.openexchange.control", "name", "Control"),
                    new GeneralControl(bundleContext));
                logger.info("Control MBean successfully registered.");
                return addedService;
            } catch (final MalformedObjectNameException e) {
                logger.error("Control MBean registration failed.", e);
                bundleContext.ungetService(reference);
            } catch (final ManagementException e) {
                logger.error("Control MBean registration failed.", e);
                bundleContext.ungetService(reference);
            } catch (final NullPointerException e) {
                logger.error("Control MBean registration failed.", e);
                bundleContext.ungetService(reference);
            }
            return null;
        }

        public void modifiedService(final ServiceReference reference, final Object service) {
            // Nothing to do
        }

        public void removedService(final ServiceReference reference, final Object service) {
            if (null != service) {
                try {
                    ((ManagementService) service).unregisterMBean(new ObjectName("com.openexchange.control", "name", "Control"));
                    logger.info("Control MBean successfully unregistered.");
                } catch (final MalformedObjectNameException e) {
                    logger.error("Control MBean unregistration failed.", e);
                } catch (final ManagementException e) {
                    logger.error("Control MBean unregistration failed.", e);
                } catch (final NullPointerException e) {
                    logger.error("Control MBean unregistration failed.", e);
                } finally {
                    bundleContext.ungetService(reference);
                }
            }
        }
    } // End of ManagementServiceTrackerCustomizer

    /**
     * {@link ControlShutdownHookThread} - The shutdown hook thread of control bundle.
     * 
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    private final class ControlShutdownHookThread extends Thread {

        private final BundleContext bundleContext;

        /**
         * Initializes a new {@link ControlShutdownHookThread}
         * 
         * @param bundleContext The bundle context
         */
        public ControlShutdownHookThread(final BundleContext bundleContext) {
            super();
            this.bundleContext = bundleContext;
        }

        @Override
        public void run() {
            shutdown(bundleContext, true);
        }

    } // End of ControlShutdownHookThread
}
