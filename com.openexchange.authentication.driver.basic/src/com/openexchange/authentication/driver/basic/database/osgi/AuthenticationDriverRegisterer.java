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

package com.openexchange.authentication.driver.basic.database.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.authentication.AuthenticationDriver;
import com.openexchange.authentication.BasicAuthenticationService;
import com.openexchange.authentication.driver.basic.database.DatabaseAuthenticationDriver;

/**
 * Registers the database authentication driver.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AuthenticationDriverRegisterer implements ServiceTrackerCustomizer<BasicAuthenticationService,BasicAuthenticationService> {

    private final BundleContext context;
    private volatile ServiceRegistration<AuthenticationDriver> registration;

    /**
     * Initializes a new {@link AuthenticationDriverRegisterer}.
     *
     * @param context The bundle context
     */
    public AuthenticationDriverRegisterer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public BasicAuthenticationService addingService(ServiceReference<BasicAuthenticationService> reference) {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthenticationDriverRegisterer.class);
        BasicAuthenticationService basicAuthenticationService = context.getService(reference);

        DatabaseAuthenticationDriver driver = new DatabaseAuthenticationDriver(basicAuthenticationService);
        Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
        properties.put(AuthenticationDriver.PROPERTY_ID, driver.getId());
        registration = context.registerService(AuthenticationDriver.class, driver, properties);
        logger.info("Registered database authentication driver.");

        return basicAuthenticationService;
    }

    @Override
    public void modifiedService(ServiceReference<BasicAuthenticationService> reference, BasicAuthenticationService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<BasicAuthenticationService> reference, BasicAuthenticationService service) {
        try {
            ServiceRegistration<AuthenticationDriver> registration = this.registration;
            if (null != registration) {
                this.registration = null;
                registration.unregister();
            }
        } finally {
            context.ungetService(reference);
        }
    }

}
