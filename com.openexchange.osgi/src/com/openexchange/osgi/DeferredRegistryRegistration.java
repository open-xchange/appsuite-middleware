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
public abstract class DeferredRegistryRegistration<R, P> extends ServiceTracker {


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
    public Object addingService(final ServiceReference reference) {
        Object service = remember(reference);
        if (isComplete()) {
            register(registry, item);
        }
        return service;
    }

    private Object remember(ServiceReference reference) {
        Object service = super.addingService(reference);
        for (Class<?> klass : expectedServices) {
            if (klass.isInstance(service)) {
                PriorityQueue<ServiceEntry> priorityQueue = serviceMap.get(klass);
                if (priorityQueue == null) {
                    priorityQueue = new PriorityQueue<ServiceEntry>();
                    PriorityQueue<ServiceEntry> otherQueue = serviceMap.put(klass, priorityQueue);
                    if(otherQueue != null) {
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
    public void removedService(final ServiceReference reference, final Object service) {
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

        public ServiceReference ref;

        public Object service;

        public ServiceEntry(ServiceReference ref, Object service) {
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
