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

package com.openexchange.groupware.infostore.osgi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.filestore.FileLocationHandler;
import com.openexchange.groupware.impl.FolderLockManagerImpl;
import com.openexchange.groupware.infostore.InfostoreAvailable;
import com.openexchange.groupware.infostore.InfostoreFacades;
import com.openexchange.groupware.infostore.database.InfostoreFilestoreLocationUpdater;
import com.openexchange.groupware.infostore.database.impl.InfostoreFilenameReservationsCreateTableTask;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.LockCleaner;
import com.openexchange.groupware.infostore.webdav.PropertyCleaner;
import com.openexchange.groupware.infostore.webdav.PropertyStoreImpl;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.jslob.shared.SharedJSlobService;
import com.openexchange.server.services.SharedInfostoreJSlob;

/**
 * {@link InfostoreActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreActivator implements BundleActivator {

    /**
     * A flag that indicates whether InfoStore file storage bundle is available or not.
     *
     * @see InfostoreFacades#isInfoStoreAvailable()
     */
    public static final AtomicReference<InfostoreAvailable> INFOSTORE_FILE_STORAGE_AVAILABLE = new AtomicReference<InfostoreAvailable>();

    private volatile Queue<ServiceRegistration<?>> registrations;
    private volatile ServiceTracker<FileStorageServiceRegistry, FileStorageServiceRegistry> tracker;
    private volatile ServiceTracker<ConfigurationService, ConfigurationService> configTracker;
    private volatile ServiceTracker<QuotaFileStorageService, QuotaFileStorageService> qfsTracker;

    @Override
    public void start(final BundleContext context) throws Exception {
        try {

            LockCleaner lockCleaner = new LockCleaner(new FolderLockManagerImpl(new DBPoolProvider()), new EntityLockManagerImpl(new DBPoolProvider(), "infostore_lock"));
            PropertyCleaner propertyCleaner = new PropertyCleaner(new PropertyStoreImpl(new DBPoolProvider(), "oxfolder_property"), new PropertyStoreImpl(new DBPoolProvider(), "infostore_property"));
            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, FileStorageEventConstants.ALL_TOPICS);
            /*
             * Service registrations
             */
            Queue<ServiceRegistration<?>> registrations = new LinkedList<ServiceRegistration<?>>();
//            registrations.offer(context.registerService(CreateTableService.class.getName(), task, null));
//            registrations.offer(
            registrations.offer(context.registerService(EventHandler.class, lockCleaner, serviceProperties));
            registrations.offer(context.registerService(EventHandler.class, propertyCleaner, serviceProperties));

            /*
             * Register infostore filestore location updater for move context filestore
             */
            registrations.offer(context.registerService(FileLocationHandler.class, new InfostoreFilestoreLocationUpdater(), null));

            this.registrations = registrations;
            /*
             * Service trackers
             */
            class AvailableTracker extends ServiceTracker<FileStorageServiceRegistry, FileStorageServiceRegistry> {

                AvailableTracker(final BundleContext context) {
                    super(context, FileStorageServiceRegistry.class, null);
                }

                @Override
                public FileStorageServiceRegistry addingService(final ServiceReference<FileStorageServiceRegistry> reference) {
                    final FileStorageServiceRegistry registry = super.addingService(reference);
                    INFOSTORE_FILE_STORAGE_AVAILABLE.set(new InfostoreAvailable() {

                        @Override
                        public boolean available() {
                            return registry.containsFileStorageService("com.openexchange.infostore");
                        }
                    });
                    return registry;
                }

                @Override
                public void removedService(final ServiceReference<FileStorageServiceRegistry> reference, final FileStorageServiceRegistry service) {
                    INFOSTORE_FILE_STORAGE_AVAILABLE.set(null);
                    super.removedService(reference, service);
                }
            }
            AvailableTracker tracker = new AvailableTracker(context);
            tracker.open();
            this.tracker = tracker;

            final InfostoreFilenameReservationsCreateTableTask task = new InfostoreFilenameReservationsCreateTableTask();
            context.registerService(CreateTableService.class, task, null);
            context.registerService(UpdateTaskProviderService.class.getName(), new UpdateTaskProviderService() {
                @Override
                public Collection<UpdateTaskV2> getUpdateTasks() {
                    return Arrays.asList(((UpdateTaskV2) task));
                }
            }, null);

            ServiceTracker<ConfigurationService, ConfigurationService> configTracker = new ServiceTracker<ConfigurationService, ConfigurationService>(context, ConfigurationService.class, new ServiceTrackerCustomizer<ConfigurationService, ConfigurationService>() {

                /*
                 * Register quotas as SharedJSLob
                 */
                ServiceRegistration<SharedJSlobService> registration;

                @Override
                public ConfigurationService addingService(ServiceReference<ConfigurationService> arg0) {

                    ConfigurationService configService = context.getService(arg0);
                    SharedJSlobService infostoreJSlob = new SharedInfostoreJSlob();
                    registration = context.registerService(SharedJSlobService.class, infostoreJSlob, null);
                    return configService;
                }

                @Override
                public void modifiedService(ServiceReference<ConfigurationService> arg0, ConfigurationService arg1) {
                    //nothing to do
                }

                @Override
                public void removedService(ServiceReference<ConfigurationService> arg0, ConfigurationService arg1) {
                    registration.unregister();
                    context.ungetService(arg0);
                }
            });
            this.configTracker = configTracker;
            configTracker.open();

            ServiceTracker<QuotaFileStorageService, QuotaFileStorageService> qfsTracker = new ServiceTracker<QuotaFileStorageService, QuotaFileStorageService>(context, QuotaFileStorageService.class, null) {

                @Override
                public QuotaFileStorageService addingService(ServiceReference<QuotaFileStorageService> reference) {
                    QuotaFileStorageService service = super.addingService(reference);
                    InfostoreFacadeImpl.setQuotaFileStorageService(service);
                    return service;
                }

                @Override
                public void removedService(ServiceReference<QuotaFileStorageService> reference, QuotaFileStorageService service) {
                    InfostoreFacadeImpl.setQuotaFileStorageService(null);
                    super.removedService(reference, service);
                }
            };
            this.qfsTracker = qfsTracker;
            qfsTracker.open();

        } catch (final Exception e) {
            final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InfostoreActivator.class);
            logger.error("Starting InfostoreActivator failed.", e);
            throw e;
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        try {
            ServiceTracker<FileStorageServiceRegistry, FileStorageServiceRegistry> tracker = this.tracker;
            if (null != tracker) {
                tracker.close();
                this.tracker = null;
            }

            ServiceTracker<QuotaFileStorageService, QuotaFileStorageService> qfsTracker = this.qfsTracker;
            if (null != qfsTracker) {
                qfsTracker.close();
                this.qfsTracker = null;
            }

            ServiceTracker<ConfigurationService, ConfigurationService> configTracker = this.configTracker;
            if (null != configTracker) {
                configTracker.close();
                this.configTracker = null;
            }

            Queue<ServiceRegistration<?>> registrations = this.registrations;
            if (null != registrations) {
                ServiceRegistration<?> polled;
                while ((polled = registrations.poll()) != null) {
                    polled.unregister();
                }
                this.registrations = null;
            }
        } catch (final Exception e) {
            final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InfostoreActivator.class);
            logger.error("Stopping InfostoreActivator failed.", e);
            throw e;
        }
    }

}
