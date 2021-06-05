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

package com.openexchange.tools.pipesnfilters.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersService;
import com.openexchange.tools.pipesnfilters.internal.PipesAndFiltersFactory;

/**
 * {@link PipesAndFiltersRegisterer}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class PipesAndFiltersRegisterer implements ServiceTrackerCustomizer<ThreadPoolService,ThreadPoolService> {

    private final BundleContext context;
    private volatile ServiceRegistration<PipesAndFiltersService> registration;

    public PipesAndFiltersRegisterer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public ThreadPoolService addingService(final ServiceReference<ThreadPoolService> reference) {
        final ThreadPoolService service = context.getService(reference);
        if (null == registration) {
            registration = context.registerService(PipesAndFiltersService.class, new PipesAndFiltersFactory(service), null);
            return service;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<ThreadPoolService> reference, final ThreadPoolService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<ThreadPoolService> reference, final ThreadPoolService service) {
        final ServiceRegistration<PipesAndFiltersService> unregister = this.registration;
        if (null != unregister) {
            unregister.unregister();
            this.registration = null;
        }
        context.ungetService(reference);
    }
}
