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

package com.openexchange.server.osgiservice;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DeferredActivator} - Supports the deferred starting of a bundle which highly depends on other services.
 * <p>
 * The needed services are specified through providing their classes by {@link #getNeededServices()}.
 * <p>
 * When all needed services are available, the {@link #startBundle()} method is invoked. For each absent service the
 * {@link #handleUnavailability(Class)} method is triggered to let the programmer decide which actions are further taken. In turn, the
 * {@link #handleAvailability(Class)} method is invoked if a service re-appears.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class DeferredActivator implements BundleActivator, ServiceLookup {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(DeferredActivator.class));

    /**
     * The empty class array.
     */
    protected static final Class<?>[] EMPTY_CLASSES = new Class<?>[0];

    private <S> DeferredSTC<S> newDeferredSTC(final Class<? extends S> clazz, final int index, final Map<Class<?>, Object> services) {
        return new DeferredSTC<S>(clazz, index, services);
    }

    private final class DeferredSTC<S> implements ServiceTrackerCustomizer<S, S> {

        private final Class<? extends S> clazz;

        private final int index;

        private final Map<Class<?>, Object> stcServices;

        public DeferredSTC(final Class<? extends S> clazz, final int index, final Map<Class<?>, Object> services) {
            super();
            this.clazz = clazz;
            this.index = index;
            stcServices = services;
        }

        @Override
        public S addingService(final ServiceReference<S> reference) {
            final Object addedService = context.getService(reference);
            if (clazz.isInstance(addedService)) {
                stcServices.put(clazz, addedService);
                /*
                 * Signal availability
                 */
                signalAvailability(index, clazz);
                return clazz.cast(addedService);
            }
            context.ungetService(reference);
            return null;
        }

        @Override
        public void modifiedService(final ServiceReference<S> reference, final S service) {
            // Nothing to do
        }

        @Override
        public void removedService(final ServiceReference<S> reference, final S service) {
            if (null != service) {
                try {
                    if (clazz.isInstance(service)) {
                        /*
                         * Signal unavailability
                         */
                        signalUnavailability(index, clazz);
                        /*
                         * ... and remove from services
                         */
                        stcServices.remove(clazz);
                    }
                } finally {
                    if (null != context) {
                        context.ungetService(reference);
                    }
                }
            }
        }
    }

    /**
     * An atomic boolean to keep track of started/stopped status.
     */
    protected final AtomicBoolean started;

    /**
     * The bit mask reflecting already tracked needed services.
     */
    private int availability;

    /**
     * The bit mask if all needed services are available.
     */
    private int allAvailable;

    /**
     * The execution context of the bundle.
     */
    protected BundleContext context;

    /**
     * The initialized service trackers for needed services.
     */
    private ServiceTracker<?, ?>[] serviceTrackers;

    /**
     * The available service instances.
     */
    private ConcurrentMap<Class<?>, Object> services;

    /**
     * Initializes a new {@link DeferredActivator}.
     */
    protected DeferredActivator() {
        super();
        started = new AtomicBoolean();
    }

    /**
     * Gets the classes of the services which need to be available to start this activator.
     *
     * @return The array of {@link Class} instances of needed services
     */
    protected abstract Class<?>[] getNeededServices();

    /**
     * Handles the (possibly temporary) unavailability of a needed service. The specific activator may decide which actions are further done
     * dependent on given service's class.
     * <p>
     * On the one hand, if the service in question is not needed to further keep on running, it may be discarded. On the other hand, if the
     * service is absolutely needed; the {@link #stopBundle()} method can be invoked.
     *
     * @param clazz The service's class
     */
    protected abstract void handleUnavailability(final Class<?> clazz);

    /**
     * Handles the re-availability of a needed service. The specific activator may decide which actions are further done dependent on given
     * service's class.
     *
     * @param clazz The service's class
     */
    protected abstract void handleAvailability(final Class<?> clazz);

    /**
     * Initializes this deferred activator's members.
     *
     * @throws Exception If no needed services are specified and immediately starting bundle fails
     */
    private final void init() throws Exception {
        final Class<?>[] classes = getNeededServices();
        if (null == classes) {
            services = new ConcurrentHashMap<Class<?>, Object>(1);
            serviceTrackers = new ServiceTracker[0];
            availability = allAvailable = 0;
            startBundle();
        } else {
            final int len = classes.length;
            if (len > 0 && new HashSet<Class<?>>(Arrays.asList(classes)).size() != len) {
                throw new IllegalArgumentException("Duplicate class/interface provided through getNeededServices()");
            }
            services = new ConcurrentHashMap<Class<?>, Object>(len);
            serviceTrackers = new ServiceTracker[len];
            availability = 0;
            allAvailable = (1 << len) - 1;
            /*
             * Initialize service trackers for needed services
             */
            for (int i = 0; i < len; i++) {
                final Class<? extends Object> clazz = classes[i];
                serviceTrackers[i] = new ServiceTracker<Object, Object>(context, clazz.getName(), newDeferredSTC(clazz, i, services));
                serviceTrackers[i].open();
            }
            if (len == 0) {
                startBundle();
            }
        }
    }

    /**
     * Resets this deferred activator's members.
     */
    private final void reset() {
        if (null != serviceTrackers) {
            for (int i = 0; i < serviceTrackers.length; i++) {
                serviceTrackers[i].close();
                serviceTrackers[i] = null;
            }
            serviceTrackers = null;
        }
        availability = 0;
        allAvailable = -1;
        if (null != services) {
            services.clear();
            services = null;
        }
        context = null;
    }

    /**
     * Signals availability of the class whose index in array provided by {@link #getNeededServices()} is equal to given <code>index</code>
     * argument.
     * <p>
     * If not started, yet, and all needed services are available, then the {@link #startBundle()} method is invoked. If already started the
     * service's re-availability is propagated.
     *
     * @param index The class' index
     * @param clazz The service's class
     */
    final void signalAvailability(final int index, final Class<?> clazz) {
        availability |= (1 << index);
        if (started.get()) {
            /*
             * Signal availability of single service
             */
            handleAvailability(clazz);
        } else {
            if (availability == allAvailable) {
                /*
                 * Start bundle
                 */
                try {
                    startBundle();
                    started.set(true);
                } catch (final Exception e) {
                    final Bundle bundle = context.getBundle();
                    final StringBuilder errorBuilder = new StringBuilder(64);
                    if (LOG.isErrorEnabled()) {
                        errorBuilder.append("\nStart-up of bundle \"").append(bundle.getSymbolicName()).append("\" failed: ");
                        final String errorMsg = e.getMessage();
                        if (null == errorMsg || "null".equals(errorMsg)) {
                            errorBuilder.append(e.getClass().getName());
                        } else {
                            errorBuilder.append(errorMsg);
                        }
                        LOG.error(errorBuilder.toString(), e);
                    }
                    /*
                     * Shut-down
                     */
                    reset();
                    if (Bundle.STARTING == bundle.getState()) {
                        /*
                         * Bundle cannot be stopped by same thread if still in STARTING state
                         */
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                shutDownBundle(bundle, errorBuilder);
                            }
                        }).start();
                    } else {
                        shutDownBundle(bundle, errorBuilder);
                    }
                }
            }
        }
    }

    /**
     * Shuts-down specified bundle.
     *
     * @param bundle The bundle to shut-down
     * @param errorBuilder The error string builder
     */
    static void shutDownBundle(final Bundle bundle, final StringBuilder errorBuilder) {
        try {
            /*
             * Stop with Bundle.STOP_TRANSIENT set to zero
             */
            bundle.stop(0);
            if (LOG.isErrorEnabled()) {
                errorBuilder.setLength(0);
                LOG.error(errorBuilder.append("\n\nBundle \"").append(bundle.getSymbolicName()).append("\" stopped.\n"));
            }
        } catch (final BundleException e) {
            if (LOG.isErrorEnabled()) {
                errorBuilder.setLength(0);
                LOG.error(errorBuilder.append("\n\nBundle \"").append(bundle.getSymbolicName()).append("\" could not be stopped.\n"));
            }
        }
    }

    /**
     * Marks the class whose index in array provided by {@link #getNeededServices()} is equal to given <code>index</code> argument as absent
     * and notifies its unavailability.
     *
     * @param index The class' index
     * @param clazz The service's class
     */
    final void signalUnavailability(final int index, final Class<?> clazz) {
        availability &= ~(1 << index);
        if (started.get()) {
            handleUnavailability(clazz);
        }
    }

    @Override
    public final void start(final BundleContext context) throws Exception {
        try {
            this.context = context;
            init();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Called when this bundle is started; meaning all needed services are available. So the framework can perform the bundle-specific
     * activities necessary to start this bundle. This method can be used to register services or to allocate any resources that this bundle
     * needs.
     * <p>
     * This method must complete and return to its caller in a timely manner.
     *
     * @throws Exception If this method throws an exception, this bundle is marked as stopped and the Framework will remove this bundle's
     *             listeners, unregister all services registered by this bundle, and release all services used by this bundle.
     */
    protected abstract void startBundle() throws Exception;

    @Override
    public final void stop(final BundleContext context) throws Exception {
        try {
            stopBundle();
            started.set(false);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } finally {
            reset();
        }
    }

    /**
     * Called when this bundle is stopped so the framework can perform the bundle-specific activities necessary to stop the bundle. In
     * general, this method should undo the work that the BundleActivator.start method started. There should be no active threads that were
     * started by this bundle when this bundle returns. A stopped bundle must not call any Framework objects.
     * <p>
     * This method must complete and return to its caller in a timely manner.
     *
     * @throws Exception If this method throws an exception, the bundle is still marked as stopped, and the Framework will remove the
     *             bundle's listeners, unregister all services registered by the bundle, and release all services used by the bundle.
     */
    protected abstract void stopBundle() throws Exception;

    /**
     * Returns {@link ServiceTracker#getService()} invoked on the service tracker bound to specified class.
     *
     * @param <S> Type of service's class
     * @param clazz The service's class
     * @return The service obtained by service tracker or <code>null</code>
     */
    @Override
    public final <S extends Object> S getService(final Class<? extends S> clazz) {
        if (null == services) {
            /*
             * Services not initialized
             */
            return null;
        }
        final Object service = services.get(clazz);
        if (null == service) {
            /*
             * Given class is not tracked by any service tracker
             */
            return null;
        }
        return clazz.cast(service);
    }

    /**
     * Adds specified service (if absent).
     *
     * @param <S> Type of service's class
     * @param clazz The service's class
     * @param service The service to add
     * @return <code>true</code> if service is added; otherwise <code>false</code> if not initialized or such a service already exists
     */
    protected final <S> boolean addService(final Class<S> clazz, final S service) {
        if (null == services || !clazz.isInstance(service)) {
            /*
             * Services not initialized
             */
            return false;
        }
        return (null == services.putIfAbsent(clazz, service));
    }

    /**
     * Removes specified service.
     *
     * @param <S> Type of service's class
     * @param clazz The service's class
     * @return <code>true</code> if service is removes; otherwise <code>false</code> if not initialized or absent
     */
    protected final <S> boolean removeService(final Class<S> clazz) {
        if (null == services) {
            /*
             * Services not initialized
             */
            return false;
        }
        return (null != services.remove(clazz));
    }

    /**
     * Checks if activator currently holds all needed services.
     *
     * @return <code>true</code> if activator currently holds all needed services; otherwise <code>false</code>
     */
    protected final boolean allAvailable() {
        final Class<?>[] classes = getNeededServices();
        if (null == classes) {
            /*
             * This deferred activator waits for no services
             */
            return true;
        }
        final int len = classes.length;
        if (len == 0) {
            /*
             * This deferred activator waits for no services
             */
            return true;
        }
        for (int i = 0; i < len; i++) {
            if (!services.containsKey(classes[i])) {
                return false;
            }
        }
        return true;
    }
}
