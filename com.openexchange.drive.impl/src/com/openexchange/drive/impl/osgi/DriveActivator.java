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

package com.openexchange.drive.impl.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.cluster.timer.ClusterTimerService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.drive.BrandedDriveVersionService;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.checksum.rdb.DriveCreateTableService;
import com.openexchange.drive.checksum.rdb.DriveDeleteListener;
import com.openexchange.drive.checksum.rdb.SQL;
import com.openexchange.drive.impl.checksum.PeriodicChecksumCleaner;
import com.openexchange.drive.impl.checksum.events.DelayedChecksumEventListener;
import com.openexchange.drive.impl.internal.DriveServiceImpl;
import com.openexchange.drive.impl.internal.DriveServiceLookup;
import com.openexchange.drive.impl.internal.throttle.BucketInputStream;
import com.openexchange.drive.impl.internal.throttle.DriveTokenBucket;
import com.openexchange.drive.impl.internal.throttle.ThrottlingDriveService;
import com.openexchange.drive.impl.management.DriveConfig;
import com.openexchange.drive.impl.management.version.BrandedDriveVersionServiceImpl;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.filemanagement.ManagedFileManagement;
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

/**
 * {@link DriveActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveActivator.class);

    /**
     * Initializes a new {@link DriveActivator}.
     */
    public DriveActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { IDBasedFileAccessFactory.class, ManagedFileManagement.class, DatabaseService.class, CapabilityService.class,
            IDBasedFolderAccessFactory.class, EventAdmin.class, ConfigurationService.class, ThreadPoolService.class, TimerService.class,
            UserService.class, GroupService.class, ModuleSupport.class, ShareService.class, ContextService.class, ShareNotificationService.class,
            ContactService.class, ContactUserStorage.class, ConfigViewFactory.class
        };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: \"com.openexchange.drive\"");
        /*
         * set references
         */
        DriveServiceLookup.set(this);
        DriveConfig.getInstance().start();
        BucketInputStream.setTokenBucket(new DriveTokenBucket());
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
        track(ClusterTimerService.class, new ServiceTrackerCustomizer<ClusterTimerService, ClusterTimerService>() {

            private volatile PeriodicChecksumCleaner checksumCleaner;

            @Override
            public ClusterTimerService addingService(ServiceReference<ClusterTimerService> reference) {
                LOG.debug("Initializing periodic checksum cleaner task");
                ClusterTimerService timerService = context.getService(reference);
                long interval = DriveConfig.getInstance().getChecksumCleanerInterval();
                if (0 < interval) {
                    PeriodicChecksumCleaner checksumCleaner = new PeriodicChecksumCleaner(DriveConfig.getInstance().getChecksumCleanerMaxAge());
                    this.checksumCleaner = checksumCleaner;
                    timerService.scheduleWithFixedDelay(PeriodicChecksumCleaner.class.getName(), checksumCleaner, interval, interval);
                }
                return timerService;
            }

            @Override
            public void modifiedService(ServiceReference<ClusterTimerService> reference, ClusterTimerService service) {
                // Ignored
            }

            @Override
            public void removedService(ServiceReference<ClusterTimerService> reference, ClusterTimerService service) {
                LOG.debug("Stopping periodic checksum cleaner task");
                PeriodicChecksumCleaner checksumCleaner = this.checksumCleaner;
                if (null != checksumCleaner) {
                    checksumCleaner.stop();
                    this.checksumCleaner = null;
                }
            }
        });
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: \"com.openexchange.drive\"");
        BucketInputStream.setTokenBucket(null);
        DelayedChecksumEventListener.getInstance().stop();
        DriveConfig.getInstance().stop();
        DriveServiceLookup.set(null);
        super.stopBundle();
    }

}
