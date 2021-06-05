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

package com.openexchange.exception.interception.internal;

import static com.openexchange.java.Autoboxing.I;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.interception.OXExceptionInterceptor;

/**
 * Tracker for new registered {@link OXExceptionInterceptor}s
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class OXExceptionInterceptorTracker implements ServiceTrackerCustomizer<OXExceptionInterceptor, OXExceptionInterceptor> {

    private final BundleContext context;

    /**
     * Initializes a new {@link OXExceptionInterceptorTracker}.
     */
    public OXExceptionInterceptorTracker(BundleContext context) {
        super();
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OXExceptionInterceptor addingService(ServiceReference<OXExceptionInterceptor> reference) {
        OXExceptionInterceptor interceptor = context.getService(reference);

        if (OXExceptionInterceptorRegistration.getInstance().put(interceptor)) {
            return interceptor;
        }

        context.ungetService(reference);
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OXExceptionInterceptorTracker.class);
        logger.error("Interceptor for the given ranking {} and desired module/action combination already registered! Discard the new one from type: {}", I(interceptor.getRanking()), interceptor.getClass());
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifiedService(ServiceReference<OXExceptionInterceptor> reference, OXExceptionInterceptor service) {
        // Nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removedService(ServiceReference<OXExceptionInterceptor> reference, OXExceptionInterceptor service) {
        OXExceptionInterceptorRegistration.getInstance().remove(service);
    }
}
