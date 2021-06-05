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

package com.openexchange.user.copy.internal.usecount.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.internal.usecount.UseCountCopyTask;


/**
 * {@link UseCountCopyActivator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class UseCountCopyActivator implements BundleActivator {

    private ServiceRegistration<CopyUserTaskService> serviceRegistration;

    /**
     * Initializes a new {@link UseCountCopyActivator}.
     */
    public UseCountCopyActivator() {
        super();
    }

    @Override
    public synchronized void start(BundleContext context) throws Exception {
        serviceRegistration = context.registerService(CopyUserTaskService.class, new UseCountCopyTask(), null);
    }

    @Override
    public synchronized void stop(BundleContext context) throws Exception {
        if (null != serviceRegistration) {
            serviceRegistration.unregister();
        }
    }

}
