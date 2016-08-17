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
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.osgi.console.ServiceStateLookup;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.SimpleServiceLookup;


/**
 * A utility to allow complex start-up/shut-down routines being executed based on the
 * presence/absence of services. It is inspired by on the {@link DependentServiceRegisterer}.
 * An inheritor only needs to implement two methods, <code>start()</code> and <code>stop()</code>.
 *
 * Concrete implementations are meant to be used in bundle activators like this:
 * <pre>
 * public class MyActivator implements BundleActivator {
 *
 *     private MyStarter starter;
 *
 *     @Override
 *     public void start(BundleContext context) throws Exception {
 *       starter = new MyStarter(context);
 *       starter.open();
 *     }
 *
 *     @Override
 *     public void stop(BundleContext context) throws Exception {
 *         if (starter != null) {
 *             starter.close();
 *         }
 *     }
 *
 * }
 * </pre>
 *
 * Beside defining mandatory service dependencies, that need to be fulfilled before
 * a start-up routine can be executed, an additional set of optional services can be specified.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public abstract class DependentServiceStarter extends ServiceTracker<Object, Object> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DependentServiceStarter.class);

    private final Lock lock = new ReentrantLock();
    protected final BundleContext context;
    private final Class<?>[] neededServices;
    private final Class<?>[] optionalServices;
    private final Object[] foundServices;
    private SimpleServiceLookup serviceLookup = new SimpleServiceLookup();
    private boolean started = false;


    public DependentServiceStarter(BundleContext context, Class<?>... neededServices) throws InvalidSyntaxException {
        super(context, Tools.generateServiceFilter(context, neededServices), null);
        this.context = context;
        this.neededServices = neededServices;
        this.optionalServices = new Class<?>[0];
        this.foundServices = new Object[neededServices.length];
        setState();
    }

    public DependentServiceStarter(BundleContext context, Class<?>[] neededServices, Class<?>[] optionalServices) throws InvalidSyntaxException {
        super(context, Tools.generateServiceFilter(context, joinArrays(neededServices, optionalServices)), null);
        this.context = context;
        this.neededServices = neededServices;
        this.optionalServices = optionalServices;
        this.foundServices = new Object[neededServices.length];
        setState();
    }

    /**
     * Is called when all needed services are available. The <code>getService(Class<?> clazz)</code>
     * method of the passed {@link ServiceLookup} will never fail or return <code>null</code> for any
     * needed service. The service lookup is valid until <code>stop()</code> is called, it might be passed
     * to service instances, servlets etc. that are initialized within the <code>start()</code> method.
     * After <code>stop()</code> was called this service lookup instance is invalid and must not be used
     * anymore. All optional service dependencies are meant to be received via the <code>getOptionalService(Class<?> clazz)</code>
     * method. In case the service is currently not available, <code>null</code> is returned. An optional
     * service instance must not me stored within fields, long-living variables etc. The service lookup
     * always represents the presence/absence of optional services and must be called before every usage.
     *
     * @param services The service lookup.
     * @throws Exception If any exception is thrown, the starter stay in <i>not started</i> state. Exceptions
     *         will be logged. Severe errors, like {@link OutOfMemoryError} are re-thrown.
     */
    protected abstract void start(ServiceLookup services) throws Exception;

    /**
     * This method will be called when either a needed service disappears or the underlying service tracker
     * is closed. It is guaranteed that the method is not called if <code>start()</code> has not been
     * successfully called before.
     *
     * <code>start()</code> and <code>stop()</code> are mutually exclusive.
     */
    protected abstract void stop(ServiceLookup services) throws Exception;

    // -------------- ServiceTracker overrides -------------- \\

    @Override
    public Object addingService(ServiceReference<Object> reference) {
        final Object service = context.getService(reference);
        if (service == null) {
            return null;
        }

        boolean needsStart = true;
        lock.lock();
        try {
            for (int i = 0; i < neededServices.length; i++) {
                if (neededServices[i].isAssignableFrom(service.getClass())) {
                    foundServices[i] = service;
                }
                needsStart &= null != foundServices[i];
            }
            for (int i = 0; i < optionalServices.length; i++) {
                Class<?> clazz = optionalServices[i];
                if (clazz.isAssignableFrom(service.getClass())) {
                    serviceLookup.add(clazz, service);
                }
            }
            needsStart &= !started;
            if (needsStart) {
                for (int i = 0; i < neededServices.length; i++) {
                    if (service != null) {
                        serviceLookup.add(neededServices[i], foundServices[i]);
                    }
                }
                startSafe(serviceLookup);
            }
        } finally {
            lock.unlock();
        }
        setState();
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<Object> reference, Object service) {
        lock.lock();
        try {
            boolean someServiceMissing = false;
            for (int i = 0; i < neededServices.length; i++) {
                Class<?> clazz = neededServices[i];
                if (clazz.isAssignableFrom(service.getClass())) {
                    foundServices[i] = null;
                    // fill service lookup with correct instance of removed service
                    serviceLookup.add(clazz, service);
                }
                someServiceMissing |= null == foundServices[i];
            }
            for (int i = 0; i < optionalServices.length; i++) {
                Class<?> clazz = optionalServices[i];
                if (clazz.isAssignableFrom(service.getClass())) {
                    serviceLookup.remove(clazz);
                }
            }
            if (started && someServiceMissing) {
                stopSafe(serviceLookup);
                serviceLookup = new SimpleServiceLookup(); // invalidate old service references
            }
        } finally {
            lock.unlock();
        }

        setState();
        context.ungetService(reference);
    }

    @Override
    public void close() {
        lock.lock();
        try {
            if (started) {
                stopSafe(serviceLookup);
            }
        } finally {
            lock.unlock();
            super.close();
        }
    }

    // -------------- Utility methods -------------- \\

    private void startSafe(ServiceLookup services) {
        try {
            start(services);
            started = true;
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("start() failed for {}", getClass().getName(), t);
        }
    }

    private void stopSafe(ServiceLookup services) {
        try {
            stop(services);
            started = false;
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("stop() failed for {}", getClass().getName(), t);
        }
    }

    private void setState() {
        ServiceStateLookup lookup = DeferredActivator.getLookup();
        List<String> missing = new ArrayList<String>();
        List<String> present = new ArrayList<String>();
        for (int i = 0; i < neededServices.length; i++) {
            String serviceName = neededServices[i].getName();
            if (null == foundServices[i]) {
                missing.add(serviceName);
            } else {
                present.add(serviceName);
            }
        }
        lookup.setState(context.getBundle().getSymbolicName(), missing, present);
    }

    private static Class<?>[] joinArrays(Class<?>[] neededServices, Class<?>[] optionalServices) {
        Class<?>[] allServices = new Class<?>[neededServices.length + optionalServices.length];
        int i = 0;
        for (int j = 0; j < neededServices.length; j++) {
            allServices[i++] = neededServices[j];
        }
        for (int j = 0; j < optionalServices.length; j++) {
            allServices[i++] = optionalServices[j];
        }
        return allServices;
    }

}
