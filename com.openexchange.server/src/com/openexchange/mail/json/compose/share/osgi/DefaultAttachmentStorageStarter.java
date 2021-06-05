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

package com.openexchange.mail.json.compose.share.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.cleanup.DatabaseCleanUpService;
import com.openexchange.mail.json.compose.share.DefaultAttachmentStorage;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DefaultAttachmentStorageStarter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class DefaultAttachmentStorageStarter implements ServiceTrackerCustomizer<DatabaseCleanUpService, DatabaseCleanUpService> {

    private final BundleContext context;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link DefaultAttachmentStorageStarter}.
     */
    public DefaultAttachmentStorageStarter(BundleContext context, ServiceLookup services) {
        super();
        this.context = context;
        this.services = services;
    }

    @Override
    public DatabaseCleanUpService addingService(ServiceReference<DatabaseCleanUpService> reference) {
        DatabaseCleanUpService service = context.getService(reference);
        try {
            DefaultAttachmentStorage.initiateCleaner(services.getService(ConfigurationService.class), service);
            return service;
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DefaultAttachmentStorageStarter.class);
            logger.warn("Failed to initiate cleaner task", e);
            context.ungetService(reference);
            return null;
        }
    }

    @Override
    public void modifiedService(ServiceReference<DatabaseCleanUpService> reference, DatabaseCleanUpService service) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<DatabaseCleanUpService> reference, DatabaseCleanUpService service) {
        DefaultAttachmentStorage.dropCleaner();
        context.ungetService(reference);
    }

}
