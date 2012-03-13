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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.service.indexing.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CompositeServiceLookup} - The composite service lookup.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CompositeServiceLookup implements ServiceLookup {

    private static interface ExtendedServiceLookup<S> extends ServiceLookup {

        /**
         * Gets the underlying {@link ServiceTracker}.
         * 
         * @return The tracker or <code>null</code>
         */
        ServiceTracker<S, S> optTracker();
    }

    private static final class Wrapper implements ExtendedServiceLookup<Object> {

        private final ServiceLookup serviceLookup;

        public Wrapper(final ServiceLookup serviceLookup) {
            super();
            this.serviceLookup = serviceLookup;
        }

        @Override
        public <S> S getService(final Class<? extends S> clazz) {
            return serviceLookup.getService(clazz);
        }

        @Override
        public <S> S getOptionalService(final Class<? extends S> clazz) {
            return serviceLookup.getOptionalService(clazz);
        }

        @Override
        public ServiceTracker<Object, Object> optTracker() {
            return null;
        }

    }

    private static final class ServiceTrackerServiceLookup<S> implements ExtendedServiceLookup<S> {

        private final ServiceTracker<S, S> tracker;

        protected ServiceTrackerServiceLookup(final ServiceTracker<S, S> tracker) {
            super();
            this.tracker = tracker;
        }

        @Override
        public <Service> Service getService(final Class<? extends Service> clazz) {
            @SuppressWarnings("unchecked") final Service service = (Service) tracker.getService();
            if (null == service) {
                throw new IllegalStateException("Missing service: " + clazz.getSimpleName());
            }
            return service;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <Service> Service getOptionalService(final Class<? extends Service> clazz) {
            return (Service) tracker.getService();
        }

        @Override
        public ServiceTracker<S, S> optTracker() {
            return tracker;
        }

    }

    private final ConcurrentMap<Class<?>, ExtendedServiceLookup<?>> map;

    private final BundleContext context;

    /**
     * Initializes a new {@link CompositeServiceLookup}.
     */
    public CompositeServiceLookup(final BundleContext context) {
        super();
        map = new ConcurrentHashMap<Class<?>, ExtendedServiceLookup<?>>(8);
        this.context = context;
    }

    /**
     * Closes this composite service lookup
     */
    public void close() {
        for (final ExtendedServiceLookup<?> serviceLookup : map.values()) {
            final ServiceTracker<?, ?> tracker = serviceLookup.optTracker();
            if (null != tracker) {
                tracker.close();
            }
        }
        map.clear();
    }

    /**
     * Checks if all services are present.
     * 
     * @param classes The services' classes
     * @return <code>true</code> if all served; otherwise <code>false</code>
     */
    public boolean servesAll(final Class<?>[] classes) {
        if (null == classes || 0 == classes.length) {
            return true;
        }
        boolean served = true;
        for (int i = 0; served && i < classes.length; i++) {
            served = null != map.get(classes[i]);
        }
        return served;
    }

    /**
     * Await service.
     * 
     * @param clazz The service's class
     */
    public <Service> void await(final Class<Service> clazz) {
        final ServiceLookup prev = map.get(clazz);
        if (null != prev) {
            return;
        }
        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        /*
         * Create & open new service tracker for missing service
         */
        final BundleContext context = this.context;
        final ServiceTracker<Service, Service> tracker =
            new ServiceTracker<Service, Service>(context, clazz, new ServiceTrackerCustomizer<Service, Service>() {

                @Override
                public Service addingService(final ServiceReference<Service> reference) {
                    final Service service = context.getService(reference);
                    lock.lock();
                    try {
                        condition.signalAll();
                    } finally {
                        lock.unlock();
                    }
                    return service;
                }

                @Override
                public void modifiedService(final ServiceReference<Service> reference, final Service service) {
                    // Nothing to do
                }

                @Override
                public void removedService(final ServiceReference<Service> reference, final Service service) {
                    context.ungetService(reference);
                }

            });
        tracker.open();
        /*
         * Add appropriate service lookup
         */
        final ServiceTrackerServiceLookup<Service> serviceLookup = new ServiceTrackerServiceLookup<Service>(tracker);
        map.putIfAbsent(clazz, serviceLookup);
        /*
         * Await service's availability
         */
        lock.lock();
        try {
            condition.await();
        } catch (final InterruptedException e) {
            // Keep interrupted flag
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds all to this composite service lookup
     * 
     * @param classes The service classes
     * @param serviceLookup The associated service lookup
     */
    public void addAll(final Class<?>[] classes, final ServiceLookup serviceLookup) {
        final ExtendedServiceLookup<Object> wrapper = new Wrapper(serviceLookup);
        for (final Class<?> clazz : classes) {
            map.putIfAbsent(clazz, wrapper);
        }
    }

    /**
     * Associates specified service lookup with given service's class.
     * 
     * @param clazz The service's class
     * @param serviceLookup The {@link ServiceLookup} instance
     * @return <code>true</code> if inserting into this composite service lookup was successful; otherwise <code>false</code>
     */
    public boolean addIfAbsent(final Class<?> clazz, final ServiceLookup serviceLookup) {
        final ServiceLookup prev = map.putIfAbsent(clazz, new Wrapper(serviceLookup));
        return ((null == prev) || (prev == serviceLookup));
    }

    @Override
    public <S> S getService(final Class<? extends S> clazz) {
        final ServiceLookup serviceLookup = map.get(clazz);
        if (null == serviceLookup) {
            throw new IllegalStateException("Missing service: " + clazz.getSimpleName());
        }
        return serviceLookup.getService(clazz);
    }

    @Override
    public <S> S getOptionalService(final Class<? extends S> clazz) {
        final ServiceLookup serviceLookup = map.get(clazz);
        if (null == serviceLookup) {
            return null;
        }
        return serviceLookup.getOptionalService(clazz);
    }

}
