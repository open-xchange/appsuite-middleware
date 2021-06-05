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

package com.openexchange.authentication.database.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.authentication.AuthenticationDriver;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.database.impl.DatabaseAuthentication;

/**
 * Dependently registers the AuthenticationService.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class AuthenticationRegisterer implements ServiceTrackerCustomizer<AuthenticationDriver,AuthenticationDriver> {

    private final BundleContext context;

    private ServiceRegistration<AuthenticationService> registration; // Guarded by synchronized

    /**
     * Initializes a new {@link AuthenticationRegisterer}.
     *
     * @param context The bundle context
     */
    public AuthenticationRegisterer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public synchronized AuthenticationDriver addingService(ServiceReference<AuthenticationDriver> reference) {
        String driverId = (String) reference.getProperty(AuthenticationDriver.PROPERTY_ID);
        if (!"database".equals(driverId)) {
            return null;
        }

        AuthenticationDriver databaseAuthenticationDriver = context.getService(reference);
        registration = context.registerService(AuthenticationService.class, new DatabaseAuthentication(databaseAuthenticationDriver), null);
        org.slf4j.LoggerFactory.getLogger(AuthenticationRegisterer.class).info("Registered database authentication service.");
        return databaseAuthenticationDriver;
    }

    @Override
    public void modifiedService(ServiceReference<AuthenticationDriver> reference, AuthenticationDriver driver) {
        // Nothing to do.
    }

    @Override
    public synchronized void removedService(ServiceReference<AuthenticationDriver> reference, AuthenticationDriver driver) {
        String driverId = (String) reference.getProperty(AuthenticationDriver.PROPERTY_ID);
        if (!"database".equals(driverId)) {
            context.ungetService(reference);
            return;
        }

        ServiceRegistration<AuthenticationService> registration = this.registration;
        if (null != registration) {
            registration.unregister();
            this.registration = null;
        }

        context.ungetService(reference);
    }

}
