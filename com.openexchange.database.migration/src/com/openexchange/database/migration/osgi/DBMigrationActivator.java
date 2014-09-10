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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.database.migration.osgi;

import liquibase.servicelocator.CustomResolverServiceLocator;
import liquibase.servicelocator.ServiceLocator;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.database.migration.internal.BundlePackageScanClassResolver;
import com.openexchange.database.migration.internal.DBMigrationExecutorServiceImpl;
import com.openexchange.database.migration.internal.Services;
import com.openexchange.database.migration.osgi.tracker.ManagementServiceTracker;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.startup.SignalStartedService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * Activator for the main migration bundle
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class DBMigrationActivator extends HousekeepingActivator {

    private static final Class<?>[] NEEDED_SERVICES = { DatabaseService.class, SignalStartedService.class, ThreadPoolService.class };

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DBMigrationActivator.class);

    private ServiceTracker<ManagementService, ManagementService> managementServiceTracker;

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
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: " + this.context.getBundle().getSymbolicName());

        Services.setServiceLookup(this);
        // Important: Enable liquibase to load required classes (e.g. liquibase.logging.Logger implementation) from this bundle
        ServiceLocator.setInstance(new CustomResolverServiceLocator(new BundlePackageScanClassResolver(this.context.getBundle())));

        final DatabaseService databaseService = getService(DatabaseService.class);
        final ThreadPoolService threadPoolService = Services.getService(ThreadPoolService.class);
        threadPoolService.submit(new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {
                DBMigrationExecutorServiceImpl dbMigrationExecutorService = new DBMigrationExecutorServiceImpl(databaseService);
                dbMigrationExecutorService.runConfigDBCoreMigrations();
                context.registerService(DBMigrationExecutorService.class, dbMigrationExecutorService, null);

                managementServiceTracker = new ServiceTracker<ManagementService, ManagementService>(
                    context,
                    ManagementService.class,
                    new ManagementServiceTracker(context, dbMigrationExecutorService, databaseService));
                managementServiceTracker.open();
                return null;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle: " + this.context.getBundle().getSymbolicName());
        if (managementServiceTracker != null) {
            managementServiceTracker.close();
            managementServiceTracker = null;
        }

        Services.setServiceLookup(null);
        ServiceLocator.reset();
        super.stopBundle();
    }
}
