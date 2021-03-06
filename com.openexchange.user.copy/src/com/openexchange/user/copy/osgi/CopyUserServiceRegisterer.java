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

package com.openexchange.user.copy.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.UserCopyService;
import com.openexchange.user.copy.internal.UserCopyServiceImpl;

/**
 * {@link CopyUserServiceRegisterer}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class CopyUserServiceRegisterer implements ServiceTrackerCustomizer<CopyUserTaskService, CopyUserTaskService> {

    private final BundleContext context;

    private volatile UserCopyServiceImpl copyService;

    private volatile ServiceRegistration<UserCopyService> registration;

    public CopyUserServiceRegisterer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public CopyUserTaskService addingService(final ServiceReference<CopyUserTaskService> reference) {
        final CopyUserTaskService taskService = context.getService(reference);
        UserCopyServiceImpl copyService = this.copyService;
        if (copyService == null) {
            copyService = new UserCopyServiceImpl();
            this.copyService = copyService;
            registration = context.registerService(UserCopyService.class, copyService, null);
        }

        copyService.addTask(taskService);
        return taskService;
    }

    @Override
    public void modifiedService(final ServiceReference<CopyUserTaskService> reference, final CopyUserTaskService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<CopyUserTaskService> reference, final CopyUserTaskService taskService) {
        final UserCopyServiceImpl copyService = this.copyService;
        if (copyService != null) {
            copyService.removeTask(taskService);
            if (0 == copyService.getTaskCount()) {
                final ServiceRegistration<UserCopyService> registration = this.registration;
                if (null != registration) {
                    registration.unregister();
                    this.registration = null;
                }
                this.copyService = null;
            }
        }
        context.ungetService(reference);
    }
}
