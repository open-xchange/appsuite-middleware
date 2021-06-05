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

package com.openexchange.systemname.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.systemname.SystemNameService;
import com.openexchange.systemname.internal.JVMRouteSystemNameImpl;

/**
 * {@link SystemNameServiceRegisterer}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class SystemNameServiceRegisterer implements ServiceTrackerCustomizer<ConfigurationService,ConfigurationService> {

    private final BundleContext context;
    private ServiceRegistration<SystemNameService> registration;

    public SystemNameServiceRegisterer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public ConfigurationService addingService(final ServiceReference<ConfigurationService> reference) {
        final ConfigurationService configService = context.getService(reference);
        registration = context.registerService(SystemNameService.class, new JVMRouteSystemNameImpl(configService), null);
        return configService;
    }

    @Override
    public void modifiedService(final ServiceReference<ConfigurationService> reference, final ConfigurationService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<ConfigurationService> reference, final ConfigurationService service) {
        registration.unregister();
        context.ungetService(reference);
    }
}
