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
import com.openexchange.groupware.delete.contextgroup.DeleteContextGroupListener;
import com.openexchange.groupware.delete.contextgroup.DeleteContextGroupRegistry;

/**
 * {@link DeleteContextGroupListenerServiceTracker}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DeleteContextGroupListenerServiceTracker implements ServiceTrackerCustomizer<DeleteContextGroupListener, DeleteContextGroupListener> {

    private BundleContext context;

    /**
     * Initialises a new {@link DeleteContextGroupListenerServiceTracker}.
     * 
     * @param context The bundle context instance
     */
    public DeleteContextGroupListenerServiceTracker(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public DeleteContextGroupListener addingService(ServiceReference<DeleteContextGroupListener> reference) {
        DeleteContextGroupListener listener = context.getService(reference);
        if (DeleteContextGroupRegistry.getInstance().registerDeleteContextGroupListener(listener)) {
            return listener;
        } else {
            context.ungetService(reference);
            return null;
        }
    }

    @Override
    public void modifiedService(ServiceReference<DeleteContextGroupListener> reference, DeleteContextGroupListener service) {
        // no-op
    }

    @Override
    public void removedService(ServiceReference<DeleteContextGroupListener> reference, DeleteContextGroupListener service) {
        if (service != null) {
            try {
                DeleteContextGroupRegistry.getInstance().unregisterDeleteContextGroupListener(service);
            } finally {
                context.ungetService(reference);
            }
        }
    }

}
