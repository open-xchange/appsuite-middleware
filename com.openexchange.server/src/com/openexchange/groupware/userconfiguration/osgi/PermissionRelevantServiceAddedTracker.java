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

package com.openexchange.groupware.userconfiguration.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.service.PermissionAvailabilityService;
import com.openexchange.passwordchange.PasswordChangeService;

/**
 * The {@link PermissionRelevantServiceAddedTracker} is used to track services which availability have got impact for the user interface (e.
 * g. {@link PasswordChangeService}). If the service is available it the availability will be registered for the
 * {@link PermissionAvailabilityService} to analyze it for the user interface.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class PermissionRelevantServiceAddedTracker<S> extends ServiceTracker<S, S> {

    private volatile ServiceRegistration<PermissionAvailabilityService> serviceRegistration;

    /**
     * Initializes a new {@link PermissionRelevantServiceAddedTracker}.
     *
     * @param context - the context
     * @param clazz - the class to track
     */
    public PermissionRelevantServiceAddedTracker(BundleContext context, Class<S> clazz) {
        super(context, clazz, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public S addingService(ServiceReference<S> reference) {
        final S service = context.getService(reference);
        serviceRegistration = context.registerService(PermissionAvailabilityService.class, new PermissionAvailabilityService(Permission.EDIT_PASSWORD), null);
        return service;
    }

    @Override
    public void removedService(ServiceReference<S> reference, S service) {
        final ServiceRegistration<PermissionAvailabilityService> serviceRegistration = this.serviceRegistration;
        if (null != serviceRegistration) {
            serviceRegistration.unregister();
            this.serviceRegistration = null;
        }
        super.removedService(reference, service);
    }

}
