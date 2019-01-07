/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.admin.autocontextid.osgi;

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
 * {@link AutoCIDDBMigrationServiceTracker}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class AutoCIDDBMigrationServiceTracker implements ServiceTrackerCustomizer<DBMigrationExecutorService, DBMigrationExecutorService> {

    private final BundleContext context;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link AutoCIDDBMigrationServiceTracker}.
     */
    public AutoCIDDBMigrationServiceTracker(ServiceLookup services, BundleContext context) {
        super();
        this.context = context;
        this.services = services;
    }

    @Override
    public DBMigrationExecutorService addingService(ServiceReference<DBMigrationExecutorService> reference) {
        DBMigrationExecutorService migrationService = context.getService(reference);

        Logger logger = org.slf4j.LoggerFactory.getLogger(AutoCIDDBMigrationServiceTracker.class);
        String changeSetLocaltion = "/liquibase/autocidConfigdbChangeLog.xml";
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
