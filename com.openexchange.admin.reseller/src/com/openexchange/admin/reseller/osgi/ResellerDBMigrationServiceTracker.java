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

package com.openexchange.admin.reseller.osgi;

import java.sql.Connection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.migration.DBMigration;
import com.openexchange.database.migration.DBMigrationConnectionProvider;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.database.migration.resource.accessor.BundleResourceAccessor;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import liquibase.resource.ResourceAccessor;

/**
 *
 * {@link ResellerDBMigrationServiceTracker}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class ResellerDBMigrationServiceTracker implements ServiceTrackerCustomizer<DBMigrationExecutorService, DBMigrationExecutorService> {

    private final BundleContext context;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link ResellerDBMigrationServiceTracker}.
     */
    public ResellerDBMigrationServiceTracker(ServiceLookup services, BundleContext context) {
        super();
        this.context = context;
        this.services = services;
    }

    @Override
    public DBMigrationExecutorService addingService(ServiceReference<DBMigrationExecutorService> reference) {
        DBMigrationExecutorService migrationService = context.getService(reference);

        Logger logger = org.slf4j.LoggerFactory.getLogger(ResellerDBMigrationServiceTracker.class);
        String changeSetLocaltion = "/liquibase/resellerConfigdbChangeLog.xml";
        try {
            // Get the connection/service to use
            final DatabaseService databaseService = services.getService(DatabaseService.class);
            String configDbSchemaName;
            {
                Connection con = databaseService.getWritable();
                try {
                    configDbSchemaName = con.getCatalog();
                } finally {
                    databaseService.backWritableAfterReading(con);
                }
            }

            // Initialize resource accessor
            ResourceAccessor resourceAccessor = new BundleResourceAccessor(context.getBundle());

            // Initialize connection provider
            DBMigrationConnectionProvider connectionProvider = new DBMigrationConnectionProvider() {

                @Override
                public Connection get() throws OXException {
                    return databaseService.getWritable();
                }

                @Override
                public void backAfterReading(Connection connection) {
                    Databases.autocommit(connection);
                    databaseService.backWritableAfterReading(connection);
                }

                @Override
                public void back(Connection connection) {
                    Databases.autocommit(connection);
                    databaseService.backWritable(connection);
                }
            };

            // Schedule the migration
            migrationService.scheduleDBMigration(new DBMigration(connectionProvider, changeSetLocaltion, resourceAccessor, configDbSchemaName));

            // Return tracked service
            return migrationService;
        } catch (Exception e) {
            logger.error("Failed to apply Swift change-set: {}", changeSetLocaltion, e);
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<DBMigrationExecutorService> reference, DBMigrationExecutorService service) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<DBMigrationExecutorService> reference, DBMigrationExecutorService service) {
        context.ungetService(reference);
    }

}
