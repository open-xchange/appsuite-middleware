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
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * Registers update tasks if the {@link DatabaseService} becomes available. This allows writing update tasks not coupled to the
 * {@link ServerServiceRegistry}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class UpdateTaskRegisterer implements ServiceTrackerCustomizer<DatabaseService, DatabaseService> {

    private final BundleContext context;
    private ServiceRegistration<UpdateTaskProviderService> registration;

    public UpdateTaskRegisterer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public DatabaseService addingService(ServiceReference<DatabaseService> reference) {
        final DatabaseService service = context.getService(reference);
        registration = context.registerService(UpdateTaskProviderService.class, new UpdateTaskProviderService() {
            @Override
            public Collection<? extends UpdateTaskV2> getUpdateTasks() {
                return createTasks(service);
            }
        }, null);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<DatabaseService> reference, DatabaseService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<DatabaseService> reference, DatabaseService service) {
        registration.unregister();
        context.ungetService(reference);
    }

    /**
     * Overwrite this method and return your update tasks to register by instantiating them. Pass the {@link DatabaseService} to the
     * constructor if your update tasks.
     * @param service the database service discovered by this service tracker customizer.
     * @return update tasks to be registered.
     */
    protected abstract Collection<? extends UpdateTaskV2> createTasks(DatabaseService service);
}
