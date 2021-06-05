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

package com.openexchange.folder.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.folder.FolderDeleteListenerService;

/**
 * {@link FolderDeleteListenerServiceTrackerCustomizer} - The service tracker customizer for {@link FolderDeleteListenerService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderDeleteListenerServiceTrackerCustomizer implements ServiceTrackerCustomizer<FolderDeleteListenerService,FolderDeleteListenerService> {

    private final BundleContext context;

    /**
     * Initializes a new {@link FolderDeleteListenerServiceTrackerCustomizer}.
     */
    public FolderDeleteListenerServiceTrackerCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public FolderDeleteListenerService addingService(final ServiceReference<FolderDeleteListenerService> reference) {
        /*
         * Optionally expect class name with org.osgi.framework.Constants.SERVICE_DESCRIPTION property
         */
        final Object property = reference.getProperty(org.osgi.framework.Constants.SERVICE_DESCRIPTION);
        final FolderDeleteListenerRegistry registry = FolderDeleteListenerRegistry.getInstance();
        if (property != null && registry.containsByClassName(property.toString())) {
            // Nothing to track
            return null;
        }
        final FolderDeleteListenerService deleteListenerService = context.getService(reference);
        if (registry.addDeleteListenerService(deleteListenerService)) {
            return deleteListenerService;
        }
        // Nothing to track
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<FolderDeleteListenerService> reference, final FolderDeleteListenerService service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<FolderDeleteListenerService> reference, final FolderDeleteListenerService service) {
        if (null != service) {
            try {
                FolderDeleteListenerRegistry.getInstance().removeDeleteListenerService(service);
            } finally {
                context.ungetService(reference);
            }
        }
    }

}
