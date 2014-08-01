/**
 * Licensed under same license as rest of project (Apache 2.0).
 *
 * History:
 *  - github.com/samhendley - 1/31/12 : initial implementation, tested in karaf.
 */
package com.openexchange.database.liquibase.osgi;

import liquibase.servicelocator.CustomResolverServiceLocator;
import liquibase.servicelocator.DefaultPackageScanClassResolver;
import liquibase.servicelocator.ServiceLocator;
import org.everit.osgi.liquibase.bundle.internal.BundlePackageScanClassResolver;
import org.osgi.framework.BundleContext;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.liquibase.LiquibaseExecutor;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * osgi activator that patches the ServiceLocator so it will work to pickup the "builtin" implementations for all of the packages. It will
 * not work to find liquibase "plugins" that use the package scanning technique to pull classes out of other libraries.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class LiquibaseActivator extends HousekeepingActivator {

    private static final Class<?>[] NEEDED_SERVICES = { DatabaseService.class };

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
    public void stop(BundleContext bundleContext) throws Exception {
        ServiceLocator.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startBundle() throws Exception {
        try {
            DefaultPackageScanClassResolver resolver = new BundlePackageScanClassResolver(this.context.getBundle());

            ServiceLocator.setInstance(new CustomResolverServiceLocator(resolver));

            final DatabaseService dbService = getService(DatabaseService.class);

            LiquibaseExecutor.runMigration(dbService);
        } catch (final Exception e) {
            org.slf4j.LoggerFactory.getLogger(LiquibaseActivator.class).error("", e);
            throw e;
        }

    }
}
