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

package com.openexchange.osgi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.java.Autoboxing;

/**
 * {@link DeferredRegistryRegistration}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class DeferredRegistryRegistration<R, P> extends ServiceTracker<R, P> {


    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeferredRegistryRegistration.class);

    private R registry;

    private final Class<R> registryClass;

    private final P item;

    private final List<Class<?>> expectedServices;

    private final Map<Class<?>, PriorityQueue<ServiceEntry>> serviceMap = new ConcurrentHashMap<Class<?>, PriorityQueue<ServiceEntry>>();

    public DeferredRegistryRegistration(final BundleContext context, final Class<R> registryClass, final P item, Class<?>... additionalServices) {
        super(context, buildFilter(context, registryClass, additionalServices), null);
        this.registryClass = registryClass;
        this.item = item;
        this.expectedServices = new ArrayList<Class<?>>(1 + ((additionalServices == null) ? 0 : additionalServices.length));
        expectedServices.add(registryClass);
        if (additionalServices != null) {
            for (Class<?> serviceClass : additionalServices) {
                expectedServices.add(serviceClass);
            }
        }
    }

    private static Filter buildFilter(BundleContext context, Class<?> registryClass, Class<?>[] additionalServices) {
        try {
            if (additionalServices == null || additionalServices.length == 0) {
                return context.createFilter("("+Constants.OBJECTCLASS+"="+registryClass.getName()+")");

            }
            StringBuilder builder = new StringBuilder("(| (").append(Constants.OBJECTCLASS).append('=').append(registryClass.getName()).append(')');
            for (Class<?> klass : additionalServices) {
                builder.append('(').append(Constants.OBJECTCLASS).append('=').append(klass.getName()).append(')');
            }
            builder.append(')');
            return context.createFilter(builder.toString());
        } catch (InvalidSyntaxException e) {
            LOG.error("", e);
            return null;
        }
    }

    public abstract void register(R registry, P item);

    public abstract void unregister(R registry, P item);

    public void remove() {
        close();
    }

    @Override
    public P addingService(final ServiceReference<R> reference) {
        P service = remember(reference);
        if (isComplete()) {
            register(registry, item);
        }
        return service;
    }

    private P remember(ServiceReference<R> reference) {
        P service = super.addingService(reference);
        for (Class<?> klass : expectedServices) {
            if (klass.isInstance(service)) {
                PriorityQueue<ServiceEntry> priorityQueue = serviceMap.get(klass);
                if (priorityQueue == null) {
                    priorityQueue = new PriorityQueue<ServiceEntry>();
                    PriorityQueue<ServiceEntry> otherQueue = serviceMap.put(klass, priorityQueue);
                    if (otherQueue != null) {
                        priorityQueue = otherQueue;
                    }
                }
                priorityQueue.add(new ServiceEntry(reference, service));
            }
        }
        if (registryClass.isInstance(service)) {
            registry = (R) service;
        }
        return service;
    }
    
    @Override
    public void removedService(final ServiceReference<R> reference, final P service) {
        forget(service);
        if (!isComplete()) {
            unregister(registry, item);
        }
        if (service == registry) {
            registry = null;
        }
    }

    private void forget(Object service) {
        for (Class<?> klass : expectedServices) {
            if (klass.isInstance(service)) {
                PriorityQueue<ServiceEntry> priorityQueue = serviceMap.get(klass);
                if (priorityQueue != null) {
                    Iterator<ServiceEntry> iterator = priorityQueue.iterator();
                    while (iterator.hasNext()) {
                        ServiceEntry next = iterator.next();
                        if (next.service == service) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    private boolean isComplete() {
        for (Class<?> klass : expectedServices) {
            PriorityQueue<ServiceEntry> priorityQueue = serviceMap.get(klass);
            if (priorityQueue == null || priorityQueue.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public <I> I getService(Class<I> klass) {
        PriorityQueue<ServiceEntry> priorityQueue = serviceMap.get(klass);
        if (priorityQueue == null || priorityQueue.isEmpty()) {
            return null;
        }
        return (I) priorityQueue.peek().service;
    }

    private final class ServiceEntry implements Comparable<ServiceEntry> {

        public ServiceReference<R> ref;
        public P service;

        public ServiceEntry(ServiceReference<R> ref, P service) {
            this.ref = ref;
            this.service = service;
        }

        @Override
        public int compareTo(ServiceEntry o) {
            return getPriority() - o.getPriority();
        }

        private int getPriority() {
            Object property = ref.getProperty(Constants.SERVICE_RANKING);
            if (property == null) {
                return 0;
            }
            return Autoboxing.a2i(property);
        }
    }
}
