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

package com.openexchange.control.osgi;

import static com.openexchange.control.internal.GeneralControl.shutdown;
import org.osgi.framework.BundleContext;
import com.openexchange.control.internal.GeneralControl;
import com.openexchange.control.internal.GeneralControlMBean;
import com.openexchange.management.ManagementService;
import com.openexchange.management.osgi.HousekeepingManagementTracker;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.version.VersionService;

/**
 * {@link ControlActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ControlActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ControlActivator.class);

    private Thread shutdownHookThread;

    /**
     * Initializes a new {@link ControlActivator}
     */
    public ControlActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { VersionService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        LOG.info("starting bundle: com.openexchange.control");
        try {
            /*
             * Create & open service tracker
             */
            track(ManagementService.class, new HousekeepingManagementTracker(context, GeneralControlMBean.MBEAN_NAME, GeneralControlMBean.DOMAIN, new GeneralControl(context, getServiceSafe(VersionService.class))));
            openTrackers();
            /*
             * Add shutdown hook
             */
            final Thread shutdownHookThread = new ControlShutdownHookThread(context);
            Runtime.getRuntime().addShutdownHook(shutdownHookThread);
            this.shutdownHookThread = shutdownHookThread;
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.control");
        try {
            final Thread shutdownHookThread = this.shutdownHookThread;
            if (null != shutdownHookThread) {
                if (!shutdownHookThread.isAlive()) {
                    /*
                     * Remove shutdown hook if not running. Otherwise stop() is invoked by the thread itself.
                     */
                    try {
                        if (!Runtime.getRuntime().removeShutdownHook(shutdownHookThread)) {
                            LOG.error("com.openexchange.control shutdown hook could not be deregistered");
                        }
                    } catch (IllegalStateException e) {
                        /*
                         * Just for safety reason...
                         */
                        LOG.error("Virtual machine is already in the process of shutting down!", e);
                    }
                }
                this.shutdownHookThread = null;
            }
            // Do bundle clean-up
            cleanUp();
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    /**
     * {@link ControlShutdownHookThread} - The shutdown hook thread of control bundle.
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    private static final class ControlShutdownHookThread extends Thread {

        private final BundleContext bundleContext;

        /**
         * Initializes a new {@link ControlShutdownHookThread}
         *
         * @param bundleContext The bundle context
         */
        ControlShutdownHookThread(final BundleContext bundleContext) {
            super();
            this.bundleContext = bundleContext;
        }

        @Override
        public void run() {
            shutdown(bundleContext, true);
        }

    } // End of ControlShutdownHookThread

}
