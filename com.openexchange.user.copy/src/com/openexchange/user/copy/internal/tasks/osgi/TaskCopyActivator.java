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

package com.openexchange.user.copy.internal.tasks.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.user.UserService;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.internal.tasks.TaskCopyTask;


/**
 * {@link TaskCopyActivator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @param <T>
 */
public class TaskCopyActivator implements BundleActivator {

    private ServiceTracker<UserService, UserService> tracker;

    /**
     * Initializes a new {@link TaskCopyActivator}.
     */
    public TaskCopyActivator() {
        super();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        tracker = new ServiceTracker<UserService, UserService>(context, UserService.class.getName(), new ServiceTrackerCustomizer<UserService, UserService>() {

            private ServiceRegistration<CopyUserTaskService> registration;

            @Override
            public UserService addingService(final ServiceReference<UserService> reference) {
                final UserService service = context.getService(reference);
                registration = context.registerService(CopyUserTaskService.class, new TaskCopyTask(service), null);
                return service;
            }

            @Override
            public void modifiedService(final ServiceReference<UserService> reference, final UserService service) {
                // Nope
            }

            @Override
            public void removedService(final ServiceReference<UserService> reference, final UserService service) {
                registration.unregister();
                context.ungetService(reference);
            }
        });
        tracker.open();
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        if (tracker != null) {
            tracker.close();
        }
    }

}
