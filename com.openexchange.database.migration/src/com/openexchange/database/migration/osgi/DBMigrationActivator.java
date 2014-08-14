/**
 * Licensed under same license as rest of project (Apache 2.0).
 *
 * History:
 *  - github.com/samhendley - 1/31/12 : initial implementation, tested in karaf.
 */
package com.openexchange.database.migration.osgi;

import liquibase.servicelocator.CustomResolverServiceLocator;
import liquibase.servicelocator.DefaultPackageScanClassResolver;
import liquibase.servicelocator.ServiceLocator;
import org.apache.commons.lang.Validate;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.database.migration.internal.BundlePackageScanClassResolver;
import com.openexchange.database.migration.internal.DBMigrationExecutorServiceImpl;
import com.openexchange.database.migration.internal.Services;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * TODO
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class DBMigrationActivator extends HousekeepingActivator {

    private static final Class<?>[] NEEDED_SERVICES = { DatabaseService.class, ConfigurationService.class };

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DBMigrationActivator.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();

        LOG.info("Stopping bundle: " + this.context.getBundle().getSymbolicName());
        cleanUp();
        Services.setServiceLookup(null);
        ServiceLocator.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: " + this.context.getBundle().getSymbolicName());

        Services.setServiceLookup(this);

        final DatabaseService dbService = getService(DatabaseService.class);
        Validate.notNull(dbService, "Not able to execute database migration! DatabaseService is absent.");

        final ConfigurationService configurationService = getService(ConfigurationService.class);
        Validate.notNull(configurationService, "Cannot read migration files because ConfigurationService is absent.");

        DBMigrationExecutorServiceImpl dbMigrationExecutorServiceImpl = new DBMigrationExecutorServiceImpl(dbService, configurationService);
        registerService(DBMigrationExecutorService.class, dbMigrationExecutorServiceImpl);

        DefaultPackageScanClassResolver resolver = new BundlePackageScanClassResolver(this.context.getBundle());
        ServiceLocator.setInstance(new CustomResolverServiceLocator(resolver));
    }
}
