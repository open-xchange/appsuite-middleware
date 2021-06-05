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

package com.openexchange.authentication.service.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.service.Authentication;

/**
 * Authentication service tracker putting the service into the static authentication class.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AuthenticationCustomizer implements ServiceTrackerCustomizer<AuthenticationService, AuthenticationService> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AuthenticationCustomizer.class);

    private final BundleContext context;

    public AuthenticationCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public AuthenticationService addingService(final ServiceReference<AuthenticationService> reference) {
        final AuthenticationService auth = context.getService(reference);
        if (Authentication.setService(auth)) {
            return auth;
        }
        LOG.error("Several authentication services found. Remove all except one!");
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<AuthenticationService> reference, final AuthenticationService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<AuthenticationService> reference, final AuthenticationService service) {
        final AuthenticationService auth = service;
        if (!Authentication.dropService(auth)) {
            LOG.error("Removed authentication services was not active!");
        }
        context.ungetService(reference);
    }
}
