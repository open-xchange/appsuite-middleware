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

package com.openexchange.subscribe.osgi;

import java.util.Arrays;
import java.util.Collection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.subscribe.database.AddFolderIndex;
import com.openexchange.subscribe.database.EnabledColumn;
import com.openexchange.subscribe.database.FixSubscriptionTablePrimaryKey;
import com.openexchange.subscribe.database.SubscriptionsCreatedAndLastModifiedColumn;
import com.openexchange.subscribe.database.SubscriptionsTablesUtf8Mb4UpdateTask;

/**
 * {@link UpdateTaskRegisterer}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class UpdateTaskRegisterer implements ServiceTrackerCustomizer<DatabaseService, DatabaseService> {

    private final BundleContext context;

    private ServiceRegistration<UpdateTaskProviderService> registration;

    public UpdateTaskRegisterer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public DatabaseService addingService(final ServiceReference<DatabaseService> reference) {
        final DatabaseService service = context.getService(reference);
        registration = context.registerService(UpdateTaskProviderService.class, new UpdateTaskProviderService() {

            @Override
            public Collection<UpdateTaskV2> getUpdateTasks() {
                return Arrays.asList(
                    (UpdateTaskV2) new EnabledColumn(),
                    new SubscriptionsCreatedAndLastModifiedColumn(),
                    new FixSubscriptionTablePrimaryKey(),
                    new AddFolderIndex(), 
                    new SubscriptionsTablesUtf8Mb4UpdateTask());
            }
        }, null);
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<DatabaseService> reference, final DatabaseService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<DatabaseService> reference, final DatabaseService service) {
        registration.unregister();
        context.ungetService(reference);
    }
}
