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

package com.openexchange.net.ssl.config.impl.osgi;

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.net.ssl.config.impl.internal.UserAwareSSLConfigurationImpl;
import com.openexchange.net.ssl.config.impl.jslob.AcceptUntrustedCertificatesJSLobEntry;
import com.openexchange.net.ssl.config.impl.jslob.UserCanManageOwnCertificatesJSLobEntry;
import com.openexchange.osgi.Tools;
import com.openexchange.user.UserService;

/**
 * {@link UserAwareSSLConfigurationServiceRegisterer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class UserAwareSSLConfigurationServiceRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private final BundleContext context;
    private final ConfigViewFactory factory;

    private UserService userService = null;
    private ContextService contextService = null;
    private List<ServiceRegistration<?>> registrations = null;
    private ConfigurationService configService;

    /**
     * Initializes a new {@link UserAwareSSLConfigurationServiceRegisterer}.
     *
     * @param factory The config-cascade service
     * @param context The bundle context
     */
    public UserAwareSSLConfigurationServiceRegisterer(ConfigViewFactory factory, ConfigurationService configService, BundleContext context) {
        super();
        this.factory = factory;
        this.configService = configService;
        this.context = context;
    }

    /**
     * Gets the filter expression for this registerer.
     *
     * @return The filter expression
     */
    public Filter getFilter() throws InvalidSyntaxException {
        return Tools.generateServiceFilter(context, UserService.class, ContextService.class);
    }

    @Override
    public synchronized Object addingService(ServiceReference<Object> reference) {
        // Exclusively accessed...
        Object service = context.getService(reference);
        if (service instanceof UserService) {
            this.userService = (UserService) service;
            update();
        } else if (service instanceof ContextService) {
            this.contextService = (ContextService) service;
            update();
        } else {
            // Of no need
            context.ungetService(reference);
            return null;
        }

        return service;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // Ignore
    }

    @Override
    public synchronized void removedService(ServiceReference<Object> reference, Object service) {
        // Exclusively accessed...
        if (service instanceof UserService) {
            this.userService = null;
            update();
        } else if (service instanceof ContextService) {
            this.contextService = null;
            update();
        }

        context.ungetService(reference);
    }

    private void update() {
        ContextService contextService = this.contextService;
        if (null == contextService) {
            // Not all needed service available
            optUnregister();
            return;
        }

        UserService userService = this.userService;
        if (null == userService) {
            // Not all needed service available
            optUnregister();
            return;
        }

        List<ServiceRegistration<?>> registrations = this.registrations;
        if (null != registrations) {
            // Already registered
            return;
        }

        UserAwareSSLConfigurationImpl userAwareSSLConfigurationImpl = new UserAwareSSLConfigurationImpl(userService, contextService, configService, factory);
        AcceptUntrustedCertificatesJSLobEntry acceptUntrustedCertsJSLobEntry = new AcceptUntrustedCertificatesJSLobEntry(contextService, userAwareSSLConfigurationImpl);
        UserCanManageOwnCertificatesJSLobEntry userCanManageOwnCertsJSLobEntry = new UserCanManageOwnCertificatesJSLobEntry(userAwareSSLConfigurationImpl);

        registrations = new ArrayList<>(2);
        this.registrations = registrations;
        registrations.add(context.registerService(UserAwareSSLConfigurationService.class, userAwareSSLConfigurationImpl, null));
        registrations.add(context.registerService(JSlobEntry.class, acceptUntrustedCertsJSLobEntry, null));
        registrations.add(context.registerService(JSlobEntry.class, userCanManageOwnCertsJSLobEntry, null));
    }

    private void optUnregister() {
        List<ServiceRegistration<?>> registrations = this.registrations;
        if (null != registrations) {
            this.registrations = null;
            for (ServiceRegistration<?> registration : registrations) {
                registration.unregister();
            }
        }
    }

}
