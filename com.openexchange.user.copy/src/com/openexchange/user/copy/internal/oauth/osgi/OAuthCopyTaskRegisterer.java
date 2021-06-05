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

package com.openexchange.user.copy.internal.oauth.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.internal.oauth.OAuthCopyTask;


/**
 * {@link OAuthCopyTaskRegisterer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class OAuthCopyTaskRegisterer implements ServiceTrackerCustomizer<IDGeneratorService, IDGeneratorService> {

    private final BundleContext context;

    private ServiceRegistration<CopyUserTaskService> registerService;

    private OAuthCopyTask task;


    public OAuthCopyTaskRegisterer(final BundleContext context) {
        super();
        this.context = context;
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public IDGeneratorService addingService(final ServiceReference<IDGeneratorService> reference) {
        final IDGeneratorService service = context.getService(reference);
        task = new OAuthCopyTask(service);
        registerService = context.registerService(CopyUserTaskService.class, task, null);

        return service;
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void modifiedService(final ServiceReference<IDGeneratorService> reference, final IDGeneratorService service) {
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void removedService(final ServiceReference<IDGeneratorService> reference, final IDGeneratorService service) {
        if (registerService != null) {
            registerService.unregister();
            task = null;
            context.ungetService(reference);
        }
    }

}
