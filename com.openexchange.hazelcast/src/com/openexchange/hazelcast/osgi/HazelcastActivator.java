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


package com.openexchange.hazelcast.osgi;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.OutOfMemoryHandler;
import com.hazelcast.instance.OutOfMemoryErrorDispatcher;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.hazelcast.HazelcastMBeanImpl;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.management.ManagementService;

/**
 * {@link HazelcastActivator} - The activator for Hazelcast bundle (registers a {@link HazelcastInstance} for this JVM)
 * <p>
 * When should you add node?<br>
 * 1. You reached the limits of your CPU or RAM.<br>
 * 2. You reached the limits of GC. You started seeing full-GC
 * <p>
 * When should you stop adding nodes? Should you have 10, 30, 50, 100, or 1000 nodes?<br>
 * 1. You reached the limits of your network. Your switch is not able to handle the amount of data passed around.<br>
 * 2. You reached the limits of the way application utilizing Hazelcast.<br>
 * Adding node is not increasing your total throughput and not reducing the latency.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HazelcastActivator implements BundleActivator {

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastActivator.class);

    private boolean stopped; // Guarded by synchronized

    volatile ServiceTracker<HazelcastConfigurationService, HazelcastConfigurationService> configTracker;
    volatile ServiceTracker<HazelcastInstanceNotActiveException, HazelcastInstanceNotActiveException> inactiveTracker;
    volatile ServiceTracker<ManagementService, ManagementService> managementTracker;
    volatile ServiceRegistration<HazelcastInstance> serviceRegistration;
    volatile HazelcastInstance hazelcastInstance;

    /**
     * Initializes a new {@link HazelcastActivator}.
     */
    public HazelcastActivator() {
        super();
        stopped = false;
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        /*
         * track HazelcastConfigurationService
         */
        synchronized (this) {
            stopped = false;
        }
        ServiceTrackerCustomizer<HazelcastConfigurationService, HazelcastConfigurationService> customizer =
            new ServiceTrackerCustomizer<HazelcastConfigurationService, HazelcastConfigurationService>() {

            @Override
            public HazelcastConfigurationService addingService(ServiceReference<HazelcastConfigurationService> reference) {
                HazelcastConfigurationService configService = context.getService(reference);
                try {
                    if (configService.isEnabled()) {
                        HazelcastInstance hazelcast = startHazelcastInstance(configService);
                        // hazelcast = new InactiveAwareHazelcastInstance(hazelcast, HazelcastActivator.this);
                        if (null != hazelcast) {
                            serviceRegistration = context.registerService(HazelcastInstance.class, hazelcast, null);
                            hazelcastInstance = hazelcast;
                            HazelcastMBeanImpl.setHazelcastInstance(hazelcast);
                        }
                    } else {
                        String lf = Strings.getLineSeparator();
                        LOG.info("{}Hazelcast:{}    Startup of Hazelcast clustering and data distribution platform denied per configuration.{}", lf, lf, lf);
                    }
                } catch (Exception e) {
                    String msg = "Error starting \"com.openexchange.hazelcast\"";
                    LOG.error(msg, e);
                    throw new IllegalStateException(msg, new BundleException(msg, BundleException.ACTIVATOR_ERROR, e));
                }
                return configService;
            }

            @Override
            public void modifiedService(ServiceReference<HazelcastConfigurationService> reference, HazelcastConfigurationService service) {
                // nothing to do
            }

            @Override
            public void removedService(ServiceReference<HazelcastConfigurationService> reference, HazelcastConfigurationService service) {
                context.ungetService(reference);
                stop();
            }
        };
        ServiceTracker<HazelcastConfigurationService, HazelcastConfigurationService> configTracker = new ServiceTracker<HazelcastConfigurationService, HazelcastConfigurationService>(context, HazelcastConfigurationService.class, customizer);
        this.configTracker = configTracker;
        configTracker.open();
        /*
         * track HazelcastInstanceNotActiveException
         */
        ServiceTrackerCustomizer<HazelcastInstanceNotActiveException, HazelcastInstanceNotActiveException> stc =
            new ServiceTrackerCustomizer<HazelcastInstanceNotActiveException, HazelcastInstanceNotActiveException>() {

            @Override
            public void removedService(ServiceReference<HazelcastInstanceNotActiveException> reference, HazelcastInstanceNotActiveException service) {
                context.ungetService(reference);
            }

            @Override
            public void modifiedService(ServiceReference<HazelcastInstanceNotActiveException> reference, HazelcastInstanceNotActiveException service) {
                // Nothing to do
            }

            @Override
            public HazelcastInstanceNotActiveException addingService(ServiceReference<HazelcastInstanceNotActiveException> reference) {
                HazelcastInstanceNotActiveException notActiveException = context.getService(reference);

                String lf = Strings.getLineSeparator();
                LOG.warn("{}Hazelcast:{}    Detected a {}. Hazelcast is going to be shut-down!{}", lf, lf, HazelcastInstanceNotActiveException.class.getSimpleName(), lf);

                stop();
                return notActiveException;
            }
        };
        ServiceTracker<HazelcastInstanceNotActiveException, HazelcastInstanceNotActiveException> inactiveTracker = new ServiceTracker<HazelcastInstanceNotActiveException, HazelcastInstanceNotActiveException>(context, HazelcastInstanceNotActiveException.class, stc);
        this.inactiveTracker = inactiveTracker;
        inactiveTracker.open();
        /*
         * track ManagementService
         */
        ServiceTracker<ManagementService, ManagementService> managementTracker = new ServiceTracker<ManagementService, ManagementService>(context, ManagementService.class, new ManagementRegisterer(context));
        this.managementTracker = managementTracker;
        managementTracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        stop();
    }

    /**
     * Stops Hazelcast.
     */
    protected synchronized void stop() {
        if (stopped) {
            return;
        }

        stopped = true;
        ServiceRegistration<HazelcastInstance> serviceRegistration = this.serviceRegistration;
        if (null != serviceRegistration) {
            serviceRegistration.unregister();
            this.serviceRegistration = null;
        }
        closeTrackers();
        stopHazelcastInstance();
    }

    /**
     * Closes all opened service trackers.
     */
    void closeTrackers() {
        ServiceTracker<HazelcastConfigurationService, HazelcastConfigurationService> tracker = this.configTracker;
        if (null != tracker) {
            this.configTracker = null;
            closeTrackerSafe(tracker);
        }
        ServiceTracker<HazelcastInstanceNotActiveException, HazelcastInstanceNotActiveException> inactiveTracker = this.inactiveTracker;
        if (null != inactiveTracker) {
            this.inactiveTracker = null;
            closeTrackerSafe(inactiveTracker);
        }
        ServiceTracker<ManagementService, ManagementService> managementTracker = this.managementTracker;
        if (null != managementTracker) {
            this.managementTracker = null;
            closeTrackerSafe(managementTracker);
        }
    }

    private <S, T> void closeTrackerSafe(ServiceTracker<S, T> tracker) {
        if (null != tracker) {
            try {
                tracker.close();
            } catch (java.lang.IllegalStateException e) {
                // Apparently already closed, since BundleContext is no longer valid
            }
        }
    }

    void stopHazelcastInstance() {
        HazelcastInstance hazelcast = this.hazelcastInstance;
        if (null != hazelcast) {
            this.hazelcastInstance = null;
            HazelcastMBeanImpl.setHazelcastInstance(null);

            // Do shut-down
            String lf = Strings.getLineSeparator();
            long start = System.currentTimeMillis();
            Future<Void> shutDownTask = initiateShutDown(hazelcast);
            try {
                LOG.info("{}Hazelcast:{}    Awaiting graceful Hazelcast shut-down...{}", lf, lf, lf);
                shutDownTask.get(10L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.warn("{}Hazelcast:{}    Awaiting graceful Hazecast shut-down was interrupted{}", lf, lf, lf, e);
            } catch (ExecutionException e) {
                LOG.error("{}Hazelcast:{}    Failed to await graceful Hazecast shut-down{}", lf, lf, lf, e.getCause());
            } catch (TimeoutException x) {
                LOG.info("{}Hazelcast:{}    Timed out while awaiting graceful Hazecast shut-down. Forcing immediate shut-down...{}", lf, lf, lf);
                shutDownTask.cancel(true);
                hazelcast.getLifecycleService().terminate();
            }
            LOG.info("{}Hazelcast:{}    Shutdown completed after {} msec.{}", lf, lf, (System.currentTimeMillis() - start), lf);
        }
    }

    HazelcastInstance startHazelcastInstance(HazelcastConfigurationService configService) throws Exception {
        String lf = Strings.getLineSeparator();
        LOG.info("{}Hazelcast:{}    Starting...{}", lf, lf, lf);
        if (false == configService.isEnabled()) {
            LOG.info("{}Hazelcast:{}    Startup of Hazelcast clustering and data distribution platform denied per configuration.{}", lf, lf, lf);
            return null;
        }

        // Create Hazelcast instance from configuration
        Config config = configService.getConfig();
        {
            LOG.info("{}Hazelcast:{}    Creating new hazelcast instance...{}", lf, lf, lf);
            if (config.getNetworkConfig().getJoin().getMulticastConfig().isEnabled()) {
                LOG.info("{}Hazelcast:{}    Using network join: {}{}", lf, lf, config.getNetworkConfig().getJoin().getMulticastConfig(), lf);
            }
            if (config.getNetworkConfig().getJoin().getTcpIpConfig().isEnabled()) {
                LOG.info("{}Hazelcast:{}    Using network join: {}{}", lf, lf, config.getNetworkConfig().getJoin().getTcpIpConfig(), lf);
            }
        }

        // Custom OutOfMemoryHandler implementation
        final boolean shutdownOnOutOfMemory = configService.shutdownOnOutOfMemory();
        OutOfMemoryHandler handler = new OutOfMemoryHandler() {

            @Override
            public void onOutOfMemory(OutOfMemoryError oom, HazelcastInstance[] hazelcastInstances) {
                if (shutdownOnOutOfMemory) {
                    try {
                        stop();
                    } catch (Exception e) {
                        LOG.error("Failed to shut-down Hazelcast", e);
                    }
                }
                ExceptionUtils.handleOOM(oom);
            }
        };
        OutOfMemoryErrorDispatcher.setServerHandler(handler);
        long hzStart = System.currentTimeMillis();
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(config);
        LOG.info("{}Hazelcast:{}    New hazelcast instance successfully created in {} msec.{}", lf, lf, (System.currentTimeMillis() - hzStart), lf);
        this.hazelcastInstance = hazelcast;
        return hazelcast;
    }

    private Future<Void> initiateShutDown(final HazelcastInstance hzInstance) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                // Graceful shut-down
                hzInstance.getLifecycleService().shutdown();
            }
        };
        FutureTask<Void> ft = new FutureTask<Void>(r, null);
        new Thread(ft, "Hazelcast Shut-Down Performer").start();
        return ft;
    }

}
