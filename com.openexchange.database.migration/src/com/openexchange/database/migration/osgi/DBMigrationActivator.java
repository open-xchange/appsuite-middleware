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

package com.openexchange.database.migration.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.database.migration.DBMigrationMonitorService;
import com.openexchange.database.migration.internal.BundlePackageScanClassResolver;
import com.openexchange.database.migration.internal.DBMigrationExecutorServiceImpl;
import com.openexchange.database.migration.internal.DBMigrationMonitor;
import com.openexchange.database.migration.rmi.DBMigrationRMIServiceImpl;
import com.openexchange.osgi.HousekeepingActivator;
import liquibase.servicelocator.CustomResolverServiceLocator;
import liquibase.servicelocator.ServiceLocator;

/**
 * Activator for the main migration bundle
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class DBMigrationActivator extends HousekeepingActivator {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(DBMigrationActivator.class).info("Starting bundle: {}", context.getBundle().getSymbolicName());

        // Important: Enable liquibase to load required classes (e.g. liquibase.logging.Logger implementation) from this bundle
        ServiceLocator.setInstance(new CustomResolverServiceLocator(new BundlePackageScanClassResolver(this.context.getBundle())));
        /*
         * instantiate & register services
         */
        DBMigrationExecutorServiceImpl executorService = new DBMigrationExecutorServiceImpl();
        DBMigrationRMIServiceImpl rmiService = new DBMigrationRMIServiceImpl(executorService);
        executorService.setRegisterer(rmiService);
        registerService(DBMigrationMonitorService.class, DBMigrationMonitor.getInstance());
        registerService(DBMigrationExecutorService.class, executorService);
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put("RMI_NAME", DBMigrationRMIServiceImpl.RMI_NAME);
        registerService(Remote.class, rmiService, serviceProperties);
        openTrackers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(DBMigrationActivator.class).info("Stopping bundle: {}", this.context.getBundle().getSymbolicName());
        ServiceLocator.reset();
        super.stopBundle();
    }
}
