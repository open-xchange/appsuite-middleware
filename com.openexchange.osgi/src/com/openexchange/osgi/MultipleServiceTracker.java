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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * This abstract service tracker can wait for multiple services and performs a certain operation once all are available.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MultipleServiceTracker implements ServiceTrackerCustomizer<Object,Object> {

    private final Lock lock = new ReentrantLock();
    private final BundleContext context;
    private final Class<?>[] neededServices;
    private final Object[] foundServices;
    private boolean invoked;

    /**
     * Initializes a new {@link MultipleServiceTracker}.
     *
     * @param context The bundle context
     * @param neededServices The needed services
     */
    public MultipleServiceTracker(BundleContext context, Class<?>... neededServices) {
        super();
        invoked = false;
        this.context = context;
        this.neededServices = neededServices;
        this.foundServices = new Object[neededServices.length];
    }

    /**
     * Creates an appropriate {@link ServiceTracker} instance
     *
     * @return The newly created {@link ServiceTracker} instance
     * @throws InvalidSyntaxException If filter cannot be generated
     * @see #getFilter()
     */
    public ServiceTracker<Object, Object> createTracker() throws InvalidSyntaxException {
        return new ServiceTracker<Object, Object>(context, getFilter(), this);
    }

    /**
     * Gets the associated filter expression
     *
     * @return The filter
     * @throws InvalidSyntaxException If filter cannot be generated
     * @see #createTracker()
     */
    public Filter getFilter() throws InvalidSyntaxException {
        return Tools.generateServiceFilter(context, neededServices);
    }

    @Override
    public Object addingService(ServiceReference<Object> reference) {
        final Object obj = context.getService(reference);
        boolean allAvailable = true;
        lock.lock();
        try {
            for (int i = 0; i < neededServices.length; i++) {
                if (neededServices[i].isAssignableFrom(obj.getClass())) {
                    foundServices[i] = obj;
                }
                allAvailable &= null != foundServices[i];
            }
            allAvailable &= (false == invoked);
            if (allAvailable) {
                onAllAvailable();
                invoked = true;
            }
        } finally {
            lock.unlock();
        }
        return obj;
    }

    @Override
    public void modifiedService(ServiceReference<Object> arg0, Object arg1) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<Object> reference, Object service) {
        boolean someServiceMissing = false;
        lock.lock();
        try {
            for (int i = 0; i < neededServices.length; i++) {
                if (neededServices[i].isAssignableFrom(service.getClass())) {
                    foundServices[i] = null;
                }
                someServiceMissing |= null == foundServices[i];
            }
            if (invoked && someServiceMissing) {
                if (serviceRemoved(service)) {
                    invoked = false;
                }
            }
        } finally {
            lock.unlock();
        }
        context.ungetService(reference);
    }

    /**
     * Gets the tracked service associated with given class.
     *
     * @param clazz The service's class
     * @return The service instance or <code>null</code>
     */
    protected <S> S getTrackedService(Class<? extends S> clazz) {
        for (int i = 0; i < neededServices.length; i++) {
            if (neededServices[i].isAssignableFrom(clazz)) {
                return (S) foundServices[i];
            }
        }
        return null;
    }

    /**
     * Invoked once all services are available
     */
    protected abstract void onAllAvailable();

    /**
     * Invoked once a service was removed <b><i>after</i></b> {@link #onAllAvailable()} has been invoked.
     *
     * @param service The removed service
     * @return <code>true</code> if {@link #onAllAvailable()} is supposed to be re-invoked once all services are available again (default); otherwise <code>false</code>
     */
    protected abstract boolean serviceRemoved(Object service);

}
