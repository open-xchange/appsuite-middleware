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

package com.openexchange.database.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.database.internal.Initialization;
import com.openexchange.management.ManagementService;

/**
 * Injects the {@link ManagementService} for monitoring.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ManagementServiceCustomizer implements ServiceTrackerCustomizer<ManagementService, ManagementService> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ManagementServiceCustomizer.class);

    private final BundleContext context;

    /**
     * Default constructor.
     */
    public ManagementServiceCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public ManagementService addingService(final ServiceReference<ManagementService> reference) {
        final ManagementService service = context.getService(reference);
        LOG.info("Injecting ManagementService into database bundle.");
        Initialization.getInstance().getManagement().setManagementService(service);
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<ManagementService> reference, final ManagementService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<ManagementService> reference, final ManagementService service) {
        LOG.info("Removing ManagementService from database bundle.");
        Initialization.getInstance().getManagement().removeManagementService();
        context.ungetService(reference);
    }
}
