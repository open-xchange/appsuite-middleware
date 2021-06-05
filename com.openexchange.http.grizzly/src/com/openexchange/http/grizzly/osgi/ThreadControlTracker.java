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

package com.openexchange.http.grizzly.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.http.grizzly.util.ThreadControlReference;
import com.openexchange.startup.ThreadControlService;


/**
 * {@link ThreadControlTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadControlTracker implements ServiceTrackerCustomizer<ThreadControlService, ThreadControlService> {

    private final BundleContext context;

    /**
     * Initializes a new {@link ThreadControlTracker}.
     */
    public ThreadControlTracker(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public ThreadControlService addingService(ServiceReference<ThreadControlService> reference) {
        ThreadControlService service = context.getService(reference);
        ThreadControlReference.setThreadControlService(service);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<ThreadControlService> reference, ThreadControlService service) {
        // Nothing to do
    }

    @Override
    public void removedService(ServiceReference<ThreadControlService> reference, ThreadControlService service) {
        ThreadControlReference.setThreadControlService(null);
        context.ungetService(reference);
    }

}
