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

package com.openexchange.server.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.authorization.Authorization;
import com.openexchange.authorization.AuthorizationService;

/**
 * Puts a found imstance of {@link AuthorizationService} into the according registry {@link Authorization}.
 *
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 */
public class AuthorizationCustomizer implements ServiceTrackerCustomizer<AuthorizationService, AuthorizationService> {

    private final BundleContext context;

    /**
     * Initializes a new {@link AuthorizationCustomizer}.
     *
     * @param context The bundle context
     */
    public AuthorizationCustomizer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public AuthorizationService addingService(ServiceReference<AuthorizationService> reference) {
        AuthorizationService auth = context.getService(reference);
        if (Authorization.setService(auth)) {
            return auth;
        }

        context.ungetService(reference);
        org.slf4j.LoggerFactory.getLogger(AuthorizationCustomizer.class).error("Several authorization services found. Remove all except one!");
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<AuthorizationService> reference, AuthorizationService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<AuthorizationService> reference, AuthorizationService service) {
        if (null != service) {
            if (false == Authorization.dropService(service)) {
                org.slf4j.LoggerFactory.getLogger(AuthorizationCustomizer.class).error("Disappearing authorization service was not active!");
            }
            context.ungetService(reference);
        }
    }

}
