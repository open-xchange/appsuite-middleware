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
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.AssignmentFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.GeneralDatabaseConnectionListener;
import com.openexchange.database.JdbcProperties;
import com.openexchange.database.internal.AssignmentFactoryImpl;
import com.openexchange.database.internal.Configuration;
import com.openexchange.database.internal.DatabaseServiceImpl;
import com.openexchange.database.internal.Initialization;
import com.openexchange.database.internal.JdbcPropertiesImpl;
import com.openexchange.database.internal.ConnectionReloaderImpl;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.osgi.ServiceListing;

/**
 * Injects the {@link ConfigurationService} and publishes the DatabaseService.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class DatabaseServiceRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DatabaseServiceRegisterer.class);

    private final BundleContext context;
    private final ServiceListing<GeneralDatabaseConnectionListener> connectionListeners;

    private ConfigurationService configService;
    private ConfigViewFactory configViewFactory;
    private DBMigrationExecutorService migrationService;

    private ServiceRegistration<DatabaseService> databaseServiceRegistration;

    /**
     * Initializes a new {@link DatabaseServiceRegisterer}.
     *
     * @param context The bundle context
     */
    public DatabaseServiceRegisterer(ServiceListing<GeneralDatabaseConnectionListener> connectionListeners, BundleContext context) {
        super();
        this.connectionListeners = connectionListeners;
        this.context = context;
    }

    @Override
    public synchronized Object addingService(final ServiceReference<Object> reference) {
        final Object obj = context.getService(reference);
        if (obj instanceof ConfigurationService) {
            configService = (ConfigurationService) obj;
        }
        if (obj instanceof ConfigViewFactory) {
            configViewFactory = (ConfigViewFactory) obj;
        }
        if (obj instanceof DBMigrationExecutorService) {
            migrationService = (DBMigrationExecutorService) obj;
        }
        boolean needsRegistration = null != configService && null != configViewFactory && null != migrationService;

        if (needsRegistration && !Initialization.getInstance().isStarted()) {
            DatabaseServiceImpl databaseService = null;
            try {
                Initialization.setConfigurationService(configService);
                // Parse configuration
                Configuration configuration = new Configuration();
                configuration.readConfiguration(configService);
                JdbcPropertiesImpl.getInstance().setJdbcProperties(configuration.getJdbcProps());
                ConnectionReloaderImpl reloader = new ConnectionReloaderImpl(configuration);
                context.registerService(Reloadable.class, reloader, null);
                context.registerService(JdbcProperties.class, JdbcPropertiesImpl.getInstance(), null);
                databaseService = Initialization.getInstance().start(configService, configViewFactory, migrationService, connectionListeners, configuration, reloader);
                LOG.info("Publishing DatabaseService.");
                databaseServiceRegistration = context.registerService(DatabaseService.class, databaseService, null);
            } catch (Exception e) {
                LOG.error("Publishing the DatabaseService failed.", e);
            }
            try {
                if (databaseService != null) {
                    AssignmentFactoryImpl assignmentFactoryImpl = new AssignmentFactoryImpl(databaseService);
                    assignmentFactoryImpl.reload();
                    LOG.info("Publishing AssignmentFactory.");
                    context.registerService(AssignmentFactory.class, assignmentFactoryImpl, null);
                } else {
                    LOG.error("Publishing AssignmentFactory failed due to missing DatabaseService.");
                }
            } catch (Exception e) {
                LOG.error("Publishing AssignmentFactory failed. This is normal until a server has been registered.", e);
            }
        }
        return obj;
    }

    @Override
    public void modifiedService(final ServiceReference<Object> reference, final Object service) {
        // Nothing to do.
    }

    @Override
    public synchronized void removedService(final ServiceReference<Object> reference, final Object service) {
        ServiceRegistration<DatabaseService> databaseServiceRegistration = this.databaseServiceRegistration;
        if (null != databaseServiceRegistration) {
            LOG.info("Unpublishing DatabaseService.");
            this.databaseServiceRegistration = null;
            databaseServiceRegistration.unregister();
            Initialization.getInstance().stop();
            Initialization.setConfigurationService(null);
        }
        context.ungetService(reference);
    }
}
