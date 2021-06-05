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

package com.openexchange.capabilities.osgi;

import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.service.PermissionAvailabilityService;
import com.openexchange.java.ConcurrentEnumMap;

/**
 * {@link PermissionAvailabilityServiceRegistry} - A registry service tracker for <code>PermissionAvailabilityService</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PermissionAvailabilityServiceRegistry extends ServiceTracker<PermissionAvailabilityService, PermissionAvailabilityService> {

    private final ConcurrentEnumMap<Permission, PermissionAvailabilityService> services;

    /**
     * Initializes a new {@link PermissionAvailabilityServiceRegistry}.
     *
     * @param context The bundle context
     * @param clazz The service class
     */
    public PermissionAvailabilityServiceRegistry(final BundleContext context) {
        super(context, PermissionAvailabilityService.class, null);
        services = new ConcurrentEnumMap<Permission, PermissionAvailabilityService>(Permission.class);
    }

    /**
     * Gets the service list
     *
     * @return The service list
     */
    public Map<Permission, PermissionAvailabilityService> getServiceMap() {
        return services;
    }

    @Override
    public PermissionAvailabilityService addingService(final ServiceReference<PermissionAvailabilityService> reference) {
        final PermissionAvailabilityService service = context.getService(reference);
        if (null == services.putIfAbsent(service.getRegisteredPermission(), service)) {
            return service;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void removedService(final ServiceReference<PermissionAvailabilityService> reference, final PermissionAvailabilityService service) {
        services.remove(service.getRegisteredPermission());
        context.ungetService(reference);
    }

}
