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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.filestore.impl.DBQuotaFileStorageService;
import com.openexchange.filestore.impl.groupware.QuotaModePreferenceItem;
import com.openexchange.filestore.unified.UnifiedQuotaService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;

/**
 * {@link DBQuotaFileStorageRegisterer} - Registers the {@link QuotaFileStorageService} service if all required services are available.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DBQuotaFileStorageRegisterer implements ServiceTrackerCustomizer<FileStorageService, FileStorageService> {

    private final BundleContext context;
    private final Lock lock = new ReentrantLock();

    private final FileStorageListenerRegistry storageListenerRegistry;
    private final QuotaFileStorageListenerTracker quotaListenerTracker;
    private final RankingAwareNearRegistryServiceTracker<UnifiedQuotaService> unifiedQuotaServices;

    private List<ServiceRegistration<?>> registrations;
    boolean isRegistered = false;

    /**
     * Initializes a new {@link DBQuotaFileStorageRegisterer}.
     *
     * @param context The bundle context
     */
    public DBQuotaFileStorageRegisterer(FileStorageListenerRegistry storageListenerRegistry, RankingAwareNearRegistryServiceTracker<UnifiedQuotaService> unifiedQuotaServices, QuotaFileStorageListenerTracker quotaListenerTracker, BundleContext context) {
        super();
        this.storageListenerRegistry = storageListenerRegistry;
        this.unifiedQuotaServices = unifiedQuotaServices;
        this.quotaListenerTracker = quotaListenerTracker;
        this.context = context;
    }

    @Override
    public FileStorageService addingService(ServiceReference<FileStorageService> reference) {
        FileStorageService service = context.getService(reference);

        lock.lock();
        try {
            boolean needsRegistration = false;
            if (false == isRegistered) {
                needsRegistration = true;
                isRegistered = true;
            }
            if (needsRegistration) {
                List<ServiceRegistration<?>> registrations = new ArrayList<>(4);
                this.registrations = registrations;

                QuotaFileStorageService qfss = new DBQuotaFileStorageService(storageListenerRegistry, unifiedQuotaServices, quotaListenerTracker, service);
                registrations.add(context.registerService(QuotaFileStorageService.class, qfss, null));

                QuotaModePreferenceItem item = new QuotaModePreferenceItem(qfss);
                registrations.add(context.registerService(PreferencesItemService.class, item, null));
                registrations.add(context.registerService(ConfigTreeEquivalent.class, item, null));

                return service;
            }
        } finally {
            lock.unlock();
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<FileStorageService> reference, FileStorageService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<FileStorageService> reference, FileStorageService service) {
        lock.lock();
        try {
            boolean needsUnregistration = false;
            List<ServiceRegistration<?>> regs = registrations;
            if (isRegistered) {
                registrations = null;
                needsUnregistration = true;
                isRegistered = false;
            }
            if (needsUnregistration) {
                if (null != regs) {
                    for (ServiceRegistration<?> reg : regs) {
                        reg.unregister();
                    }
                }
            }
            context.ungetService(reference);
        } finally {
            lock.unlock();
        }
    }

}
