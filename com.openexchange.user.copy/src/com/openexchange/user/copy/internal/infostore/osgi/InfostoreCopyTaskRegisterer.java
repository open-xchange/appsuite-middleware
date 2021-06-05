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

package com.openexchange.user.copy.internal.infostore.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.internal.infostore.InfostoreCopyTask;


/**
 * {@link InfostoreCopyTaskRegisterer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InfostoreCopyTaskRegisterer implements ServiceTrackerCustomizer<QuotaFileStorageService, QuotaFileStorageService> {

    private final BundleContext context;

    private ServiceRegistration<CopyUserTaskService> registration;

    private InfostoreCopyTask copyTask;


    public InfostoreCopyTaskRegisterer(final BundleContext context) {
        super();
        this.context = context;
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public QuotaFileStorageService addingService(final ServiceReference<QuotaFileStorageService> reference) {
        final QuotaFileStorageService service = context.getService(reference);
        copyTask = new InfostoreCopyTask(service);
        registration = context.registerService(CopyUserTaskService.class, copyTask, null);
        return service;
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void modifiedService(final ServiceReference<QuotaFileStorageService> reference, final QuotaFileStorageService service) {
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void removedService(final ServiceReference<QuotaFileStorageService> reference, final QuotaFileStorageService service) {
        registration.unregister();
        context.ungetService(reference);
        copyTask = null;
    }

}
