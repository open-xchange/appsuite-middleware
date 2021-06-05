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

package com.openexchange.admin.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.admin.storage.utils.CreateTableRegistry;
import com.openexchange.database.CreateTableService;

/**
 * Remember {@link CreateTableService} instances in the according registry.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class CreateTableCustomizer implements ServiceTrackerCustomizer<CreateTableService,CreateTableService> {

    private final BundleContext context;

    public CreateTableCustomizer(final BundleContext context) {
        super();
        this.context = context;

    }

    @Override
    public CreateTableService addingService(final ServiceReference<CreateTableService> reference) {
        final CreateTableService service = context.getService(reference);
        CreateTableRegistry.getInstance().addCreateTable(service);
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<CreateTableService> reference, final CreateTableService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<CreateTableService> reference, final CreateTableService service) {
        CreateTableRegistry.getInstance().removeCreateTable(service);
        context.ungetService(reference);
    }
}
