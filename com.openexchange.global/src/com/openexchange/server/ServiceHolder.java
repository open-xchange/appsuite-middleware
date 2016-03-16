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

package com.openexchange.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link ServiceHolder} - Provides convenient access to a bundle service formerly applied with {@link #setService(Object)}. The service may
 * be acquired multiple times.
 * <p>
 * The service is acquired through {@link #getService()} and must be released afterwards via {@link #ungetService(Object)}
 * <p>
 * A security mechanism keeps track of acquired services and forces an "unget" after a certain timeout
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class ServiceHolder<S> {

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServiceHolder.class);

    private static final Object PRESENT = new Object();

    private final class ServiceHolderTask extends TimerTask {

        ServiceHolderTask() {
            super();
        }

        @Override
        public void run() {
            try {
                if (usingThreads.isEmpty()) {
                    return;
                }
                for (final Iterator<Map.Entry<Thread, Map<ServiceProxy, Object>>> iter = usingThreads.entrySet().iterator(); iter.hasNext();) {
                    final Map.Entry<Thread, Map<ServiceProxy, Object>> e = iter.next();
                    final Map<ServiceProxy, Object> q = e.getValue();
                    for (final Iterator<ServiceProxy> proxyIter = q.keySet().iterator(); proxyIter.hasNext();) {
                        final ServiceProxy proxy = proxyIter.next();
                        if (proxy.isExceeded()) {
                            LOG.error("Forced unget: Found non-ungetted service after {}msec that was acquired at:\n{}", serviceUsageTimeout, printStackTrace(proxy.trace));
                            proxy.proxyService = null;
                            proxy.delegate = null;
                            proxy.propagateForcedUnget();
                            proxyIter.remove();
                        }
                    }
                    if (q.isEmpty()) {
                        iter.remove();
                    }
                }
            } catch (final Exception e) {
                LOG.error("", e);
            }
        }
    }

    private final class ServiceProxy implements java.lang.reflect.InvocationHandler {

        final long creationTime;
        S delegate;
        S proxyService;
        final StackTraceElement[] trace;

        public ServiceProxy(final S service, final StackTraceElement[] trace) {
            this.delegate = service;
            creationTime = System.currentTimeMillis();
            this.trace = trace;
        }

        @Override
        public Object invoke(final Object proxy, final Method m, final Object[] args) throws Throwable {
            if (delegate == null) {
                throw new NullPointerException("Service is not available anymore. Forgot to unget and reacquire?");
            }
            Object result;
            try {
                result = m.invoke(delegate, args);
            } catch (final InvocationTargetException e) {
                throw e.getTargetException();
            } catch (final Exception e) {
                throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
            }
            return result;
        }

        public S newProxyInstance() {
            if (proxyService == null) {
                final @SuppressWarnings("unchecked") S s =
                    (S) java.lang.reflect.Proxy.newProxyInstance(
                        delegate.getClass().getClassLoader(),
                        delegate.getClass().getInterfaces(),
                        this);
                proxyService = s;
            }
            return proxyService;
        }

        public void propagateForcedUnget() {
            if (countActive.get() > 0) {
                countActive.decrementAndGet();
            }
            if (waiting.get()) {
                synchronized (countActive) {
                    if (waiting.get()) {
                        countActive.notifyAll();
                    }
                }
            }
        }

        public boolean isExceeded() {
            return (System.currentTimeMillis() - creationTime) > serviceUsageTimeout;
        }

        public StackTraceElement[] getTrace() {
            return trace;
        }
    }

    /**
     * Enables the service usage inspection
     *
     * @param serviceUsageTimeout the service usage timeout
     */
    static void enableServiceUsageInspection(final int serviceUsageTimeout) {
        ServiceHolder.serviceUsageTimeout = serviceUsageTimeout;
        ServiceHolder.serviceHolderTimer = new Timer("ServiceHolderTimer");
        serviceUsageInspection = true;
    }

    /**
     * Disables the service usage inspection
     */
    static void disableServiceUsageInspection() {
        final Timer timer = ServiceHolder.serviceHolderTimer;
        if (null != timer) {
            timer.cancel();
            ServiceHolder.serviceHolderTimer = null;
        }
    }

    private static boolean serviceUsageInspection = false;

    protected static volatile int serviceUsageTimeout;

    private static volatile Timer serviceHolderTimer;

    /** Prints given stack trace */
    static final String printStackTrace(final StackTraceElement[] trace) {
        final StringBuilder sb = new StringBuilder(512);
        for (int i = 2; i < trace.length; i++) {
            sb.append("\tat ").append(trace[i]).append('\n');
        }
        return sb.toString();
    }

    protected final AtomicInteger countActive;
    protected final Map<String, ServiceHolderListener<S>> listeners;
    protected final Map<Thread, Map<ServiceProxy, Object>> usingThreads;
    protected final AtomicBoolean waiting;
    protected final AtomicReference<S> serviceReference;

    /**
     * Default constructor
     */
    protected ServiceHolder() {
        super();
        usingThreads = new ConcurrentHashMap<Thread, Map<ServiceProxy, Object>>();
        countActive = new AtomicInteger();
        waiting = new AtomicBoolean();
        listeners = new ConcurrentHashMap<String, ServiceHolderListener<S>>();
        serviceReference = new AtomicReference<S>();
        if (serviceUsageInspection) {
            /*
             * Service inspection is enabled
             */
            serviceHolderTimer.schedule(new ServiceHolderTask(), 1000, 5000);
        }
    }

    /**
     * Add a service holder listener
     *
     * @param listener The listener
     * @throws Exception If listener cannot be added
     */
    public final void addServiceHolderListener(final ServiceHolderListener<S> listener) throws Exception {
        if (listeners.containsKey(listener.getClass().getName())) {
            return;
        }
        listeners.put(listener.getClass().getName(), listener);
        if (null != serviceReference.get()) {
            listener.onServiceAvailable(serviceReference.get());
        }
    }

    /**
     * Gets the service or <code>null</code> if service is not active, yet<br>
     * <b>Note:</b> Don't forget to unget the service via {@link #ungetService()}
     *
     * <pre>
     * ...
     * final Service s = myServiceHolder.getService();
     * try {
     *     // Do something...
     * } finally {
     *     myServiceHolder.ungetService(s);
     * }
     * ...
     * </pre>
     *
     * @return The bundle service instance or <code>null</code> if none available
     */
    public final S getService() {
        if (null == serviceReference.get()) {
            return null;
        }
        countActive.incrementAndGet();
        if (serviceUsageInspection) {
            if (usingThreads.containsKey(Thread.currentThread())) {
                LOG.warn("Found thread using two (or more) services without ungetting service.", new Throwable());
            }
            final Thread thread = Thread.currentThread();
            Map<ServiceProxy, Object> proxySet = usingThreads.get(thread);
            if (null == proxySet) {
                final Map<ServiceProxy, Object> newProxySet = new ConcurrentHashMap<ServiceProxy, Object>();
                proxySet = usingThreads.put(thread, newProxySet);
                if (null == proxySet) {
                    proxySet = newProxySet;
                }
            }
            final ServiceProxy proxy = new ServiceProxy(serviceReference.get(), thread.getStackTrace());
            proxySet.put(proxy, PRESENT);
            return proxy.newProxyInstance();
        }
        return serviceReference.get();
    }

    private final void notifyListener(final boolean isAvailable) throws Exception {
        if (isAvailable) {
            for (final ServiceHolderListener<S> serviceHolderListener : listeners.values()) {
                serviceHolderListener.onServiceAvailable(serviceReference.get());
            }
        } else {
            for (final ServiceHolderListener<S> serviceHolderListener : listeners.values()) {
                serviceHolderListener.onServiceRelease();
            }
        }
    }

    /**
     * Removes the service from this service holder
     *
     * @throws Exception If service cannot be properly removed
     */
    public final void removeService() throws Exception {
        if (null == serviceReference.get()) {
            return;
        }
        final S service = serviceReference.get();
        if (serviceUsageInspection && countActive.get() > 0) {
            /*
             * Blocking OSGi framework is not allowed, but security mechanism built into this class ensures that an acquired service is
             * released in any case.
             */
            LOG.error("Service counting for {} is not zero: {}", this.getClass().getName(), countActive);
            if (waiting.compareAndSet(false, true)) {
                synchronized (countActive) {
                    try {
                        while (countActive.get() > 0) {
                            countActive.wait();
                        }
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOG.error("", e);
                    } finally {
                        waiting.set(false);
                    }
                }
            }
        }
        if (serviceReference.compareAndSet(service, null)) {
            /*
             * No other thread removed service in the meantime
             */
            notifyListener(false);
        }
    }

    /**
     * Removes the listener by given class
     *
     * @param clazz Listener class
     */
    public final void removeServiceHolderListenerByClass(final Class<? extends ServiceHolderListener<S>> clazz) {
        listeners.remove(clazz.getName());
    }

    /**
     * Removes the listener by given class name
     *
     * @param className Listener class name
     */
    public final void removeServiceHolderListenerByName(final String className) {
        listeners.remove(className);
    }

    /**
     * Removes the listener by given listener reference
     *
     * @param listener Listener reference
     */
    public final void removeServiceHolderListenerByRef(final ServiceHolderListener<S> listener) {
        for (final Iterator<ServiceHolderListener<S>> iter = listeners.values().iterator(); iter.hasNext();) {
            if (iter.next() == listener) {
                iter.remove();
            }
        }
    }

    /**
     * Clears service holder listeners
     */
    public final void clearServiceHolderListener() {
        listeners.clear();
    }

    /**
     * Sets the service of this service holder
     *
     * @param service The service
     * @throws Exception If service cannot be applied
     */
    public final void setService(final S service) throws Exception {
        if (null == service) {
            LOG.warn("#setService called with null argument! ", new Throwable());
        }
        if (serviceReference.compareAndSet(null, service)) {
            /*
             * No other thread set the service in the meantime
             */
            notifyListener(true);
        }
    }

    /**
     * Ungets the given bundle service instance
     *
     * @param service The bundle service instance
     */
    public final void ungetService(final S service) {
        if (service == null || countActive.get() == 0) {
            return;
        }
        if (serviceUsageInspection) {
            final Thread thread = Thread.currentThread();
            final Map<ServiceProxy, Object> proxySet = usingThreads.get(thread);
            if (null != proxySet) {
                for (final Iterator<ServiceProxy> iter = proxySet.keySet().iterator(); iter.hasNext();) {
                    final ServiceProxy proxy = iter.next();
                    if (proxy.proxyService == service) {
                        iter.remove();
                    }
                }
                if (proxySet.isEmpty()) {
                    usingThreads.remove(thread);
                }
            }
        }
        countActive.decrementAndGet();
        if (waiting.get()) {
            synchronized (countActive) {
                if (waiting.get()) {
                    countActive.notifyAll();
                }
            }
        }
    }

}
