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
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.daemons.AdminDaemonService;
import com.openexchange.admin.storage.utils.Filestore2UserUtil;
import com.openexchange.database.DatabaseService;


/**
 * {@link AdminDaemonInitializer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class AdminDaemonInitializer implements ServiceTrackerCustomizer<DatabaseService, DatabaseService> {

    private final AdminDaemon daemon;
    private final BundleContext context;
    private ServiceRegistration<AdminDaemonService> adminDaemonRegistration;

    /**
     * Initializes a new {@link AdminDaemonInitializer}.
     */
    public AdminDaemonInitializer(AdminDaemon daemon, BundleContext context) {
        super();
        this.daemon = daemon;
        this.context = context;
    }

    @Override
    public synchronized DatabaseService addingService(ServiceReference<DatabaseService> reference) {
        DatabaseService databaseService = context.getService(reference);
        try {
            Filestore2UserUtil.initFilestore2User(databaseService);

            // Initialize & register RMI interfaces
            daemon.initRMI(context);

            // Signal admin daemon being successfully started
            adminDaemonRegistration = context.registerService(AdminDaemonService.class, daemon, null);

            return databaseService;
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AdminDaemonInitializer.class);
            logger.error("Failed to initialize file store utils", e);
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public synchronized void modifiedService(ServiceReference<DatabaseService> reference, DatabaseService service) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<DatabaseService> reference, DatabaseService service) {
        ServiceRegistration<AdminDaemonService> adminDaemonRegistration = this.adminDaemonRegistration;
        if (null != adminDaemonRegistration) {
            this.adminDaemonRegistration = null;
            adminDaemonRegistration.unregister();
        }

        context.ungetService(reference);
    }

}
