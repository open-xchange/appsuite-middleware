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

package com.openexchange.geolocation;

import java.sql.Connection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * {@link AbstractDBMigrationServiceTracker}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public abstract class AbstractDBMigrationServiceTracker implements ServiceTrackerCustomizer<DBMigrationExecutorService, DBMigrationExecutorService> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDBMigrationServiceTracker.class);
    private static final String DEFAULT = "";

    private final BundleContext context;
    private final ServiceLookup services;
    private final String changeSetLocation;

    /**
     * Initializes a new {@link SwiftDBMigrationServiceTracker}.
     */
    public AbstractDBMigrationServiceTracker(ServiceLookup services, BundleContext context, String changeSetLocation) {
        super();
        this.context = context;
        this.services = services;
        this.changeSetLocation = changeSetLocation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public DBMigrationExecutorService addingService(ServiceReference<DBMigrationExecutorService> reference) {
        DBMigrationExecutorService migrationService = context.getService(reference);
        try {
            // Get the connection/service to use
            final DatabaseService databaseService = services.getService(DatabaseService.class);
            String globalDbSchemaName;
            {
                Connection con = databaseService.getReadOnlyForGlobal(DEFAULT); //get for 'default' group?
                try {
                    globalDbSchemaName = con.getCatalog();
                } finally {
                    databaseService.backReadOnlyForGlobal(DEFAULT, con);
                }
            }

            // Initialise resource accessor
            ResourceAccessor resourceAccessor = new BundleResourceAccessor(context.getBundle());

            // Initialise connection provider
            DBMigrationConnectionProvider connectionProvider = new DBMigrationConnectionProvider() {

                @Override
                public Connection get() throws OXException {
                    return databaseService.getWritableForGlobal(DEFAULT);
                }

                @Override
                public void backAfterReading(Connection connection) {
                    Databases.autocommit(connection);
                    databaseService.backWritableAfterReading(connection);
                }

                @Override
                public void back(Connection connection) {
                    Databases.autocommit(connection);
                    databaseService.backWritableForGlobal(DEFAULT, connection);
                }
            };

            // Schedule the migration
            migrationService.scheduleDBMigration(new DBMigration(connectionProvider, changeSetLocation, resourceAccessor, globalDbSchemaName));

            // Return tracked service
            return migrationService;
        } catch (Exception e) {
            logger.error("Failed to apply change-set: {}", changeSetLocation, e);
        }

        context.ungetService(reference);
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void modifiedService(ServiceReference<DBMigrationExecutorService> reference, DBMigrationExecutorService service) {
        // Ignore
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference<DBMigrationExecutorService> reference, DBMigrationExecutorService service) {
        context.ungetService(reference);
    }
}
