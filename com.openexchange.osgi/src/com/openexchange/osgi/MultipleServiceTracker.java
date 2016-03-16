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
