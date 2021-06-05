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

package com.openexchange.server.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.delete.DeleteRegistry;

/**
 * {@link DeleteListenerServiceTrackerCustomizer} - The {@link ServiceTrackerCustomizer} for delete registry.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DeleteListenerServiceTrackerCustomizer implements ServiceTrackerCustomizer<DeleteListener,DeleteListener> {

    private final BundleContext context;

    /**
     * Initializes a new {@link DeleteListenerServiceTrackerCustomizer}.
     *
     * @param context The bundle context
     */
    public DeleteListenerServiceTrackerCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public DeleteListener addingService(final ServiceReference<DeleteListener> reference) {
        final DeleteListener addedService = context.getService(reference);
        if (DeleteRegistry.getInstance().registerDeleteListener(addedService)) {
            return addedService;
        }
        // Nothing to track since adding to registry failed
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<DeleteListener> reference, final DeleteListener service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<DeleteListener> reference, final DeleteListener service) {
        if (null != service) {
            try {
                DeleteRegistry.getInstance().unregisterDeleteListener(service);
            } finally {
                context.ungetService(reference);
            }
        }
    }

}
