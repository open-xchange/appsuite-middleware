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

package com.openexchange.user.copy.internal.connection.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.database.DatabaseService;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;

/**
 * {@link ConnectionFetcherRegisterer}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ConnectionFetcherRegisterer implements ServiceTrackerCustomizer<DatabaseService, DatabaseService> {

    private final BundleContext context;

    private ServiceRegistration<CopyUserTaskService> registration;


    public ConnectionFetcherRegisterer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public DatabaseService addingService(final ServiceReference<DatabaseService> reference) {
        final DatabaseService service = context.getService(reference);
        final ConnectionFetcherTask task = new ConnectionFetcherTask(service);
        registration = context.registerService(CopyUserTaskService.class, task, null);

        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<DatabaseService> reference, final DatabaseService service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<DatabaseService> reference, final DatabaseService service) {
        registration.unregister();
        context.ungetService(reference);
    }
}
