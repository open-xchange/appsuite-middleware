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

package com.openexchange.authentication.kerberos.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.kerberos.impl.KerberosAuthentication;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.user.UserService;

/**
 * Dependently registers the AuthenticationService.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class AuthenticationRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationRegisterer.class);

    private final BundleContext context;
    private ServiceRegistration<AuthenticationService> registration;
    private KerberosService kerberosService;
    private ContextService contextService;
    private UserService userService;
    private ConfigurationService configService;

    public AuthenticationRegisterer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public synchronized Object addingService(ServiceReference<Object> reference) {
        final Object obj = context.getService(reference);
        final boolean needsRegistration;
        {
            if (obj instanceof KerberosService) {
                kerberosService = (KerberosService) obj;
            } else if (obj instanceof ContextService) {
                contextService = (ContextService) obj;
            } else if (obj instanceof UserService) {
                userService = (UserService) obj;
            } else if (obj instanceof ConfigurationService) {
                configService = (ConfigurationService) obj;
            }
            needsRegistration = null != kerberosService && null != contextService && null != userService && null != configService && registration == null;
        }
        if (needsRegistration) {
            LOG.info("Registering Kerberos authentication service.");
            registration = context.registerService(AuthenticationService.class, new KerberosAuthentication(
                kerberosService,
                contextService,
                userService,
                configService), null);
        }
        return obj;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // Nothing to do.
    }

    @Override
    public synchronized void removedService(ServiceReference<Object> reference, Object service) {
        ServiceRegistration<AuthenticationService> unregister = null;
        {
            if (service instanceof ContextService) {
                contextService = null;
            } else if (service instanceof UserService) {
                userService = null;
            } else if (service instanceof KerberosService) {
                kerberosService = null;
            } else if (service instanceof ConfigurationService) {
                configService = null;
            }
            if (registration != null && (null == contextService || null == userService || null == kerberosService)) {
                unregister = registration;
                registration = null;
            }
        }
        if (null != unregister) {
            LOG.info("Unregistering Kerberos authentication service.");
            unregister.unregister();
        }
        context.ungetService(reference);
    }
}
