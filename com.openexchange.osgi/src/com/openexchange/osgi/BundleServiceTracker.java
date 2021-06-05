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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.server.ServiceHolder;

/**
 * {@link BundleServiceTracker} - Tracks a bundle service and fills or empties corresponding {@link ServiceHolder} instance
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class BundleServiceTracker<S> implements ServiceTrackerCustomizer<S, S> {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(BundleServiceTracker.class);

    protected final BundleContext context;

    protected final ServiceHolder<S> serviceHolder;

    protected final Class<S> serviceClass;

    /**
     * Initializes a new bundle service tracker
     *
     * @param context The bundle context
     * @param serviceClass The service's class (used for dynamic type comparison and casts)
     */
    public BundleServiceTracker(final BundleContext context, final Class<S> serviceClass) {
        this(context, null, serviceClass);
    }

    /**
     * Initializes a new bundle service tracker
     *
     * @param context The bundle context
     * @param serviceHolder The service holder
     * @param serviceClass The service's class (used for dynamic type comparison and casts)
     */
    public BundleServiceTracker(final BundleContext context, final ServiceHolder<S> serviceHolder, final Class<S> serviceClass) {
        super();
        this.context = context;
        this.serviceClass = serviceClass;
        this.serviceHolder = serviceHolder;
    }

    @Override
    public final S addingService(final ServiceReference<S> reference) {
        final S addedService = context.getService(reference);
        if (null == addedService) {
            LOG.warn("added service is null! {}", serviceClass.getName(), new Throwable());
        }
        if (serviceClass.isInstance(addedService)) {
            try {
                final S service = serviceClass.cast(addedService);
                if (serviceHolder != null) {
                    serviceHolder.setService(service);
                }
                addingServiceInternal(service);
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
        return addedService;
    }

    /**
     * Invoked when service is added
     *
     * @param service The service
     */
    protected void addingServiceInternal(final S service) {
        LOG.trace("BundleServiceTracker.addingServiceInternal(): {}", service);
    }

    @Override
    public final void modifiedService(final ServiceReference<S> reference, final S service) {
        // Nope
    }

    @Override
    public final void removedService(final ServiceReference<S> reference, final S service) {
        try {
            if (serviceClass.isInstance(service)) {
                try {
                    if (serviceHolder != null) {
                        serviceHolder.removeService();
                    }
                    removedServiceInternal(serviceClass.cast(service));
                } catch (Exception e) {
                    LOG.error("", e);
                }
            }
        } finally {
            /*
             * Release service
             */
            context.ungetService(reference);
        }
    }

    /**
     * Invoked when service is added
     *
     * @param service The service
     */
    protected void removedServiceInternal(final S service) {
        LOG.trace("BundleServiceTracker.removedServiceInternal()");
    }

}
