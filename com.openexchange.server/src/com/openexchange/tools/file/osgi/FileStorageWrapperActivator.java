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

package com.openexchange.tools.file.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;

/**
 * {@link FileStorageWrapperActivator} - The activator that wraps old legacy file storage layer around new
 * <code>com.openexchange.filestore</code> API.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileStorageWrapperActivator implements BundleActivator {

    private ServiceTracker<FileStorageService, FileStorageService> fsTracker;
    private ServiceTracker<QuotaFileStorageService, QuotaFileStorageService> qfsTracker;

    @Override
    public void start(BundleContext context) throws Exception {
        ServiceTracker<FileStorageService, FileStorageService> fsTracker = new ServiceTracker<FileStorageService, FileStorageService>(context, FileStorageService.class, new FSTrackerCustomizer(context));
        this.fsTracker = fsTracker;

        ServiceTracker<QuotaFileStorageService, QuotaFileStorageService> qfsTracker = new ServiceTracker<QuotaFileStorageService, QuotaFileStorageService>(context, QuotaFileStorageService.class, new QFSTrackerCustomizer(context));
        this.qfsTracker = qfsTracker;

        fsTracker.open();
        qfsTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        ServiceTracker<FileStorageService, FileStorageService> fsTracker = this.fsTracker;
        if (null != fsTracker) {
            fsTracker.close();
            this.fsTracker = null;
        }

        ServiceTracker<QuotaFileStorageService, QuotaFileStorageService> qfsTracker = this.qfsTracker;
        if (null != qfsTracker) {
            qfsTracker.close();
            this.qfsTracker = null;
        }
    }

    private static class FSTrackerCustomizer implements ServiceTrackerCustomizer<FileStorageService, FileStorageService> {

        private final BundleContext context;

        public FSTrackerCustomizer(final BundleContext context) {
            this.context = context;
        }

        @Override
        public FileStorageService addingService(ServiceReference<FileStorageService> reference) {
            final FileStorageService service = context.getService(reference);
            FileStorage.setFileStorageStarter(service);
            return service;
        }

        @Override
        public void modifiedService(ServiceReference<FileStorageService> reference, FileStorageService service) {
            // Nothing to do here

        }

        @Override
        public void removedService(ServiceReference<FileStorageService> reference, FileStorageService service) {
            FileStorage.setFileStorageStarter(null);
            context.ungetService(reference);
        }

    }

    private static class QFSTrackerCustomizer implements ServiceTrackerCustomizer<QuotaFileStorageService, QuotaFileStorageService> {

        private final BundleContext context;

        public QFSTrackerCustomizer(final BundleContext context) {
            this.context = context;
        }

        @Override
        public QuotaFileStorageService addingService(ServiceReference<QuotaFileStorageService> reference) {
            QuotaFileStorageService service = context.getService(reference);
            QuotaFileStorage.setQuotaFileStorageStarter(service);
            return service;
        }

        @Override
        public void modifiedService(ServiceReference<QuotaFileStorageService> reference, QuotaFileStorageService service) {
            // Nothing to do here

        }

        @Override
        public void removedService(ServiceReference<QuotaFileStorageService> reference, QuotaFileStorageService service) {
            QuotaFileStorage.setQuotaFileStorageStarter(null);
            context.ungetService(reference);
        }

    }

}
