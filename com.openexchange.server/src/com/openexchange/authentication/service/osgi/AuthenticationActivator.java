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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.context.ContextService;
import com.openexchange.user.UserService;

/**
 * Activator to start {@link ServiceTracker} to listen for {@link AutoLoginAuthenticationService}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class AuthenticationActivator implements BundleActivator {

    private volatile ServiceTracker<AuthenticationService, AuthenticationService> authTracker;
    private volatile ServiceTracker<Object, Object> basicAuthTracker;

    /**
     * Initializes a new {@link AuthenticationActivator}.
     */
    public AuthenticationActivator() {
        super();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        try {
            {
                ServiceTracker<AuthenticationService, AuthenticationService> tracker = new ServiceTracker<AuthenticationService, AuthenticationService>(context, AuthenticationService.class.getName(), new AuthenticationCustomizer(context));
                this.authTracker = tracker;
                tracker.open();
            }

            {
                Filter filter = context.createFilter("(|(" + Constants.OBJECTCLASS + '=' + ContextService.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + UserService.class.getName() + "))");
                BasicAuthenticationRegisterer registerer = new BasicAuthenticationRegisterer(context);
                ServiceTracker<Object, Object> tracker = new ServiceTracker<Object, Object>(context, filter, registerer);
                this.basicAuthTracker = tracker;
                tracker.open();
            }
        } catch (Exception e) {
            final Logger logger = org.slf4j.LoggerFactory.getLogger(AuthenticationActivator.class);
            logger.error("Failed to start-up bundle {}", context.getBundle().getSymbolicName(), e);
            throw e;
        }
    }

    @Override
    public void stop(BundleContext context) {
        ServiceTracker<AuthenticationService, AuthenticationService> authTracker = this.authTracker;
        if (null != authTracker) {
            authTracker.close();
            this.authTracker = null;
        }

        ServiceTracker<Object, Object> basicAuthTracker = this.basicAuthTracker;
        if (null != basicAuthTracker) {
            basicAuthTracker.close();
            this.basicAuthTracker = null;
        }
    }

}
