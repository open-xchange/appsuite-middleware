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

package com.openexchange.http.grizzly.osgi;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.OXHttpServer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.http.grizzly.GrizzlyConfig;
import com.openexchange.http.grizzly.service.http.HttpServiceFactory;
import com.openexchange.http.grizzly.service.http.OSGiCleanMapper;
import com.openexchange.http.grizzly.service.http.OSGiHandler;
import com.openexchange.http.grizzly.service.http.OSGiMainHandler;
import com.openexchange.http.grizzly.service.http.OSGiServletHandler;
import com.openexchange.http.grizzly.util.ThreadControlReference;
import com.openexchange.startup.SignalHttpApiAvailabilityService;
import com.openexchange.startup.SignalStartedService;
import com.openexchange.startup.ThreadControlService;

/**
 * {@link StartUpTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.0
 */
public final class StartUpTracker implements ServiceTrackerCustomizer<SignalStartedService, SignalStartedService> {

    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StartUpTracker.class);

    private final BundleContext context;
    private final OXHttpServer grizzly;
    private final GrizzlyConfig grizzlyConfig;
    private ServiceRegistration<SignalHttpApiAvailabilityService> availabilityReg;
    final ServiceRegistration<HttpService> httpServiceRegistration;

    /**
     * Initializes a new {@link StartUpTracker}.
     */
    public StartUpTracker(HttpServiceFactory httpServiceFactory, OXHttpServer grizzly, GrizzlyConfig grizzlyConfig, BundleContext context) {
        super();
        OSGiMainHandler.unmarkShutdownRequested();
        this.grizzly = grizzly;
        this.grizzlyConfig = grizzlyConfig;
        this.context = context;

        if (grizzlyConfig.isShutdownFast()) {
            httpServiceRegistration = null;
        } else {
            /*-
             * Servicefactory that creates instances of the HttpService interface that grizzly implements. Each distinct bundle that uses
             * getService() will get its own instance of HttpServiceImpl
             *
             * The HttpService is managed by this tracker (tracking SignalStartedService) to have that HttpService be unregistered as first
             * step when performing a shut-down of the server. Thus we ensure currently running requests are completed prior to dropping any
             * services; see com.openexchange.http.grizzly.service.http.OSGiCleanMapper.doUnregister(String, boolean).
             */
            httpServiceRegistration = context.registerService(HttpService.class, httpServiceFactory, null);
            LOGGER.info("Registered OSGi HttpService for Grizzly server.");
        }
    }

    @Override
    public SignalStartedService addingService(final ServiceReference<SignalStartedService> reference) {
        synchronized (grizzlyConfig) {
            SignalStartedService service = context.getService(reference);

            try {
                grizzly.startListeners();
                LOGGER.info("Registered Grizzly HttpNetworkListener on host: {} and port: {}", grizzlyConfig.getHttpHost(), Integer.valueOf(grizzlyConfig.getHttpPort()));
                availabilityReg = context.registerService(SignalHttpApiAvailabilityService.class, new SignalHttpApiAvailabilityService() {}, null);
            } catch (final Exception e) {
                LOGGER.error(" ---=== /!\\ ===--- Network listeners could not be started! ---=== /!\\ ===--- ", e);
            }

            return service;
        }
    }

    @Override
    public void modifiedService(final ServiceReference<SignalStartedService> reference, final SignalStartedService service) {
        // Nope
    }

    @Override
    public void removedService(final ServiceReference<SignalStartedService> reference, final SignalStartedService service) {
        synchronized (grizzlyConfig) {
            OSGiMainHandler.markShutdownRequested();

            ThreadControlService threadControl = ThreadControlReference.getThreadControlService();
            if (null != threadControl) {
                threadControl.interruptAll();
            }

            if (availabilityReg != null) {
                availabilityReg.unregister();
                availabilityReg = null;
            }

            if (!grizzlyConfig.isShutdownFast()) {
                // Check the number of seconds to await the shut-down
                int awaitShutDownSeconds = grizzlyConfig.getAwaitShutDownSeconds();
                if (awaitShutDownSeconds > 0) {
                    // Perform an orderly shut-down
                    AtomicBoolean doQuit = new AtomicBoolean(false);
                    FutureTask<Void> shutDownTask = initiateShutDown(doQuit);

                    // Await shut-down
                    try {
                        LOGGER.info("Awaiting orderly shut-down...");
                        shutDownTask.get(awaitShutDownSeconds, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.warn("Awaiting shut-down was interrupted", e);
                    } catch (ExecutionException e) {
                        LOGGER.error("Failed to await shut-down", e.getCause());
                    } catch (TimeoutException x) {
                        LOGGER.info("Timed out while awaiting shut-down");
                        doQuit.set(true);
                        shutDownTask.cancel(true);
                    }
                } else {
                    LOGGER.info("Performing orderly shut-down...");
                    List<Lock> acquiredLocks = new LinkedList<Lock>();
                    try {
                        for (HttpHandler httpHandler : OSGiCleanMapper.getAllHttpHandlers()) {
                            if (httpHandler instanceof OSGiServletHandler) {
                                WriteLock removalLock = ((OSGiHandler) httpHandler).getRemovalLock();
                                removalLock.lock();
                                acquiredLocks.add(removalLock);
                            }
                        }

                        if (null != httpServiceRegistration) {
                            httpServiceRegistration.unregister();
                            LOGGER.info("Unregistered OSGi HttpService for Grizzly server.");
                        }
                    } finally {
                        for (Lock lock : acquiredLocks) {
                            lock.unlock();
                        }
                    }
                }
            }

            // Stop Grizzly
            grizzly.stop();
            LOGGER.info("Grizzly stopped.");

            // Unget service
            context.ungetService(reference);
        }
    }

    private FutureTask<Void> initiateShutDown(final AtomicBoolean doQuit) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                Thread current = Thread.currentThread();

                LOGGER.info("Performing orderly shut-down...");
                List<Lock> acquiredLocks = new LinkedList<Lock>();
                try {
                    for (HttpHandler httpHandler : OSGiCleanMapper.getAllHttpHandlers()) {
                        if (doQuit.get() || current.isInterrupted()) {
                            return;
                        }
                        if (httpHandler instanceof OSGiServletHandler) {
                            WriteLock removalLock = ((OSGiHandler) httpHandler).getRemovalLock();
                            removalLock.lock();
                            acquiredLocks.add(removalLock);
                        }
                        if (doQuit.get() || current.isInterrupted()) {
                            return;
                        }
                    }

                    if (null != httpServiceRegistration) {
                        httpServiceRegistration.unregister();
                        LOGGER.info("Unregistered OSGi HttpService for Grizzly server.");
                    }
                } finally {
                    for (Lock lock : acquiredLocks) {
                        lock.unlock();
                    }
                }
            }
        };

        FutureTask<Void> shutDownTask = new FutureTask<Void>(r, null);
        new Thread(shutDownTask, "Grizzly Shut-Down Performer").start();
        return shutDownTask;
    }

}
