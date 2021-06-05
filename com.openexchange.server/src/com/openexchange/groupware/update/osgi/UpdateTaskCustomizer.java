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

package com.openexchange.groupware.update.osgi;

import java.util.Collection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.groupware.update.internal.DynamicSet;

/**
 * {@link UpdateTaskCustomizer} - The {@link ServiceTrackerCustomizer service tracker customizer} for update tasks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateTaskCustomizer implements ServiceTrackerCustomizer<UpdateTaskProviderService, UpdateTaskProviderService> {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdateTaskCustomizer.class);
    }

    private final BundleContext context;

    /**
     * Initializes a new {@link UpdateTaskCustomizer}.
     *
     * @param context The bundle context
     */
    public UpdateTaskCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public UpdateTaskProviderService addingService(final ServiceReference<UpdateTaskProviderService> reference) {
        final UpdateTaskProviderService providerService = context.getService(reference);
        final DynamicSet registry = DynamicSet.getInstance();
        // Get provider's collection
        final Collection<? extends UpdateTaskV2> collection = providerService.getUpdateTasks();
        boolean error = false;
        for (final UpdateTaskV2 task : collection) {
            if (!registry.addUpdateTask(task)) {
                LoggerHolder.LOG.error("Update task \"{}\" could not be registered.", task.getClass().getName(), new Exception());
                error = true;
                break;
            }
        }
        if (!error) {
            // Everything worked fine
            return providerService;
        }
        // Rollback
        for (final UpdateTaskV2 task : collection) {
            registry.removeUpdateTask(task);
        }
        // Nothing to track, return null
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<UpdateTaskProviderService> reference, final UpdateTaskProviderService service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<UpdateTaskProviderService> reference, final UpdateTaskProviderService service) {
        if (null != service) {
            try {
                final DynamicSet registry = DynamicSet.getInstance();
                final UpdateTaskProviderService providerService = service;
                final Collection<? extends UpdateTaskV2> collection = providerService.getUpdateTasks();
                for (final UpdateTaskV2 task : collection) {
                    registry.removeUpdateTask(task);
                }
            } finally {
                context.ungetService(reference);
            }
        }
    }

}
