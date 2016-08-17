/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
