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

package com.openexchange.oauth.json.osgi;

import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link AbstractOSGiDelegateService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractOSGiDelegateService<S> {

    private final Class<S> clazz;

    private final AtomicReference<S> service;

    private volatile ServiceTracker<?, ?> tracker;

    /**
     * Initializes a new {@link AbstractOSGiDelegateService}.
     */
    protected AbstractOSGiDelegateService(final Class<S> clazz) {
        super();
        this.clazz = clazz;
        service = new AtomicReference<>();
    }

    /**
     * Starts tracking the delegate service.
     *
     * @param bundleContext The bundle context
     * @return This instance for method chaining
     */
    @SuppressWarnings("unchecked")
    public <I extends AbstractOSGiDelegateService<S>> I start(BundleContext bundleContext) {
        if (null != tracker) {
            return (I) this;
        }
        synchronized (this) {
            ServiceTracker<?, ?> tmp = tracker;
            if (null == tracker) {
                tracker = tmp = new ServiceTracker<>(bundleContext, clazz.getName(), new Customizer<>(service, bundleContext));
                tmp.open();
            }
        }
        return (I) this;
    }

    /**
     * Stops tracking the delegate service.
     */
    public void stop() {
        ServiceTracker<?, ?> tmp = tracker;
        if (null != tmp) {
            tmp.close();
        }
    }

    /**
     * Gets the service from service reference.
     *
     * @return The service
     * @throws OXException If service reference returned <code>null</code>
     */
    protected S getService() throws OXException {
        S serviceInst = service.get();
        if (null == serviceInst) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(clazz.getName());
        }
        return serviceInst;
    }

    /**
     * Gets the service from service reference.
     *
     * @return The service or <code>null</code> if absent
     */
    protected S optService() {
        return service.get();
    }

    private static final class Customizer<S> implements ServiceTrackerCustomizer<S, S> {

        private final AtomicReference<S> reference;
        private final BundleContext context;

        /**
         * Initializes a new {@link Customizer}.
         *
         * @param reference The service reference
         * @param context The bundle context
         */
        public Customizer(AtomicReference<S> reference, BundleContext context) {
            super();
            this.reference = reference;
            this.context = context;
        }

        @Override
        public S addingService(ServiceReference<S> reference) {
            S service = context.getService(reference);
            this.reference.set(service);
            return service;
        }

        @Override
        public void modifiedService(ServiceReference<S> reference, S service) {
            // Nope
        }

        @Override
        public void removedService(ServiceReference<S> reference, S service) {
            this.reference.set(null);
            context.ungetService(reference);
        }

    }

}
