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

package com.openexchange.drive.impl.osgi;

import java.time.Duration;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.clientinfo.ClientInfoProvider;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.cleanup.CleanUpInfo;
import com.openexchange.database.cleanup.CleanUpJob;
import com.openexchange.database.cleanup.DatabaseCleanUpService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.drive.BrandedDriveVersionService;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.checksum.rdb.DriveCreateTableService;
import com.openexchange.drive.checksum.rdb.DriveDeleteListener;
import com.openexchange.drive.checksum.rdb.SQL;
import com.openexchange.drive.impl.DriveClientInfoProvider;
import com.openexchange.drive.impl.checksum.PeriodicChecksumCleaner;
import com.openexchange.drive.impl.checksum.events.DelayedChecksumEventListener;
import com.openexchange.drive.impl.internal.DriveServiceImpl;
import com.openexchange.drive.impl.internal.DriveServiceLookup;
import com.openexchange.drive.impl.internal.throttle.BucketInputStream;
import com.openexchange.drive.impl.internal.throttle.DriveTokenBucket;
import com.openexchange.drive.impl.internal.throttle.ThrottlingDriveService;
import com.openexchange.drive.impl.management.DriveConfig;
import com.openexchange.drive.impl.management.version.BrandedDriveVersionServiceImpl;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.share.ShareService;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;
import com.openexchange.version.VersionService;

/**
 * {@link DriveActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveActivator extends HousekeepingActivator {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveActivator.class);

    /**
     * Initializes a new {@link DriveActivator}.
     */
    public DriveActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { IDBasedFileAccessFactory.class, ManagedFileManagement.class, DatabaseService.class, CapabilityService.class,
            IDBasedFolderAccessFactory.class, EventAdmin.class, ThreadPoolService.class, TimerService.class, ConfigurationService.class,
            UserService.class, GroupService.class, ModuleSupport.class, ShareService.class, ContextService.class, ShareNotificationService.class,
            ContactService.class, ContactUserStorage.class, FolderService.class, DispatcherPrefixService.class, LeanConfigurationService.class,
            VersionService.class
        };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: \"com.openexchange.drive\"");
        /*
         * set references
         */
        DriveConfig globalConfig = new DriveConfig(-1, -1);
        DriveServiceLookup.set(this);
        BucketInputStream.setTokenBucket(new DriveTokenBucket(globalConfig.getMaxBandwidth(), globalConfig.getMaxBandwidthPerClient()));
        /*
         * register Drive client info
         */
        registerService(ClientInfoProvider.class, new DriveClientInfoProvider(), 10);
        /*
         * register services
         */
        registerService(DriveService.class, new ThrottlingDriveService(new DriveServiceImpl()));
        registerService(CreateTableService.class, new DriveCreateTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(SQL.getUpdateTasks()));
        registerService(DeleteListener.class, new DriveDeleteListener());
        registerService(BrandedDriveVersionService.class, BrandedDriveVersionServiceImpl.getInstance());
        /*
         * register event handler
         */
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, DelayedChecksumEventListener.getHandledTopics());
        registerService(EventHandler.class, DelayedChecksumEventListener.getInstance(), serviceProperties);
        DelayedChecksumEventListener.getInstance().start();
        /*
         * schedule cluster-wide periodic checksum cleanup task
         */
        final BundleContext context = this.context;
        track(DatabaseCleanUpService.class, new ServiceTrackerCustomizer<DatabaseCleanUpService, DatabaseCleanUpService>() {

            private DatabaseCleanUpService cleanUpService;
            private CleanUpInfo jobInfo;

            @Override
            public synchronized DatabaseCleanUpService addingService(ServiceReference<DatabaseCleanUpService> reference) {
                LOG.debug("Initializing periodic checksum cleaner task");
                this.cleanUpService = context.getService(reference);
                long interval = globalConfig.getChecksumCleanerInterval();
                Duration intervalDuration = Duration.ofMillis(interval);
                if (0 < interval) {
                    long checksumCleanerMaxAge = globalConfig.getChecksumCleanerMaxAge();
                    PeriodicChecksumCleaner checksumCleaner = new PeriodicChecksumCleaner(checksumCleanerMaxAge, intervalDuration, intervalDuration);
                    CleanUpJob cleanUpJob = checksumCleaner.getCleanUpJob();
                    try {
                        this.jobInfo = cleanUpService.scheduleCleanUpJob(cleanUpJob);
                    } catch (OXException e) {
                        LOG.error("Clean up task {} cannot be scheduled:{}", cleanUpJob.getId(), e.getErrorCode());
                    }
                }
                return cleanUpService;
            }

            @Override
            public void modifiedService(ServiceReference<DatabaseCleanUpService> reference, DatabaseCleanUpService service) {
                // Ignored
            }

            @Override
            public synchronized void removedService(ServiceReference<com.openexchange.database.cleanup.DatabaseCleanUpService> reference, DatabaseCleanUpService service) {
                LOG.debug("Stopping periodic checksum cleaner task");
                if (cleanUpService != null && jobInfo != null) {
                    try {
                        jobInfo.cancel(true);
                    } catch (Exception e) {
                        LOG.error("Clean up task {} cannot be canceled", jobInfo.getJobId(), e);
                    }
                }
                jobInfo = null;
                cleanUpService = null;
            }
        });
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: \"com.openexchange.drive\"");
        BucketInputStream.setTokenBucket(null);
        DelayedChecksumEventListener.getInstance().stop();
        DriveServiceLookup.set(null);
        super.stopBundle();
    }

}
