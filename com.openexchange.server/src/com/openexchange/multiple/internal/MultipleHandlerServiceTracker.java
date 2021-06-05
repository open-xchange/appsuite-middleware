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

package com.openexchange.multiple.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link MultipleHandlerServiceTracker} - Service tracker for multiple handler factory services.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MultipleHandlerServiceTracker implements ServiceTrackerCustomizer<MultipleHandlerFactoryService,MultipleHandlerFactoryService> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MultipleHandlerServiceTracker.class);

    private final BundleContext context;

    /**
     * Initializes a new {@link MultipleHandlerServiceTracker}.
     *
     * @param context The bundle context
     */
    public MultipleHandlerServiceTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public MultipleHandlerFactoryService addingService(final ServiceReference<MultipleHandlerFactoryService> reference) {
        final MultipleHandlerFactoryService addedService = context.getService(reference);
        if (null == addedService) {
            LOG.warn("Added service is null!", new Throwable());
            context.ungetService(reference);
            return null;
        }
        final MultipleHandlerRegistry registry = ServerServiceRegistry.getInstance().getService(MultipleHandlerRegistry.class);
        if (null != registry && registry.addFactoryService(addedService)) {
            return addedService;
        }
        // Drop reference
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<MultipleHandlerFactoryService> reference, final MultipleHandlerFactoryService service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<MultipleHandlerFactoryService> reference, final MultipleHandlerFactoryService service) {
        if (null == service) {
            return;
        }
        try {
            final MultipleHandlerRegistry registry = ServerServiceRegistry.getInstance().getService(MultipleHandlerRegistry.class);
            if (null != registry) {
                registry.removeFactoryService(service.getSupportedModule());
            }
        } finally {
            context.ungetService(reference);
        }
    }

}
