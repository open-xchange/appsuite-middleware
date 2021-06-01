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

package com.openexchange.filestore.impl.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.filestore.DatabaseAccessService;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.FilestoreDataMoveListener;
import com.openexchange.filestore.impl.DatabaseAccessServiceImpl;
import com.openexchange.filestore.impl.groupware.AddFilestoreColumnsToUserTable;
import com.openexchange.filestore.impl.groupware.AddFilestoreOwnerColumnToUserTable;
import com.openexchange.filestore.impl.groupware.AddInitialUserFilestoreUsage;
import com.openexchange.filestore.impl.groupware.AddUserColumnToFilestoreUsageTable;
import com.openexchange.filestore.impl.groupware.MakeQuotaMaxConsistentInUserTable;
import com.openexchange.filestore.impl.groupware.unified.UnifiedQuotaDeleteListener;
import com.openexchange.filestore.impl.groupware.unified.UnifiedQuotaFilestoreDataMoveListener;
import com.openexchange.filestore.unified.UnifiedQuotaService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.uploaddir.UploadDirService;
import com.openexchange.user.UserService;

/**
 * {@link DBQuotaFileStorageActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DBQuotaFileStorageActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link DBQuotaFileStorageActivator}.
     */
    public DBQuotaFileStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(DBQuotaFileStorageActivator.class);
        final BundleContext context = this.context;

        Services.setServiceLookup(this);

        // Service trackers
        RankingAwareNearRegistryServiceTracker<UnifiedQuotaService> unifiedQuotaServices = new RankingAwareNearRegistryServiceTracker<>(context, UnifiedQuotaService.class, 0);
        rememberTracker(unifiedQuotaServices);
        {
            QuotaFileStorageListenerTracker listenerTracker = new QuotaFileStorageListenerTracker(context);
            rememberTracker(listenerTracker);

            FileStorageListenerRegistry listenerRegistry = new FileStorageListenerRegistry(context);
            rememberTracker(listenerRegistry);

            ServiceTracker<FileStorageService,FileStorageService> tracker = new ServiceTracker<FileStorageService,FileStorageService>(context, FileStorageService.class, new DBQuotaFileStorageRegisterer(listenerRegistry, unifiedQuotaServices, listenerTracker, context));
            rememberTracker(tracker);

            trackService(ContextService.class);
            trackService(UserService.class);
            trackService(ConfigViewFactory.class);
            trackService(UploadDirService.class);

            DatabaseAccessServiceImpl databaseAccessService = new DatabaseAccessServiceImpl(context);
            rememberTracker(databaseAccessService);

            openTrackers();

            registerService(DatabaseAccessService.class, databaseAccessService);
        }

        // Update tasks
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new AddFilestoreColumnsToUserTable(), new AddFilestoreOwnerColumnToUserTable(), new AddUserColumnToFilestoreUsageTable(), new AddInitialUserFilestoreUsage(), new MakeQuotaMaxConsistentInUserTable()));
        registerService(DeleteListener.class, new UnifiedQuotaDeleteListener(unifiedQuotaServices), null);
        registerService(FilestoreDataMoveListener.class, new UnifiedQuotaFilestoreDataMoveListener(), null);

        logger.info("Bundle successfully started: {}", context.getBundle().getSymbolicName());
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
        Logger logger = org.slf4j.LoggerFactory.getLogger(DBQuotaFileStorageActivator.class);
        logger.info("Bundle successfully stopped: {}", context.getBundle().getSymbolicName());
    }

}
