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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.authentication.BasicAuthenticationService;
import com.openexchange.authentication.basic.DefaultBasicAuthentication;
import com.openexchange.authentication.service.Authentication;
import com.openexchange.context.ContextService;
import com.openexchange.user.UserService;

/**
 * Dependently registers the AuthenticationService.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class BasicAuthenticationRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BasicAuthenticationRegisterer.class);

    private final BundleContext context;
    private final Lock lock;

    private ServiceRegistration<BasicAuthenticationService> registration;
    private ContextService contextService;
    private UserService userService;

    /**
     * Initializes a new {@link BasicAuthenticationRegisterer}.
     *
     * @param context The bundle context
     */
    public BasicAuthenticationRegisterer(BundleContext context) {
        super();
        this.context = context;
        lock = new ReentrantLock();
    }

    @Override
    public Object addingService(final ServiceReference<Object> reference) {
        Object obj = context.getService(reference);
        lock.lock();
        try {
            if (obj instanceof ContextService) {
                contextService = (ContextService) obj;
            }
            if (obj instanceof UserService) {
                userService = (UserService) obj;
            }
            boolean needsRegistration = null != contextService && null != userService && registration == null;
            if (needsRegistration) {
                LOG.info("Registering default basic authentication service.");
                DefaultBasicAuthentication basicAuthentication = new DefaultBasicAuthentication(contextService, userService);
                registration = context.registerService(BasicAuthenticationService.class, basicAuthentication, null);
                Authentication.setBasicService(basicAuthentication);
            }
        } finally {
            lock.unlock();
        }
        return obj;
    }

    @Override
    public void modifiedService(final ServiceReference<Object> reference, final Object service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<Object> reference, final Object service) {
        ServiceRegistration<?> unregister = null;
        lock.lock();
        try {
            if (service instanceof ContextService) {
                contextService = null;
            }
            if (service instanceof UserService) {
                userService = null;
            }
            if (registration != null && (contextService == null || userService == null)) {
                unregister = registration;
                this.registration = null;
            }
            if (null != unregister) {
                LOG.info("Unregistering default basic authentication service.");
                unregister.unregister();
                Authentication.setBasicService(null);
            }
        } finally {
            lock.unlock();
        }
        context.ungetService(reference);
    }

}
