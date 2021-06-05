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

package com.openexchange.ajax.requesthandler;

import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * {@link ExtendableAJAXFactoryTracker} - A general-purpose tracker for {@link ExtendableAJAXActionServiceFactory} instances providing
 * customizable call-back through {@link #onFactoryAvailable(ExtendableAJAXActionServiceFactory)}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.0
 */
public class ExtendableAJAXFactoryTracker extends ServiceTracker<ExtendableAJAXActionServiceFactory, ExtendableAJAXActionServiceFactory> {

    private final AtomicReference<ExtendableAJAXActionServiceFactory> ref;
    private final String module;

    /**
     * Initializes a new {@link ExtendableAJAXFactoryTracker}.
     */
    public ExtendableAJAXFactoryTracker(String module, BundleContext context) {
        super(context, ExtendableAJAXActionServiceFactory.class, null);
        this.module = module;
        ref = new AtomicReference<ExtendableAJAXActionServiceFactory>();
    }

    @Override
    public ExtendableAJAXActionServiceFactory addingService(ServiceReference<ExtendableAJAXActionServiceFactory> reference) {
        ExtendableAJAXActionServiceFactory serviceFactory = super.addingService(reference);
        if (module.equals(serviceFactory.getModule()) && ref.compareAndSet(null, serviceFactory)) {
            onFactoryAvailable(serviceFactory);
            return serviceFactory;
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void removedService(ServiceReference<ExtendableAJAXActionServiceFactory> reference, ExtendableAJAXActionServiceFactory service) {
        ref.set(null);
        super.removedService(reference, service);
    }

    /**
     * Invoked when tracked factory is available; likely to add custom actions.
     * <p>
     * Example:
     *
     * <pre>
     *
     * protected void onFactoryAvailable(ExtendableAJAXActionServiceFactory serviceFactory) {
     *     serviceFactory.addAction(new MyCustomAction());
     * }
     * </pre>
     *
     * @param serviceFactory The tracked factory
     */
    protected void onFactoryAvailable(ExtendableAJAXActionServiceFactory serviceFactory) {
        // Empty method
    }

    /**
     * Gets the service factory
     *
     * @return The service factory or <code>null</code> if not yet available
     */
    public ExtendableAJAXActionServiceFactory getFactory() {
        return ref.get();
    }

}
