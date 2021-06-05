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

package com.openexchange.user.copy.internal.attachment.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.internal.attachment.AttachmentCopyTask;


/**
 * {@link AttachmentCopyRegisterer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class AttachmentCopyRegisterer implements ServiceTrackerCustomizer<QuotaFileStorageService, QuotaFileStorageService> {

    private final BundleContext context;
    private ServiceRegistration<CopyUserTaskService> registration;
    private AttachmentCopyTask task;

    /**
     * Initializes a new {@link AttachmentCopyRegisterer}.
     */
    public AttachmentCopyRegisterer(BundleContext context) {
        super();
        this.context = context;
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public QuotaFileStorageService addingService(ServiceReference<QuotaFileStorageService> reference) {
        final QuotaFileStorageService service = context.getService(reference);
        task = new AttachmentCopyTask(service);
        registration = context.registerService(CopyUserTaskService.class, task, null);

        return service;
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void modifiedService(ServiceReference<QuotaFileStorageService> reference, QuotaFileStorageService service) {
        // Nothing
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference<QuotaFileStorageService> reference, QuotaFileStorageService service) {
        registration.unregister();
        context.ungetService(reference);
        task = null;
    }

}
