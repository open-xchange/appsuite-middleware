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

package com.openexchange.http.grizzly.osgi;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
import com.openexchange.java.Strings;
import com.openexchange.startup.SignalHttpApiAvailabilityService;
import com.openexchange.startup.SignalStartedService;
import com.openexchange.startup.StaticSignalStartedService;
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

            boolean isOk = true;
            StaticSignalStartedService singleton = null;
            if (service instanceof StaticSignalStartedService) {
                singleton = (StaticSignalStartedService) service;
                if (StaticSignalStartedService.State.OK != singleton.getState()) {
                    // Error during start-up...
                    isOk = false;
                }
            }

            if (isOk) {
                try {
                    grizzly.startListeners();
                    LOGGER.info("Registered Grizzly HttpNetworkListener on host: {} and port: {}", grizzlyConfig.getHttpHost(), Integer.valueOf(grizzlyConfig.getHttpPort()));
                    availabilityReg = context.registerService(SignalHttpApiAvailabilityService.class, new SignalHttpApiAvailabilityService() {/*nothing inside*/}, null);
                } catch (Exception e) {
                    LOGGER.error(" ---=== /!\\ ===--- Grizzly network listeners could not be started! ---=== /!\\ ===--- ", e);
                }
            } else {
                String sep = Strings.getLineSeparator();
                LOGGER.error("{}\t ---=== /!\\ ===--- Grizzly network listeners not started due to start-up error! ---=== /!\\ ===--- {}", sep, sep);
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
                    Future<Void> shutDownTask = initiateShutDown(doQuit);

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
            grizzly.shutdownNow();
            LOGGER.info("Grizzly stopped.");

            // Unget service
            context.ungetService(reference);
        }
    }

    private Future<Void> initiateShutDown(final AtomicBoolean doQuit) {
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
